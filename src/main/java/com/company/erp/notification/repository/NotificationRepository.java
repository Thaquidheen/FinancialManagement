package com.company.erp.notification.repository;

import com.company.erp.notification.entity.Notification;
import com.company.erp.notification.entity.NotificationType;
import com.company.erp.notification.entity.NotificationPriority;
import com.company.erp.notification.entity.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Basic finder methods
    Optional<Notification> findByIdAndActiveTrue(Long id);

    List<Notification> findByActiveTrue();

    // Find by user
    List<Notification> findByUserIdAndActiveTrue(Long userId);

    Page<Notification> findByUserIdAndActiveTrueOrderByCreatedDateDesc(Long userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findRecentNotificationsByUser(@Param("userId") Long userId, Pageable pageable);

    // Find by read status
    List<Notification> findByUserIdAndReadAndActiveTrue(Long userId, Boolean read);

    Page<Notification> findByUserIdAndReadAndActiveTrueOrderByCreatedDateDesc(Long userId, Boolean read, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false AND n.active = true")
    long countUnreadByUserId(@Param("userId") Long userId);

    // Find by notification type
    List<Notification> findByTypeAndActiveTrue(NotificationType type);

    List<Notification> findByUserIdAndTypeAndActiveTrue(Long userId, NotificationType type);

    Page<Notification> findByUserIdAndTypeAndActiveTrueOrderByCreatedDateDesc(Long userId, NotificationType type, Pageable pageable);

    // Find by priority
    List<Notification> findByPriorityAndActiveTrue(NotificationPriority priority);

    List<Notification> findByUserIdAndPriorityAndActiveTrue(Long userId, NotificationPriority priority);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.priority = :priority AND n.active = true " +
            "ORDER BY n.createdDate DESC")
    Page<Notification> findByUserIdAndPriorityWithPaging(@Param("userId") Long userId,
                                                         @Param("priority") NotificationPriority priority,
                                                         Pageable pageable);

    // Find by channel
    List<Notification> findByChannelAndActiveTrue(NotificationChannel channel);

    List<Notification> findByUserIdAndChannelAndActiveTrue(Long userId, NotificationChannel channel);

    // Find by sent status
    List<Notification> findBySentAndActiveTrue(Boolean sent);

    List<Notification> findByUserIdAndSentAndActiveTrue(Long userId, Boolean sent);

    @Query("SELECT n FROM Notification n WHERE n.sent = :sent AND n.active = true " +
            "ORDER BY n.createdDate ASC")
    List<Notification> findBySentOrderByCreatedDate(@Param("sent") Boolean sent, Pageable pageable);

    // Find pending notifications (scheduled but not sent)
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.scheduledTime <= :currentTime AND n.active = true " +
            "ORDER BY n.scheduledTime ASC")
    List<Notification> findBySentFalseAndScheduledTimeBefore(@Param("currentTime") LocalDateTime currentTime);

    List<Notification> findByScheduledTimeBeforeAndSentFalseAndActiveTrue(LocalDateTime scheduledTime);

    // Date-based queries
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdDate BETWEEN :startDate AND :endDate " +
            "AND n.active = true ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT n FROM Notification n WHERE n.createdDate >= :date AND n.active = true")
    List<Notification> findCreatedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdDate >= :date AND n.active = true " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdCreatedAfter(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    // Statistics and counts
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true")
    long countActiveNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.active = true")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type AND n.active = true")
    long countByType(@Param("type") NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.priority = :priority AND n.active = true")
    long countByPriority(@Param("priority") NotificationPriority priority);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.sent = :sent AND n.active = true")
    long countBySent(@Param("sent") Boolean sent);

    // Advanced search with multiple filters
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND (:type IS NULL OR n.type = :type) " +
            "AND (:priority IS NULL OR n.priority = :priority) " +
            "AND (:read IS NULL OR n.read = :read) " +
            "AND (:sent IS NULL OR n.sent = :sent) " +
            "AND (:startDate IS NULL OR n.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR n.createdDate <= :endDate) " +
            "ORDER BY n.createdDate DESC")
    Page<Notification> findByAdvancedSearch(@Param("userId") Long userId,
                                            @Param("type") NotificationType type,
                                            @Param("priority") NotificationPriority priority,
                                            @Param("read") Boolean read,
                                            @Param("sent") Boolean sent,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    // Title and message search
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY n.createdDate DESC")
    Page<Notification> findByUserIdAndSearchTerm(@Param("userId") Long userId,
                                                 @Param("searchTerm") String searchTerm,
                                                 Pageable pageable);

    // Update methods
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.read = false")
    void markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.sent = true, n.sentAt = :sentAt WHERE n.id = :id")
    void markAsSent(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);

    @Modifying
    @Query("UPDATE Notification n SET n.active = false WHERE n.id = :id")
    void deactivateNotification(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notification n SET n.active = false WHERE n.user.id = :userId")
    void deactivateByUserId(@Param("userId") Long userId);

    // Bulk operations
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.id IN :ids")
    void markAsReadBulk(@Param("ids") List<Long> ids, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.active = false WHERE n.id IN :ids")
    void deactivateBulk(@Param("ids") List<Long> ids);

    // Cleanup methods
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdDate < :cutoffDate AND n.read = true")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE Notification n SET n.active = false WHERE n.createdDate < :cutoffDate AND n.read = true")
    void deactivateOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Analytics queries
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.active = true GROUP BY n.type")
    List<Object[]> getNotificationCountsByType();

    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.active = true GROUP BY n.type")
    List<Object[]> getNotificationCountsByTypeForUser(@Param("userId") Long userId);

    @Query("SELECT n.priority, COUNT(n) FROM Notification n WHERE n.active = true GROUP BY n.priority")
    List<Object[]> getNotificationCountsByPriority();

    @Query("SELECT DATE(n.createdDate), COUNT(n) FROM Notification n WHERE n.active = true " +
            "AND n.createdDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(n.createdDate) ORDER BY DATE(n.createdDate)")
    List<Object[]> getDailyNotificationCounts(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // High priority unread notifications
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false " +
            "AND n.priority IN ('HIGH', 'CRITICAL') AND n.active = true " +
            "ORDER BY n.priority DESC, n.createdDate DESC")
    List<Notification> findHighPriorityUnreadByUserId(@Param("userId") Long userId);

    // Recent activity
    @Query("SELECT n FROM Notification n WHERE n.active = true " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findRecentActivity(Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND n.createdDate >= :since " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findRecentActivityByUser(@Param("userId") Long userId,
                                                @Param("since") LocalDateTime since);

    // New methods for enhanced notification management

    /**
     * Find notifications with filters
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND (:read IS NULL OR n.read = :read) " +
            "AND (:type IS NULL OR n.type = :type) " +
            "AND (:priority IS NULL OR n.priority = :priority) " +
            "ORDER BY n.createdDate DESC")
    Page<Notification> findByUserIdWithFilters(@Param("userId") Long userId,
                                              @Param("read") Boolean read,
                                              @Param("type") String type,
                                              @Param("priority") String priority,
                                              Pageable pageable);

    /**
     * Count total notifications by user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.active = true")
    long countByUserIdAndActiveTrue(@Param("userId") Long userId);

    /**
     * Count read notifications by user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.active = true AND n.read = true")
    long countReadByUserId(@Param("userId") Long userId);

    /**
     * Count today's notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND n.createdDate >= :startOfDay AND n.createdDate < :endOfDay")
    long countTodayByUserId(@Param("userId") Long userId, 
                           @Param("startOfDay") LocalDateTime startOfDay, 
                           @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Count this week's notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND n.createdDate >= :weekStart")
    long countThisWeekByUserId(@Param("userId") Long userId, @Param("weekStart") LocalDateTime weekStart);

    /**
     * Get notification type breakdown
     */
    @Query("SELECT n.type as type, COUNT(n) as count FROM Notification n " +
            "WHERE n.user.id = :userId AND n.active = true " +
            "GROUP BY n.type")
    List<Object[]> getNotificationTypeBreakdownRaw(@Param("userId") Long userId);

    default Map<String, Long> getNotificationTypeBreakdown(Long userId) {
        List<Object[]> results = getNotificationTypeBreakdownRaw(userId);
        Map<String, Long> breakdown = new HashMap<>();
        for (Object[] result : results) {
            breakdown.put(result[0].toString(), (Long) result[1]);
        }
        return breakdown;
    }

    /**
     * Find notifications by IDs and user ID
     */
    @Query("SELECT n FROM Notification n WHERE n.id IN :ids AND n.user.id = :userId AND n.active = true")
    List<Notification> findByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * Find notifications with filters for export
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "AND (:read IS NULL OR n.read = :read) " +
            "AND (:type IS NULL OR n.type = :type) " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdWithFiltersForExport(@Param("userId") Long userId,
                                                        @Param("read") Boolean read,
                                                        @Param("type") String type);

    /**
     * Find all active notifications by user (for export)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.active = true " +
            "ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdAndActiveTrueOrderByCreatedDateDesc(@Param("userId") Long userId);
}
