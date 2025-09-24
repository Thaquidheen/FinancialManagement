package com.company.erp.financial.controller;

import com.company.erp.common.dto.ApiResponse;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.financial.dto.request.BudgetTrackingRequest;
import com.company.erp.financial.dto.response.BudgetAlertResponse;
import com.company.erp.financial.dto.response.BudgetStatusResponse;
import com.company.erp.financial.dto.response.BudgetTrackingResponse;
import com.company.erp.financial.dto.response.BudgetVarianceResponse;
import com.company.erp.financial.entity.BudgetTrackingType;
import com.company.erp.financial.service.BudgetTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/financial/budget-tracking")
@Tag(name = "Budget Tracking", description = "Budget tracking and financial control APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class BudgetTrackingController {

    private final BudgetTrackingService budgetTrackingService;

    public BudgetTrackingController(BudgetTrackingService budgetTrackingService) {
        this.budgetTrackingService = budgetTrackingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    @Operation(summary = "Create budget tracking entry", description = "Create a new budget tracking entry")
    public ResponseEntity<ApiResponse<BudgetTrackingResponse>> createBudgetTracking(
            @Valid @RequestBody BudgetTrackingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        BudgetTrackingResponse response = budgetTrackingService.createBudgetTracking(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget tracking entry created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Get budget tracking by ID", description = "Retrieve a specific budget tracking entry")
    public ResponseEntity<ApiResponse<BudgetTrackingResponse>> getBudgetTrackingById(@PathVariable Long id) {
        BudgetTrackingResponse response = budgetTrackingService.getBudgetTrackingById(id);
        return ResponseEntity.ok(ApiResponse.success("Budget tracking retrieved successfully", response));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER') or " +
            "(hasRole('PROJECT_MANAGER') and @projectService.isProjectManager(#projectId, authentication.principal.id))")
    @Operation(summary = "Get project budget tracking", description = "Retrieve budget tracking for a specific project")
    public ResponseEntity<ApiResponse<Page<BudgetTrackingResponse>>> getProjectBudgetTracking(
            @PathVariable Long projectId,
            @RequestParam(required = false) BudgetTrackingType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        Page<BudgetTrackingResponse> response = budgetTrackingService.getProjectBudgetTracking(
                projectId, type, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Project budget tracking retrieved successfully", response));
    }

    @GetMapping("/status/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER') or " +
            "(hasRole('PROJECT_MANAGER') and @projectService.isProjectManager(#projectId, authentication.principal.id))")
    @Operation(summary = "Get project budget status", description = "Get current budget status and utilization for a project")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> getProjectBudgetStatus(@PathVariable Long projectId) {
        BudgetStatusResponse response = budgetTrackingService.getProjectBudgetStatus(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project budget status retrieved successfully", response));
    }

    @GetMapping("/variance/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER') or " +
            "(hasRole('PROJECT_MANAGER') and @projectService.isProjectManager(#projectId, authentication.principal.id))")
    @Operation(summary = "Get budget variance analysis", description = "Get detailed budget variance analysis for a project")
    public ResponseEntity<ApiResponse<BudgetVarianceResponse>> getBudgetVarianceAnalysis(@PathVariable Long projectId) {
        BudgetVarianceResponse response = budgetTrackingService.getBudgetVarianceAnalysis(projectId);
        return ResponseEntity.ok(ApiResponse.success("Budget variance analysis retrieved successfully", response));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    @Operation(summary = "Get budget alerts", description = "Retrieve active budget alerts and warnings")
    public ResponseEntity<ApiResponse<List<BudgetAlertResponse>>> getBudgetAlerts(
            @RequestParam(required = false, defaultValue = "80") BigDecimal warningThreshold,
            @RequestParam(required = false, defaultValue = "100") BigDecimal criticalThreshold) {

        List<BudgetAlertResponse> response = budgetTrackingService.getBudgetAlerts(warningThreshold, criticalThreshold);
        return ResponseEntity.ok(ApiResponse.success("Budget alerts retrieved successfully", response));
    }

    @GetMapping("/alerts/project/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER') or " +
            "(hasRole('PROJECT_MANAGER') and @projectService.isProjectManager(#projectId, authentication.principal.id))")
    @Operation(summary = "Get project budget alerts", description = "Get budget alerts for a specific project")
    public ResponseEntity<ApiResponse<List<BudgetAlertResponse>>> getProjectBudgetAlerts(
            @PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "80") BigDecimal warningThreshold) {

        List<BudgetAlertResponse> response = budgetTrackingService.getProjectBudgetAlerts(projectId, warningThreshold);
        return ResponseEntity.ok(ApiResponse.success("Project budget alerts retrieved successfully", response));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    @Operation(summary = "Get budget summary", description = "Get overall budget summary across all projects")
    public ResponseEntity<ApiResponse<Object>> getBudgetSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Object response = budgetTrackingService.getBudgetSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Budget summary retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    @Operation(summary = "Update budget tracking", description = "Update an existing budget tracking entry")
    public ResponseEntity<ApiResponse<BudgetTrackingResponse>> updateBudgetTracking(
            @PathVariable Long id,
            @Valid @RequestBody BudgetTrackingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        BudgetTrackingResponse response = budgetTrackingService.updateBudgetTracking(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Budget tracking updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete budget tracking", description = "Delete a budget tracking entry")
    public ResponseEntity<ApiResponse<Void>> deleteBudgetTracking(@PathVariable Long id) {
        budgetTrackingService.deleteBudgetTracking(id);
        return ResponseEntity.ok(ApiResponse.success("Budget tracking deleted successfully", null));
    }

    @PostMapping("/recalculate/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    @Operation(summary = "Recalculate project budget", description = "Recalculate budget tracking for a project")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> recalculateProjectBudget(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        BudgetStatusResponse response = budgetTrackingService.recalculateProjectBudget(projectId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Project budget recalculated successfully", response));
    }

    @GetMapping("/export/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER') or " +
            "(hasRole('PROJECT_MANAGER') and @projectService.isProjectManager(#projectId, authentication.principal.id))")
    @Operation(summary = "Export budget tracking", description = "Export budget tracking data for a project")
    public ResponseEntity<ApiResponse<String>> exportBudgetTracking(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "EXCEL") String format) {

        String response = budgetTrackingService.exportBudgetTracking(projectId, startDate, endDate, format);
        return ResponseEntity.ok(ApiResponse.success("Budget tracking exported successfully", response));
    }
}