package com.company.erp.project.dto.request;

import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private String status;

    private String reason;

    // Constructors
    public UpdateStatusRequest() {}

    public UpdateStatusRequest(String status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
