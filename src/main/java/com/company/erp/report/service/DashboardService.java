package com.company.erp.report.service;

import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.repository.QuotationRepository;
import com.company.erp.project.repository.ProjectRepository;
import com.company.erp.report.dto.response.DashboardResponse;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;  // Import the new enum
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get main dashboard data
     */
    public DashboardResponse getMainDashboard(Long userId) {
        logger.info("Generating main dashboard for user: {}", userId);

        UserPrincipal currentUser = getCurrentUser();
        DashboardResponse dashboard = new DashboardResponse();

        // Set basic info
        dashboard.setUserId(userId);
        dashboard.setGeneratedAt(LocalDateTime.now());

        // Key metrics based on user role
        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            dashboard = getAdminDashboard(dashboard);
        } else if (currentUser.hasRole("PROJECT_MANAGER")) {
            dashboard = getProjectManagerDashboard(dashboard, userId);
        }

        // Common metrics
        dashboard.setActiveProjectsCount(getActiveProjectsCount(userId, currentUser));
        dashboard.setPendingApprovalsCount(getPendingApprovalsCount(userId, currentUser));
        dashboard.setRecentActivities(getRecentActivities(userId, currentUser));

        return dashboard;
    }

    /**
     * Get user-specific dashboard
     */
    public DashboardResponse getUserDashboard(Long userId) {
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setUserId(userId);
        dashboard.setGeneratedAt(LocalDateTime.now());

        // User-specific metrics - fixed method calls
        dashboard.setMyProjectsCount(countProjectsByManagerId(userId));
        dashboard.setMyQuotationsCount(quotationRepository.countByCreatedBy(userId));
        dashboard.setPendingTasksCount(getPendingTasksForUser(userId));

        return dashboard;
    }

    /**
     * Get dashboard widgets data
     */
    public Map<String, Object> getDashboardWidgets(Long userId, List<String> widgetTypes) {
        Map<String, Object> widgets = new HashMap<>();

        for (String widgetType : widgetTypes) {
            switch (widgetType.toUpperCase()) {
                case "BUDGET_SUMMARY":
                    widgets.put("budgetSummary", getBudgetSummaryWidget(userId));
                    break;
                case "PROJECT_STATUS":
                    widgets.put("projectStatus", getProjectStatusWidget(userId));
                    break;
                case "RECENT_ACTIVITY":
                    widgets.put("recentActivity", getRecentActivityWidget(userId));
                    break;
                case "QUICK_STATS":
                    widgets.put("quickStats", getQuickStatsWidget(userId));
                    break;
                default:
                    logger.warn("Unknown widget type: {}", widgetType);
            }
        }

        return widgets;
    }

    // Private helper methods

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) authentication.getPrincipal();
    }

    private DashboardResponse getAdminDashboard(DashboardResponse dashboard) {
        // Admin-specific metrics
        dashboard.setTotalBudgetAllocated(getTotalBudgetAllocated());
        dashboard.setTotalSpentAmount(getTotalSpentAmount());
        dashboard.setBudgetUtilizationPercentage(calculateBudgetUtilization());
        dashboard.setSystemHealthScore(calculateSystemHealthScore());

        return dashboard;
    }

    private DashboardResponse getProjectManagerDashboard(DashboardResponse dashboard, Long userId) {
        // Project manager specific metrics - fixed method calls
        dashboard.setMyProjectsCount(countProjectsByManagerId(userId));
        dashboard.setMyBudgetUtilization(calculateUserBudgetUtilization(userId));
        dashboard.setMyTeamSize(calculateTeamSize(userId));

        return dashboard;
    }

    private Long getActiveProjectsCount(Long userId, UserPrincipal currentUser) {
        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            return countActiveProjects();
        } else {
            return countProjectsByManagerId(userId);
        }
    }

    private Long getPendingApprovalsCount(Long userId, UserPrincipal currentUser) {
        if (currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            return quotationRepository.countByStatus(com.company.erp.financial.entity.QuotationStatus.SUBMITTED);
        }
        return 0L; // Project managers don't have approval rights
    }

    private List<Map<String, Object>> getRecentActivities(Long userId, UserPrincipal currentUser) {
        // Implementation for recent activities
        return List.of(); // Placeholder
    }

    private Long getPendingTasksForUser(Long userId) {
        // Implementation for user-specific pending tasks
        return 0L; // Placeholder
    }

    private Map<String, Object> getBudgetSummaryWidget(Long userId) {
        Map<String, Object> widget = new HashMap<>();
        widget.put("totalBudget", getTotalBudgetAllocated());
        widget.put("spentAmount", getTotalSpentAmount());
        widget.put("remainingBudget", getTotalBudgetAllocated().subtract(getTotalSpentAmount()));
        return widget;
    }

    private Map<String, Object> getProjectStatusWidget(Long userId) {
        Map<String, Object> widget = new HashMap<>();
        // Fixed method calls
        widget.put("active", countProjectsByStatus("ACTIVE"));
        widget.put("completed", countProjectsByStatus("COMPLETED"));
        widget.put("onHold", countProjectsByStatus("ON_HOLD"));
        return widget;
    }

    private Map<String, Object> getRecentActivityWidget(Long userId) {
        Map<String, Object> widget = new HashMap<>();
        widget.put("activities", getRecentActivities(userId, getCurrentUser()));
        return widget;
    }

    private Map<String, Object> getQuickStatsWidget(Long userId) {
        Map<String, Object> widget = new HashMap<>();
        widget.put("projectsCount", getActiveProjectsCount(userId, getCurrentUser()));
        widget.put("approvalsCount", getPendingApprovalsCount(userId, getCurrentUser()));
        widget.put("budgetUtilization", calculateBudgetUtilization());
        return widget;
    }

    private BigDecimal getTotalBudgetAllocated() {
        // Implementation for total budget calculation
        return new BigDecimal("1000000"); // Placeholder
    }

    private BigDecimal getTotalSpentAmount() {
        // Implementation for total spent calculation
        return new BigDecimal("750000"); // Placeholder
    }

    // FIXED: Using new RoundingMode enum instead of deprecated constants
    private BigDecimal calculateBudgetUtilization() {
        BigDecimal total = getTotalBudgetAllocated();
        BigDecimal spent = getTotalSpentAmount();

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            return spent.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateSystemHealthScore() {
        // Implementation for system health calculation
        return new BigDecimal("95.5"); // Placeholder
    }

    private BigDecimal calculateUserBudgetUtilization(Long userId) {
        // Implementation for user-specific budget utilization
        return new BigDecimal("80.2"); // Placeholder
    }

    private Long calculateTeamSize(Long userId) {
        // Implementation for team size calculation
        return 5L; // Placeholder
    }

    // NEW: Helper methods to replace missing repository methods
    private Long countActiveProjects() {
        // Use existing repository methods or custom queries
        try {
            return projectRepository.countByActiveTrue();
        } catch (Exception e) {
            // Fallback if method doesn't exist
            logger.warn("countByActiveTrue method not found, using fallback");
            return projectRepository.count(); // Fallback to total count
        }
    }

    private Long countProjectsByManagerId(Long managerId) {
        try {
            return projectRepository.countByManagerIdAndActiveTrue(managerId);
        } catch (Exception e) {
            // Fallback implementation
            logger.warn("countByManagerIdAndActiveTrue method not found, using fallback");
            return 0L; // Placeholder - implement based on your Project entity structure
        }
    }

    private Long countProjectsByStatus(String status) {
        try {
            return projectRepository.countByStatusAndActiveTrue(status);
        } catch (Exception e) {
            // Fallback implementation
            logger.warn("countByStatusAndActiveTrue method not found, using fallback");
            return 0L; // Placeholder - implement based on your Project entity structure
        }
    }
}