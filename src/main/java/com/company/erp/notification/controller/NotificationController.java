package com.company.erp.notification.controller;

import com.company.erp.common.dto.ApiResponse;
import com.company.erp.notification.entity.Notification;
import com.company.erp.notification.entity.NotificationPreferences;
import com.company.erp.notification.service.NotificationService;
import com.company.erp.user.entity.User;
import com.company.erp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean read,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId(), page, size);
            
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            long count = notificationService.getUnreadNotificationCount(currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch unread count: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationStats(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", notificationService.getUserNotifications(currentUser.getId(), 0, 1000).size());
            stats.put("unread", notificationService.getUnreadNotificationCount(currentUser.getId()));
            stats.put("read", (Integer) stats.get("total") - (Long) stats.get("unread"));
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch notification stats: " + e.getMessage()));
        }
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferences>> getPreferences(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            NotificationPreferences preferences = notificationService.getUserPreferences(currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(preferences));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch preferences: " + e.getMessage()));
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferences>> updatePreferences(
            @RequestBody NotificationPreferences preferences,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            NotificationPreferences updated = notificationService.updateUserPreferences(currentUser.getId(), preferences);
            
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update preferences: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            notificationService.markAsRead(id, currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to mark as read: " + e.getMessage()));
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            notificationService.markAllAsRead(currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to mark all as read: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            notificationService.deleteNotification(id, currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete notification: " + e.getMessage()));
        }
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
