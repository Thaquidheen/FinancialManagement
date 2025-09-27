package com.company.erp.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public class SendNotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;
    
    private Map<String, Object> templateData;
    
    // Constructors
    public SendNotificationRequest() {}
    
    public SendNotificationRequest(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Map<String, Object> getTemplateData() { return templateData; }
    public void setTemplateData(Map<String, Object> templateData) { this.templateData = templateData; }
    
    @Override
    public String toString() {
        return "SendNotificationRequest{" +
                "userId=" + userId +
                ", message='" + message + '\'' +
                '}';
    }
}