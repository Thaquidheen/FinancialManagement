package com.company.erp.report.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for Dashboard data
 * Contains all metrics and information for the main dashboard
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    // Basic Info
    private Long userId;
    private String userRole;
    private LocalDateTime generatedAt;
    private String dashboardType; // ADMIN, PROJECT_MANAGER, USER

    // Project Metrics
    private Long activeProjectsCount;
    private Long completedProjectsCount;
    private Long totalProjectsCount;
    private Long myProjectsCount;
    private Long delayedProjectsCount;

    // Budget & Financial Metrics
    private BigDecimal totalBudgetAllocated;
    private BigDecimal totalSpentAmount;
    private BigDecimal budgetUtilizationPercentage;
    private BigDecimal myBudgetUtilization;
    private BigDecimal remainingBudget;

    // Approval & Workflow Metrics
    private Long pendingApprovalsCount;
    private Long pendingTasksCount;
    private Long myApprovalRequestsCount;

    // Quotation Metrics
    private Long myQuotationsCount;
    private Long pendingQuotationsCount;
    private Long approvedQuotationsCount;

    // Team & User Metrics
    private Long myTeamSize;
    private Long totalUsersCount;
    private Long activeUsersCount;

    // System Health & Performance
    private BigDecimal systemHealthScore;
    private String systemStatus;
    private Double averageResponseTime;

    // Activity & Notifications
    private List<Map<String, Object>> recentActivities;
    private Long unreadNotificationsCount;
    private List<Map<String, Object>> upcomingDeadlines;
    private List<Map<String, Object>> alertsSummary;

    // Charts & Analytics Data
    private List<Map<String, Object>> budgetTrends;
    private List<Map<String, Object>> projectStatusDistribution;
    private Map<String, Object> monthlyMetrics;
    private Map<String, Object> performanceIndicators;

    // Quick Actions & Links
    private List<Map<String, Object>> quickActions;
    private Map<String, String> frequentlyAccessedLinks;

    // Widget Configurations
    private Map<String, Object> widgetSettings;
    private List<String> enabledWidgets;

    // Constructors
    public DashboardResponse() {
        this.generatedAt = LocalDateTime.now();
        this.systemStatus = "OPERATIONAL";
    }

    public DashboardResponse(Long userId, String userRole) {
        this();
        this.userId = userId;
        this.userRole = userRole;
        this.dashboardType = determineDashboardType(userRole);
    }

    // Utility Methods
    private String determineDashboardType(String userRole) {
        if (userRole == null) return "USER";

        switch (userRole.toUpperCase()) {
            case "SUPER_ADMIN":
            case "ACCOUNT_MANAGER":
                return "ADMIN";
            case "PROJECT_MANAGER":
                return "PROJECT_MANAGER";
            default:
                return "USER";
        }
    }

    /**
     * Calculate remaining budget
     */
    public void calculateRemainingBudget() {
        if (totalBudgetAllocated != null && totalSpentAmount != null) {
            this.remainingBudget = totalBudgetAllocated.subtract(totalSpentAmount);
        }
    }

    /**
     * Calculate budget utilization percentage
     */
    public void calculateBudgetUtilization() {
        if (totalBudgetAllocated != null && totalSpentAmount != null &&
                totalBudgetAllocated.compareTo(BigDecimal.ZERO) > 0) {
            this.budgetUtilizationPercentage = totalSpentAmount
                    .divide(totalBudgetAllocated, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * Determine system health status based on score
     */
    public void updateSystemStatus() {
        if (systemHealthScore != null) {
            double score = systemHealthScore.doubleValue();
            if (score >= 95) {
                this.systemStatus = "EXCELLENT";
            } else if (score >= 85) {
                this.systemStatus = "GOOD";
            } else if (score >= 70) {
                this.systemStatus = "WARNING";
            } else {
                this.systemStatus = "CRITICAL";
            }
        }
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) {
        this.userRole = userRole;
        this.dashboardType = determineDashboardType(userRole);
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getDashboardType() { return dashboardType; }
    public void setDashboardType(String dashboardType) { this.dashboardType = dashboardType; }

    public Long getActiveProjectsCount() { return activeProjectsCount; }
    public void setActiveProjectsCount(Long activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }

    public Long getCompletedProjectsCount() { return completedProjectsCount; }
    public void setCompletedProjectsCount(Long completedProjectsCount) { this.completedProjectsCount = completedProjectsCount; }

    public Long getTotalProjectsCount() { return totalProjectsCount; }
    public void setTotalProjectsCount(Long totalProjectsCount) { this.totalProjectsCount = totalProjectsCount; }

    public Long getMyProjectsCount() { return myProjectsCount; }
    public void setMyProjectsCount(Long myProjectsCount) { this.myProjectsCount = myProjectsCount; }

    public Long getDelayedProjectsCount() { return delayedProjectsCount; }
    public void setDelayedProjectsCount(Long delayedProjectsCount) { this.delayedProjectsCount = delayedProjectsCount; }

    public BigDecimal getTotalBudgetAllocated() { return totalBudgetAllocated; }
    public void setTotalBudgetAllocated(BigDecimal totalBudgetAllocated) {
        this.totalBudgetAllocated = totalBudgetAllocated;
        calculateRemainingBudget();
        calculateBudgetUtilization();
    }

    public BigDecimal getTotalSpentAmount() { return totalSpentAmount; }
    public void setTotalSpentAmount(BigDecimal totalSpentAmount) {
        this.totalSpentAmount = totalSpentAmount;
        calculateRemainingBudget();
        calculateBudgetUtilization();
    }

    public BigDecimal getBudgetUtilizationPercentage() { return budgetUtilizationPercentage; }
    public void setBudgetUtilizationPercentage(BigDecimal budgetUtilizationPercentage) {
        this.budgetUtilizationPercentage = budgetUtilizationPercentage;
    }

    public BigDecimal getMyBudgetUtilization() { return myBudgetUtilization; }
    public void setMyBudgetUtilization(BigDecimal myBudgetUtilization) { this.myBudgetUtilization = myBudgetUtilization; }

    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }

    public Long getPendingApprovalsCount() { return pendingApprovalsCount; }
    public void setPendingApprovalsCount(Long pendingApprovalsCount) { this.pendingApprovalsCount = pendingApprovalsCount; }

    public Long getPendingTasksCount() { return pendingTasksCount; }
    public void setPendingTasksCount(Long pendingTasksCount) { this.pendingTasksCount = pendingTasksCount; }

    public Long getMyApprovalRequestsCount() { return myApprovalRequestsCount; }
    public void setMyApprovalRequestsCount(Long myApprovalRequestsCount) { this.myApprovalRequestsCount = myApprovalRequestsCount; }

    public Long getMyQuotationsCount() { return myQuotationsCount; }
    public void setMyQuotationsCount(Long myQuotationsCount) { this.myQuotationsCount = myQuotationsCount; }

    public Long getPendingQuotationsCount() { return pendingQuotationsCount; }
    public void setPendingQuotationsCount(Long pendingQuotationsCount) { this.pendingQuotationsCount = pendingQuotationsCount; }

    public Long getApprovedQuotationsCount() { return approvedQuotationsCount; }
    public void setApprovedQuotationsCount(Long approvedQuotationsCount) { this.approvedQuotationsCount = approvedQuotationsCount; }

    public Long getMyTeamSize() { return myTeamSize; }
    public void setMyTeamSize(Long myTeamSize) { this.myTeamSize = myTeamSize; }

    public Long getTotalUsersCount() { return totalUsersCount; }
    public void setTotalUsersCount(Long totalUsersCount) { this.totalUsersCount = totalUsersCount; }

    public Long getActiveUsersCount() { return activeUsersCount; }
    public void setActiveUsersCount(Long activeUsersCount) { this.activeUsersCount = activeUsersCount; }

    public BigDecimal getSystemHealthScore() { return systemHealthScore; }
    public void setSystemHealthScore(BigDecimal systemHealthScore) {
        this.systemHealthScore = systemHealthScore;
        updateSystemStatus();
    }

    public String getSystemStatus() { return systemStatus; }
    public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }

    public Double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(Double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public List<Map<String, Object>> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<Map<String, Object>> recentActivities) { this.recentActivities = recentActivities; }

    public Long getUnreadNotificationsCount() { return unreadNotificationsCount; }
    public void setUnreadNotificationsCount(Long unreadNotificationsCount) { this.unreadNotificationsCount = unreadNotificationsCount; }

    public List<Map<String, Object>> getUpcomingDeadlines() { return upcomingDeadlines; }
    public void setUpcomingDeadlines(List<Map<String, Object>> upcomingDeadlines) { this.upcomingDeadlines = upcomingDeadlines; }

    public List<Map<String, Object>> getAlertsSummary() { return alertsSummary; }
    public void setAlertsSummary(List<Map<String, Object>> alertsSummary) { this.alertsSummary = alertsSummary; }

    public List<Map<String, Object>> getBudgetTrends() { return budgetTrends; }
    public void setBudgetTrends(List<Map<String, Object>> budgetTrends) { this.budgetTrends = budgetTrends; }

    public List<Map<String, Object>> getProjectStatusDistribution() { return projectStatusDistribution; }
    public void setProjectStatusDistribution(List<Map<String, Object>> projectStatusDistribution) {
        this.projectStatusDistribution = projectStatusDistribution;
    }

    public Map<String, Object> getMonthlyMetrics() { return monthlyMetrics; }
    public void setMonthlyMetrics(Map<String, Object> monthlyMetrics) { this.monthlyMetrics = monthlyMetrics; }

    public Map<String, Object> getPerformanceIndicators() { return performanceIndicators; }
    public void setPerformanceIndicators(Map<String, Object> performanceIndicators) {
        this.performanceIndicators = performanceIndicators;
    }

    public List<Map<String, Object>> getQuickActions() { return quickActions; }
    public void setQuickActions(List<Map<String, Object>> quickActions) { this.quickActions = quickActions; }

    public Map<String, String> getFrequentlyAccessedLinks() { return frequentlyAccessedLinks; }
    public void setFrequentlyAccessedLinks(Map<String, String> frequentlyAccessedLinks) {
        this.frequentlyAccessedLinks = frequentlyAccessedLinks;
    }

    public Map<String, Object> getWidgetSettings() { return widgetSettings; }
    public void setWidgetSettings(Map<String, Object> widgetSettings) { this.widgetSettings = widgetSettings; }

    public List<String> getEnabledWidgets() { return enabledWidgets; }
    public void setEnabledWidgets(List<String> enabledWidgets) { this.enabledWidgets = enabledWidgets; }

    @Override
    public String toString() {
        return "DashboardResponse{" +
                "userId=" + userId +
                ", userRole='" + userRole + '\'' +
                ", dashboardType='" + dashboardType + '\'' +
                ", activeProjectsCount=" + activeProjectsCount +
                ", totalBudgetAllocated=" + totalBudgetAllocated +
                ", generatedAt=" + generatedAt +
                '}';
    }
}