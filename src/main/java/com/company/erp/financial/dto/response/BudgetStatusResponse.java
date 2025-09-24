package com.company.erp.financial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetStatusResponse {

    private Long projectId;
    private String projectName;
    private BigDecimal allocatedBudget;
    private BigDecimal spentAmount;
    private BigDecimal remainingBudget;
    private String currency;
    private BigDecimal utilizationPercentage;
    private String budgetStatus; // NORMAL, WARNING, CRITICAL, OVER_BUDGET
    private Long recentTransactionsCount;
    private LocalDateTime lastTransactionDate;
    private LocalDateTime lastUpdated;

    // Constructors
    public BudgetStatusResponse() {
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public BigDecimal getAllocatedBudget() { return allocatedBudget; }
    public void setAllocatedBudget(BigDecimal allocatedBudget) { this.allocatedBudget = allocatedBudget; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(BigDecimal utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

    public String getBudgetStatus() { return budgetStatus; }
    public void setBudgetStatus(String budgetStatus) { this.budgetStatus = budgetStatus; }

    public Long getRecentTransactionsCount() { return recentTransactionsCount; }
    public void setRecentTransactionsCount(Long recentTransactionsCount) { this.recentTransactionsCount = recentTransactionsCount; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}