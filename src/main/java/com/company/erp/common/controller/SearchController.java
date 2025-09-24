package com.company.erp.common.controller;

import com.company.erp.common.dto.ApiResponse;
import com.company.erp.common.dto.SearchRequest;
import com.company.erp.common.dto.SearchResult;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.common.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/search")
@Tag(name = "Search", description = "Universal search and filtering APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    @Operation(summary = "Universal search", description = "Search across all entities with advanced filtering")
    public ResponseEntity<ApiResponse<Page<SearchResult>>> search(
            @Valid @RequestBody SearchRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {

        Page<SearchResult> results = searchService.universalSearch(request, currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }

    @GetMapping("/quick")
    @Operation(summary = "Quick search", description = "Simple text-based search across all entities")
    public ResponseEntity<ApiResponse<Page<SearchResult>>> quickSearch(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {

        SearchRequest request = new SearchRequest(q);
        if (type != null) {
            request.setSearchTypes(Set.of(type.toUpperCase()));
        }

        Page<SearchResult> results = searchService.universalSearch(request, currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Quick search completed", results));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Search suggestions", description = "Get search suggestions based on partial query")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<String> suggestions = searchService.getSuggestions(q, currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success("Suggestions retrieved", suggestions));
    }

    @GetMapping("/facets")
    @Operation(summary = "Search facets", description = "Get search result counts by category")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getFacets(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Map<String, Long> facets = searchService.getSearchFacets(q, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Search facets retrieved", facets));
    }
}
