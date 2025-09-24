package com.company.erp.financial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class QuotationResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String createdBy;
    private String createdByUsername;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String submissionNotes;
    private LocalDateTime submittedDate;
    private LocalDateTime approvedDate;
    private String approvedBy;
    private String rejectionReason;
    private LocalDateTime createdDate;
    private Boolean active;
    private List<QuotationItemResponse> items;

    // Budget information
    private BigDecimal projectBudget;
    private BigDecimal remainingBudget;
    private BigDecimal budgetImpact;
    private Boolean exceedsBudget;

    // Constructors
    public QuotationResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<QuotationItemResponse> getItems() {
        return items;
    }

    public void setItems(List<QuotationItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getProjectBudget() {
        return projectBudget;
    }

    public void setProjectBudget(BigDecimal projectBudget) {
        this.projectBudget = projectBudget;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public BigDecimal getBudgetImpact() {
        return budgetImpact;
    }

    public void setBudgetImpact(BigDecimal budgetImpact) {
        this.budgetImpact = budgetImpact;
    }

    public Boolean getExceedsBudget() {
        return exceedsBudget;
    }

    public void setExceedsBudget(Boolean exceedsBudget) {
        this.exceedsBudget = exceedsBudget;
    }

    @Override
    public String toString() {
        return "QuotationResponse{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", itemCount=" + (items != null ? items.size() : 0) +
                '}';
    }

    // Nested QuotationItemResponse class
    public static class QuotationItemResponse {

        private Long id;
        private String description;
        private BigDecimal amount;
        private String currency;
        private String category;
        private String accountHead;
        private LocalDate itemDate;
        private String vendorName;
        private String vendorContact;
        private Integer itemOrder;

        // Constructors
        public QuotationItemResponse() {}

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getAccountHead() {
            return accountHead;
        }

        public void setAccountHead(String accountHead) {
            this.accountHead = accountHead;
        }

        public LocalDate getItemDate() {
            return itemDate;
        }

        public void setItemDate(LocalDate itemDate) {
            this.itemDate = itemDate;
        }

        public String getVendorName() {
            return vendorName;
        }

        public void setVendorName(String vendorName) {
            this.vendorName = vendorName;
        }

        public String getVendorContact() {
            return vendorContact;
        }

        public void setVendorContact(String vendorContact) {
            this.vendorContact = vendorContact;
        }

        public Integer getItemOrder() {
            return itemOrder;
        }

        public void setItemOrder(Integer itemOrder) {
            this.itemOrder = itemOrder;
        }

        @Override
        public String toString() {
            return "QuotationItemResponse{" +
                    "id=" + id +
                    ", description='" + (description != null && description.length() > 30 ?
                    description.substring(0, 30) + "..." : description) + '\'' +
                    ", amount=" + amount +
                    ", category='" + category + '\'' +
                    '}';
        }
    }
}