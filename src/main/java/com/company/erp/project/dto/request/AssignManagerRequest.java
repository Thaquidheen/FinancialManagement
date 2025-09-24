package com.company.erp.project.dto.request;


import jakarta.validation.constraints.NotNull;

// Assign Manager Request DTO
public class AssignManagerRequest {

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    // Constructors
    public AssignManagerRequest() {}

    public AssignManagerRequest(Long managerId) {
        this.managerId = managerId;
    }

    // Getters and Setters
    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
}

