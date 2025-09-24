package com.company.erp.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentStatusResponse {

    private Long id;
    private Long quotationId;
    private String quotationDescription;
    private String projectName;
    private String payeeName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String statusDescription;
    private String bankName;
    private String accountNumber;
    private String bankReference;
    private LocalDateTime paymentDate;
    private String batchNumber;
    private String failureReason;
    private Boolean canRetry;
    private LocalDateTime createdDate;

    // Constructors
    public PaymentStatusResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuotationId() { return quotationId; }
    public void setQuotationId(Long quotationId) { this.quotationId = quotationId; }

    public String getQuotationDescription() { return quotationDescription; }
    public void setQuotationDescription(String quotationDescription) { this.quotationDescription = quotationDescription; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Boolean getCanRetry() { return canRetry; }
    public void setCanRetry(Boolean canRetry) { this.canRetry = canRetry; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}