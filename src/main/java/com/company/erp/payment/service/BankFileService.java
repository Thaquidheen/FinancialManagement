package com.company.erp.payment.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.payment.dto.response.BankFileResponse;
import com.company.erp.payment.entity.BankFile;
import com.company.erp.payment.entity.PaymentBatch;
import com.company.erp.payment.repository.BankFileRepository;
import com.company.erp.payment.repository.PaymentBatchRepository;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List; // Import List

@Service
@Transactional
public class BankFileService {

    private static final Logger logger = LoggerFactory.getLogger(BankFileService.class);

    @Value("${app.file.upload-dir:/tmp/bank-files}")
    private String uploadDir;

    @Autowired
    private PaymentBatchRepository paymentBatchRepository;

    @Autowired
    private BankFileRepository bankFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SaudiBankFileGenerator bankFileGenerator;

    /**
     * Generate bank file and save to disk
     */
    public BankFileResponse generateBankFile(PaymentBatch batch) {
        logger.info("Generating and saving bank file for batch: {}", batch.getBatchNumber());

        try {
            // Generate the file content
            byte[] fileContent = bankFileGenerator.generateBankFile(batch, batch.getBankName());

            // Generate file name
            String fileName = bankFileGenerator.generateFileName(batch);

            // Save file to disk
            String filePath = saveFileToDisk(fileName, fileContent);

            // Update batch with file information
            batch.markAsGenerated(fileName, filePath);
            paymentBatchRepository.save(batch);

            // Create bank file record
            BankFile bankFile = createBankFileRecord(batch, fileName, filePath, fileContent.length);
            bankFileRepository.save(bankFile);

            // Create response
            BankFileResponse response = new BankFileResponse();
            response.setBatchId(batch.getId());
            response.setBatchNumber(batch.getBatchNumber());
            response.setFileName(fileName);
            response.setDownloadUrl("/api/payments/download-bank-file/" + batch.getId());
            response.setBankName(batch.getBankName());
            response.setPaymentCount(batch.getPaymentCount());
            response.setTotalAmount(batch.getTotalAmount());
            response.setCurrency(batch.getCurrency());
            response.setGeneratedDate(batch.getGeneratedDate());
            response.setStatus(batch.getStatus().name());

            logger.info("Bank file generated and saved successfully: {}", fileName);

            return response;

        } catch (Exception e) {
            logger.error("Error generating bank file for batch: {}", batch.getBatchNumber(), e);
            throw new BusinessException("BANK_FILE_GENERATION_ERROR",
                    "Failed to generate bank file: " + e.getMessage());
        }
    }

    /**
     * Download bank file content
     */
    @Transactional(readOnly = true)
    public byte[] downloadBankFile(Long batchId) {
        logger.info("Downloading bank file for batch: {}", batchId);

        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentBatch", "id", batchId));

        if (batch.getFilePath() == null) {
            throw new BusinessException("BANK_FILE_NOT_FOUND",
                    "Bank file not generated for batch: " + batch.getBatchNumber());
        }

        try {
            Path filePath = Paths.get(batch.getFilePath());
            byte[] fileContent = Files.readAllBytes(filePath);

            // Update download tracking
            batch.markAsDownloaded();
            paymentBatchRepository.save(batch);

            // Update bank file download count
                // Increment download count on the first active bank file for this batch
                bankFileRepository.findFirstByBatchIdAndActiveTrue(batchId)
                    .ifPresent(bankFile -> bankFileRepository.incrementDownloadCount(bankFile.getId()));

            logger.info("Bank file downloaded successfully for batch: {}", batchId);

            return fileContent;

        } catch (IOException e) {
            logger.error("Error reading bank file for batch: {}", batchId, e);
            throw new BusinessException("BANK_FILE_READ_ERROR",
                    "Failed to read bank file: " + e.getMessage());
        }
    }

    /**
     * Get bank file name
     */
    @Transactional(readOnly = true)
    public String getBankFileName(Long batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentBatch", "id", batchId));

        return batch.getFileName() != null ? batch.getFileName() :
                "payment_file_" + batch.getBatchNumber() + ".xlsx";
    }

    /**
     * Delete bank file
     */
    public void deleteBankFile(Long batchId) {
        logger.info("Deleting bank file for batch: {}", batchId);

        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentBatch", "id", batchId));

        if (batch.getFilePath() != null) {
            try {
                Path filePath = Paths.get(batch.getFilePath());
                Files.deleteIfExists(filePath);

                // Clear file path from batch
                batch.setFilePath(null);
                bankFileRepository.findFirstByBatchIdAndActiveTrue(batchId)
                    .ifPresent(bankFile -> {
                        bankFile.setActive(false);
                        bankFileRepository.save(bankFile);
                    });

                logger.info("Bank file deleted successfully for batch: {}", batchId);

            } catch (IOException e) {
                logger.error("Error deleting bank file for batch: {}", batchId, e);
                throw new BusinessException("BANK_FILE_DELETE_ERROR",
                        "Failed to delete bank file: " + e.getMessage());
            }
        }
    }

    /**
     * Check if bank file exists
     */
    @Transactional(readOnly = true)
    public boolean bankFileExists(Long batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId).orElse(null);
        if (batch == null || batch.getFilePath() == null) {
            return false;
        }

        Path filePath = Paths.get(batch.getFilePath());
        return Files.exists(filePath);
    }

    /**
     * Get file size
     */
    @Transactional(readOnly = true)
    public long getFileSize(Long batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentBatch", "id", batchId));

        if (batch.getFilePath() == null) {
            return 0;
        }

        try {
            Path filePath = Paths.get(batch.getFilePath());
            return Files.exists(filePath) ? Files.size(filePath) : 0;
        } catch (IOException e) {
            logger.warn("Error getting file size for batch: {}", batchId, e);
            return 0;
        }
    }

    // Private helper methods

    private String saveFileToDisk(String fileName, byte[] fileContent) throws IOException {
        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Create file path with timestamp to avoid conflicts
        String timestampedFileName = addTimestampToFileName(fileName);
        Path filePath = uploadPath.resolve(timestampedFileName);

        // Write file to disk
        Files.write(filePath, fileContent);

        logger.debug("File saved to disk: {}", filePath.toString());

        return filePath.toString();
    }

    private String addTimestampToFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName + "_" + System.currentTimeMillis();
        }

        String name = fileName.substring(0, dotIndex);
        String extension = fileName.substring(dotIndex);
        return name + "_" + System.currentTimeMillis() + extension;
    }

    private BankFile createBankFileRecord(PaymentBatch batch, String fileName,
                                          String filePath, long fileSize) {
        BankFile bankFile = new BankFile();
        bankFile.setBatch(batch);
        bankFile.setFileName(fileName);
        bankFile.setFilePath(filePath);
        bankFile.setFileSize(fileSize);
        bankFile.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        bankFile.setBankName(batch.getBankName());

        // Set generator from current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User generator = userRepository.findByUsername(currentUsername).orElse(null);
        if (generator != null) {
            bankFile.setGeneratedBy(generator);
        }

        return bankFile;
    }

    /**
     * Cleanup old bank files (can be called by scheduled task)
     */
    @Transactional
    public void cleanupOldBankFiles(int daysOld) {
        logger.info("Cleaning up bank files older than {} days", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        // This would need a custom repository method to find old files
        // For now, just log the cleanup attempt
        logger.info("Bank file cleanup completed for files older than {}", cutoffDate);
    }
}