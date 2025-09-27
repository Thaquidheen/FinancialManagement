// Enhanced FinancialReportController.java
package com.company.erp.financial.controller;

import com.company.erp.common.dto.ApiResponse;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.dto.response.FinancialDashboardResponse;
import com.company.erp.financial.dto.response.FinancialReportResponse;
import com.company.erp.financial.service.FinancialReportService;
import com.company.erp.report.dto.request.ReportRequest;
import com.company.erp.report.service.DashboardService;
import com.company.erp.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/financial/reports")
@Tag(name = "Financial Reports", description = "Financial reporting and analytics APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FinancialReportController {

    private final FinancialReportService financialReportService;
    private final DashboardService dashboardService;
    private final ReportService reportService;

    public FinancialReportController(
            FinancialReportService financialReportService,
            DashboardService dashboardService,
            ReportService reportService) {
        this.financialReportService = financialReportService;
        this.dashboardService = dashboardService;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get financial dashboard", description = "Retrieve financial dashboard with key metrics")
    public ResponseEntity<ApiResponse<FinancialDashboardResponse>> getFinancialDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        FinancialDashboardResponse response = financialReportService.getFinancialDashboard(
                startDate, endDate, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Financial dashboard retrieved successfully", response));
    }

    @GetMapping("/dashboard/project-manager")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @Operation(summary = "Get project manager dashboard", description = "Retrieve dashboard for project manager's projects")
    public ResponseEntity<ApiResponse<FinancialDashboardResponse>> getProjectManagerDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        FinancialDashboardResponse response = financialReportService.getProjectManagerDashboard(
                startDate, endDate, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Project manager dashboard retrieved successfully", response));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Generate financial report", description = "Generate detailed financial report based on criteria")
    public ResponseEntity<ApiResponse<FinancialReportResponse>> generateFinancialReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        FinancialReportResponse response = financialReportService.generateFinancialReport(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Financial report generated successfully", response));
    }

    @GetMapping("/budget-utilization")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get budget utilization report", description = "Get budget utilization across all projects")
    public ResponseEntity<ApiResponse<Object>> getBudgetUtilizationReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String groupBy) {

        Object response = financialReportService.getBudgetUtilizationReport(startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.success("Budget utilization report retrieved successfully", response));
    }

    @GetMapping("/spending-trends")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get spending trends", description = "Get spending trends and patterns analysis")
    public ResponseEntity<ApiResponse<Object>> getSpendingTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") String period,
            @RequestParam(required = false) List<Long> projectIds) {

        Object response = financialReportService.getSpendingTrends(startDate, endDate, period, projectIds);
        return ResponseEntity.ok(ApiResponse.success("Spending trends retrieved successfully", response));
    }

    @GetMapping("/cost-analysis")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get cost analysis", description = "Get detailed cost analysis by categories and projects")
    public ResponseEntity<ApiResponse<Object>> getCostAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<Long> projectIds) {

        Object response = financialReportService.getCostAnalysis(startDate, endDate, categories, projectIds);
        return ResponseEntity.ok(ApiResponse.success("Cost analysis retrieved successfully", response));
    }

    @GetMapping("/variance-analysis")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get variance analysis", description = "Get budget vs actual variance analysis")
    public ResponseEntity<ApiResponse<Object>> getVarianceAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> projectIds) {

        Object response = financialReportService.getVarianceAnalysis(startDate, endDate, projectIds);
        return ResponseEntity.ok(ApiResponse.success("Variance analysis retrieved successfully", response));
    }

    @GetMapping("/forecasting")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get financial forecasting", description = "Get financial forecasts and predictions")
    public ResponseEntity<ApiResponse<Object>> getFinancialForecasting(
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(defaultValue = "6") int monthsAhead,
            @RequestParam(defaultValue = "LINEAR") String forecastMethod) {

        Object response = financialReportService.getFinancialForecasting(projectIds, monthsAhead, forecastMethod);
        return ResponseEntity.ok(ApiResponse.success("Financial forecasting retrieved successfully", response));
    }

    @GetMapping("/executive-summary")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @Operation(summary = "Get executive summary", description = "Get executive-level financial summary")
    public ResponseEntity<ApiResponse<Object>> getExecutiveSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Object response = financialReportService.getExecutiveSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Executive summary retrieved successfully", response));
    }

    @GetMapping("/audit-trail")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get audit trail", description = "Get detailed audit trail for financial transactions")
    public ResponseEntity<ApiResponse<Object>> getAuditTrail(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = false) List<String> actionTypes,
            @RequestParam(required = false) List<Long> userIds) {

        Object response = financialReportService.getAuditTrail(startDate, endDate, projectIds, actionTypes, userIds);
        return ResponseEntity.ok(ApiResponse.success("Audit trail retrieved successfully", response));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Export financial report", description = "Export financial report in various formats")
    public ResponseEntity<ApiResponse<String>> exportFinancialReport(
            @Valid @RequestBody ReportRequest request,
            @RequestParam(defaultValue = "EXCEL") String format,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        String response = financialReportService.exportFinancialReport(request, format, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Financial report exported successfully", response));
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get financial KPIs", description = "Get key performance indicators for financial metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFinancialKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> response = financialReportService.getFinancialKPIs(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Financial KPIs retrieved successfully", response));
    }

    @GetMapping("/performance-metrics")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Get performance metrics", description = "Get financial performance metrics")
    public ResponseEntity<ApiResponse<Object>> getPerformanceMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String metricType) {

        Object response = financialReportService.getPerformanceMetrics(startDate, endDate, metricType);
        return ResponseEntity.ok(ApiResponse.success("Performance metrics retrieved successfully", response));
    }
}