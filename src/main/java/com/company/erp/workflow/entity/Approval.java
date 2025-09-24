package com.company.erp.workflow.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.financial.entity.Quotation;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
public class Approval extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "level_order")
    private Integer levelOrder = 1;

    // Constructors
    public Approval() {}

    public Approval(Quotation quotation, User approver) {
        this.quotation = quotation;
        this.approver = approver;
        this.status = ApprovalStatus.PENDING;
    }

    // Business methods
    public void approve(String comments) {
        this.status = ApprovalStatus.APPROVED;
        this.comments = comments;
        this.approvalDate = LocalDateTime.now();
    }

    public void reject(String comments) {
        this.status = ApprovalStatus.REJECTED;
        this.comments = comments;
        this.approvalDate = LocalDateTime.now();
    }

    public void requestChanges(String comments) {
        this.status = ApprovalStatus.CHANGES_REQUESTED;
        this.comments = comments;
        this.approvalDate = LocalDateTime.now();
    }

    public boolean isPending() {
        return ApprovalStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return ApprovalStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return ApprovalStatus.REJECTED.equals(this.status);
    }

    public boolean isChangesRequested() {
        return ApprovalStatus.CHANGES_REQUESTED.equals(this.status);
    }

    public boolean isCompleted() {
        return !ApprovalStatus.PENDING.equals(this.status);
    }

    // Getters and Setters
    public Quotation getQuotation() {
        return quotation;
    }

    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Integer getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(Integer levelOrder) {
        this.levelOrder = levelOrder;
    }

    @Override
    public String toString() {
        return "Approval{" +
                "id=" + getId() +
                ", quotationId=" + (quotation != null ? quotation.getId() : null) +
                ", approverId=" + (approver != null ? approver.getId() : null) +
                ", status=" + status +
                ", approvalDate=" + approvalDate +
                '}';
    }
}

