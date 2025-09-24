package com.company.erp.document.repository;

import com.company.erp.document.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

    Optional<DocumentMetadata> findByDocumentId(Long documentId);

    List<DocumentMetadata> findByIndexedTrue();

    List<DocumentMetadata> findByIndexedFalse();

    // Full-text search in extracted text
    @Query("SELECT dm FROM DocumentMetadata dm " +
            "WHERE dm.indexed = true " +
            "AND LOWER(dm.extractedText) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentMetadata> searchInExtractedText(@Param("searchTerm") String searchTerm);

    // Find documents by author
    List<DocumentMetadata> findByAuthorContainingIgnoreCase(String author);

    // Find documents by application
    List<DocumentMetadata> findByApplicationContainingIgnoreCase(String application);

    // Find documents with specific dimensions
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.imageWidth >= :minWidth AND dm.imageHeight >= :minHeight")
    List<DocumentMetadata> findByMinimumDimensions(@Param("minWidth") Integer minWidth, @Param("minHeight") Integer minHeight);

    // Find documents by page count
    List<DocumentMetadata> findByPageCountGreaterThan(Integer pageCount);

    // Statistics
    @Query("SELECT COUNT(dm) FROM DocumentMetadata dm WHERE dm.extractedText IS NOT NULL AND dm.extractedText != ''")
    long countWithExtractedText();

    @Query("SELECT COUNT(dm) FROM DocumentMetadata dm WHERE dm.indexed = true")
    long countIndexed();

    @Query("SELECT dm.mimeType, COUNT(dm) FROM DocumentMetadata dm GROUP BY dm.mimeType")
    List<Object[]> countByMimeType();

    @Query("SELECT AVG(dm.pageCount) FROM DocumentMetadata dm WHERE dm.pageCount IS NOT NULL")
    Double averagePageCount();
}
