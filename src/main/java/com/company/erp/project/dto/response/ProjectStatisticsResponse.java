package com.company.erp.project.dto.response;

import java.math.BigDecimal;

public class ProjectStatisticsResponse {

    private long totalProjects;
    private long activeProjects;
    private long completedProjects;
    private long onHoldProjects;
    private long cancelledProjects;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal budgetUtilization;
    private BigDecimal averageCompletion;
    private java.util.Map<String, Long> managerWorkload;
    private java.util.Map<String, Object> departmentStatistics;

    public ProjectStatisticsResponse() {}

    // Getters and Setters
    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public long getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(long activeProjects) {
        this.activeProjects = activeProjects;
    }

    public long getCompletedProjects() {
        return completedProjects;
    }

    public void setCompletedProjects(long completedProjects) {
        this.completedProjects = completedProjects;
    }

    public long getOnHoldProjects() {
        return onHoldProjects;
    }

    public void setOnHoldProjects(long onHoldProjects) {
        this.onHoldProjects = onHoldProjects;
    }

    public long getCancelledProjects() {
        return cancelledProjects;
    }

    public void setCancelledProjects(long cancelledProjects) {
        this.cancelledProjects = cancelledProjects;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getBudgetUtilization() {
        return budgetUtilization;
    }

    public void setBudgetUtilization(BigDecimal budgetUtilization) {
        this.budgetUtilization = budgetUtilization;
    }

    public BigDecimal getAverageCompletion() {
        return averageCompletion;
    }

    public void setAverageCompletion(BigDecimal averageCompletion) {
        this.averageCompletion = averageCompletion;
    }

    public java.util.Map<String, Long> getManagerWorkload() {
        return managerWorkload;
    }

    public void setManagerWorkload(java.util.Map<String, Long> managerWorkload) {
        this.managerWorkload = managerWorkload;
    }

    public java.util.Map<String, Object> getDepartmentStatistics() {
        return departmentStatistics;
    }

    public void setDepartmentStatistics(java.util.Map<String, Object> departmentStatistics) {
        this.departmentStatistics = departmentStatistics;
    }
}
