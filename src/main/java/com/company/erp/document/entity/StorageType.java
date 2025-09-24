package com.company.erp.document.entity;

public enum StorageType {
    LOCAL("Local File System", "Files stored on local server"),
    DATABASE("Database Storage", "Files stored in PostgreSQL database"),
    S3("Amazon S3", "Files stored in Amazon S3 bucket"),
    AZURE("Azure Blob", "Files stored in Azure Blob Storage"),
    HYBRID("Hybrid Storage", "Combination of multiple storage types");

    private final String displayName;
    private final String description;

    StorageType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
