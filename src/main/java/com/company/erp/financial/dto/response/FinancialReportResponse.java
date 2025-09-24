// FinancialReportResponse.java
package com.company.erp.financial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FinancialReportResponse {
    private String reportId;
    private String reportType;
    private String reportTitle;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Executive summary
    private Map<String, Object> executiveSummary;

    // Financial data
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netAmount;
    private BigDecimal budgetVariance;
    private BigDecimal netIncome;


    // Detailed sections
    private List<ProjectFinancialSummary> projectSummaries;
    private List<CategorySummary> categorySummaries;
    private List<MonthlyBreakdown> monthlyBreakdowns;
    private List<VarianceAnalysis> varianceAnalyses;

    // Charts and visualizations
    private Map<String, Object> chartData;
    private List<String> recommendations;
    private List<String> riskFactors;

    // Metadata
    private Map<String, Object> reportParameters;
    private String exportFormat;
    private String downloadUrl;

    // Inner classes
    public static class ProjectFinancialSummary {
        private Long projectId;
        private String projectName;
        private String managerName;
        private BigDecimal allocatedBudget;
        private BigDecimal spentAmount;
        private BigDecimal remainingBudget;
        private BigDecimal utilizationPercentage;
        private String status;
        private List<String> keyTransactions;

        // Constructors, Getters and Setters
        public ProjectFinancialSummary() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public String getManagerName() { return managerName; }
        public void setManagerName(String managerName) { this.managerName = managerName; }

        public BigDecimal getAllocatedBudget() { return allocatedBudget; }
        public void setAllocatedBudget(BigDecimal allocatedBudget) { this.allocatedBudget = allocatedBudget; }

        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

        public BigDecimal getRemainingBudget() { return remainingBudget; }
        public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }

        public BigDecimal getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(BigDecimal utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public List<String> getKeyTransactions() { return keyTransactions; }
        public void setKeyTransactions(List<String> keyTransactions) { this.keyTransactions = keyTransactions; }
    }

    public static class CategorySummary {
        private String categoryName;
        private BigDecimal totalAmount;
        private BigDecimal budgetedAmount;
        private BigDecimal variance;
        private BigDecimal variancePercentage;
        private int transactionCount;

        // Constructors, Getters and Setters
        public CategorySummary() {}

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getBudgetedAmount() { return budgetedAmount; }
        public void setBudgetedAmount(BigDecimal budgetedAmount) { this.budgetedAmount = budgetedAmount; }

        public BigDecimal getVariance() { return variance; }
        public void setVariance(BigDecimal variance) { this.variance = variance; }

        public BigDecimal getVariancePercentage() { return variancePercentage; }
        public void setVariancePercentage(BigDecimal variancePercentage) { this.variancePercentage = variancePercentage; }

        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    }

    public static class MonthlyBreakdown {
        private String month;
        private int year;
        private BigDecimal totalSpending;
        private BigDecimal budgetedAmount;
        private BigDecimal variance;
        private int projectCount;
        private int transactionCount;

        // Constructors, Getters and Setters
        public MonthlyBreakdown() {}

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public BigDecimal getTotalSpending() { return totalSpending; }
        public void setTotalSpending(BigDecimal totalSpending) { this.totalSpending = totalSpending; }

        public BigDecimal getBudgetedAmount() { return budgetedAmount; }
        public void setBudgetedAmount(BigDecimal budgetedAmount) { this.budgetedAmount = budgetedAmount; }

        public BigDecimal getVariance() { return variance; }
        public void setVariance(BigDecimal variance) { this.variance = variance; }

        public int getProjectCount() { return projectCount; }
        public void setProjectCount(int projectCount) { this.projectCount = projectCount; }

        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    }

    public static class VarianceAnalysis {
        private String analysisType;
        private String description;
        private BigDecimal impact;
        private String severity;
        private List<String> affectedProjects;
        private String recommendation;

        // Constructors, Getters and Setters
        public VarianceAnalysis() {}

        public String getAnalysisType() { return analysisType; }
        public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getImpact() { return impact; }
        public void setImpact(BigDecimal impact) { this.impact = impact; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public List<String> getAffectedProjects() { return affectedProjects; }
        public void setAffectedProjects(List<String> affectedProjects) { this.affectedProjects = affectedProjects; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    // Main class constructors
    public FinancialReportResponse() {}

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }

    // Main class getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public Map<String, Object> getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(Map<String, Object> executiveSummary) { this.executiveSummary = executiveSummary; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public BigDecimal getBudgetVariance() { return budgetVariance; }
    public void setBudgetVariance(BigDecimal budgetVariance) { this.budgetVariance = budgetVariance; }

    public List<ProjectFinancialSummary> getProjectSummaries() { return projectSummaries; }
    public void setProjectSummaries(List<ProjectFinancialSummary> projectSummaries) { this.projectSummaries = projectSummaries; }

    public List<CategorySummary> getCategorySummaries() { return categorySummaries; }
    public void setCategorySummaries(List<CategorySummary> categorySummaries) { this.categorySummaries = categorySummaries; }

    public List<MonthlyBreakdown> getMonthlyBreakdowns() { return monthlyBreakdowns; }
    public void setMonthlyBreakdowns(List<MonthlyBreakdown> monthlyBreakdowns) { this.monthlyBreakdowns = monthlyBreakdowns; }

    public List<VarianceAnalysis> getVarianceAnalyses() { return varianceAnalyses; }
    public void setVarianceAnalyses(List<VarianceAnalysis> varianceAnalyses) { this.varianceAnalyses = varianceAnalyses; }

    public Map<String, Object> getChartData() { return chartData; }
    public void setChartData(Map<String, Object> chartData) { this.chartData = chartData; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public Map<String, Object> getReportParameters() { return reportParameters; }
    public void setReportParameters(Map<String, Object> reportParameters) { this.reportParameters = reportParameters; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}