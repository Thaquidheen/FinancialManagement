package com.company.erp.payment.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.financial.entity.Quotation;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee; // Employee receiving payment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private PaymentBatch batch;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "SAR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "beneficiary_address", columnDefinition = "TEXT")
    private String beneficiaryAddress;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    // Constructors
    public Payment() {}

    public Payment(Quotation quotation, User payee) {
        this.quotation = quotation;
        this.payee = payee;
        this.amount = quotation.getTotalAmount();
        this.currency = quotation.getCurrency();
        this.status = PaymentStatus.PENDING;

        // Copy bank details from user profile
        if (payee.getBankDetails() != null) {
            this.bankName = payee.getBankDetails().getBankName();
            this.accountNumber = payee.getBankDetails().getAccountNumber();
            this.iban = payee.getBankDetails().getIban();
            this.beneficiaryAddress = payee.getBankDetails().getBeneficiaryAddress();
        }
    }

    // Business methods
    public void markAsPaid(String bankReference) {
        this.status = PaymentStatus.PAID;
        this.bankReference = bankReference;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.retryCount++;
    }

    public void processForPayment() {
        this.status = PaymentStatus.PROCESSING;
    }

    public void markAsSentToBank() {
        this.status = PaymentStatus.SENT_TO_BANK;
    }

    public boolean canBeRetried() {
        return PaymentStatus.FAILED.equals(this.status) && this.retryCount < 3;
    }

    public boolean isPaid() {
        return PaymentStatus.PAID.equals(this.status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }

    // Validation methods
    public boolean hasValidBankDetails() {
        return bankName != null && !bankName.trim().isEmpty() &&
                accountNumber != null && !accountNumber.trim().isEmpty() &&
                iban != null && !iban.trim().isEmpty();
    }

    // Getters and Setters
    public Quotation getQuotation() {
        return quotation;
    }

    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }

    public User getPayee() {
        return payee;
    }

    public void setPayee(User payee) {
        this.payee = payee;
    }

    public PaymentBatch getBatch() {
        return batch;
    }

    public void setBatch(PaymentBatch batch) {
        this.batch = batch;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBeneficiaryAddress() {
        return beneficiaryAddress;
    }

    public void setBeneficiaryAddress(String beneficiaryAddress) {
        this.beneficiaryAddress = beneficiaryAddress;
    }

    public String getBankReference() {
        return bankReference;
    }

    public void setBankReference(String bankReference) {
        this.bankReference = bankReference;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + getId() +
                ", quotationId=" + (quotation != null ? quotation.getId() : null) +
                ", payeeId=" + (payee != null ? payee.getId() : null) +
                ", amount=" + amount +
                ", status=" + status +
                ", bankName='" + bankName + '\'' +
                '}';
    }
}