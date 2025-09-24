
package com.company.erp.common.exception;



// File Upload Exception
public class FileUploadException extends BusinessException {
    public FileUploadException(String message) {
        super("FILE_UPLOAD_ERROR", message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}