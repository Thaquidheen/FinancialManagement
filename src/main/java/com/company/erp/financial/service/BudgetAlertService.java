// Enhanced Alert System
package com.company.erp.financial.service;

import com.company.erp.financial.dto.response.BudgetAlertResponse;
import com.company.erp.notification.service.NotificationService;
import com.company.erp.project.entity.Project;
import com.company.erp.project.service.ProjectService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetAlertService {

    private final ProjectService projectService;
    private final NotificationService notificationService;

    public BudgetAlertService(ProjectService projectService,
                              NotificationService notificationService) {
        this.projectService = projectService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI") // 9 AM on weekdays
    public void checkBudgetAlerts() {
        List<Project> activeProjects = projectService.getActiveProjects();

        for (Project project : activeProjects) {
            checkProjectBudgetAlert(project);
        }
    }

    private void checkProjectBudgetAlert(Project project) {
        BigDecimal utilizationPercentage = project.getBudgetUtilizationPercentage();

        if (utilizationPercentage.compareTo(new BigDecimal("100")) > 0) {
            // Over budget - critical alert
            sendBudgetAlert(project, "CRITICAL", "Project is over budget", utilizationPercentage);
        } else if (utilizationPercentage.compareTo(new BigDecimal("90")) >= 0) {
            // 90% or more - high alert
            sendBudgetAlert(project, "HIGH", "Project approaching budget limit", utilizationPercentage);
        } else if (utilizationPercentage.compareTo(new BigDecimal("80")) >= 0) {
            // 80% or more - warning alert
            sendBudgetAlert(project, "WARNING", "Budget utilization warning", utilizationPercentage);
        }
    }

    private void sendBudgetAlert(Project project, String alertLevel, String message, BigDecimal utilization) {
        // Create alert notification
        String subject = String.format("Budget Alert - %s: %s", project.getName(), message);
        String body = String.format(
                "Project: %s\nManager: %s\nBudget Utilization: %s%%\nAllocated: SAR %s\nSpent: SAR %s\nRemaining: SAR %s",
                project.getName(),
                project.getManager().getFullName(),
                utilization,
                project.getAllocatedBudget(),
                project.getSpentAmount(),
                project.getRemainingBudget()
        );

        // Send to project manager using the correct createNotification method
        if (project.getManager() != null) {
            notificationService.createNotification(
                project.getManager().getId(),
                subject,
                body,
                "BUDGET_ALERT"
            );
        }

        // Send SMS for critical alerts - use createNotification instead of sendSmsNotification
        if ("CRITICAL".equals(alertLevel) && project.getManager() != null) {
            String smsMessage = String.format("CRITICAL: %s is over budget. Check email for details.", project.getName());
            notificationService.createNotification(
                project.getManager().getId(),
                "Budget Alert",
                smsMessage,
                "BUDGET_ALERT"
            );
        }
    }

    public List<BudgetAlertResponse> getCurrentBudgetAlerts() {
        List<Project> activeProjects = projectService.getActiveProjects();

        return activeProjects.stream()
                .filter(project -> project.getBudgetUtilizationPercentage().compareTo(new BigDecimal("80")) >= 0)
                .map(this::createBudgetAlertResponse)
                .collect(Collectors.toList());
    }

    private BudgetAlertResponse createBudgetAlertResponse(Project project) {
        BudgetAlertResponse alert = new BudgetAlertResponse();
        alert.setProjectId(project.getId());
        alert.setProjectName(project.getName());
        alert.setProjectManager(project.getManager().getFullName());
        alert.setAllocatedBudget(project.getAllocatedBudget());
        alert.setSpentAmount(project.getSpentAmount());
        alert.setRemainingBudget(project.getRemainingBudget());

        BigDecimal utilization = project.getBudgetUtilizationPercentage();
        alert.setUtilizationPercentage(utilization);

        // Calculate variance percentage
        BigDecimal variance = project.getSpentAmount().subtract(project.getAllocatedBudget());
        if (project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variancePercentage = variance.divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            alert.setVariancePercentage(variancePercentage);
        }

        // Set alert level and message
        if (utilization.compareTo(new BigDecimal("100")) > 0) {
            alert.setAlertLevel("CRITICAL");
            alert.setAlertMessage("Project is over budget");
            alert.setOverBudget(true);
            alert.setRequiresAttention(true);
        } else if (utilization.compareTo(new BigDecimal("90")) >= 0) {
            alert.setAlertLevel("HIGH");
            alert.setAlertMessage("Project approaching budget limit");
            alert.setRequiresAttention(true);
        } else if (utilization.compareTo(new BigDecimal("80")) >= 0) {
            alert.setAlertLevel("WARNING");
            alert.setAlertMessage("Budget utilization warning");
            alert.setRequiresAttention(true);
        }

        alert.setLastUpdated(LocalDateTime.now());
        return alert;
    }
}