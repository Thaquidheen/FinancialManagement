package com.company.erp.document.repository;

import com.company.erp.document.entity.DocumentTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTagRepository extends JpaRepository<DocumentTag, Long> {

    Optional<DocumentTag> findByName(String name);

    Optional<DocumentTag> findByNameIgnoreCase(String name);

    List<DocumentTag> findByNameContainingIgnoreCase(String name);

    List<DocumentTag> findByDisplayNameContainingIgnoreCase(String displayName);

    // Find popular tags
    @Query("SELECT dt FROM DocumentTag dt ORDER BY dt.usageCount DESC")
    List<DocumentTag> findMostUsedTags(Pageable pageable);

    // Find tags by usage count range
    List<DocumentTag> findByUsageCountGreaterThan(Integer count);

    List<DocumentTag> findByUsageCountBetween(Integer minCount, Integer maxCount);

    // Search tags
    @Query("SELECT dt FROM DocumentTag dt WHERE " +
            "LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(dt.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(dt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentTag> searchTags(@Param("searchTerm") String searchTerm);

    // Find unused tags
    @Query("SELECT dt FROM DocumentTag dt WHERE dt.usageCount = 0 OR dt.usageCount IS NULL")
    List<DocumentTag> findUnusedTags();

    // Statistics
    @Query("SELECT COUNT(dt) FROM DocumentTag dt WHERE dt.usageCount > 0")
    long countUsedTags();

    @Query("SELECT SUM(dt.usageCount) FROM DocumentTag dt")
    long totalUsageCount();

    @Query("SELECT AVG(dt.usageCount) FROM DocumentTag dt WHERE dt.usageCount > 0")
    Double averageUsageCount();

    // Color-based queries
    List<DocumentTag> findByColor(String color);

    @Query("SELECT dt.color, COUNT(dt) FROM DocumentTag dt WHERE dt.color IS NOT NULL GROUP BY dt.color")
    List<Object[]> countByColor();
}