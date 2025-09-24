// FinancialDashboardResponse.java
package com.company.erp.financial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FinancialDashboardResponse {
    // Overview metrics
    private BigDecimal totalBudgetAllocated;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private BigDecimal overallUtilizationPercentage;
    private int totalProjects;
    private int activeProjects;
    private int projectsOverBudget;
    private int projectsAtRisk;

    // Recent activity
    private List<RecentTransaction> recentTransactions;
    private List<BudgetAlertSummary> budgetAlerts;

    // Charts data
    private List<SpendingTrendData> spendingTrends;
    private List<BudgetUtilizationData> budgetUtilization;
    private List<CategorySpendingData> categorySpending;
    private List<ProjectPerformanceData> projectPerformance;

    // Financial metrics
    private BigDecimal averageProjectBudget;
    private BigDecimal averageSpendingPerProject;
    private BigDecimal totalPendingApprovals;
    private BigDecimal totalPendingPayments;

    // Time period info
    private LocalDateTime reportGeneratedAt;
    private LocalDateTime dataAsOf;
    private String reportPeriod;

    // Risk indicators
    private List<RiskIndicator> riskIndicators;
    private String overallRiskLevel;
    private BigDecimal riskScore;

    // Inner classes for structured data
    public static class RecentTransaction {
        private Long id;
        private String projectName;
        private String transactionType;
        private BigDecimal amount;
        private String description;
        private LocalDateTime transactionDate;
        private String status;

        // Constructors, Getters and Setters
        public RecentTransaction() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class BudgetAlertSummary {
        private Long projectId;
        private String projectName;
        private String alertLevel;
        private String message;
        private BigDecimal utilizationPercentage;
        private LocalDateTime alertTime;

        // Constructors, Getters and Setters
        public BudgetAlertSummary() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public String getAlertLevel() { return alertLevel; }
        public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public BigDecimal getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(BigDecimal utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

        public LocalDateTime getAlertTime() { return alertTime; }
        public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }
    }

    public static class SpendingTrendData {
        private String period;
        private BigDecimal amount;
        private BigDecimal cumulativeAmount;
        private int transactionCount;

        // Constructors, Getters and Setters
        public SpendingTrendData() {}

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public BigDecimal getCumulativeAmount() { return cumulativeAmount; }
        public void setCumulativeAmount(BigDecimal cumulativeAmount) { this.cumulativeAmount = cumulativeAmount; }

        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    }

    public static class BudgetUtilizationData {
        private Long projectId;
        private String projectName;
        private BigDecimal allocated;
        private BigDecimal spent;
        private BigDecimal remaining;
        private BigDecimal utilizationPercentage;
        private String status;

        // Constructors, Getters and Setters
        public BudgetUtilizationData() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public BigDecimal getAllocated() { return allocated; }
        public void setAllocated(BigDecimal allocated) { this.allocated = allocated; }

        public BigDecimal getSpent() { return spent; }
        public void setSpent(BigDecimal spent) { this.spent = spent; }

        public BigDecimal getRemaining() { return remaining; }
        public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }

        public BigDecimal getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(BigDecimal utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class CategorySpendingData {
        private String category;
        private BigDecimal amount;
        private BigDecimal percentage;
        private int transactionCount;

        // Constructors, Getters and Setters
        public CategorySpendingData() {}

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    }

    public static class ProjectPerformanceData {
        private Long projectId;
        private String projectName;
        private String managerName;
        private BigDecimal budgetEfficiency;
        private BigDecimal timeEfficiency;
        private BigDecimal overallPerformance;
        private String riskLevel;

        // Constructors, Getters and Setters
        public ProjectPerformanceData() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public String getManagerName() { return managerName; }
        public void setManagerName(String managerName) { this.managerName = managerName; }

        public BigDecimal getBudgetEfficiency() { return budgetEfficiency; }
        public void setBudgetEfficiency(BigDecimal budgetEfficiency) { this.budgetEfficiency = budgetEfficiency; }

        public BigDecimal getTimeEfficiency() { return timeEfficiency; }
        public void setTimeEfficiency(BigDecimal timeEfficiency) { this.timeEfficiency = timeEfficiency; }

        public BigDecimal getOverallPerformance() { return overallPerformance; }
        public void setOverallPerformance(BigDecimal overallPerformance) { this.overallPerformance = overallPerformance; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class RiskIndicator {
        private String riskType;
        private String description;
        private String severity;
        private BigDecimal impactAmount;
        private List<Long> affectedProjects;
        private String recommendedAction;

        // Constructors, Getters and Setters
        public RiskIndicator() {}

        public String getRiskType() { return riskType; }
        public void setRiskType(String riskType) { this.riskType = riskType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public BigDecimal getImpactAmount() { return impactAmount; }
        public void setImpactAmount(BigDecimal impactAmount) { this.impactAmount = impactAmount; }

        public List<Long> getAffectedProjects() { return affectedProjects; }
        public void setAffectedProjects(List<Long> affectedProjects) { this.affectedProjects = affectedProjects; }

        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }

    // Main class constructors
    public FinancialDashboardResponse() {}

    // Main class getters and setters
    public BigDecimal getTotalBudgetAllocated() { return totalBudgetAllocated; }
    public void setTotalBudgetAllocated(BigDecimal totalBudgetAllocated) { this.totalBudgetAllocated = totalBudgetAllocated; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public BigDecimal getTotalRemaining() { return totalRemaining; }
    public void setTotalRemaining(BigDecimal totalRemaining) { this.totalRemaining = totalRemaining; }

    public BigDecimal getOverallUtilizationPercentage() { return overallUtilizationPercentage; }
    public void setOverallUtilizationPercentage(BigDecimal overallUtilizationPercentage) { this.overallUtilizationPercentage = overallUtilizationPercentage; }

    public int getTotalProjects() { return totalProjects; }
    public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }

    public int getActiveProjects() { return activeProjects; }
    public void setActiveProjects(int activeProjects) { this.activeProjects = activeProjects; }

    public int getProjectsOverBudget() { return projectsOverBudget; }
    public void setProjectsOverBudget(int projectsOverBudget) { this.projectsOverBudget = projectsOverBudget; }

    public int getProjectsAtRisk() { return projectsAtRisk; }
    public void setProjectsAtRisk(int projectsAtRisk) { this.projectsAtRisk = projectsAtRisk; }

    public List<RecentTransaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<RecentTransaction> recentTransactions) { this.recentTransactions = recentTransactions; }

    public List<BudgetAlertSummary> getBudgetAlerts() { return budgetAlerts; }
    public void setBudgetAlerts(List<BudgetAlertSummary> budgetAlerts) { this.budgetAlerts = budgetAlerts; }

    public List<SpendingTrendData> getSpendingTrends() { return spendingTrends; }
    public void setSpendingTrends(List<SpendingTrendData> spendingTrends) { this.spendingTrends = spendingTrends; }

    public List<BudgetUtilizationData> getBudgetUtilization() { return budgetUtilization; }
    public void setBudgetUtilization(List<BudgetUtilizationData> budgetUtilization) { this.budgetUtilization = budgetUtilization; }

    public List<CategorySpendingData> getCategorySpending() { return categorySpending; }
    public void setCategorySpending(List<CategorySpendingData> categorySpending) { this.categorySpending = categorySpending; }

    public List<ProjectPerformanceData> getProjectPerformance() { return projectPerformance; }
    public void setProjectPerformance(List<ProjectPerformanceData> projectPerformance) { this.projectPerformance = projectPerformance; }

    public BigDecimal getAverageProjectBudget() { return averageProjectBudget; }
    public void setAverageProjectBudget(BigDecimal averageProjectBudget) { this.averageProjectBudget = averageProjectBudget; }

    public BigDecimal getAverageSpendingPerProject() { return averageSpendingPerProject; }
    public void setAverageSpendingPerProject(BigDecimal averageSpendingPerProject) { this.averageSpendingPerProject = averageSpendingPerProject; }

    public BigDecimal getTotalPendingApprovals() { return totalPendingApprovals; }
    public void setTotalPendingApprovals(BigDecimal totalPendingApprovals) { this.totalPendingApprovals = totalPendingApprovals; }

    public BigDecimal getTotalPendingPayments() { return totalPendingPayments; }
    public void setTotalPendingPayments(BigDecimal totalPendingPayments) { this.totalPendingPayments = totalPendingPayments; }

    public LocalDateTime getReportGeneratedAt() { return reportGeneratedAt; }
    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) { this.reportGeneratedAt = reportGeneratedAt; }

    public LocalDateTime getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(LocalDateTime dataAsOf) { this.dataAsOf = dataAsOf; }

    public String getReportPeriod() { return reportPeriod; }
    public void setReportPeriod(String reportPeriod) { this.reportPeriod = reportPeriod; }

    public List<RiskIndicator> getRiskIndicators() { return riskIndicators; }
    public void setRiskIndicators(List<RiskIndicator> riskIndicators) { this.riskIndicators = riskIndicators; }

    public String getOverallRiskLevel() { return overallRiskLevel; }
    public void setOverallRiskLevel(String overallRiskLevel) { this.overallRiskLevel = overallRiskLevel; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }
}

