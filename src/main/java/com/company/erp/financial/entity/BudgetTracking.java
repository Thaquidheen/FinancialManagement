package com.company.erp.financial.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.project.entity.Project;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_tracking")
public class BudgetTracking extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type", nullable = false, length = 30)
    private BudgetTrackingType trackingType;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // QUOTATION, PAYMENT, ADJUSTMENT

    @Column(name = "reference_id")
    private Long referenceId;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "SAR";

    @Column(name = "previous_balance", precision = 15, scale = 2)
    private BigDecimal previousBalance;

    @Column(name = "new_balance", precision = 15, scale = 2)
    private BigDecimal newBalance;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "is_budget_exceeded")
    private Boolean isBudgetExceeded = false;

    @Column(name = "variance_percentage", precision = 5, scale = 2)
    private BigDecimal variancePercentage;

    // Constructors
    public BudgetTracking() {}

    public BudgetTracking(Project project, BudgetTrackingType trackingType, BigDecimal amount) {
        this.project = project;
        this.trackingType = trackingType;
        this.amount = amount;
        this.transactionDate = LocalDateTime.now();
        this.currency = project.getCurrency();
    }

    // Business methods
    public void calculateVariance() {
        if (project != null && project.getAllocatedBudget() != null &&
                project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal variance = project.getSpentAmount().subtract(project.getAllocatedBudget());
            this.variancePercentage = variance.divide(project.getAllocatedBudget(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            this.isBudgetExceeded = variance.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public boolean isOverBudget() {
        return Boolean.TRUE.equals(isBudgetExceeded);
    }

    public boolean isWarningLevel(BigDecimal warningThreshold) {
        if (variancePercentage != null && warningThreshold != null) {
            return variancePercentage.compareTo(warningThreshold) >= 0;
        }
        return false;
    }

    // Getters and Setters
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BudgetTrackingType getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(BudgetTrackingType trackingType) {
        this.trackingType = trackingType;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Boolean getIsBudgetExceeded() {
        return isBudgetExceeded;
    }

    public void setIsBudgetExceeded(Boolean budgetExceeded) {
        isBudgetExceeded = budgetExceeded;
    }

    public BigDecimal getVariancePercentage() {
        return variancePercentage;
    }

    public void setVariancePercentage(BigDecimal variancePercentage) {
        this.variancePercentage = variancePercentage;
    }

    @Override
    public String toString() {
        return "BudgetTracking{" +
                "id=" + getId() +
                ", projectId=" + (project != null ? project.getId() : null) +
                ", trackingType=" + trackingType +
                ", amount=" + amount +
                ", transactionDate=" + transactionDate +
                '}';
    }
}

