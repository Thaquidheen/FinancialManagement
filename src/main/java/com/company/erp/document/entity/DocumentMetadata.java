package com.company.erp.document.entity;

import com.company.erp.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_metadata")
public class DocumentMetadata extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "indexed", nullable = false)
    private Boolean indexed = false;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "author")
    private String author;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "modification_date")
    private LocalDateTime modificationDate;

    @Column(name = "application")
    private String application;

    @Column(name = "language")
    private String language;

    // Constructors
    public DocumentMetadata() {}

    public DocumentMetadata(Document document) {
        this.document = document;
        this.uploadDate = LocalDateTime.now();
    }

    // Business methods
    public boolean hasText() {
        return extractedText != null && !extractedText.trim().isEmpty();
    }

    public boolean isImage() {
        return imageWidth != null && imageHeight != null;
    }

    public String getDimensions() {
        if (imageWidth != null && imageHeight != null) {
            return imageWidth + "x" + imageHeight;
        }
        return null;
    }

    // Getters and Setters
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public Boolean getIndexed() { return indexed; }
    public void setIndexed(Boolean indexed) { this.indexed = indexed; }

    public Integer getImageWidth() { return imageWidth; }
    public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }

    public Integer getImageHeight() { return imageHeight; }
    public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public LocalDateTime getModificationDate() { return modificationDate; }
    public void setModificationDate(LocalDateTime modificationDate) { this.modificationDate = modificationDate; }

    public String getApplication() { return application; }
    public void setApplication(String application) { this.application = application; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
