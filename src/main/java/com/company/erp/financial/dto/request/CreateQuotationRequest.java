package com.company.erp.financial.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateQuotationRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "SAR";

    @NotEmpty(message = "At least one quotation item is required")
    @Valid
    private List<QuotationItemRequest> items;

    // Constructors
    public CreateQuotationRequest() {}

    public CreateQuotationRequest(Long projectId, String description, List<QuotationItemRequest> items) {
        this.projectId = projectId;
        this.description = description;
        this.items = items;
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<QuotationItemRequest> getItems() {
        return items;
    }

    public void setItems(List<QuotationItemRequest> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CreateQuotationRequest{" +
                "projectId=" + projectId +
                ", description='" + description + '\'' +
                ", currency='" + currency + '\'' +
                ", itemCount=" + (items != null ? items.size() : 0) +
                '}';
    }

    // Nested QuotationItemRequest class
    public static class QuotationItemRequest {

        @NotBlank(message = "Item description is required")
        @Size(max = 500, message = "Item description must not exceed 500 characters")
        private String description;

        @NotNull(message = "Item amount is required")
        @DecimalMin(value = "0.01", message = "Item amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
        private BigDecimal amount;

        @Size(max = 3, message = "Currency code must not exceed 3 characters")
        private String currency = "SAR";

        @Size(max = 50, message = "Category must not exceed 50 characters")
        private String category;

        @Size(max = 100, message = "Account head must not exceed 100 characters")
        private String accountHead;

        private LocalDate itemDate;

        @Size(max = 200, message = "Vendor name must not exceed 200 characters")
        private String vendorName;

        @Size(max = 100, message = "Vendor contact must not exceed 100 characters")
        private String vendorContact;

        // Constructors
        public QuotationItemRequest() {}

        public QuotationItemRequest(String description, BigDecimal amount) {
            this.description = description;
            this.amount = amount;
        }

        public QuotationItemRequest(String description, BigDecimal amount, String category, String accountHead) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.accountHead = accountHead;
        }

        // Getters and Setters
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

        @Override
        public String toString() {
            return "QuotationItemRequest{" +
                    "description='" + (description != null && description.length() > 30 ?
                    description.substring(0, 30) + "..." : description) + '\'' +
                    ", amount=" + amount +
                    ", category='" + category + '\'' +
                    '}';
        }
    }
}