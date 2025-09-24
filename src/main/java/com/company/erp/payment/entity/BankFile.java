package com.company.erp.payment.entity;

import com.company.erp.common.entity.AuditableEntity;
import com.company.erp.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_files")
public class BankFile extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private PaymentBatch batch;

    @NotNull
    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "last_downloaded_date")
    private LocalDateTime lastDownloadedDate;

    // Constructors
    public BankFile() {}

    public BankFile(PaymentBatch batch, String fileName, User generatedBy) {
        this.batch = batch;
        this.fileName = fileName;
        this.generatedBy = generatedBy;
        this.bankName = batch.getBankName();
    }

    // Business methods
    public void incrementDownloadCount() {
        this.downloadCount++;
        this.lastDownloadedDate = LocalDateTime.now();
    }

    public boolean hasBeenDownloaded() {
        return this.downloadCount > 0;
    }

    // Getters and Setters
    public PaymentBatch getBatch() {
        return batch;
    }

    public void setBatch(PaymentBatch batch) {
        this.batch = batch;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public User getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(User generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LocalDateTime getLastDownloadedDate() {
        return lastDownloadedDate;
    }

    public void setLastDownloadedDate(LocalDateTime lastDownloadedDate) {
        this.lastDownloadedDate = lastDownloadedDate;
    }

    @Override
    public String toString() {
        return "BankFile{" +
                "id=" + getId() +
                ", fileName='" + fileName + '\'' +
                ", bankName='" + bankName + '\'' +
                ", fileSize=" + fileSize +
                ", downloadCount=" + downloadCount +
                '}';
    }
}