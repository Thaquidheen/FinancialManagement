package com.company.erp.common.constants;

public final class ApplicationConstants {

    private ApplicationConstants() {
        // Utility class
    }

    // Security Constants
    public static final class Security {
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String ROLE_PREFIX = "ROLE_";
        public static final int TOKEN_BEGIN_INDEX = 7;
    }

    // User Roles
    public static final class Roles {
        public static final String SUPER_ADMIN = "SUPER_ADMIN";
        public static final String PROJECT_MANAGER = "PROJECT_MANAGER";
        public static final String ACCOUNT_MANAGER = "ACCOUNT_MANAGER";
        public static final String EMPLOYEE = "EMPLOYEE";
    }

    // Quotation Status
    public static final class QuotationStatus {
        public static final String DRAFT = "DRAFT";
        public static final String SUBMITTED = "SUBMITTED";
        public static final String UNDER_REVIEW = "UNDER_REVIEW";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
        public static final String PAYMENT_FILE_GENERATED = "PAYMENT_FILE_GENERATED";
        public static final String SENT_TO_BANK = "SENT_TO_BANK";
        public static final String PAID = "PAID";
    }

    // Project Status
    public static final class ProjectStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String COMPLETED = "COMPLETED";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String CANCELLED = "CANCELLED";
    }

    // Approval Status
    public static final class ApprovalStatus {
        public static final String PENDING = "PENDING";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
        public static final String CHANGES_REQUESTED = "CHANGES_REQUESTED";
    }

    // Document Types
    public static final class DocumentType {
        public static final String INVOICE = "INVOICE";
        public static final String BILL = "BILL";
        public static final String RECEIPT = "RECEIPT";
        public static final String CONTRACT = "CONTRACT";
        public static final String PHOTO = "PHOTO";
        public static final String DELIVERY_NOTE = "DELIVERY_NOTE";
        public static final String OTHER = "OTHER";
    }

    // Account Categories
    public static final class AccountHead {
        public static final String ROOM_RENT = "Room rent";
        public static final String DIRECT_MATERIAL = "Consumption - Direct Material";
        public static final String CONTRACT_LABOUR = "Contract Labour";
        public static final String EQUIPMENT_RENTALS = "Equipment Rentals";
        public static final String TRANSPORTATION = "Transportation";
        public static final String OTHER = "Other";
    }

    // File Constants
    public static final class File {
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final String[] ALLOWED_FILE_TYPES = {
                "application/pdf",
                "image/jpeg",
                "image/png",
                "image/gif",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
    }

    // Saudi Bank Constants
    public static final class SaudiBanks {
        public static final String AL_RAJHI = "Al Rajhi Bank";
        public static final String NCB = "National Commercial Bank";
        public static final String SABB = "SABB Bank";
        public static final String ALINMA = "Alinma Bank";
        public static final String ARAB_NATIONAL = "Arab National Bank";
    }

    // Notification Types
    public static final class NotificationType {
        public static final String EMAIL = "EMAIL";
        public static final String SMS = "SMS";
        public static final String IN_APP = "IN_APP";
    }

    // Currency
    public static final String DEFAULT_CURRENCY = "SAR";

    // Pagination
    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_FIELD = "createdDate";
        public static final String DEFAULT_SORT_DIRECTION = "desc";
    }
}