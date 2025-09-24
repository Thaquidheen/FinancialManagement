// ConfirmPaymentRequest.java
package com.company.erp.payment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ConfirmPaymentRequest {

    @NotEmpty(message = "At least one payment ID is required")
    private List<Long> paymentIds;

    @Size(max = 100, message = "Bank reference must not exceed 100 characters")
    private String bankReference;

    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;

    // Constructors
    public ConfirmPaymentRequest() {}

    // Getters and Setters
    public List<Long> getPaymentIds() { return paymentIds; }
    public void setPaymentIds(List<Long> paymentIds) { this.paymentIds = paymentIds; }

    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}