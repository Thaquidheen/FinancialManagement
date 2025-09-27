// Enhanced NotificationService.java
package com.company.erp.notification.service;

import com.company.erp.common.service.AuditService;
import com.company.erp.notification.entity.*;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.company.erp.notification.dto.response.NotificationResponse;
import com.company.erp.notification.dto.request.SendNotificationRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    public NotificationPreferences getUserPreferences(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        NotificationPreference preference = getOrCreateUserPreference(user);
        return convertToPreferences(preference);
    }

    public NotificationPreferences updateUserPreferences(Long userId, NotificationPreferences preferences) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        NotificationPreference preference = getOrCreateUserPreference(user);
        preference.setEmailEnabled(preferences.isEmailEnabled());
        preference.setSmsEnabled(preferences.isSmsEnabled());
        preference.setInAppEnabled(preferences.isInAppEnabled());
        preference.setPushEnabled(preferences.isPushEnabled());
        
        NotificationPreference saved = preferenceRepository.save(preference);
        return convertToPreferences(saved);
    }

    private NotificationPreferences convertToPreferences(NotificationPreference preference) {
        NotificationPreferences dto = new NotificationPreferences();
        dto.setId(preference.getId());
        dto.setUserId(preference.getUser().getId());
        dto.setEmailEnabled(preference.getEmailEnabled());
        dto.setSmsEnabled(preference.getSmsEnabled());
        dto.setInAppEnabled(preference.getInAppEnabled());
        dto.setPushEnabled(preference.getPushEnabled());
        // Set default values for notification types since they don't exist in the entity
        dto.setQuotationNotifications(true);
        dto.setPaymentNotifications(true);
        dto.setSystemNotifications(true);
        dto.setMarketingNotifications(false);
        return dto;
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndActiveTrue(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }
        
        notification.setActive(false);
        notificationRepository.save(notification);
    }

    // New enhanced notification management methods

    /**
     * Get paginated notifications with filters
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Boolean read, 
                                                          String type, String priority, 
                                                          Pageable pageable) {
        Page<Notification> notifications;
        
        if (read != null || type != null || priority != null) {
            // Use custom query with filters
            notifications = notificationRepository.findByUserIdWithFilters(
                    userId, read, type, priority, pageable);
        } else {
            // Get all active notifications
            notifications = notificationRepository.findByUserIdAndActiveTrueOrderByCreatedDateDesc(
                    userId, pageable);
        }
        
        return notifications.map(this::convertToResponse);
    }

    /**
     * Get notification statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalNotifications", notificationRepository.countByUserIdAndActiveTrue(userId));
        stats.put("unreadCount", notificationRepository.countUnreadByUserId(userId));
        stats.put("readCount", notificationRepository.countReadByUserId(userId));
        
        // Calculate today's count
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        stats.put("todayCount", notificationRepository.countTodayByUserId(userId, startOfDay, endOfDay));
        
        // Calculate week start (Monday)
        LocalDateTime weekStart = LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        stats.put("thisWeekCount", notificationRepository.countThisWeekByUserId(userId, weekStart));
        
        // Notification types breakdown
        Map<String, Long> typeBreakdown = notificationRepository.getNotificationTypeBreakdown(userId);
        stats.put("typeBreakdown", typeBreakdown);
        
        return stats;
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndActiveTrue(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }
        
        return convertToResponse(notification);
    }

    /**
     * Bulk delete notifications
     */
    @Transactional
    public void bulkDeleteNotifications(List<Long> notificationIds, Long userId) {
        List<Notification> notifications = notificationRepository.findByIdsAndUserId(notificationIds, userId);
        
        for (Notification notification : notifications) {
            notification.setActive(false);
        }
        
        notificationRepository.saveAll(notifications);
        
        auditService.logAction(userId, "BULK_DELETE_NOTIFICATIONS", "NOTIFICATION",
                null, "Bulk deleted " + notifications.size() + " notifications", null, null);
    }

    /**
     * Get notification preferences
     */
    @Transactional(readOnly = true)
    public NotificationPreference getNotificationPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Update notification preferences
     */
    @Transactional
    public NotificationPreference updateNotificationPreferences(Long userId, 
                                                               NotificationPreference preferences) {
        NotificationPreference existing = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        
        // Update fields
        existing.setEmailEnabled(preferences.getEmailEnabled());
        existing.setSmsEnabled(preferences.getSmsEnabled());
        existing.setPushEnabled(preferences.getPushEnabled());
        existing.setDoNotDisturbStart(preferences.getDoNotDisturbStart());
        existing.setDoNotDisturbEnd(preferences.getDoNotDisturbEnd());
        existing.setTimezone(preferences.getTimezone());
        existing.setEnabledTypes(preferences.getEnabledTypes());
        
        NotificationPreference saved = preferenceRepository.save(existing);
        
        auditService.logAction(userId, "UPDATE_NOTIFICATION_PREFERENCES", "NOTIFICATION_PREFERENCE",
                null, "Updated notification preferences", null, null);
        
        return saved;
    }

    /**
     * Send test notification
     */
    @Transactional
    public void sendTestNotification(SendNotificationRequest request, Long adminUserId) {
        Long targetUserId = request.getUserId() != null ? request.getUserId() : adminUserId;
        
        // Send test notification
        sendNotification(targetUserId, NotificationType.ANNOUNCEMENT, 
                        Map.of("message", request.getMessage()), 
                        NotificationPriority.NORMAL);
        
        auditService.logAction(adminUserId, "SEND_TEST_NOTIFICATION", "NOTIFICATION",
                null, "Sent test notification to user: " + targetUserId, null, null);
    }

    /**
     * Export notifications
     */
    @Transactional(readOnly = true)
    public byte[] exportNotifications(Long userId, String format, Boolean read, String type) {
        List<Notification> notifications;
        
        if (read != null || type != null) {
            notifications = notificationRepository.findByUserIdWithFiltersForExport(userId, read, type);
        } else {
            notifications = notificationRepository.findByUserIdAndActiveTrueOrderByCreatedDateDesc(userId);
        }
        
        if ("csv".equals(format)) {
            return exportToCsv(notifications);
        } else {
            return exportToExcel(notifications);
        }
    }

    /**
     * Search notifications
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> searchNotifications(Long userId, String query, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndSearchTerm(
                userId, query, pageable);
        
        return notifications.map(this::convertToResponse);
    }

    /**
     * Convert Notification entity to response DTO
     */
    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setPriority(notification.getPriority());
        response.setChannel(notification.getChannel());
        response.setRead(notification.getRead());
        response.setReadAt(notification.getReadAt());
        response.setSent(notification.getSent());
        response.setSentAt(notification.getSentAt());
        response.setCreatedAt(notification.getCreatedDate());
        response.setActionUrl(notification.getActionUrl());
        response.setActionLabel(notification.getActionLabel());
        response.setReferenceType(notification.getReferenceType());
        response.setReferenceId(notification.getReferenceId());
        
        return response;
    }

    /**
     * Create default preferences for new user
     */
    private NotificationPreference createDefaultPreferences(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        NotificationPreference preferences = new NotificationPreference();
        preferences.setUser(user);
        preferences.setEmailEnabled(true);
        preferences.setSmsEnabled(false);
        preferences.setPushEnabled(true);
        preferences.setDoNotDisturbStart(LocalTime.of(22, 0)); // 10 PM
        preferences.setDoNotDisturbEnd(LocalTime.of(7, 0)); // 7 AM
        preferences.setTimezone("Asia/Riyadh");
        preferences.setEnabledTypes(Set.of(NotificationType.values()));
        
        return preferenceRepository.save(preferences);
    }

    /**
     * Export notifications to CSV
     */
    private byte[] exportToCsv(List<Notification> notifications) {
        try (StringWriter writer = new StringWriter();
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("ID", "Title", "Message", "Type", "Priority", "Read", "Created Date")
                     .build())) {
            
            for (Notification notification : notifications) {
                printer.printRecord(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getType(),
                        notification.getPriority(),
                        notification.getRead(),
                        notification.getCreatedDate()
                );
            }
            
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export notifications to CSV", e);
        }
    }

    /**
     * Export notifications to Excel
     */
    private byte[] exportToExcel(List<Notification> notifications) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Notifications");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Title", "Message", "Type", "Priority", "Read", "Created Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Notification notification : notifications) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(notification.getId());
                row.createCell(1).setCellValue(notification.getTitle());
                row.createCell(2).setCellValue(notification.getMessage());
                row.createCell(3).setCellValue(notification.getType().toString());
                row.createCell(4).setCellValue(notification.getPriority().toString());
                row.createCell(5).setCellValue(notification.getRead());
                row.createCell(6).setCellValue(notification.getCreatedDate().toString());
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to export notifications to Excel", e);
        }
    }
}

