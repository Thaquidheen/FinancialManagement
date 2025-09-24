package com.company.erp.document.repository;

import com.company.erp.document.entity.Document;
import com.company.erp.document.entity.DocumentCategory;
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
import java.util.Set;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Basic finder methods
    List<Document> findByDeletedFalse();

    Page<Document> findByDeletedFalse(Pageable pageable);

    Optional<Document> findByIdAndDeletedFalse(Long id);

    // Find by project
    List<Document> findByProjectIdAndDeletedFalse(Long projectId);

    Page<Document> findByProjectIdAndDeletedFalse(Long projectId, Pageable pageable);

    // Find by uploader
    List<Document> findByUploadedByIdAndDeletedFalse(Long uploaderId);

    Page<Document> findByUploadedByIdAndDeletedFalse(Long uploaderId, Pageable pageable);

    // Find by category
    List<Document> findByCategoryAndDeletedFalse(DocumentCategory category);

    Page<Document> findByCategoryAndDeletedFalse(DocumentCategory category, Pageable pageable);

    // Advanced search method
    @Query("SELECT DISTINCT d FROM Document d " +
            "LEFT JOIN d.tags t " +
            "LEFT JOIN DocumentMetadata dm ON dm.document.id = d.id " +
            "WHERE d.deleted = false " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(d.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(dm.extractedText) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:projectId IS NULL OR d.projectId = :projectId) " +
            "AND (:category IS NULL OR d.category = :category) " +
            "AND (:startDate IS NULL OR d.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR d.createdDate <= :endDate) " +
            "AND (:tags IS NULL OR t.name IN :tags) " +
            "AND ((:userRole = 'SUPER_ADMIN' OR :userRole = 'ACCOUNT_MANAGER') OR " +
            "     d.uploadedBy.id = :userId OR " +
            "     (:userRole = 'PROJECT_MANAGER' AND d.projectId IN " +
            "      (SELECT p.id FROM Project p WHERE p.manager.id = :userId)))")
    Page<Document> searchDocuments(@Param("searchTerm") String searchTerm,
                                   @Param("projectId") Long projectId,
                                   @Param("category") DocumentCategory category,
                                   @Param("tags") Set<String> tags,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("userId") Long userId,
                                   @Param("userRole") String userRole,
                                   Pageable pageable);

    // Recent documents
    @Query("SELECT d FROM Document d " +
            "WHERE d.deleted = false " +
            "AND ((:userRole = 'SUPER_ADMIN' OR :userRole = 'ACCOUNT_MANAGER') OR " +
            "     d.uploadedBy.id = :userId OR " +
            "     (:userRole = 'PROJECT_MANAGER' AND d.projectId IN " +
            "      (SELECT p.id FROM Project p WHERE p.manager.id = :userId))) " +
            "ORDER BY d.createdDate DESC")
    Page<Document> findRecentDocuments(@Param("userId") Long userId,
                                       @Param("userRole") String userRole,
                                       Pageable pageable);

    // Frequently accessed documents
    @Query("SELECT d FROM Document d " +
            "WHERE d.deleted = false " +
            "AND d.accessCount > 0 " +
            "AND ((:userRole = 'SUPER_ADMIN' OR :userRole = 'ACCOUNT_MANAGER') OR " +
            "     d.uploadedBy.id = :userId OR " +
            "     (:userRole = 'PROJECT_MANAGER' AND d.projectId IN " +
            "      (SELECT p.id FROM Project p WHERE p.manager.id = :userId))) " +
            "ORDER BY d.accessCount DESC, d.lastAccessedDate DESC")
    Page<Document> findFrequentlyAccessedDocuments(@Param("userId") Long userId,
                                                   @Param("userRole") String userRole,
                                                   Pageable pageable);

    // Statistics methods
    long countByDeletedFalse();

    long countByProjectIdAndDeletedFalse(Long projectId);

    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.deleted = false")
    Long sumFileSize();

    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.projectId = :projectId AND d.deleted = false")
    Long sumFileSizeByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT d.category, COUNT(d) FROM Document d WHERE d.deleted = false GROUP BY d.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT d.category, COUNT(d) FROM Document d " +
            "WHERE d.projectId = :projectId AND d.deleted = false GROUP BY d.category")
    List<Object[]> countByProjectIdGroupByCategory(@Param("projectId") Long projectId);

    @Query("SELECT FUNCTION('DATE_FORMAT', d.createdDate, '%Y-%m') as month, COUNT(d) " +
            "FROM Document d WHERE d.deleted = false " +
            "GROUP BY FUNCTION('DATE_FORMAT', d.createdDate, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Object[]> countUploadsByMonth();

    // Find documents by checksum (duplicate detection)
    Optional<Document> findByChecksumAndDeletedFalse(String checksum);

    List<Document> findByChecksumAndDeletedFalseAndIdNot(String checksum, Long excludeId);

    // Find by MIME type
    List<Document> findByMimeTypeAndDeletedFalse(String mimeType);

    // Find large files
    @Query("SELECT d FROM Document d WHERE d.fileSize > :minSize AND d.deleted = false ORDER BY d.fileSize DESC")
    List<Document> findLargeDocuments(@Param("minSize") Long minSize);
}