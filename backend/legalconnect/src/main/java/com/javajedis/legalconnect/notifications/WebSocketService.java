package com.javajedis.legalconnect.notifications;

import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.exception.NotificationDeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for managing active connections and real-time notification delivery.
 * Maintains active user sessions and provides methods to send notifications to specific users.
 */
@Slf4j
@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // Store active user sessions: userId -> sessionId
    private final ConcurrentHashMap<UUID, String> activeUserSessions = new ConcurrentHashMap<>();

    // Store session to user mapping: sessionId -> userId (for cleanup)
    private final ConcurrentHashMap<String, UUID> sessionToUserMap = new ConcurrentHashMap<>();

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a notification to a specific user via WebSocket if they are connected.
     * Implements proper error handling and graceful degradation.
     */
    public boolean sendNotificationToUser(UUID userId, NotificationResponseDTO notification) {
        if (userId == null || notification == null) {
            log.warn("Cannot send notification: userId or notification is null");
            throw new IllegalArgumentException("UserId and notification cannot be null");
        }

        String sessionId = activeUserSessions.get(userId);
        if (sessionId == null) {
            log.debug("User {} is not connected via WebSocket, skipping real-time delivery", userId);
            return false;
        }

        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            log.warn("Attempted to send notification with empty content to user: {}", userId);
            throw new NotificationDeliveryException("Cannot send notification with empty content");
        }

        String topicDestination = "/topic/user-" + userId;
        messagingTemplate.convertAndSend(topicDestination, notification);

        log.debug("Notification sent to user {} via WebSocket topic: {}", userId, notification.getContent());
        return true;
    }

    /**
     * Checks if a user is currently connected via WebSocket.
     */
    public boolean isUserConnected(UUID userId) {
        return activeUserSessions.containsKey(userId);
    }

    /**
     * Gets the number of active WebSocket connections.
     */
    public int getActiveConnectionCount() {
        return activeUserSessions.size();
    }

    /**
     * Handles WebSocket connection events.
     * Registers the user session when they connect with comprehensive error handling.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId == null) {
            log.warn("WebSocket connection event received with null session ID");
            return;
        }

        if (headerAccessor.getSessionAttributes() == null) {
            log.warn("WebSocket connection event received with null session attributes for session: {}", sessionId);
            return;
        }

        Object userIdObj = headerAccessor.getSessionAttributes().get("userId");

        if (userIdObj instanceof String string) {
            UUID userId = UUID.fromString(string);

            String existingSessionId = activeUserSessions.get(userId);
            if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
                log.info("User {} had existing WebSocket session {}, replacing with new session {}",
                        userId, existingSessionId, sessionId);
                sessionToUserMap.remove(existingSessionId);
            }

            activeUserSessions.put(userId, sessionId);
            sessionToUserMap.put(sessionId, userId);

            log.info("User {} connected via WebSocket with session {}", userId, sessionId);
            log.debug("Active WebSocket connections: {}", activeUserSessions.size());
        } else if (userIdObj == null) {
            log.warn("No userId found in WebSocket session attributes for session: {}", sessionId);
        } else {
            log.warn("Invalid userId type in WebSocket session {}: expected String, got {}",
                    sessionId, userIdObj.getClass().getSimpleName());
        }
    }

    /**
     * Handles WebSocket disconnection events.
     * Cleans up user session when they disconnect with comprehensive error handling.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId == null) {
            log.warn("WebSocket disconnection event received with null session ID");
            return;
        }

        UUID userId = sessionToUserMap.remove(sessionId);
        if (userId != null) {
            String removedSessionId = activeUserSessions.remove(userId);
            if (removedSessionId != null && !removedSessionId.equals(sessionId)) {
                log.warn("Session ID mismatch during cleanup for user {}: expected {}, found {}",
                        userId, sessionId, removedSessionId);
            }

            log.info("User {} disconnected from WebSocket session {}", userId, sessionId);
            log.debug("Active WebSocket connections: {}", activeUserSessions.size());
        } else {
            log.debug("WebSocket session {} disconnected (no associated user)", sessionId);
        }
    }

    /**
     * Manually disconnects a user session (for administrative purposes).
     */
    public boolean disconnectUser(UUID userId) {
        String sessionId = activeUserSessions.remove(userId);
        if (sessionId != null) {
            sessionToUserMap.remove(sessionId);
            log.info("Manually disconnected user {} from WebSocket", userId);
            return true;
        }
        return false;
    }

    /**
     * Sends a broadcast message to all connected users.
     * This method is provided for future use cases like system announcements.
     * Implements proper error handling and validation.
     */
    public void broadcastToAllUsers(Object message) {
        if (message == null) {
            log.warn("Cannot broadcast null message");
            throw new IllegalArgumentException("Broadcast message cannot be null");
        }

        int activeConnections = getActiveConnectionCount();
        if (activeConnections == 0) {
            log.debug("No active WebSocket connections for broadcast");
            return;
        }

        messagingTemplate.convertAndSend("/topic/broadcast", message);
        log.info("Broadcast message sent to {} connected users", activeConnections);
    }
}