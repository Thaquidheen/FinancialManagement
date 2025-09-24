package com.company.erp.project.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Create Project Request DTO
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @NotNull(message = "Allocated budget is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Budget must be positive")
    @Digits(integer = 13, fraction = 2, message = "Budget format is invalid")
    private BigDecimal allocatedBudget;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "SAR";

    private LocalDate startDate;

    private LocalDate endDate;

    // Constructors
    public CreateProjectRequest() {}

    public CreateProjectRequest(String name, String description, BigDecimal allocatedBudget) {
        this.name = name;
        this.description = description;
        this.allocatedBudget = allocatedBudget;
    }

    // Validation method
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateValid() {
        if (startDate == null || endDate == null) {
            return true; // Let other validations handle null cases
        }
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "CreateProjectRequest{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", allocatedBudget=" + allocatedBudget +
                ", currency='" + currency + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}




