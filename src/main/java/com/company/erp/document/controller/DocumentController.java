package com.company.erp.document.controller;

import com.company.erp.common.security.UserPrincipal;
import com.company.erp.common.dto.ApiResponse;
// import com.company.erp.document.dto.request.DocumentUploadRequest; // Not used
// import com.company.erp.document.dto.response.DocumentMetadataResponse; // Not used
import com.company.erp.document.dto.response.DocumentResponse;
import com.company.erp.document.entity.Document;
import com.company.erp.document.entity.DocumentCategory;
import com.company.erp.document.entity.DocumentTag;
import com.company.erp.document.service.DocumentService;
import com.company.erp.document.service.DocumentSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Management", description = "Document upload, download, and management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final DocumentSecurityService documentSecurityService;

    public DocumentController(DocumentService documentService, DocumentSecurityService documentSecurityService) {
        this.documentService = documentService;
        this.documentSecurityService = documentSecurityService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Upload documents", description = "Upload one or more documents with metadata")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Documents uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "File too large"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "Unsupported file type")
    })
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> uploadDocuments(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam("category") DocumentCategory category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("Uploading {} documents for user {}", files.length, currentUser.getId());

        try {
            Set<String> tagSet = tags != null && !tags.trim().isEmpty() 
                ? Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
                : Set.of();

            List<DocumentResponse> uploadedDocuments = Arrays.stream(files)
                .map(file -> {
                    Document document = documentService.uploadDocument(
                        file, projectId, currentUser.getId(), category, tagSet);
                    return convertToResponse(document);
                })
                .collect(Collectors.toList());

            logger.info("Successfully uploaded {} documents", uploadedDocuments.size());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Documents uploaded successfully", uploadedDocuments));

        } catch (Exception e) {
            logger.error("Failed to upload documents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to upload documents: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Download document", description = "Download a document by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("Downloading document {} for user {}", id, currentUser.getId());

        try {
            byte[] fileData = documentService.downloadDocument(id, currentUser.getId());
            Document document = documentService.getDocument(id, currentUser.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getMimeType()));
            headers.setContentDispositionFormData("attachment", document.getFileName());
            headers.setContentLength(fileData.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);

        } catch (Exception e) {
            logger.error("Failed to download document {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get document preview", description = "Get document preview data for in-browser viewing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentPreview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            Document document = documentService.getDocument(id, currentUser.getId());
            
            Map<String, Object> previewData = Map.of(
                "id", document.getId(),
                "fileName", document.getFileName(),
                "mimeType", document.getMimeType(),
                "canPreview", document.isImage() || document.isPdf(),
                "previewUrl", "/api/documents/" + id + "/preview",
                "thumbnailUrl", document.isImage() ? "/api/documents/" + id + "/thumbnail" : null
            );

            return ResponseEntity.ok(ApiResponse.success("Preview data retrieved", previewData));

        } catch (Exception e) {
            logger.error("Failed to get document preview {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/thumbnail")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get document thumbnail", description = "Get thumbnail for image documents")
    public ResponseEntity<byte[]> getDocumentThumbnail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            Document document = documentService.getDocument(id, currentUser.getId());
            
            if (!document.isImage()) {
                return ResponseEntity.badRequest().build();
            }

            // For now, return the original image as thumbnail
            // In production, you'd generate actual thumbnails
            byte[] thumbnailData = document.getFileData();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getMimeType()));
            headers.setContentLength(thumbnailData.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(thumbnailData);

        } catch (Exception e) {
            logger.error("Failed to get document thumbnail {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Search documents", description = "Search documents with filters and pagination")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> searchDocuments(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "category", required = false) DocumentCategory category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "uploadDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            Set<String> tagSet = tags != null && !tags.trim().isEmpty() 
                ? Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
                : Set.of();

            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Document> documents = documentService.searchDocuments(
                searchTerm, projectId, category, tagSet, startDate, endDate, 
                currentUser.getId(), pageable);

            Page<DocumentResponse> response = documents.map(this::convertToResponse);

            return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", response));

        } catch (Exception e) {
            logger.error("Failed to search documents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to search documents: " + e.getMessage()));
        }
    }

    @GetMapping("/quotation/{quotationId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get quotation documents", description = "Get all documents associated with a quotation")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getQuotationDocuments(
            @PathVariable Long quotationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            // This would need to be implemented in the service layer
            // For now, return empty list as placeholder
            List<DocumentResponse> documents = List.of();
            return ResponseEntity.ok(ApiResponse.success("Quotation documents retrieved", documents));

        } catch (Exception e) {
            logger.error("Failed to get quotation documents {}: {}", quotationId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get quotation documents: " + e.getMessage()));
        }
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get project documents", description = "Get all documents associated with a project")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getProjectDocuments(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            // This would need to be implemented in the service layer
            // For now, return empty list as placeholder
            List<DocumentResponse> documents = List.of();
            return ResponseEntity.ok(ApiResponse.success("Project documents retrieved", documents));

        } catch (Exception e) {
            logger.error("Failed to get project documents {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get project documents: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Delete document", description = "Delete a document by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            documentService.deleteDocument(id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));

        } catch (Exception e) {
            logger.error("Failed to delete document {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete document: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/metadata")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Update document metadata", description = "Update document category, tags, and description")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocumentMetadata(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            DocumentCategory category = updates.containsKey("category") 
                ? DocumentCategory.valueOf(updates.get("category").toString())
                : null;

            Set<String> tags = updates.containsKey("tags") && updates.get("tags") instanceof List
                ? ((List<?>) updates.get("tags")).stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet())
                : null;

            String description = updates.containsKey("description") 
                ? updates.get("description").toString()
                : null;

            Document document = documentService.updateDocumentMetadata(id, currentUser.getId(), category, tags, description);
            DocumentResponse response = convertToResponse(document);

            return ResponseEntity.ok(ApiResponse.success("Document metadata updated successfully", response));

        } catch (Exception e) {
            logger.error("Failed to update document metadata {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update document metadata: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get document statistics", description = "Get document statistics and analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDocumentStats(
            @RequestParam(value = "projectId", required = false) Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            Map<String, Object> stats = documentService.getDocumentStatistics(projectId, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Document statistics retrieved", stats));

        } catch (Exception e) {
            logger.error("Failed to get document statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get document statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get document categories", description = "Get all available document categories")
    public ResponseEntity<ApiResponse<List<String>>> getDocumentCategories() {
        try {
            List<String> categories = Arrays.stream(DocumentCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Document categories retrieved", categories));

        } catch (Exception e) {
            logger.error("Failed to get document categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get document categories: " + e.getMessage()));
        }
    }

    @GetMapping("/tags")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get document tags", description = "Get all available document tags")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDocumentTags() {
        try {
            // This would need to be implemented in the service layer
            // For now, return empty list as placeholder
            List<Map<String, Object>> tags = List.of();
            return ResponseEntity.ok(ApiResponse.success("Document tags retrieved", tags));

        } catch (Exception e) {
            logger.error("Failed to get document tags: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get document tags: " + e.getMessage()));
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get recent documents", description = "Get recently uploaded documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getRecentDocuments(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            List<Document> documents = documentService.getRecentDocuments(currentUser.getId(), limit);
            List<DocumentResponse> response = documents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Recent documents retrieved", response));

        } catch (Exception e) {
            logger.error("Failed to get recent documents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get recent documents: " + e.getMessage()));
        }
    }

    @GetMapping("/most-accessed")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER') or hasAuthority('PROJECT_MANAGER') or hasAuthority('EMPLOYEE')")
    @Operation(summary = "Get most accessed documents", description = "Get most frequently accessed documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getMostAccessedDocuments(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            List<Document> documents = documentService.getFrequentlyAccessedDocuments(currentUser.getId(), limit);
            List<DocumentResponse> response = documents.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Most accessed documents retrieved", response));

        } catch (Exception e) {
            logger.error("Failed to get most accessed documents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get most accessed documents: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Bulk delete documents", description = "Delete multiple documents at once")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteDocuments(
            @RequestBody List<Long> documentIds,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            for (Long id : documentIds) {
                documentService.deleteDocument(id, currentUser.getId());
            }

            return ResponseEntity.ok(ApiResponse.success("Documents deleted successfully", null));

        } catch (Exception e) {
            logger.error("Failed to bulk delete documents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to bulk delete documents: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-update-category")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('ACCOUNT_MANAGER')")
    @Operation(summary = "Bulk update document categories", description = "Update category for multiple documents")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateCategories(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> documentIds = (List<Long>) request.get("ids");
            DocumentCategory category = DocumentCategory.valueOf(request.get("category").toString());

            for (Long id : documentIds) {
                documentService.updateDocumentMetadata(id, currentUser.getId(), category, null, null);
            }

            return ResponseEntity.ok(ApiResponse.success("Document categories updated successfully", null));

        } catch (Exception e) {
            logger.error("Failed to bulk update categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to bulk update categories: " + e.getMessage()));
        }
    }

    private DocumentResponse convertToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setFileSize(document.getFileSize());
        response.setMimeType(document.getMimeType());
        response.setProjectId(document.getProjectId());
        response.setCategory(document.getCategory());
        response.setDescription(document.getDescription());
        response.setUploadDate(document.getCreatedDate());
        response.setLastAccessedDate(document.getLastAccessedDate());
        response.setAccessCount(document.getAccessCount());
        response.setVersion(1); // Default version

        // Convert uploaded by user
        if (document.getUploadedBy() != null) {
            response.setUploadedBy(convertUserToResponse(document.getUploadedBy()));
        }

        // Convert tags
        if (document.getTags() != null) {
            response.setTags(document.getTags().stream()
                .map(this::convertTagToResponse)
                .collect(Collectors.toList()));
        }

        return response;
    }

    private DocumentResponse.UserResponse convertUserToResponse(com.company.erp.user.entity.User user) {
        DocumentResponse.UserResponse userResponse = new DocumentResponse.UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        // Split fullName into first and last name
        String[] nameParts = user.getFullName().split(" ", 2);
        userResponse.setFirstName(nameParts[0]);
        userResponse.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        return userResponse;
    }

    private DocumentResponse.TagResponse convertTagToResponse(DocumentTag tag) {
        DocumentResponse.TagResponse tagResponse = new DocumentResponse.TagResponse();
        tagResponse.setId(tag.getId());
        tagResponse.setName(tag.getName());
        tagResponse.setColor(tag.getColor());
        return tagResponse;
    }
}
