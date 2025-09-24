package com.company.erp.common.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for entities that can be searched through the universal search functionality
 * Provides standardized methods for search indexing and relevance scoring
 */
public interface SearchableEntity {

    /**
     * Get the unique identifier of the entity
     */
    Long getId();

    /**
     * Get the entity type for search categorization
     * Examples: "PROJECT", "QUOTATION", "DOCUMENT", "USER"
     */
    String getEntityType();

    /**
     * Get the primary title/name for search display
     */
    String getSearchTitle();

    /**
     * Get the description or content for search indexing
     */
    String getSearchDescription();

    /**
     * Get searchable content including all text fields
     * This should include all relevant text that should be searchable
     */
    String getSearchableContent();

    /**
     * Get searchable keywords and tags
     */
    Set<String> getSearchKeywords();

    /**
     * Get the creation date for date-based filtering
     */
    LocalDateTime getCreatedDate();

    /**
     * Get the last modification date
     */
    LocalDateTime getLastModifiedDate();

    /**
     * Get the entity status for filtering
     * Examples: "ACTIVE", "COMPLETED", "DRAFT", "ARCHIVED"
     */
    String getStatus();

    /**
     * Get the owner/creator of the entity
     */
    Long getOwnerId();

    /**
     * Get the owner's name for display
     */
    String getOwnerName();

    /**
     * Get additional metadata for advanced filtering
     * This can include custom fields, categories, departments, etc.
     */
    Map<String, Object> getSearchMetadata();

    /**
     * Get related entity IDs for relationship-based searches
     * Examples: project IDs for quotations, user IDs for projects
     */
    Map<String, List<Long>> getRelatedEntityIds();

    /**
     * Calculate relevance score based on search query
     * Higher score means better match
     *
     * @param query The search query
     * @param searchFields The fields being searched
     * @return Relevance score (0.0 to 1.0)
     */
    default Double calculateRelevanceScore(String query, Set<String> searchFields) {
        if (query == null || query.trim().isEmpty()) {
            return 0.0;
        }

        String normalizedQuery = query.toLowerCase().trim();
        String[] queryTerms = normalizedQuery.split("\\s+");

        double totalScore = 0.0;
        double maxPossibleScore = 0.0;

        // Title matching (highest weight)
        if (searchFields.contains("title") && getSearchTitle() != null) {
            double titleScore = calculateFieldRelevance(getSearchTitle().toLowerCase(), queryTerms, 3.0);
            totalScore += titleScore;
            maxPossibleScore += 3.0;
        }

        // Description matching (medium weight)
        if (searchFields.contains("description") && getSearchDescription() != null) {
            double descScore = calculateFieldRelevance(getSearchDescription().toLowerCase(), queryTerms, 2.0);
            totalScore += descScore;
            maxPossibleScore += 2.0;
        }

        // Content matching (lower weight)
        if (searchFields.contains("content") && getSearchableContent() != null) {
            double contentScore = calculateFieldRelevance(getSearchableContent().toLowerCase(), queryTerms, 1.0);
            totalScore += contentScore;
            maxPossibleScore += 1.0;
        }

        // Keywords matching (high weight for exact matches)
        if (searchFields.contains("keywords") && getSearchKeywords() != null) {
            double keywordScore = calculateKeywordRelevance(getSearchKeywords(), queryTerms, 2.5);
            totalScore += keywordScore;
            maxPossibleScore += 2.5;
        }

        // Owner name matching
        if (searchFields.contains("owner") && getOwnerName() != null) {
            double ownerScore = calculateFieldRelevance(getOwnerName().toLowerCase(), queryTerms, 1.5);
            totalScore += ownerScore;
            maxPossibleScore += 1.5;
        }

        // Normalize score to 0-1 range
        return maxPossibleScore > 0 ? Math.min(totalScore / maxPossibleScore, 1.0) : 0.0;
    }

    /**
     * Calculate field relevance based on query terms
     */
    default double calculateFieldRelevance(String fieldContent, String[] queryTerms, double weight) {
        if (fieldContent == null || fieldContent.trim().isEmpty()) {
            return 0.0;
        }

        double fieldScore = 0.0;

        for (String term : queryTerms) {
            if (term.length() < 2) continue; // Skip very short terms

            // Exact phrase match (highest score)
            if (fieldContent.contains(String.join(" ", queryTerms))) {
                fieldScore += 1.0 * weight;
                break;
            }

            // Individual term matches
            if (fieldContent.contains(term)) {
                // Bonus for exact word match vs partial match
                if (fieldContent.matches(".*\\b" + term + "\\b.*")) {
                    fieldScore += 0.8 * weight;
                } else {
                    fieldScore += 0.4 * weight;
                }
            }

            // Fuzzy matching for typos (basic)
            if (calculateLevenshteinDistance(term, fieldContent) <= 2) {
                fieldScore += 0.2 * weight;
            }
        }

        return fieldScore;
    }

    /**
     * Calculate keyword relevance
     */
    default double calculateKeywordRelevance(Set<String> keywords, String[] queryTerms, double weight) {
        if (keywords == null || keywords.isEmpty()) {
            return 0.0;
        }

        double keywordScore = 0.0;
        Set<String> lowerKeywords = keywords.stream()
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        for (String term : queryTerms) {
            if (lowerKeywords.contains(term.toLowerCase())) {
                keywordScore += weight;
            }
        }

        return keywordScore;
    }

