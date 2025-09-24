package com.company.erp.report.entity;

import com.company.erp.common.entity.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "report_configs")
public class ReportConfig extends AuditableEntity {

    @NotNull
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Size(max = 50)
    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Size(max = 50)
    @Column(name = "schedule_frequency", length = 50)
    private String scheduleFrequency;

    @ElementCollection
    @CollectionTable(name = "report_config_parameters", joinColumns = @JoinColumn(name = "config_id"))
    @MapKeyColumn(name = "parameter_key")
    @Column(name = "parameter_value", columnDefinition = "TEXT")
    private Map<String, String> parameters = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "report_config_filters", joinColumns = @JoinColumn(name = "config_id"))
    @MapKeyColumn(name = "filter_key")
    @Column(name = "filter_value", columnDefinition = "TEXT")
    private Map<String, String> defaultFilters = new HashMap<>();

    @Size(max = 20)
    @Column(name = "output_format", length = 20)
    private String outputFormat = "EXCEL";

    @Column(name = "include_charts")
    private Boolean includeCharts = true;

    @Column(name = "include_raw_data")
    private Boolean includeRawData = false;

    @Column(name = "template_path")
    private String templatePath;

    @Column(name = "version")
    private Integer version = 1;

    // Constructors
    public ReportConfig() {}

    public ReportConfig(String name, String reportType) {
        this.name = name;
        this.reportType = reportType;
    }

    // Business methods
    public void addParameter(String key, String value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }

    public void addDefaultFilter(String key, String value) {
        if (this.defaultFilters == null) {
            this.defaultFilters = new HashMap<>();
        }
        this.defaultFilters.put(key, value);
    }

    public String getParameter(String key) {
        return this.parameters != null ? this.parameters.get(key) : null;
    }

    public String getDefaultFilter(String key) {
        return this.defaultFilters != null ? this.defaultFilters.get(key) : null;
    }

    public void incrementVersion() {
        this.version++;
    }

    public boolean isScheduledReport() {
        return Boolean.TRUE.equals(this.isScheduled);
    }

    public boolean isPublicReport() {
        return Boolean.TRUE.equals(this.isPublic);
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Boolean getIsScheduled() { return isScheduled; }
    public void setIsScheduled(Boolean isScheduled) { this.isScheduled = isScheduled; }

    public String getScheduleFrequency() { return scheduleFrequency; }
    public void setScheduleFrequency(String scheduleFrequency) { this.scheduleFrequency = scheduleFrequency; }

    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }

    public Map<String, String> getDefaultFilters() { return defaultFilters; }
    public void setDefaultFilters(Map<String, String> defaultFilters) { this.defaultFilters = defaultFilters; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public Boolean getIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(Boolean includeCharts) { this.includeCharts = includeCharts; }

    public Boolean getIncludeRawData() { return includeRawData; }
    public void setIncludeRawData(Boolean includeRawData) { this.includeRawData = includeRawData; }

    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public String toString() {
        return "ReportConfig{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", reportType='" + reportType + '\'' +
                ", category='" + category + '\'' +
                ", version=" + version +
                '}';
    }
}