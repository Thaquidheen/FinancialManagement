package com.company.erp.report.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class AnalyticsResponse {

    private String reportType;
    private String analyticsType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    private String generatedBy;

    // Analytics Data
    private Map<String, Object> data;
    private Map<String, Object> summary;
    private Map<String, Object> trends;
    private Map<String, Object> comparisons;

    // Metadata
    private Long totalRecords;
    private String dataSource;
    private Map<String, Object> filters;
    private String version;

    // Constructors
    public AnalyticsResponse() {
        this.generatedAt = LocalDateTime.now();
        this.version = "1.0";
    }

    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getAnalyticsType() { return analyticsType; }
    public void setAnalyticsType(String analyticsType) { this.analyticsType = analyticsType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public Map<String, Object> getTrends() { return trends; }
    public void setTrends(Map<String, Object> trends) { this.trends = trends; }

    public Map<String, Object> getComparisons() { return comparisons; }
    public void setComparisons(Map<String, Object> comparisons) { this.comparisons = comparisons; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    @Override
    public String toString() {
        return "AnalyticsResponse{" +
                "reportType='" + reportType + '\'' +
                ", analyticsType='" + analyticsType + '\'' +
                ", generatedAt=" + generatedAt +
                '}';
    }
}
