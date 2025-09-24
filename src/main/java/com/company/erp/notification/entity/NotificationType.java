package com.company.erp.notification.entity;

public enum NotificationType {
    // User Management
    USER_CREATED("User Created", "New user account created"),
    USER_UPDATED("User Updated", "User account updated"),
    PASSWORD_CHANGED("Password Changed", "Password changed successfully"),
    ACCOUNT_LOCKED("Account Locked", "Account locked due to security reasons"),
    ACCOUNT_UNLOCKED("Account Unlocked", "Account unlocked"),

    // Authentication & Security
    LOGIN_SUCCESS("Login Success", "Successful login detected"),
    LOGIN_FAILED("Login Failed", "Failed login attempt"),
    SESSION_EXPIRED("Session Expired", "Your session has expired"),

    // Project Management
    PROJECT_CREATED("Project Created", "New project created"),
    PROJECT_UPDATED("Project Updated", "Project information updated"),
    PROJECT_ASSIGNED("Project Assigned", "Project assigned to you"),
    PROJECT_DEADLINE_APPROACHING("Project Deadline", "Project deadline approaching"),
    PROJECT_COMPLETED("Project Completed", "Project marked as completed"),
    PROJECT_CANCELLED("Project Cancelled", "Project has been cancelled"),

    // Budget Management
    BUDGET_ALLOCATED("Budget Allocated", "Budget allocated to project"),
    BUDGET_WARNING("Budget Warning", "Budget utilization warning"),
    BUDGET_CRITICAL("Budget Critical", "Critical budget utilization"),
    BUDGET_EXCEEDED("Budget Exceeded", "Budget limit exceeded"),
    BUDGET_UPDATED("Budget Updated", "Project budget updated"),

    // Quotation Management
    QUOTATION_CREATED("Quotation Created", "New quotation created"),
    QUOTATION_SUBMITTED("Quotation Submitted", "Quotation submitted for approval"),
    QUOTATION_APPROVED("Quotation Approved", "Quotation approved"),
    QUOTATION_REJECTED("Quotation Rejected", "Quotation rejected"),
    QUOTATION_UPDATED("Quotation Updated", "Quotation updated"),
    QUOTATION_EXPIRED("Quotation Expired", "Quotation has expired"),

    // Approval Workflow
    APPROVAL_REQUEST("Approval Request", "New approval request"),
    APPROVAL_PENDING("Approval Pending", "Approval pending your action"),
    APPROVAL_APPROVED("Approval Approved", "Request approved"),
    APPROVAL_REJECTED("Approval Rejected", "Request rejected"),
    APPROVAL_DELEGATED("Approval Delegated", "Approval delegated"),

    // Payment Processing
    PAYMENT_CREATED("Payment Created", "Payment created"),
    PAYMENT_FILE_GENERATED("Payment File Generated", "Bank file generated"),
    PAYMENT_SENT_TO_BANK("Payment Sent", "Payment sent to bank"),
    PAYMENT_COMPLETED("Payment Completed", "Payment completed successfully"),
    PAYMENT_FAILED("Payment Failed", "Payment processing failed"),

    // Document Management
    DOCUMENT_UPLOADED("Document Uploaded", "New document uploaded"),
    DOCUMENT_SHARED("Document Shared", "Document shared with you"),
    DOCUMENT_UPDATED("Document Updated", "Document updated"),
    DOCUMENT_EXPIRED("Document Expired", "Document expired"),

    // System Notifications
    SYSTEM_MAINTENANCE("System Maintenance", "System maintenance scheduled"),
    SYSTEM_UPDATE("System Update", "System update available"),
    SYSTEM_ERROR("System Error", "System error occurred"),
    DATA_BACKUP("Data Backup", "Data backup completed"),

    // Reports & Analytics
    REPORT_GENERATED("Report Generated", "Report generated successfully"),
    REPORT_FAILED("Report Failed", "Report generation failed"),
    DASHBOARD_UPDATE("Dashboard Update", "Dashboard data updated"),

    // General
    REMINDER("Reminder", "General reminder notification"),
    ANNOUNCEMENT("Announcement", "System announcement"),
    WELCOME("Welcome", "Welcome to the system"),
    DAILY_SUMMARY("Daily Summary", "Daily activity summary"),
    WEEKLY_SUMMARY("Weekly Summary", "Weekly activity summary");

    private final String displayName;
    private final String description;

    NotificationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // Helper methods to categorize notification types
    public boolean isSecurityRelated() {
        return this == LOGIN_SUCCESS || this == LOGIN_FAILED ||
                this == PASSWORD_CHANGED || this == ACCOUNT_LOCKED ||
                this == ACCOUNT_UNLOCKED || this == SESSION_EXPIRED;
    }

    public boolean isProjectRelated() {
        return name().startsWith("PROJECT_");
    }

    public boolean isBudgetRelated() {
        return name().startsWith("BUDGET_");
    }

    public boolean isQuotationRelated() {
        return name().startsWith("QUOTATION_");
    }

    public boolean isApprovalRelated() {
        return name().startsWith("APPROVAL_");
    }

    public boolean isPaymentRelated() {
        return name().startsWith("PAYMENT_");
    }

    public boolean isSystemRelated() {
        return name().startsWith("SYSTEM_");
    }

    public boolean requiresImmediateAction() {
        return this == APPROVAL_PENDING || this == BUDGET_CRITICAL ||
                this == BUDGET_EXCEEDED || this == SYSTEM_ERROR ||
                this == PAYMENT_FAILED || this == ACCOUNT_LOCKED;
    }
}