//package com.company.erp.financial.dto.response;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//public class FinancialDashboardResponse {
//
//    // Key Financial Metrics
//    private BigDecimal totalAllocatedBudget;
//    private BigDecimal totalSpentAmount;
//    private BigDecimal totalRemainingBudget;
//    private BigDecimal overallUtilizationPercentage;
//
//    // Quotation Statistics
//    private Long pendingQuotationsCount;
//    private Long approvedQuotationsCount;
//    private Long totalQuotationsCount;
//    private Long rejectedQuotationsCount;
//
//    // Project Statistics
//    private Long activeProjectsCount;
//    private Long completedProjectsCount;
//    private Long totalProjectsCount;
//
//    // Recent Activity
//    private List<Map<String, Object>> recentTransactions;
//    private List<Map<String, Object>> budgetAlerts;
//    private List<Map<String, Object>> topSpendingProjects;
//    private List<Map<String, Object>> projectBudgetStatus;
//
//    // Trend Data
//    private List<Object[]> monthlySpendingTrend;
//    private Map<String, BigDecimal> categoryWiseSpending;
//    private List<Map<String, Object>> departmentWiseSpending;
//
//    // Performance Indicators
//    private BigDecimal budgetAccuracy;
//    private BigDecimal averageApprovalTime;
//    private BigDecimal costEfficiencyRatio;
//    private BigDecimal budgetVariancePercentage;
//
//    // System Health
//    private String systemStatus;
//    private BigDecimal systemHealthScore;
//    private Long totalUsers;
//    private Long activeUsers;
//
//    // Metadata
//    private LocalDateTime generatedAt;
//    private String generatedBy;
//    private String dashboardType;
//
//    // Constructors
//    public FinancialDashboardResponse() {
//        this.generatedAt = LocalDateTime.now();
//        this.dashboardType = "FINANCIAL";
//    }
//
//    // Getters and Setters
//    public BigDecimal getTotalAllocatedBudget() { return totalAllocatedBudget; }
//    public void setTotalAllocatedBudget(BigDecimal totalAllocatedBudget) { this.totalAllocatedBudget = totalAllocatedBudget; }
//
//    public BigDecimal getTotalSpentAmount() { return totalSpentAmount; }
//    public void setTotalSpentAmount(BigDecimal totalSpentAmount) { this.totalSpentAmount = totalSpentAmount; }
//
//    public BigDecimal getTotalRemainingBudget() { return totalRemainingBudget; }
//    public void setTotalRemainingBudget(BigDecimal totalRemainingBudget) { this.totalRemainingBudget = totalRemainingBudget; }
//
//    public BigDecimal getOverallUtilizationPercentage() { return overallUtilizationPercentage; }
//    public void setOverallUtilizationPercentage(BigDecimal overallUtilizationPercentage) { this.overallUtilizationPercentage = overallUtilizationPercentage; }
//
//    public Long getPendingQuotationsCount() { return pendingQuotationsCount; }
//    public void setPendingQuotationsCount(Long pendingQuotationsCount) { this.pendingQuotationsCount = pendingQuotationsCount; }
//
//    public Long getApprovedQuotationsCount() { return approvedQuotationsCount; }
//    public void setApprovedQuotationsCount(Long approvedQuotationsCount) { this.approvedQuotationsCount = approvedQuotationsCount; }
//
//    public Long getTotalQuotationsCount() { return totalQuotationsCount; }
//    public void setTotalQuotationsCount(Long totalQuotationsCount) { this.totalQuotationsCount = totalQuotationsCount; }
//
//    public Long getRejectedQuotationsCount() { return rejectedQuotationsCount; }
//    public void setRejectedQuotationsCount(Long rejectedQuotationsCount) { this.rejectedQuotationsCount = rejectedQuotationsCount; }
//
//    public Long getActiveProjectsCount() { return activeProjectsCount; }
//    public void setActiveProjectsCount(Long activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }
//
//    public Long getCompletedProjectsCount() { return completedProjectsCount; }
//    public void setCompletedProjectsCount(Long completedProjectsCount) { this.completedProjectsCount = completedProjectsCount; }
//
//    public Long getTotalProjectsCount() { return totalProjectsCount; }
//    public void setTotalProjectsCount(Long totalProjectsCount) { this.totalProjectsCount = totalProjectsCount; }
//
//    public List<Map<String, Object>> getRecentTransactions() { return recentTransactions; }
//    public void setRecentTransactions(List<Map<String, Object>> recentTransactions) { this.recentTransactions = recentTransactions; }
//
//    public List<Map<String, Object>> getBudgetAlerts() { return budgetAlerts; }
//    public void setBudgetAlerts(List<Map<String, Object>> budgetAlerts) { this.budgetAlerts = budgetAlerts; }
//
//    public List<Map<String, Object>> getTopSpendingProjects() { return topSpendingProjects; }
//    public void setTopSpendingProjects(List<Map<String, Object>> topSpendingProjects) { this.topSpendingProjects = topSpendingProjects; }
//
//    public List<Map<String, Object>> getProjectBudgetStatus() { return projectBudgetStatus; }
//    public void setProjectBudgetStatus(List<Map<String, Object>> projectBudgetStatus) { this.projectBudgetStatus = projectBudgetStatus; }
//
//    public List<Object[]> getMonthlySpendingTrend() { return monthlySpendingTrend; }
//    public void setMonthlySpendingTrend(List<Object[]> monthlySpendingTrend) { this.monthlySpendingTrend = monthlySpendingTrend; }
//
//    public Map<String, BigDecimal> getCategoryWiseSpending() { return categoryWiseSpending; }
//    public void setCategoryWiseSpending(Map<String, BigDecimal> categoryWiseSpending) { this.categoryWiseSpending = categoryWiseSpending; }
//
//    public List<Map<String, Object>> getDepartmentWiseSpending() { return departmentWiseSpending; }
//    public void setDepartmentWiseSpending(List<Map<String, Object>> departmentWiseSpending) { this.departmentWiseSpending = departmentWiseSpending; }
//
//    public BigDecimal getBudgetAccuracy() { return budgetAccuracy; }
//    public void setBudgetAccuracy(BigDecimal budgetAccuracy) { this.budgetAccuracy = budgetAccuracy; }
//
//    public BigDecimal getAverageApprovalTime() { return averageApprovalTime; }
//    public void setAverageApprovalTime(BigDecimal averageApprovalTime) { this.averageApprovalTime = averageApprovalTime; }
//
//    public BigDecimal getCostEfficiencyRatio() { return costEfficiencyRatio; }
//    public void setCostEfficiencyRatio(BigDecimal costEfficiencyRatio) { this.costEfficiencyRatio = costEfficiencyRatio; }
//
//    public BigDecimal getBudgetVariancePercentage() { return budgetVariancePercentage; }
//    public void setBudgetVariancePercentage(BigDecimal budgetVariancePercentage) { this.budgetVariancePercentage = budgetVariancePercentage; }
//
//    public String getSystemStatus() { return systemStatus; }
//    public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
//
//    public BigDecimal getSystemHealthScore() { return systemHealthScore; }
//    public void setSystemHealthScore(BigDecimal systemHealthScore) { this.systemHealthScore = systemHealthScore; }
//
//    public Long getTotalUsers() { return totalUsers; }
//    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
//
//    public Long getActiveUsers() { return activeUsers; }
//    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
//
//    public LocalDateTime getGeneratedAt() { return generatedAt; }
//    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
//
//    public String getGeneratedBy() { return generatedBy; }
//    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
//
//    public String getDashboardType() { return dashboardType; }
//    public void setDashboardType(String dashboardType) { this.dashboardType = dashboardType; }
//
//    @Override
//    public String toString() {
//        return "FinancialDashboardResponse{" +
//                "totalAllocatedBudget=" + totalAllocatedBudget +
//                ", totalSpentAmount=" + totalSpentAmount +
//                ", overallUtilizationPercentage=" + overallUtilizationPercentage +
//                ", generatedAt=" + generatedAt +
//                '}';
//    }
//}