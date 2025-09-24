package com.company.erp.workflow.controller;

import com.company.erp.workflow.dto.request.ApprovalRequest;
import com.company.erp.workflow.dto.request.BulkApprovalRequest;
import com.company.erp.workflow.dto.response.ApprovalResponse;
import com.company.erp.workflow.dto.response.PendingApprovalsResponse;
import com.company.erp.workflow.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/approvals")
@Tag(name = "Approval Management", description = "Workflow approval operations for quotations")
@Validated
public class ApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);

    @Autowired
    private ApprovalService approvalService;

    @Operation(summary = "Process single approval", description = "Approve, reject, or request changes for a quotation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid approval request or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only"),
            @ApiResponse(responseCode = "404", description = "Quotation not found")
    })
    @PostMapping("/process")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApprovalResponse> processApproval(@Valid @RequestBody ApprovalRequest request) {
        logger.info("Processing approval for quotation: {} with action: {}",
                request.getQuotationId(), request.getAction());

        ApprovalResponse response = approvalService.processApproval(request);

        logger.info("Approval processed successfully for quotation: {} by user", request.getQuotationId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process bulk approvals", description = "Process multiple quotations with the same action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk approvals processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bulk approval request"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> processBulkApprovals(@Valid @RequestBody BulkApprovalRequest request) {
        logger.info("Processing bulk approval for {} quotations with action: {}",
                request.getQuotationIds().size(), request.getAction());

        List<ApprovalResponse> results = approvalService.processBulkApprovals(request);

        Map<String, Object> response = new HashMap<>();
        response.put("totalRequested", request.getQuotationIds().size());
        response.put("successfullyProcessed", results.size());
        response.put("results", results);

        logger.info("Bulk approval completed: {} out of {} quotations processed successfully",
                results.size(), request.getQuotationIds().size());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending approvals", description = "Get paginated list of quotations pending approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending approvals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<PendingApprovalsResponse>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.debug("Fetching pending approvals - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PendingApprovalsResponse> pendingApprovals = approvalService.getPendingApprovals(pageable);

        return ResponseEntity.ok(pendingApprovals);
    }

    @Operation(summary = "Get urgent approvals", description = "Get quotations pending approval for more than 3 days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Urgent approvals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @GetMapping("/urgent")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<PendingApprovalsResponse>> getUrgentApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.debug("Fetching urgent approvals - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PendingApprovalsResponse> urgentApprovals = approvalService.getUrgentApprovals(pageable);

        return ResponseEntity.ok(urgentApprovals);
    }

    @Operation(summary = "Get approval history", description = "Get approval history for a specific quotation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/quotation/{quotationId}/history")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN') or hasAuthority('PROJECT_MANAGER')")
    public ResponseEntity<List<ApprovalResponse>> getApprovalHistory(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId) {

        logger.debug("Fetching approval history for quotation: {}", quotationId);

        List<ApprovalResponse> approvalHistory = approvalService.getApprovalHistory(quotationId);

        return ResponseEntity.ok(approvalHistory);
    }

    @Operation(summary = "Get approval statistics", description = "Get approval statistics for current user or specified approver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApprovalService.ApprovalStatistics> getApprovalStatistics(
            @RequestParam(required = false) Long approverId) {

        logger.debug("Fetching approval statistics for approver: {}", approverId);

        ApprovalService.ApprovalStatistics statistics = approvalService.getApprovalStatistics(approverId);

        return ResponseEntity.ok(statistics);
    }

    // Convenience endpoints for specific actions

    @Operation(summary = "Quick approve", description = "Quick approve a quotation with optional comments")
    @PostMapping("/{quotationId}/approve")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApprovalResponse> quickApprove(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @RequestParam(required = false) String comments) {

        logger.info("Quick approving quotation: {}", quotationId);

        ApprovalRequest request = new ApprovalRequest(quotationId, "APPROVE", comments);
        ApprovalResponse response = approvalService.processApproval(request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Quick reject", description = "Quick reject a quotation with mandatory reason")
    @PostMapping("/{quotationId}/reject")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApprovalResponse> quickReject(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @RequestParam String reason) {

        logger.info("Quick rejecting quotation: {} with reason: {}", quotationId, reason);

        ApprovalRequest request = new ApprovalRequest(quotationId, "REJECT", reason);
        ApprovalResponse response = approvalService.processApproval(request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Request changes", description = "Request changes for a quotation with comments")
    @PostMapping("/{quotationId}/changes")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApprovalResponse> requestChanges(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @RequestParam String comments) {

        logger.info("Requesting changes for quotation: {}", quotationId);

        ApprovalRequest request = new ApprovalRequest(quotationId, "CHANGES_REQUESTED", comments);
        ApprovalResponse response = approvalService.processApproval(request);

        return ResponseEntity.ok(response);
    }

    // Dashboard endpoint for approval metrics
    @Operation(summary = "Get approval dashboard", description = "Get comprehensive approval dashboard data")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getApprovalDashboard() {
        logger.debug("Fetching approval dashboard data");

        ApprovalService.ApprovalStatistics stats = approvalService.getApprovalStatistics(null);

        // Get urgent approvals count
        Page<PendingApprovalsResponse> urgentApprovals = approvalService.getUrgentApprovals(
                PageRequest.of(0, 1));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("statistics", stats);
        dashboard.put("urgentApprovalsCount", urgentApprovals.getTotalElements());
        dashboard.put("totalPendingApprovals", stats.getPendingApprovals());

        return ResponseEntity.ok(dashboard);
    }
}