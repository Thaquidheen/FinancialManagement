package com.company.erp.document.entity;

public enum DocumentCategory {
    INVOICE("Invoice", "Financial invoices and bills"),
    RECEIPT("Receipt", "Payment receipts and confirmations"),
    CONTRACT("Contract", "Contracts and agreements"),
    PHOTO("Photo", "Project photos and images"),
    DOCUMENT("Document", "General documents and PDFs"),
    SPREADSHEET("Spreadsheet", "Excel and other spreadsheet files"),
    DELIVERY_NOTE("Delivery Note", "Delivery and shipping documents"),
    REPORT("Report", "Reports and analysis documents"),
    CERTIFICATE("Certificate", "Certificates and credentials"),
    OTHER("Other", "Other document types");

    private final String displayName;
    private final String description;

    DocumentCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return displayName; }
}