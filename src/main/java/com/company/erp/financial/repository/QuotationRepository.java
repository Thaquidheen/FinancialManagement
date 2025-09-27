package com.company.erp.financial.repository;

import com.company.erp.financial.entity.Quotation;
import com.company.erp.financial.entity.QuotationStatus;
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
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    // Basic finder methods
    Optional<Quotation> findByIdAndActiveTrue(Long id);

    List<Quotation> findByActiveTrue();

    Page<Quotation> findByActiveTrue(Pageable pageable);

    // Find with relationships loaded
    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator WHERE q.id = :id AND q.active = true")
    Optional<Quotation> findByIdWithProjectAndCreator(@Param("id") Long id);

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator LEFT JOIN FETCH q.items WHERE q.id = :id AND q.active = true")
    Optional<Quotation> findByIdWithProjectAndItems(@Param("id") Long id);

    // Find by project
    List<Quotation> findByProjectIdAndActiveTrue(Long projectId);

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.items WHERE q.project.id = :projectId AND q.active = true")
    List<Quotation> findByProjectIdWithItems(@Param("projectId") Long projectId);

    // Find by creator (project manager)
    @Query("SELECT q FROM Quotation q WHERE q.creator.id = :createdById AND q.active = true")
    List<Quotation> findByCreatedByIdAndActiveTrue(@Param("createdById") Long createdById);

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project WHERE q.creator.id = :createdById AND q.active = true")
    Page<Quotation> findByCreatedByIdWithProject(@Param("createdById") Long createdById, Pageable pageable);

    // Find by status
    List<Quotation> findByStatusAndActiveTrue(QuotationStatus status);

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator WHERE q.status = :status AND q.active = true")
    Page<Quotation> findByStatusWithProject(@Param("status") QuotationStatus status, Pageable pageable);

    // Find all with project info
    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator WHERE q.active = true")
    Page<Quotation> findAllWithProject(Pageable pageable);

    // Search by multiple criteria
    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator WHERE " +
            "(:projectId IS NULL OR q.project.id = :projectId) AND " +
            "(:createdById IS NULL OR q.creator.id = :createdById) AND " +
            "(:status IS NULL OR q.status = :status) AND " +
            "(:description IS NULL OR LOWER(q.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
            "q.active = true")
    Page<Quotation> findBySearchCriteria(@Param("projectId") Long projectId,
                                         @Param("createdById") Long createdById,
                                         @Param("status") QuotationStatus status,
                                         @Param("description") String description,
                                         Pageable pageable);

    // Amount-related queries
    @Query("SELECT SUM(q.totalAmount) FROM Quotation q WHERE q.project.id = :projectId AND q.status IN ('APPROVED', 'PAYMENT_FILE_GENERATED', 'SENT_TO_BANK', 'PAID') AND q.active = true")
    BigDecimal getTotalApprovedAmountByProject(@Param("projectId") Long projectId);

    @Query("SELECT SUM(q.totalAmount) FROM Quotation q WHERE q.creator.id = :createdById AND q.status = :status AND q.active = true")
    BigDecimal getTotalAmountByCreatorAndStatus(@Param("createdById") Long createdById, @Param("status") QuotationStatus status);

    @Query("SELECT q FROM Quotation q WHERE q.totalAmount > :amount AND q.active = true")
    List<Quotation> findByTotalAmountGreaterThan(@Param("amount") BigDecimal amount);

    // Date-related queries
    @Query("SELECT q FROM Quotation q WHERE q.submittedDate BETWEEN :startDate AND :endDate AND q.active = true")
    List<Quotation> findBySubmittedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT q FROM Quotation q WHERE q.createdDate >= :date AND q.active = true")
    List<Quotation> findCreatedAfter(@Param("date") LocalDateTime date);

    // Statistics and counts
    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.active = true")
    long countActiveQuotations();

    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.status = :status AND q.active = true")
    long countByStatus(@Param("status") QuotationStatus status);

    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.creator.id = :createdById AND q.active = true")
    long countByCreatedBy(@Param("createdById") Long createdById);

    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.project.id = :projectId AND q.active = true")
    long countByProject(@Param("projectId") Long projectId);

    // Update methods
    @Modifying
    @Query("UPDATE Quotation q SET q.totalAmount = :totalAmount WHERE q.id = :quotationId")
    void updateTotalAmount(@Param("quotationId") Long quotationId, @Param("totalAmount") BigDecimal totalAmount);

    @Modifying
    @Query("UPDATE Quotation q SET q.status = :status WHERE q.id = :quotationId")
    void updateStatus(@Param("quotationId") Long quotationId, @Param("status") QuotationStatus status);

    @Modifying
    @Query("UPDATE Quotation q SET q.active = false WHERE q.id = :quotationId")
    void deactivateQuotation(@Param("quotationId") Long quotationId);

    // Approval workflow queries
    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator WHERE q.status IN ('SUBMITTED', 'UNDER_REVIEW') AND q.active = true ORDER BY q.submittedDate ASC")
    List<Quotation> findPendingApprovals();

    @Query("SELECT q FROM Quotation q WHERE q.approvedBy.id = :approverId AND q.active = true")
    List<Quotation> findApprovedBy(@Param("approverId") Long approverId);

    // Budget-related queries
    @Query("SELECT q FROM Quotation q WHERE q.totalAmount > q.project.remainingBudget AND q.active = true")
    List<Quotation> findQuotationsExceedingBudget();

    // Recent quotations
    @Query("SELECT q FROM Quotation q WHERE q.active = true ORDER BY q.createdDate DESC")
    List<Quotation> findRecentQuotations(Pageable pageable);

    // Department/manager statistics
    @Query("SELECT p.manager.department, COUNT(q), SUM(q.totalAmount) FROM Quotation q JOIN q.project p WHERE q.active = true AND p.manager.department IS NOT NULL GROUP BY p.manager.department")
    List<Object[]> getQuotationStatsByDepartment();

    @Query("SELECT q.creator.fullName, COUNT(q), SUM(q.totalAmount) FROM Quotation q WHERE q.active = true GROUP BY q.creator.id, q.creator.fullName")
    List<Object[]> getQuotationStatsByManager();

    // Find approved quotations without payments
    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.creator " +
            "WHERE q.status = 'APPROVED' AND q.active = true " +
            "AND NOT EXISTS (SELECT 1 FROM Payment p WHERE p.quotation.id = q.id AND p.active = true)")
    Page<Quotation> findApprovedQuotationsWithoutPayments(Pageable pageable);

    // Find quotations submitted before a certain date (for urgent approvals)
    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.createdBy " +
            "WHERE q.status = :status AND q.submittedDate < :submittedBefore AND q.active = true")
    Page<Quotation> findByStatusAndSubmittedDateBeforeWithProject(@Param("status") QuotationStatus status,
                                                                  @Param("submittedBefore") LocalDateTime submittedBefore,
                                                                  Pageable pageable);

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.createdBy " +
            "WHERE q.status IN :statuses AND q.active = true")
    Page<Quotation> findByStatusInWithProject(@Param("statuses") List<QuotationStatus> statuses, Pageable pageable);
}