package com.company.erp.notification.entity;

public enum SmsProvider {
    TAQNYAT("Taqnyat", "https://api.taqnyat.sa", "Saudi SMS provider"),
    MSEGAT("Msegat", "https://www.msegat.com", "Saudi SMS provider"),
    UNIFONIC("Unifonic", "https://api.unifonic.com", "MENA SMS provider"),
    STC("STC", "https://api.stc.com.sa", "STC SMS service");

    private final String displayName;
    private final String apiUrl;
    private final String description;

    SmsProvider(String displayName, String apiUrl, String description) {
        this.displayName = displayName;
        this.apiUrl = apiUrl;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getApiUrl() { return apiUrl; }
    public String getDescription() { return description; }
}
