package com.company.erp.financial.entity;

public enum QuotationStatus {

    DRAFT("Draft", "Quotation is being prepared"),
    SUBMITTED("Submitted", "Quotation submitted for approval"),
    UNDER_REVIEW("Under Review", "Quotation is being reviewed by account manager"),
    APPROVED("Approved", "Quotation has been approved"),
    REJECTED("Rejected", "Quotation has been rejected"),
    PAYMENT_FILE_GENERATED("Payment File Generated", "Payment file has been generated"),
    SENT_TO_BANK("Sent to Bank", "Payment file sent to bank"),
    PAID("Paid", "Payment has been completed");

    private final String displayName;
    private final String description;

    QuotationStatus(String displayName, String description) {
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
    public boolean canBeEdited() {
        return this == DRAFT;
    }

    public boolean canBeSubmitted() {
        return this == DRAFT;
    }

    public boolean canBeApproved() {
        return this == SUBMITTED || this == UNDER_REVIEW;
    }

    public boolean canBeRejected() {
        return this == SUBMITTED || this == UNDER_REVIEW;
    }

    public boolean canBeDeleted() {
        return this == DRAFT;
    }

    public boolean isFinalized() {
        return this == APPROVED || this == REJECTED ||
                this == PAYMENT_FILE_GENERATED ||
                this == SENT_TO_BANK || this == PAID;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isInProgress() {
        return this == SUBMITTED || this == UNDER_REVIEW ||
                this == PAYMENT_FILE_GENERATED || this == SENT_TO_BANK;
    }

    @Override
    public String toString() {
        return displayName;
    }
}