    /**
     * Simple Levenshtein distance calculation for fuzzy matching
     */
    default int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        if (s1.length() > 20 || s2.length() > 20) return Integer.MAX_VALUE; // Skip very long strings

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Check if entity matches given search criteria
     */
    default boolean matchesSearchCriteria(String query, Set<String> searchFields,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          String status, Long ownerId) {

        // Check relevance score threshold
        if (calculateRelevanceScore(query, searchFields) < 0.1) {
            return false;
        }

        // Check date range
        if (startDate != null && getCreatedDate() != null && getCreatedDate().isBefore(startDate)) {
            return false;
        }
        if (endDate != null && getCreatedDate() != null && getCreatedDate().isAfter(endDate)) {
            return false;
        }

        // Check status filter
        if (status != null && !status.equalsIgnoreCase(getStatus())) {
            return false;
        }

        // Check owner filter
        if (ownerId != null && !ownerId.equals(getOwnerId())) {
            return false;
        }

        return true;
    }

    /**
     * Get search result summary for display
     */
    default String getSearchSummary() {
        StringBuilder summary = new StringBuilder();

        if (getSearchTitle() != null) {
            summary.append(getSearchTitle());
        }

        if (getSearchDescription() != null) {
            if (summary.length() > 0) summary.append(" - ");

            String description = getSearchDescription();
            if (description.length() > 150) {
                description = description.substring(0, 147) + "...";
            }
            summary.append(description);
        }

        return summary.toString();
    }

    /**
     * Get highlighted search result with query terms emphasized
     */
    default String getHighlightedSummary(String query) {
        String summary = getSearchSummary();

        if (query == null || query.trim().isEmpty()) {
            return summary;
        }

        String[] queryTerms = query.toLowerCase().split("\\s+");
        String highlightedSummary = summary;

        for (String term : queryTerms) {
            if (term.length() > 1) {
                // Simple highlighting - in a real implementation you might use HTML tags
                highlightedSummary = highlightedSummary.replaceAll(
                        "(?i)" + java.util.regex.Pattern.quote(term),
                        "**" + term.toUpperCase() + "**"
                );
            }
        }

        return highlightedSummary;
    }

    /**
     * Check if user has access to this entity
     */
    default boolean hasUserAccess(Long userId, Set<String> userRoles) {
        // Default implementation - can be overridden by implementing classes

        // Owner always has access
        if (userId.equals(getOwnerId())) {
            return true;
        }

        // Super admin has access to everything
        if (userRoles.contains("SUPER_ADMIN")) {
            return true;
        }

        // Check entity-specific access rules
        return checkEntitySpecificAccess(userId, userRoles);
    }

    /**
     * Entity-specific access control logic
     * Should be implemented by each entity type
     */
    default boolean checkEntitySpecificAccess(Long userId, Set<String> userRoles) {
        // Default: allow access for account managers and project managers
        return userRoles.contains("ACCOUNT_MANAGER") || userRoles.contains("PROJECT_MANAGER");
    }

    /**
     * Get boost factor for search ranking
     * More important entities get higher boost
     */
    default double getSearchBoost() {
        // Default boost - can be overridden
        double boost = 1.0;

        // Boost newer items slightly
        if (getCreatedDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime monthAgo = now.minusMonths(1);

            if (getCreatedDate().isAfter(monthAgo)) {
                boost += 0.1;
            }
        }

        // Boost active/important items
        if ("ACTIVE".equalsIgnoreCase(getStatus()) || "PRIORITY".equalsIgnoreCase(getStatus())) {
            boost += 0.2;
        }

        return boost;
    }

    /**
     * Get entity URL for navigation
     */
    default String getEntityUrl() {
        return "/" + getEntityType().toLowerCase() + "/" + getId();
    }

    /**
     * Get entity icon for UI display
     */
    default String getEntityIcon() {
        return switch (getEntityType().toUpperCase()) {
            case "PROJECT" -> "folder";
            case "QUOTATION" -> "receipt";
            case "DOCUMENT" -> "file";
            case "USER" -> "user";
            case "PAYMENT" -> "credit-card";
            case "APPROVAL" -> "check-circle";
            default -> "item";
        };
    }

    /**
     * Get search index data for external search engines
     */
    default Map<String, Object> getSearchIndexData() {
        Map<String, Object> indexData = new HashMap<>();

        indexData.put("id", getId());
        indexData.put("entityType", getEntityType());
        indexData.put("title", getSearchTitle());
        indexData.put("description", getSearchDescription());
        indexData.put("content", getSearchableContent());
        indexData.put("keywords", getSearchKeywords());
        indexData.put("createdDate", getCreatedDate());
        indexData.put("lastModifiedDate", getLastModifiedDate());
        indexData.put("status", getStatus());
        indexData.put("ownerId", getOwnerId());
        indexData.put("ownerName", getOwnerName());
        indexData.put("metadata", getSearchMetadata());
        indexData.put("relatedEntities", getRelatedEntityIds());
        indexData.put("boost", getSearchBoost());
        indexData.put("url", getEntityUrl());
        indexData.put("icon", getEntityIcon());

        return indexData;
    }
}