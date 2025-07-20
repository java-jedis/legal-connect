package com.javajedis.legalconnect.notifications;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.common.utility.WebSocketUtil;
import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.exception.NotificationDeliveryException;
import com.javajedis.legalconnect.notifications.exception.WebSocketAuthenticationException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class NotificationWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketService webSocketService;
    private final NotificationService notificationService;

    public NotificationWebSocketController(SimpMessagingTemplate messagingTemplate,
                                           WebSocketService webSocketService,
                                           NotificationService notificationService) {
        this.messagingTemplate = messagingTemplate;
        this.webSocketService = webSocketService;
        this.notificationService = notificationService;
    }

    /**
     * Handles client subscription to notification updates.
     * This endpoint is called when a client subscribes to their notification queue.
     * Implements comprehensive error handling and validation.
     */
    @MessageMapping("/notifications/subscribe")
    @SendToUser("/queue/notifications")
    public void subscribeToNotifications(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            log.warn("Unauthenticated user attempted to subscribe to notifications");
            throw new WebSocketAuthenticationException("Authentication required for notification subscription");
        }

        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        UUID userUuid = UUID.fromString(userId);
        String sessionId = headerAccessor != null ? headerAccessor.getSessionId() : "unknown";

        log.info("User {} subscribed to notifications via WebSocket session {}", userUuid, sessionId);

        if (!webSocketService.isUserConnected(userUuid)) {
            log.warn("User {} attempted to subscribe but is not properly connected", userUuid);
            throw new NotificationDeliveryException("WebSocket connection not properly established");
        }

        NotificationResponseDTO confirmationMessage = new NotificationResponseDTO();
        confirmationMessage.setId(UUID.randomUUID());
        confirmationMessage.setContent("Successfully connected to notification service");
        confirmationMessage.setRead(true);
        confirmationMessage.setCreatedAt(java.time.OffsetDateTime.now());
        messagingTemplate.convertAndSendToUser(
                userUuid.toString(),
                "/queue/notifications",
                confirmationMessage
        );

        log.debug("Subscription confirmation sent to user: {}", userUuid);
    }

    /**
     * Handles client requests to check connection status.
     * This endpoint allows clients to verify their WebSocket connection is active.
     * Implements comprehensive error handling and validation.
     */
    @MessageMapping("/notifications/ping")
    @SendToUser("/queue/status")
    public String handlePing(Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated ping request received");
            throw new WebSocketAuthenticationException("Authentication required for ping requests");
        }
        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        UUID userUuid = UUID.fromString(userId);
        boolean isConnected = webSocketService.isUserConnected(userUuid);

        log.debug("Ping received from user {}, connection status: {}", userUuid, isConnected);
        String status = isConnected ? "Connected" : "Disconnected";
        int activeConnections = webSocketService.getActiveConnectionCount();

        return String.format("%s (Active connections: %d)", status, activeConnections);
    }

    /**
     * Handles client requests to mark notifications as read via WebSocket.
     * This provides an alternative to the REST API for marking notifications as read.
     * Implements comprehensive error handling and validation.
     */
    @MessageMapping("/notifications/mark-read")
    public void markNotificationAsRead(@Payload String notificationId, Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated user attempted to mark notification as read");
            throw new WebSocketAuthenticationException("Authentication required for mark-read requests");
        }
        if (notificationId == null || notificationId.trim().isEmpty()) {
            log.warn("User {} attempted to mark notification as read with empty notification ID", principal.getName());
            throw new IllegalArgumentException("Notification ID cannot be empty");
        }
        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        UUID userUuid = UUID.fromString(userId);
        UUID notificationUuid = UUID.fromString(notificationId.trim());

        log.debug("User {} requested to mark notification {} as read via WebSocket", userUuid, notificationUuid);

        if (!webSocketService.isUserConnected(userUuid)) {
            log.warn("User {} attempted mark-read but is not properly connected", userUuid);
            throw new NotificationDeliveryException("WebSocket connection not properly established");
        }

        notificationService.markAsRead(notificationUuid, userUuid);

        messagingTemplate.convertAndSendToUser(
                userUuid.toString(),
                "/queue/status",
                "Notification mark-read request received for: " + notificationId
        );

        log.debug("Mark-read acknowledgment sent to user: {}", userUuid);
    }

    /**
     * Handles WebSocket-specific exceptions and sends error messages back to the client.
     * This ensures that WebSocket errors are properly communicated to the client with
     * comprehensive error handling and logging.
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception exception, Principal principal) {
        String userId = principal != null ? principal.getName() : "unknown";
        String sessionInfo = principal != null ? " (user: " + userId + ")" : " (unauthenticated)";

        if (exception instanceof IllegalArgumentException) {
            log.warn("Invalid request format in WebSocket message{}: {}", sessionInfo, exception.getMessage());
            return "Invalid request format. Please check your request parameters.";
        } else if (exception instanceof WebSocketAuthenticationException) {
            log.warn("WebSocket authentication error{}: {}", sessionInfo, exception.getMessage());
            return "Authentication required. Please reconnect with valid credentials.";
        } else if (exception instanceof NotificationDeliveryException) {
            log.warn("Notification delivery error{}: {}", sessionInfo, exception.getMessage());
            return "Failed to deliver notification. Please try again or use the REST API.";
        } else if (exception instanceof SecurityException) {
            log.warn("Security error in WebSocket message{}: {}", sessionInfo, exception.getMessage());
            return "Access denied. You don't have permission to perform this action.";
        } else if (exception instanceof NullPointerException) {
            log.error("Null pointer exception in WebSocket message{}: {}", sessionInfo, exception.getMessage(), exception);
            return "Internal error occurred. Please try again.";
        } else {
            log.error("Unexpected WebSocket message handling error{}: {}", sessionInfo, exception.getMessage(), exception);
            return "An unexpected error occurred while processing your request. Please try again.";
        }
    }

    /**
     * Sends a notification directly to a specific user's WebSocket connection.
     * This method is used internally by the notification service.
     * Implements comprehensive error handling and validation.
     */
    public void sendNotificationToUser(UUID userId, NotificationResponseDTO notification) {
        if (userId == null || notification == null) {
            log.warn("Cannot send notification via WebSocket: userId or notification is null");
            throw new IllegalArgumentException("UserId and notification cannot be null");
        }
        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            log.warn("Attempted to send notification with empty content to user: {}", userId);
            throw new IllegalArgumentException("Notification content cannot be empty");
        }
        if (!webSocketService.isUserConnected(userId)) {
            log.debug("User {} is not connected, cannot send WebSocket notification", userId);
            throw new NotificationDeliveryException("User is not connected via WebSocket");
        }

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );

        log.debug("Notification sent to user {} via WebSocket message controller: {}",
                userId, notification.getContent());
    }

    /**
     * Broadcasts a system-wide notification to all connected users.
     * This method is used for system announcements or maintenance notifications.
     * Implements comprehensive error handling and validation.
     */
    public void broadcastNotification(NotificationResponseDTO notification) {
        if (notification == null) {
            log.warn("Cannot broadcast notification: notification is null");
            throw new IllegalArgumentException("Notification cannot be null for broadcast");
        }
        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            log.warn("Attempted to broadcast notification with empty content");
            throw new IllegalArgumentException("Notification content cannot be empty for broadcast");
        }

        int activeConnections = webSocketService.getActiveConnectionCount();
        if (activeConnections == 0) {
            log.debug("No active WebSocket connections for broadcast");
            return;
        }

        messagingTemplate.convertAndSend("/topic/notifications", notification);

        log.info("System notification broadcasted to {} connected users: {}",
                activeConnections, notification.getContent());
    }

}