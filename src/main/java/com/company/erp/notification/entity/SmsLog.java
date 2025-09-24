package com.company.erp.notification.entity;

import com.company.erp.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_logs")
public class SmsLog extends BaseEntity {

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private SmsProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SmsStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cost", precision = 10, scale = 4)
    private java.math.BigDecimal cost;

    // Constructors
    public SmsLog() {}

    // Getters and Setters
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public SmsProvider getProvider() { return provider; }
    public void setProvider(SmsProvider provider) { this.provider = provider; }

    public SmsStatus getStatus() { return status; }
    public void setStatus(SmsStatus status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public java.math.BigDecimal getCost() { return cost; }
    public void setCost(java.math.BigDecimal cost) { this.cost = cost; }
}
