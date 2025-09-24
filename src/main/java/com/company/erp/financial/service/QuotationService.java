package com.company.erp.financial.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.common.exception.UnauthorizedAccessException;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.dto.request.CreateQuotationRequest;
import com.company.erp.financial.dto.request.SubmitQuotationRequest;
import com.company.erp.financial.dto.response.QuotationResponse;
import com.company.erp.financial.dto.response.QuotationSummaryResponse;
import com.company.erp.financial.entity.Quotation;
import com.company.erp.financial.entity.QuotationItem;
import com.company.erp.financial.entity.QuotationStatus;
import com.company.erp.financial.repository.QuotationItemRepository;
import com.company.erp.financial.repository.QuotationRepository;
import com.company.erp.project.entity.Project;
import com.company.erp.project.repository.ProjectRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuotationService {

    private static final Logger logger = LoggerFactory.getLogger(QuotationService.class);

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private QuotationItemRepository quotationItemRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create new quotation with line items
     */
    public QuotationResponse createQuotation(CreateQuotationRequest request) {
        logger.info("Creating new quotation for project: {}", request.getProjectId());

        UserPrincipal currentUser = getCurrentUser();

        // Validate project exists and user has access
        Project project = validateProjectAccess(request.getProjectId(), currentUser.getId());
        User creator = getUserById(currentUser.getId());

        // Create quotation entity
        Quotation quotation = new Quotation(project, creator);
        quotation.setDescription(request.getDescription());
        quotation.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SAR");
        quotation.setStatus(QuotationStatus.DRAFT);

        // Save quotation first to get ID
        Quotation savedQuotation = quotationRepository.save(quotation);

        // Add line items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            addQuotationItems(savedQuotation, request.getItems());
            // Update total amount after adding items
            savedQuotation.updateTotalAmount();
            quotationRepository.save(savedQuotation);
        }

        logger.info("Quotation created successfully with ID: {} and total amount: SAR {}",
                savedQuotation.getId(), savedQuotation.getTotalAmount());

        return convertToQuotationResponse(savedQuotation);
    }

    /**
     * Update existing quotation (only if in DRAFT status)
     */
    public QuotationResponse updateQuotation(Long quotationId, CreateQuotationRequest request) {
        logger.info("Updating quotation with ID: {}", quotationId);

        UserPrincipal currentUser = getCurrentUser();
        Quotation quotation = getQuotationWithAccess(quotationId, currentUser.getId());

        // Can only update draft quotations
        if (!quotation.canBeEdited()) {
            throw new BusinessException("QUOTATION_NOT_EDITABLE",
                    "Cannot update quotation in " + quotation.getStatus() + " status");
        }

        // Update basic fields
        quotation.setDescription(request.getDescription());

        // Clear existing items and add new ones
        quotation.getItems().clear();
        quotationItemRepository.deactivateByQuotationId(quotationId);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            addQuotationItems(quotation, request.getItems());
        }

        // Update total amount
        quotation.updateTotalAmount();
        Quotation savedQuotation = quotationRepository.save(quotation);

        logger.info("Quotation {} updated successfully with new total: SAR {}",
                quotationId, savedQuotation.getTotalAmount());

        return convertToQuotationResponse(savedQuotation);
    }

    /**
     * Submit quotation for approval
     */
    public QuotationResponse submitQuotation(Long quotationId, SubmitQuotationRequest request) {
        logger.info("Submitting quotation {} for approval", quotationId);

        UserPrincipal currentUser = getCurrentUser();
        Quotation quotation = getQuotationWithAccess(quotationId, currentUser.getId());

        // Can only submit draft quotations
        if (!quotation.canBeSubmitted()) {
            throw new BusinessException("QUOTATION_NOT_SUBMITTABLE",
                    "Quotation cannot be submitted in " + quotation.getStatus() + " status");
        }

        // Validate quotation has items
        if (quotation.getItems().isEmpty()) {
            throw new BusinessException("QUOTATION_EMPTY",
                    "Cannot submit quotation without line items");
        }

        // Validate against project budget
        validateBudgetCompliance(quotation);

        // Update quotation status
        quotation.submit(request.getSubmissionNotes());
        Quotation savedQuotation = quotationRepository.save(quotation);

        // TODO: Trigger notification to account managers
        logger.info("Notification would be sent to account managers for quotation {}", quotationId);

        logger.info("Quotation {} submitted successfully for SAR {}",
                quotationId, savedQuotation.getTotalAmount());

        return convertToQuotationResponse(savedQuotation);
    }

    /**
     * Approve quotation (Account Managers only)
     */
    public QuotationResponse approveQuotation(Long quotationId, String approvalComments) {
        logger.info("Approving quotation {}", quotationId);

        UserPrincipal currentUser = getCurrentUser();

        // Only Account Managers and Super Admin can approve
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            throw new UnauthorizedAccessException("Only Account Managers can approve quotations");
        }

        Quotation quotation = quotationRepository.findByIdWithProjectAndItems(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quotationId));

        if (!quotation.canBeApproved()) {
            throw new BusinessException("QUOTATION_NOT_APPROVABLE",
                    "Quotation cannot be approved in " + quotation.getStatus() + " status");
        }

        User approver = getUserById(currentUser.getId());
        quotation.approve(approver);

        // Update project budget
        updateProjectBudget(quotation);

        Quotation savedQuotation = quotationRepository.save(quotation);

        logger.info("Quotation {} approved successfully by {} for SAR {}",
                quotationId, approver.getFullName(), savedQuotation.getTotalAmount());

        return convertToQuotationResponse(savedQuotation);
    }

    /**
     * Reject quotation (Account Managers only)
     */


    /**
     * Get quotation by ID (with access control)
     */
    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(Long quotationId) {
        UserPrincipal currentUser = getCurrentUser();
        Quotation quotation;

        // Super Admin and Account Managers can view all quotations
        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            quotation = quotationRepository.findByIdWithProjectAndItems(quotationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quotationId));
        } else {
            // Project managers can only view their own quotations
            quotation = getQuotationWithAccess(quotationId, currentUser.getId());
        }

        return convertToQuotationResponse(quotation);
    }

    /**
     * Get quotations for current user (with pagination)
     */
    @Transactional(readOnly = true)
    public Page<QuotationSummaryResponse> getMyQuotations(Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();

        Page<Quotation> quotations;

        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            // Can view all quotations
            quotations = quotationRepository.findAllWithProject(pageable);
        } else {
            // Project managers see only their quotations
            quotations = quotationRepository.findByCreatedByIdWithProject(currentUser.getId(), pageable);
        }

        return quotations.map(this::convertToQuotationSummaryResponse);
    }

    /**
     * Get quotations by project
     */
    @Transactional(readOnly = true)
    public List<QuotationSummaryResponse> getQuotationsByProject(Long projectId) {
        UserPrincipal currentUser = getCurrentUser();

        // Validate project access for project managers
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            validateProjectAccess(projectId, currentUser.getId());
        }

        List<Quotation> quotations = quotationRepository.findByProjectIdWithItems(projectId);

        return quotations.stream()
                .map(this::convertToQuotationSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending quotations for approval (Account Managers only)
     */
    @Transactional(readOnly = true)
    public Page<QuotationSummaryResponse> getPendingQuotations(Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();

        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            throw new UnauthorizedAccessException("Only Account Managers can view pending quotations");
        }

        Page<Quotation> quotations = quotationRepository.findByStatusWithProject(
                QuotationStatus.SUBMITTED, pageable);

        return quotations.map(this::convertToQuotationSummaryResponse);
    }

    /**
     * Search quotations with filters
     */
    @Transactional(readOnly = true)
    public Page<QuotationSummaryResponse> searchQuotations(Long projectId, Long createdById,
                                                           String status, String description,
                                                           Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();

        // Project managers can only search their own quotations
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            createdById = currentUser.getId();
        }

        QuotationStatus quotationStatus = null;
        if (status != null) {
            try {
                quotationStatus = QuotationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid quotation status: {}", status);
            }
        }

        Page<Quotation> quotations = quotationRepository.findBySearchCriteria(
                projectId, createdById, quotationStatus, description, pageable);

        return quotations.map(this::convertToQuotationSummaryResponse);
    }


    public QuotationResponse rejectQuotation(Long quotationId, String rejectionReason) {
        logger.info("Rejecting quotation {} with reason: {}", quotationId, rejectionReason);

        UserPrincipal currentUser = getCurrentUser();

        // Only Account Managers and Super Admin can reject
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            throw new UnauthorizedAccessException("Only Account Managers can reject quotations");
        }

        Quotation quotation = quotationRepository.findByIdWithProjectAndItems(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quotationId));

        if (!quotation.canBeRejected()) {
            throw new BusinessException("QUOTATION_NOT_REJECTABLE",
                    "Quotation cannot be rejected in " + quotation.getStatus() + " status");
        }

        User approver = getUserById(currentUser.getId());
        quotation.reject(approver, rejectionReason);

        Quotation savedQuotation = quotationRepository.save(quotation);

        logger.info("Quotation {} rejected successfully by {}", quotationId, approver.getFullName());

        return convertToQuotationResponse(savedQuotation);
    }
    /**
     * Delete quotation (only if in DRAFT status)
     */
    public void deleteQuotation(Long quotationId) {
        logger.info("Deleting quotation with ID: {}", quotationId);

        UserPrincipal currentUser = getCurrentUser();
        Quotation quotation = getQuotationWithAccess(quotationId, currentUser.getId());

        // Can only delete draft quotations
        if (!quotation.canBeDeleted()) {
            throw new BusinessException("QUOTATION_NOT_DELETABLE",
                    "Cannot delete quotation in " + quotation.getStatus() + " status");
        }

        quotation.setActive(false);
        quotationRepository.save(quotation);

        logger.info("Quotation {} deleted successfully", quotationId);
    }

    public boolean canUserAccessQuotation(Long quotationId, Long userId) {
        try {
            getQuotationWithAccess(quotationId, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Get quotation statistics for dashboard
     */
    @Transactional(readOnly = true)
    public QuotationStatistics getQuotationStatistics(Long projectManagerId) {
        UserPrincipal currentUser = getCurrentUser();

        // Project managers can only view their own stats
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER") && projectManagerId == null) {
            projectManagerId = currentUser.getId();
        }

        QuotationStatistics stats = new QuotationStatistics();

        if (projectManagerId != null) {
            // Manager-specific statistics
            stats.setTotalQuotations(quotationRepository.countByCreatedBy(projectManagerId));
            stats.setDraftQuotations(getQuotationCountByStatus(projectManagerId, QuotationStatus.DRAFT));
            stats.setPendingQuotations(getQuotationCountByStatus(projectManagerId, QuotationStatus.SUBMITTED));
            stats.setApprovedQuotations(getQuotationCountByStatus(projectManagerId, QuotationStatus.APPROVED));
            stats.setRejectedQuotations(getQuotationCountByStatus(projectManagerId, QuotationStatus.REJECTED));

            stats.setTotalAmount(getTotalAmountByStatus(projectManagerId, QuotationStatus.APPROVED));
            stats.setPendingAmount(getTotalAmountByStatus(projectManagerId, QuotationStatus.SUBMITTED));
        } else {
            // System-wide statistics (for admins)
            stats.setTotalQuotations(quotationRepository.countActiveQuotations());
            stats.setDraftQuotations(quotationRepository.countByStatus(QuotationStatus.DRAFT));
            stats.setPendingQuotations(quotationRepository.countByStatus(QuotationStatus.SUBMITTED));
            stats.setApprovedQuotations(quotationRepository.countByStatus(QuotationStatus.APPROVED));
            stats.setRejectedQuotations(quotationRepository.countByStatus(QuotationStatus.REJECTED));
        }

        return stats;
    }

    // Private helper methods

    private void addQuotationItems(Quotation quotation, List<CreateQuotationRequest.QuotationItemRequest> itemRequests) {
        for (int i = 0; i < itemRequests.size(); i++) {
            CreateQuotationRequest.QuotationItemRequest itemRequest = itemRequests.get(i);

            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setDescription(itemRequest.getDescription());
            item.setAmount(itemRequest.getAmount());
            item.setCurrency(itemRequest.getCurrency() != null ? itemRequest.getCurrency() : "SAR");
            item.setCategory(itemRequest.getCategory());
            item.setAccountHead(itemRequest.getAccountHead());
            item.setItemDate(itemRequest.getItemDate());
            item.setVendorName(itemRequest.getVendorName());
            item.setVendorContact(itemRequest.getVendorContact());
            item.setItemOrder(i + 1);

            quotation.addItem(item);
            quotationItemRepository.save(item);
        }
    }

    private Project validateProjectAccess(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        UserPrincipal currentUser = getCurrentUser();

        // Super Admin and Account Managers can access all projects
        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            return project;
        }

        // Project managers can only access assigned projects
        if (project.getManager() == null || !project.getManager().getId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You don't have access to project: " + project.getName());
        }

        return project;
    }

    private void validateBudgetCompliance(Quotation quotation) {
        Project project = quotation.getProject();
        BigDecimal quotationAmount = quotation.getTotalAmount();
        BigDecimal remainingBudget = project.getRemainingBudget();

        // Check if quotation exceeds remaining budget
        if (quotationAmount.compareTo(remainingBudget) > 0) {
            BigDecimal exceedAmount = quotationAmount.subtract(remainingBudget);

            // Hard limit: Cannot exceed budget by more than 10%
            BigDecimal maxExceedLimit = project.getAllocatedBudget().multiply(new BigDecimal("0.10"));

            if (exceedAmount.compareTo(maxExceedLimit) > 0) {
                throw new BusinessException("BUDGET_EXCEEDED",
                        String.format("Quotation amount (SAR %.2f) exceeds project budget limit. " +
                                        "Remaining budget: SAR %.2f, Maximum allowed excess: SAR %.2f",
                                quotationAmount.doubleValue(), remainingBudget.doubleValue(), maxExceedLimit.doubleValue()));
            }

            // Soft warning: Log budget warning
            logger.warn("Quotation {} exceeds remaining budget by SAR {} but within allowed limit",
                    quotation.getId(), exceedAmount);
        }
    }

    private void updateProjectBudget(Quotation quotation) {
        Project project = quotation.getProject();
        BigDecimal newSpentAmount = project.getSpentAmount().add(quotation.getTotalAmount());
        project.setSpentAmount(newSpentAmount);
        projectRepository.save(project);

        logger.info("Updated project {} budget. Spent: SAR {}, Remaining: SAR {}",
                project.getId(), newSpentAmount, project.getRemainingBudget());
    }

    private Quotation getQuotationWithAccess(Long quotationId, Long userId) {
        Quotation quotation = quotationRepository.findByIdWithProjectAndItems(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quotationId));

        UserPrincipal currentUser = getCurrentUser();

        // Super Admin and Account Managers can access all quotations
        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            return quotation;
        }

        // Project managers can only access their own quotations
        if (!quotation.getCreator().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this quotation");
        }

        return quotation;
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

    private long getQuotationCountByStatus(Long userId, QuotationStatus status) {
        return quotationRepository.findByCreatedByIdAndActiveTrue(userId).stream()
                .mapToLong(q -> q.getStatus() == status ? 1L : 0L)
                .sum();
    }

    private BigDecimal getTotalAmountByStatus(Long userId, QuotationStatus status) {
        return quotationRepository.getTotalAmountByCreatorAndStatus(userId, status);
    }

    private QuotationResponse convertToQuotationResponse(Quotation quotation) {
        QuotationResponse response = new QuotationResponse();
        response.setId(quotation.getId());
        response.setProjectId(quotation.getProject().getId());
        response.setProjectName(quotation.getProject().getName());
        response.setCreatedBy(quotation.getCreator().getFullName());
        response.setCreatedByUsername(quotation.getCreator().getUsername());
        response.setDescription(quotation.getDescription());
        response.setTotalAmount(quotation.getTotalAmount());
        response.setCurrency(quotation.getCurrency());
        response.setStatus(quotation.getStatus().name());
        response.setSubmissionNotes(quotation.getSubmissionNotes());
        response.setSubmittedDate(quotation.getSubmittedDate());
        response.setApprovedDate(quotation.getApprovedDate());
        response.setRejectionReason(quotation.getRejectionReason());
        response.setCreatedDate(quotation.getCreatedDate());
        response.setActive(quotation.getActive());

        if (quotation.getApprovedBy() != null) {
            response.setApprovedBy(quotation.getApprovedBy().getFullName());
        }

        // Add budget information
        Project project = quotation.getProject();
        response.setProjectBudget(project.getAllocatedBudget());
        response.setRemainingBudget(project.getRemainingBudget());
        response.setBudgetImpact(quotation.getBudgetImpact());
        response.setExceedsBudget(quotation.exceedsBudget());

        // Convert items
        if (quotation.getItems() != null) {
            response.setItems(quotation.getItems().stream()
                    .map(this::convertToItemResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private QuotationSummaryResponse convertToQuotationSummaryResponse(Quotation quotation) {
        QuotationSummaryResponse response = new QuotationSummaryResponse();
        response.setId(quotation.getId());
        response.setProjectId(quotation.getProject().getId());
        response.setProjectName(quotation.getProject().getName());
        response.setCreatedBy(quotation.getCreator().getFullName());
        response.setTotalAmount(quotation.getTotalAmount());
        response.setCurrency(quotation.getCurrency());
        response.setStatus(quotation.getStatus().name());
        response.setSubmittedDate(quotation.getSubmittedDate());
        response.setCreatedDate(quotation.getCreatedDate());
        response.setItemCount(quotation.getItems() != null ? quotation.getItems().size() : 0);
        response.setExceedsBudget(quotation.exceedsBudget());
        return response;
    }

    private QuotationResponse.QuotationItemResponse convertToItemResponse(QuotationItem item) {
        QuotationResponse.QuotationItemResponse itemResponse = new QuotationResponse.QuotationItemResponse();
        itemResponse.setId(item.getId());
        itemResponse.setDescription(item.getDescription());
        itemResponse.setAmount(item.getAmount());
        itemResponse.setCurrency(item.getCurrency());
        itemResponse.setCategory(item.getCategory());
        itemResponse.setAccountHead(item.getAccountHead());
        itemResponse.setItemDate(item.getItemDate());
        itemResponse.setVendorName(item.getVendorName());
        itemResponse.setVendorContact(item.getVendorContact());
        itemResponse.setItemOrder(item.getItemOrder());
        return itemResponse;
    }

    // Statistics inner class
    public static class QuotationStatistics {
        private long totalQuotations;
        private long draftQuotations;
        private long pendingQuotations;
        private long approvedQuotations;
        private long rejectedQuotations;
        private BigDecimal totalAmount;
        private BigDecimal pendingAmount;

        // Getters and Setters
        public long getTotalQuotations() { return totalQuotations; }
        public void setTotalQuotations(long totalQuotations) { this.totalQuotations = totalQuotations; }

        public long getDraftQuotations() { return draftQuotations; }
        public void setDraftQuotations(long draftQuotations) { this.draftQuotations = draftQuotations; }

        public long getPendingQuotations() { return pendingQuotations; }
        public void setPendingQuotations(long pendingQuotations) { this.pendingQuotations = pendingQuotations; }

        public long getApprovedQuotations() { return approvedQuotations; }
        public void setApprovedQuotations(long approvedQuotations) { this.approvedQuotations = approvedQuotations; }

        public long getRejectedQuotations() { return rejectedQuotations; }
        public void setRejectedQuotations(long rejectedQuotations) { this.rejectedQuotations = rejectedQuotations; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
    }
}