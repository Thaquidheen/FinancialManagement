package com.company.erp.document.entity;

import com.company.erp.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "document_tags")
public class DocumentTag extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "color", length = 7)
    private String color; // Hex color code

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Document> documents;

    // Constructors
    public DocumentTag() {}

    public DocumentTag(String name) {
        this.name = name.toLowerCase().trim();
        this.displayName = name.trim();
    }

    public DocumentTag(String name, String displayName, String color) {
        this.name = name.toLowerCase().trim();
        this.displayName = displayName != null ? displayName.trim() : name.trim();
        this.color = color;
    }

    // Business methods
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    public void decrementUsageCount() {
        this.usageCount = Math.max(0, (this.usageCount == null ? 0 : this.usageCount) - 1);
    }

    public String getDisplayNameOrName() {
        return displayName != null ? displayName : name;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public Set<Document> getDocuments() { return documents; }
    public void setDocuments(Set<Document> documents) { this.documents = documents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentTag)) return false;
        DocumentTag that = (DocumentTag) o;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
