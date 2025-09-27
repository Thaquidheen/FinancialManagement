package com.company.erp.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "quotation_notifications", nullable = false)
    private Boolean quotationNotifications = true;

    @Column(name = "payment_notifications", nullable = false)
    private Boolean paymentNotifications = true;

    @Column(name = "system_notifications", nullable = false)
    private Boolean systemNotifications = true;

    @Column(name = "marketing_notifications", nullable = false)
    private Boolean marketingNotifications = false;

    // Constructors
    public NotificationPreferences() {}

    public NotificationPreferences(Long userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public Boolean getSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(Boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public Boolean getInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(Boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }

    public Boolean getPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(Boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public Boolean getQuotationNotifications() {
        return quotationNotifications;
    }

    public void setQuotationNotifications(Boolean quotationNotifications) {
        this.quotationNotifications = quotationNotifications;
    }

    public Boolean getPaymentNotifications() {
        return paymentNotifications;
    }

    public void setPaymentNotifications(Boolean paymentNotifications) {
        this.paymentNotifications = paymentNotifications;
    }

    public Boolean getSystemNotifications() {
        return systemNotifications;
    }

    public void setSystemNotifications(Boolean systemNotifications) {
        this.systemNotifications = systemNotifications;
    }

    public Boolean getMarketingNotifications() {
        return marketingNotifications;
    }

    public void setMarketingNotifications(Boolean marketingNotifications) {
        this.marketingNotifications = marketingNotifications;
    }

    // Convenience methods
    public boolean isEmailEnabled() {
        return emailEnabled != null && emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled != null && smsEnabled;
    }

    public boolean isInAppEnabled() {
        return inAppEnabled != null && inAppEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled != null && pushEnabled;
    }

    public boolean isQuotationNotifications() {
        return quotationNotifications != null && quotationNotifications;
    }

    public boolean isPaymentNotifications() {
        return paymentNotifications != null && paymentNotifications;
    }

    public boolean isSystemNotifications() {
        return systemNotifications != null && systemNotifications;
    }

    public boolean isMarketingNotifications() {
        return marketingNotifications != null && marketingNotifications;
    }
}
