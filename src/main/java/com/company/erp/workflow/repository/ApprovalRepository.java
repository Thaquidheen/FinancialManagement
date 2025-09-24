package com.company.erp.workflow.repository;

import com.company.erp.workflow.entity.Approval;
import com.company.erp.workflow.entity.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // Find approvals by quotation
    List<Approval> findByQuotationIdOrderByCreatedDateDesc(Long quotationId);

    Optional<Approval> findByQuotationIdAndApproverId(Long quotationId, Long approverId);

    // Find approvals by approver
    List<Approval> findByApproverIdAndActiveTrue(Long approverId);

    Page<Approval> findByApproverIdAndActiveTrue(Long approverId, Pageable pageable);

    // Find approvals by status
    List<Approval> findByStatusAndActiveTrue(ApprovalStatus status);

    Page<Approval> findByStatusAndActiveTrue(ApprovalStatus status, Pageable pageable);

    // Find approvals by approver and status
    List<Approval> findByApproverIdAndStatusAndActiveTrue(Long approverId, ApprovalStatus status);

    // Count queries for statistics
    @Query("SELECT COUNT(a) FROM Approval a WHERE a.approver.id = :approverId AND a.status = :status AND a.active = true")
    long countByApproverIdAndStatus(@Param("approverId") Long approverId, @Param("status") ApprovalStatus status);

    @Query("SELECT COUNT(a) FROM Approval a WHERE a.status = :status AND a.active = true")
    long countByStatus(@Param("status") ApprovalStatus status);

    // Find pending approvals for a specific approver
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.quotation LEFT JOIN FETCH a.quotation.project " +
            "WHERE a.approver.id = :approverId AND a.status = 'PENDING' AND a.active = true " +
            "ORDER BY a.createdDate ASC")
    List<Approval> findPendingApprovalsByApprover(@Param("approverId") Long approverId);

    // Find all pending approvals with quotation details
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH q.createdBy " +
            "WHERE a.status = 'PENDING' AND a.active = true ORDER BY q.submittedDate ASC")
    Page<Approval> findAllPendingApprovalsWithDetails(Pageable pageable);

    // Find urgent approvals (pending for more than specified days)
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.quotation q LEFT JOIN FETCH q.project " +
            "WHERE a.status = 'PENDING' AND a.active = true AND a.createdDate < :threshold " +
            "ORDER BY a.createdDate ASC")
    Page<Approval> findUrgentApprovals(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    // Get approvals within date range
    @Query("SELECT a FROM Approval a WHERE a.approver.id = :approverId " +
            "AND a.createdDate BETWEEN :startDate AND :endDate AND a.active = true " +
            "ORDER BY a.createdDate DESC")
    List<Approval> findByApproverAndDateRange(@Param("approverId") Long approverId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Calculate average processing time
    @Query("SELECT AVG(DATEDIFF(day, a.createdDate, a.approvalDate)) FROM Approval a " +
            "WHERE a.approver.id = :approverId AND a.approvalDate IS NOT NULL AND a.active = true")
    Double getAverageProcessingDays(@Param("approverId") Long approverId);

    // Get approval workload by approver
    @Query("SELECT a.approver.id, a.approver.fullName, COUNT(a) as approvalCount " +
            "FROM Approval a WHERE a.status = 'PENDING' AND a.active = true " +
            "GROUP BY a.approver.id, a.approver.fullName ORDER BY approvalCount DESC")
    List<Object[]> getApprovalWorkloadByApprover();

    // Find approvals by quotation status
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.quotation q " +
            "WHERE q.status = :quotationStatus AND a.active = true " +
            "ORDER BY a.createdDate DESC")
    Page<Approval> findByQuotationStatus(@Param("quotationStatus") String quotationStatus, Pageable pageable);

    // Get monthly approval statistics
    @Query("SELECT YEAR(a.approvalDate) as year, MONTH(a.approvalDate) as month, " +
            "COUNT(CASE WHEN a.status = 'APPROVED' THEN 1 END) as approved, " +
            "COUNT(CASE WHEN a.status = 'REJECTED' THEN 1 END) as rejected, " +
            "COUNT(CASE WHEN a.status = 'CHANGES_REQUESTED' THEN 1 END) as changesRequested " +
            "FROM Approval a WHERE a.approvalDate IS NOT NULL AND a.active = true " +
            "GROUP BY YEAR(a.approvalDate), MONTH(a.approvalDate) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyApprovalStatistics();

    // Find approvals requiring escalation
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.quotation q LEFT JOIN FETCH q.project " +
            "WHERE a.status = 'PENDING' AND a.active = true " +
            "AND a.createdDate < :escalationThreshold " +
            "ORDER BY a.createdDate ASC")
    List<Approval> findApprovalsRequiringEscalation(@Param("escalationThreshold") LocalDateTime escalationThreshold);

    // Custom search with multiple filters
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.quotation q LEFT JOIN FETCH q.project p " +
            "WHERE (:approverId IS NULL OR a.approver.id = :approverId) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:projectId IS NULL OR p.id = :projectId) " +
            "AND (:startDate IS NULL OR a.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR a.createdDate <= :endDate) " +
            "AND a.active = true " +
            "ORDER BY a.createdDate DESC")
    Page<Approval> findBySearchCriteria(@Param("approverId") Long approverId,
                                        @Param("status") ApprovalStatus status,
                                        @Param("projectId") Long projectId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);
}