// Enhanced NotificationService.java
package com.company.erp.notification.service;

import com.company.erp.common.service.AuditService;
import com.company.erp.notification.entity.Notification;
import com.company.erp.notification.entity.NotificationPreference;
import com.company.erp.notification.entity.NotificationTemplate;
import com.company.erp.notification.entity.NotificationType;
import com.company.erp.notification.entity.NotificationChannel;
import com.company.erp.notification.entity.NotificationPriority;
import com.company.erp.notification.repository.NotificationRepository;
import com.company.erp.notification.repository.NotificationPreferenceRepository;
import com.company.erp.notification.repository.NotificationTemplateRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UserService userService;
    private final AuditService auditService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPreferenceRepository preferenceRepository,
                               NotificationTemplateRepository templateRepository,
                               EmailService emailService,
                               SmsService smsService,
                               UserService userService,
                               AuditService auditService) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.templateRepository = templateRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Async
    public CompletableFuture<Void> sendNotification(Long userId, NotificationType type,
                                                    Map<String, Object> templateData,
                                                    NotificationPriority priority) {
        try {
            // Get User entity
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            NotificationPreference preference = getOrCreateUserPreference(user);

            // Check if user wants to receive this type of notification
            if (!shouldSendNotification(preference, type, priority)) {
                logger.debug("Notification blocked by user preference: {} - {}", userId, type);
                return CompletableFuture.completedFuture(null);
            }

            // Get template for notification type
            NotificationTemplate template = templateRepository.findByType(type)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found for type: " + type));

            // Create notification record
            Notification notification = createNotification(user, type, template, templateData, priority);
            notification = notificationRepository.save(notification);

            // Send through preferred channels
            sendThroughChannels(user, preference, template, templateData, priority);

            // Mark as sent - Fix: use setSentAt instead of setSentDate
            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            // Audit log
            auditService.logAction(userId, "NOTIFICATION_SENT", "NOTIFICATION",
                    notification.getId(), "Notification sent: " + type, null, null);

        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Notification sending failed", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> createNotification(Long userId, String title, String message, String type) {
        return createNotification(userId, title, message, type, NotificationPriority.NORMAL);
    }

    public CompletableFuture<Void> createNotification(Long userId, String title, String message,
                                                     String type, NotificationPriority priority) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(NotificationType.valueOf(type));
            notification.setPriority(priority);
            notification.setChannel(NotificationChannel.IN_APP);
            notification.setRead(false);
            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());

            notificationRepository.save(notification);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to create notification for user {}: {}", userId, e.getMessage(), e);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void sendThroughChannels(User user, NotificationPreference preference,
                                     NotificationTemplate template, Map<String, Object> templateData,
                                     NotificationPriority priority) {

        Set<NotificationChannel> channels = getChannelsForPriority(preference, priority);

        for (NotificationChannel channel : channels) {
            switch (channel) {
                case EMAIL:
                    if (user.getEmail() != null && preference.getEmailEnabled()) {
                        sendEmailNotification(user, template, templateData);
                    }
                    break;
                case SMS:
                    if (user.getPhoneNumber() != null && preference.getSmsEnabled()) {
                        sendSmsNotification(user, template, templateData);
                    }
                    break;
                case IN_APP:
                    createInAppNotification(user, template, templateData);
                    break;
                case PUSH:
                    logger.debug("Push notifications not yet implemented");
                    break;
            }
        }
    }

    private Set<NotificationChannel> getChannelsForPriority(NotificationPreference preference,
                                                            NotificationPriority priority) {
        // Fix: Use correct enum constant names
        switch (priority) {
            case CRITICAL:
                return Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS,
                        NotificationChannel.IN_APP, NotificationChannel.PUSH);
            case HIGH:
                return Set.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP);
            case MEDIUM:
                return Set.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP);
            case LOW:
            case NORMAL:
            default:
                return Set.of(NotificationChannel.IN_APP);
        }
    }

    private void sendEmailNotification(User user, NotificationTemplate template,
                                       Map<String, Object> templateData) {
        try {
            String subject = processTemplate(template.getEmailSubject(), templateData);
            // Fix: use getEmailTemplate instead of getEmailBody
            String body = processTemplate(template.getEmailTemplate(), templateData);

            // Use public EmailService API
            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send email notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendSmsNotification(User user, NotificationTemplate template,
                                     Map<String, Object> templateData) {
        try {
            String message = processTemplate(template.getSmsTemplate(), templateData);

            if (message.length() > 160) {
                message = message.substring(0, 157) + "...";
            }

            smsService.sendSms(user.getPhoneNumber(), message);
        } catch (Exception e) {
            logger.error("Failed to send SMS notification to {}: {}", user.getPhoneNumber(), e.getMessage());
        }
    }

    private void createInAppNotification(User user, NotificationTemplate template,
                                         Map<String, Object> templateData) {
        try {
            String title = processTemplate(template.getTitle(), templateData);
            String message = processTemplate(template.getInAppTemplate(), templateData);

            Notification inAppNotification = new Notification();
            inAppNotification.setUser(user);
            inAppNotification.setType(template.getType());
            inAppNotification.setTitle(title);
            inAppNotification.setMessage(message);
            inAppNotification.setChannel(NotificationChannel.IN_APP);
            inAppNotification.setRead(false);
            inAppNotification.setSent(true);
            // Fix: use setSentAt instead of setSentDate
            inAppNotification.setSentAt(LocalDateTime.now());

            notificationRepository.save(inAppNotification);
        } catch (Exception e) {
            logger.error("Failed to create in-app notification for user {}: {}", user.getId(), e.getMessage());
        }
    }

    private String processTemplate(String template, Map<String, Object> data) {
        if (template == null) return "";

        String processed = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processed = processed.replace(placeholder, value);
        }
        return processed;
    }

    private Notification createNotification(User user, NotificationType type,
                                           NotificationTemplate template,
                                           Map<String, Object> templateData,
                                           NotificationPriority priority) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(processTemplate(template.getTitle(), templateData));
        notification.setMessage(processTemplate(template.getInAppTemplate(), templateData));
        notification.setPriority(priority);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setTemplateData(templateData);
        notification.setRead(false);
        notification.setSent(false);
        return notification;
    }

    private NotificationPreference getOrCreateUserPreference(User user) {
        return preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreference(user));
    }

    private NotificationPreference createDefaultPreference(User user) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUser(user);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(false);
        preference.setInAppEnabled(true);
        preference.setPushEnabled(false);
        return preferenceRepository.save(preference);
    }

    private boolean shouldSendNotification(NotificationPreference preference,
                                          NotificationType type,
                                          NotificationPriority priority) {
        // Always send critical notifications
        if (priority == NotificationPriority.CRITICAL) {
            return true;
        }

        // Check user preferences based on notification type (mapped to available prefs)
        switch (type) {
            case SYSTEM_MAINTENANCE:
            case SYSTEM_UPDATE:
            case SYSTEM_ERROR:
                return true; // Always send critical system notifications
            case BUDGET_WARNING:
            case BUDGET_CRITICAL:
            case BUDGET_EXCEEDED:
                return Boolean.TRUE.equals(preference.getInAppEnabled());
            case PAYMENT_CREATED:
            case PAYMENT_COMPLETED:
            case PAYMENT_FAILED:
                return Boolean.TRUE.equals(preference.getInAppEnabled());
            case PROJECT_CREATED:
            case PROJECT_UPDATED:
            case PROJECT_ASSIGNED:
                return Boolean.TRUE.equals(preference.getInAppEnabled());
            default:
                return Boolean.TRUE.equals(preference.getInAppEnabled());
        }
    }

    @Scheduled(cron = "0 0 9-17 * * MON-FRI") // Every hour during business hours
    public void processScheduledNotifications() {
        List<Notification> scheduledNotifications = notificationRepository
                .findByScheduledTimeBeforeAndSentFalseAndActiveTrue(LocalDateTime.now());

        for (Notification notification : scheduledNotifications) {
            try {
                sendScheduledNotification(notification);
            } catch (Exception e) {
                logger.error("Failed to send scheduled notification {}: {}",
                           notification.getId(), e.getMessage());
            }
        }
    }

    private void sendScheduledNotification(Notification notification) {
        // Implementation for sending scheduled notifications
        notification.setSent(true);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldReadNotifications(cutoffDate);
        logger.info("Cleaned up old read notifications before {}", cutoffDate);
    }

    public void sendBulkNotification(NotificationType type, String title, String message,
                                    NotificationPriority priority) {
        List<User> activeUsers = userService.findAllActive();

        for (User user : activeUsers) {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", user.getFullName());
            templateData.put("title", title);
            templateData.put("message", message);

            sendNotification(user.getId(), type, templateData, priority);
        }

        logger.info("Sent bulk notification to {} users", activeUsers.size());
    }

    public void sendProjectNotification(Long projectId, String title, String message) {
        sendProjectNotification(projectId, title, message, NotificationPriority.NORMAL);
    }

    public void sendProjectNotification(Long projectId, String title, String message,
                                       NotificationPriority priority) {
        // Implementation would get project team members and send notifications
        logger.info("Sending project notification for project {}", projectId);
    }

    public void sendBudgetAlert(Long projectId, String title, String message) {
        sendBudgetAlert(projectId, title, message, NotificationPriority.HIGH);
    }

    public void sendBudgetAlert(Long projectId, String title, String message,
                               NotificationPriority priority) {
        // Implementation would get project stakeholders and send budget alerts
        logger.info("Sending budget alert for project {}", projectId);
    }

    // User notification management methods
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return notificationRepository.findByUserIdAndActiveTrueOrderByCreatedDateDesc(userId, pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndActiveTrue(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }

        notification.setRead(true);
        // Fix: use setReadAt instead of setReadDate
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadAndActiveTrue(userId, false);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            // Fix: use setReadAt instead of setReadDate
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);
    }
}

