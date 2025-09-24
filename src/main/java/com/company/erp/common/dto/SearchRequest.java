package com.company.erp.common.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class SearchRequest {
    private String query;
    private Set<String> searchTypes; // PROJECT, QUOTATION, DOCUMENT, USER
    private Set<String> entityTypes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Object> customFilters;
    private String sortBy;
    private String sortOrder;
    private boolean includeDeleted = false;

    // Constructors
    public SearchRequest() {}

    public SearchRequest(String query) {
        this.query = query;
    }

    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Set<String> getSearchTypes() { return searchTypes; }
    public void setSearchTypes(Set<String> searchTypes) { this.searchTypes = searchTypes; }

    public Set<String> getEntityTypes() { return entityTypes; }
    public void setEntityTypes(Set<String> entityTypes) { this.entityTypes = entityTypes; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Map<String, Object> getCustomFilters() { return customFilters; }
    public void setCustomFilters(Map<String, Object> customFilters) { this.customFilters = customFilters; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public boolean isIncludeDeleted() { return includeDeleted; }
    public void setIncludeDeleted(boolean includeDeleted) { this.includeDeleted = includeDeleted; }
}