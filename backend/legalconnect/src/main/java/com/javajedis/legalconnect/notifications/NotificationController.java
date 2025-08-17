package com.javajedis.legalconnect.notifications;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.notifications.dto.*;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "8. Notifications", description = "Notification management endpoints")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final String AUTHENTICATION_REQUIRED_MSG = "Authentication required";

    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final UserRepo userRepo;

    /**
     * Send a notification to a specific user. This endpoint is intended for internal service calls.
     */
    @Operation(summary = "Send notification", description = "Sends a notification to a specific user. Intended for internal service calls.")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> sendNotification(@Valid @RequestBody SendNotificationDTO notificationData) {
        log.info("POST /notifications/send called for receiver: {}", notificationData.getReceiverId());
        return notificationService.sendNotification(notificationData);
    }

    /**
     * Get notifications for the authenticated user with pagination support.
     */
    @Operation(summary = "Get user notifications", description = "Retrieves notifications for the authenticated user with pagination support.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/")
    public ResponseEntity<ApiResponse<NotificationListResponseDTO>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("GET /notifications/ called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("GET /notifications/ called for user: {} with page={}, size={}, unreadOnly={}",
                currentUser.getId(), page, size, unreadOnly);
        return notificationService.getUserNotifications(currentUser.getId(), page, size, unreadOnly);
    }

    /**
     * Get the count of unread notifications for the authenticated user.
     */
    @Operation(summary = "Get unread notification count", description = "Retrieves the count of unread notifications for the authenticated user.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponseDTO>> getUnreadCount() {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("GET /notifications/unread-count called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("GET /notifications/unread-count called for user: {}", currentUser.getId());
        return notificationService.getUnreadCount(currentUser.getId());
    }

    /**
     * Mark a specific notification as read for the authenticated user.
     */
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read for the authenticated user.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markNotificationAsRead(@PathVariable UUID id) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("PUT /notifications/{}/read called but no authenticated user found", id);
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("PUT /notifications/{}/read called for user: {}", id, currentUser.getId());
        return notificationService.markAsRead(id, currentUser.getId());
    }

    /**
     * Mark all notifications as read for the authenticated user.
     */
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for the authenticated user.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<UnreadCountResponseDTO>> markAllNotificationsAsRead() {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("PUT /notifications/mark-all-read called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("PUT /notifications/mark-all-read called for user: {}", currentUser.getId());
        return notificationService.markAllAsRead(currentUser.getId());
    }

    /**
     * Get all notification preferences for the authenticated user..
     */
    @Operation(summary = "Get notification preferences", description = "Retrieves all notification preferences for the authenticated user.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceListResponseDTO>> getNotificationPreferences() {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("GET /notifications/preferences called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("GET /notifications/preferences called for user: {}", currentUser.getId());
        return notificationPreferenceService.getAllPreferences();
    }

    /**
     * Update a specific notification preference for the authenticated user.
     */
    @Operation(summary = "Update notification preference", description = "Updates a specific notification preference for the authenticated user.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/preferences/{type}")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> updateNotificationPreference(
            @PathVariable NotificationType type,
            @Valid @RequestBody UpdateNotificationPreferenceDTO updateData) {

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("PUT /notifications/preferences/{} called but no authenticated user found", type);
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("PUT /notifications/preferences/{} called for user: {} with emailEnabled={}, webPushEnabled={}",
                type, currentUser.getId(), updateData.isEmailEnabled(), updateData.isWebPushEnabled());

        return notificationPreferenceService.updateNotificationPref(type, updateData);
    }
}