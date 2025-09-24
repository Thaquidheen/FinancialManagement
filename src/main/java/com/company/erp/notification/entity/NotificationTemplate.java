package com.company.erp.notification.entity;

import com.company.erp.common.entity.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "notification_templates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_template_type_language",
                        columnNames = {"type", "language"})
        },
        indexes = {
                @Index(name = "idx_notification_template_type", columnList = "type"),
                @Index(name = "idx_notification_template_language", columnList = "language"),
                @Index(name = "idx_notification_template_enabled", columnList = "enabled")
        })
public class NotificationTemplate extends AuditableEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 5)
    @Column(name = "language", length = 5)
    private String language = "en";

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Size(max = 2000)
    @Column(name = "email_template", columnDefinition = "TEXT")
    private String emailTemplate;

    @Size(max = 500)
    @Column(name = "sms_template", length = 500)
    private String smsTemplate;

    @Size(max = 2000)
    @Column(name = "in_app_template", columnDefinition = "TEXT")
    private String inAppTemplate;

    @Size(max = 2000)
    @Column(name = "push_template", columnDefinition = "TEXT")
    private String pushTemplate;

    @Size(max = 255)
    @Column(name = "email_subject")
    private String emailSubject;

    @ElementCollection
    @CollectionTable(name = "notification_template_variables",
            joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "variable_name")
    @Column(name = "variable_description", columnDefinition = "TEXT")
    private Map<String, String> availableVariables = new HashMap<>();

    @Size(max = 1000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 100)
    @Column(name = "category")
    private String category;

    @Column(name = "version")
    private Integer version = 1;

    // Constructors
    public NotificationTemplate() {}

    public NotificationTemplate(NotificationType type, String title, String language) {
        this.type = type;
        this.title = title;
        this.language = language;
    }

    // Business methods
    public boolean hasEmailTemplate() {
        return emailTemplate != null && !emailTemplate.trim().isEmpty();
    }

    public boolean hasSmsTemplate() {
        return smsTemplate != null && !smsTemplate.trim().isEmpty();
    }

    public boolean hasInAppTemplate() {
        return inAppTemplate != null && !inAppTemplate.trim().isEmpty();
    }

    public boolean hasPushTemplate() {
        return pushTemplate != null && !pushTemplate.trim().isEmpty();
    }

    public boolean supportsChannel(NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return hasEmailTemplate();
            case SMS:
                return hasSmsTemplate();
            case IN_APP:
                return hasInAppTemplate();
            case PUSH:
                return hasPushTemplate();
            case MULTI:
                return hasEmailTemplate() || hasSmsTemplate() || hasInAppTemplate() || hasPushTemplate();
            default:
                return false;
        }
    }

    public void addVariable(String name, String description) {
        if (this.availableVariables == null) {
            this.availableVariables = new HashMap<>();
        }
        this.availableVariables.put(name, description);
    }

    public void incrementVersion() {
        this.version++;
    }

    // Getters and Setters
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getEmailTemplate() { return emailTemplate; }
    public void setEmailTemplate(String emailTemplate) { this.emailTemplate = emailTemplate; }

    public String getSmsTemplate() { return smsTemplate; }
    public void setSmsTemplate(String smsTemplate) { this.smsTemplate = smsTemplate; }

    public String getInAppTemplate() { return inAppTemplate; }
    public void setInAppTemplate(String inAppTemplate) { this.inAppTemplate = inAppTemplate; }

    public String getPushTemplate() { return pushTemplate; }
    public void setPushTemplate(String pushTemplate) { this.pushTemplate = pushTemplate; }

    public String getEmailSubject() { return emailSubject; }
    public void setEmailSubject(String emailSubject) { this.emailSubject = emailSubject; }

    public Map<String, String> getAvailableVariables() { return availableVariables; }
    public void setAvailableVariables(Map<String, String> availableVariables) { this.availableVariables = availableVariables; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public String toString() {
        return "NotificationTemplate{" +
                "id=" + getId() +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", enabled=" + enabled +
                ", version=" + version +
                '}';
    }
}