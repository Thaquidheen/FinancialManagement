// SmsLogRepository.java
package com.company.erp.notification.repository;

import com.company.erp.notification.entity.SmsLog;
import com.company.erp.notification.entity.SmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

    Page<SmsLog> findByPhoneNumberOrderBySentAtDesc(String phoneNumber, Pageable pageable);

    Page<SmsLog> findByStatusOrderBySentAtDesc(SmsStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SmsLog s WHERE s.status = :status AND s.sentAt >= :since")
    long countByStatusAndSentAtAfter(@Param("status") SmsStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT s FROM SmsLog s WHERE s.sentAt BETWEEN :startDate AND :endDate ORDER BY s.sentAt DESC")
    Page<SmsLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);
}