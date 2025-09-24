package com.company.erp.financial.repository;

import com.company.erp.financial.entity.BudgetTracking;
import com.company.erp.financial.entity.BudgetTrackingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetTrackingRepository extends JpaRepository<BudgetTracking, Long> {

    // Basic finder methods
    Optional<BudgetTracking> findByIdAndActiveTrue(Long id);

    List<BudgetTracking> findByActiveTrue();

    // Find by project
    List<BudgetTracking> findByProjectIdAndActiveTrue(Long projectId);

    Page<BudgetTracking> findByProjectIdAndActiveTrueOrderByTransactionDateDesc(Long projectId, Pageable pageable);

    @Query("SELECT bt FROM BudgetTracking bt LEFT JOIN FETCH bt.project LEFT JOIN FETCH bt.recordedBy " +
            "WHERE bt.project.id = :projectId AND bt.active = true " +
            "ORDER BY bt.transactionDate DESC")
    Page<BudgetTracking> findByProjectIdWithDetailsOrderByTransactionDateDesc(@Param("projectId") Long projectId, Pageable pageable);

    // Find by project with filters
    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.project.id = :projectId AND bt.active = true " +
            "AND (:trackingType IS NULL OR bt.trackingType = :trackingType) " +
            "AND (:startDate IS NULL OR bt.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR bt.transactionDate <= :endDate) " +
            "ORDER BY bt.transactionDate DESC")
    Page<BudgetTracking> findByProjectIdWithFilters(@Param("projectId") Long projectId,
                                                    @Param("trackingType") BudgetTrackingType trackingType,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);

    // Find by tracking type
    List<BudgetTracking> findByTrackingTypeAndActiveTrue(BudgetTrackingType trackingType);

    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.trackingType = :trackingType AND bt.active = true " +
            "ORDER BY bt.transactionDate DESC")
    Page<BudgetTracking> findByTrackingTypeWithProject(@Param("trackingType") BudgetTrackingType trackingType, Pageable pageable);

    // Find by reference
    List<BudgetTracking> findByReferenceTypeAndReferenceIdAndActiveTrue(String referenceType, Long referenceId);

    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.referenceType = :referenceType AND bt.referenceId = :referenceId AND bt.active = true")
    List<BudgetTracking> findByReference(@Param("referenceType") String referenceType, @Param("referenceId") Long referenceId);

    // Find by user who recorded
    List<BudgetTracking> findByRecordedByIdAndActiveTrue(Long recordedById);

    // Date-related queries
    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.transactionDate BETWEEN :startDate AND :endDate AND bt.active = true")
    List<BudgetTracking> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.project.id = :projectId AND bt.transactionDate >= :date AND bt.active = true")
    List<BudgetTracking> findByProjectAfterDate(@Param("projectId") Long projectId, @Param("date") LocalDateTime date);

    // Balance calculations
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN bt.trackingType IN ('EXPENSE', 'QUOTATION_APPROVED') THEN bt.amount " +
            "WHEN bt.trackingType IN ('REFUND', 'BUDGET_DECREASE') THEN -bt.amount " +
            "ELSE bt.amount END), 0) " +
            "FROM BudgetTracking bt WHERE bt.project.id = :projectId AND bt.active = true")
    BigDecimal getCurrentBalance(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(bt.amount), 0) FROM BudgetTracking bt " +
            "WHERE bt.project.id = :projectId AND bt.trackingType = :trackingType AND bt.active = true")
    BigDecimal getTotalAmountByProjectAndType(@Param("projectId") Long projectId, @Param("trackingType") BudgetTrackingType trackingType);

    // Statistics and counts
    @Query("SELECT COUNT(bt) FROM BudgetTracking bt WHERE bt.active = true")
    long countActiveBudgetTrackings();

    @Query("SELECT COUNT(bt) FROM BudgetTracking bt WHERE bt.project.id = :projectId AND bt.active = true")
    long countByProject(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(bt) FROM BudgetTracking bt WHERE bt.trackingType = :trackingType AND bt.active = true")
    long countByTrackingType(@Param("trackingType") BudgetTrackingType trackingType);

    @Query("SELECT COUNT(bt) FROM BudgetTracking bt " +
            "WHERE bt.project.id = :projectId AND bt.transactionDate >= :date AND bt.active = true")
    long countRecentTransactions(@Param("projectId") Long projectId, @Param("date") LocalDateTime date);

    // Budget alerts and monitoring
    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.isBudgetExceeded = true AND bt.active = true " +
            "ORDER BY bt.transactionDate DESC")
    List<BudgetTracking> findBudgetExceededTransactions();

    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.project.id = :projectId " +
            "AND bt.variancePercentage >= :threshold AND bt.active = true " +
            "ORDER BY bt.transactionDate DESC")
    List<BudgetTracking> findByProjectAndVarianceThreshold(@Param("projectId") Long projectId, @Param("threshold") BigDecimal threshold);

    // Latest transactions
    Optional<BudgetTracking> findFirstByProjectIdAndActiveTrueOrderByTransactionDateDesc(Long projectId);

    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.active = true ORDER BY bt.transactionDate DESC")
    List<BudgetTracking> findRecentTransactions(Pageable pageable);

    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.project.id = :projectId AND bt.active = true " +
            "ORDER BY bt.transactionDate DESC")
    List<BudgetTracking> findRecentTransactionsByProject(@Param("projectId") Long projectId, Pageable pageable);

    // Summary and reporting queries
    @Query("SELECT " +
            "COALESCE(SUM(p.allocatedBudget), 0), " +
            "COALESCE(SUM(p.spentAmount), 0), " +
            "COALESCE(SUM(p.remainingBudget), 0), " +
            "COUNT(DISTINCT p.id) " +
            "FROM Project p WHERE p.active = true " +
            "AND (:startDate IS NULL OR p.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.createdDate <= :endDate)")
    List<Object[]> getBudgetSummary(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.id, p.name, p.allocatedBudget, p.spentAmount, p.remainingBudget " +
            "FROM Project p WHERE p.active = true " +
            "AND (:startDate IS NULL OR p.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.createdDate <= :endDate) " +
            "ORDER BY p.spentAmount DESC")
    List<Object[]> getProjectBudgetBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Category-wise analysis
    @Query("SELECT bt.category, COUNT(bt), SUM(bt.amount) FROM BudgetTracking bt " +
            "WHERE bt.active = true AND bt.category IS NOT NULL " +
            "GROUP BY bt.category ORDER BY SUM(bt.amount) DESC")
    List<Object[]> getBudgetTrackingByCategory();

    @Query("SELECT bt.trackingType, COUNT(bt), SUM(bt.amount) FROM BudgetTracking bt " +
            "WHERE bt.project.id = :projectId AND bt.active = true " +
            "GROUP BY bt.trackingType ORDER BY SUM(bt.amount) DESC")
    List<Object[]> getProjectBudgetTrackingByType(@Param("projectId") Long projectId);

    // Monthly/Yearly analysis
    @Query("SELECT " +
            "FUNCTION('YEAR', bt.transactionDate), " +
            "FUNCTION('MONTH', bt.transactionDate), " +
            "bt.trackingType, " +
            "COUNT(bt), " +
            "SUM(bt.amount) " +
            "FROM BudgetTracking bt " +
            "WHERE bt.project.id = :projectId AND bt.active = true " +
            "AND bt.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', bt.transactionDate), FUNCTION('MONTH', bt.transactionDate), bt.trackingType " +
            "ORDER BY FUNCTION('YEAR', bt.transactionDate), FUNCTION('MONTH', bt.transactionDate)")
    List<Object[]> getMonthlyBudgetTracking(@Param("projectId") Long projectId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Variance analysis
    @Query("SELECT bt FROM BudgetTracking bt WHERE bt.project.id = :projectId " +
            "AND ABS(bt.variancePercentage) > :varianceThreshold AND bt.active = true " +
            "ORDER BY ABS(bt.variancePercentage) DESC")
    List<BudgetTracking> findHighVarianceTransactions(@Param("projectId") Long projectId, @Param("varianceThreshold") BigDecimal varianceThreshold);

    // Export queries
    @Query("SELECT bt FROM BudgetTracking bt LEFT JOIN FETCH bt.project LEFT JOIN FETCH bt.recordedBy " +
            "WHERE bt.project.id = :projectId AND bt.active = true " +
            "AND (:startDate IS NULL OR bt.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR bt.transactionDate <= :endDate) " +
            "ORDER BY bt.transactionDate ASC")
    List<BudgetTracking> findByProjectIdForExport(@Param("projectId") Long projectId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Update methods
    @Modifying
    @Query("UPDATE BudgetTracking bt SET bt.active = false WHERE bt.id = :id")
    void deactivateBudgetTracking(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BudgetTracking bt SET bt.active = false WHERE bt.project.id = :projectId")
    void deactivateByProject(@Param("projectId") Long projectId);

    @Modifying
    @Query("UPDATE BudgetTracking bt SET bt.description = :description WHERE bt.id = :id")
    void updateDescription(@Param("id") Long id, @Param("description") String description);

    // Advanced search
    @Query("SELECT bt FROM BudgetTracking bt LEFT JOIN FETCH bt.project LEFT JOIN FETCH bt.recordedBy " +
            "WHERE bt.active = true " +
            "AND (:projectId IS NULL OR bt.project.id = :projectId) " +
            "AND (:trackingType IS NULL OR bt.trackingType = :trackingType) " +
            "AND (:category IS NULL OR LOWER(bt.category) LIKE LOWER(CONCAT('%', :category, '%'))) " +
            "AND (:description IS NULL OR LOWER(bt.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
            "AND (:minAmount IS NULL OR bt.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR bt.amount <= :maxAmount) " +
            "AND (:startDate IS NULL OR bt.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR bt.transactionDate <= :endDate) " +
            "ORDER BY bt.transactionDate DESC")
    Page<BudgetTracking> findByAdvancedSearch(@Param("projectId") Long projectId,
                                              @Param("trackingType") BudgetTrackingType trackingType,
                                              @Param("category") String category,
                                              @Param("description") String description,
                                              @Param("minAmount") BigDecimal minAmount,
                                              @Param("maxAmount") BigDecimal maxAmount,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);

    // Department/User analysis
    @Query("SELECT u.fullName, u.department, COUNT(bt), SUM(bt.amount) " +
            "FROM BudgetTracking bt JOIN bt.recordedBy u " +
            "WHERE bt.active = true AND u.active = true " +
            "GROUP BY u.id, u.fullName, u.department " +
            "ORDER BY SUM(bt.amount) DESC")
    List<Object[]> getBudgetTrackingByUser();

    // Top spenders/projects
    @Query("SELECT p.name, SUM(bt.amount) FROM BudgetTracking bt JOIN bt.project p " +
            "WHERE bt.active = true AND bt.trackingType IN ('EXPENSE', 'QUOTATION_APPROVED') " +
            "GROUP BY p.id, p.name " +
            "ORDER BY SUM(bt.amount) DESC")
    List<Object[]> getTopSpendingProjects(Pageable pageable);

    // Budget utilization over time
    @Query("SELECT DATE(bt.transactionDate), SUM(bt.amount) FROM BudgetTracking bt " +
            "WHERE bt.project.id = :projectId AND bt.active = true " +
            "AND bt.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(bt.transactionDate) " +
            "ORDER BY DATE(bt.transactionDate)")
    List<Object[]> getDailySpendingTrend(@Param("projectId") Long projectId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}