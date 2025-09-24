package com.company.erp.user.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

// Assign Roles Request DTO
public class AssignRolesRequest {

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    public AssignRolesRequest() {}

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
