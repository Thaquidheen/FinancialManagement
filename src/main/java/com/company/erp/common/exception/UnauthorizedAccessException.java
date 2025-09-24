package com.company.erp.common.exception;

public class UnauthorizedAccessException extends RuntimeException {
    private final String errorCode;

    public UnauthorizedAccessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnauthorizedAccessException(String message) {
        super(message);
        this.errorCode = "UNAUTHORIZED_ACCESS";
    }

    public String getErrorCode() {
        return errorCode;
    }
}

