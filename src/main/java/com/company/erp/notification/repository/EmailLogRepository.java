// =============================================================================
// EmailLogRepository.java - Email Logging Repository
// =============================================================================

package com.company.erp.notification.repository;

import com.company.erp.notification.entity.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // Count methods for statistics
    long countBySuccess(Boolean success);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE DATE(e.sentAt) = CURRENT_DATE")
    long countTodayEmails();

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.sentAt >= :startOfWeek")
    long countThisWeekEmails(@Param("startOfWeek") LocalDateTime startOfWeek);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.sentAt >= :startOfMonth")
    long countThisMonthEmails(@Param("startOfMonth") LocalDateTime startOfMonth);

    // Find methods
    List<EmailLog> findByRecipientAndSuccess(String recipient, Boolean success);

    Page<EmailLog> findBySuccessOrderBySentAtDesc(Boolean success, Pageable pageable);

    @Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :start AND :end ORDER BY e.sentAt DESC")
    List<EmailLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Cleanup old logs
    @Query("DELETE FROM EmailLog e WHERE e.sentAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Default implementations for week and month
    default long countThisWeekEmails() {
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
        return countThisWeekEmails(startOfWeek);
    }

    default long countThisMonthEmails() {
        LocalDateTime startOfMonth = LocalDateTime.now().minusDays(30);
        return countThisMonthEmails(startOfMonth);
    }
}
