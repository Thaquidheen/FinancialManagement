package com.company.erp.notification.repository;

import com.company.erp.notification.entity.NotificationPreference;
import com.company.erp.notification.entity.NotificationType;
import com.company.erp.notification.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    // Basic finder methods
    Optional<NotificationPreference> findByIdAndActiveTrue(Long id);

    List<NotificationPreference> findByActiveTrue();

    // Find by user
    Optional<NotificationPreference> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT np FROM NotificationPreference np WHERE np.user.id = :userId AND np.active = true")
    Optional<NotificationPreference> findByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndActiveTrue(Long userId);

    // Find by preferences
    @Query("SELECT np FROM NotificationPreference np WHERE np.emailEnabled = :emailEnabled AND np.active = true")
    List<NotificationPreference> findByEmailEnabled(@Param("emailEnabled") Boolean emailEnabled);

    @Query("SELECT np FROM NotificationPreference np WHERE np.smsEnabled = :smsEnabled AND np.active = true")
    List<NotificationPreference> findBySmsEnabled(@Param("smsEnabled") Boolean smsEnabled);

    @Query("SELECT np FROM NotificationPreference np WHERE np.inAppEnabled = :inAppEnabled AND np.active = true")
    List<NotificationPreference> findByInAppEnabled(@Param("inAppEnabled") Boolean inAppEnabled);

    @Query("SELECT np FROM NotificationPreference np WHERE np.pushEnabled = :pushEnabled AND np.active = true")
    List<NotificationPreference> findByPushEnabled(@Param("pushEnabled") Boolean pushEnabled);

    // Find by notification type preferences
    @Query("SELECT np FROM NotificationPreference np JOIN np.enabledTypes et WHERE et = :type AND np.active = true")
    List<NotificationPreference> findByEnabledType(@Param("type") NotificationType type);

    @Query("SELECT np FROM NotificationPreference np JOIN np.enabledChannels ec WHERE ec = :channel AND np.active = true")
    List<NotificationPreference> findByEnabledChannel(@Param("channel") NotificationChannel channel);

    // Find by summary preferences
    @Query("SELECT np FROM NotificationPreference np WHERE np.dailySummaryEnabled = :enabled AND np.active = true")
    List<NotificationPreference> findByDailySummaryEnabled(@Param("enabled") Boolean enabled);

    @Query("SELECT np FROM NotificationPreference np WHERE np.weeklySummaryEnabled = :enabled AND np.active = true")
    List<NotificationPreference> findByWeeklySummaryEnabled(@Param("enabled") Boolean enabled);

    // Find by do not disturb settings
    @Query("SELECT np FROM NotificationPreference np WHERE np.doNotDisturbEnabled = :enabled AND np.active = true")
    List<NotificationPreference> findByDoNotDisturbEnabled(@Param("enabled") Boolean enabled);

    @Query("SELECT np FROM NotificationPreference np WHERE np.doNotDisturbEnabled = true " +
            "AND np.doNotDisturbStart <= :currentTime AND np.doNotDisturbEnd >= :currentTime " +
            "AND np.active = true")
    List<NotificationPreference> findInDoNotDisturbPeriod(@Param("currentTime") LocalTime currentTime);

    // Find by language and timezone
    @Query("SELECT np FROM NotificationPreference np WHERE np.language = :language AND np.active = true")
    List<NotificationPreference> findByLanguage(@Param("language") String language);

    @Query("SELECT np FROM NotificationPreference np WHERE np.timezone = :timezone AND np.active = true")
    List<NotificationPreference> findByTimezone(@Param("timezone") String timezone);

    // Update methods
    @Modifying
    @Query("UPDATE NotificationPreference np SET np.emailEnabled = :enabled WHERE np.user.id = :userId")
    void updateEmailEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.smsEnabled = :enabled WHERE np.user.id = :userId")
    void updateSmsEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.inAppEnabled = :enabled WHERE np.user.id = :userId")
    void updateInAppEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.pushEnabled = :enabled WHERE np.user.id = :userId")
    void updatePushEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.dailySummaryEnabled = :enabled WHERE np.user.id = :userId")
    void updateDailySummaryEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.weeklySummaryEnabled = :enabled WHERE np.user.id = :userId")
    void updateWeeklySummaryEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.doNotDisturbEnabled = :enabled, " +
            "np.doNotDisturbStart = :startTime, np.doNotDisturbEnd = :endTime WHERE np.user.id = :userId")
    void updateDoNotDisturbSettings(@Param("userId") Long userId,
                                    @Param("enabled") Boolean enabled,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.language = :language WHERE np.user.id = :userId")
    void updateLanguage(@Param("userId") Long userId, @Param("language") String language);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.timezone = :timezone WHERE np.user.id = :userId")
    void updateTimezone(@Param("userId") Long userId, @Param("timezone") String timezone);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.active = false WHERE np.user.id = :userId")
    void deactivateByUserId(@Param("userId") Long userId);

    // Statistics and analytics
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.emailEnabled = true AND np.active = true")
    long countByEmailEnabled();

    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.smsEnabled = true AND np.active = true")
    long countBySmsEnabled();

    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.inAppEnabled = true AND np.active = true")
    long countByInAppEnabled();

    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.pushEnabled = true AND np.active = true")
    long countByPushEnabled();

    @Query("SELECT np.language, COUNT(np) FROM NotificationPreference np WHERE np.active = true GROUP BY np.language")
    List<Object[]> getLanguageDistribution();

    @Query("SELECT np.timezone, COUNT(np) FROM NotificationPreference np WHERE np.active = true GROUP BY np.timezone")
    List<Object[]> getTimezoneDistribution();

    // Bulk operations
    @Modifying
    @Query("UPDATE NotificationPreference np SET np.emailEnabled = :enabled WHERE np.user.id IN :userIds")
    void bulkUpdateEmailEnabled(@Param("userIds") List<Long> userIds, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationPreference np SET np.active = false WHERE np.user.id IN :userIds")
    void bulkDeactivate(@Param("userIds") List<Long> userIds);
}