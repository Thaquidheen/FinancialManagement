



package com.company.erp.financial.entity;

import com.company.erp.common.entity.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "quotation_items")
public class QuotationItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @NotBlank
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2)
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Size(max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "SAR";

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @Size(max = 100)
    @Column(name = "account_head", length = 100)
    private String accountHead;

    @Column(name = "item_date")
    private LocalDate itemDate;

    @Size(max = 200)
    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Size(max = 100)
    @Column(name = "vendor_contact", length = 100)
    private String vendorContact;

    @Column(name = "item_order")
    private Integer itemOrder = 0;

    // Constructors
    public QuotationItem() {}

    public QuotationItem(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public QuotationItem(String description, BigDecimal amount, String category, String accountHead) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.accountHead = accountHead;
    }

    // Business methods
    public void updateAmount(BigDecimal newAmount) {
        this.amount = newAmount;
        if (this.quotation != null) {
            this.quotation.updateTotalAmount();
        }
    }

    public boolean isValid() {
        return description != null && !description.trim().isEmpty() &&
                amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Getters and Setters
    public Quotation getQuotation() {
        return quotation;
    }

    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
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
        return "QuotationItem{" +
                "id=" + getId() +
                ", description='" + (description != null && description.length() > 30 ?
                description.substring(0, 30) + "..." : description) + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", itemOrder=" + itemOrder +
                '}';
    }
}