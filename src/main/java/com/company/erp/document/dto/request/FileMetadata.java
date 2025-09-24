package com.company.erp.document.dto.request;



import java.time.Instant;

public class FileMetadata {

    private String fileName;
    private String originalFileName;
    private String filePath;
    private long fileSize;
    private long originalFileSize;
    private String mimeType;
    private String checksum;
    private String thumbnailPath;
    private Instant createdDate;
    private Instant modifiedDate;

    // Constructors
    public FileMetadata() {}

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getOriginalFileSize() { return originalFileSize; }
    public void setOriginalFileSize(long originalFileSize) { this.originalFileSize = originalFileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public Instant getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Instant modifiedDate) { this.modifiedDate = modifiedDate; }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}