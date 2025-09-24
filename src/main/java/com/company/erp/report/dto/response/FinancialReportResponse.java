//package com.company.erp.financial.dto.response;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//public class FinancialReportResponse {
//
//    private String reportType;
//    private String reportTitle;
//    private String reportDescription;
//    private LocalDateTime startDate;
//    private LocalDateTime endDate;
//    private String generatedBy;
//    private LocalDateTime generatedAt;
//
//    // Summary Data
//    private BigDecimal totalBudgetAllocated;
//    private BigDecimal totalAmountSpent;
//    private BigDecimal totalRemainingBudget;
//    private BigDecimal overallUtilizationPercentage;
//
//    // Detailed Analysis
//    private List<Map<String, Object>> projectAnalysis;
//    private List<Map<String, Object>> categoryAnalysis;
//    private List<Map<String, Object>> departmentAnalysis;
//    private List<Map<String, Object>> timeSeriesAnalysis;
//
//    // Variance Analysis
//    private List<Map<String, Object>> budgetVariances;
//    private Map<String, Object> overallVariance;
//    private List<Map<String, Object>> riskAnalysis;
//
//    // Forecast Data
//    private List<Map<String, Object>> projections;
//    private Map<String, Object> forecastSummary;
//
//    // Performance Metrics
//    private Map<String, Object> kpis;
//    private Map<String, Object> benchmarks;
//    private List<Map<String, Object>> recommendations;
//
//    // Raw Data
//    private List<Object[]> rawData;
//    private Map<String, Object> metadata;
//
//    // Export Information
//    private String exportFormat;
//    private String exportPath;
//    private Long fileSizeBytes;
//
//    // Constructors
//    public FinancialReportResponse() {
//        this.generatedAt = LocalDateTime.now();
//    }
//
//    public FinancialReportResponse(String reportType) {
//        this();
//        this.reportType = reportType;
//        this.reportTitle = generateReportTitle(reportType);
//    }
//
//    // Helper method to generate report title
//    private String generateReportTitle(String reportType) {
//        switch (reportType.toUpperCase()) {
//            case "BUDGET_UTILIZATION":
//                return "Budget Utilization Report";
//            case "SPENDING_ANALYSIS":
//                return "Spending Analysis Report";
//            case "PROJECT_FINANCIAL":
//                return "Project Financial Report";
//            case "COMPREHENSIVE":
//                return "Comprehensive Financial Report";
//            case "VARIANCE_ANALYSIS":
//                return "Budget Variance Analysis Report";
//            default:
//                return "Financial Report";
//        }
//    }
//
//    // Getters and Setters
//    public String getReportType() { return reportType; }
//    public void setReportType(String reportType) { this.reportType = reportType; }
//
//    public String getReportTitle() { return reportTitle; }
//    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }
//
//    public String getReportDescription() { return reportDescription; }
//    public void setReportDescription(String reportDescription) { this.reportDescription = reportDescription; }
//
//    public LocalDateTime getStartDate() { return startDate; }
//    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
//
//    public LocalDateTime getEndDate() { return endDate; }
//    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
//
//    public String getGeneratedBy() { return generatedBy; }
//    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
//
//    public LocalDateTime getGeneratedAt() { return generatedAt; }
//    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
//
//    public BigDecimal getTotalBudgetAllocated() { return totalBudgetAllocated; }
//    public void setTotalBudgetAllocated(BigDecimal totalBudgetAllocated) { this.totalBudgetAllocated = totalBudgetAllocated; }
//
//    public BigDecimal getTotalAmountSpent() { return totalAmountSpent; }
//    public void setTotalAmountSpent(BigDecimal totalAmountSpent) { this.totalAmountSpent = totalAmountSpent; }
//
//    public BigDecimal getTotalRemainingBudget() { return totalRemainingBudget; }
//    public void setTotalRemainingBudget(BigDecimal totalRemainingBudget) { this.totalRemainingBudget = totalRemainingBudget; }
//
//    public BigDecimal getOverallUtilizationPercentage() { return overallUtilizationPercentage; }
//    public void setOverallUtilizationPercentage(BigDecimal overallUtilizationPercentage) { this.overallUtilizationPercentage = overallUtilizationPercentage; }
//
//    public List<Map<String, Object>> getProjectAnalysis() { return projectAnalysis; }
//    public void setProjectAnalysis(List<Map<String, Object>> projectAnalysis) { this.projectAnalysis = projectAnalysis; }
//
//    public List<Map<String, Object>> getCategoryAnalysis() { return categoryAnalysis; }
//    public void setCategoryAnalysis(List<Map<String, Object>> categoryAnalysis) { this.categoryAnalysis = categoryAnalysis; }
//
//    public List<Map<String, Object>> getDepartmentAnalysis() { return departmentAnalysis; }
//    public void setDepartmentAnalysis(List<Map<String, Object>> departmentAnalysis) { this.departmentAnalysis = departmentAnalysis; }
//
//    public List<Map<String, Object>> getTimeSeriesAnalysis() { return timeSeriesAnalysis; }
//    public void setTimeSeriesAnalysis(List<Map<String, Object>> timeSeriesAnalysis) { this.timeSeriesAnalysis = timeSeriesAnalysis; }
//
//    public List<Map<String, Object>> getBudgetVariances() { return budgetVariances; }
//    public void setBudgetVariances(List<Map<String, Object>> budgetVariances) { this.budgetVariances = budgetVariances; }
//
//    public Map<String, Object> getOverallVariance() { return overallVariance; }
//    public void setOverallVariance(Map<String, Object> overallVariance) { this.overallVariance = overallVariance; }
//
//    public List<Map<String, Object>> getRiskAnalysis() { return riskAnalysis; }
//    public void setRiskAnalysis(List<Map<String, Object>> riskAnalysis) { this.riskAnalysis = riskAnalysis; }
//
//    public List<Map<String, Object>> getProjections() { return projections; }
//    public void setProjections(List<Map<String, Object>> projections) { this.projections = projections; }
//
//    public Map<String, Object> getForecastSummary() { return forecastSummary; }
//    public void setForecastSummary(Map<String, Object> forecastSummary) { this.forecastSummary = forecastSummary; }
//
//    public Map<String, Object> getKpis() { return kpis; }
//    public void setKpis(Map<String, Object> kpis) { this.kpis = kpis; }
//
//    public Map<String, Object> getBenchmarks() { return benchmarks; }
//    public void setBenchmarks(Map<String, Object> benchmarks) { this.benchmarks = benchmarks; }
//
//    public List<Map<String, Object>> getRecommendations() { return recommendations; }
//    public void setRecommendations(List<Map<String, Object>> recommendations) { this.recommendations = recommendations; }
//
//    public List<Object[]> getRawData() { return rawData; }
//    public void setRawData(List<Object[]> rawData) { this.rawData = rawData; }
//
//    public Map<String, Object> getMetadata() { return metadata; }
//    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
//
//    public String getExportFormat() { return exportFormat; }
//    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
//
//    public String getExportPath() { return exportPath; }
//    public void setExportPath(String exportPath) { this.exportPath = exportPath; }
//
//    public Long getFileSizeBytes() { return fileSizeBytes; }
//    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
//
//    @Override
//    public String toString() {
//        return "FinancialReportResponse{" +
//                "reportType='" + reportType + '\'' +
//                ", reportTitle='" + reportTitle + '\'' +
//                ", generatedAt=" + generatedAt +
//                ", totalBudgetAllocated=" + totalBudgetAllocated +
//                ", totalAmountSpent=" + totalAmountSpent +
//                '}';
//    }
//}