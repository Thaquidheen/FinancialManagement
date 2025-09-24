package com.company.erp.notification.dto.response;


import java.time.LocalDateTime;

public class EmailResponse {

    private boolean success;
    private String message;
    private String recipient;
    private LocalDateTime sentAt;
    private String errorMessage;
    private String emailId; // For tracking purposes

    // Constructors
    public EmailResponse() {}

    public EmailResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    // Static factory methods
    public static EmailResponse success(String recipient) {
        EmailResponse response = new EmailResponse(true, "Email sent successfully");
        response.setRecipient(recipient);
        return response;
    }

    public static EmailResponse failure(String errorMessage) {
        EmailResponse response = new EmailResponse(false, "Failed to send email");
        response.setErrorMessage(errorMessage);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    @Override
    public String toString() {
        return "EmailResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", recipient='" + recipient + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
