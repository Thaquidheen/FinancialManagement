package com.company.erp.report.enums;

public enum ReportStatus {
    DRAFT("Draft", "Report is being prepared"),
    GENERATING("Generating", "Report is being generated"),
    COMPLETED("Completed", "Report generation completed successfully"),
    FAILED("Failed", "Report generation failed"),
    SCHEDULED("Scheduled", "Report is scheduled for generation"),
    CANCELLED("Cancelled", "Report generation was cancelled"),
    EXPIRED("Expired", "Report has expired and needs regeneration");

    private final String displayName;
    private final String description;

    ReportStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == EXPIRED;
    }

    public boolean isActive() {
        return this == GENERATING || this == SCHEDULED;
    }
}