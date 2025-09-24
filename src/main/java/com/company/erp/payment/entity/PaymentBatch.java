package com.company.erp.payment.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_batches")
public class PaymentBatch extends AuditableEntity {

    @Column(name = "batch_number", unique = true, nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @NotNull
    @Positive
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "SAR";

    @Column(name = "payment_count")
    private Integer paymentCount = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentBatchStatus status = PaymentBatchStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User creator;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "generated_date")
    private LocalDateTime generatedDate;

    @Column(name = "downloaded_date")
    private LocalDateTime downloadedDate;

    @Column(name = "sent_to_bank_date")
    private LocalDateTime sentToBankDate;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    // Constructors
    public PaymentBatch() {}

    public PaymentBatch(String bankName, User creator) {
        this.bankName = bankName;
        this.creator = creator;
        this.batchNumber = generateBatchNumber();
        this.status = PaymentBatchStatus.DRAFT;
    }

    // Business methods
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBatch(this);
        recalculateTotals();
    }

    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setBatch(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.totalAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.paymentCount = payments.size();
    }

    public void markAsGenerated(String fileName, String filePath) {
        this.status = PaymentBatchStatus.FILE_GENERATED;
        this.fileName = fileName;
        this.filePath = filePath;
        this.generatedDate = LocalDateTime.now();
    }

    public void markAsDownloaded() {
        this.downloadedDate = LocalDateTime.now();
    }

    public void markAsSentToBank(String bankReference) {
        this.status = PaymentBatchStatus.SENT_TO_BANK;
        this.bankReference = bankReference;
        this.sentToBankDate = LocalDateTime.now();

        // Update all payments in batch
        for (Payment payment : payments) {
            payment.markAsSentToBank();
        }
    }

    public void markAsCompleted() {
        this.status = PaymentBatchStatus.COMPLETED;

        // Mark all payments as paid
        for (Payment payment : payments) {
            payment.markAsPaid(this.bankReference);
        }
    }

    public boolean canBeGenerated() {
        return PaymentBatchStatus.DRAFT.equals(this.status) && !payments.isEmpty();
    }

    public boolean canBeSentToBank() {
        return PaymentBatchStatus.FILE_GENERATED.equals(this.status);
    }

    public boolean isCompleted() {
        return PaymentBatchStatus.COMPLETED.equals(this.status);
    }

    private String generateBatchNumber() {
        return "BATCH_" + System.currentTimeMillis();
    }

    // Getters and Setters
    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public Integer getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(Integer paymentCount) {
        this.paymentCount = paymentCount;
    }

    public PaymentBatchStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentBatchStatus status) {
        this.status = status;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public LocalDateTime getDownloadedDate() {
        return downloadedDate;
    }

    public void setDownloadedDate(LocalDateTime downloadedDate) {
        this.downloadedDate = downloadedDate;
    }

    public LocalDateTime getSentToBankDate() {
        return sentToBankDate;
    }

    public void setSentToBankDate(LocalDateTime sentToBankDate) {
        this.sentToBankDate = sentToBankDate;
    }

    public String getBankReference() {
        return bankReference;
    }

    public void setBankReference(String bankReference) {
        this.bankReference = bankReference;
    }

    public String getProcessingNotes() {
        return processingNotes;
    }

    public void setProcessingNotes(String processingNotes) {
        this.processingNotes = processingNotes;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    @Override
    public String toString() {
        return "PaymentBatch{" +
                "id=" + getId() +
                ", batchNumber='" + batchNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentCount=" + paymentCount +
                ", status=" + status +
                '}';
    }
}
