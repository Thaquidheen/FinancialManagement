// =============================================================================
// ReportRepository.java - Report Configuration Repository
// =============================================================================

package com.company.erp.report.repository;

import com.company.erp.report.entity.ReportConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<ReportConfig, Long> {

    // Basic finder methods
    Optional<ReportConfig> findByIdAndActiveTrue(Long id);

    List<ReportConfig> findByActiveTrueOrderByNameAsc();

    // Find by report type
    List<ReportConfig> findByReportTypeAndActiveTrue(String reportType);

    @Query("SELECT rc FROM ReportConfig rc WHERE rc.reportType = :reportType AND rc.active = true ORDER BY rc.name ASC")
    List<ReportConfig> findByReportType(@Param("reportType") String reportType);

    // Find by category - both List and Page versions
    List<ReportConfig> findByCategoryAndActiveTrue(String category);

    @Query("SELECT rc FROM ReportConfig rc WHERE rc.category = :category AND rc.active = true ORDER BY rc.name ASC")
    List<ReportConfig> findByCategory(@Param("category") String category);

    // Pageable versions for ReportService
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.category = :category AND rc.active = true ORDER BY rc.name ASC")
    Page<ReportConfig> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT rc FROM ReportConfig rc WHERE rc.isPublic = :isPublic AND rc.active = true ORDER BY rc.name ASC")
    Page<ReportConfig> findByIsPublic(@Param("isPublic") Boolean isPublic, Pageable pageable);

    @Query("SELECT rc FROM ReportConfig rc WHERE rc.category = :category AND rc.isPublic = :isPublic AND rc.active = true ORDER BY rc.name ASC")
    Page<ReportConfig> findByCategoryAndIsPublic(@Param("category") String category, @Param("isPublic") Boolean isPublic, Pageable pageable);

    // Find public reports
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.isPublic = true AND rc.active = true ORDER BY rc.name ASC")
    List<ReportConfig> findPublicReports();

    // Find scheduled reports
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.isScheduled = true AND rc.active = true ORDER BY rc.name ASC")
    List<ReportConfig> findScheduledReports();

    @Query("SELECT rc FROM ReportConfig rc WHERE rc.isScheduled = true AND rc.scheduleFrequency = :frequency AND rc.active = true")
    List<ReportConfig> findScheduledReportsByFrequency(@Param("frequency") String frequency);

    // Find by name
    Optional<ReportConfig> findByNameAndActiveTrue(String name);

    @Query("SELECT rc FROM ReportConfig rc WHERE LOWER(rc.name) LIKE LOWER(CONCAT('%', :name, '%')) AND rc.active = true")
    List<ReportConfig> findByNameContaining(@Param("name") String name);

    // Find by output format
    List<ReportConfig> findByOutputFormatAndActiveTrue(String outputFormat);

    // Statistics
    @Query("SELECT COUNT(rc) FROM ReportConfig rc WHERE rc.active = true")
    long countActiveReports();

    @Query("SELECT COUNT(rc) FROM ReportConfig rc WHERE rc.reportType = :reportType AND rc.active = true")
    long countByReportType(@Param("reportType") String reportType);

    @Query("SELECT rc.category, COUNT(rc) FROM ReportConfig rc WHERE rc.active = true GROUP BY rc.category")
    List<Object[]> getReportCountsByCategory();

    @Query("SELECT rc.reportType, COUNT(rc) FROM ReportConfig rc WHERE rc.active = true GROUP BY rc.reportType")
    List<Object[]> getReportCountsByType();

    // Advanced search
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.active = true " +
            "AND (:reportType IS NULL OR rc.reportType = :reportType) " +
            "AND (:category IS NULL OR rc.category = :category) " +
            "AND (:isPublic IS NULL OR rc.isPublic = :isPublic) " +
            "AND (:isScheduled IS NULL OR rc.isScheduled = :isScheduled) " +
            "ORDER BY rc.name ASC")
    List<ReportConfig> findByAdvancedSearch(@Param("reportType") String reportType,
                                            @Param("category") String category,
                                            @Param("isPublic") Boolean isPublic,
                                            @Param("isScheduled") Boolean isScheduled);

    // Find by template usage
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.templatePath IS NOT NULL AND rc.active = true")
    List<ReportConfig> findReportsWithTemplate();

    @Query("SELECT rc FROM ReportConfig rc WHERE rc.templatePath = :templatePath AND rc.active = true")
    List<ReportConfig> findByTemplatePath(@Param("templatePath") String templatePath);

    // Recent reports
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.active = true ORDER BY rc.lastModifiedDate DESC")
    List<ReportConfig> findRecentlyModified(org.springframework.data.domain.Pageable pageable);

    // Cleanup methods
    @Query("SELECT rc FROM ReportConfig rc WHERE rc.isScheduled = false AND rc.lastModifiedDate < :cutoffDate")
    List<ReportConfig> findUnusedReports(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    List<ReportConfig> findByIsPublic(Boolean isPublic);
}