package com.company.erp.project.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.DuplicateResourceException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.project.dto.request.CreateProjectRequest;
import com.company.erp.project.dto.request.UpdateProjectRequest;
import com.company.erp.project.dto.response.ProjectResponse;
import com.company.erp.project.entity.Project;
import com.company.erp.project.entity.ProjectStatus;
import com.company.erp.project.repository.ProjectRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create new project
     */
    public ProjectResponse createProject(CreateProjectRequest request) {
        logger.info("Creating new project: {}", request.getName());

        // Check if project name already exists
        if (projectRepository.existsByNameAndActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Project", "name", request.getName());
        }

        // Create project entity
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLocation(request.getLocation());
        project.setAllocatedBudget(request.getAllocatedBudget());
        project.setRemainingBudget(request.getAllocatedBudget());
        project.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SAR");
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(ProjectStatus.ACTIVE);

        Project savedProject = projectRepository.save(project);
        logger.info("Project created successfully with ID: {}", savedProject.getId());

        return convertToProjectResponse(savedProject);
    }

    /**
     * Assign manager to project
     */
    public ProjectResponse assignManager(Long projectId, Long managerId) {
        logger.info("Assigning manager {} to project {}", managerId, projectId);

        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User manager = userRepository.findByIdAndActiveTrue(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));

        // Check if user has PROJECT_MANAGER or SUPER_ADMIN role
        if (!manager.hasRole("PROJECT_MANAGER") && !manager.hasRole("SUPER_ADMIN")) {
            throw new BusinessException("INVALID_ROLE", "User must have PROJECT_MANAGER or SUPER_ADMIN role");
        }

        project.assignManager(manager);
        Project savedProject = projectRepository.save(project);

        logger.info("Manager assigned successfully to project: {}", savedProject.getName());
        return convertToProjectResponse(savedProject);
    }

    /**
     * Update project
     */
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        logger.info("Updating project with ID: {}", projectId);

        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Check name uniqueness if changed
        if (!project.getName().equals(request.getName()) &&
                projectRepository.existsByNameAndIdNotAndActiveTrue(request.getName(), projectId)) {
            throw new DuplicateResourceException("Project", "name", request.getName());
        }

        // Update project fields
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLocation(request.getLocation());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        // Update budget if provided and valid
        if (request.getAllocatedBudget() != null) {
            if (request.getAllocatedBudget().compareTo(project.getSpentAmount()) < 0) {
                throw new BusinessException("INVALID_BUDGET",
                        "Allocated budget cannot be less than already spent amount");
            }
            project.setAllocatedBudget(request.getAllocatedBudget());
        }

        // Update completion percentage if provided
        if (request.getCompletionPercentage() != null) {
            project.updateCompletionPercentage(request.getCompletionPercentage());
        }

        Project savedProject = projectRepository.save(project);
        logger.info("Project updated successfully: {}", savedProject.getName());

        return convertToProjectResponse(savedProject);
    }

    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findByIdWithManagerAndAssignments(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        return convertToProjectResponse(project);
    }

    /**
     * Get all projects with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findByActiveTrue(pageable);
        return projects.map(this::convertToProjectResponse);
    }

    /**
     * Get projects by manager
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByManager(Long managerId) {
        List<Project> projects = projectRepository.findByManagerIdAndActiveTrue(managerId);
        return projects.stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search projects by criteria
     */
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(String name, String location,
                                                ProjectStatus status, Long managerId,
                                                Pageable pageable) {
        Page<Project> projects = projectRepository.findBySearchCriteria(
                name, location, status, managerId, pageable);
        return projects.map(this::convertToProjectResponse);
    }

    /**
     * Get projects by status
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        List<Project> projects = projectRepository.findByStatusAndActiveTrue(status);
        return projects.stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active projects as entities (for internal services)
     */
    @Transactional(readOnly = true)
    public List<Project> getActiveProjects() {
        return projectRepository.findByStatusAndActiveTrue(ProjectStatus.ACTIVE);
    }

    /**
     * Update project status
     */
    public ProjectResponse updateProjectStatus(Long projectId, ProjectStatus status) {
        logger.info("Updating project {} status to {}", projectId, status);

        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        project.setStatus(status);

        // Auto-update completion percentage based on status
        if (status == ProjectStatus.COMPLETED) {
            project.setCompletionPercentage(new BigDecimal("100"));
        }

        Project savedProject = projectRepository.save(project);
        logger.info("Project status updated successfully");

        return convertToProjectResponse(savedProject);
    }

    /**
     * Update project budget
     */
    public ProjectResponse updateProjectBudget(Long projectId, BigDecimal newBudget) {
        logger.info("Updating project {} budget to {}", projectId, newBudget);

        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (newBudget.compareTo(project.getSpentAmount()) < 0) {
            throw new BusinessException("INVALID_BUDGET",
                    "New budget cannot be less than already spent amount");
        }

        project.setAllocatedBudget(newBudget);
        Project savedProject = projectRepository.save(project);

        logger.info("Project budget updated successfully");
        return convertToProjectResponse(savedProject);
    }

    /**
     * Get projects requiring attention (over budget, overdue, etc.)
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsRequiringAttention() {
        List<Project> projects = projectRepository.findProjectsRequiringAttention();
        return projects.stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get project statistics
     */
    @Transactional(readOnly = true)
    public ProjectStatistics getProjectStatistics() {
        long totalProjects = projectRepository.countActiveProjects();
        long activeProjects = projectRepository.countProjectsByStatus(ProjectStatus.ACTIVE);
        long completedProjects = projectRepository.countProjectsByStatus(ProjectStatus.COMPLETED);
        long onHoldProjects = projectRepository.countProjectsByStatus(ProjectStatus.ON_HOLD);

        BigDecimal totalBudget = projectRepository.getTotalAllocatedBudget();
        BigDecimal totalSpent = projectRepository.getTotalSpentAmount();
        BigDecimal averageCompletion = projectRepository.getAverageCompletionPercentage();

        List<Object[]> managerWorkload = projectRepository.getManagerWorkloadStatistics();
        List<Object[]> departmentStats = projectRepository.getDepartmentProjectStatistics();

        return new ProjectStatistics(totalProjects, activeProjects, completedProjects,
                onHoldProjects, totalBudget, totalSpent, averageCompletion,
                managerWorkload, departmentStats);
    }

    /**
     * Deactivate project
     */
    public void deactivateProject(Long projectId) {
        logger.info("Deactivating project with ID: {}", projectId);

        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        project.setActive(false);
        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);

        logger.info("Project deactivated: {}", project.getName());
    }

    /**
     * Check if user can access project
     */
    @Transactional(readOnly = true)
    public boolean canUserAccessProject(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndActiveTrue(projectId)
                .orElse(null);

        if (project == null) {
            return false;
        }

        // Check if user is the manager
        if (project.getManager() != null && project.getManager().getId().equals(userId)) {
            return true;
        }

        // Check if user is assigned to the project
        return project.isAssigned(userRepository.findById(userId).orElse(null));
    }

    /**
     * Convert Project entity to ProjectResponse DTO
     */
    private ProjectResponse convertToProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setLocation(project.getLocation());
        response.setAllocatedBudget(project.getAllocatedBudget());
        response.setSpentAmount(project.getSpentAmount());
        response.setRemainingBudget(project.getRemainingBudget());
        response.setCurrency(project.getCurrency());
        response.setStatus(project.getStatus().name());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setCompletionPercentage(project.getCompletionPercentage());
        response.setBudgetUtilizationPercentage(project.getBudgetUtilizationPercentage());
        response.setCreatedDate(project.getCreatedDate());
        response.setActive(project.getActive());

        if (project.getManager() != null) {
            response.setManagerId(project.getManager().getId());
            response.setManagerName(project.getManager().getFullName());
            response.setManagerEmail(project.getManager().getEmail());
        }

        // Add flags for project status
        response.setOverBudget(project.isOverBudget());
        response.setBudgetWarning(project.isBudgetWarning(new BigDecimal("0.8"))); // 80% threshold

        if (project.getEndDate() != null) {
            response.setOverdue(project.getEndDate().isBefore(LocalDate.now()) &&
                    project.getStatus() != ProjectStatus.COMPLETED);
        }

        return response;
    }

    // Inner class for project statistics
    public static class ProjectStatistics {
        private long totalProjects;
        private long activeProjects;
        private long completedProjects;
        private long onHoldProjects;
        private BigDecimal totalBudget;
        private BigDecimal totalSpent;
        private BigDecimal averageCompletion;
        private List<Object[]> managerWorkload;
        private List<Object[]> departmentStats;

        public ProjectStatistics(long totalProjects, long activeProjects, long completedProjects,
                                 long onHoldProjects, BigDecimal totalBudget, BigDecimal totalSpent,
                                 BigDecimal averageCompletion, List<Object[]> managerWorkload,
                                 List<Object[]> departmentStats) {
            this.totalProjects = totalProjects;
            this.activeProjects = activeProjects;
            this.completedProjects = completedProjects;
            this.onHoldProjects = onHoldProjects;
            this.totalBudget = totalBudget != null ? totalBudget : BigDecimal.ZERO;
            this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
            this.averageCompletion = averageCompletion != null ? averageCompletion : BigDecimal.ZERO;
            this.managerWorkload = managerWorkload;
            this.departmentStats = departmentStats;
        }

        // Getters
        public long getTotalProjects() { return totalProjects; }
        public long getActiveProjects() { return activeProjects; }
        public long getCompletedProjects() { return completedProjects; }
        public long getOnHoldProjects() { return onHoldProjects; }
        public BigDecimal getTotalBudget() { return totalBudget; }
        public BigDecimal getTotalSpent() { return totalSpent; }
        public BigDecimal getAverageCompletion() { return averageCompletion; }
        public List<Object[]> getManagerWorkload() { return managerWorkload; }
        public List<Object[]> getDepartmentStats() { return departmentStats; }
    }
}