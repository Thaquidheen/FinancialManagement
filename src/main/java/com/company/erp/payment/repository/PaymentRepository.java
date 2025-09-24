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
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payments by status
    List<Payment> findByStatusAndActiveTrue(PaymentStatus status);
    Page<Payment> findByStatusAndActiveTrue(PaymentStatus status, Pageable pageable);

    // Find payments with full details loaded
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.quotation q LEFT JOIN FETCH q.project LEFT JOIN FETCH p.payee " +
            "WHERE p.status = :status AND p.active = true")
    Page<Payment> findByStatusWithDetails(@Param("status") PaymentStatus status, Pageable pageable);

    // Find payments by payee
    List<Payment> findByPayeeIdAndActiveTrue(Long payeeId);
    Page<Payment> findByPayeeIdAndActiveTrue(Long payeeId, Pageable pageable);

    // Find payments by bank
    List<Payment> findByBankNameAndActiveTrue(String bankName);
    Page<Payment> findByBankNameAndActiveTrue(String bankName, Pageable pageable);

    // Find payments by batch
    List<Payment> findByBatchIdAndActiveTrue(Long batchId);

    // Count payments by status
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.active = true")
    long countByStatus(@Param("status") PaymentStatus status);

    // Get total amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.active = true")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);

    // Find failed payments that can be retried
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < 3 AND p.active = true")
    List<Payment> findFailedPaymentsForRetry();

    // Find payments ready for processing by bank
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.quotation q LEFT JOIN FETCH q.project " +
            "WHERE p.status IN ('PENDING', 'READY_FOR_PAYMENT') AND p.bankName = :bankName AND p.active = true " +
            "ORDER BY p.createdDate ASC")
    List<Payment> findPaymentsReadyForBank(@Param("bankName") String bankName);
}