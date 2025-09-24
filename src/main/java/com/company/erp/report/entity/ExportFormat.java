package com.company.erp.report.enums;

public enum ExportFormat {
    EXCEL("Excel", "Microsoft Excel format (.xlsx)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("PDF", "Portable Document Format (.pdf)", "application/pdf"),
    CSV("CSV", "Comma Separated Values (.csv)", "text/csv"),
    JSON("JSON", "JavaScript Object Notation (.json)", "application/json"),
    XML("XML", "Extensible Markup Language (.xml)", "application/xml"),
    HTML("HTML", "HyperText Markup Language (.html)", "text/html");

    private final String displayName;
    private final String description;
    private final String mimeType;

    ExportFormat(String displayName, String description, String mimeType) {
        this.displayName = displayName;
        this.description = description;
        this.mimeType = mimeType;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getMimeType() { return mimeType; }

    public String getFileExtension() {
        return switch (this) {
            case EXCEL -> ".xlsx";
            case PDF -> ".pdf";
            case CSV -> ".csv";
            case JSON -> ".json";
            case XML -> ".xml";
            case HTML -> ".html";
        };
    }
}