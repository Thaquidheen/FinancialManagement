
// NotificationPreference.java
package com.company.erp.notification.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.Set;

    @Entity
    @Table(name = "notification_preferences")
    public class NotificationPreference extends AuditableEntity {

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        private User user;

        @Column(name = "email_enabled")
        private Boolean emailEnabled = true;

        @Column(name = "sms_enabled")
        private Boolean smsEnabled = true;

        @Column(name = "in_app_enabled")
        private Boolean inAppEnabled = true;

        @Column(name = "push_enabled")
        private Boolean pushEnabled = false;

        @Column(name = "daily_summary_enabled")
        private Boolean dailySummaryEnabled = true;

        @Column(name = "weekly_summary_enabled")
        private Boolean weeklySummaryEnabled = true;

        @Column(name = "do_not_disturb_enabled")
        private Boolean doNotDisturbEnabled = false;

        @Column(name = "do_not_disturb_start")
        private LocalTime doNotDisturbStart;

        @Column(name = "do_not_disturb_end")
        private LocalTime doNotDisturbEnd;

        @Column(name = "language", length = 5)
        private String language = "en";

        @Column(name = "timezone")
        private String timezone = "Asia/Riyadh";

        @Enumerated(EnumType.STRING)
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "user_notification_types",
                joinColumns = @JoinColumn(name = "preference_id"))
        @Column(name = "notification_type")
        private Set<NotificationType> enabledTypes;

        @Enumerated(EnumType.STRING)
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "user_notification_channels",
                joinColumns = @JoinColumn(name = "preference_id"))
        @Column(name = "notification_channel")
        private Set<NotificationChannel> enabledChannels;

        // Constructors
        public NotificationPreference() {}

        public NotificationPreference(User user) {
            this.user = user;
        }

        // Getters and Setters
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }

        public Boolean getEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }

        public Boolean getSmsEnabled() { return smsEnabled; }
        public void setSmsEnabled(Boolean smsEnabled) { this.smsEnabled = smsEnabled; }

        public Boolean getInAppEnabled() { return inAppEnabled; }
        public void setInAppEnabled(Boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }

        public Boolean getPushEnabled() { return pushEnabled; }
        public void setPushEnabled(Boolean pushEnabled) { this.pushEnabled = pushEnabled; }

        public Boolean getDailySummaryEnabled() { return dailySummaryEnabled; }
        public void setDailySummaryEnabled(Boolean dailySummaryEnabled) { this.dailySummaryEnabled = dailySummaryEnabled; }

        public Boolean getWeeklySummaryEnabled() { return weeklySummaryEnabled; }
        public void setWeeklySummaryEnabled(Boolean weeklySummaryEnabled) { this.weeklySummaryEnabled = weeklySummaryEnabled; }

        public Boolean getDoNotDisturbEnabled() { return doNotDisturbEnabled; }
        public void setDoNotDisturbEnabled(Boolean doNotDisturbEnabled) { this.doNotDisturbEnabled = doNotDisturbEnabled; }

        public LocalTime getDoNotDisturbStart() { return doNotDisturbStart; }
        public void setDoNotDisturbStart(LocalTime doNotDisturbStart) { this.doNotDisturbStart = doNotDisturbStart; }

        public LocalTime getDoNotDisturbEnd() { return doNotDisturbEnd; }
        public void setDoNotDisturbEnd(LocalTime doNotDisturbEnd) { this.doNotDisturbEnd = doNotDisturbEnd; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public Set<NotificationType> getEnabledTypes() { return enabledTypes; }
        public void setEnabledTypes(Set<NotificationType> enabledTypes) { this.enabledTypes = enabledTypes; }

        public Set<NotificationChannel> getEnabledChannels() { return enabledChannels; }
        public void setEnabledChannels(Set<NotificationChannel> enabledChannels) { this.enabledChannels = enabledChannels; }
    }