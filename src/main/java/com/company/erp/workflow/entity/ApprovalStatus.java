package com.company.erp.workflow.entity;

public enum ApprovalStatus {
    PENDING("Pending", "Awaiting approval decision"),
    APPROVED("Approved", "Quotation has been approved"),
    REJECTED("Rejected", "Quotation has been rejected"),
    CHANGES_REQUESTED("Changes Requested", "Approver has requested changes");

    private final String displayName;
    private final String description;

    ApprovalStatus(String displayName, String description) {
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