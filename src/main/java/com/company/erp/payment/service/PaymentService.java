package com.company.erp.payment.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.common.exception.UnauthorizedAccessException;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.entity.Quotation;
import com.company.erp.financial.entity.QuotationStatus;
import com.company.erp.financial.repository.QuotationRepository;
import com.company.erp.payment.dto.request.ConfirmPaymentRequest;
import com.company.erp.payment.dto.request.GenerateBankFileRequest;
import com.company.erp.payment.dto.response.BankFileResponse;
import com.company.erp.payment.dto.response.PaymentStatusResponse;
import com.company.erp.payment.dto.response.PaymentSummaryResponse;
import com.company.erp.payment.entity.Payment;
import com.company.erp.payment.entity.PaymentBatch;
import com.company.erp.payment.entity.PaymentStatus;
import com.company.erp.payment.repository.PaymentBatchRepository;
import com.company.erp.payment.repository.PaymentRepository;
import com.company.erp.financial.dto.response.QuotationSummaryResponse;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentBatchRepository paymentBatchRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankFileService bankFileService;

    /**
     * Create payments for approved quotations
     */
    public List<PaymentSummaryResponse> createPaymentsForApprovedQuotations(List<Long> quotationIds) {
        logger.info("Creating payments for {} approved quotations", quotationIds.size());

        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        List<Quotation> quotations = quotationRepository.findAllById(quotationIds);

        if (quotations.size() != quotationIds.size()) {
            throw new BusinessException("QUOTATIONS_NOT_FOUND",
                    "Some quotations were not found");
        }

        List<Payment> payments = quotations.stream()
                .filter(this::validateQuotationForPayment)
                .map(this::createPaymentFromQuotation)
                .collect(Collectors.toList());

        List<Payment> savedPayments = paymentRepository.saveAll(payments);

        // Update quotation statuses
        quotations.forEach(q -> {
            q.setStatus(QuotationStatus.PAYMENT_FILE_GENERATED);
            quotationRepository.save(q);
        });

        logger.info("Created {} payments successfully", savedPayments.size());

        return savedPayments.stream()
                .map(this::convertToPaymentSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generate bank file for selected payments
     */
    public BankFileResponse generateBankFile(GenerateBankFileRequest request) {
        logger.info("Generating bank file for {} payments, bank: {}",
                request.getPaymentIds().size(), request.getBankName());

        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        List<Payment> payments = paymentRepository.findAllById(request.getPaymentIds());

        if (payments.isEmpty()) {
            throw new BusinessException("NO_PAYMENTS_FOUND", "No payments found for bank file generation");
        }

        // Validate all payments are ready
        validatePaymentsForBankFile(payments, request.getBankName());

        // Create payment batch
        User creator = getUserById(currentUser.getId());
        PaymentBatch batch = new PaymentBatch(request.getBankName(), creator);

        // Add payments to batch
        for (Payment payment : payments) {
            batch.addPayment(payment);
            payment.setStatus(PaymentStatus.FILE_GENERATED);
        }

        PaymentBatch savedBatch = paymentBatchRepository.save(batch);
        paymentRepository.saveAll(payments);

        // Generate the actual file
        BankFileResponse fileResponse = bankFileService.generateBankFile(savedBatch);

        logger.info("Bank file generated successfully for batch: {}", savedBatch.getBatchNumber());

        return fileResponse;
    }

    /**
     * Confirm payment batch sent to bank
     */
    public void confirmPaymentSentToBank(Long batchId, String bankReference) {
        logger.info("Confirming payment batch {} sent to bank with reference: {}", batchId, bankReference);

        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentBatch", "id", batchId));

        if (!batch.canBeSentToBank()) {
            throw new BusinessException("BATCH_NOT_READY",
                    "Payment batch cannot be sent to bank in current status: " + batch.getStatus());
        }

        batch.markAsSentToBank(bankReference);
        paymentBatchRepository.save(batch);

        // Update related quotations
        for (Payment payment : batch.getPayments()) {
            payment.getQuotation().setStatus(QuotationStatus.SENT_TO_BANK);
            quotationRepository.save(payment.getQuotation());
        }

        logger.info("Payment batch {} confirmed as sent to bank", batchId);
    }

    /**
     * Confirm payments completed
     */
    public void confirmPaymentsCompleted(ConfirmPaymentRequest request) {
        logger.info("Confirming {} payments as completed", request.getPaymentIds().size());

        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        List<Payment> payments = paymentRepository.findAllById(request.getPaymentIds());

        for (Payment payment : payments) {
            payment.markAsPaid(request.getBankReference());
            payment.getQuotation().setStatus(QuotationStatus.PAID);

            quotationRepository.save(payment.getQuotation());
        }

        paymentRepository.saveAll(payments);

        // Update batch if all payments in batch are completed
        if (!payments.isEmpty() && payments.get(0).getBatch() != null) {
            PaymentBatch batch = payments.get(0).getBatch();
            boolean allPaymentsCompleted = batch.getPayments().stream()
                    .allMatch(Payment::isPaid);

            if (allPaymentsCompleted) {
                batch.markAsCompleted();
                paymentBatchRepository.save(batch);
            }
        }

        logger.info("Confirmed {} payments as completed", payments.size());
    }

    /**
     * Get payments ready for processing
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummaryResponse> getPaymentsReadyForProcessing(Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        // Get payments that are ready for processing (both PENDING and READY_FOR_PAYMENT)
        Page<Payment> payments = paymentRepository.findByStatusInWithDetails(
                Arrays.asList(PaymentStatus.PENDING, PaymentStatus.READY_FOR_PAYMENT), pageable);

        return payments.map(this::convertToPaymentSummaryResponse);
    }

    /**
     * Get approved quotations ready for payment creation
     */
    @Transactional(readOnly = true)
    public Page<QuotationSummaryResponse> getApprovedQuotationsReadyForPayment(Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        // Get approved quotations that don't have payments created yet
        Page<Quotation> quotations = quotationRepository.findApprovedQuotationsWithoutPayments(pageable);

        return quotations.map(this::convertToQuotationSummaryResponse);
    }

    /**
     * Get payments by status
     */
    @Transactional(readOnly = true)
    public Page<PaymentSummaryResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        Page<Payment> payments = paymentRepository.findByStatusWithDetails(status, pageable);

        return payments.map(this::convertToPaymentSummaryResponse);
    }

    /**
     * Get payment batches
     */
    @Transactional(readOnly = true)
    public Page<PaymentBatch> getPaymentBatches(Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        return paymentBatchRepository.findAllWithDetails(pageable);
    }

    /**
     * Get payment statistics
     */
    @Transactional(readOnly = true)
    public PaymentStatistics getPaymentStatistics() {
        UserPrincipal currentUser = getCurrentUser();
        validatePaymentAccess(currentUser);

        PaymentStatistics stats = new PaymentStatistics();
        
        // Count pending payments (PENDING + READY_FOR_PAYMENT)
        long pendingCount = paymentRepository.countByStatus(PaymentStatus.PENDING) + 
                           paymentRepository.countByStatus(PaymentStatus.READY_FOR_PAYMENT);
        stats.setPendingPayments(pendingCount);
        
        // Count processing payments (PROCESSING + FILE_GENERATED + SENT_TO_BANK)
        long processingCount = paymentRepository.countByStatus(PaymentStatus.PROCESSING) +
                              paymentRepository.countByStatus(PaymentStatus.FILE_GENERATED) +
                              paymentRepository.countByStatus(PaymentStatus.SENT_TO_BANK);
        stats.setProcessingPayments(processingCount);
        
        // Count completed payments
        stats.setCompletedPayments(paymentRepository.countByStatus(PaymentStatus.PAID));
        
        // Count failed payments (FAILED + CANCELLED)
        long failedCount = paymentRepository.countByStatus(PaymentStatus.FAILED) +
                          paymentRepository.countByStatus(PaymentStatus.CANCELLED);
        stats.setFailedPayments(failedCount);

        // Calculate amounts
        BigDecimal pendingAmount = paymentRepository.getTotalAmountByStatus(PaymentStatus.PENDING)
                .add(paymentRepository.getTotalAmountByStatus(PaymentStatus.READY_FOR_PAYMENT));
        stats.setTotalPendingAmount(pendingAmount);
        
        BigDecimal processingAmount = paymentRepository.getTotalAmountByStatus(PaymentStatus.PROCESSING)
                .add(paymentRepository.getTotalAmountByStatus(PaymentStatus.FILE_GENERATED))
                .add(paymentRepository.getTotalAmountByStatus(PaymentStatus.SENT_TO_BANK));
        stats.setTotalProcessingAmount(processingAmount);
        
        stats.setTotalCompletedAmount(paymentRepository.getTotalAmountByStatus(PaymentStatus.PAID));

        return stats;
    }

    // Private helper methods

    private boolean validateQuotationForPayment(Quotation quotation) {
        if (!QuotationStatus.APPROVED.equals(quotation.getStatus())) {
            logger.warn("Quotation {} is not approved for payment, status: {}",
                    quotation.getId(), quotation.getStatus());
            return false;
        }

        // Fix: Use getCreator() instead of getCreatedBy()
        if (quotation.getCreator() == null || quotation.getCreator().getBankDetails() == null) {
            logger.warn("Quotation {} has incomplete payee bank details", quotation.getId());
            return false;
        }

        return true;
    }

    private Payment createPaymentFromQuotation(Quotation quotation) {
        // Fix: Use getCreator() instead of getCreatedBy()
        Payment payment = new Payment(quotation, quotation.getCreator());
        payment.setStatus(PaymentStatus.READY_FOR_PAYMENT);
        return payment;
    }

    private void validatePaymentsForBankFile(List<Payment> payments, String bankName) {
        for (Payment payment : payments) {
            // Fix: Use existing validation methods instead of canBeProcessed()
            if (!PaymentStatus.READY_FOR_PAYMENT.equals(payment.getStatus()) &&
                !PaymentStatus.PENDING.equals(payment.getStatus())) {
                throw new BusinessException("PAYMENT_NOT_READY",
                        "Payment " + payment.getId() + " is not ready for processing");
            }

            if (!payment.hasValidBankDetails()) {
                throw new BusinessException("INVALID_BANK_DETAILS",
                        "Payment " + payment.getId() + " has incomplete bank details");
            }

            if (!bankName.equals(payment.getBankName())) {
                throw new BusinessException("BANK_MISMATCH",
                        "Payment " + payment.getId() + " belongs to different bank");
            }
        }
    }

    private void validatePaymentAccess(UserPrincipal currentUser) {
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            throw new UnauthorizedAccessException("Only Account Managers can process payments");
        }
    }

    private QuotationSummaryResponse convertToQuotationSummaryResponse(Quotation quotation) {
        QuotationSummaryResponse response = new QuotationSummaryResponse();
        response.setId(quotation.getId());
        response.setProjectId(quotation.getProject().getId());
        response.setProjectName(quotation.getProject().getName());
        response.setCreatedBy(quotation.getCreator().getFullName());
        response.setTotalAmount(quotation.getTotalAmount());
        response.setCurrency(quotation.getCurrency());
        response.setStatus(quotation.getStatus().toString());
        response.setCreatedDate(quotation.getCreatedDate());
        response.setSubmittedDate(quotation.getSubmittedDate());
        response.setItemCount(quotation.getItems() != null ? quotation.getItems().size() : 0);
        response.setExceedsBudget(quotation.getTotalAmount().compareTo(quotation.getProject().getRemainingBudget()) > 0);
        return response;
    }

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException("NO_AUTHENTICATED_USER", "No authenticated user found");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private PaymentSummaryResponse convertToPaymentSummaryResponse(Payment payment) {
        PaymentSummaryResponse response = new PaymentSummaryResponse();
        response.setId(payment.getId());
        response.setQuotationId(payment.getQuotation().getId());
        response.setProjectName(payment.getQuotation().getProject().getName());
        response.setPayeeName(payment.getPayee().getFullName());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus().name());
        response.setBankName(payment.getBankName());
        response.setAccountNumber(payment.getAccountNumber());
        response.setCreatedDate(payment.getCreatedDate());
        response.setPaymentDate(payment.getPaymentDate());

        if (payment.getBatch() != null) {
            response.setBatchNumber(payment.getBatch().getBatchNumber());
        }

        return response;
    }

    // Statistics inner class
    public static class PaymentStatistics {
        private long pendingPayments;
        private long processingPayments;
        private long completedPayments;
        private long failedPayments;
        private BigDecimal totalPendingAmount;
        private BigDecimal totalProcessingAmount;
        private BigDecimal totalCompletedAmount;

        // Getters and Setters
        public long getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }

        public long getProcessingPayments() { return processingPayments; }
        public void setProcessingPayments(long processingPayments) { this.processingPayments = processingPayments; }

        public long getCompletedPayments() { return completedPayments; }
        public void setCompletedPayments(long completedPayments) { this.completedPayments = completedPayments; }

        public long getFailedPayments() { return failedPayments; }
        public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }

        public BigDecimal getTotalPendingAmount() { return totalPendingAmount; }
        public void setTotalPendingAmount(BigDecimal totalPendingAmount) { this.totalPendingAmount = totalPendingAmount; }

        public BigDecimal getTotalProcessingAmount() { return totalProcessingAmount; }
        public void setTotalProcessingAmount(BigDecimal totalProcessingAmount) { this.totalProcessingAmount = totalProcessingAmount; }

        public BigDecimal getTotalCompletedAmount() { return totalCompletedAmount; }
        public void setTotalCompletedAmount(BigDecimal totalCompletedAmount) { this.totalCompletedAmount = totalCompletedAmount; }
    }
}