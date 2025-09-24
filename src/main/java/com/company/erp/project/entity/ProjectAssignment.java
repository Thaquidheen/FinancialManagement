package com.company.erp.project.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id", "role"}))
public class ProjectAssignment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ProjectRole role;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;

    @Column(name = "unassigned_date")
    private LocalDateTime unassignedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public ProjectAssignment() {}

    public ProjectAssignment(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
        this.assignedDate = LocalDateTime.now();
        this.isActive = true;
    }

    // Business methods
    public void unassign() {
        this.isActive = false;
        this.unassignedDate = LocalDateTime.now();
    }

    public void reassign() {
        this.isActive = true;
        this.unassignedDate = null;
        this.assignedDate = LocalDateTime.now();
    }

    public boolean isCurrentlyAssigned() {
        return this.isActive && this.unassignedDate == null;
    }

    // Getters and Setters
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDateTime getUnassignedDate() {
        return unassignedDate;
    }

    public void setUnassignedDate(LocalDateTime unassignedDate) {
        this.unassignedDate = unassignedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "ProjectAssignment{" +
                "id=" + getId() +
                ", projectId=" + (project != null ? project.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}

