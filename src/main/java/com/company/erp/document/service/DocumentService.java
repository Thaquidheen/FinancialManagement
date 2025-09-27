// Complete DocumentService.java
package com.company.erp.document.service;

import com.company.erp.common.exception.FileUploadException;
import com.company.erp.common.service.AuditService;
import com.company.erp.document.entity.Document;
import com.company.erp.document.entity.DocumentCategory;
import com.company.erp.document.entity.DocumentMetadata;
import com.company.erp.document.entity.DocumentTag;
import com.company.erp.document.repository.DocumentRepository;
import com.company.erp.document.repository.DocumentMetadataRepository;
import com.company.erp.document.repository.DocumentTagRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg", "image/jpg", "image/png", "image/gif",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "text/csv"
    );

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/gif");

    @Value("${app.document.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${app.document.compression-enabled:true}")
    private boolean compressionEnabled;

    @Value("${app.document.virus-scan-enabled:false}")
    private boolean virusScanEnabled;

    private final DocumentRepository documentRepository;
    private final DocumentMetadataRepository metadataRepository;
    private final DocumentTagRepository tagRepository;
    // private final FileStorageService fileStorageService; // Not used in current implementation
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final Tika tika = new Tika();

    public DocumentService(DocumentRepository documentRepository,
                           DocumentMetadataRepository metadataRepository,
                           DocumentTagRepository tagRepository,
                           UserRepository userRepository,
                           AuditService auditService) {
        this.documentRepository = documentRepository;
        this.metadataRepository = metadataRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public Document uploadDocument(MultipartFile file, Long projectId, Long uploadedById,
                                   DocumentCategory category, Set<String> tags) {
        validateFile(file);

        try {
            User uploader = userRepository.findById(uploadedById)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + uploadedById));

            // Create document entity
            Document document = new Document();
            document.setFileName(file.getOriginalFilename());
            document.setFileSize(file.getSize());
            document.setMimeType(detectMimeType(file));
            document.setProjectId(projectId);
            document.setUploadedBy(uploader);
            document.setCategory(category != null ? category : autoDetectCategory(file));
            document.setChecksum(calculateChecksum(file.getBytes()));

            // Process file content
            byte[] processedContent = processFileContent(file);
            document.setFileData(processedContent);
            document.setFileSize((long) processedContent.length);

            // Extract metadata
            DocumentMetadata metadata = extractMetadata(file, document);

            // Save document
            document = documentRepository.save(document);

            // Save metadata
            metadata.setDocument(document);
            metadataRepository.save(metadata);

            // Process and save tags
            if (tags != null && !tags.isEmpty()) {
                saveTags(document, tags);
            }

            // Auto-tag based on content
            autoTagDocument(document);

            // Extract text content for search (OCR for images)
            extractAndSaveTextContent(document, processedContent);

            // Audit log
            auditService.logAction(uploadedById, "DOCUMENT_UPLOADED", "DOCUMENT",
                    document.getId(), "Document uploaded: " + file.getOriginalFilename(),
                    null, document);

            logger.info("Document uploaded successfully: {} by user {}",
                    file.getOriginalFilename(), uploadedById);

            return document;

        } catch (Exception e) {
            logger.error("Failed to upload document {}: {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new FileUploadException("Failed to upload document: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileUploadException("File size exceeds maximum allowed size of " +
                    (maxFileSize / 1024 / 1024) + "MB");
        }

        String mimeType = detectMimeType(file);
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new FileUploadException("File type not allowed: " + mimeType);
        }

        if (virusScanEnabled && !isFileClean(file)) {
            throw new FileUploadException("File failed security scan");
        }
    }

    private String detectMimeType(MultipartFile file) {
        try {
            return tika.detect(file.getBytes(), file.getOriginalFilename());
        } catch (Exception e) {
            logger.warn("Failed to detect MIME type for {}, using content type: {}",
                    file.getOriginalFilename(), file.getContentType());
            return file.getContentType();
        }
    }

    private boolean isFileClean(MultipartFile file) {
        // Placeholder for virus scanning integration
        // In production, integrate with ClamAV or similar
        return true;
    }

    private DocumentCategory autoDetectCategory(MultipartFile file) {
        String fileName = file.getOriginalFilename().toLowerCase();
        String mimeType = detectMimeType(file);

        if (fileName.contains("invoice") || fileName.contains("bill")) {
            return DocumentCategory.INVOICE;
        } else if (fileName.contains("receipt")) {
            return DocumentCategory.RECEIPT;
        } else if (fileName.contains("contract") || fileName.contains("agreement")) {
            return DocumentCategory.CONTRACT;
        } else if (IMAGE_TYPES.contains(mimeType)) {
            return DocumentCategory.PHOTO;
        } else if (mimeType.equals("application/pdf")) {
            return DocumentCategory.DOCUMENT;
        } else {
            return DocumentCategory.OTHER;
        }
    }

    private byte[] processFileContent(MultipartFile file) throws IOException {
        byte[] originalContent = file.getBytes();

        if (!compressionEnabled) {
            return originalContent;
        }

        String mimeType = detectMimeType(file);

        // Compress images if they're too large
        if (IMAGE_TYPES.contains(mimeType) && originalContent.length > 1024 * 1024) { // 1MB
            return compressImage(originalContent, mimeType);
        }

        return originalContent;
    }

    private byte[] compressImage(byte[] imageData, String mimeType) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));

            // Calculate new dimensions (max 1920x1080)
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            double ratio = Math.min(1920.0 / originalWidth, 1080.0 / originalHeight);

            if (ratio >= 1.0) {
                return imageData; // No compression needed
            }

            int newWidth = (int) (originalWidth * ratio);
            int newHeight = (int) (originalHeight * ratio);

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            resizedImage.createGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = mimeType.equals("image/png") ? "png" : "jpg";
            ImageIO.write(resizedImage, format, baos);

            byte[] compressed = baos.toByteArray();
            logger.info("Image compressed from {} bytes to {} bytes", imageData.length, compressed.length);

            return compressed;

        } catch (Exception e) {
            logger.error("Failed to compress image: {}", e.getMessage());
            return imageData; // Return original if compression fails
        }
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Failed to calculate checksum: {}", e.getMessage());
            return null;
        }
    }

    private DocumentMetadata extractMetadata(MultipartFile file, Document document) {
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setOriginalFileName(file.getOriginalFilename());
        metadata.setFileSize(file.getSize());
        metadata.setMimeType(document.getMimeType());
        metadata.setUploadDate(LocalDateTime.now());

        try {
            // Extract additional metadata using Tika
            String extractedText = tika.parseToString(file.getInputStream());
            if (extractedText.length() > 1000) {
                extractedText = extractedText.substring(0, 1000) + "...";
            }
            metadata.setExtractedText(extractedText);
        } catch (Exception e) {
            logger.debug("Failed to extract text from {}: {}", file.getOriginalFilename(), e.getMessage());
        }

        return metadata;
    }

    private void saveTags(Document document, Set<String> tagNames) {
        Set<DocumentTag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {
                DocumentTag tag = tagRepository.findByNameIgnoreCase(tagName.trim())
                        .orElseGet(() -> {
                            DocumentTag newTag = new DocumentTag();
                            newTag.setName(tagName.trim().toLowerCase());
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
        }

        document.setTags(tags);
    }

    private void autoTagDocument(Document document) {
        String fileName = document.getFileName().toLowerCase();
        Set<String> autoTags = new HashSet<>();

        // Auto-tag based on filename patterns
        if (fileName.contains("invoice")) autoTags.add("invoice");
        if (fileName.contains("receipt")) autoTags.add("receipt");
        if (fileName.contains("contract")) autoTags.add("contract");
        if (fileName.contains("photo")) autoTags.add("photo");
        if (fileName.matches(".*\\d{4}-\\d{2}-\\d{2}.*")) autoTags.add("dated");

        // Auto-tag based on file type
        String mimeType = document.getMimeType();
        if (mimeType.equals("application/pdf")) autoTags.add("pdf");
        else if (IMAGE_TYPES.contains(mimeType)) autoTags.add("image");
        else if (mimeType.contains("spreadsheet") || mimeType.contains("excel")) autoTags.add("spreadsheet");

        if (!autoTags.isEmpty()) {
            Set<String> existingTagNames = document.getTags() != null ?
                    document.getTags().stream()
                            .map(DocumentTag::getName)
                            .collect(HashSet::new, Set::add, Set::addAll) :
                    new HashSet<>();

            autoTags.removeAll(existingTagNames);

            if (!autoTags.isEmpty()) {
                saveTags(document, autoTags);
            }
        }
    }

    private void extractAndSaveTextContent(Document document, byte[] fileData) {
        try {
            String textContent = tika.parseToString(new ByteArrayInputStream(fileData));

            if (textContent != null && !textContent.trim().isEmpty()) {
                DocumentMetadata metadata = metadataRepository.findByDocumentId(document.getId())
                        .orElseGet(() -> {
                            DocumentMetadata newMetadata = new DocumentMetadata();
                            newMetadata.setDocument(document);
                            return newMetadata;
                        });

                metadata.setExtractedText(textContent);
                metadata.setIndexed(true);
                metadataRepository.save(metadata);

                logger.debug("Extracted {} characters of text from {}",
                        textContent.length(), document.getFileName());
            }
        } catch (Exception e) {
            logger.debug("Failed to extract text content from {}: {}",
                    document.getFileName(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<Document> searchDocuments(String searchTerm, Long projectId, DocumentCategory category,
                                          Set<String> tags, LocalDateTime startDate, LocalDateTime endDate,
                                          Long userId, Pageable pageable) {

        // Implement role-based access control
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        String primaryRole = getPrimaryRole(user);
        return documentRepository.searchDocuments(searchTerm, projectId, category, tags,
                startDate, endDate, userId, primaryRole, pageable);
    }

    @Transactional(readOnly = true)
    public List<Document> getRecentDocuments(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        String primaryRole = getPrimaryRole(user);
        Page<Document> page = documentRepository.findRecentDocuments(userId, primaryRole, PageRequest.of(0, limit));
        return page.getContent();
    }

    @Transactional(readOnly = true)
    public List<Document> getFrequentlyAccessedDocuments(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        String primaryRole = getPrimaryRole(user);
        Page<Document> page = documentRepository.findFrequentlyAccessedDocuments(userId, primaryRole, PageRequest.of(0, limit));
        return page.getContent();
    }

    /**
     * Get the user's primary role based on hierarchy (highest privilege role)
     */
    private String getPrimaryRole(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(HashSet::new, Set::add, Set::addAll);

        // Return the highest privilege role in order of precedence
        if (roleNames.contains("SUPER_ADMIN")) {
            return "SUPER_ADMIN";
        } else if (roleNames.contains("ACCOUNT_MANAGER")) {
            return "ACCOUNT_MANAGER";
        } else if (roleNames.contains("PROJECT_MANAGER")) {
            return "PROJECT_MANAGER";
        } else if (roleNames.contains("EMPLOYEE")) {
            return "EMPLOYEE";
        } else {
            return "EMPLOYEE"; // Default role
        }
    }

    @Transactional(readOnly = true)
    public Document getDocument(Long documentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Check access permissions
        if (!canUserAccessDocument(user, document)) {
            throw new SecurityException("Access denied to document");
        }

        // Log document access
        auditService.logAction(userId, "DOCUMENT_ACCESSED", "DOCUMENT", documentId,
                "Document accessed: " + document.getFileName(), null, null);

        // Update access count and last access date
        document.setAccessCount(document.getAccessCount() + 1);
        document.setLastAccessedDate(LocalDateTime.now());
        documentRepository.save(document);

        return document;
    }

    public byte[] downloadDocument(Long documentId, Long userId) {
        Document document = getDocument(documentId, userId);

        // Log document download
        auditService.logAction(userId, "DOCUMENT_DOWNLOADED", "DOCUMENT", documentId,
                "Document downloaded: " + document.getFileName(), null, null);

        return document.getFileData();
    }

    public byte[] downloadMultipleDocuments(Set<Long> documentIds, Long userId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (Long documentId : documentIds) {
                Document document = getDocument(documentId, userId);

                ZipEntry entry = new ZipEntry(document.getFileName());
                zos.putNextEntry(entry);
                zos.write(document.getFileData());
                zos.closeEntry();
            }

            zos.close();

            // Log bulk download
            auditService.logAction(userId, "DOCUMENTS_BULK_DOWNLOADED", "DOCUMENT", null,
                    "Bulk download of " + documentIds.size() + " documents", null, null);

            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Failed to create ZIP for bulk download: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create download archive");
        }
    }

    public void deleteDocument(Long documentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Check delete permissions (only uploader or admin can delete)
        if (!canUserDeleteDocument(user, document)) {
            throw new SecurityException("Access denied to delete document");
        }

        // Soft delete - mark as deleted instead of removing
        document.setDeleted(true);
        document.setDeletedDate(LocalDateTime.now());
        document.setDeletedBy(user);
        documentRepository.save(document);

        // Log deletion
        auditService.logAction(userId, "DOCUMENT_DELETED", "DOCUMENT", documentId,
                "Document deleted: " + document.getFileName(), document, null);

        logger.info("Document {} deleted by user {}", document.getFileName(), userId);
    }

    public Document updateDocumentMetadata(Long documentId, Long userId, DocumentCategory category,
                                           Set<String> tags, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!canUserEditDocument(user, document)) {
            throw new SecurityException("Access denied to edit document");
        }

        Document oldDocument = new Document();
        // Copy old values for audit
        oldDocument.setCategory(document.getCategory());
        oldDocument.setDescription(document.getDescription());

        // Update fields
        if (category != null) {
            document.setCategory(category);
        }

        if (description != null) {
            document.setDescription(description);
        }

        if (tags != null) {
            saveTags(document, tags);
        }

//        document.setUpdatedDate(LocalDateTime.now());
        document = documentRepository.save(document);

        // Log update
        auditService.logAction(userId, "DOCUMENT_UPDATED", "DOCUMENT", documentId,
                "Document metadata updated: " + document.getFileName(), oldDocument, document);

        return document;
    }

    private boolean canUserAccessDocument(User user, Document document) {
        // Check if user has required roles using hasRole method
        if (user.hasRole("SUPER_ADMIN") || user.hasRole("ACCOUNT_MANAGER")) {
            return true;
        } else if (user.hasRole("PROJECT_MANAGER")) {
            // Can access documents from their projects or documents they uploaded
            return document.getUploadedBy().getId().equals(user.getId()) ||
                    isUserAssignedToProject(user.getId(), document.getProjectId());
        } else {
            return document.getUploadedBy().getId().equals(user.getId());
        }
    }

    private boolean canUserDeleteDocument(User user, Document document) {
        if (user.hasRole("SUPER_ADMIN")) {
            return true;
        } else if (user.hasRole("ACCOUNT_MANAGER") || user.hasRole("PROJECT_MANAGER")) {
            return document.getUploadedBy().getId().equals(user.getId());
        } else {
            return document.getUploadedBy().getId().equals(user.getId());
        }
    }

    private boolean canUserEditDocument(User user, Document document) {
        return canUserDeleteDocument(user, document);
    }

    private boolean isUserAssignedToProject(Long userId, Long projectId) {
        // Implementation would check project assignments
        // For now, return true as placeholder
        return true;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDocumentStatistics(Long projectId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Map<String, Object> stats = new HashMap<>();

        if (projectId != null) {
            stats.put("totalDocuments", documentRepository.countByProjectIdAndDeletedFalse(projectId));
            stats.put("totalSize", documentRepository.sumFileSizeByProjectId(projectId));
            stats.put("documentsByCategory", documentRepository.countByProjectIdGroupByCategory(projectId));
        } else {
            // System-wide statistics (admin only)
            if (user.hasRole("SUPER_ADMIN")) {
                stats.put("totalDocuments", documentRepository.countByDeletedFalse());
                stats.put("totalSize", documentRepository.sumFileSize());
                stats.put("documentsByCategory", documentRepository.countGroupByCategory());
                stats.put("uploadsByMonth", documentRepository.countUploadsByMonth());
            }
        }

        stats.put("recentUploads", getRecentDocuments(userId, 5));

        return stats;
    }
}

