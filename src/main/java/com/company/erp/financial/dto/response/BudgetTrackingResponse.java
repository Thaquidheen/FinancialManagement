// BudgetTrackingResponse.java
package com.company.erp.financial.dto.response;

import com.company.erp.financial.entity.BudgetTrackingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetTrackingResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private BudgetTrackingType trackingType;
    private String trackingTypeDisplay;
    private String referenceType;
    private Long referenceId;
    private BigDecimal amount;
    private String currency;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private String description;
    private String category;
    private String recordedByName;
    private Long recordedById;
    private LocalDateTime transactionDate;
    private Boolean isBudgetExceeded;
    private BigDecimal variancePercentage;
    private LocalDateTime createdDate;

    // Constructors
    public BudgetTrackingResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public BudgetTrackingType getTrackingType() { return trackingType; }
    public void setTrackingType(BudgetTrackingType trackingType) {
        this.trackingType = trackingType;
        this.trackingTypeDisplay = trackingType != null ? trackingType.getDisplayName() : null;
    }

    public String getTrackingTypeDisplay() { return trackingTypeDisplay; }
    public void setTrackingTypeDisplay(String trackingTypeDisplay) { this.trackingTypeDisplay = trackingTypeDisplay; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getPreviousBalance() { return previousBalance; }
    public void setPreviousBalance(BigDecimal previousBalance) { this.previousBalance = previousBalance; }

    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRecordedByName() { return recordedByName; }
    public void setRecordedByName(String recordedByName) { this.recordedByName = recordedByName; }

    public Long getRecordedById() { return recordedById; }
    public void setRecordedById(Long recordedById) { this.recordedById = recordedById; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public Boolean getIsBudgetExceeded() { return isBudgetExceeded; }
    public void setIsBudgetExceeded(Boolean budgetExceeded) { this.isBudgetExceeded = budgetExceeded; }

    public BigDecimal getVariancePercentage() { return variancePercentage; }
    public void setVariancePercentage(BigDecimal variancePercentage) { this.variancePercentage = variancePercentage; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}