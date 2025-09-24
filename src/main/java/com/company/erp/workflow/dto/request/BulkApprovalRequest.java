package com.company.erp.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class BulkApprovalRequest {

    @NotEmpty(message = "At least one quotation ID is required")
    private List<Long> quotationIds;

    @NotBlank(message = "Action is required")
    private String action; // APPROVE, REJECT, CHANGES_REQUESTED

    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;

    // Optional filters for conditional bulk operations
    private Long projectId;
    private BigDecimal maxAmount;
    private String status;

    // Constructors
    public BulkApprovalRequest() {}

    public BulkApprovalRequest(List<Long> quotationIds, String action, String comments) {
        this.quotationIds = quotationIds;
        this.action = action;
        this.comments = comments;
    }

    // Getters and Setters
    public List<Long> getQuotationIds() {
        return quotationIds;
    }

    public void setQuotationIds(List<Long> quotationIds) {
        this.quotationIds = quotationIds;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BulkApprovalRequest{" +
                "quotationIds=" + (quotationIds != null ? quotationIds.size() : 0) + " items" +
                ", action='" + action + '\'' +
                ", projectId=" + projectId +
                ", maxAmount=" + maxAmount +
                '}';
    }
}