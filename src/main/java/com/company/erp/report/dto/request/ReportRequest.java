package com.company.erp.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportRequest {

    @NotBlank(message = "Report type is required")
    @Size(max = 50, message = "Report type cannot exceed 50 characters")
    private String reportType;

    @Size(max = 255, message = "Report title cannot exceed 255 characters")
    private String reportTitle;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    // Filter Parameters
    private List<Long> projectIds;
    private List<Long> userIds;
    private List<String> categories;
    private List<String> departments;
    private List<String> statuses;

    // Grouping and Aggregation
    private String groupBy; // PROJECT, DEPARTMENT, CATEGORY, USER, DATE
    private String aggregationType; // SUM, AVG, COUNT, MIN, MAX
    private String period; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

    // Output Options
    private String outputFormat; // EXCEL, PDF, CSV, JSON
    private boolean includeCharts;
    private boolean includeRawData;
    private boolean includeSummary;

    // Advanced Options
    private Map<String, Object> customFilters;
    private List<String> includeFields;
    private List<String> excludeFields;
    private String sortBy;
    private String sortOrder; // ASC, DESC

    // Scheduling Options
    private boolean isScheduled;
    private String scheduleFrequency; // DAILY, WEEKLY, MONTHLY
    private List<String> emailRecipients;

    // Constructors
    public ReportRequest() {}

    public ReportRequest(String reportType) {
        this.reportType = reportType;
    }

    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<Long> getProjectIds() { return projectIds; }
    public void setProjectIds(List<Long> projectIds) { this.projectIds = projectIds; }

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public List<String> getDepartments() { return departments; }
    public void setDepartments(List<String> departments) { this.departments = departments; }

    public List<String> getStatuses() { return statuses; }
    public void setStatuses(List<String> statuses) { this.statuses = statuses; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }

    public String getAggregationType() { return aggregationType; }
    public void setAggregationType(String aggregationType) { this.aggregationType = aggregationType; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public boolean isIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(boolean includeCharts) { this.includeCharts = includeCharts; }

    public boolean isIncludeRawData() { return includeRawData; }
    public void setIncludeRawData(boolean includeRawData) { this.includeRawData = includeRawData; }

    public boolean isIncludeSummary() { return includeSummary; }
    public void setIncludeSummary(boolean includeSummary) { this.includeSummary = includeSummary; }

    public Map<String, Object> getCustomFilters() { return customFilters; }
    public void setCustomFilters(Map<String, Object> customFilters) { this.customFilters = customFilters; }

    public List<String> getIncludeFields() { return includeFields; }
    public void setIncludeFields(List<String> includeFields) { this.includeFields = includeFields; }

    public List<String> getExcludeFields() { return excludeFields; }
    public void setExcludeFields(List<String> excludeFields) { this.excludeFields = excludeFields; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public boolean isScheduled() { return isScheduled; }
    public void setScheduled(boolean scheduled) { isScheduled = scheduled; }

    public String getScheduleFrequency() { return scheduleFrequency; }
    public void setScheduleFrequency(String scheduleFrequency) { this.scheduleFrequency = scheduleFrequency; }

    public List<String> getEmailRecipients() { return emailRecipients; }
    public void setEmailRecipients(List<String> emailRecipients) { this.emailRecipients = emailRecipients; }

    @Override
    public String toString() {
        return "ReportRequest{" +
                "reportType='" + reportType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", outputFormat='" + outputFormat + '\'' +
                '}';
    }
}