package com.company.erp.common.service;

import com.company.erp.common.dto.SearchResult;
import com.company.erp.common.dto.SearchRequest;
import com.company.erp.common.entity.SearchableEntity;
import com.company.erp.document.service.DocumentService;
import com.company.erp.financial.service.QuotationService;
import com.company.erp.project.service.ProjectService;
import com.company.erp.user.entity.User;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final ProjectService projectService;
    private final QuotationService quotationService;
    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public SearchService(ProjectService projectService,
                         QuotationService quotationService,
                         DocumentService documentService,
                         UserRepository userRepository,
                         AuditService auditService) {
        this.projectService = projectService;
        this.quotationService = quotationService;
        this.documentService = documentService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public Page<SearchResult> universalSearch(SearchRequest request, Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        List<SearchResult> allResults = new ArrayList<>();

        try {
            // Search across different entity types based on user's search scope
            if (request.getSearchTypes().isEmpty() || request.getSearchTypes().contains("PROJECT")) {
                allResults.addAll(searchProjects(request, user));
            }

            if (request.getSearchTypes().isEmpty() || request.getSearchTypes().contains("QUOTATION")) {
                allResults.addAll(searchQuotations(request, user));
            }

            if (request.getSearchTypes().isEmpty() || request.getSearchTypes().contains("DOCUMENT")) {
                allResults.addAll(searchDocuments(request, user));
            }

            if (request.getSearchTypes().isEmpty() || request.getSearchTypes().contains("USER")) {
                allResults.addAll(searchUsers(request, user));
            }

            // Sort results by relevance score
            allResults.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));

            // Apply additional filters
            allResults = applyFilters(allResults, request);

            // Log search
            auditService.logAction(userId, "SEARCH_PERFORMED", "SEARCH", null,
                    "Universal search: " + request.getQuery(), null, request);

            // Paginate results
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allResults.size());
            List<SearchResult> pageContent = start < allResults.size() ?
                    allResults.subList(start, end) : Collections.emptyList();

            return new PageImpl<>(pageContent, pageable, allResults.size());

        } catch (Exception e) {
            logger.error("Error performing universal search for user {}: {}", userId, e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    private List<SearchResult> searchProjects(SearchRequest request, User user) {
        // Implementation would call ProjectService with search parameters
        // For now, return placeholder
        return Collections.emptyList();
    }

    private List<SearchResult> searchQuotations(SearchRequest request, User user) {
        // Implementation would call QuotationService with search parameters
        return Collections.emptyList();
    }

    private List<SearchResult> searchDocuments(SearchRequest request, User user) {
        // Implementation would call DocumentService with search parameters
        return Collections.emptyList();
    }

    private List<SearchResult> searchUsers(SearchRequest request, User user) {
        // Implementation would call UserService with search parameters
        // Only allow if user has appropriate permissions
        if (user.hasRole("SUPER_ADMIN") || user.hasRole("ACCOUNT_MANAGER")) {
            return Collections.emptyList(); // Placeholder
        }
        return Collections.emptyList();
    }

    private List<SearchResult> applyFilters(List<SearchResult> results, SearchRequest request) {
        return results.stream()
                .filter(result -> matchesDateFilter(result, request.getStartDate(), request.getEndDate()))
                .filter(result -> matchesEntityTypeFilter(result, request.getEntityTypes()))
                .filter(result -> matchesCustomFilters(result, request.getCustomFilters()))
                .collect(Collectors.toList());
    }

    private boolean matchesDateFilter(SearchResult result, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) return true;
        if (result.getCreatedDate() == null) return true;

        if (startDate != null && result.getCreatedDate().isBefore(startDate)) return false;
        if (endDate != null && result.getCreatedDate().isAfter(endDate)) return false;

        return true;
    }

    private boolean matchesEntityTypeFilter(SearchResult result, Set<String> entityTypes) {
        if (entityTypes == null || entityTypes.isEmpty()) return true;
        return entityTypes.contains(result.getEntityType());
    }

    private boolean matchesCustomFilters(SearchResult result, Map<String, Object> customFilters) {
        if (customFilters == null || customFilters.isEmpty()) return true;

        // Apply custom filters based on result metadata
        for (Map.Entry<String, Object> filter : customFilters.entrySet()) {
            if (!result.getMetadata().containsKey(filter.getKey())) continue;

            Object resultValue = result.getMetadata().get(filter.getKey());
            if (!Objects.equals(resultValue, filter.getValue())) {
                return false;
            }
        }

        return true;
    }

    public List<String> getSuggestions(String partialQuery, Long userId, int maxSuggestions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Set<String> suggestions = new LinkedHashSet<>();

        try {
            // Get suggestions from different sources
            suggestions.addAll(getProjectSuggestions(partialQuery, user, maxSuggestions / 4));
            suggestions.addAll(getDocumentSuggestions(partialQuery, user, maxSuggestions / 4));
            suggestions.addAll(getQuotationSuggestions(partialQuery, user, maxSuggestions / 4));
            suggestions.addAll(getTagSuggestions(partialQuery, user, maxSuggestions / 4));

            return suggestions.stream()
                    .limit(maxSuggestions)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting search suggestions for user {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Set<String> getProjectSuggestions(String partialQuery, User user, int limit) {
        // Implementation would query project names and descriptions
        return Collections.emptySet();
    }

    private Set<String> getDocumentSuggestions(String partialQuery, User user, int limit) {
        // Implementation would query document filenames and content
        return Collections.emptySet();
    }

    private Set<String> getQuotationSuggestions(String partialQuery, User user, int limit) {
        // Implementation would query quotation descriptions and items
        return Collections.emptySet();
    }

    private Set<String> getTagSuggestions(String partialQuery, User user, int limit) {
        // Implementation would query document tags and categories
        return Collections.emptySet();
    }

    public Map<String, Long> getSearchFacets(String query, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Map<String, Long> facets = new HashMap<>();

        try {
            // Count results by entity type
            facets.put("projects", getProjectSearchCount(query, user));
            facets.put("quotations", getQuotationSearchCount(query, user));
            facets.put("documents", getDocumentSearchCount(query, user));

            if (canSearchUsers(user)) {
                facets.put("users", getUserSearchCount(query, user));
            }

        } catch (Exception e) {
            logger.error("Error getting search facets for user {}: {}", userId, e.getMessage(), e);
        }

        return facets;
    }

    private Long getProjectSearchCount(String query, User user) {
        // Implementation would return count of matching projects
        return 0L;
    }

    private Long getQuotationSearchCount(String query, User user) {
        // Implementation would return count of matching quotations
        return 0L;
    }

    private Long getDocumentSearchCount(String query, User user) {
        // Implementation would return count of matching documents
        return 0L;
    }

    private Long getUserSearchCount(String query, User user) {
        // Implementation would return count of matching users
        return 0L;
    }

    private boolean canSearchUsers(User user) {
        return user.hasRole("SUPER_ADMIN") || user.hasRole("ACCOUNT_MANAGER");
    }
}
