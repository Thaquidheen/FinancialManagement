package com.company.erp.financial.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.common.exception.UnauthorizedAccessException;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.dto.request.BudgetTrackingRequest;
import com.company.erp.financial.dto.response.*;
import com.company.erp.financial.entity.BudgetTracking;
import com.company.erp.financial.entity.BudgetTrackingType;
import com.company.erp.financial.entity.Quotation;
import com.company.erp.financial.entity.QuotationStatus;
import com.company.erp.financial.repository.BudgetTrackingRepository;
import com.company.erp.financial.repository.QuotationRepository;
import com.company.erp.project.entity.Project;
import com.company.erp.project.repository.ProjectRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import com.company.erp.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetTrackingService.class);
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("80.00");
    private static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("90.00");

    @Autowired
    private BudgetTrackingRepository budgetTrackingRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create new budget tracking entry
     */
    public BudgetTrackingResponse createBudgetTracking(BudgetTrackingRequest request, Long userId) {
        logger.info("Creating budget tracking entry for project: {}", request.getProjectId());

        UserPrincipal currentUser = getCurrentUser();
        validateBudgetTrackingAccess(currentUser);

        // Validate project exists
        Project project = projectRepository.findByIdAndActiveTrue(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // Get user who recorded the transaction
        User recordedBy = getUserById(userId);

        // Calculate previous balance
        BigDecimal previousBalance = getCurrentProjectBalance(project.getId());

        // Create budget tracking entry
        BudgetTracking budgetTracking = new BudgetTracking(project, request.getTrackingType(),
                request.getAmount());

        budgetTracking.setRecordedBy(recordedBy);

        budgetTracking.setDescription(request.getDescription());
        budgetTracking.setCategory(request.getCategory());
        budgetTracking.setReferenceType(request.getReferenceType());
        budgetTracking.setReferenceId(request.getReferenceId());
        budgetTracking.setPreviousBalance(previousBalance);
        budgetTracking.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SAR");

        if (request.getTransactionDate() != null) {
            budgetTracking.setTransactionDate(request.getTransactionDate().atStartOfDay());
        }

        // Calculate new balance based on tracking type
        BigDecimal newBalance = calculateNewBalance(previousBalance, request.getAmount(), request.getTrackingType());
        budgetTracking.setNewBalance(newBalance);

        // Update project budget
        updateProjectBudget(project, request.getTrackingType(), request.getAmount());

        // Calculate variance and check budget status
        budgetTracking.calculateVariance();

        BudgetTracking savedTracking = budgetTrackingRepository.save(budgetTracking);

        // Check for budget alerts
        checkAndSendBudgetAlerts(project, savedTracking);

        logger.info("Budget tracking entry created successfully with ID: {}", savedTracking.getId());

        return convertToBudgetTrackingResponse(savedTracking);
    }

    /**
     * Get budget tracking by ID
     */
    @Transactional(readOnly = true)
    public BudgetTrackingResponse getBudgetTrackingById(Long id) {
        BudgetTracking budgetTracking = budgetTrackingRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget Tracking", "id", id));

        return convertToBudgetTrackingResponse(budgetTracking);
    }

    /**
     * Get project budget tracking with filters
     */
    @Transactional(readOnly = true)
    public Page<BudgetTrackingResponse> getProjectBudgetTracking(Long projectId, BudgetTrackingType type,
                                                                 LocalDate startDate, LocalDate endDate, Pageable pageable) {

        UserPrincipal currentUser = getCurrentUser();

        // Validate project access for project managers
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            validateProjectManagerAccess(projectId, currentUser.getId());
        }

        Page<BudgetTracking> budgetTrackings;

        if (type != null || startDate != null || endDate != null) {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

            budgetTrackings = budgetTrackingRepository.findByProjectIdWithFilters(
                    projectId, type, startDateTime, endDateTime, pageable);
        } else {
            budgetTrackings = budgetTrackingRepository.findByProjectIdAndActiveTrueOrderByTransactionDateDesc(
                    projectId, pageable);
        }

        return budgetTrackings.map(this::convertToBudgetTrackingResponse);
    }

    /**
     * Get project budget status and utilization
     */
    @Transactional(readOnly = true)
    public BudgetStatusResponse getProjectBudgetStatus(Long projectId) {
        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        BudgetStatusResponse response = new BudgetStatusResponse();
        response.setProjectId(project.getId());
        response.setProjectName(project.getName());
        response.setAllocatedBudget(project.getAllocatedBudget());
        response.setSpentAmount(project.getSpentAmount());
        response.setRemainingBudget(project.getRemainingBudget());
        response.setCurrency(project.getCurrency());

        // Calculate utilization percentage
        if (project.getAllocatedBudget() != null && project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilization = project.getSpentAmount()
                    .divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            response.setUtilizationPercentage(utilization);

            // Set budget status
            if (utilization.compareTo(new BigDecimal("100")) > 0) {
                response.setBudgetStatus("OVER_BUDGET");
            } else if (utilization.compareTo(CRITICAL_THRESHOLD) >= 0) {
                response.setBudgetStatus("CRITICAL");
            } else if (utilization.compareTo(WARNING_THRESHOLD) >= 0) {
                response.setBudgetStatus("WARNING");
            } else {
                response.setBudgetStatus("NORMAL");
            }
        } else {
            response.setUtilizationPercentage(BigDecimal.ZERO);
            response.setBudgetStatus("NORMAL");
        }

        // Get recent transactions count
        long recentTransactionsCount = budgetTrackingRepository.countRecentTransactions(
                projectId, LocalDateTime.now().minusDays(30));
        response.setRecentTransactionsCount(recentTransactionsCount);

        // Get last transaction date
        budgetTrackingRepository.findFirstByProjectIdAndActiveTrueOrderByTransactionDateDesc(projectId)
                .ifPresent(bt -> response.setLastTransactionDate(bt.getTransactionDate()));

        return response;
    }

    /**
     * Get detailed budget variance analysis for a project
     */
    @Transactional(readOnly = true)
    public BudgetVarianceResponse getBudgetVarianceAnalysis(Long projectId) {
        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        BudgetVarianceResponse response = new BudgetVarianceResponse();
        response.setProjectId(project.getId());
        response.setProjectName(project.getName());
        response.setPeriod("TOTAL");

        BigDecimal planned = project.getAllocatedBudget() != null ? project.getAllocatedBudget() : BigDecimal.ZERO;
        BigDecimal actual = project.getSpentAmount() != null ? project.getSpentAmount() : BigDecimal.ZERO;
        response.setPlannedAmount(planned);
        response.setActualAmount(actual);

        BigDecimal variance = actual.subtract(planned);
        response.setVariance(variance);

        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variancePct = variance
                    .divide(planned, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            response.setVariancePercentage(variancePct);
        } else {
            response.setVariancePercentage(BigDecimal.ZERO);
        }

        response.setVarianceType(variance.compareTo(BigDecimal.ZERO) >= 0 ? "NEGATIVE" : "POSITIVE");
        return response;
    }

    /**
     * Get budget alerts for a project
     */
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> getProjectBudgetAlerts(Long projectId, BigDecimal warningThreshold) {
        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<BudgetAlertResponse> alerts = new ArrayList<>();
        BigDecimal threshold = warningThreshold != null ? warningThreshold : WARNING_THRESHOLD;

        if (project.getAllocatedBudget() != null && project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilization = project.getSpentAmount()
                    .divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            BudgetAlertResponse alert = new BudgetAlertResponse();
            alert.setProjectId(project.getId());
            alert.setProjectName(project.getName());
            alert.setUtilizationPercentage(utilization);
            alert.setAllocatedBudget(project.getAllocatedBudget());
            alert.setSpentAmount(project.getSpentAmount());
            alert.setRemainingBudget(project.getRemainingBudget());

            if (utilization.compareTo(new BigDecimal("100")) > 0) {
                alert.setAlertLevel("CRITICAL");
                alert.setMessage("Project is over budget by " +
                        utilization.subtract(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");
                alerts.add(alert);
            } else if (utilization.compareTo(threshold) >= 0) {
                alert.setAlertLevel("WARNING");
                alert.setMessage("Budget utilization has reached " +
                        utilization.setScale(2, RoundingMode.HALF_UP) + "%");
                alerts.add(alert);
            }
        }

        return alerts;
    }

    /**
     * Get active budget alerts across all projects using thresholds
     */
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> getBudgetAlerts(BigDecimal warningThreshold, BigDecimal criticalThreshold) {
        BigDecimal warn = warningThreshold != null ? warningThreshold : WARNING_THRESHOLD;
        BigDecimal critical = criticalThreshold != null ? criticalThreshold : CRITICAL_THRESHOLD;

        List<Project> projects = projectRepository.findAllActiveWithManager();

        return projects.stream()
                .map(project -> {
                    BigDecimal allocated = project.getAllocatedBudget() != null ? project.getAllocatedBudget() : BigDecimal.ZERO;
                    BigDecimal spent = project.getSpentAmount() != null ? project.getSpentAmount() : BigDecimal.ZERO;

                    if (allocated.compareTo(BigDecimal.ZERO) <= 0) {
                        return null; // skip projects without a valid budget
                    }

                    BigDecimal utilization = spent
                            .divide(allocated, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));

                    BudgetAlertResponse alert = new BudgetAlertResponse();
                    alert.setProjectId(project.getId());
                    alert.setProjectName(project.getName());
                    if (project.getManager() != null) {
                        alert.setProjectManager(project.getManager().getFullName());
                    }
                    alert.setAllocatedBudget(allocated);
                    alert.setSpentAmount(spent);
                    alert.setRemainingBudget(project.getRemainingBudget());
                    alert.setUtilizationPercentage(utilization);

                    BigDecimal variance = spent.subtract(allocated);
                    BigDecimal variancePct = variance
                            .divide(allocated, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    alert.setVariancePercentage(variancePct);

                    if (utilization.compareTo(new BigDecimal("100")) > 0) {
                        alert.setAlertLevel("CRITICAL");
                        alert.setAlertMessage("Project is over budget");
                        alert.setOverBudget(true);
                        alert.setRequiresAttention(true);
                        return alert;
                    }

                    if (utilization.compareTo(critical) >= 0) {
                        alert.setAlertLevel("HIGH");
                        alert.setAlertMessage("Project approaching budget limit");
                        alert.setRequiresAttention(true);
                        return alert;
                    }

                    if (utilization.compareTo(warn) >= 0) {
                        alert.setAlertLevel("WARNING");
                        alert.setAlertMessage("Budget utilization warning");
                        alert.setRequiresAttention(true);
                        return alert;
                    }

                    return null;
                })
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }

    /**
     * Get overall budget summary
     */
    @Transactional(readOnly = true)
    public Object getBudgetSummary(LocalDate startDate, LocalDate endDate) {
        UserPrincipal currentUser = getCurrentUser();
        validateBudgetTrackingAccess(currentUser);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<Object[]> summaryData = budgetTrackingRepository.getBudgetSummary(startDateTime, endDateTime);

        BudgetSummaryResponse response = new BudgetSummaryResponse();

        if (!summaryData.isEmpty()) {
            Object[] data = summaryData.get(0);
            response.setTotalAllocatedBudget((BigDecimal) data[0]);
            response.setTotalSpentAmount((BigDecimal) data[1]);
            response.setTotalRemainingBudget((BigDecimal) data[2]);
            response.setActiveProjectsCount(((Number) data[3]).longValue());
        }

        // Get project-wise breakdown
        List<Object[]> projectBreakdown = budgetTrackingRepository.getProjectBudgetBreakdown(startDateTime, endDateTime);
        response.setProjectBreakdown(projectBreakdown.stream()
                .map(this::convertToProjectBudgetSummary)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * Update budget tracking entry
     */
    public BudgetTrackingResponse updateBudgetTracking(Long id, BudgetTrackingRequest request, Long userId) {
        logger.info("Updating budget tracking entry: {}", id);

        UserPrincipal currentUser = getCurrentUser();
        validateBudgetTrackingAccess(currentUser);

        BudgetTracking budgetTracking = budgetTrackingRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget Tracking", "id", id));

        // Store original values for reversion if needed
        BigDecimal originalAmount = budgetTracking.getAmount();
        BudgetTrackingType originalType = budgetTracking.getTrackingType();

        // Update fields
        budgetTracking.setDescription(request.getDescription());
        budgetTracking.setCategory(request.getCategory());

        // If amount or type changed, update project budget
        if (!originalAmount.equals(request.getAmount()) || !originalType.equals(request.getTrackingType())) {
            // Revert original transaction
            updateProjectBudget(budgetTracking.getProject(), originalType, originalAmount.negate());

            // Apply new transaction
            updateProjectBudget(budgetTracking.getProject(), request.getTrackingType(), request.getAmount());

            budgetTracking.setAmount(request.getAmount());
            budgetTracking.setTrackingType(request.getTrackingType());

            // Recalculate balance
            BigDecimal previousBalance = getCurrentProjectBalance(budgetTracking.getProject().getId());
            budgetTracking.setPreviousBalance(previousBalance);
            budgetTracking.setNewBalance(calculateNewBalance(previousBalance, request.getAmount(), request.getTrackingType()));
        }

        if (request.getTransactionDate() != null) {
            budgetTracking.setTransactionDate(request.getTransactionDate().atStartOfDay());
        }

        budgetTracking.calculateVariance();

        BudgetTracking savedTracking = budgetTrackingRepository.save(budgetTracking);

        logger.info("Budget tracking entry updated successfully: {}", id);

        return convertToBudgetTrackingResponse(savedTracking);
    }

    /**
     * Delete budget tracking entry (soft delete)
     */
    public void deleteBudgetTracking(Long id) {
        logger.info("Deleting budget tracking entry: {}", id);

        UserPrincipal currentUser = getCurrentUser();
        if (!currentUser.hasRole("SUPER_ADMIN")) {
            throw new UnauthorizedAccessException("Only Super Admins can delete budget tracking entries");
        }

        BudgetTracking budgetTracking = budgetTrackingRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget Tracking", "id", id));

        // Revert the transaction from project budget
        updateProjectBudget(budgetTracking.getProject(),
                budgetTracking.getTrackingType(),
                budgetTracking.getAmount().negate());

        // Soft delete
        budgetTracking.setActive(false);
        budgetTrackingRepository.save(budgetTracking);

        logger.info("Budget tracking entry deleted successfully: {}", id);
    }

    /**
     * Recalculate project budget from all transactions
     */
    public BudgetStatusResponse recalculateProjectBudget(Long projectId, Long userId) {
        logger.info("Recalculating budget for project: {}", projectId);

        UserPrincipal currentUser = getCurrentUser();
        validateBudgetTrackingAccess(currentUser);

        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Get all approved quotations for the project
        BigDecimal totalApprovedAmount = quotationRepository.getTotalApprovedAmountByProject(projectId);
        if (totalApprovedAmount == null) {
            totalApprovedAmount = BigDecimal.ZERO;
        }

        // Update project spent amount
        project.setSpentAmount(totalApprovedAmount);
        project.setRemainingBudget(project.getAllocatedBudget().subtract(totalApprovedAmount));

        projectRepository.save(project);

        // Create audit trail entry
        User recordedBy = getUserById(userId);
        BudgetTracking auditEntry = new BudgetTracking(project, BudgetTrackingType.ADJUSTMENT,
                BigDecimal.ZERO);
        auditEntry.setRecordedBy(recordedBy);
        auditEntry.setDescription("Budget recalculation performed");
        auditEntry.setCategory("AUDIT");
        auditEntry.setPreviousBalance(project.getSpentAmount());
        auditEntry.setNewBalance(project.getSpentAmount());
        budgetTrackingRepository.save(auditEntry);

        logger.info("Budget recalculated for project: {}. New spent amount: {}",
                projectId, totalApprovedAmount);

        return getProjectBudgetStatus(projectId);
    }

    /**
     * Export budget tracking data
     */
    @Transactional(readOnly = true)
    public String exportBudgetTracking(Long projectId, LocalDate startDate, LocalDate endDate, String format) {
        logger.info("Exporting budget tracking data for project: {} in format: {}", projectId, format);

        UserPrincipal currentUser = getCurrentUser();

        // Validate project access
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            validateProjectManagerAccess(projectId, currentUser.getId());
        }

        // Get tracking data
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<BudgetTracking> trackingData = budgetTrackingRepository.findByProjectIdForExport(
                projectId, startDateTime, endDateTime);

        // For now, return a placeholder file path
        // In real implementation, you would generate Excel/CSV files
        String fileName = String.format("budget_tracking_%d_%s_%s.%s",
                projectId,
                startDate != null ? startDate.toString() : "all",
                endDate != null ? endDate.toString() : "all",
                format.toLowerCase());

        logger.info("Budget tracking export completed: {}", fileName);

        return fileName;
    }

    // Helper methods

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserById(Long userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private void validateBudgetTrackingAccess(UserPrincipal currentUser) {
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            throw new UnauthorizedAccessException("Insufficient permissions for budget tracking operations");
        }
    }

    private void validateProjectManagerAccess(Long projectId, Long userId) {
        boolean hasAccess = projectRepository.existsByIdAndManagerIdAndActiveTrue(projectId, userId);
        if (!hasAccess) {
            throw new UnauthorizedAccessException("Access denied for this project");
        }
    }

    private BigDecimal getCurrentProjectBalance(Long projectId) {
        return budgetTrackingRepository.getCurrentBalance(projectId);
    }

    private BigDecimal calculateNewBalance(BigDecimal previousBalance, BigDecimal amount, BudgetTrackingType type) {
        switch (type) {
            case EXPENSE:
            case QUOTATION_APPROVED:
                return previousBalance.add(amount);
            case BUDGET_ALLOCATION:
            case BUDGET_INCREASE:
                return previousBalance.add(amount);
            case BUDGET_DECREASE:
            case REFUND:
                return previousBalance.subtract(amount);
            case ADJUSTMENT:
                return previousBalance; // No change for adjustments
            default:
                return previousBalance.add(amount);
        }
    }

    private void updateProjectBudget(Project project, BudgetTrackingType type, BigDecimal amount) {
        switch (type) {
            case EXPENSE:
            case QUOTATION_APPROVED:
                project.setSpentAmount(project.getSpentAmount().add(amount));
                project.setRemainingBudget(project.getAllocatedBudget().subtract(project.getSpentAmount()));
                break;
            case BUDGET_ALLOCATION:
            case BUDGET_INCREASE:
                project.setAllocatedBudget(project.getAllocatedBudget().add(amount));
                project.setRemainingBudget(project.getAllocatedBudget().subtract(project.getSpentAmount()));
                break;
            case BUDGET_DECREASE:
                project.setAllocatedBudget(project.getAllocatedBudget().subtract(amount));
                project.setRemainingBudget(project.getAllocatedBudget().subtract(project.getSpentAmount()));
                break;
            case REFUND:
                project.setSpentAmount(project.getSpentAmount().subtract(amount));
                project.setRemainingBudget(project.getAllocatedBudget().subtract(project.getSpentAmount()));
                break;
        }

        projectRepository.save(project);
    }

    private void checkAndSendBudgetAlerts(Project project, BudgetTracking tracking) {
        if (project.getAllocatedBudget() != null && project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilization = project.getSpentAmount()
                    .divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            String alertLevel = null;
            String message = null;

            if (utilization.compareTo(new BigDecimal("100")) > 0) {
                alertLevel = "CRITICAL";
                message = String.format("Project '%s' is over budget by %.2f%%",
                        project.getName(), utilization.subtract(new BigDecimal("100")).doubleValue());
            } else if (utilization.compareTo(CRITICAL_THRESHOLD) >= 0) {
                alertLevel = "HIGH";
                message = String.format("Project '%s' has reached %.2f%% budget utilization",
                        project.getName(), utilization.doubleValue());
            } else if (utilization.compareTo(WARNING_THRESHOLD) >= 0) {
                alertLevel = "WARNING";
                message = String.format("Project '%s' budget utilization warning: %.2f%%",
                        project.getName(), utilization.doubleValue());
            }

            if (alertLevel != null && message != null) {
                // Send notification to project manager and super admin
                sendBudgetAlert(project, alertLevel, message, utilization);
            }
        }
    }

    private void sendBudgetAlert(Project project, String alertLevel, String message, BigDecimal utilization) {
        try {
            // Create notification for project manager
            if (project.getManager() != null) {
                notificationService.createNotification(project.getManager().getId(),
                        "Budget Alert", message, "BUDGET_ALERT");
            }

            // Find users with SUPER_ADMIN role using a stream-based approach
            List<User> superAdmins = userRepository.findAll().stream()
                    .filter(User::getActive)
                    .filter(user -> user.getRoles().stream().anyMatch(role -> "SUPER_ADMIN".equals(role.getName())))
                    .collect(Collectors.toList());

            // Send notifications to all super admins
            for (User admin : superAdmins) {
                notificationService.createNotification(admin.getId(),
                        "Budget Alert", message, "BUDGET_ALERT");
            }

        } catch (Exception e) {
            logger.error("Failed to send budget alert for project {}: {}",
                    project.getId(), e.getMessage());
        }
    }

    private BudgetTrackingResponse convertToBudgetTrackingResponse(BudgetTracking budgetTracking) {
        BudgetTrackingResponse response = new BudgetTrackingResponse();

        response.setId(budgetTracking.getId());
        response.setProjectId(budgetTracking.getProject().getId());
        response.setProjectName(budgetTracking.getProject().getName());
        response.setTrackingType(budgetTracking.getTrackingType());
        response.setReferenceType(budgetTracking.getReferenceType());
        response.setReferenceId(budgetTracking.getReferenceId());
        response.setAmount(budgetTracking.getAmount());
        response.setCurrency(budgetTracking.getCurrency());
        response.setPreviousBalance(budgetTracking.getPreviousBalance());
        response.setNewBalance(budgetTracking.getNewBalance());
        response.setDescription(budgetTracking.getDescription());
        response.setCategory(budgetTracking.getCategory());
        response.setTransactionDate(budgetTracking.getTransactionDate());
        response.setIsBudgetExceeded(budgetTracking.getIsBudgetExceeded());
        response.setVariancePercentage(budgetTracking.getVariancePercentage());
        response.setCreatedDate(budgetTracking.getCreatedDate());

        if (budgetTracking.getRecordedBy() != null) {
            response.setRecordedByName(budgetTracking.getRecordedBy().getFullName());
            response.setRecordedById(budgetTracking.getRecordedBy().getId());
        }

        return response;
    }

    private ProjectBudgetSummary convertToProjectBudgetSummary(Object[] data) {
        ProjectBudgetSummary summary = new ProjectBudgetSummary();
        summary.setProjectId(((Number) data[0]).longValue());
        summary.setProjectName((String) data[1]);
        summary.setAllocatedBudget((BigDecimal) data[2]);
        summary.setSpentAmount((BigDecimal) data[3]);
        summary.setRemainingBudget((BigDecimal) data[4]);

        if (summary.getAllocatedBudget() != null && summary.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilization = summary.getSpentAmount()
                    .divide(summary.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            summary.setUtilizationPercentage(utilization);
        }

        return summary;
    }

    // Inner classes for response objects
    public static class BudgetSummaryResponse {
        private BigDecimal totalAllocatedBudget;
        private BigDecimal totalSpentAmount;
        private BigDecimal totalRemainingBudget;
        private Long activeProjectsCount;
        private List<ProjectBudgetSummary> projectBreakdown;

        // Constructors, getters and setters
        public BudgetSummaryResponse() {}

        // Getters and Setters
        public BigDecimal getTotalAllocatedBudget() { return totalAllocatedBudget; }
        public void setTotalAllocatedBudget(BigDecimal totalAllocatedBudget) { this.totalAllocatedBudget = totalAllocatedBudget; }

        public BigDecimal getTotalSpentAmount() { return totalSpentAmount; }
        public void setTotalSpentAmount(BigDecimal totalSpentAmount) { this.totalSpentAmount = totalSpentAmount; }

        public BigDecimal getTotalRemainingBudget() { return totalRemainingBudget; }
        public void setTotalRemainingBudget(BigDecimal totalRemainingBudget) { this.totalRemainingBudget = totalRemainingBudget; }

        public Long getActiveProjectsCount() { return activeProjectsCount; }
        public void setActiveProjectsCount(Long activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }

        public List<ProjectBudgetSummary> getProjectBreakdown() { return projectBreakdown; }
        public void setProjectBreakdown(List<ProjectBudgetSummary> projectBreakdown) { this.projectBreakdown = projectBreakdown; }
    }

    public static class ProjectBudgetSummary {
        private Long projectId;
        private String projectName;
        private BigDecimal allocatedBudget;
        private BigDecimal spentAmount;
        private BigDecimal remainingBudget;
        private BigDecimal utilizationPercentage;

        // Constructors, getters and setters
        public ProjectBudgetSummary() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public BigDecimal getAllocatedBudget() { return allocatedBudget; }
        public void setAllocatedBudget(BigDecimal allocatedBudget) { this.allocatedBudget = allocatedBudget; }

        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

        public BigDecimal getRemainingBudget() { return remainingBudget; }
        public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }

        public BigDecimal getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(BigDecimal utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
    }
}