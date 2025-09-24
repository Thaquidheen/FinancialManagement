package com.company.erp.report.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive response DTO for all report types
 * Supports multiple report formats and data structures
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    // Basic Report Information
    @NotNull
    private String reportId;

    @NotNull
    private String reportType;

    private String reportTitle;
    private String description;
    private String category;

    // Generation Metadata
    private LocalDateTime generatedAt;
    private String generatedBy;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Report Status
    private ReportStatus status;
    private String statusMessage;
    private Double progressPercentage;

    // Executive Summary
    private Map<String, Object> executiveSummary;

    // Main Data Container - flexible structure for different report types
    private Object data;

    // Financial Metrics (for financial reports)
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netIncome;
    private BigDecimal budgetUtilization;

    // Performance Metrics
    private Map<String, Object> kpis;
    private Map<String, Object> performanceMetrics;

    // Charts and Visualizations Data
    private List<ChartData> charts;

    // Data Analysis Results
    private List<DataInsight> insights;
    private List<Recommendation> recommendations;
    private List<Alert> alerts;

    // Export Information
    private ExportInfo exportInfo;

    // Pagination and Filtering
    private PaginationInfo pagination;
    private Map<String, Object> appliedFilters;

    // Raw Data (optional - for detailed reports)
    private List<Map<String, Object>> rawData;

    // Report Configuration Used
    private Map<String, Object> reportConfig;

    // Generation Statistics
    private GenerationStats generationStats;

    // Constructors
    public ReportResponse() {
        this.generatedAt = LocalDateTime.now();
        this.status = ReportStatus.GENERATING;
        this.progressPercentage = 0.0;
        this.executiveSummary = new HashMap<>();
        this.kpis = new HashMap<>();
        this.performanceMetrics = new HashMap<>();
        this.charts = new ArrayList<>();
        this.insights = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.alerts = new ArrayList<>();
        this.appliedFilters = new HashMap<>();
        this.rawData = new ArrayList<>();
        this.reportConfig = new HashMap<>();
    }

    public ReportResponse(String reportType) {
        this();
        this.reportType = reportType;
        this.reportTitle = generateDefaultTitle(reportType);
    }

    // Inner Classes for Complex Data Structures

    /**
     * Chart data structure for visualizations
     */
    public static class ChartData {
        private String chartId;
        private String chartType; // BAR, LINE, PIE, AREA, etc.
        private String title;
        private String description;
        private List<String> labels;
        private List<Dataset> datasets;
        private Map<String, Object> options;

        public static class Dataset {
            private String label;
            private List<Object> data;
            private String backgroundColor;
            private String borderColor;
            private Map<String, Object> properties;

            // Constructors
            public Dataset() {
                this.properties = new HashMap<>();
            }

            public Dataset(String label, List<Object> data) {
                this();
                this.label = label;
                this.data = data;
            }

            // Getters and Setters
            public String getLabel() { return label; }
            public void setLabel(String label) { this.label = label; }

            public List<Object> getData() { return data; }
            public void setData(List<Object> data) { this.data = data; }

            public String getBackgroundColor() { return backgroundColor; }
            public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

            public String getBorderColor() { return borderColor; }
            public void setBorderColor(String borderColor) { this.borderColor = borderColor; }

            public Map<String, Object> getProperties() { return properties; }
            public void setProperties(Map<String, Object> properties) { this.properties = properties; }
        }

        // Constructors
        public ChartData() {
            this.labels = new ArrayList<>();
            this.datasets = new ArrayList<>();
            this.options = new HashMap<>();
        }

        public ChartData(String chartType, String title) {
            this();
            this.chartType = chartType;
            this.title = title;
        }

        // Getters and Setters
        public String getChartId() { return chartId; }
        public void setChartId(String chartId) { this.chartId = chartId; }

        public String getChartType() { return chartType; }
        public void setChartType(String chartType) { this.chartType = chartType; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }

        public List<Dataset> getDatasets() { return datasets; }
        public void setDatasets(List<Dataset> datasets) { this.datasets = datasets; }

        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }

    /**
     * Data insight structure for analytical findings
     */
    public static class DataInsight {
        private String insightId;
        private String type; // TREND, ANOMALY, CORRELATION, etc.
        private String title;
        private String description;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private BigDecimal impact;
        private Map<String, Object> metrics;
        private List<String> affectedAreas;
        private LocalDateTime identifiedAt;

        // Constructors
        public DataInsight() {
            this.metrics = new HashMap<>();
            this.affectedAreas = new ArrayList<>();
            this.identifiedAt = LocalDateTime.now();
        }

        public DataInsight(String type, String title, String description) {
            this();
            this.type = type;
            this.title = title;
            this.description = description;
        }

        // Getters and Setters
        public String getInsightId() { return insightId; }
        public void setInsightId(String insightId) { this.insightId = insightId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public BigDecimal getImpact() { return impact; }
        public void setImpact(BigDecimal impact) { this.impact = impact; }

        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

        public List<String> getAffectedAreas() { return affectedAreas; }
        public void setAffectedAreas(List<String> affectedAreas) { this.affectedAreas = affectedAreas; }

        public LocalDateTime getIdentifiedAt() { return identifiedAt; }
        public void setIdentifiedAt(LocalDateTime identifiedAt) { this.identifiedAt = identifiedAt; }
    }

    /**
     * Recommendation structure for actionable insights
     */
    public static class Recommendation {
        private String recommendationId;
        private String title;
        private String description;
        private String category;
        private String priority; // LOW, MEDIUM, HIGH, URGENT
        private String actionType; // IMMEDIATE, SHORT_TERM, LONG_TERM
        private BigDecimal potentialImpact;
        private String implementationEffort; // LOW, MEDIUM, HIGH
        private List<String> steps;
        private Map<String, Object> metrics;

        // Constructors
        public Recommendation() {
            this.steps = new ArrayList<>();
            this.metrics = new HashMap<>();
        }

        public Recommendation(String title, String description, String priority) {
            this();
            this.title = title;
            this.description = description;
            this.priority = priority;
        }

        // Getters and Setters
        public String getRecommendationId() { return recommendationId; }
        public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public BigDecimal getPotentialImpact() { return potentialImpact; }
        public void setPotentialImpact(BigDecimal potentialImpact) { this.potentialImpact = potentialImpact; }

        public String getImplementationEffort() { return implementationEffort; }
        public void setImplementationEffort(String implementationEffort) { this.implementationEffort = implementationEffort; }

        public List<String> getSteps() { return steps; }
        public void setSteps(List<String> steps) { this.steps = steps; }

        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    }

    /**
     * Alert structure for critical issues
     */
    public static class Alert {
        private String alertId;
        private String type; // WARNING, ERROR, CRITICAL
        private String title;
        private String message;
        private String severity;
        private LocalDateTime alertTime;
        private Map<String, Object> context;
        private List<String> affectedResources;
        private String resolution;

        // Constructors
        public Alert() {
            this.alertTime = LocalDateTime.now();
            this.context = new HashMap<>();
            this.affectedResources = new ArrayList<>();
        }

        public Alert(String type, String title, String message) {
            this();
            this.type = type;
            this.title = title;
            this.message = message;
        }

        // Getters and Setters
        public String getAlertId() { return alertId; }
        public void setAlertId(String alertId) { this.alertId = alertId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getAlertTime() { return alertTime; }
        public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }

        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }

        public List<String> getAffectedResources() { return affectedResources; }
        public void setAffectedResources(List<String> affectedResources) { this.affectedResources = affectedResources; }

        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
    }

    /**
     * Export information structure
     */
    public static class ExportInfo {
        private String fileName;
        private String filePath;
        private String format;
        private Long fileSizeBytes;
        private String downloadUrl;
        private LocalDateTime expiresAt;
        private Boolean compressed;

        // Constructors
        public ExportInfo() {}

        public ExportInfo(String fileName, String format) {
            this.fileName = fileName;
            this.format = format;
        }

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }

        public Long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public Boolean getCompressed() { return compressed; }
        public void setCompressed(Boolean compressed) { this.compressed = compressed; }
    }

    /**
     * Pagination information structure
     */
    public static class PaginationInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;

        // Constructors
        public PaginationInfo() {}

        public PaginationInfo(int currentPage, int totalPages, long totalElements, int pageSize) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.pageSize = pageSize;
            this.hasNext = currentPage < totalPages - 1;
            this.hasPrevious = currentPage > 0;
        }

        // Getters and Setters
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

        public boolean isHasPrevious() { return hasPrevious; }
        public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    }

    /**
     * Generation statistics structure
     */
    public static class GenerationStats {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMillis;
        private int dataPointsProcessed;
        private int queriesExecuted;
        private String processingNode;
        private Map<String, Object> performanceMetrics;

        // Constructors
        public GenerationStats() {
            this.performanceMetrics = new HashMap<>();
        }

        // Getters and Setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public Long getDurationMillis() { return durationMillis; }
        public void setDurationMillis(Long durationMillis) { this.durationMillis = durationMillis; }

        public int getDataPointsProcessed() { return dataPointsProcessed; }
        public void setDataPointsProcessed(int dataPointsProcessed) { this.dataPointsProcessed = dataPointsProcessed; }

        public int getQueriesExecuted() { return queriesExecuted; }
        public void setQueriesExecuted(int queriesExecuted) { this.queriesExecuted = queriesExecuted; }

        public String getProcessingNode() { return processingNode; }
        public void setProcessingNode(String processingNode) { this.processingNode = processingNode; }

        public Map<String, Object> getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(Map<String, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    }

    /**
     * Report status enumeration
     */
    public enum ReportStatus {
        GENERATING,
        COMPLETED,
        FAILED,
        CANCELLED,
        EXPIRED
    }

    // Utility Methods
    private String generateDefaultTitle(String reportType) {
        switch (reportType.toUpperCase()) {
            case "PROJECT_SUMMARY":
                return "Project Summary Report";
            case "FINANCIAL_OVERVIEW":
                return "Financial Overview Report";
            case "BUDGET_UTILIZATION":
                return "Budget Utilization Report";
            case "USER_ACTIVITY":
                return "User Activity Report";
            case "APPROVAL_WORKFLOW":
                return "Approval Workflow Report";
            case "PAYMENT_ANALYSIS":
                return "Payment Analysis Report";
            case "COMPREHENSIVE":
                return "Comprehensive Business Report";
            default:
                return "Custom Report";
        }
    }

    /**
     * Add chart data to the report
     */
    public void addChart(ChartData chart) {
        if (this.charts == null) {
            this.charts = new ArrayList<>();
        }
        this.charts.add(chart);
    }

    /**
     * Add insight to the report
     */
    public void addInsight(DataInsight insight) {
        if (this.insights == null) {
            this.insights = new ArrayList<>();
        }
        this.insights.add(insight);
    }

    /**
     * Add recommendation to the report
     */
    public void addRecommendation(Recommendation recommendation) {
        if (this.recommendations == null) {
            this.recommendations = new ArrayList<>();
        }
        this.recommendations.add(recommendation);
    }

    /**
     * Add alert to the report
     */
    public void addAlert(Alert alert) {
        if (this.alerts == null) {
            this.alerts = new ArrayList<>();
        }
        this.alerts.add(alert);
    }

    /**
     * Add KPI metric
     */
    public void addKPI(String key, Object value) {
        if (this.kpis == null) {
            this.kpis = new HashMap<>();
        }
        this.kpis.put(key, value);
    }

    /**
     * Mark report as completed
     */
    public void markCompleted() {
        this.status = ReportStatus.COMPLETED;
        this.progressPercentage = 100.0;
        this.statusMessage = "Report generation completed successfully";
    }

    /**
     * Mark report as failed
     */
    public void markFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.statusMessage = errorMessage;
    }

    // Main class getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

    public Map<String, Object> getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(Map<String, Object> executiveSummary) { this.executiveSummary = executiveSummary; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getNetIncome() { return netIncome; }
    public void setNetIncome(BigDecimal netIncome) { this.netIncome = netIncome; }

    public BigDecimal getBudgetUtilization() { return budgetUtilization; }
    public void setBudgetUtilization(BigDecimal budgetUtilization) { this.budgetUtilization = budgetUtilization; }

    public Map<String, Object> getKpis() { return kpis; }
    public void setKpis(Map<String, Object> kpis) { this.kpis = kpis; }

    public Map<String, Object> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }

    public List<ChartData> getCharts() { return charts; }
    public void setCharts(List<ChartData> charts) { this.charts = charts; }

    public List<DataInsight> getInsights() { return insights; }
    public void setInsights(List<DataInsight> insights) { this.insights = insights; }

    public List<Recommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }

    public List<Alert> getAlerts() { return alerts; }
    public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }

    public ExportInfo getExportInfo() { return exportInfo; }
    public void setExportInfo(ExportInfo exportInfo) { this.exportInfo = exportInfo; }

    public PaginationInfo getPagination() { return pagination; }
    public void setPagination(PaginationInfo pagination) { this.pagination = pagination; }

    public Map<String, Object> getAppliedFilters() { return appliedFilters; }
    public void setAppliedFilters(Map<String, Object> appliedFilters) { this.appliedFilters = appliedFilters; }

    public List<Map<String, Object>> getRawData() { return rawData; }
    public void setRawData(List<Map<String, Object>> rawData) { this.rawData = rawData; }

    public Map<String, Object> getReportConfig() { return reportConfig; }
    public void setReportConfig(Map<String, Object> reportConfig) { this.reportConfig = reportConfig; }

    public GenerationStats getGenerationStats() { return generationStats; }
    public void setGenerationStats(GenerationStats generationStats) { this.generationStats = generationStats; }

    @Override
    public String toString() {
        return "ReportResponse{" +
                "reportId='" + reportId + '\'' +
                ", reportType='" + reportType + '\'' +
                ", reportTitle='" + reportTitle + '\'' +
                ", status=" + status +
                ", generatedAt=" + generatedAt +
                ", generatedBy='" + generatedBy + '\'' +
                '}';
    }
}