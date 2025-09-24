package com.company.erp.document.controller;

import com.company.erp.common.dto.ApiResponse;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.document.dto.request.FileMetadata;
import com.company.erp.document.dto.response.FileUploadResponse;
import com.company.erp.document.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@Tag(name = "File Storage", description = "File storage and management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FileStorageController {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    private final FileStorageService fileStorageService;

    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ACCOUNT_MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Upload single file", description = "Upload a single file to the storage system")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            FileUploadResponse response = fileStorageService.storeFile(file, category, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
        } catch (Exception e) {
            logger.error("File upload failed for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/multiple")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ACCOUNT_MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Upload multiple files", description = "Upload multiple files to the storage system")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            List<FileUploadResponse> responses = fileStorageService.storeFiles(files, category, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Files processed successfully", responses));
        } catch (Exception e) {
            logger.error("Multiple file upload failed for user {}: {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Multiple file upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{fileName}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ACCOUNT_MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Download file", description = "Download a file from the storage system")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            @RequestParam(value = "category", defaultValue = "general") String category) {

        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName, category);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("File download failed for file {}: {}", fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/metadata/{fileName}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ACCOUNT_MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get file metadata", description = "Get metadata for a specific file")
    public ResponseEntity<ApiResponse<FileMetadata>> getFileMetadata(
            @PathVariable String fileName,
            @RequestParam(value = "category", defaultValue = "general") String category) {

        try {
            FileMetadata metadata = fileStorageService.getFileMetadata(fileName, category);
            return ResponseEntity.ok(ApiResponse.success("File metadata retrieved successfully", metadata));
        } catch (Exception e) {
            logger.error("Failed to get metadata for file {}: {}", fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("File not found: " + fileName));
        }
    }

    @GetMapping("/list/{category}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ACCOUNT_MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "List files in category", description = "List all files in a specific category")
    public ResponseEntity<ApiResponse<List<FileMetadata>>> listFiles(@PathVariable String category) {

        try {
            List<FileMetadata> files = fileStorageService.listFiles(category);
            return ResponseEntity.ok(ApiResponse.success("Files listed successfully", files));
        } catch (Exception e) {
            logger.error("Failed to list files in category {}: {}", category, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to list files: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{fileName}")
    @PreAuthorize("hasRole('ACCOUNT_MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete file", description = "Delete a file from the storage system")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String fileName,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            boolean deleted = fileStorageService.deleteFile(fileName, category);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("File not found: " + fileName));
            }
        } catch (Exception e) {
            logger.error("Failed to delete file {} by user {}: {}", fileName, currentUser.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete file: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get storage statistics", description = "Get storage system statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStorageStatistics() {

        try {
            Map<String, Object> stats = fileStorageService.getStorageStatistics();
            return ResponseEntity.ok(ApiResponse.success("Storage statistics retrieved successfully", stats));
        } catch (Exception e) {
            logger.error("Failed to get storage statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get storage statistics: " + e.getMessage()));
        }
    }

    @PostMapping("/cleanup/temp")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Cleanup temporary files", description = "Clean up temporary files older than specified hours")
    public ResponseEntity<ApiResponse<Void>> cleanupTempFiles(
            @RequestParam(value = "hoursOld", defaultValue = "24") int hoursOld) {

        try {
            fileStorageService.cleanupTempFiles(hoursOld);
            return ResponseEntity.ok(ApiResponse.success("Temporary files cleaned up successfully", null));
        } catch (Exception e) {
            logger.error("Failed to cleanup temporary files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cleanup temporary files: " + e.getMessage()));
        }
    }
}