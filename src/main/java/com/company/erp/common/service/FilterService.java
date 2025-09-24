package com.company.erp.common.service;

import com.company.erp.common.dto.FilterRequest;
import com.company.erp.common.dto.FilterResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilterService {

    public <T> List<T> applyFilters(List<T> data, FilterRequest filterRequest) {
        if (filterRequest == null || filterRequest.getFilters().isEmpty()) {
            return data;
        }

        return data.stream()
                .filter(item -> matchesAllFilters(item, filterRequest.getFilters()))
                .collect(Collectors.toList());
    }

    private <T> boolean matchesAllFilters(T item, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            if (!matchesFilter(item, filter.getKey(), filter.getValue())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> boolean matchesFilter(T item, String fieldName, Object filterValue) {
        try {
            Object fieldValue = getFieldValue(item, fieldName);

            if (fieldValue == null) {
                return filterValue == null;
            }

            if (filterValue == null) {
                return false;
            }

            // Handle different filter types
            if (filterValue instanceof Map) {
                Map<String, Object> filterMap = (Map<String, Object>) filterValue;
                return applyComplexFilter(fieldValue, filterMap);
            }

            // Simple equality check
            return Objects.equals(fieldValue, filterValue);

        } catch (Exception e) {
            return false;
        }
    }

    private Object getFieldValue(Object item, String fieldName) {
        try {
            // Use reflection to get field value
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() +
                    fieldName.substring(1);
            return item.getClass().getMethod(methodName).invoke(item);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean applyComplexFilter(Object fieldValue, Map<String, Object> filterMap) {
        for (Map.Entry<String, Object> filter : filterMap.entrySet()) {
            String operator = filter.getKey();
            Object value = filter.getValue();

            switch (operator.toLowerCase()) {
                case "eq": // equals
                    if (!Objects.equals(fieldValue, value)) return false;
                    break;
                case "ne": // not equals
                    if (Objects.equals(fieldValue, value)) return false;
                    break;
                case "gt": // greater than
                    if (!isGreaterThan(fieldValue, value)) return false;
                    break;
                case "gte": // greater than or equal
                    if (!isGreaterThanOrEqual(fieldValue, value)) return false;
                    break;
                case "lt": // less than
                    if (!isLessThan(fieldValue, value)) return false;
                    break;
                case "lte": // less than or equal
                    if (!isLessThanOrEqual(fieldValue, value)) return false;
                    break;
                case "in": // in list
                    if (value instanceof List && !((List<?>) value).contains(fieldValue)) return false;
                    break;
                case "nin": // not in list
                    if (value instanceof List && ((List<?>) value).contains(fieldValue)) return false;
                    break;
                case "contains": // string contains
                    if (fieldValue instanceof String && value instanceof String) {
                        if (!((String) fieldValue).toLowerCase().contains(((String) value).toLowerCase())) {
                            return false;
                        }
                    }
                    break;
                case "startswith": // string starts with
                    if (fieldValue instanceof String && value instanceof String) {
                        if (!((String) fieldValue).toLowerCase().startsWith(((String) value).toLowerCase())) {
                            return false;
                        }
                    }
                    break;
                case "endswith": // string ends with
                    if (fieldValue instanceof String && value instanceof String) {
                        if (!((String) fieldValue).toLowerCase().endsWith(((String) value).toLowerCase())) {
                            return false;
                        }
                    }
                    break;
                case "between": // between two values
                    if (value instanceof List && ((List<?>) value).size() == 2) {
                        List<?> range = (List<?>) value;
                        if (!isBetween(fieldValue, range.get(0), range.get(1))) return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean isGreaterThan(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(b) > 0;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isGreaterThanOrEqual(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(b) >= 0;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isLessThan(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(b) < 0;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isLessThanOrEqual(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(b) <= 0;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isBetween(Object value, Object min, Object max) {
        if (value instanceof Comparable && min instanceof Comparable && max instanceof Comparable) {
            Comparable<Object> comp = (Comparable<Object>) value;
            return comp.compareTo(min) >= 0 && comp.compareTo(max) <= 0;
        }
        return false;
    }

    public FilterResult buildFilterOptions(String entityType, Long userId) {
        FilterResult result = new FilterResult();

        switch (entityType.toUpperCase()) {
            case "PROJECT":
                result = buildProjectFilters();
                break;
            case "QUOTATION":
                result = buildQuotationFilters();
                break;
            case "DOCUMENT":
                result = buildDocumentFilters();
                break;
            case "USER":
                result = buildUserFilters();
                break;
            default:
                result.setAvailableFilters(Collections.emptyMap());
        }

        return result;
    }

    private FilterResult buildProjectFilters() {
        FilterResult result = new FilterResult();
        Map<String, Object> filters = new HashMap<>();

        filters.put("status", Map.of(
                "type", "select",
                "options", Arrays.asList("ACTIVE", "COMPLETED", "ON_HOLD", "CANCELLED"),
                "label", "Project Status"
        ));

        filters.put("manager", Map.of(
                "type", "user_select",
                "label", "Project Manager"
        ));

        filters.put("budgetRange", Map.of(
                "type", "number_range",
                "label", "Budget Range (SAR)",
                "min", 0,
                "max", 10000000
        ));

        filters.put("createdDate", Map.of(
                "type", "date_range",
                "label", "Created Date"
        ));

        result.setAvailableFilters(filters);
        return result;
    }

    private FilterResult buildQuotationFilters() {
        FilterResult result = new FilterResult();
        Map<String, Object> filters = new HashMap<>();

        filters.put("status", Map.of(
                "type", "select",
                "options", Arrays.asList("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "PAID"),
                "label", "Quotation Status"
        ));

        filters.put("project", Map.of(
                "type", "project_select",
                "label", "Project"
        ));

        filters.put("amountRange", Map.of(
                "type", "number_range",
                "label", "Amount Range (SAR)",
                "min", 0,
                "max", 1000000
        ));

        filters.put("submittedDate", Map.of(
                "type", "date_range",
                "label", "Submitted Date"
        ));

        result.setAvailableFilters(filters);
        return result;
    }

    private FilterResult buildDocumentFilters() {
        FilterResult result = new FilterResult();
        Map<String, Object> filters = new HashMap<>();

        filters.put("category", Map.of(
                "type", "select",
                "options", Arrays.asList("INVOICE", "RECEIPT", "CONTRACT", "PHOTO", "DOCUMENT", "OTHER"),
                "label", "Document Category"
        ));

        filters.put("mimeType", Map.of(
                "type", "select",
                "options", Arrays.asList("application/pdf", "image/jpeg", "image/png",
                        "application/vnd.ms-excel", "application/msword"),
                "label", "File Type"
        ));

        filters.put("fileSize", Map.of(
                "type", "number_range",
                "label", "File Size (MB)",
                "min", 0,
                "max", 10
        ));

        filters.put("uploadDate", Map.of(
                "type", "date_range",
                "label", "Upload Date"
        ));

        filters.put("tags", Map.of(
                "type", "multi_select",
                "label", "Tags"
        ));

        result.setAvailableFilters(filters);
        return result;
    }

    private FilterResult buildUserFilters() {
        FilterResult result = new FilterResult();
        Map<String, Object> filters = new HashMap<>();

        filters.put("role", Map.of(
                "type", "select",
                "options", Arrays.asList("SUPER_ADMIN", "ACCOUNT_MANAGER", "PROJECT_MANAGER"),
                "label", "User Role"
        ));

        filters.put("active", Map.of(
                "type", "boolean",
                "label", "Active Status"
        ));

        filters.put("department", Map.of(
                "type", "select",
                "label", "Department"
        ));

        filters.put("createdDate", Map.of(
                "type", "date_range",
                "label", "Created Date"
        ));

        result.setAvailableFilters(filters);
        return result;
    }
}