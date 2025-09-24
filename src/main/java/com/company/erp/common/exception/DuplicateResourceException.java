
package com.company.erp.common.exception;



// Duplicate Resource Exception
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super("DUPLICATE_RESOURCE",
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}