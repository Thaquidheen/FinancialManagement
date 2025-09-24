package com.company.erp.project.repository;

import com.company.erp.project.entity.Project;
import com.company.erp.project.entity.ProjectStatus;
import com.company.erp.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Basic finder methods
    Optional<Project> findByIdAndActiveTrue(Long id);

    List<Project> findByActiveTrue();

    Page<Project> findByActiveTrue(Pageable pageable);

    boolean existsByNameAndActiveTrue(String name);

    boolean existsByNameAndIdNotAndActiveTrue(String name, Long id);

    // Find by status
    List<Project> findByStatusAndActiveTrue(ProjectStatus status);

    Page<Project> findByStatusAndActiveTrue(ProjectStatus status, Pageable pageable);

    // Find by manager
    List<Project> findByManagerAndActiveTrue(User manager);

    Page<Project> findByManagerAndActiveTrue(User manager, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.manager.id = :managerId AND p.active = true")
    List<Project> findByManagerIdAndActiveTrue(@Param("managerId") Long managerId);

    @Query("SELECT p FROM Project p WHERE p.manager.id = :managerId AND p.active = true")
    Page<Project> findByManagerIdAndActiveTrue(@Param("managerId") Long managerId, Pageable pageable);

    // Search projects by multiple criteria
    @Query("SELECT p FROM Project p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:managerId IS NULL OR p.manager.id = :managerId) AND " +
            "p.active = true")
    Page<Project> findBySearchCriteria(@Param("name") String name,
                                       @Param("location") String location,
                                       @Param("status") ProjectStatus status,
                                       @Param("managerId") Long managerId,
                                       Pageable pageable);

    // Budget-related queries
    @Query("SELECT p FROM Project p WHERE p.spentAmount > p.allocatedBudget AND p.active = true")
    List<Project> findOverBudgetProjects();

    @Query("SELECT p FROM Project p WHERE " +
            "(p.spentAmount / p.allocatedBudget) >= :threshold AND " +
            "p.spentAmount <= p.allocatedBudget AND p.active = true")
    List<Project> findProjectsNearBudgetLimit(@Param("threshold") BigDecimal threshold);

    @Query("SELECT p FROM Project p WHERE p.allocatedBudget BETWEEN :minBudget AND :maxBudget AND p.active = true")
    List<Project> findProjectsByBudgetRange(@Param("minBudget") BigDecimal minBudget,
                                            @Param("maxBudget") BigDecimal maxBudget);

    // Date-related queries
    @Query("SELECT p FROM Project p WHERE p.startDate <= :date AND " +
            "(p.endDate IS NULL OR p.endDate >= :date) AND p.active = true")
    List<Project> findActiveProjectsOnDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM Project p WHERE p.endDate < :date AND p.status != 'COMPLETED' AND p.active = true")
    List<Project> findOverdueProjects(@Param("date") LocalDate date);

    @Query("SELECT p FROM Project p WHERE p.endDate BETWEEN :startDate AND :endDate AND p.active = true")
    List<Project> findProjectsEndingBetween(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // Statistics and aggregations
    @Query("SELECT COUNT(p) FROM Project p WHERE p.active = true")
    long countActiveProjects();

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status AND p.active = true")
    long countProjectsByStatus(@Param("status") ProjectStatus status);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.manager.id = :managerId AND p.active = true")
    long countProjectsByManager(@Param("managerId") Long managerId);

    @Query("SELECT SUM(p.allocatedBudget) FROM Project p WHERE p.active = true")
    BigDecimal getTotalAllocatedBudget();

    @Query("SELECT SUM(p.spentAmount) FROM Project p WHERE p.active = true")
    BigDecimal getTotalSpentAmount();

    @Query("SELECT AVG(p.completionPercentage) FROM Project p WHERE p.active = true")
    BigDecimal getAverageCompletionPercentage();

    // Complex queries with joins
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.manager LEFT JOIN FETCH p.assignments WHERE p.id = :id")
    Optional<Project> findByIdWithManagerAndAssignments(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.manager WHERE p.active = true")
    List<Project> findAllActiveWithManager();

    @Query("SELECT p FROM Project p WHERE p.manager IS NULL AND p.active = true")
    List<Project> findUnassignedProjects();

    // Update methods
    @Modifying
    @Query("UPDATE Project p SET p.spentAmount = :spentAmount, p.remainingBudget = p.allocatedBudget - :spentAmount WHERE p.id = :projectId")
    void updateProjectBudgetSpent(@Param("projectId") Long projectId, @Param("spentAmount") BigDecimal spentAmount);

    @Modifying
    @Query("UPDATE Project p SET p.allocatedBudget = :allocatedBudget, p.remainingBudget = :allocatedBudget - p.spentAmount WHERE p.id = :projectId")
    void updateProjectAllocatedBudget(@Param("projectId") Long projectId, @Param("allocatedBudget") BigDecimal allocatedBudget);

    @Modifying
    @Query("UPDATE Project p SET p.completionPercentage = :percentage WHERE p.id = :projectId")
    void updateCompletionPercentage(@Param("projectId") Long projectId, @Param("percentage") BigDecimal percentage);

    @Modifying
    @Query("UPDATE Project p SET p.status = :status WHERE p.id = :projectId")
    void updateProjectStatus(@Param("projectId") Long projectId, @Param("status") ProjectStatus status);

    @Modifying
    @Query("UPDATE Project p SET p.manager = :manager WHERE p.id = :projectId")
    void updateProjectManager(@Param("projectId") Long projectId, @Param("manager") User manager);

    @Modifying
    @Query("UPDATE Project p SET p.active = false WHERE p.id = :projectId")
    void deactivateProject(@Param("projectId") Long projectId);

    // Manager workload queries
    @Query("SELECT p.manager.id, COUNT(p) FROM Project p WHERE p.active = true AND p.manager IS NOT NULL GROUP BY p.manager.id")
    List<Object[]> getManagerWorkloadStatistics();

    @Query("SELECT p FROM Project p WHERE p.manager.id = :managerId AND p.status IN ('ACTIVE', 'ON_HOLD') AND p.active = true")
    List<Project> findActiveProjectsByManager(@Param("managerId") Long managerId);

    // Department-wise project statistics
    @Query("SELECT p.manager.department, COUNT(p), SUM(p.allocatedBudget), SUM(p.spentAmount) " +
            "FROM Project p WHERE p.active = true AND p.manager IS NOT NULL AND p.manager.department IS NOT NULL " +
            "GROUP BY p.manager.department")
    List<Object[]> getDepartmentProjectStatistics();

    // Recent projects
    @Query("SELECT p FROM Project p WHERE p.active = true ORDER BY p.createdDate DESC")
    List<Project> findRecentProjects(Pageable pageable);

    // Projects requiring attention
    @Query("SELECT p FROM Project p WHERE " +
            "(p.spentAmount > p.allocatedBudget OR " +
            "(p.spentAmount / p.allocatedBudget) >= 0.9 OR " +
            "(p.endDate < CURRENT_DATE AND p.status != 'COMPLETED')) " +
            "AND p.active = true")
    List<Project> findProjectsRequiringAttention();

    boolean existsByIdAndManagerIdAndActiveTrue(Long id, Long managerId, Boolean active);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.active = true")
    Long countByActiveTrue();

    /**
     * Count projects by manager ID and active status
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.managerId = :managerId AND p.active = true")
    Long countByManagerIdAndActiveTrue(@Param("managerId") Long managerId);

    /**
     * Count projects by status and active flag
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status AND p.active = true")
    Long countByStatusAndActiveTrue(@Param("status") String status);

    /**
     * Find active projects ordered by spent amount (descending)
     */
    @Query("SELECT p FROM Project p WHERE p.active = true ORDER BY p.spentAmount DESC")
    List<Project> findByActiveTrueOrderBySpentAmountDesc();



    /**
     * Find projects by status
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.active = true")
    List<Project> findByStatusAndActiveTrue(@Param("status") String status);




    /**
     * Get total allocated budget for projects managed by specific user
     */
    @Query("SELECT COALESCE(SUM(p.allocatedBudget), 0) FROM Project p WHERE p.managerId = :managerId AND p.active = true")
    BigDecimal getTotalAllocatedBudgetByManager(@Param("managerId") Long managerId);

    /**
     * Get total spent amount for projects managed by specific user
     */
    @Query("SELECT COALESCE(SUM(p.spentAmount), 0) FROM Project p WHERE p.managerId = :managerId AND p.active = true")
    BigDecimal getTotalSpentAmountByManager(@Param("managerId") Long managerId);

    /**
     * Find projects with budget overruns
     */
    @Query("SELECT p FROM Project p WHERE p.spentAmount > p.allocatedBudget AND p.active = true")
    List<Project> findProjectsOverBudget();

    /**
     * Find projects by status list
     */
    @Query("SELECT p FROM Project p WHERE p.status IN :statuses AND p.active = true")
    List<Project> findByStatusInAndActiveTrue(@Param("statuses") List<String> statuses);

    /**
     * Count projects by date range
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdDate BETWEEN :startDate AND :endDate AND p.active = true")
    Long countByCreatedDateBetweenAndActiveTrue(@Param("startDate") java.time.LocalDateTime startDate,
                                                @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find top spending projects
     */
    @Query("SELECT p FROM Project p WHERE p.active = true ORDER BY p.spentAmount DESC LIMIT :limit")
    List<Project> findTopSpendingProjects(@Param("limit") int limit);

    /**
     * Get project count by status for dashboard
     */
    @Query("SELECT p.status, COUNT(p) FROM Project p WHERE p.active = true GROUP BY p.status")
    List<Object[]> getProjectCountByStatus();

    /**
     * Get projects with budget utilization over threshold
     */
    @Query("SELECT p FROM Project p WHERE p.active = true AND " +
            "(p.spentAmount / NULLIF(p.allocatedBudget, 0)) * 100 > :threshold")
    List<Project> findProjectsOverBudgetThreshold(@Param("threshold") Double threshold);
}