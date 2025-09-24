package com.company.erp.financial.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.dto.response.FinancialDashboardResponse;
import com.company.erp.financial.dto.response.FinancialReportResponse;
import com.company.erp.financial.entity.Quotation;
import com.company.erp.financial.entity.QuotationStatus;
import com.company.erp.financial.repository.QuotationRepository;
import com.company.erp.financial.repository.BudgetTrackingRepository;
import com.company.erp.project.entity.Project;
import com.company.erp.project.repository.ProjectRepository;
import com.company.erp.report.dto.request.ReportRequest;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinancialReportService {

    private static final Logger logger = LoggerFactory.getLogger(FinancialReportService.class);

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private BudgetTrackingRepository budgetTrackingRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Get comprehensive financial dashboard
     */
    public FinancialDashboardResponse getFinancialDashboard(LocalDate startDate, LocalDate endDate, Long userId) {
        logger.info("Generating financial dashboard for date range: {} to {}", startDate, endDate);

        UserPrincipal currentUser = getCurrentUser();
        validateDashboardAccess(currentUser);

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(6);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        FinancialDashboardResponse dashboard = new FinancialDashboardResponse();

        // Key Financial Metrics
        dashboard.setTotalBudgetAllocated(getTotalAllocatedBudget(start, end));
        dashboard.setTotalSpent(getTotalSpentAmount(start, end));
        dashboard.setTotalRemaining(dashboard.getTotalBudgetAllocated().subtract(dashboard.getTotalSpent()));

        // Calculate utilization percentage
        if (dashboard.getTotalBudgetAllocated().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilization = dashboard.getTotalSpent()
                    .divide(dashboard.getTotalBudgetAllocated(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            dashboard.setOverallUtilizationPercentage(utilization);
        }

        // Quotation Statistics
        // Additional quotation stats can be added to a separate report if needed

        // Recent Activity
        dashboard.setRecentTransactions(mapRecentTransactions(getRecentTransactions(10)));
        dashboard.setBudgetAlerts(mapBudgetAlerts(getBudgetAlerts()));

        // Monthly Trends
        dashboard.setSpendingTrends(mapSpendingTrends(getMonthlySpendingTrend(start, end)));
        dashboard.setCategorySpending(mapCategorySpending(getCategoryWiseSpending(start, end)));

        dashboard.setReportGeneratedAt(LocalDateTime.now());

        logger.info("Financial dashboard generated successfully");
        return dashboard;
    }

    /**
     * Get project manager specific dashboard
     */
    public FinancialDashboardResponse getProjectManagerDashboard(LocalDate startDate, LocalDate endDate, Long userId) {
        logger.info("Generating project manager dashboard for user: {}", userId);

        UserPrincipal currentUser = getCurrentUser();

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(3);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        // Get projects managed by this user
        List<Project> managedProjects = projectRepository.findByManagerIdAndActiveTrue(userId);
        List<Long> projectIds = managedProjects.stream().map(Project::getId).collect(Collectors.toList());

        FinancialDashboardResponse dashboard = new FinancialDashboardResponse();

        // Calculate totals for managed projects only
        dashboard.setTotalBudgetAllocated(getTotalAllocatedBudgetForProjects(projectIds));
        dashboard.setTotalSpent(getTotalSpentAmountForProjects(projectIds));
        dashboard.setTotalRemaining(dashboard.getTotalBudgetAllocated().subtract(dashboard.getTotalSpent()));

        // Project-specific metrics
        dashboard.setActiveProjects(managedProjects.size());

        // Recent activity for managed projects
        dashboard.setRecentTransactions(mapRecentTransactions(getRecentTransactionsForProjects(projectIds, 10)));

        dashboard.setReportGeneratedAt(LocalDateTime.now());

        return dashboard;
    }

    /**
     * Generate detailed financial report
     */
    public FinancialReportResponse generateFinancialReport(ReportRequest request, Long userId) {
        logger.info("Generating financial report: {}", request.getReportType());

        UserPrincipal currentUser = getCurrentUser();
        validateReportAccess(currentUser);

        LocalDateTime start = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : LocalDateTime.now();

        FinancialReportResponse report = new FinancialReportResponse();
        report.setReportType(request.getReportType());
        report.setStartDate(start);
        report.setEndDate(end);
        report.setGeneratedBy(getUserById(userId).getFullName());
        report.setGeneratedAt(LocalDateTime.now());

        switch (request.getReportType().toUpperCase()) {
            case "BUDGET_UTILIZATION":
                report = generateBudgetUtilizationReport(request, start, end);
                break;
            case "SPENDING_ANALYSIS":
                report = generateSpendingAnalysisReport(request, start, end);
                break;
            case "PROJECT_FINANCIAL":
                report = generateProjectFinancialReport(request, start, end);
                break;
            case "COMPREHENSIVE":
                report = generateComprehensiveReport(request, start, end);
                break;
            default:
                throw new BusinessException("INVALID_REPORT_TYPE", "Unsupported report type: " + request.getReportType());
        }

        return report;
    }

    /**
     * Get budget utilization report
     */
    public Object getBudgetUtilizationReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(3);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> report = new HashMap<>();

        List<Object[]> utilizationData;
        if ("PROJECT".equalsIgnoreCase(groupBy)) {
            utilizationData = budgetTrackingRepository.getProjectBudgetBreakdown(start, end);
        } else if ("DEPARTMENT".equalsIgnoreCase(groupBy)) {
            utilizationData = quotationRepository.getQuotationStatsByDepartment();
        } else {
            utilizationData = budgetTrackingRepository.getBudgetSummary(start, end);
        }

        report.put("utilizationData", utilizationData);
        report.put("groupBy", groupBy);
        report.put("startDate", start);
        report.put("endDate", end);
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    /**
     * Get spending trends analysis
     */
    public Object getSpendingTrends(LocalDate startDate, LocalDate endDate, String period, List<Long> projectIds) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(6);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> trends = new HashMap<>();

        // Get spending trends based on period
        List<Object[]> trendData;
        if (projectIds != null && !projectIds.isEmpty()) {
            trendData = getSpendingTrendsByProjects(projectIds, start, end, period);
        } else {
            trendData = getSpendingTrendsByPeriod(start, end, period);
        }

        trends.put("trendData", trendData);
        trends.put("period", period);
        trends.put("projectIds", projectIds);
        trends.put("totalSpending", calculateTotalSpending(trendData));
        trends.put("averageSpending", calculateAverageSpending(trendData));
        trends.put("projectedSpending", calculateProjectedSpending(trendData, period));

        return trends;
    }

    /**
     * Get detailed cost analysis
     */
    public Object getCostAnalysis(LocalDate startDate, LocalDate endDate, List<String> categories, List<Long> projectIds) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(3);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> analysis = new HashMap<>();

        // Category-wise analysis
        List<Object[]> categoryData = budgetTrackingRepository.getBudgetTrackingByCategory();
        analysis.put("categoryBreakdown", categoryData);

        // Project-wise analysis if specified
        if (projectIds != null && !projectIds.isEmpty()) {
            Map<String, Object> projectAnalysis = new HashMap<>();
            for (Long projectId : projectIds) {
                List<Object[]> projectData = budgetTrackingRepository.getProjectBudgetTrackingByType(projectId);
                projectAnalysis.put("project_" + projectId, projectData);
            }
            analysis.put("projectAnalysis", projectAnalysis);
        }

        // Cost variance analysis
        analysis.put("costVariance", calculateCostVariance(start, end, projectIds));
        analysis.put("topCostCategories", getTopCostCategories(start, end, 10));

        return analysis;
    }

    /**
     * Get variance analysis (Budget vs Actual)
     */
    public Object getVarianceAnalysis(LocalDate startDate, LocalDate endDate, List<Long> projectIds) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(3);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> analysis = new HashMap<>();
        List<Map<String, Object>> projectVariances = new ArrayList<>();

        List<Project> projects;
        if (projectIds != null && !projectIds.isEmpty()) {
            projects = projectRepository.findAllById(projectIds);
        } else {
            projects = projectRepository.findByActiveTrue();
        }

        for (Project project : projects) {
            Map<String, Object> variance = new HashMap<>();
            variance.put("projectId", project.getId());
            variance.put("projectName", project.getName());
            variance.put("allocatedBudget", project.getAllocatedBudget());
            variance.put("spentAmount", project.getSpentAmount());
            variance.put("remainingBudget", project.getRemainingBudget());

            // Calculate variance
            BigDecimal varianceAmount = project.getSpentAmount().subtract(project.getAllocatedBudget());
            BigDecimal variancePercentage = BigDecimal.ZERO;
            if (project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
                variancePercentage = varianceAmount.divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            variance.put("varianceAmount", varianceAmount);
            variance.put("variancePercentage", variancePercentage);
            variance.put("status", getVarianceStatus(variancePercentage));

            projectVariances.add(variance);
        }

        analysis.put("projectVariances", projectVariances);
        analysis.put("overallVariance", calculateOverallVariance(projectVariances));
        analysis.put("analysisDate", LocalDateTime.now());

        return analysis;
    }

    /**
     * Get financial forecasting
     */
    public Object getFinancialForecasting(List<Long> projectIds, int monthsAhead, String forecastMethod) {
        Map<String, Object> forecast = new HashMap<>();

        List<Project> projects;
        if (projectIds != null && !projectIds.isEmpty()) {
            projects = projectRepository.findAllById(projectIds);
        } else {
            projects = projectRepository.findByActiveTrue();
        }

        List<Map<String, Object>> projectForecasts = new ArrayList<>();

        for (Project project : projects) {
            Map<String, Object> projectForecast = generateProjectForecast(project, monthsAhead, forecastMethod);
            projectForecasts.add(projectForecast);
        }

        forecast.put("projectForecasts", projectForecasts);
        forecast.put("overallForecast", calculateOverallForecast(projectForecasts));
        forecast.put("forecastMethod", forecastMethod);
        forecast.put("monthsAhead", monthsAhead);
        forecast.put("generatedAt", LocalDateTime.now());

        return forecast;
    }

    /**
     * Get executive summary
     */
    public Object getExecutiveSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(6);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> summary = new HashMap<>();

        // High-level metrics
        summary.put("totalBudgetAllocated", getTotalAllocatedBudget(start, end));
        summary.put("totalAmountSpent", getTotalSpentAmount(start, end));
        summary.put("activeProjectsCount", projectRepository.countByActiveTrue());
        summary.put("completedProjectsCount", projectRepository.countByStatusAndActiveTrue("COMPLETED"));

        // Performance indicators
        summary.put("budgetUtilizationRate", calculateOverallUtilizationRate());
        summary.put("averageProjectDuration", calculateAverageProjectDuration());
        summary.put("costEfficiencyRatio", calculateCostEfficiencyRatio());

        // Risk indicators
        summary.put("projectsOverBudget", getProjectsOverBudget());
        summary.put("budgetAlertsCount", getBudgetAlertsCount());
        summary.put("riskProjects", getHighRiskProjects());

        // Trends
        summary.put("spendingTrend", getExecutiveSpendingTrend(start, end));
        summary.put("quarterComparison", getQuarterlyComparison());

        return summary;
    }

    /**
     * Get audit trail
     */
    public Object getAuditTrail(LocalDate startDate, LocalDate endDate, List<Long> projectIds,
                                List<String> actionTypes, List<Long> userIds) {
        // This would integrate with your audit service
        Map<String, Object> auditTrail = new HashMap<>();

        // Placeholder implementation - would need integration with AuditService
        auditTrail.put("message", "Audit trail functionality requires AuditService integration");
        auditTrail.put("startDate", startDate);
        auditTrail.put("endDate", endDate);
        auditTrail.put("projectIds", projectIds);
        auditTrail.put("actionTypes", actionTypes);
        auditTrail.put("userIds", userIds);

        return auditTrail;
    }

    /**
     * Export financial report
     */
    public String exportFinancialReport(ReportRequest request, String format, Long userId) {
        logger.info("Exporting financial report in format: {}", format);

        try {
            FinancialReportResponse report = generateFinancialReport(request, userId);

            switch (format.toUpperCase()) {
                case "EXCEL":
                    return exportToExcel(report);
                case "PDF":
                    return exportToPDF(report);
                case "CSV":
                    return exportToCSV(report);
                default:
                    throw new BusinessException("INVALID_FORMAT", "Unsupported export format: " + format);
            }
        } catch (Exception e) {
            logger.error("Failed to export financial report", e);
            throw new BusinessException("EXPORT_FAILED", "Failed to export report: " + e.getMessage());
        }
    }

    /**
     * Get financial KPIs
     */
    public Map<String, Object> getFinancialKPIs(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(3);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> kpis = new HashMap<>();

        // Budget KPIs
        kpis.put("budgetUtilizationRate", calculateOverallUtilizationRate());
        kpis.put("budgetVariancePercentage", calculateOverallBudgetVariance());
        kpis.put("costPerProject", calculateAverageCostPerProject());

        // Efficiency KPIs
        kpis.put("approvalCycleTime", calculateAverageApprovalTime());
        kpis.put("paymentProcessingTime", calculateAveragePaymentTime());
        kpis.put("documentProcessingEfficiency", calculateDocumentEfficiency());

        // Quality KPIs
        kpis.put("budgetAccuracyRate", calculateBudgetAccuracy());
        kpis.put("forecastAccuracy", calculateForecastAccuracy());
        kpis.put("complianceScore", calculateComplianceScore());

        return kpis;
    }

    /**
     * Get performance metrics
     */
    public Object getPerformanceMetrics(LocalDate startDate, LocalDate endDate, String metricType) {
        Map<String, Object> metrics = new HashMap<>();

        switch (metricType != null ? metricType.toUpperCase() : "ALL") {
            case "FINANCIAL":
                metrics.put("financialMetrics", getFinancialPerformanceMetrics(startDate, endDate));
                break;
            case "OPERATIONAL":
                metrics.put("operationalMetrics", getOperationalPerformanceMetrics(startDate, endDate));
                break;
            case "PROJECT":
                metrics.put("projectMetrics", getProjectPerformanceMetrics(startDate, endDate));
                break;
            default:
                metrics.put("financialMetrics", getFinancialPerformanceMetrics(startDate, endDate));
                metrics.put("operationalMetrics", getOperationalPerformanceMetrics(startDate, endDate));
                metrics.put("projectMetrics", getProjectPerformanceMetrics(startDate, endDate));
        }

        return metrics;
    }

    // Private helper methods

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserById(Long userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private void validateDashboardAccess(UserPrincipal currentUser) {
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER", "PROJECT_MANAGER")) {
            throw new BusinessException("ACCESS_DENIED", "Insufficient permissions for dashboard access");
        }
    }

    private void validateReportAccess(UserPrincipal currentUser) {
        if (!currentUser.hasAnyRole("SUPER_ADMIN", "ACCOUNT_MANAGER")) {
            throw new BusinessException("ACCESS_DENIED", "Insufficient permissions for report generation");
        }
    }

    private BigDecimal getTotalAllocatedBudget(LocalDateTime start, LocalDateTime end) {
        List<Object[]> summary = budgetTrackingRepository.getBudgetSummary(start, end);
        return !summary.isEmpty() ? (BigDecimal) summary.get(0)[0] : BigDecimal.ZERO;
    }

    private BigDecimal getTotalSpentAmount(LocalDateTime start, LocalDateTime end) {
        List<Object[]> summary = budgetTrackingRepository.getBudgetSummary(start, end);
        return !summary.isEmpty() ? (BigDecimal) summary.get(0)[1] : BigDecimal.ZERO;
    }

    private BigDecimal getTotalAllocatedBudgetForProjects(List<Long> projectIds) {
        if (projectIds.isEmpty()) return BigDecimal.ZERO;

        return projectIds.stream()
                .map(id -> projectRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Project::getAllocatedBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalSpentAmountForProjects(List<Long> projectIds) {
        if (projectIds.isEmpty()) return BigDecimal.ZERO;

        return projectIds.stream()
                .map(id -> projectRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Project::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long getPendingQuotationsCountForProjects(List<Long> projectIds) {
        // This would need a custom repository method
        return 0; // Placeholder
    }

    private long getApprovedQuotationsCountForProjects(List<Long> projectIds) {
        // This would need a custom repository method
        return 0; // Placeholder
    }

    private List<Map<String, Object>> getRecentTransactions(int limit) {
        // Get recent budget tracking transactions
        List<Map<String, Object>> transactions = new ArrayList<>();
        // Implementation would fetch from BudgetTrackingRepository
        return transactions;
    }

    private List<FinancialDashboardResponse.RecentTransaction> mapRecentTransactions(List<Map<String, Object>> items) {
        return items.stream().map(map -> {
            FinancialDashboardResponse.RecentTransaction rt = new FinancialDashboardResponse.RecentTransaction();
            rt.setId((Long) map.getOrDefault("id", null));
            rt.setProjectName((String) map.getOrDefault("projectName", ""));
            rt.setTransactionType((String) map.getOrDefault("transactionType", ""));
            rt.setAmount((BigDecimal) map.getOrDefault("amount", BigDecimal.ZERO));
            rt.setDescription((String) map.getOrDefault("description", ""));
            rt.setTransactionDate((LocalDateTime) map.getOrDefault("transactionDate", null));
            rt.setStatus((String) map.getOrDefault("status", ""));
            return rt;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getRecentTransactionsForProjects(List<Long> projectIds, int limit) {
        // Get recent transactions for specific projects
        List<Map<String, Object>> transactions = new ArrayList<>();
        // Implementation would fetch from BudgetTrackingRepository
        return transactions;
    }

    private List<Map<String, Object>> getBudgetAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        List<Project> projects = projectRepository.findByActiveTrue();
        for (Project project : projects) {
            BigDecimal utilizationPercentage = BigDecimal.ZERO;
            if (project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
                utilizationPercentage = project.getSpentAmount()
                        .divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            if (utilizationPercentage.compareTo(new BigDecimal("80")) >= 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("projectId", project.getId());
                alert.put("projectName", project.getName());
                alert.put("utilization", utilizationPercentage);
                alert.put("alertLevel", utilizationPercentage.compareTo(new BigDecimal("100")) > 0 ? "CRITICAL" :
                        utilizationPercentage.compareTo(new BigDecimal("90")) >= 0 ? "HIGH" : "WARNING");
                alerts.add(alert);
            }
        }

        return alerts;
    }

    private List<FinancialDashboardResponse.BudgetAlertSummary> mapBudgetAlerts(List<Map<String, Object>> items) {
        return items.stream().map(map -> {
            FinancialDashboardResponse.BudgetAlertSummary b = new FinancialDashboardResponse.BudgetAlertSummary();
            b.setProjectId((Long) map.getOrDefault("projectId", null));
            b.setProjectName((String) map.getOrDefault("projectName", ""));
            b.setAlertLevel((String) map.getOrDefault("alertLevel", ""));
            b.setMessage((String) map.getOrDefault("message", ""));
            b.setUtilizationPercentage((BigDecimal) map.getOrDefault("utilization", BigDecimal.ZERO));
            b.setAlertTime(LocalDateTime.now());
            return b;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getTopSpendingProjects(int limit) {
        List<Project> projects = projectRepository.findByActiveTrueOrderBySpentAmountDesc();

        return projects.stream()
                .limit(limit)
                .map(project -> {
                    Map<String, Object> projectData = new HashMap<>();
                    projectData.put("projectId", project.getId());
                    projectData.put("projectName", project.getName());
                    projectData.put("spentAmount", project.getSpentAmount());
                    projectData.put("allocatedBudget", project.getAllocatedBudget());
                    return projectData;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getProjectBudgetStatusList(List<Long> projectIds) {
        return projectIds.stream()
                .map(projectRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(project -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("projectId", project.getId());
                    status.put("projectName", project.getName());
                    status.put("allocatedBudget", project.getAllocatedBudget());
                    status.put("spentAmount", project.getSpentAmount());
                    status.put("remainingBudget", project.getRemainingBudget());

                    BigDecimal utilization = BigDecimal.ZERO;
                    if (project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
                        utilization = project.getSpentAmount()
                                .divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
                    }
                    status.put("utilizationPercentage", utilization);

                    return status;
                })
                .collect(Collectors.toList());
    }

    private List<Object[]> getMonthlySpendingTrend(LocalDateTime start, LocalDateTime end) {
        // This would call a custom repository method for monthly aggregation
        return new ArrayList<>();
    }

    private List<FinancialDashboardResponse.SpendingTrendData> mapSpendingTrends(List<Object[]> rows) {
        return rows.stream().map(row -> {
            FinancialDashboardResponse.SpendingTrendData d = new FinancialDashboardResponse.SpendingTrendData();
            d.setPeriod(String.valueOf(row[0]));
            d.setAmount((BigDecimal) row[1]);
            if (row.length > 2 && row[2] instanceof BigDecimal) {
                d.setCumulativeAmount((BigDecimal) row[2]);
            }
            if (row.length > 3 && row[3] instanceof Integer) {
                d.setTransactionCount((Integer) row[3]);
            }
            return d;
        }).collect(Collectors.toList());
    }

    private Map<String, BigDecimal> getCategoryWiseSpending(LocalDateTime start, LocalDateTime end) {
        List<Object[]> categoryData = budgetTrackingRepository.getBudgetTrackingByCategory();
        Map<String, BigDecimal> categorySpending = new HashMap<>();

        for (Object[] data : categoryData) {
            String category = (String) data[0];
            BigDecimal amount = (BigDecimal) data[2];
            categorySpending.put(category, amount);
        }

        return categorySpending;
    }

    private List<FinancialDashboardResponse.CategorySpendingData> mapCategorySpending(Map<String, BigDecimal> map) {
        return map.entrySet().stream().map(e -> {
            FinancialDashboardResponse.CategorySpendingData d = new FinancialDashboardResponse.CategorySpendingData();
            d.setCategory(e.getKey());
            d.setAmount(e.getValue());
            return d;
        }).collect(Collectors.toList());
    }

    // Additional helper methods would be implemented here...

    private FinancialReportResponse generateBudgetUtilizationReport(ReportRequest request, LocalDateTime start, LocalDateTime end) {
        // Implementation for budget utilization report
        return new FinancialReportResponse();
    }

    private FinancialReportResponse generateSpendingAnalysisReport(ReportRequest request, LocalDateTime start, LocalDateTime end) {
        // Implementation for spending analysis report
        return new FinancialReportResponse();
    }

    private FinancialReportResponse generateProjectFinancialReport(ReportRequest request, LocalDateTime start, LocalDateTime end) {
        // Implementation for project financial report
        return new FinancialReportResponse();
    }

    private FinancialReportResponse generateComprehensiveReport(ReportRequest request, LocalDateTime start, LocalDateTime end) {
        // Implementation for comprehensive report
        return new FinancialReportResponse();
    }

    private String exportToCSV(FinancialReportResponse report) {
        // CSV export implementation
        return "csv_export_placeholder.csv";
    }

    private List<Object[]> getSpendingTrendsByProjects(List<Long> projectIds, LocalDateTime start, LocalDateTime end, String period) {
        // Implementation for project-specific spending trends
        return new ArrayList<>();
    }

    private List<Object[]> getSpendingTrendsByPeriod(LocalDateTime start, LocalDateTime end, String period) {
        // Implementation for overall spending trends
        return new ArrayList<>();
    }

    private BigDecimal calculateTotalSpending(List<Object[]> trendData) {
        return trendData.stream()
                .map(data -> (BigDecimal) data[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageSpending(List<Object[]> trendData) {
        if (trendData.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = calculateTotalSpending(trendData);
        return total.divide(new BigDecimal(trendData.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateProjectedSpending(List<Object[]> trendData, String period) {
        // Simple linear projection - could be enhanced with more sophisticated forecasting
        if (trendData.size() < 2) return BigDecimal.ZERO;

        BigDecimal lastAmount = (BigDecimal) trendData.get(trendData.size() - 1)[1];
        BigDecimal secondLastAmount = (BigDecimal) trendData.get(trendData.size() - 2)[1];
        BigDecimal trend = lastAmount.subtract(secondLastAmount);

        return lastAmount.add(trend);
    }

    private Map<String, Object> calculateCostVariance(LocalDateTime start, LocalDateTime end, List<Long> projectIds) {
        Map<String, Object> variance = new HashMap<>();
        // Implementation for cost variance calculation
        return variance;
    }

    private List<Object[]> getTopCostCategories(LocalDateTime start, LocalDateTime end, int limit) {
        List<Object[]> categoryData = budgetTrackingRepository.getBudgetTrackingByCategory();
        return categoryData.stream()
                .sorted((a, b) -> ((BigDecimal) b[2]).compareTo((BigDecimal) a[2]))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String getVarianceStatus(BigDecimal variancePercentage) {
        if (variancePercentage.compareTo(new BigDecimal("10")) > 0) {
            return "OVER_BUDGET";
        } else if (variancePercentage.compareTo(new BigDecimal("-10")) < 0) {
            return "UNDER_BUDGET";
        } else {
            return "ON_TARGET";
        }
    }

    private Map<String, Object> calculateOverallVariance(List<Map<String, Object>> projectVariances) {
        Map<String, Object> overall = new HashMap<>();

        BigDecimal totalAllocated = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Map<String, Object> variance : projectVariances) {
            totalAllocated = totalAllocated.add((BigDecimal) variance.get("allocatedBudget"));
            totalSpent = totalSpent.add((BigDecimal) variance.get("spentAmount"));
        }

        BigDecimal overallVariance = totalSpent.subtract(totalAllocated);
        BigDecimal overallVariancePercentage = BigDecimal.ZERO;

        if (totalAllocated.compareTo(BigDecimal.ZERO) > 0) {
            overallVariancePercentage = overallVariance.divide(totalAllocated, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        overall.put("totalAllocated", totalAllocated);
        overall.put("totalSpent", totalSpent);
        overall.put("variance", overallVariance);
        overall.put("variancePercentage", overallVariancePercentage);

        return overall;
    }

    private Map<String, Object> generateProjectForecast(Project project, int monthsAhead, String forecastMethod) {
        Map<String, Object> forecast = new HashMap<>();

        forecast.put("projectId", project.getId());
        forecast.put("projectName", project.getName());
        forecast.put("currentSpent", project.getSpentAmount());
        forecast.put("allocatedBudget", project.getAllocatedBudget());

        // Simple linear forecasting - could be enhanced with more sophisticated methods
        BigDecimal monthlySpendingRate = calculateMonthlySpendingRate(project);
        BigDecimal projectedSpending = project.getSpentAmount().add(
                monthlySpendingRate.multiply(new BigDecimal(monthsAhead))
        );

        forecast.put("projectedSpending", projectedSpending);
        forecast.put("projectedOverrun", projectedSpending.subtract(project.getAllocatedBudget()));
        forecast.put("forecastMethod", forecastMethod);

        return forecast;
    }

    private BigDecimal calculateMonthlySpendingRate(Project project) {
        // This would analyze historical spending patterns
        // Placeholder implementation
        return project.getSpentAmount().divide(new BigDecimal("6"), RoundingMode.HALF_UP);
    }

    private Map<String, Object> calculateOverallForecast(List<Map<String, Object>> projectForecasts) {
        Map<String, Object> overall = new HashMap<>();

        BigDecimal totalProjected = projectForecasts.stream()
                .map(forecast -> (BigDecimal) forecast.get("projectedSpending"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAllocated = projectForecasts.stream()
                .map(forecast -> (BigDecimal) forecast.get("allocatedBudget"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        overall.put("totalProjectedSpending", totalProjected);
        overall.put("totalAllocatedBudget", totalAllocated);
        overall.put("projectedOverrun", totalProjected.subtract(totalAllocated));

        return overall;
    }

    // Executive summary helper methods
    private BigDecimal calculateOverallUtilizationRate() {
        List<Project> projects = projectRepository.findByActiveTrue();
        BigDecimal totalAllocated = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Project project : projects) {
            totalAllocated = totalAllocated.add(project.getAllocatedBudget());
            totalSpent = totalSpent.add(project.getSpentAmount());
        }

        if (totalAllocated.compareTo(BigDecimal.ZERO) > 0) {
            return totalSpent.divide(totalAllocated, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }

        return BigDecimal.ZERO;
    }

    private Double calculateAverageProjectDuration() {
        // Implementation for average project duration calculation
        return 6.0; // Placeholder
    }

    private BigDecimal calculateCostEfficiencyRatio() {
        // Implementation for cost efficiency ratio
        return new BigDecimal("85.5"); // Placeholder
    }

    private List<Map<String, Object>> getProjectsOverBudget() {
        return projectRepository.findByActiveTrue().stream()
                .filter(project -> project.getSpentAmount().compareTo(project.getAllocatedBudget()) > 0)
                .map(project -> {
                    Map<String, Object> overBudget = new HashMap<>();
                    overBudget.put("projectId", project.getId());
                    overBudget.put("projectName", project.getName());
                    overBudget.put("overrun", project.getSpentAmount().subtract(project.getAllocatedBudget()));
                    return overBudget;
                })
                .collect(Collectors.toList());
    }

    private long getBudgetAlertsCount() {
        return getBudgetAlerts().size();
    }

    private List<Map<String, Object>> getHighRiskProjects() {
        return projectRepository.findByActiveTrue().stream()
                .filter(project -> {
                    BigDecimal utilization = BigDecimal.ZERO;
                    if (project.getAllocatedBudget().compareTo(BigDecimal.ZERO) > 0) {
                        utilization = project.getSpentAmount()
                                .divide(project.getAllocatedBudget(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
                    }
                    return utilization.compareTo(new BigDecimal("90")) >= 0;
                })
                .map(project -> {
                    Map<String, Object> riskProject = new HashMap<>();
                    riskProject.put("projectId", project.getId());
                    riskProject.put("projectName", project.getName());
                    riskProject.put("riskLevel", "HIGH");
                    return riskProject;
                })
                .collect(Collectors.toList());
    }

    private List<Object[]> getExecutiveSpendingTrend(LocalDateTime start, LocalDateTime end) {
        // Implementation for executive-level spending trends
        return new ArrayList<>();
    }

    private Map<String, Object> getQuarterlyComparison() {
        // Implementation for quarterly comparison
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("currentQuarter", "Q4 2024");
        comparison.put("previousQuarter", "Q3 2024");
        comparison.put("growthRate", new BigDecimal("12.5"));
        return comparison;
    }

    // KPI calculation methods
    private BigDecimal calculateOverallBudgetVariance() {
        // Implementation for overall budget variance
        return new BigDecimal("5.2"); // Placeholder
    }

    private BigDecimal calculateAverageCostPerProject() {
        List<Project> projects = projectRepository.findByActiveTrue();
        if (projects.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalCost = projects.stream()
                .map(Project::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCost.divide(new BigDecimal(projects.size()), RoundingMode.HALF_UP);
    }

    private Double calculateAverageApprovalTime() {
        // Implementation for average approval time calculation
        return 2.5; // days - Placeholder
    }

    private Double calculateAveragePaymentTime() {
        // Implementation for average payment processing time
        return 3.2; // days - Placeholder
    }

    private BigDecimal calculateDocumentEfficiency() {
        // Implementation for document processing efficiency
        return new BigDecimal("92.3"); // Placeholder
    }

    private BigDecimal calculateBudgetAccuracy() {
        // Implementation for budget accuracy calculation
        return new BigDecimal("87.8"); // Placeholder
    }

    private BigDecimal calculateForecastAccuracy() {
        // Implementation for forecast accuracy calculation
        return new BigDecimal("84.5"); // Placeholder
    }

    private BigDecimal calculateComplianceScore() {
        // Implementation for compliance score calculation
        return new BigDecimal("96.2"); // Placeholder
    }

    // Performance metrics methods
    private Map<String, Object> getFinancialPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("budgetUtilization", calculateOverallUtilizationRate());
        metrics.put("costEfficiency", calculateCostEfficiencyRatio());
        metrics.put("budgetAccuracy", calculateBudgetAccuracy());
        return metrics;
    }

    private Map<String, Object> getOperationalPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("approvalTime", calculateAverageApprovalTime());
        metrics.put("paymentTime", calculateAveragePaymentTime());
        metrics.put("documentEfficiency", calculateDocumentEfficiency());
        return metrics;
    }

    private Map<String, Object> getProjectPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("averageProjectDuration", calculateAverageProjectDuration());
        metrics.put("projectSuccessRate", new BigDecimal("89.5")); // Placeholder
        metrics.put("resourceUtilization", new BigDecimal("78.3")); // Placeholder
        return metrics;
    }

    private String exportToExcel(FinancialReportResponse report) {
        // Minimal stub to satisfy compiler; implement as needed
        String fileName = "financial_report_" + System.currentTimeMillis() + ".xlsx";
        return fileName;
    }

    private String exportToPDF(FinancialReportResponse report) {
        // Minimal stub to satisfy compiler; implement as needed
        String fileName = "financial_report_" + System.currentTimeMillis() + ".pdf";
        return fileName;
    }
}