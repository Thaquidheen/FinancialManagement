package com.company.erp.project.controller;

import com.company.erp.common.security.UserPrincipal;
import com.company.erp.project.dto.request.AssignManagerRequest;
import com.company.erp.project.dto.request.CreateProjectRequest;
import com.company.erp.project.dto.request.UpdateBudgetRequest;
import com.company.erp.project.dto.request.UpdateProjectRequest;
import com.company.erp.project.dto.request.UpdateStatusRequest;
import com.company.erp.project.dto.response.ProjectResponse;
import com.company.erp.project.entity.ProjectStatus;
import com.company.erp.project.service.ProjectService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
@Tag(name = "Project Management", description = "Project CRUD operations and management endpoints")
@Validated
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Operation(summary = "Create new project", description = "Create a new project with budget allocation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate project name"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        logger.info("Creating new project: {}", request.getName());

        ProjectResponse projectResponse = projectService.createProject(request);

        logger.info("Project created successfully with ID: {}", projectResponse.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
    }

    @Operation(summary = "Get project by ID", description = "Retrieve project details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or @projectService.canUserAccessProject(#projectId, authentication.principal.id)")
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Project ID") @PathVariable Long projectId) {

        logger.debug("Fetching project with ID: {}", projectId);
        ProjectResponse projectResponse = projectService.getProjectById(projectId);
        return ResponseEntity.ok(projectResponse);
    }

    @Operation(summary = "Get all projects", description = "Retrieve paginated list of all projects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectResponse> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Search projects", description = "Search projects by name, location, status, and manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER')")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(
            @Parameter(description = "Project name search term") @RequestParam(required = false) String name,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Manager ID filter") @RequestParam(required = false) Long managerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        ProjectStatus projectStatus = null;
        if (status != null) {
            try {
                projectStatus = ProjectStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid project status: {}", status);
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<ProjectResponse> projects = projectService.searchProjects(name, location, projectStatus, managerId, pageable);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Get my projects", description = "Get projects assigned to current user as manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/my-projects")
    @PreAuthorize("hasAuthority('PROJECT_MANAGER') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        // Get current user ID from security context
        Long currentUserId = getCurrentUserId(); // This method needs to be implemented

        List<ProjectResponse> projects = projectService.getProjectsByManager(currentUserId);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Update project", description = "Update project information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @projectService.canUserAccessProject(#projectId, authentication.principal.id)")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        logger.info("Updating project with ID: {}", projectId);
        ProjectResponse projectResponse = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(projectResponse);
    }

    @Operation(summary = "Assign manager to project", description = "Assign a project manager to a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manager assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid manager or project"),
            @ApiResponse(responseCode = "404", description = "Project or manager not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{projectId}/assign-manager")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProjectResponse> assignManager(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            @Valid @RequestBody AssignManagerRequest request) {

        logger.info("Assigning manager {} to project {}", request.getManagerId(), projectId);
        ProjectResponse projectResponse = projectService.assignManager(projectId, request.getManagerId());
        return ResponseEntity.ok(projectResponse);
    }

    @Operation(summary = "Update project budget", description = "Update project allocated budget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid budget amount"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{projectId}/budget")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProjectResponse> updateProjectBudget(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            @Valid @RequestBody UpdateBudgetRequest request) {

        logger.info("Updating budget for project {} to {}", projectId, request.getAllocatedBudget());
        ProjectResponse projectResponse = projectService.updateProjectBudget(projectId, request.getAllocatedBudget());
        return ResponseEntity.ok(projectResponse);
    }

    @Operation(summary = "Update project status", description = "Update project status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{projectId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @projectService.canUserAccessProject(#projectId, authentication.principal.id)")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @Parameter(description = "Project ID") @PathVariable Long projectId,
            @Valid @RequestBody UpdateStatusRequest request) {

        ProjectStatus status;
        try {
            status = ProjectStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid project status: " + request.getStatus());
            return ResponseEntity.badRequest().body(null);
        }

        logger.info("Updating status for project {} to {}", projectId, status);
        ProjectResponse projectResponse = projectService.updateProjectStatus(projectId, status);
        return ResponseEntity.ok(projectResponse);
    }

    @Operation(summary = "Get projects by status", description = "Get all projects with specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    public ResponseEntity<List<ProjectResponse>> getProjectsByStatus(
            @Parameter(description = "Project status") @PathVariable String status) {

        ProjectStatus projectStatus;
        try {
            projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid project status: " + status);
            return ResponseEntity.badRequest().body(null);
        }

        List<ProjectResponse> projects = projectService.getProjectsByStatus(projectStatus);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Get projects requiring attention", description = "Get projects that are over budget, overdue, or need attention")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/requiring-attention")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    public ResponseEntity<List<ProjectResponse>> getProjectsRequiringAttention() {
        List<ProjectResponse> projects = projectService.getProjectsRequiringAttention();
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Get project statistics", description = "Get project statistics and metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ACCOUNT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getProjectStatistics() {
        ProjectService.ProjectStatistics stats = projectService.getProjectStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("totalProjects", stats.getTotalProjects());
        response.put("activeProjects", stats.getActiveProjects());
        response.put("completedProjects", stats.getCompletedProjects());
        response.put("onHoldProjects", stats.getOnHoldProjects());
        response.put("totalBudget", stats.getTotalBudget());
        response.put("totalSpent", stats.getTotalSpent());
        response.put("averageCompletion", stats.getAverageCompletion());
        response.put("managerWorkload", stats.getManagerWorkload());
        response.put("departmentStats", stats.getDepartmentStats());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate project", description = "Deactivate a project (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateProject(
            @Parameter(description = "Project ID") @PathVariable Long projectId) {

        logger.info("Deactivating project with ID: {}", projectId);
        projectService.deactivateProject(projectId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Project deactivated successfully");
        return ResponseEntity.ok(response);
    }

    // Helper method to get current user ID from security context
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getId();
        }
        throw new RuntimeException("Unable to get current user ID from security context");
    }
}