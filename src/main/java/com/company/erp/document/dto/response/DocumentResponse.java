package com.company.erp.document.dto.response;

import com.company.erp.document.entity.DocumentCategory;

import java.time.LocalDateTime;
import java.util.List;

public class DocumentResponse {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Long projectId;
    private UserResponse uploadedBy;
    private DocumentCategory category;
    private List<TagResponse> tags;
    private LocalDateTime uploadDate;
    private LocalDateTime lastAccessedDate;
    private Integer accessCount;
    private String description;
    private Integer version;

    // Constructors
    public DocumentResponse() {}

    public DocumentResponse(Long id, String fileName, Long fileSize, String mimeType) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public UserResponse getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UserResponse uploadedBy) { this.uploadedBy = uploadedBy; }

    public DocumentCategory getCategory() { return category; }
    public void setCategory(DocumentCategory category) { this.category = category; }

    public List<TagResponse> getTags() { return tags; }
    public void setTags(List<TagResponse> tags) { this.tags = tags; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getLastAccessedDate() { return lastAccessedDate; }
    public void setLastAccessedDate(LocalDateTime lastAccessedDate) { this.lastAccessedDate = lastAccessedDate; }

    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    // Inner classes
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;

        // Constructors
        public UserResponse() {}

        public UserResponse(Long id, String username, String email, String firstName, String lastName) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class TagResponse {
        private Long id;
        private String name;
        private String color;

        // Constructors
        public TagResponse() {}

        public TagResponse(Long id, String name, String color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}
