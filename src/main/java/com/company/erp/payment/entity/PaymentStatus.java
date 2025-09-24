package com.company.erp.payment.entity;

public enum PaymentStatus {
    PENDING("Pending", "Payment is pending processing"),
    READY_FOR_PAYMENT("Ready for Payment", "Payment is ready to be included in bank file"),
    FILE_GENERATED("File Generated", "Payment file has been generated"),
    PROCESSING("Processing", "Payment is being processed"),
    SENT_TO_BANK("Sent to Bank", "Payment file has been sent to bank"),
    PAID("Paid", "Payment has been completed successfully"),
    FAILED("Failed", "Payment has failed"),
    CANCELLED("Cancelled", "Payment has been cancelled"),
    ON_HOLD("On Hold", "Payment is on hold pending review");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // Business logic methods
    public boolean isCompleted() {
        return this == PAID;
    }

    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }

    public boolean canBeProcessed() {
        return this == PENDING || this == READY_FOR_PAYMENT;
    }

    public boolean canBeRetried() {
        return this == FAILED;
    }

    public boolean isInProgress() {
        return this == PROCESSING || this == SENT_TO_BANK || this == FILE_GENERATED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}