// Enhanced SmsService.java
package com.company.erp.notification.service;

import com.company.erp.common.service.AuditService;
import com.company.erp.notification.entity.SmsProvider;
import com.company.erp.notification.entity.SmsLog;
import com.company.erp.notification.entity.SmsStatus;
import com.company.erp.notification.repository.SmsLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    // Saudi mobile number pattern
    private static final Pattern SAUDI_MOBILE_PATTERN = Pattern.compile("^(\\+966|966|0)?5[0-9]{8}$");

    @Value("${app.sms.provider:TAQNYAT}")
    private String defaultProvider;

    @Value("${app.sms.taqnyat.api-key:}")
    private String taqnyatApiKey;

    @Value("${app.sms.taqnyat.sender:}")
    private String taqnyatSender;

    @Value("${app.sms.msegat.api-key:}")
    private String msegatApiKey;

    @Value("${app.sms.msegat.username:}")
    private String msegatUsername;

    @Value("${app.sms.enabled:true}")
    private boolean smsEnabled;

    private final RestTemplate restTemplate;
    private final SmsLogRepository smsLogRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public SmsService(RestTemplate restTemplate,
                      SmsLogRepository smsLogRepository,
                      AuditService auditService,
                      ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.smsLogRepository = smsLogRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Async
    public CompletableFuture<Boolean> sendSms(String phoneNumber, String message) {
        return sendSms(phoneNumber, message, SmsProvider.valueOf(defaultProvider));
    }

    @Async
    public CompletableFuture<Boolean> sendSms(String phoneNumber, String message, SmsProvider provider) {
        if (!smsEnabled) {
            logger.info("SMS service is disabled. Message not sent to: {}", phoneNumber);
            return CompletableFuture.completedFuture(false);
        }

        // Validate phone number
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        if (!isValidSaudiNumber(normalizedNumber)) {
            logger.error("Invalid Saudi phone number: {}", phoneNumber);
            logSmsAttempt(phoneNumber, message, provider, SmsStatus.FAILED, "Invalid phone number", null);
            return CompletableFuture.completedFuture(false);
        }

        // Validate message
        if (message == null || message.trim().isEmpty()) {
            logger.error("SMS message is empty for number: {}", phoneNumber);
            logSmsAttempt(phoneNumber, message, provider, SmsStatus.FAILED, "Empty message", null);
            return CompletableFuture.completedFuture(false);
        }

        try {
            boolean success = false;
            String response = null;
            String errorMessage = null;

            switch (provider) {
                case TAQNYAT:
                    success = sendViaTaqnyat(normalizedNumber, message);
                    break;
                case MSEGAT:
                    success = sendViaMsegat(normalizedNumber, message);
                    break;
                case UNIFONIC:
                    success = sendViaUnifonc(normalizedNumber, message);
                    break;
                default:
                    errorMessage = "Unsupported SMS provider: " + provider;
                    logger.error(errorMessage);
            }

            SmsStatus status = success ? SmsStatus.SENT : SmsStatus.FAILED;
            logSmsAttempt(normalizedNumber, message, provider, status, errorMessage, response);

            if (success) {
                logger.info("SMS sent successfully to {} via {}", normalizedNumber, provider);
            } else {
                logger.error("Failed to send SMS to {} via {}: {}", normalizedNumber, provider, errorMessage);
            }

            return CompletableFuture.completedFuture(success);

        } catch (Exception e) {
            logger.error("Exception while sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
            logSmsAttempt(normalizedNumber, message, provider, SmsStatus.FAILED, e.getMessage(), null);
            return CompletableFuture.completedFuture(false);
        }
    }

    private boolean sendViaTaqnyat(String phoneNumber, String message) {
        try {
            String url = "https://api.taqnyat.sa/v1/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + taqnyatApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("recipients", new String[]{phoneNumber});
            body.put("body", message);
            body.put("sender", taqnyatSender);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Taqnyat API response: {}", response.getBody());
                return true;
            } else {
                logger.error("Taqnyat API error: Status {}, Body: {}", response.getStatusCode(), response.getBody());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error calling Taqnyat API: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendViaMsegat(String phoneNumber, String message) {
        try {
            String url = "https://www.msegat.com/gw/sendsms.php";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> body = new HashMap<>();
            body.put("apiKey", msegatApiKey);
            body.put("numbers", phoneNumber);
            body.put("msg", message);
            body.put("userName", msegatUsername);
            body.put("msgEncoding", "UTF8");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Msegat returns "1" for success
                boolean success = response.getBody().trim().equals("1");
                if (!success) {
                    logger.error("Msegat API error response: {}", response.getBody());
                }
                return success;
            } else {
                logger.error("Msegat API error: Status {}, Body: {}", response.getStatusCode(), response.getBody());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error calling Msegat API: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendViaUnifonc(String phoneNumber, String message) {
        // Implementation for Unifonic SMS provider (popular in Saudi Arabia)
        try {
            String url = "https://api.unifonic.com/v1/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + getUnifonicAuthToken());

            Map<String, Object> body = new HashMap<>();
            body.put("Recipient", phoneNumber);
            body.put("Body", message);
            body.put("SenderID", "YourSenderID");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Unifonic API response: {}", response.getBody());
                return true;
            } else {
                logger.error("Unifonic API error: Status {}, Body: {}", response.getStatusCode(), response.getBody());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error calling Unifonic API: {}", e.getMessage(), e);
            return false;
        }
    }

    private String getUnifonicAuthToken() {
        // Implementation for Unifonic authentication
        return ""; // Placeholder
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // Convert to international format (+966)
        if (cleaned.startsWith("966")) {
            return "+" + cleaned;
        } else if (cleaned.startsWith("05")) {
            return "+966" + cleaned.substring(1);
        } else if (cleaned.startsWith("5") && cleaned.length() == 9) {
            return "+966" + cleaned;
        }

        return "+" + cleaned;
    }

    private boolean isValidSaudiNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        return SAUDI_MOBILE_PATTERN.matcher(phoneNumber).matches();
    }

    private void logSmsAttempt(String phoneNumber, String message, SmsProvider provider,
                               SmsStatus status, String errorMessage, String response) {
        try {
            SmsLog smsLog = new SmsLog();
            smsLog.setPhoneNumber(phoneNumber);
            smsLog.setMessage(message);
            smsLog.setProvider(provider);
            smsLog.setStatus(status);
            smsLog.setErrorMessage(errorMessage);
            smsLog.setProviderResponse(response);
            smsLog.setSentAt(LocalDateTime.now());

            smsLogRepository.save(smsLog);

            // Audit log
            auditService.logAction(null, "SMS_SENT", "SMS_LOG", smsLog.getId(),
                    "SMS " + status.name().toLowerCase() + " to " + phoneNumber, null, null);

        } catch (Exception e) {
            logger.error("Failed to log SMS attempt: {}", e.getMessage(), e);
        }
    }

    // Bulk SMS methods
    @Async
    public CompletableFuture<Boolean> sendBulkSms(Map<String, String> phoneToMessageMap) {
        boolean allSuccess = true;

        for (Map.Entry<String, String> entry : phoneToMessageMap.entrySet()) {
            try {
                boolean success = sendSms(entry.getKey(), entry.getValue()).get();
                if (!success) {
                    allSuccess = false;
                }

                // Rate limiting - wait 100ms between SMS
                Thread.sleep(100);

            } catch (Exception e) {
                logger.error("Error in bulk SMS for {}: {}", entry.getKey(), e.getMessage());
                allSuccess = false;
            }
        }

        return CompletableFuture.completedFuture(allSuccess);
    }

    // Administrative methods
    public void sendTestSms(String phoneNumber) {
        String testMessage = "Test message from ERP System. If you received this, SMS is working correctly.";
        sendSms(phoneNumber, testMessage);
    }

    public boolean isPhoneNumberValid(String phoneNumber) {
        return isValidSaudiNumber(normalizePhoneNumber(phoneNumber));
    }

    public SmsProvider getDefaultProvider() {
        return SmsProvider.valueOf(defaultProvider);
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }
}