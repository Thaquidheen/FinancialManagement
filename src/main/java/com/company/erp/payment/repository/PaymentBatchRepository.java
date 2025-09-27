package com.company.erp.payment.repository;

import com.company.erp.payment.entity.PaymentBatch;
import com.company.erp.payment.entity.PaymentBatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, Long> {

    // Find by batch number
    Optional<PaymentBatch> findByBatchNumberAndActiveTrue(String batchNumber);

    // Find by status
    List<PaymentBatch> findByStatusAndActiveTrue(PaymentBatchStatus status);
    Page<PaymentBatch> findByStatusAndActiveTrue(PaymentBatchStatus status, Pageable pageable);

    // Find by bank
    List<PaymentBatch> findByBankNameAndActiveTrue(String bankName);
    Page<PaymentBatch> findByBankNameAndActiveTrue(String bankName, Pageable pageable);

    // FIXED: Use 'creator' instead of 'createdBy' since creator is the User entity
    List<PaymentBatch> findByCreatorIdAndActiveTrue(Long creatorId);
    Page<PaymentBatch> findByCreatorIdAndActiveTrue(Long creatorId, Pageable pageable);

    // Find all with details - eagerly fetch creator to avoid LazyInitializationException
    @Query("SELECT b FROM PaymentBatch b LEFT JOIN FETCH b.creator WHERE b.active = true")
    Page<PaymentBatch> findAllWithDetails(Pageable pageable);

    // Find all with payments - use separate queries to avoid Hibernate pagination issues
    @Query("SELECT DISTINCT b FROM PaymentBatch b LEFT JOIN FETCH b.creator WHERE b.active = true")
    Page<PaymentBatch> findAllWithPayments(Pageable pageable);

    // Find batches ready for download - eagerly fetch creator to avoid LazyInitializationException
    @Query("SELECT b FROM PaymentBatch b LEFT JOIN FETCH b.creator WHERE b.status = 'FILE_GENERATED' AND b.downloadedDate IS NULL AND b.active = true")
    List<PaymentBatch> findBatchesReadyForDownload();
}