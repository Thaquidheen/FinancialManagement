// FilterRequest.java
package com.company.erp.common.dto;

import java.util.Map;

public class FilterRequest {
    private Map<String, Object> filters;
    private String sortBy;
    private String sortOrder = "asc";

    // Constructors
    public FilterRequest() {}

    public FilterRequest(Map<String, Object> filters) {
        this.filters = filters;
    }

    // Getters and Setters
    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}