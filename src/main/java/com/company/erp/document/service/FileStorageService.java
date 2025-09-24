package com.company.erp.document.service;

import com.company.erp.common.exception.FileStorageException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.document.dto.request.FileMetadata;
import com.company.erp.document.dto.response.FileUploadResponse;
import com.company.erp.document.entity.Document;
import com.company.erp.document.entity.StorageType;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Tika tika = new Tika();

    // Configuration properties
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${app.file.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt}")
    private String[] allowedTypes;

    @Value("${app.file.storage-type:local}") // local, s3, azure
    private String storageType;

    @Value("${app.file.virus-scan-enabled:false}")
    private boolean virusScanEnabled;

    @Value("${app.file.compression-enabled:true}")
    private boolean compressionEnabled;

    @Value("${app.file.image-compression-quality:0.8}")
    private float imageCompressionQuality;

    @Value("${app.file.generate-thumbnails:true}")
    private boolean generateThumbnails;

    @Value("${app.file.thumbnail-size:200}")
    private int thumbnailSize;

    private Path fileStorageLocation;
    private Set<String> allowedMimeTypes;
    private Set<String> allowedExtensions;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);

            // Initialize allowed types
            this.allowedExtensions = new HashSet<>(Arrays.asList(allowedTypes));
            this.allowedMimeTypes = initializeAllowedMimeTypes();

            logger.info("File storage initialized at: {}", this.fileStorageLocation);
            logger.info("Storage type: {}", storageType);
            logger.info("Max file size: {} MB", maxFileSize / 1024 / 1024);
            logger.info("Allowed extensions: {}", allowedExtensions);

        } catch (IOException e) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored", e);
        }
    }

    /**
     * Store a single file
     */
    public FileUploadResponse storeFile(MultipartFile file, String category, Long userId) {
        validateFile(file);

        try {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFileName = generateUniqueFileName(fileName);

            // Create directory structure based on date and category
            Path targetLocation = createDirectoryStructure(category);
            Path filePath = targetLocation.resolve(uniqueFileName);

            // Process file content (compression, virus scan, etc.)
            byte[] processedContent = processFileContent(file);

            // Store file
            Files.write(filePath, processedContent, StandardOpenOption.CREATE);

            // Generate thumbnail for images
            String thumbnailPath = null;
            if (isImageFile(file) && generateThumbnails) {
                thumbnailPath = generateThumbnail(filePath, processedContent);
            }

            // Create file metadata
            FileMetadata metadata = createFileMetadata(file, filePath, processedContent, thumbnailPath);

            // Create response
            FileUploadResponse response = new FileUploadResponse();
            response.setSuccess(true);
            response.setFileName(uniqueFileName);
            response.setOriginalFileName(fileName);
            response.setFilePath(filePath.toString());
            response.setFileSize(processedContent.length);
            response.setMimeType(metadata.getMimeType());
            response.setChecksum(metadata.getChecksum());
            response.setThumbnailPath(thumbnailPath);
            response.setUploadedAt(LocalDateTime.now());
            response.setMetadata(metadata);

            logger.info("File stored successfully: {} ({})", uniqueFileName, formatFileSize(processedContent.length));

            return response;

        } catch (IOException e) {
            logger.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Store multiple files
     */
    public List<FileUploadResponse> storeFiles(MultipartFile[] files, String category, Long userId) {
        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    FileUploadResponse response = storeFile(file, category, userId);
                    responses.add(response);
                } catch (Exception e) {
                    logger.error("Failed to store file: {}", file.getOriginalFilename(), e);

                    FileUploadResponse errorResponse = new FileUploadResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setOriginalFileName(file.getOriginalFilename());
                    errorResponse.setErrorMessage(e.getMessage());
                    responses.add(errorResponse);
                }
            }
        }

        return responses;
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(String fileName, String category) {
        try {
            Path filePath = resolveFilePath(fileName, category);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                logger.debug("Loading file: {}", filePath);
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + fileName + " (Invalid file path: " + e.getMessage() + ")");
        }
    }

    /**
     * Get file content as byte array
     */
    public byte[] getFileContent(String fileName, String category) {
        try {
            Path filePath = resolveFilePath(fileName, category);

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }

            byte[] content = Files.readAllBytes(filePath);

            // Decompress if needed
            if (isCompressed(fileName)) {
                content = decompressFile(content);
            }

            return content;

        } catch (IOException e) {
            logger.error("Failed to read file: {}", fileName, e);
            throw new FileStorageException("Could not read file: " + fileName, e);
        }
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String fileName, String category) {
        try {
            Path filePath = resolveFilePath(fileName, category);

            boolean deleted = Files.deleteIfExists(filePath);

            // Also delete thumbnail if exists
            String thumbnailFileName = getThumbnailFileName(fileName);
            Path thumbnailPath = resolveFilePath(thumbnailFileName, category + "/thumbnails");
            Files.deleteIfExists(thumbnailPath);

            if (deleted) {
                logger.info("File deleted: {}", filePath);
            } else {
                logger.warn("File not found for deletion: {}", filePath);
            }

            return deleted;

        } catch (IOException e) {
            logger.error("Failed to delete file: {}", fileName, e);
            return false;
        }
    }

    /**
     * Move file from temporary location to permanent storage
     */
    public String moveFile(String tempFileName, String finalFileName, String category) {
        try {
            Path tempPath = fileStorageLocation.resolve("temp").resolve(tempFileName);
            Path targetLocation = createDirectoryStructure(category);
            Path finalPath = targetLocation.resolve(finalFileName);

            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File moved from {} to {}", tempPath, finalPath);

            return finalPath.toString();

        } catch (IOException e) {
            logger.error("Failed to move file from {} to {}", tempFileName, finalFileName, e);
            throw new FileStorageException("Could not move file", e);
        }
    }

    /**
     * Copy file
     */
    public String copyFile(String sourceFileName, String targetFileName, String category) {
        try {
            Path sourcePath = resolveFilePath(sourceFileName, category);
            Path targetLocation = createDirectoryStructure(category);
            Path targetPath = targetLocation.resolve(targetFileName);

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File copied from {} to {}", sourcePath, targetPath);

            return targetPath.toString();

        } catch (IOException e) {
            logger.error("Failed to copy file from {} to {}", sourceFileName, targetFileName, e);
            throw new FileStorageException("Could not copy file", e);
        }
    }

    /**
     * Get file information without loading content
     */
    public FileMetadata getFileMetadata(String fileName, String category) {
        try {
            Path filePath = resolveFilePath(fileName, category);

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(fileName);
            metadata.setFilePath(filePath.toString());
            metadata.setFileSize(Files.size(filePath));
            metadata.setCreatedDate(Files.getLastModifiedTime(filePath).toInstant());
            metadata.setModifiedDate(Files.getLastModifiedTime(filePath).toInstant());

            // Detect MIME type
            String mimeType = tika.detect(filePath);
            metadata.setMimeType(mimeType);

            return metadata;

        } catch (IOException e) {
            logger.error("Failed to get file metadata: {}", fileName, e);
            throw new FileStorageException("Could not get file metadata", e);
        }
    }

    /**
     * List files in a category/directory
     */
    public List<FileMetadata> listFiles(String category) {
        try {
            Path categoryPath = fileStorageLocation.resolve(category);

            if (!Files.exists(categoryPath)) {
                return new ArrayList<>();
            }

            List<FileMetadata> files = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(categoryPath)) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path)) {
                        String fileName = path.getFileName().toString();
                        FileMetadata metadata = getFileMetadata(fileName, category);
                        files.add(metadata);
                    }
                }
            }

            // Sort by modified date (newest first)
            files.sort((a, b) -> b.getModifiedDate().compareTo(a.getModifiedDate()));

            return files;

        } catch (IOException e) {
            logger.error("Failed to list files in category: {}", category, e);
            throw new FileStorageException("Could not list files", e);
        }
    }

    /**
     * Get storage statistics
     */
    public Map<String, Object> getStorageStatistics() {
        try {
            long totalSize = 0;
            long fileCount = 0;
            Map<String, Long> sizeByCategory = new HashMap<>();
            Map<String, Long> countByCategory = new HashMap<>();

            if (Files.exists(fileStorageLocation)) {
                try (DirectoryStream<Path> categories = Files.newDirectoryStream(fileStorageLocation)) {
                    for (Path categoryPath : categories) {
                        if (Files.isDirectory(categoryPath)) {
                            String category = categoryPath.getFileName().toString();
                            long categorySize = 0;
                            long categoryCount = 0;

                            try (DirectoryStream<Path> files = Files.newDirectoryStream(categoryPath)) {
                                for (Path filePath : files) {
                                    if (Files.isRegularFile(filePath)) {
                                        long size = Files.size(filePath);
                                        categorySize += size;
                                        totalSize += size;
                                        categoryCount++;
                                        fileCount++;
                                    }
                                }
                            }

                            sizeByCategory.put(category, categorySize);
                            countByCategory.put(category, categoryCount);
                        }
                    }
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSize", totalSize);
            stats.put("totalSizeFormatted", formatFileSize(totalSize));
            stats.put("fileCount", fileCount);
            stats.put("sizeByCategory", sizeByCategory);
            stats.put("countByCategory", countByCategory);
            stats.put("storagePath", fileStorageLocation.toString());
            stats.put("storageType", storageType);

            return stats;

        } catch (IOException e) {
            logger.error("Failed to get storage statistics", e);
            throw new FileStorageException("Could not get storage statistics", e);
        }
    }

    /**
     * Cleanup temporary files
     */
    public void cleanupTempFiles(int hoursOld) {
        try {
            Path tempDir = fileStorageLocation.resolve("temp");

            if (!Files.exists(tempDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (hoursOld * 60 * 60 * 1000);
            int deletedCount = 0;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir)) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path)) {
                        long fileTime = Files.getLastModifiedTime(path).toMillis();
                        if (fileTime < cutoffTime) {
                            Files.delete(path);
                            deletedCount++;
                        }
                    }
                }
            }

            logger.info("Cleaned up {} temporary files older than {} hours", deletedCount, hoursOld);

        } catch (IOException e) {
            logger.error("Failed to cleanup temporary files", e);
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum allowed size of " +
                    formatFileSize(maxFileSize));
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new FileStorageException("Invalid file name: " + fileName);
        }

        String extension = getFileExtension(fileName);
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new FileStorageException("File type not allowed: " + extension);
        }

        // Virus scan if enabled
        if (virusScanEnabled && !isFileClean(file)) {
            throw new FileStorageException("File failed security scan");
        }
    }

    /**
     * Process file content (compression, optimization, etc.)
     */
    private byte[] processFileContent(MultipartFile file) throws IOException {
        byte[] originalContent = file.getBytes();

        // Compress images
        if (isImageFile(file)) {
            return compressImage(originalContent, file.getContentType());
        }

        // Compress other files if enabled
        if (compressionEnabled && shouldCompress(file)) {
            return compressFile(originalContent);
        }

        return originalContent;
    }

    /**
     * Compress image while maintaining quality
     */
    private byte[] compressImage(byte[] imageData, String mimeType) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(bais);

            if (image == null) {
                return imageData; // Return original if can't process
            }

            // Convert to RGB if needed
            if (image.getType() != BufferedImage.TYPE_INT_RGB &&
                    !mimeType.equalsIgnoreCase("image/png")) {
                BufferedImage rgbImage = new BufferedImage(
                        image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgbImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = rgbImage;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String format = mimeType.substring(mimeType.indexOf('/') + 1);
                if (format.equals("jpeg")) format = "jpg";

                ImageIO.write(image, format, baos);
                byte[] compressedData = baos.toByteArray();

                // Return compressed only if it's smaller
                return compressedData.length < imageData.length ? compressedData : imageData;
            }
        }
    }

    /**
     * Generate thumbnail for images
     */
    private String generateThumbnail(Path originalPath, byte[] imageData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            BufferedImage originalImage = ImageIO.read(bais);

            if (originalImage == null) {
                return null;
            }

            // Calculate thumbnail dimensions
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            double scale = Math.min(
                    (double) thumbnailSize / originalWidth,
                    (double) thumbnailSize / originalHeight
            );

            int thumbnailWidth = (int) (originalWidth * scale);
            int thumbnailHeight = (int) (originalHeight * scale);

            // Create thumbnail
            BufferedImage thumbnail = new BufferedImage(
                    thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
            g2d.dispose();

            // Save thumbnail
            String thumbnailFileName = getThumbnailFileName(originalPath.getFileName().toString());
            Path thumbnailPath = originalPath.getParent().resolve("thumbnails");
            Files.createDirectories(thumbnailPath);
            Path thumbnailFilePath = thumbnailPath.resolve(thumbnailFileName);

            ImageIO.write(thumbnail, "jpg", thumbnailFilePath.toFile());

            logger.debug("Thumbnail generated: {}", thumbnailFilePath);

            return thumbnailFilePath.toString();

        } catch (IOException e) {
            logger.warn("Failed to generate thumbnail for: {}", originalPath, e);
            return null;
        }
    }

    /**
     * Compress file using GZIP
     */
    private byte[] compressFile(byte[] data) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {

            gzipOut.write(data);
            gzipOut.finish();

            byte[] compressedData = baos.toByteArray();

            // Only return compressed version if it's actually smaller
            return compressedData.length < data.length ? compressedData : data;
        }
    }

    /**
     * Decompress GZIP file
     */
    private byte[] decompressFile(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIn = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            return baos.toByteArray();
        }
    }

    /**
     * Create file metadata
     */
    private FileMetadata createFileMetadata(MultipartFile file, Path filePath,
                                            byte[] processedContent, String thumbnailPath) throws IOException {
        FileMetadata metadata = new FileMetadata();

        metadata.setFileName(filePath.getFileName().toString());
        metadata.setOriginalFileName(file.getOriginalFilename());
        metadata.setFilePath(filePath.toString());
        metadata.setFileSize(processedContent.length);
        metadata.setOriginalFileSize(file.getSize());

        // Detect MIME type
        String mimeType = tika.detect(processedContent, file.getOriginalFilename());
        metadata.setMimeType(mimeType);

        // Calculate checksum
        metadata.setChecksum(calculateChecksum(processedContent));

        // Set timestamps
        metadata.setCreatedDate(Files.getLastModifiedTime(filePath).toInstant());
        metadata.setModifiedDate(Files.getLastModifiedTime(filePath).toInstant());

        // Set thumbnail path if available
        metadata.setThumbnailPath(thumbnailPath);

        return metadata;
    }

    /**
     * Generate unique filename with timestamp
     */
    private String generateUniqueFileName(String originalFileName) {
        String name = getFileNameWithoutExtension(originalFileName);
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s_%s.%s", name, timestamp, uuid, extension);
    }

    /**
     * Create directory structure for organized storage
     */
    private Path createDirectoryStructure(String category) throws IOException {
        // Create structure: uploads/category/yyyy/MM/dd/
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());

        Path targetLocation = fileStorageLocation
                .resolve(category != null ? category : "general")
                .resolve(year)
                .resolve(month)
                .resolve(day);

        Files.createDirectories(targetLocation);

        return targetLocation;
    }

    /**
     * Resolve file path based on filename and category
     */
    private Path resolveFilePath(String fileName, String category) {
        // For now, search in the category directory
        // In a production system, you might store the full path in database
        Path categoryPath = fileStorageLocation.resolve(category != null ? category : "general");

        try {
            // Search for file recursively in the category directory
            return Files.walk(categoryPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileName));
        } catch (IOException e) {
            throw new FileStorageException("Error searching for file: " + fileName, e);
        }
    }

    // Helper methods

    private Set<String> initializeAllowedMimeTypes() {
        Set<String> mimeTypes = new HashSet<>();
        mimeTypes.add("image/jpeg");
        mimeTypes.add("image/jpg");
        mimeTypes.add("image/png");
        mimeTypes.add("image/gif");
        mimeTypes.add("application/pdf");
        mimeTypes.add("application/msword");
        mimeTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes.add("application/vnd.ms-excel");
        mimeTypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes.add("text/plain");
        mimeTypes.add("text/csv");
        return mimeTypes;
    }

    private boolean isImageFile(MultipartFile file) {
        String mimeType = file.getContentType();
        return mimeType != null && mimeType.startsWith("image/");
    }

    private boolean shouldCompress(MultipartFile file) {
        // Don't compress already compressed formats
        String mimeType = file.getContentType();
        return mimeType != null &&
                !mimeType.startsWith("image/") &&
                !mimeType.equals("application/pdf") &&
                file.getSize() > 1024; // Only compress files larger than 1KB
    }

    private boolean isCompressed(String fileName) {
        return fileName.endsWith(".gz");
    }

    private boolean isFileClean(MultipartFile file) {
        // Placeholder for virus scanning integration
        // In production, integrate with ClamAV or similar
        return true;
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new FileStorageException("Could not calculate file checksum", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
    }

    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? fileName : fileName.substring(0, lastDotIndex);
    }

    private String getThumbnailFileName(String originalFileName) {
        String nameWithoutExtension = getFileNameWithoutExtension(originalFileName);
        return nameWithoutExtension + "_thumb.jpg";
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}