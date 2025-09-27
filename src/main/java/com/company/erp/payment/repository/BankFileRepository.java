package com.company.erp.payment.repository;

import com.company.erp.payment.entity.BankFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankFileRepository extends JpaRepository<BankFile, Long> {

    // Find by batch
    List<BankFile> findByBatchIdAndActiveTrue(Long batchId);
    Optional<BankFile> findFirstByBatchIdAndActiveTrue(Long batchId);

    // Find by bank name
    List<BankFile> findByBankNameAndActiveTrue(String bankName);

    // Find by generator
    List<BankFile> findByGeneratedByIdAndActiveTrue(Long generatedById);

    // Update download count
    @Modifying
    @Query("UPDATE BankFile b SET b.downloadCount = b.downloadCount + 1, b.lastDownloadedDate = CURRENT_TIMESTAMP " +
            "WHERE b.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
}