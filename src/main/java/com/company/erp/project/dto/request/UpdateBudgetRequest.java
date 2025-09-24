package com.company.erp.project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Update Budget Request DTO
public class UpdateBudgetRequest {

    @NotNull(message = "Allocated budget is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Budget must be positive")
    @Digits(integer = 13, fraction = 2, message = "Budget format is invalid")
    private BigDecimal allocatedBudget;

    private String reason;

    // Constructors
    public UpdateBudgetRequest() {}

    public UpdateBudgetRequest(BigDecimal allocatedBudget, String reason) {
        this.allocatedBudget = allocatedBudget;
        this.reason = reason;
    }

    // Getters and Setters
    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

