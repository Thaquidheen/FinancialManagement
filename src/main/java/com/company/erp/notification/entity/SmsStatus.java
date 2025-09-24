// SmsStatus.java
package com.company.erp.notification.entity;

public enum SmsStatus {
    PENDING("Pending", "SMS is queued for sending"),
    SENT("Sent", "SMS has been sent successfully"),
    DELIVERED("Delivered", "SMS has been delivered"),
    FAILED("Failed", "SMS sending failed"),
    EXPIRED("Expired", "SMS expired before delivery");

    private final String displayName;
    private final String description;

    SmsStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
