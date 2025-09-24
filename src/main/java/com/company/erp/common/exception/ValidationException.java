
package com.company.erp.common.exception;



public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String fieldName, String message) {
        super("VALIDATION_ERROR", String.format("Validation failed for field '%s': %s", fieldName, message));
    }
}