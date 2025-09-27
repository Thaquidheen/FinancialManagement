package com.company.erp.payment.dto.response;

import com.company.erp.payment.entity.Payment;
import com.company.erp.payment.entity.PaymentBatch;
import com.company.erp.payment.entity.PaymentBatchStatus;
import com.company.erp.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentBatchWithPaymentsResponse {
    private Long id;
    private String batchNumber;
    private String bankName;
    private BigDecimal totalAmount;
    private String currency;
    private Integer paymentCount;
    private PaymentBatchStatus status;
    private String fileName;
    private String filePath;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime downloadedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentToBankDate;
    
    private String bankReference;
    private String processingNotes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
    
    private Long createdBy;
    private Long lastModifiedBy;
    private Boolean active;
    
    // Include payments for batch actions
    private List<Payment> payments;
    
    // Creator information
    private User creator;

    // Constructors
    public PaymentBatchWithPaymentsResponse() {}

    public PaymentBatchWithPaymentsResponse(PaymentBatch batch) {
        this.id = batch.getId();
        this.batchNumber = batch.getBatchNumber();
        this.bankName = batch.getBankName();
        this.totalAmount = batch.getTotalAmount();
        this.currency = batch.getCurrency();
        this.paymentCount = batch.getPaymentCount();
        this.status = batch.getStatus();
        this.fileName = batch.getFileName();
        this.filePath = batch.getFilePath();
        this.generatedDate = batch.getGeneratedDate();
        this.downloadedDate = batch.getDownloadedDate();
        this.sentToBankDate = batch.getSentToBankDate();
        this.bankReference = batch.getBankReference();
        this.processingNotes = batch.getProcessingNotes();
        this.createdDate = batch.getCreatedDate();
        this.lastModifiedDate = batch.getLastModifiedDate();
        this.createdBy = batch.getCreatedBy();
        this.lastModifiedBy = batch.getLastModifiedBy();
        this.active = batch.getActive();
        this.payments = batch.getPayments();
        this.creator = batch.getCreator();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getPaymentCount() { return paymentCount; }
    public void setPaymentCount(Integer paymentCount) { this.paymentCount = paymentCount; }

    public PaymentBatchStatus getStatus() { return status; }
    public void setStatus(PaymentBatchStatus status) { this.status = status; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }

    public LocalDateTime getDownloadedDate() { return downloadedDate; }
    public void setDownloadedDate(LocalDateTime downloadedDate) { this.downloadedDate = downloadedDate; }

    public LocalDateTime getSentToBankDate() { return sentToBankDate; }
    public void setSentToBankDate(LocalDateTime sentToBankDate) { this.sentToBankDate = sentToBankDate; }

    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }

    public String getProcessingNotes() { return processingNotes; }
    public void setProcessingNotes(String processingNotes) { this.processingNotes = processingNotes; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(Long lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
}
