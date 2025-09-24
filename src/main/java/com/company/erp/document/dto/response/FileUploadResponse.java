package com.company.erp.document.dto.response;

import com.company.erp.document.dto.request.FileMetadata;

import java.time.LocalDateTime;

public class FileUploadResponse {

    private boolean success;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private long fileSize;
    private String mimeType;
    private String checksum;
    private String thumbnailPath;
    private LocalDateTime uploadedAt;
    private String errorMessage;
    private FileMetadata metadata;

    // Constructors
    public FileUploadResponse() {}

    public FileUploadResponse(boolean success, String fileName) {
        this.success = success;
        this.fileName = fileName;
        this.uploadedAt = LocalDateTime.now();
    }

    // Static factory methods
    public static FileUploadResponse success(String fileName, String filePath, long fileSize) {
        FileUploadResponse response = new FileUploadResponse(true, fileName);
        response.setFilePath(filePath);
        response.setFileSize(fileSize);
        return response;
    }

    public static FileUploadResponse failure(String originalFileName, String errorMessage) {
        FileUploadResponse response = new FileUploadResponse(false, originalFileName);
        response.setErrorMessage(errorMessage);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public FileMetadata getMetadata() { return metadata; }
    public void setMetadata(FileMetadata metadata) { this.metadata = metadata; }

    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "success=" + success +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
