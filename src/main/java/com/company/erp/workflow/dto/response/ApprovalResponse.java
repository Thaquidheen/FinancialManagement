package com.company.erp.workflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ApprovalResponse {

    private Long id;
    private Long quotationId;
    private String projectName;
    private String quotationDescription;
    private BigDecimal totalAmount;
    private String currency;
    private String quotationStatus;
    private String approverName;
    private String approverUsername;
    private String status;
    private String comments;
    private LocalDateTime approvalDate;
    private LocalDateTime createdDate;
    private Integer levelOrder;

    // Project manager info
    private String createdByName;
    private String createdByUsername;

    // Budget info
    private BigDecimal projectBudget;
    private BigDecimal remainingBudget;
    private Boolean exceedsBudget;

    // Constructors
    public ApprovalResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getQuotationDescription() {
        return quotationDescription;
    }

    public void setQuotationDescription(String quotationDescription) {
        this.quotationDescription = quotationDescription;
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

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApproverUsername() {
        return approverUsername;
    }

    public void setApproverUsername(String approverUsername) {
        this.approverUsername = approverUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(Integer levelOrder) {
        this.levelOrder = levelOrder;
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

    public Boolean getExceedsBudget() {
        return exceedsBudget;
    }

    public void setExceedsBudget(Boolean exceedsBudget) {
        this.exceedsBudget = exceedsBudget;
    }

    @Override
    public String toString() {
        return "ApprovalResponse{" +
                "id=" + id +
                ", quotationId=" + quotationId +
                ", projectName='" + projectName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", approverName='" + approverName + '\'' +
                '}';
    }
}