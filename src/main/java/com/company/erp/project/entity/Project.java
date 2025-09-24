package com.company.erp.project.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project extends AuditableEntity {

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 200)
    @Column(name = "location", length = 200)
    private String location;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    @Column(name = "allocated_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedBudget = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    @Column(name = "spent_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    @Column(name = "remaining_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBudget = BigDecimal.ZERO;

    @NotNull
    @Size(max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "SAR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    @Digits(integer = 3, fraction = 2)
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectAssignment> assignments = new HashSet<>();

    // Constructors
    public Project() {}

    public Project(String name, String description, BigDecimal allocatedBudget) {
        this.name = name;
        this.description = description;
        this.allocatedBudget = allocatedBudget;
        this.remainingBudget = allocatedBudget;
    }

    // Business methods
    public void assignManager(User manager) {
        this.manager = manager;
        // Add manager as project assignment too
        addAssignment(manager, ProjectRole.MANAGER);
    }

    public void addAssignment(User user, ProjectRole role) {
        ProjectAssignment assignment = new ProjectAssignment(this, user, role);
        this.assignments.add(assignment);
    }

    public void removeAssignment(User user, ProjectRole role) {
        assignments.removeIf(assignment ->
                assignment.getUser().equals(user) && assignment.getRole().equals(role));
    }

    public void updateBudget(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
        this.remainingBudget = this.allocatedBudget.subtract(spentAmount);
    }

    public void increaseBudget(BigDecimal additionalAmount) {
        this.allocatedBudget = this.allocatedBudget.add(additionalAmount);
        this.remainingBudget = this.allocatedBudget.subtract(this.spentAmount);
    }

    public void decreaseBudget(BigDecimal reductionAmount) {
        if (this.allocatedBudget.subtract(reductionAmount).compareTo(this.spentAmount) >= 0) {
            this.allocatedBudget = this.allocatedBudget.subtract(reductionAmount);
            this.remainingBudget = this.allocatedBudget.subtract(this.spentAmount);
        } else {
            throw new IllegalArgumentException("Cannot reduce budget below spent amount");
        }
    }

    public boolean isOverBudget() {
        return this.spentAmount.compareTo(this.allocatedBudget) > 0;
    }

    public boolean isBudgetWarning(BigDecimal warningThreshold) {
        BigDecimal warningAmount = this.allocatedBudget.multiply(warningThreshold);
        return this.spentAmount.compareTo(warningAmount) >= 0;
    }

    public BigDecimal getBudgetUtilizationPercentage() {
        if (allocatedBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.multiply(new BigDecimal("100")).divide(allocatedBudget, 2, BigDecimal.ROUND_HALF_UP);
    }

    public void updateCompletionPercentage(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) >= 0 && percentage.compareTo(new BigDecimal("100")) <= 0) {
            this.completionPercentage = percentage;

            // Auto-update status based on completion
            if (percentage.compareTo(new BigDecimal("100")) == 0) {
                this.status = ProjectStatus.COMPLETED;
            } else if (this.status == ProjectStatus.COMPLETED && percentage.compareTo(new BigDecimal("100")) < 0) {
                this.status = ProjectStatus.ACTIVE;
            }
        }
    }

    public boolean hasManager() {
        return this.manager != null;
    }

    public boolean isAssigned(User user) {
        return assignments.stream()
                .anyMatch(assignment -> assignment.getUser().equals(user));
    }

    public boolean isManager(User user) {
        return this.manager != null && this.manager.equals(user);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
        // Recalculate remaining budget
        this.remainingBudget = allocatedBudget.subtract(this.spentAmount);
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
        // Recalculate remaining budget
        this.remainingBudget = this.allocatedBudget.subtract(spentAmount);
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(BigDecimal completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public Set<ProjectAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<ProjectAssignment> assignments) {
        this.assignments = assignments;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", allocatedBudget=" + allocatedBudget +
                ", spentAmount=" + spentAmount +
                ", status=" + status +
                ", managerId=" + (manager != null ? manager.getId() : null) +
                '}';
    }
}




