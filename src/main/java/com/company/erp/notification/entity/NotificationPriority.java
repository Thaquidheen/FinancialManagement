package com.company.erp.notification.entity;

public enum NotificationPriority {
    LOW("Low", "Low priority notification", "#28a745"),
    NORMAL("Normal", "Normal priority notification", "#007bff"),
    MEDIUM("Medium", "Medium priority notification", "#fd7e14"),
    HIGH("High", "High priority notification", "#dc3545"),
    CRITICAL("Critical", "Critical priority notification", "#dc3545");

    private final String displayName;
    private final String description;
    private final String colorCode;

    NotificationPriority(String displayName, String description, String colorCode) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getPriorityLevel() {
        return ordinal();
    }

    public boolean isHigherThan(NotificationPriority other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean isAtLeast(NotificationPriority threshold) {
        return this.ordinal() >= threshold.ordinal();
    }
}