package com.company.erp.financial.entity;

public enum BudgetTrackingType {
    BUDGET_ALLOCATION("Budget Allocation", "Initial budget allocation for project"),
    BUDGET_INCREASE("Budget Increase", "Increase in project budget"),
    BUDGET_DECREASE("Budget Decrease", "Decrease in project budget"),
    EXPENSE("Expense", "Regular project expense"),
    QUOTATION_APPROVED("Quotation Approved", "Approved quotation amount"),
    REFUND("Refund", "Refund or reversal of expense"),
    ADJUSTMENT("Adjustment", "Manual budget adjustment"),
    TRANSFER_IN("Transfer In", "Budget transferred from another project"),
    TRANSFER_OUT("Transfer Out", "Budget transferred to another project");

    private final String displayName;
    private final String description;

    BudgetTrackingType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isIncreaseType() {
        return this == BUDGET_ALLOCATION || this == BUDGET_INCREASE ||
                this == REFUND || this == TRANSFER_IN;
    }

    public boolean isDecreaseType() {
        return this == EXPENSE || this == QUOTATION_APPROVED ||
                this == BUDGET_DECREASE || this == TRANSFER_OUT;
    }

    public boolean isNeutralType() {
        return this == ADJUSTMENT;
    }
}