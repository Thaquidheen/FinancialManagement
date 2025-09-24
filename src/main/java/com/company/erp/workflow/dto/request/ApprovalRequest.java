package com.company.erp.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApprovalRequest {

    @NotNull(message = "Quotation ID is required")
    private Long quotationId;

    @NotBlank(message = "Action is required")
    private String action; // APPROVE, REJECT, CHANGES_REQUESTED

    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;

    // Constructors
    public ApprovalRequest() {}

    public ApprovalRequest(Long quotationId, String action, String comments) {
        this.quotationId = quotationId;
        this.action = action;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
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

    @Override
    public String toString() {
        return "ApprovalRequest{" +
                "quotationId=" + quotationId +
                ", action='" + action + '\'' +
                ", comments='" + (comments != null && comments.length() > 50 ?
                comments.substring(0, 50) + "..." : comments) + '\'' +
                '}';
    }
}