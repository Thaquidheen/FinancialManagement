package com.company.erp.common.dto;

import java.util.Map;

public class FilterResult {
    private Map<String, Object> availableFilters;
    private Map<String, Object> appliedFilters;
    private long totalResults;
    private long filteredResults;

    // Constructors
    public FilterResult() {}

    // Getters and Setters
    public Map<String, Object> getAvailableFilters() { return availableFilters; }
    public void setAvailableFilters(Map<String, Object> availableFilters) { this.availableFilters = availableFilters; }

    public Map<String, Object> getAppliedFilters() { return appliedFilters; }
    public void setAppliedFilters(Map<String, Object> appliedFilters) { this.appliedFilters = appliedFilters; }

    public long getTotalResults() { return totalResults; }
    public void setTotalResults(long totalResults) { this.totalResults = totalResults; }

    public long getFilteredResults() { return filteredResults; }
    public void setFilteredResults(long filteredResults) { this.filteredResults = filteredResults; }
}