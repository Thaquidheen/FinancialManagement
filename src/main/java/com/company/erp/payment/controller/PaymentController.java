package com.company.erp.payment.controller;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.payment.dto.request.ConfirmPaymentRequest;
import com.company.erp.payment.dto.request.GenerateBankFileRequest;
import com.company.erp.payment.dto.response.BankFileResponse;
import com.company.erp.payment.dto.response.PaymentSummaryResponse;
import com.company.erp.payment.entity.PaymentBatch;
import com.company.erp.payment.entity.PaymentStatus;
import com.company.erp.payment.service.BankFileService;
import com.company.erp.payment.service.PaymentService;
import com.company.erp.financial.dto.response.QuotationSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Management", description = "Saudi bank payment processing and file generation")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BankFileService bankFileService;

    @Operation(summary = "Create payments from approved quotations",
            description = "Create payment records for approved quotations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payments created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quotations or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<PaymentSummaryResponse>> createPayments(
            @RequestBody List<Long> quotationIds) {

        logger.info("Creating payments for {} quotations", quotationIds.size());

        List<PaymentSummaryResponse> payments = paymentService.createPaymentsForApprovedQuotations(quotationIds);

        logger.info("Created {} payments successfully", payments.size());

        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Generate Saudi bank file",
            description = "Generate Excel file in Saudi bank format for selected payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payments or bank validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @PostMapping("/generate-bank-file")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<BankFileResponse> generateBankFile(@Valid @RequestBody GenerateBankFileRequest request) {

        logger.info("Generating bank file for {} payments, bank: {}",
                request.getPaymentIds().size(), request.getBankName());

        BankFileResponse response = paymentService.generateBankFile(request);

        logger.info("Bank file generated successfully: {}", response.getFileName());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download bank file",
            description = "Download the generated Excel file for bank processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "400", description = "Business error - bank file not generated or invalid batch"),
            @ApiResponse(responseCode = "404", description = "Payment batch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/download-bank-file/{batchId}")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Resource> downloadBankFile(
            @Parameter(description = "Payment Batch ID") @PathVariable Long batchId) {

        logger.info("Downloading bank file for batch: {}", batchId);

        try {
            byte[] fileContent = bankFileService.downloadBankFile(batchId);
            String fileName = bankFileService.getBankFileName(batchId);

            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (ResourceNotFoundException e) {
            logger.error("Payment batch not found: {}", batchId, e);
            return ResponseEntity.notFound().build();
        } catch (BusinessException e) {
            logger.error("Business error downloading bank file for batch: {}", batchId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error downloading bank file for batch: {}", batchId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Confirm payment sent to bank",
            description = "Mark payment batch as sent to bank with reference number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment batch confirmed as sent to bank"),
            @ApiResponse(responseCode = "400", description = "Invalid batch status or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Payment batch not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/confirm-sent-to-bank/{batchId}")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> confirmSentToBank(
            @Parameter(description = "Payment Batch ID") @PathVariable Long batchId,
            @RequestParam String bankReference) {

        logger.info("Confirming batch {} sent to bank with reference: {}", batchId, bankReference);

        paymentService.confirmPaymentSentToBank(batchId, bankReference);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment batch confirmed as sent to bank");
        response.put("batchId", batchId.toString());
        response.put("bankReference", bankReference);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm payments completed",
            description = "Mark payments as completed when bank confirms payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments confirmed as completed"),
            @ApiResponse(responseCode = "400", description = "Invalid payment status or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/confirm-completed")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> confirmPaymentsCompleted(
            @Valid @RequestBody ConfirmPaymentRequest request) {

        logger.info("Confirming {} payments as completed", request.getPaymentIds().size());

        paymentService.confirmPaymentsCompleted(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Payments confirmed as completed successfully");
        response.put("processedCount", String.valueOf(request.getPaymentIds().size()));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payments ready for processing",
            description = "Get paginated list of payments ready for bank file generation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/ready-for-processing")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<PaymentSummaryResponse>> getPaymentsReadyForProcessing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.debug("Fetching payments ready for processing - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentSummaryResponse> payments = paymentService.getPaymentsReadyForProcessing(pageable);

        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Get approved quotations ready for payment creation",
            description = "Get approved quotations that don't have payments created yet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotations retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/approved-quotations")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<QuotationSummaryResponse>> getApprovedQuotationsReadyForPayment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "approvedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.debug("Fetching approved quotations ready for payment - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QuotationSummaryResponse> quotations = paymentService.getApprovedQuotationsReadyForPayment(pageable);

        return ResponseEntity.ok(quotations);
    }

    @Operation(summary = "Get payments by status",
            description = "Get paginated list of payments filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<PaymentSummaryResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment Status") @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.debug("Fetching payments by status: {} - page: {}, size: {}", status, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        Page<PaymentSummaryResponse> payments = paymentService.getPaymentsByStatus(paymentStatus, pageable);

        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Get payment batches",
            description = "Get paginated list of payment batches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment batches retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/batches")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<PaymentBatch>> getPaymentBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.debug("Fetching payment batches - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentBatch> batches = paymentService.getPaymentBatches(pageable);

        return ResponseEntity.ok(batches);
    }

    @Operation(summary = "Get payment statistics",
            description = "Get payment processing statistics for dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<PaymentService.PaymentStatistics> getPaymentStatistics() {

        logger.debug("Fetching payment statistics");

        PaymentService.PaymentStatistics statistics = paymentService.getPaymentStatistics();

        return ResponseEntity.ok(statistics);
    }

    // Convenience endpoints for Saudi banks
    @Operation(summary = "Get payments by bank",
            description = "Get payments filtered by specific Saudi bank")
    @GetMapping("/by-bank/{bankName}")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<PaymentSummaryResponse>> getPaymentsByBank(
            @Parameter(description = "Bank Name") @PathVariable String bankName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        logger.debug("Fetching payments by bank: {}", bankName);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").ascending());
        Page<PaymentSummaryResponse> payments = paymentService.getPaymentsByStatus(
                PaymentStatus.READY_FOR_PAYMENT, pageable);

        // Filter by bank name (could be optimized with repository method)
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Get payment dashboard",
            description = "Get comprehensive payment dashboard data")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentDashboard() {
        logger.debug("Fetching payment dashboard data");

        PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics();
        
        // Get recent payments
        Pageable recentPageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());
        Page<PaymentSummaryResponse> recentPayments = paymentService.getPaymentsReadyForProcessing(recentPageable);
        
        // Get pending batches
        Pageable batchPageable = PageRequest.of(0, 5, Sort.by("createdDate").descending());
        Page<PaymentBatch> pendingBatches = paymentService.getPaymentBatches(batchPageable);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("statistics", stats);
        dashboard.put("recentPayments", recentPayments.getContent());
        dashboard.put("pendingBatches", pendingBatches.getContent());
        dashboard.put("alerts", new ArrayList<>()); // Empty alerts for now
        // Quick actions will be generated on frontend based on statistics

        return ResponseEntity.ok(dashboard);
    }
}