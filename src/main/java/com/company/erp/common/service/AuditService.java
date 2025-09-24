// AuditService.java
package com.company.erp.common.service;

import com.company.erp.common.entity.AuditLog;
import com.company.erp.common.repository.AuditLogRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository,
                        UserRepository userRepository,
                        ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public void logAction(Long userId, String actionType, String entityType, Long entityId,
                          String description, Object oldValues, Object newValues) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            AuditLog auditLog = new AuditLog(user, actionType, entityType, entityId, description);

            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }

            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            // Set additional context from HTTP request if available
            setRequestContext(auditLog);

            // Determine severity based on action type
            auditLog.setSeverity(determineSeverity(actionType, entityType));

            // Set category
            auditLog.setCategory(determineCategory(entityType));

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            logger.error("Failed to create audit log: ", e);
        }
    }

    public void logFinancialAction(Long userId, String actionType, String entityType,
                                   Long entityId, Long projectId, String description,
                                   Object oldValues, Object newValues) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            AuditLog auditLog = new AuditLog(user, actionType, entityType, entityId, description);
            auditLog.setProjectId(projectId);

            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }

            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            setRequestContext(auditLog);
            auditLog.setSeverity(determineSeverity(actionType, entityType));
            auditLog.setCategory("FINANCIAL");

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            logger.error("Failed to create financial audit log: ", e);
        }
    }

    private void setRequestContext(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }
        } catch (Exception e) {
            logger.debug("Failed to set request context: ", e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedForHeader)) {
            return xForwardedForHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String determineSeverity(String actionType, String entityType) {
        if ("DELETE".equals(actionType) || "REJECT".equals(actionType)) {
            return "HIGH";
        }
        if ("APPROVE".equals(actionType) || "BUDGET_TRACKING".equals(entityType)) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String determineCategory(String entityType) {
        switch (entityType) {
            case "QUOTATION":
            case "BUDGET_TRACKING":
            case "PAYMENT":
                return "FINANCIAL";
            case "USER":
            case "LOGIN":
                return "SECURITY";
            case "PROJECT":
            case "APPROVAL":
                return "OPERATIONAL";
            default:
                return "GENERAL";
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(List<Long> projectIds, List<String> actionTypes,
                                       List<Long> userIds, LocalDateTime startDate,
                                       LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByFilters(projectIds, actionTypes, userIds,
                startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getProjectAuditLogs(Long projectId, Pageable pageable) {
        return auditLogRepository.findByProjectIdOrderByCreatedDateDesc(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAuditStatistics(LocalDateTime since) {
        LocalDateTime sinceDate = since != null ? since : LocalDateTime.now().minusDays(30);

        long totalEntries = auditLogRepository.count();
        long criticalEntries = auditLogRepository.countBySeverityAndCreatedDateAfter("CRITICAL", sinceDate);
        long highEntries = auditLogRepository.countBySeverityAndCreatedDateAfter("HIGH", sinceDate);

        List<Object[]> actionStats = auditLogRepository.getActionTypeStatistics(sinceDate);
        List<Object[]> categoryStats = auditLogRepository.getCategoryStatistics(sinceDate);

        return Map.of(
                "totalEntries", totalEntries,
                "criticalEntries", criticalEntries,
                "highEntries", highEntries,
                "actionTypeStatistics", actionStats,
                "categoryStatistics", categoryStats,
                "reportPeriod", sinceDate
        );
    }
}