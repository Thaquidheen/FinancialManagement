package com.company.erp.notification.repository;

import com.company.erp.notification.entity.NotificationTemplate;
import com.company.erp.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    // Basic finder methods
    Optional<NotificationTemplate> findByIdAndActiveTrue(Long id);

    List<NotificationTemplate> findByActiveTrue();

    Page<NotificationTemplate> findByActiveTrueOrderByTypeAsc(Pageable pageable);

    // Find by notification type
    Optional<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.type = :type AND nt.active = true")
    Optional<NotificationTemplate> findByType(@Param("type") NotificationType type);

    List<NotificationTemplate> findByTypeInAndActiveTrue(List<NotificationType> types);

    boolean existsByTypeAndActiveTrue(NotificationType type);

    // Find by language
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.language = :language AND nt.active = true")
    List<NotificationTemplate> findByLanguage(@Param("language") String language);

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.type = :type AND nt.language = :language AND nt.active = true")
    Optional<NotificationTemplate> findByTypeAndLanguage(@Param("type") NotificationType type,
                                                         @Param("language") String language);

    // Find by enabled status
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.enabled = :enabled AND nt.active = true")
    List<NotificationTemplate> findByEnabled(@Param("enabled") Boolean enabled);

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.type = :type AND nt.enabled = true AND nt.active = true")
    Optional<NotificationTemplate> findByTypeAndEnabled(@Param("type") NotificationType type);

    // Search by content
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.active = true " +
            "AND (LOWER(nt.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(nt.emailTemplate) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(nt.smsTemplate) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<NotificationTemplate> findBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.active = true " +
            "AND LOWER(nt.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<NotificationTemplate> findByTitleContaining(@Param("title") String title);

    // Find by template fields existence
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.emailTemplate IS NOT NULL AND nt.active = true")
    List<NotificationTemplate> findWithEmailTemplate();

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.smsTemplate IS NOT NULL AND nt.active = true")
    List<NotificationTemplate> findWithSmsTemplate();

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.inAppTemplate IS NOT NULL AND nt.active = true")
    List<NotificationTemplate> findWithInAppTemplate();

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.pushTemplate IS NOT NULL AND nt.active = true")
    List<NotificationTemplate> findWithPushTemplate();

    // Advanced filtering
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.active = true " +
            "AND (:type IS NULL OR nt.type = :type) " +
            "AND (:language IS NULL OR nt.language = :language) " +
            "AND (:enabled IS NULL OR nt.enabled = :enabled) " +
            "ORDER BY nt.type ASC")
    Page<NotificationTemplate> findByAdvancedSearch(@Param("type") NotificationType type,
                                                    @Param("language") String language,
                                                    @Param("enabled") Boolean enabled,
                                                    Pageable pageable);

    // Update methods
    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.enabled = :enabled WHERE nt.id = :id")
    void updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.enabled = :enabled WHERE nt.type = :type")
    void updateEnabledByType(@Param("type") NotificationType type, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.title = :title WHERE nt.id = :id")
    void updateTitle(@Param("id") Long id, @Param("title") String title);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.emailTemplate = :template WHERE nt.id = :id")
    void updateEmailTemplate(@Param("id") Long id, @Param("template") String template);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.smsTemplate = :template WHERE nt.id = :id")
    void updateSmsTemplate(@Param("id") Long id, @Param("template") String template);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.inAppTemplate = :template WHERE nt.id = :id")
    void updateInAppTemplate(@Param("id") Long id, @Param("template") String template);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.pushTemplate = :template WHERE nt.id = :id")
    void updatePushTemplate(@Param("id") Long id, @Param("template") String template);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.active = false WHERE nt.id = :id")
    void deactivateTemplate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.active = false WHERE nt.type = :type")
    void deactivateByType(@Param("type") NotificationType type);

    // Statistics and analytics
    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.active = true")
    long countActiveTemplates();

    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.enabled = true AND nt.active = true")
    long countEnabledTemplates();

    @Query("SELECT nt.type, COUNT(nt) FROM NotificationTemplate nt WHERE nt.active = true GROUP BY nt.type")
    List<Object[]> getTemplateCountsByType();

    @Query("SELECT nt.language, COUNT(nt) FROM NotificationTemplate nt WHERE nt.active = true GROUP BY nt.language")
    List<Object[]> getTemplateCountsByLanguage();

    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.type = :type AND nt.active = true")
    long countByType(@Param("type") NotificationType type);

    // Validation queries
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.active = true " +
            "AND (nt.emailTemplate IS NULL OR nt.emailTemplate = '') " +
            "AND (nt.smsTemplate IS NULL OR nt.smsTemplate = '') " +
            "AND (nt.inAppTemplate IS NULL OR nt.inAppTemplate = '') " +
            "AND (nt.pushTemplate IS NULL OR nt.pushTemplate = '')")
    List<NotificationTemplate> findTemplatesWithoutContent();

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.enabled = true AND nt.active = true " +
            "AND nt.type NOT IN (SELECT DISTINCT n.type FROM Notification n WHERE n.active = true)")
    List<NotificationTemplate> findUnusedEnabledTemplates();

    // Bulk operations
    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.enabled = :enabled WHERE nt.type IN :types")
    void bulkUpdateEnabled(@Param("types") List<NotificationType> types, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.active = false WHERE nt.type IN :types")
    void bulkDeactivate(@Param("types") List<NotificationType> types);

    @Modifying
    @Query("UPDATE NotificationTemplate nt SET nt.language = :newLanguage WHERE nt.language = :oldLanguage")
    void bulkUpdateLanguage(@Param("oldLanguage") String oldLanguage, @Param("newLanguage") String newLanguage);
}