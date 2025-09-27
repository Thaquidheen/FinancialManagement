package com.company.erp.payment.repository;

import com.company.erp.payment.entity.Payment;
import com.company.erp.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByQuotationId(Long quotationId);

    Optional<Payment> findByQuotationId(Long quotationId);
    // Find payments by status
    List<Payment> findByStatusAndActiveTrue(PaymentStatus status);
    Page<Payment> findByStatusAndActiveTrue(PaymentStatus status, Pageable pageable);

    // Find payments with full details loaded
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH p.payee " +
            "WHERE p.status = :status AND p.active = true")
    Page<Payment> findByStatusWithDetails(@Param("status") PaymentStatus status, Pageable pageable);

    // Find payments with multiple statuses
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH p.payee " +
            "WHERE p.status IN :statuses AND p.active = true")
    Page<Payment> findByStatusInWithDetails(@Param("statuses") List<PaymentStatus> statuses, Pageable pageable);
    Page<Payment> findByStatusOrderByCreatedDateDesc(PaymentStatus status, Pageable pageable);
    // Find payments by payee

    List<Payment> findByPayeeIdAndActiveTrue(Long payeeId);
    Page<Payment> findByPayeeIdAndActiveTrue(Long payeeId, Pageable pageable);
    Page<Payment> findByBankNameContainingIgnoreCaseOrderByCreatedDateDesc(String bankName, Pageable pageable);
    // Find payments by bank
    List<Payment> findByBankNameAndActiveTrue(String bankName);
    Page<Payment> findByBankNameAndActiveTrue(String bankName, Pageable pageable);

    // Find payments by batch
    List<Payment> findByBatchIdAndActiveTrue(Long batchId);

    // Find payments by batch with quotation and project details for serialization
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.quotation q LEFT JOIN FETCH q.project pr LEFT JOIN FETCH pr.manager WHERE p.batch.id = :batchId AND p.active = true")
    List<Payment> findByBatchIdWithDetails(@Param("batchId") Long batchId);

    // Count payments by status
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.active = true")
    long countByStatus(@Param("status") PaymentStatus status);

    // Get total amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.active = true")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);

    // Find failed payments that can be retried
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < 3 AND p.active = true")
    List<Payment> findFailedPaymentsForRetry();
    List<Payment> findByBatchIdOrderByCreatedDateDesc(Long batchId);
    @Query("SELECT p FROM Payment p WHERE p.createdDate BETWEEN :startDate AND :endDate ORDER BY p.createdDate DESC")
    Page<Payment> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    // Find payments ready for processing by bank
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.quotation q LEFT JOIN FETCH q.project " +
            "WHERE p.status IN ('PENDING', 'READY_FOR_PAYMENT') AND p.bankName = :bankName AND p.active = true " +
            "ORDER BY p.createdDate ASC")
    List<Payment> findPaymentsReadyForBank(@Param("bankName") String bankName);
}