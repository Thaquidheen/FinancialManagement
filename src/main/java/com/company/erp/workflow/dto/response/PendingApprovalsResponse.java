package com.company.erp.workflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PendingApprovalsResponse {

    private Long quotationId;
    private Long projectId;
    private String projectName;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String createdByName;
    private String createdByUsername;
    private LocalDateTime submittedDate;
    private LocalDateTime createdDate;
    private Integer itemCount;
    private Integer daysPending;

    // Priority indicators
    private Boolean isUrgent;
    private Boolean exceedsBudget;
    private String priority; // HIGH, MEDIUM, LOW

    // Budget context
    private BigDecimal projectBudget;
    private BigDecimal remainingBudget;

    // Constructors
    public PendingApprovalsResponse() {}

    // Getters and Setters
    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Integer getDaysPending() {
        return daysPending;
    }

    public void setDaysPending(Integer daysPending) {
        this.daysPending = daysPending;
    }

    public Boolean getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Boolean urgent) {
        isUrgent = urgent;
    }

    public Boolean getExceedsBudget() {
        return exceedsBudget;
    }

    public void setExceedsBudget(Boolean exceedsBudget) {
        this.exceedsBudget = exceedsBudget;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public BigDecimal getProjectBudget() {
        return projectBudget;
    }

    public void setProjectBudget(BigDecimal projectBudget) {
        this.projectBudget = projectBudget;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    @Override
    public String toString() {
        return "PendingApprovalsResponse{" +
                "quotationId=" + quotationId +
                ", projectName='" + projectName + '\'' +
                ", totalAmount=" + totalAmount +
                ", daysPending=" + daysPending +
                ", priority='" + priority + '\'' +
                '}';
    }
}