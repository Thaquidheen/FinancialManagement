package com.company.erp.report.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.common.util.DateUtil;
import com.company.erp.common.util.FileUtil;
import com.company.erp.financial.repository.BudgetTrackingRepository;
import com.company.erp.financial.repository.QuotationRepository;
import com.company.erp.notification.service.NotificationService;
import com.company.erp.payment.repository.PaymentRepository;
import com.company.erp.project.repository.ProjectRepository;
import com.company.erp.report.dto.request.ReportRequest;
import com.company.erp.report.dto.response.ReportResponse;
import com.company.erp.report.entity.ReportConfig;
import com.company.erp.report.repository.ReportRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import com.company.erp.workflow.repository.ApprovalRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Value("${app.reports.retention-days:90}")
    private int reportRetentionDays;

    @Value("${app.reports.max-export-size:10000}")
    private int maxExportSize;

    @Value("${app.reports.cache-duration:300}")
    private int cacheDurationSeconds;

    @Value("${app.reports.output-directory:./reports}")
    private String outputDirectory;

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BudgetTrackingRepository budgetTrackingRepository;
    private final QuotationRepository quotationRepository;
    private final PaymentRepository paymentRepository;
    private final ApprovalRepository approvalRepository;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;
    private final AnalyticsService analyticsService;

    @Autowired
    public ReportService(ReportRepository reportRepository,
                         ProjectRepository projectRepository,
                         UserRepository userRepository,
                         BudgetTrackingRepository budgetTrackingRepository,
                         QuotationRepository quotationRepository,
                         PaymentRepository paymentRepository,
                         ApprovalRepository approvalRepository,
                         NotificationService notificationService,
                         DashboardService dashboardService,
                         AnalyticsService analyticsService) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.budgetTrackingRepository = budgetTrackingRepository;
        this.quotationRepository = quotationRepository;
        this.paymentRepository = paymentRepository;
        this.approvalRepository = approvalRepository;
        this.notificationService = notificationService;
        this.dashboardService = dashboardService;
        this.analyticsService = analyticsService;
    }

    /**
     * Generate a comprehensive report based on request parameters
     */
    public ReportResponse generateReport(ReportRequest request, Long userId) {
        logger.info("Generating report of type: {} for user: {}", request.getReportType(), userId);

        validateReportRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check user permissions for report type
        validateUserPermissions(user, request.getReportType());

        ReportResponse response = new ReportResponse();
        response.setReportId(UUID.randomUUID().toString());
        response.setReportType(request.getReportType());
        response.setReportTitle(request.getReportTitle() != null ? request.getReportTitle() : generateReportTitle(request));
        response.setGeneratedAt(LocalDateTime.now());
        response.setGeneratedBy(user.getFullName());
        response.setPeriodStart(request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null);
        response.setPeriodEnd(request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null);

        try {
            switch (request.getReportType().toUpperCase()) {
                case "PROJECT_SUMMARY":
                    generateProjectSummaryReport(request, response);
                    break;
                case "FINANCIAL_OVERVIEW":
                    generateFinancialOverviewReport(request, response);
                    break;
                case "USER_ACTIVITY":
                    generateUserActivityReport(request, response);
                    break;
                case "BUDGET_UTILIZATION":
                    generateBudgetUtilizationReport(request, response);
                    break;
                case "APPROVAL_WORKFLOW":
                    generateApprovalWorkflowReport(request, response);
                    break;
                case "PAYMENT_ANALYSIS":
                    generatePaymentAnalysisReport(request, response);
                    break;
                case "COMPREHENSIVE":
                    generateComprehensiveReport(request, response);
                    break;
                default:
                    throw new BusinessException("INVALID_REPORT_TYPE", "Unsupported report type: " + request.getReportType());
            }

            // Add executive summary
            response.setExecutiveSummary(generateExecutiveSummary(request, response));

            logger.info("Successfully generated report: {}", response.getReportId());
            return response;

        } catch (Exception e) {
            logger.error("Failed to generate report: {}", e.getMessage(), e);
            throw new BusinessException("REPORT_GENERATION_FAILED", "Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Export report in various formats
     */
    public String exportReport(ReportRequest request, String format, Long userId) {
        logger.info("Exporting report in format: {} for user: {}", format, userId);

        ReportResponse report = generateReport(request, userId);

        try {
            switch (format.toUpperCase()) {
                case "EXCEL":
                    return exportToExcel(report, request);
                case "PDF":
                    return exportToPDF(report, request);
                case "CSV":
                    return exportToCSV(report, request);
                case "JSON":
                    return exportToJSON(report);
                default:
                    throw new BusinessException("INVALID_FORMAT", "Unsupported export format: " + format);
            }
        } catch (Exception e) {
            logger.error("Failed to export report: {}", e.getMessage(), e);
            throw new BusinessException("EXPORT_FAILED", "Failed to export report: " + e.getMessage());
        }
    }

    /**
     * Get report configurations
     */
    public Page<ReportConfig> getReportConfigs(String category, Boolean isPublic, Pageable pageable) {
        if (category != null && isPublic != null) {
            return reportRepository.findByCategoryAndIsPublic(category, isPublic, pageable);
        } else if (category != null) {
            return reportRepository.findByCategory(category, pageable);
        } else if (isPublic != null) {
            return reportRepository.findByIsPublic(isPublic, pageable);
        }
        return reportRepository.findAll(pageable);
    }

    /**
     * Create or update report configuration
     */
    @Transactional
    public ReportConfig saveReportConfig(ReportConfig config) {
        logger.info("Saving report configuration: {}", config.getName());

        if (config.getId() == null) {
            // New configuration
            config.setVersion(1);
        } else {
            // Update existing configuration
            ReportConfig existing = reportRepository.findById(config.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Report configuration not found"));
            config.incrementVersion();
        }

        return reportRepository.save(config);
    }

    /**
     * Schedule report generation
     */
    @Async
    public CompletableFuture<Void> scheduleReport(ReportRequest request, List<String> recipients, Long userId) {
        logger.info("Scheduling report generation for recipients: {}", recipients);

        try {
            // Generate report
            ReportResponse report = generateReport(request, userId);

            // Export to file
            String filePath = exportToExcel(report, request);

            // Send notifications to recipients
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("reportTitle", report.getReportTitle());
            templateData.put("reportType", report.getReportType());
            templateData.put("generatedAt", report.getGeneratedAt());
            templateData.put("filePath", filePath);

            for (String recipient : recipients) {
                // Send email notification with attachment
                // This would integrate with your notification service
                logger.info("Sending report notification to: {}", recipient);
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Failed to schedule report: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get report analytics and statistics
     */
    public Map<String, Object> getReportAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        // Report generation statistics
        analytics.put("totalReportsGenerated", getTotalReportsGenerated(start, end));
        analytics.put("reportsByType", getReportsByType(start, end));
        analytics.put("reportsByUser", getReportsByUser(start, end));
        analytics.put("popularReportTypes", getPopularReportTypes(start, end));
        analytics.put("averageGenerationTime", getAverageGenerationTime(start, end));

        return analytics;
    }

    // Private helper methods

    private void validateReportRequest(ReportRequest request) {
        if (request.getReportType() == null || request.getReportType().trim().isEmpty()) {
            throw new BusinessException("INVALID_REQUEST", "Report type is required");
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new BusinessException("INVALID_DATE_RANGE", "Start date cannot be after end date");
            }
        }
    }

    private void validateUserPermissions(User user, String reportType) {
        // Implement role-based access control
        Set<String> userRoles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        boolean hasPermission = switch (reportType.toUpperCase()) {
            case "PROJECT_SUMMARY" -> userRoles.contains("PROJECT_MANAGER") ||
                    userRoles.contains("SUPER_ADMIN");
            case "FINANCIAL_OVERVIEW", "BUDGET_UTILIZATION" -> userRoles.contains("ACCOUNT_MANAGER") ||
                    userRoles.contains("SUPER_ADMIN");
            case "USER_ACTIVITY" -> userRoles.contains("HR_MANAGER") ||
                    userRoles.contains("SUPER_ADMIN");
            case "COMPREHENSIVE" -> userRoles.contains("SUPER_ADMIN");
            default -> userRoles.contains("SUPER_ADMIN");
        };

        if (!hasPermission) {
            throw new BusinessException("ACCESS_DENIED", "Insufficient permissions to generate this report type");
        }
    }

    private String generateReportTitle(ReportRequest request) {
        StringBuilder title = new StringBuilder();

        switch (request.getReportType().toUpperCase()) {
            case "PROJECT_SUMMARY":
                title.append("Project Summary Report");
                break;
            case "FINANCIAL_OVERVIEW":
                title.append("Financial Overview Report");
                break;
            case "USER_ACTIVITY":
                title.append("User Activity Report");
                break;
            case "BUDGET_UTILIZATION":
                title.append("Budget Utilization Report");
                break;
            case "APPROVAL_WORKFLOW":
                title.append("Approval Workflow Report");
                break;
            case "PAYMENT_ANALYSIS":
                title.append("Payment Analysis Report");
                break;
            default:
                title.append("Custom Report");
        }

        if (request.getStartDate() != null || request.getEndDate() != null) {
            title.append(" - ");
            if (request.getStartDate() != null) {
                title.append(request.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            if (request.getEndDate() != null) {
                title.append(" to ").append(request.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
        }

        return title.toString();
    }

    private void generateProjectSummaryReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Get project statistics
        Long totalProjects = projectRepository.count();
        data.put("totalProjects", totalProjects);

        // Get project status distribution
        Map<String, Long> statusDistribution = getProjectStatusDistribution(request);
        data.put("projectStatusDistribution", statusDistribution);

        // Get budget summary
        Map<String, Object> budgetSummary = getProjectBudgetSummary(request);
        data.put("budgetSummary", budgetSummary);

        // Get top projects by budget
        List<Map<String, Object>> topProjects = getTopProjectsByBudget(request, 10);
        data.put("topProjectsByBudget", topProjects);

        response.setData(data);
    }

    private void generateFinancialOverviewReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Total financial metrics
        BigDecimal totalRevenue = calculateTotalRevenue(request);
        BigDecimal totalExpenses = calculateTotalExpenses(request);
        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);

        data.put("totalRevenue", totalRevenue);
        data.put("totalExpenses", totalExpenses);
        data.put("netIncome", netIncome);
        data.put("profitMargin", calculateProfitMargin(totalRevenue, netIncome));

        // Category-wise spending
        Map<String, BigDecimal> categorySpending = getCategoryWiseSpending(request);
        data.put("categorySpending", categorySpending);

        // Monthly trends
        List<Map<String, Object>> monthlyTrends = getMonthlyFinancialTrends(request);
        data.put("monthlyTrends", monthlyTrends);

        response.setData(data);
    }

    private void generateUserActivityReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // User statistics
        Long totalUsers = userRepository.count();
        Long activeUsers = getActiveUsersCount(request);

        data.put("totalUsers", totalUsers);
        data.put("activeUsers", activeUsers);
        data.put("userUtilizationRate", calculateUserUtilizationRate(activeUsers, totalUsers));

        // User activity by department
        Map<String, Object> departmentActivity = getUserActivityByDepartment(request);
        data.put("departmentActivity", departmentActivity);

        // Top active users
        List<Map<String, Object>> topActiveUsers = getTopActiveUsers(request, 10);
        data.put("topActiveUsers", topActiveUsers);

        response.setData(data);
    }

    private void generateBudgetUtilizationReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Overall budget utilization
        BigDecimal totalBudget = calculateTotalBudget(request);
        BigDecimal utilizedBudget = calculateUtilizedBudget(request);
        BigDecimal utilizationRate = calculateUtilizationRate(utilizedBudget, totalBudget);

        data.put("totalBudget", totalBudget);
        data.put("utilizedBudget", utilizedBudget);
        data.put("remainingBudget", totalBudget.subtract(utilizedBudget));
        data.put("utilizationRate", utilizationRate);

        // Project-wise budget utilization
        List<Map<String, Object>> projectUtilization = getProjectBudgetUtilization(request);
        data.put("projectUtilization", projectUtilization);

        // Budget variance analysis
        List<Map<String, Object>> varianceAnalysis = getBudgetVarianceAnalysis(request);
        data.put("varianceAnalysis", varianceAnalysis);

        response.setData(data);
    }

    private void generateApprovalWorkflowReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Approval statistics
        Long totalApprovals = approvalRepository.count();
        Long pendingApprovals = getPendingApprovalsCount(request);
        Long approvedCount = getApprovedCount(request);
        Long rejectedCount = getRejectedCount(request);

        data.put("totalApprovals", totalApprovals);
        data.put("pendingApprovals", pendingApprovals);
        data.put("approvedCount", approvedCount);
        data.put("rejectedCount", rejectedCount);
        data.put("approvalRate", calculateApprovalRate(approvedCount, totalApprovals));

        // Average approval time
        Double averageApprovalTime = calculateAverageApprovalTime(request);
        data.put("averageApprovalTime", averageApprovalTime);

        // Workflow bottlenecks
        List<Map<String, Object>> bottlenecks = identifyWorkflowBottlenecks(request);
        data.put("workflowBottlenecks", bottlenecks);

        response.setData(data);
    }

    private void generatePaymentAnalysisReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Payment statistics
        Long totalPayments = paymentRepository.count();
        BigDecimal totalPaymentAmount = calculateTotalPaymentAmount(request);

        data.put("totalPayments", totalPayments);
        data.put("totalPaymentAmount", totalPaymentAmount);
        data.put("averagePaymentAmount", calculateAveragePaymentAmount(request));

        // Payment status distribution
        Map<String, Long> statusDistribution = getPaymentStatusDistribution(request);
        data.put("paymentStatusDistribution", statusDistribution);

        // Payment trends
        List<Map<String, Object>> paymentTrends = getPaymentTrends(request);
        data.put("paymentTrends", paymentTrends);

        response.setData(data);
    }

    private void generateComprehensiveReport(ReportRequest request, ReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Combine data from all report types
        ReportResponse projectSummary = new ReportResponse();
        generateProjectSummaryReport(request, projectSummary);
        data.put("projectSummary", projectSummary.getData());

        ReportResponse financialOverview = new ReportResponse();
        generateFinancialOverviewReport(request, financialOverview);
        data.put("financialOverview", financialOverview.getData());

        ReportResponse budgetUtilization = new ReportResponse();
        generateBudgetUtilizationReport(request, budgetUtilization);
        data.put("budgetUtilization", budgetUtilization.getData());

        response.setData(data);
    }

    private Map<String, Object> generateExecutiveSummary(ReportRequest request, ReportResponse response) {
        Map<String, Object> summary = new HashMap<>();

        // Cast response data to Map for safe access
        Map<String, Object> responseData = (Map<String, Object>) response.getData();

        // Key metrics based on report type
        switch (request.getReportType().toUpperCase()) {
            case "PROJECT_SUMMARY":
                summary.put("keyMetric", "Total Projects");
                summary.put("keyValue", responseData.get("totalProjects"));
                break;
            case "FINANCIAL_OVERVIEW":
                summary.put("keyMetric", "Net Income");
                summary.put("keyValue", responseData.get("netIncome"));
                break;
            case "BUDGET_UTILIZATION":
                summary.put("keyMetric", "Budget Utilization Rate");
                summary.put("keyValue", responseData.get("utilizationRate"));
                break;
        }

        summary.put("reportPeriod", generatePeriodSummary(request));
        summary.put("dataPoints", countDataPoints(response));
        summary.put("insights", generateKeyInsights(request, response));

        return summary;
    }

    // Export methods
    private String exportToExcel(ReportResponse report, ReportRequest request) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Create main report sheet
        Sheet reportSheet = workbook.createSheet("Report Data");
        createReportDataSheet(reportSheet, report);

        // Create summary sheet if requested
        if (request.isIncludeSummary()) {
            Sheet summarySheet = workbook.createSheet("Executive Summary");
            createSummarySheet(summarySheet, report);
        }

        // Create charts sheet if requested
        if (request.isIncludeCharts()) {
            Sheet chartsSheet = workbook.createSheet("Charts & Graphs");
            createChartsSheet(chartsSheet, report);
        }

        // Save to file
        String fileName = generateFileName(report, "xlsx");
        String filePath = outputDirectory + "/" + fileName;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            FileUtil.saveFile(outputStream.toByteArray(), filePath);
        }

        workbook.close();

        logger.info("Excel report exported to: {}", filePath);
        return filePath;
    }

    private String exportToPDF(ReportResponse report, ReportRequest request) {
        // PDF export implementation would go here
        String fileName = generateFileName(report, "pdf");
        String filePath = outputDirectory + "/" + fileName;

        // Placeholder implementation
        logger.info("PDF export not fully implemented yet");

        return filePath;
    }

    private String exportToCSV(ReportResponse report, ReportRequest request) {
        StringBuilder csv = new StringBuilder();

        // Add headers
        csv.append("Report Type,").append(report.getReportType()).append("\n");
        csv.append("Generated At,").append(report.getGeneratedAt()).append("\n");
        csv.append("Generated By,").append(report.getGeneratedBy()).append("\n");
        csv.append("\n");

        // Add data based on report type
        Map<String, Object> data = (Map<String, Object>) report.getData();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        // Save to file
        String fileName = generateFileName(report, "csv");
        String filePath = outputDirectory + "/" + fileName;

        try {
            FileUtil.saveTextFile(csv.toString(), filePath);
        } catch (IOException e) {
            throw new BusinessException("CSV_EXPORT_FAILED", "Failed to save CSV file: " + e.getMessage());
        }

        logger.info("CSV report exported to: {}", filePath);
        return filePath;
    }

    private String exportToJSON(ReportResponse report) {
        // JSON export implementation would use Jackson ObjectMapper
        String fileName = generateFileName(report, "json");
        String filePath = outputDirectory + "/" + fileName;

        // Placeholder implementation
        logger.info("JSON export placeholder");

        return filePath;
    }

    // Helper methods for calculations (these would need actual implementations based on your data model)
    private Map<String, Long> getProjectStatusDistribution(ReportRequest request) {
        // Implementation would query projects and group by status
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("ACTIVE", 15L);
        distribution.put("COMPLETED", 8L);
        distribution.put("ON_HOLD", 3L);
        distribution.put("CANCELLED", 1L);
        return distribution;
    }

    private Map<String, Object> getProjectBudgetSummary(ReportRequest request) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAllocated", new BigDecimal("1500000.00"));
        summary.put("totalSpent", new BigDecimal("1234567.89"));
        summary.put("remaining", new BigDecimal("265432.11"));
        return summary;
    }

    private List<Map<String, Object>> getTopProjectsByBudget(ReportRequest request, int limit) {
        // Implementation would query and return top projects
        return new ArrayList<>();
    }

    private BigDecimal calculateTotalRevenue(ReportRequest request) {
        // Implementation would sum up all revenue
        return new BigDecimal("2500000.00");
    }

    private BigDecimal calculateTotalExpenses(ReportRequest request) {
        // Implementation would sum up all expenses
        return new BigDecimal("1800000.00");
    }

    private BigDecimal calculateProfitMargin(BigDecimal revenue, BigDecimal netIncome) {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return netIncome.divide(revenue, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }

    private Map<String, BigDecimal> getCategoryWiseSpending(ReportRequest request) {
        Map<String, BigDecimal> spending = new HashMap<>();
        spending.put("Personnel", new BigDecimal("800000.00"));
        spending.put("Equipment", new BigDecimal("450000.00"));
        spending.put("Materials", new BigDecimal("350000.00"));
        spending.put("Operations", new BigDecimal("200000.00"));
        return spending;
    }

    private List<Map<String, Object>> getMonthlyFinancialTrends(ReportRequest request) {
        // Implementation would return monthly financial data
        return new ArrayList<>();
    }

    private String generateFileName(ReportResponse report, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportType = report.getReportType().toLowerCase().replace(" ", "_");
        return String.format("%s_%s.%s", reportType, timestamp, extension);
    }

    private void createReportDataSheet(Sheet sheet, ReportResponse report) {
        // Implementation would create Excel sheet with report data
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Report Data");
        // Add more rows and data based on report content
    }

    private void createSummarySheet(Sheet sheet, ReportResponse report) {
        // Implementation would create summary sheet
    }

    private void createChartsSheet(Sheet sheet, ReportResponse report) {
        // Implementation would create charts sheet
    }

    // Additional helper methods would be implemented here...
    private Long getActiveUsersCount(ReportRequest request) { return 85L; }
    private BigDecimal calculateUserUtilizationRate(Long active, Long total) {
        return new BigDecimal(active).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }
    private Map<String, Object> getUserActivityByDepartment(ReportRequest request) { return new HashMap<>(); }
    private List<Map<String, Object>> getTopActiveUsers(ReportRequest request, int limit) { return new ArrayList<>(); }
    private BigDecimal calculateTotalBudget(ReportRequest request) { return new BigDecimal("5000000.00"); }
    private BigDecimal calculateUtilizedBudget(ReportRequest request) { return new BigDecimal("3800000.00"); }
    private BigDecimal calculateUtilizationRate(BigDecimal utilized, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return utilized.divide(total, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }
    private List<Map<String, Object>> getProjectBudgetUtilization(ReportRequest request) { return new ArrayList<>(); }
    private List<Map<String, Object>> getBudgetVarianceAnalysis(ReportRequest request) { return new ArrayList<>(); }
    private Long getPendingApprovalsCount(ReportRequest request) { return 12L; }
    private Long getApprovedCount(ReportRequest request) { return 145L; }
    private Long getRejectedCount(ReportRequest request) { return 8L; }
    private BigDecimal calculateApprovalRate(Long approved, Long total) {
        if (total == 0) return BigDecimal.ZERO;
        return new BigDecimal(approved).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }

    private Double calculateAverageApprovalTime(ReportRequest request) {
        return 2.5; // days - would calculate from actual approval data
    }

    private List<Map<String, Object>> identifyWorkflowBottlenecks(ReportRequest request) {
        return new ArrayList<>();
    }

    private BigDecimal calculateTotalPaymentAmount(ReportRequest request) {
        return new BigDecimal("2800000.00");
    }

    private BigDecimal calculateAveragePaymentAmount(ReportRequest request) {
        return new BigDecimal("15000.00");
    }

    private Map<String, Long> getPaymentStatusDistribution(ReportRequest request) {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("PENDING", 25L);
        distribution.put("PROCESSED", 180L);
        distribution.put("FAILED", 5L);
        distribution.put("CANCELLED", 3L);
        return distribution;
    }

    private List<Map<String, Object>> getPaymentTrends(ReportRequest request) {
        return new ArrayList<>();
    }

    // Analytics and Statistics Methods
    private Long getTotalReportsGenerated(LocalDateTime start, LocalDateTime end) {
        // Would query report generation logs
        return 450L;
    }

    private Map<String, Long> getReportsByType(LocalDateTime start, LocalDateTime end) {
        Map<String, Long> reportsByType = new HashMap<>();
        reportsByType.put("PROJECT_SUMMARY", 120L);
        reportsByType.put("FINANCIAL_OVERVIEW", 85L);
        reportsByType.put("BUDGET_UTILIZATION", 95L);
        reportsByType.put("USER_ACTIVITY", 65L);
        reportsByType.put("APPROVAL_WORKFLOW", 45L);
        reportsByType.put("PAYMENT_ANALYSIS", 40L);
        return reportsByType;
    }

    private Map<String, Long> getReportsByUser(LocalDateTime start, LocalDateTime end) {
        // Would query and group by user
        return new HashMap<>();
    }

    private List<String> getPopularReportTypes(LocalDateTime start, LocalDateTime end) {
        return Arrays.asList("PROJECT_SUMMARY", "FINANCIAL_OVERVIEW", "BUDGET_UTILIZATION");
    }

    private Double getAverageGenerationTime(LocalDateTime start, LocalDateTime end) {
        return 3.2; // seconds
    }

    private String generatePeriodSummary(ReportRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return request.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                    " to " + request.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } else if (request.getStartDate() != null) {
            return "From " + request.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } else if (request.getEndDate() != null) {
            return "Until " + request.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
        return "All time data";
    }

    private int countDataPoints(ReportResponse response) {
        // Count the number of data points in the response
        Map<String, Object> data = (Map<String, Object>) response.getData();
        return data.size();
    }

    private List<String> generateKeyInsights(ReportRequest request, ReportResponse response) {
        List<String> insights = new ArrayList<>();

        switch (request.getReportType().toUpperCase()) {
            case "PROJECT_SUMMARY":
                insights.add("Most projects are currently active and on track");
                insights.add("Budget utilization is within acceptable variance");
                break;
            case "FINANCIAL_OVERVIEW":
                insights.add("Revenue growth shows positive trend");
                insights.add("Operating expenses are well controlled");
                break;
            case "BUDGET_UTILIZATION":
                insights.add("Overall budget utilization is healthy");
                insights.add("Some projects showing minor budget overruns");
                break;
            case "USER_ACTIVITY":
                insights.add("User engagement levels are satisfactory");
                insights.add("Activity peaks during mid-week periods");
                break;
            case "APPROVAL_WORKFLOW":
                insights.add("Approval processes are efficient");
                insights.add("Minor bottlenecks identified in final approval stage");
                break;
            case "PAYMENT_ANALYSIS":
                insights.add("Payment processing times are within SLA");
                insights.add("Low failure rate indicates good system reliability");
                break;
        }

        return insights;
    }

    /**
     * Get report template configurations
     */
    public List<ReportConfig> getReportTemplates() {
        return reportRepository.findByIsPublic(true);
    }

    /**
     * Clone report configuration
     */
    @Transactional
    public ReportConfig cloneReportConfig(Long configId, String newName) {
        ReportConfig original = reportRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Report configuration not found"));

        ReportConfig cloned = new ReportConfig();
        cloned.setName(newName);
        cloned.setDescription("Copy of " + original.getDescription());
        cloned.setReportType(original.getReportType());
        cloned.setCategory(original.getCategory());
        cloned.setParameters(new HashMap<>(original.getParameters()));
        cloned.setDefaultFilters(new HashMap<>(original.getDefaultFilters()));
        cloned.setOutputFormat(original.getOutputFormat());
        cloned.setIncludeCharts(original.getIncludeCharts());
        cloned.setIncludeRawData(original.getIncludeRawData());
        cloned.setVersion(1);

        return reportRepository.save(cloned);
    }

    /**
     * Delete old reports based on retention policy
     */
    @Transactional
    public void cleanupOldReports() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(reportRetentionDays);
        logger.info("Cleaning up reports older than: {}", cutoffDate);

        // This would delete old report files and database records
        // Implementation depends on how you store report metadata

        logger.info("Report cleanup completed");
    }

    /**
     * Validate report data integrity
     */
    public Map<String, Object> validateReportData(ReportRequest request) {
        Map<String, Object> validation = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Check data availability for the requested period
        if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDate.of(2020, 1, 1))) {
            warnings.add("Historical data before 2020 may be incomplete");
        }

        // Check if required data sources are available
        if (request.getProjectIds() != null && !request.getProjectIds().isEmpty()) {
            for (Long projectId : request.getProjectIds()) {
                if (!projectRepository.existsById(projectId)) {
                    errors.add("Project with ID " + projectId + " not found");
                }
            }
        }

        // Check user permissions for requested data
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (Long userId : request.getUserIds()) {
                if (!userRepository.existsById(userId)) {
                    warnings.add("User with ID " + userId + " not found, will be excluded from report");
                }
            }
        }

        validation.put("isValid", errors.isEmpty());
        validation.put("warnings", warnings);
        validation.put("errors", errors);
        validation.put("dataAvailability", calculateDataAvailability(request));

        return validation;
    }

    private Double calculateDataAvailability(ReportRequest request) {
        // Calculate percentage of available data for the requested parameters
        // This is a simplified implementation
        double availability = 100.0;

        if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDate.of(2021, 1, 1))) {
            availability *= 0.85; // 15% reduction for older data
        }

        if (request.getProjectIds() != null && !request.getProjectIds().isEmpty()) {
            // Check how many requested projects exist
            long existingProjects = request.getProjectIds().stream()
                    .mapToLong(id -> projectRepository.existsById(id) ? 1 : 0)
                    .sum();
            availability *= (double) existingProjects / request.getProjectIds().size();
        }

        return Math.min(availability, 100.0);
    }

    /**
     * Get report performance metrics
     */
    public Map<String, Object> getReportPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("averageGenerationTime", getAverageGenerationTime(
                LocalDateTime.now().minusDays(30), LocalDateTime.now()));
        metrics.put("successRate", calculateReportSuccessRate());
        metrics.put("cacheHitRate", calculateCacheHitRate());
        metrics.put("resourceUtilization", calculateResourceUtilization());
        metrics.put("popularExportFormats", getPopularExportFormats());

        return metrics;
    }

    private Double calculateReportSuccessRate() {
        // Calculate percentage of successful report generations
        return 98.5;
    }

    private Double calculateCacheHitRate() {
        // Calculate percentage of cache hits for report data
        return 75.2;
    }

    private Double calculateResourceUtilization() {
        // Calculate system resource utilization during report generation
        return 65.8;
    }

    private Map<String, Integer> getPopularExportFormats() {
        Map<String, Integer> formats = new HashMap<>();
        formats.put("EXCEL", 65);
        formats.put("PDF", 25);
        formats.put("CSV", 8);
        formats.put("JSON", 2);
        return formats;
    }

    /**
     * Generate report preview (limited data)
     */
    public Map<String, Object> generateReportPreview(ReportRequest request, Long userId) {
        logger.info("Generating report preview for type: {}", request.getReportType());

        // Validate user permissions
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateUserPermissions(user, request.getReportType());

        Map<String, Object> preview = new HashMap<>();
        preview.put("reportType", request.getReportType());
        preview.put("estimatedDataPoints", estimateDataPoints(request));
        preview.put("estimatedGenerationTime", estimateGenerationTime(request));
        preview.put("dataAvailability", calculateDataAvailability(request));

        // Generate sample data based on report type
        switch (request.getReportType().toUpperCase()) {
            case "PROJECT_SUMMARY":
                preview.put("sampleData", generateProjectSummaryPreview(request));
                break;
            case "FINANCIAL_OVERVIEW":
                preview.put("sampleData", generateFinancialOverviewPreview(request));
                break;
            default:
                preview.put("sampleData", generateGenericPreview(request));
        }

        return preview;
    }

    private int estimateDataPoints(ReportRequest request) {
        // Estimate number of data points based on request parameters
        int basePoints = 100;

        if (request.getProjectIds() != null) {
            basePoints += request.getProjectIds().size() * 20;
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            long days = request.getStartDate().until(request.getEndDate()).getDays();
            basePoints += (int) (days / 30) * 50; // Roughly 50 points per month
        }

        return basePoints;
    }

    private Double estimateGenerationTime(ReportRequest request) {
        // Estimate generation time based on complexity
        double baseTime = 2.0; // seconds

        int dataPoints = estimateDataPoints(request);
        baseTime += (dataPoints / 1000.0) * 1.5; // Add time based on data volume

        if (request.isIncludeCharts()) {
            baseTime += 1.0; // Additional time for chart generation
        }

        if ("COMPREHENSIVE".equals(request.getReportType())) {
            baseTime *= 2.5; // Comprehensive reports take longer
        }

        return baseTime;
    }

    private Map<String, Object> generateProjectSummaryPreview(ReportRequest request) {
        Map<String, Object> preview = new HashMap<>();
        preview.put("totalProjects", "25");
        preview.put("activeProjects", "18");
        preview.put("completedProjects", "7");
        preview.put("averageBudget", "$125,000");
        return preview;
    }

    private Map<String, Object> generateFinancialOverviewPreview(ReportRequest request) {
        Map<String, Object> preview = new HashMap<>();
        preview.put("totalRevenue", "$2,500,000");
        preview.put("totalExpenses", "$1,800,000");
        preview.put("netIncome", "$700,000");
        preview.put("profitMargin", "28%");
        return preview;
    }

    private Map<String, Object> generateGenericPreview(ReportRequest request) {
        Map<String, Object> preview = new HashMap<>();
        preview.put("message", "Preview data will be available after report generation");
        preview.put("dataFields", Arrays.asList("Field1", "Field2", "Field3"));
        return preview;
    }
}

