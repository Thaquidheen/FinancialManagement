package com.company.erp.financial.controller;

import com.company.erp.financial.dto.request.CreateQuotationRequest;
import com.company.erp.financial.dto.request.SubmitQuotationRequest;
import com.company.erp.financial.dto.response.QuotationResponse;
import com.company.erp.financial.dto.response.QuotationSummaryResponse;
import com.company.erp.financial.service.QuotationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quotations")
@Tag(name = "Quotation Management", description = "Quotation CRUD operations and workflow management endpoints")
@Validated
public class QuotationController {

    private static final Logger logger = LoggerFactory.getLogger(QuotationController.class);

    @Autowired
    private QuotationService quotationService;

    @Operation(summary = "Create new quotation", description = "Create a new quotation with line items for a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quotation created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - user not authorized for project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<QuotationResponse> createQuotation(@Valid @RequestBody CreateQuotationRequest request) {
        logger.info("Creating new quotation for project: {}", request.getProjectId());

        QuotationResponse quotationResponse = quotationService.createQuotation(request);

        logger.info("Quotation created successfully with ID: {} and total amount: {} {}",
                quotationResponse.getId(), quotationResponse.getTotalAmount(), quotationResponse.getCurrency());

        return ResponseEntity.status(HttpStatus.CREATED).body(quotationResponse);
    }

    @Operation(summary = "Get quotation by ID", description = "Retrieve detailed quotation information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotation found"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions")
    })
    @GetMapping("/{quotationId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or @quotationService.canUserAccessQuotation(#quotationId, authentication.principal.id)")
    public ResponseEntity<QuotationResponse> getQuotationById(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId) {

        logger.debug("Fetching quotation with ID: {}", quotationId);
        QuotationResponse quotationResponse = quotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(quotationResponse);
    }

    @Operation(summary = "Update quotation", description = "Update existing quotation (only if in DRAFT status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or quotation not editable"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{quotationId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<QuotationResponse> updateQuotation(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @Valid @RequestBody CreateQuotationRequest request) {

        logger.info("Updating quotation with ID: {}", quotationId);

        QuotationResponse quotationResponse = quotationService.updateQuotation(quotationId, request);

        logger.info("Quotation {} updated successfully with new total: {} {}",
                quotationId, quotationResponse.getTotalAmount(), quotationResponse.getCurrency());

        return ResponseEntity.ok(quotationResponse);
    }

    @Operation(summary = "Submit quotation for approval", description = "Submit quotation for approval by account managers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotation submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Quotation cannot be submitted in current status"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{quotationId}/submit")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<QuotationResponse> submitQuotation(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @Valid @RequestBody SubmitQuotationRequest request) {

        logger.info("Submitting quotation {} for approval", quotationId);

        QuotationResponse quotationResponse = quotationService.submitQuotation(quotationId, request);

        logger.info("Quotation {} submitted successfully for approval with total: {} {}",
                quotationId, quotationResponse.getTotalAmount(), quotationResponse.getCurrency());

        return ResponseEntity.ok(quotationResponse);
    }

    @Operation(summary = "Approve quotation", description = "Approve quotation (Account Managers only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotation approved successfully"),
            @ApiResponse(responseCode = "400", description = "Quotation cannot be approved in current status"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - only Account Managers can approve")
    })
    @PostMapping("/{quotationId}/approve")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<QuotationResponse> approveQuotation(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @RequestParam(required = false) String comments) {

        logger.info("Approving quotation {}", quotationId);

        QuotationResponse quotationResponse = quotationService.approveQuotation(quotationId, comments);

        logger.info("Quotation {} approved successfully", quotationId);

        return ResponseEntity.ok(quotationResponse);
    }

    @Operation(summary = "Reject quotation", description = "Reject quotation with reason (Account Managers only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotation rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Quotation cannot be rejected in current status"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - only Account Managers can reject")
    })
    @PostMapping("/{quotationId}/reject")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<QuotationResponse> rejectQuotation(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId,
            @RequestParam String reason) {

        logger.info("Rejecting quotation {} with reason: {}", quotationId, reason);

        QuotationResponse quotationResponse = quotationService.rejectQuotation(quotationId, reason);

        logger.info("Quotation {} rejected successfully", quotationId);

        return ResponseEntity.ok(quotationResponse);
    }

    @Operation(summary = "Get my quotations", description = "Get paginated list of quotations for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotations retrieved successfully")
    })
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<Page<QuotationSummaryResponse>> getMyQuotations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.debug("Fetching quotations - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QuotationSummaryResponse> quotations = quotationService.getMyQuotations(pageable);

        return ResponseEntity.ok(quotations);
    }

    @Operation(summary = "Get quotations by project", description = "Get all quotations for a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project quotations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied to project")
    })
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or @projectService.canUserAccessProject(#projectId, authentication.principal.id)")
    public ResponseEntity<List<QuotationSummaryResponse>> getQuotationsByProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId) {

        logger.debug("Fetching quotations for project: {}", projectId);

        List<QuotationSummaryResponse> quotations = quotationService.getQuotationsByProject(projectId);

        return ResponseEntity.ok(quotations);
    }

    @Operation(summary = "Get pending quotations", description = "Get quotations pending approval (Account Managers only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending quotations retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Account Managers only")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ACCOUNT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<QuotationSummaryResponse>> getPendingQuotations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.debug("Fetching pending quotations - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QuotationSummaryResponse> quotations = quotationService.getPendingQuotations(pageable);

        return ResponseEntity.ok(quotations);
    }

    @Operation(summary = "Search quotations", description = "Search quotations with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<Page<QuotationSummaryResponse>> searchQuotations(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long createdById,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.debug("Searching quotations with filters - projectId: {}, createdById: {}, status: {}, description: {}",
                projectId, createdById, status, description);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QuotationSummaryResponse> quotations = quotationService.searchQuotations(
                projectId, createdById, status, description, pageable);

        return ResponseEntity.ok(quotations);
    }

    @Operation(summary = "Delete quotation", description = "Delete quotation (only if in DRAFT status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotation deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Quotation cannot be deleted in current status"),
            @ApiResponse(responseCode = "404", description = "Quotation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{quotationId}")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteQuotation(
            @Parameter(description = "Quotation ID") @PathVariable Long quotationId) {

        logger.info("Deleting quotation with ID: {}", quotationId);

        quotationService.deleteQuotation(quotationId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Quotation deleted successfully");

        logger.info("Quotation {} deleted successfully", quotationId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get quotation statistics", description = "Get quotation statistics for dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<QuotationService.QuotationStatistics> getQuotationStatistics(
            @RequestParam(required = false) Long projectManagerId) {

        logger.debug("Fetching quotation statistics for project manager: {}", projectManagerId);

        QuotationService.QuotationStatistics statistics = quotationService.getQuotationStatistics(projectManagerId);

        return ResponseEntity.ok(statistics);
    }
}