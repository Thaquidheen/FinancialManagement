package com.company.erp.report.enums;

public enum ReportType {
    FINANCIAL_DASHBOARD("Financial Dashboard", "Comprehensive financial overview"),
    BUDGET_UTILIZATION("Budget Utilization", "Budget usage analysis across projects"),
    SPENDING_ANALYSIS("Spending Analysis", "Detailed spending patterns and trends"),
    PROJECT_FINANCIAL("Project Financial", "Financial analysis by project"),
    VARIANCE_ANALYSIS("Variance Analysis", "Budget vs actual variance analysis"),
    FORECASTING("Financial Forecasting", "Financial projections and forecasts"),
    EXECUTIVE_SUMMARY("Executive Summary", "High-level executive financial summary"),
    AUDIT_TRAIL("Audit Trail", "Financial transaction audit trail"),
    KPI_REPORT("KPI Report", "Key performance indicators report"),
    COMPLIANCE_REPORT("Compliance Report", "Financial compliance and regulatory report"),
    COMPREHENSIVE("Comprehensive Report", "Complete financial analysis report");

    private final String displayName;
    private final String description;

    ReportType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}