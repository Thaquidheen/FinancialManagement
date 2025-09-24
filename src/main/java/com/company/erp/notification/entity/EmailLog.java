package com.company.erp.notification.entity;

import com.company.erp.common.entity.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs", indexes = {
        @Index(name = "idx_email_logs_recipient", columnList = "recipient"),
        @Index(name = "idx_email_logs_success", columnList = "success"),
        @Index(name = "idx_email_logs_sent_at", columnList = "sent_at")
})
public class EmailLog extends AuditableEntity {

    @NotNull
    @Size(max = 255)
    @Column(name = "recipient", nullable = false)
    private String recipient;

    @NotNull
    @Size(max = 500)
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Size(max = 1000)
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Size(max = 100)
    @Column(name = "template_name", length = 100)
    private String templateName;

    // Constructors
    public EmailLog() {}

    public EmailLog(String recipient, String subject, Boolean success) {
        this.recipient = recipient;
        this.subject = subject;
        this.success = success;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    @Override
    public String toString() {
        return "EmailLog{" +
                "id=" + getId() +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", success=" + success +
                ", sentAt=" + sentAt +
                '}';
    }
}
