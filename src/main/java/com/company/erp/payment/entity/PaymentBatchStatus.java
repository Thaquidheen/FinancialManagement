package com.company.erp.payment.entity;

public enum PaymentBatchStatus {
    DRAFT("Draft", "Batch is being prepared"),
    FILE_GENERATED("File Generated", "Payment file has been generated"),
    SENT_TO_BANK("Sent to Bank", "Batch has been sent to bank"),
    COMPLETED("Completed", "All payments in batch are completed"),
    CANCELLED("Cancelled", "Batch has been cancelled");

    private final String displayName;
    private final String description;

    PaymentBatchStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
