package com.company.erp.financial.dto.request;

import jakarta.validation.constraints.Size;

public class SubmitQuotationRequest {

    @Size(max = 1000, message = "Submission notes must not exceed 1000 characters")
    private String submissionNotes;

    // Constructors
    public SubmitQuotationRequest() {}

    public SubmitQuotationRequest(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    // Getters and Setters
    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    @Override
    public String toString() {
        return "SubmitQuotationRequest{" +
                "submissionNotes='" + (submissionNotes != null && submissionNotes.length() > 50 ?
                submissionNotes.substring(0, 50) + "..." : submissionNotes) + '\'' +
                '}';
    }
}