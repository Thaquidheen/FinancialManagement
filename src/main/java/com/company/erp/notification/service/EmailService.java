

package com.company.erp.notification.service;

import com.company.erp.common.exception.BusinessException;

import com.company.erp.notification.dto.response.EmailResponse;
import com.company.erp.notification.entity.EmailLog;
import com.company.erp.notification.repository.EmailLogRepository;
import com.company.erp.user.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Value("${app.notification.email.from-address:noreply@company.com}")
    private String fromAddress;

    @Value("${app.notification.email.from-name:ERP System}")
    private String fromName;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.notification.email.template-path:classpath:/templates/email/}")
    private String templatePath;

    /**
     * Send simple email with plain text
     */
    public EmailResponse sendSimpleEmail(String to, String subject, String content) {
        return sendEmail(to, subject, content, false, null, null);
    }

    /**
     * Send HTML email
     */
    public EmailResponse sendHtmlEmail(String to, String subject, String htmlContent) {
        return sendEmail(to, subject, htmlContent, true, null, null);
    }

    /**
     * Send email with attachments
     */
    public EmailResponse sendEmailWithAttachments(String to, String subject, String content,
                                                  boolean isHtml, List<File> attachments) {
        return sendEmail(to, subject, content, isHtml, attachments, null);
    }

    /**
     * Send templated email
     */
    public EmailResponse sendTemplatedEmail(String to, String subject, String templateName,
                                            Map<String, Object> templateVariables) {
        try {
            String processedContent = processTemplate(templateName, templateVariables);
            return sendEmail(to, subject, processedContent, true, null, null);
        } catch (Exception e) {
            logger.error("Failed to process email template: {}", templateName, e);
            throw new BusinessException("EMAIL_TEMPLATE_ERROR", "Failed to process email template");
        }
    }

    /**
     * Send email to multiple recipients
     */
    public EmailResponse sendBulkEmail(List<String> recipients, String subject, String content, boolean isHtml) {
        EmailResponse response = new EmailResponse();
        int successCount = 0;
        int failureCount = 0;

        for (String recipient : recipients) {
            try {
                EmailResponse singleResponse = sendEmail(recipient, subject, content, isHtml, null, null);
                if (singleResponse.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to send bulk email to: {}", recipient, e);
                failureCount++;
            }
        }

        response.setSuccess(successCount > 0);
        response.setMessage(String.format("Sent %d emails successfully, %d failed", successCount, failureCount));
        response.setSentAt(LocalDateTime.now());

        return response;
    }

    /**
     * Send notification email using predefined templates
     */
    public EmailResponse sendNotificationEmail(User user, String notificationType,
                                               Map<String, Object> templateData) {
        try {
            // Get email template for notification type
            String templateName = getTemplateNameForNotification(notificationType);
            String subject = generateSubjectForNotification(notificationType, templateData);

            // Add user-specific data to template variables
            templateData.put("userName", user.getFullName());
            templateData.put("userEmail", user.getEmail());
            templateData.put("companyName", "Your Company Name");
            templateData.put("systemUrl", "https://erp.company.com");
            templateData.put("currentDate", LocalDateTime.now());

            return sendTemplatedEmail(user.getEmail(), subject, templateName, templateData);

        } catch (Exception e) {
            logger.error("Failed to send notification email to user: {}", user.getId(), e);
            throw new BusinessException("NOTIFICATION_EMAIL_FAILED",
                    "Failed to send notification email: " + e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    public EmailResponse sendPasswordResetEmail(User user, String resetToken) {
        Map<String, Object> templateData = Map.of(
                "userName", user.getFullName(),
                "resetToken", resetToken,
                "resetUrl", "https://erp.company.com/reset-password?token=" + resetToken,
                "expiryHours", "24"
        );

        return sendTemplatedEmail(user.getEmail(), "Password Reset Request",
                "password-reset", templateData);
    }

    /**
     * Send welcome email to new user
     */
    public EmailResponse sendWelcomeEmail(User user, String temporaryPassword) {
        Map<String, Object> templateData = Map.of(
                "userName", user.getFullName(),
                "email", user.getEmail(),
                "temporaryPassword", temporaryPassword,
                "loginUrl", "https://erp.company.com/login",
                "supportEmail", "support@company.com"
        );

        return sendTemplatedEmail(user.getEmail(), "Welcome to ERP System",
                "welcome-user", templateData);
    }

    /**
     * Send budget alert email
     */
    public EmailResponse sendBudgetAlertEmail(User user, String projectName,
                                              String alertLevel, double utilizationPercentage) {
        Map<String, Object> templateData = Map.of(
                "userName", user.getFullName(),
                "projectName", projectName,
                "alertLevel", alertLevel,
                "utilizationPercentage", String.format("%.2f", utilizationPercentage),
                "dashboardUrl", "https://erp.company.com/dashboard"
        );

        String subject = String.format("Budget Alert: %s - %s", alertLevel, projectName);
        return sendTemplatedEmail(user.getEmail(), subject, "budget-alert", templateData);
    }

    /**
     * Send quotation approval email
     */
    public EmailResponse sendQuotationApprovalEmail(User user, String quotationNumber,
                                                    String projectName, String amount) {
        Map<String, Object> templateData = Map.of(
                "userName", user.getFullName(),
                "quotationNumber", quotationNumber,
                "projectName", projectName,
                "amount", amount,
                "approvalUrl", "https://erp.company.com/approvals/" + quotationNumber
        );

        String subject = String.format("Quotation Approval Required: %s", quotationNumber);
        return sendTemplatedEmail(user.getEmail(), subject, "quotation-approval", templateData);
    }

    /**
     * Send daily summary email
     */
    public EmailResponse sendDailySummaryEmail(User user, Map<String, Object> summaryData) {
        summaryData.put("userName", user.getFullName());
        summaryData.put("reportDate", LocalDateTime.now().toLocalDate());

        return sendTemplatedEmail(user.getEmail(), "Daily Activity Summary",
                "daily-summary", summaryData);
    }

    /**
     * Send payment notification email
     */
    public EmailResponse sendPaymentNotificationEmail(User user, String paymentType,
                                                      String amount, String status) {
        Map<String, Object> templateData = Map.of(
                "userName", user.getFullName(),
                "paymentType", paymentType,
                "amount", amount,
                "status", status,
                "paymentDate", LocalDateTime.now(),
                "paymentsUrl", "https://erp.company.com/payments"
        );

        String subject = String.format("Payment %s: %s", status, amount);
        return sendTemplatedEmail(user.getEmail(), subject, "payment-notification", templateData);
    }

    /**
     * Async email sending for better performance
     */
    @Async
    public CompletableFuture<EmailResponse> sendEmailAsync(String to, String subject,
                                                           String content, boolean isHtml) {
        EmailResponse response = sendEmail(to, subject, content, isHtml, null, null);
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Core email sending method
     */
    private EmailResponse sendEmail(String to, String subject, String content,
                                    boolean isHtml, List<File> attachments, String replyTo) {
        EmailResponse response = new EmailResponse();

        if (!emailEnabled) {
            logger.warn("Email sending is disabled");
            response.setSuccess(false);
            response.setMessage("Email sending is disabled");
            return response;
        }

        if (to == null || to.trim().isEmpty()) {
            logger.error("Email recipient is null or empty");
            response.setSuccess(false);
            response.setMessage("Email recipient is required");
            return response;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    attachments != null && !attachments.isEmpty(), "UTF-8");

            // Set basic email properties
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            if (replyTo != null && !replyTo.trim().isEmpty()) {
                helper.setReplyTo(replyTo);
            }

            // Add attachments if provided
            if (attachments != null && !attachments.isEmpty()) {
                for (File attachment : attachments) {
                    if (attachment.exists() && attachment.isFile()) {
                        helper.addAttachment(attachment.getName(), attachment);
                    }
                }
            }

            // Send the email
            mailSender.send(message);

            // Log successful email
            logEmail(to, subject, content, true, null);

            response.setSuccess(true);
            response.setMessage("Email sent successfully");
            response.setRecipient(to);
            response.setSentAt(LocalDateTime.now());

            logger.info("Email sent successfully to: {}", to);

        } catch (MailException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);

            // Log failed email
            logEmail(to, subject, content, false, e.getMessage());

            response.setSuccess(false);
            response.setMessage("Failed to send email: " + e.getMessage());
            response.setErrorMessage(e.getMessage());

        } catch (MessagingException e) {
            logger.error("Messaging exception while sending email to {}: {}", to, e.getMessage(), e);

            // Log failed email
            logEmail(to, subject, content, false, e.getMessage());

            response.setSuccess(false);
            response.setMessage("Email messaging error: " + e.getMessage());
            response.setErrorMessage(e.getMessage());

        } catch (Exception e) {
            logger.error("Unexpected error while sending email to {}: {}", to, e.getMessage(), e);

            // Log failed email
            logEmail(to, subject, content, false, e.getMessage());

            response.setSuccess(false);
            response.setMessage("Unexpected error: " + e.getMessage());
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    /**
     * Process email template with variables
     */
    private String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            // Add common variables
            context.setVariable("currentYear", LocalDateTime.now().getYear());
            context.setVariable("companyName", "Your Company Name");
            context.setVariable("supportEmail", "support@company.com");
            context.setVariable("systemUrl", "https://erp.company.com");

            return templateEngine.process(templateName, context);

        } catch (Exception e) {
            logger.error("Failed to process email template: {}", templateName, e);
            throw new BusinessException("TEMPLATE_PROCESSING_ERROR",
                    "Failed to process email template: " + e.getMessage());
        }
    }

    /**
     * Log email sending attempts
     */
    private void logEmail(String recipient, String subject, String content,
                          boolean success, String errorMessage) {
        try {
            EmailLog emailLog = new EmailLog();
            emailLog.setRecipient(recipient);
            emailLog.setSubject(subject);
            emailLog.setContent(content.length() > 1000 ? content.substring(0, 1000) + "..." : content);
            emailLog.setSuccess(success);
            emailLog.setSentAt(LocalDateTime.now());
            emailLog.setErrorMessage(errorMessage);

            emailLogRepository.save(emailLog);

        } catch (Exception e) {
            logger.error("Failed to log email sending attempt: {}", e.getMessage());
        }
    }

    /**
     * Get template name for notification type
     */
    private String getTemplateNameForNotification(String notificationType) {
        return switch (notificationType) {
            case "QUOTATION_APPROVED" -> "quotation-approved";
            case "QUOTATION_REJECTED" -> "quotation-rejected";
            case "BUDGET_WARNING" -> "budget-warning";
            case "BUDGET_CRITICAL" -> "budget-critical";
            case "BUDGET_EXCEEDED" -> "budget-exceeded";
            case "PROJECT_ASSIGNED" -> "project-assigned";
            case "APPROVAL_REQUEST" -> "approval-request";
            case "PAYMENT_COMPLETED" -> "payment-completed";
            case "PAYMENT_FAILED" -> "payment-failed";
            case "USER_CREATED" -> "user-created";
            case "PASSWORD_CHANGED" -> "password-changed";
            default -> "generic-notification";
        };
    }

    /**
     * Generate subject for notification
     */
    private String generateSubjectForNotification(String notificationType, Map<String, Object> data) {
        return switch (notificationType) {
            case "QUOTATION_APPROVED" -> "Quotation Approved: " + data.get("quotationNumber");
            case "QUOTATION_REJECTED" -> "Quotation Rejected: " + data.get("quotationNumber");
            case "BUDGET_WARNING" -> "Budget Warning: " + data.get("projectName");
            case "BUDGET_CRITICAL" -> "Critical Budget Alert: " + data.get("projectName");
            case "BUDGET_EXCEEDED" -> "Budget Exceeded: " + data.get("projectName");
            case "PROJECT_ASSIGNED" -> "Project Assignment: " + data.get("projectName");
            case "APPROVAL_REQUEST" -> "Approval Required: " + data.get("itemDescription");
            case "PAYMENT_COMPLETED" -> "Payment Completed: " + data.get("amount");
            case "PAYMENT_FAILED" -> "Payment Failed: " + data.get("amount");
            case "USER_CREATED" -> "Welcome to ERP System";
            case "PASSWORD_CHANGED" -> "Password Changed Successfully";
            default -> "ERP System Notification";
        };
    }

    /**
     * Validate email address format
     */
    public boolean isValidEmailAddress(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Get email sending statistics
     */
    public Map<String, Long> getEmailStatistics() {
        return Map.of(
                "totalSent", emailLogRepository.countBySuccess(true),
                "totalFailed", emailLogRepository.countBySuccess(false),
                "totalToday", emailLogRepository.countTodayEmails(),
                "totalThisWeek", emailLogRepository.countThisWeekEmails(),
                "totalThisMonth", emailLogRepository.countThisMonthEmails()
        );
    }

    /**
     * Check email service health
     */
    public boolean isEmailServiceHealthy() {
        try {
            // Test connection by creating a message (but not sending)
            MimeMessage testMessage = mailSender.createMimeMessage();
            return testMessage != null;
        } catch (Exception e) {
            logger.error("Email service health check failed: {}", e.getMessage());
            return false;
        }
    }
}




