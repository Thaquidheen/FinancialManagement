package com.company.erp.notification.entity;

public enum NotificationChannel {
    EMAIL("Email", "Email notification", "envelope"),
    SMS("SMS", "SMS notification", "phone"),
    IN_APP("In-App", "In-application notification", "bell"),
    PUSH("Push", "Push notification", "mobile"),
    MULTI("Multi-Channel", "Multiple channels", "broadcast");

    private final String displayName;
    private final String description;
    private final String icon;

    NotificationChannel(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isRealTime() {
        return this == IN_APP || this == PUSH;
    }

    public boolean requiresExternalService() {
        return this == EMAIL || this == SMS || this == PUSH;
    }
}
