package com.company.erp.notification.controller;

import com.company.erp.common.dto.ApiResponse;
import com.company.erp.common.security.UserPrincipal;
import com.company.erp.notification.dto.request.SendNotificationRequest;
import com.company.erp.notification.dto.response.NotificationResponse;
import com.company.erp.notification.entity.Notification;
import com.company.erp.notification.entity.NotificationPreference;
import com.company.erp.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Notification management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get paginated notifications for the current user
     */
    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieve paginated notifications for the current user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            Pageable pageable) {

        Page<NotificationResponse> notifications = notificationService.getUserNotifications(
                currentUser.getId(), read, type, priority, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count", description = "Get count of unread notifications for current user")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal currentUser) {
        long count = notificationService.getUnreadNotificationCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics", description = "Get notification statistics for dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationStats(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Map<String, Object> stats = notificationService.getNotificationStatistics(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification statistics retrieved successfully", stats));
    }

    /**
     * Get a specific notification by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        NotificationResponse notification = notificationService.getNotificationById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }

    /**
     * Mark a notification as read
     */
    @PostMapping("/{id}/mark-read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for current user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.deleteNotification(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    /**
     * Bulk delete notifications
     */
    @PostMapping("/bulk-delete")
    @Operation(summary = "Bulk delete notifications", description = "Delete multiple notifications")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteNotifications(
            @RequestBody List<Long> notificationIds,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.bulkDeleteNotifications(notificationIds, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications deleted successfully", null));
    }

    /**
     * Get notification preferences
     */
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences", description = "Get user notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreference>> getPreferences(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        NotificationPreference preferences = notificationService.getNotificationPreferences(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification preferences retrieved successfully", preferences));
    }

    /**
     * Update notification preferences
     */
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences", description = "Update user notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreference>> updatePreferences(
            @Valid @RequestBody NotificationPreference preferences,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        NotificationPreference updated = notificationService.updateNotificationPreferences(
                currentUser.getId(), preferences);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated successfully", updated));
    }

    /**
     * Send test notification (Admin only)
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Send test notification", description = "Send a test notification (Admin only)")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(
            @Valid @RequestBody SendNotificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        notificationService.sendTestNotification(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Test notification sent successfully", null));
    }

    /**
     * Export notifications
     */
    @GetMapping("/export")
    @Operation(summary = "Export notifications", description = "Export notifications to CSV/Excel")
    public ResponseEntity<byte[]> exportNotifications(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        byte[] exportData = notificationService.exportNotifications(
                currentUser.getId(), format, read, type);

        String filename = "notifications." + format;
        String contentType = format.equals("csv") ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", contentType)
                .body(exportData);
    }

    /**
     * Search notifications
     */
    @GetMapping("/search")
    @Operation(summary = "Search notifications", description = "Search notifications by keyword")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> searchNotifications(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {

        Page<NotificationResponse> results = notificationService.searchNotifications(
                currentUser.getId(), q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }
}