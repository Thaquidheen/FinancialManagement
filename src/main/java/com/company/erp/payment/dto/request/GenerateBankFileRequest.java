package com.company.erp.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class GenerateBankFileRequest {

    @NotEmpty(message = "At least one payment ID is required")
    private List<Long> paymentIds;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Constructors
    public GenerateBankFileRequest() {}

    // Getters and Setters
    public List<Long> getPaymentIds() { return paymentIds; }
    public void setPaymentIds(List<Long> paymentIds) { this.paymentIds = paymentIds; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
