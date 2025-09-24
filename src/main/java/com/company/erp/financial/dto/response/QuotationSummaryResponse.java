package com.company.erp.financial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuotationSummaryResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String createdBy;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private LocalDateTime submittedDate;
    private LocalDateTime createdDate;
    private Integer itemCount;
    private Boolean exceedsBudget;

    // Constructors
    public QuotationSummaryResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getExceedsBudget() {
        return exceedsBudget;
    }

    public void setExceedsBudget(Boolean exceedsBudget) {
        this.exceedsBudget = exceedsBudget;
    }

    @Override
    public String toString() {
        return "QuotationSummaryResponse{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", itemCount=" + itemCount +
                ", exceedsBudget=" + exceedsBudget +
                '}';
    }
}