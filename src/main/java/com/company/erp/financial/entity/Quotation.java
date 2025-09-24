package com.company.erp.financial.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.project.entity.Project;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
public class Quotation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User creator;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Size(max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "SAR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<QuotationItem> items = new ArrayList<>();

    // Constructors
    public Quotation() {}

    public Quotation(Project project, User creator) {
        this.project = project;
        this.creator = creator;
    }

    // Business methods
    public void addItem(QuotationItem item) {
        items.add(item);
        item.setQuotation(this);
        updateTotalAmount();
    }

    public void removeItem(QuotationItem item) {
        items.remove(item);
        item.setQuotation(null);
        updateTotalAmount();
    }

    public void updateTotalAmount() {
        this.totalAmount = items.stream()
                .map(QuotationItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isDraft() {
        return QuotationStatus.DRAFT.equals(this.status);
    }

    public boolean isSubmitted() {
        return QuotationStatus.SUBMITTED.equals(this.status) ||
                QuotationStatus.UNDER_REVIEW.equals(this.status);
    }

    public boolean isApproved() {
        return QuotationStatus.APPROVED.equals(this.status);
    }

    public boolean canBeEdited() {
        return QuotationStatus.DRAFT.equals(this.status);
    }

    public boolean canBeSubmitted() {
        return QuotationStatus.DRAFT.equals(this.status) && !items.isEmpty();
    }

    public boolean canBeDeleted() {
        return QuotationStatus.DRAFT.equals(this.status);
    }

    public void submit(String submissionNotes) {
        if (!canBeSubmitted()) {
            throw new IllegalStateException("Quotation cannot be submitted in current state");
        }
        this.status = QuotationStatus.SUBMITTED;
        this.submissionNotes = submissionNotes;
        this.submittedDate = LocalDateTime.now();
    }

    public void approve(User approver) {
        if (!isSubmitted()) {
            throw new IllegalStateException("Can only approve submitted quotations");
        }
        this.status = QuotationStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
    }
    public void reject(String rejectionReason) {
        if (!isSubmitted()) {
            throw new IllegalStateException("Quotation cannot be rejected in current state");
        }
        this.status = QuotationStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }

    public boolean canBeApproved() {
        return QuotationStatus.SUBMITTED.equals(this.status) ||
                QuotationStatus.UNDER_REVIEW.equals(this.status);
    }

    public boolean canBeRejected() {
        return QuotationStatus.SUBMITTED.equals(this.status) ||
                QuotationStatus.UNDER_REVIEW.equals(this.status);
    }

    public void reject(User approver, String rejectionReason) {
        if (!isSubmitted()) {
            throw new IllegalStateException("Can only reject submitted quotations");
        }
        this.status = QuotationStatus.REJECTED;
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    public BigDecimal getBudgetImpact() {
        if (project != null) {
            return project.getRemainingBudget().subtract(this.totalAmount);
        }
        return BigDecimal.ZERO;
    }

    public boolean exceedsBudget() {
        return getBudgetImpact().compareTo(BigDecimal.ZERO) < 0;
    }

    // Getters and Setters
    public Project getProject() {
        return project;
    }


    public void setProject(Project project) {
        this.project = project;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public List<QuotationItem> getItems() {
        return items;
    }

    public void setItems(List<QuotationItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Quotation{" +
                "id=" + getId() +
                ", projectId=" + (project != null ? project.getId() : null) +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", itemCount=" + items.size() +
                '}';
    }
}

// Quotation Status Enum
