package com.javajedis.legalconnect.chat;

import com.javajedis.legalconnect.chat.dto.MessageResponseDTO;
import com.javajedis.legalconnect.chat.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.common.utility.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketService webSocketService;

    /**
     * Handles client subscription to chat messages.
     * This endpoint is called when a client subscribes to their chat message queue.
     * Implements comprehensive error handling and validation.
     */
    @MessageMapping("/chat/subscribe")
    @SendToUser("/queue/chat")
    public void subscribeToChat(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            log.warn("Unauthenticated user attempted to subscribe to chat");
            throw new ChatWebSocketException("Authentication required for chat subscription");
        }

        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        UUID userUuid = UUID.fromString(userId);
        String sessionId = headerAccessor != null ? headerAccessor.getSessionId() : "unknown";

        log.info("User {} subscribed to chat via WebSocket session {}", userUuid, sessionId);

        if (!webSocketService.isUserConnected(userUuid)) {
            log.warn("User {} attempted to subscribe but is not properly connected", userUuid);
            throw new ChatWebSocketException("WebSocket connection not properly established");
        }

        MessageResponseDTO confirmationMessage = new MessageResponseDTO();
        confirmationMessage.setId(UUID.randomUUID());
        confirmationMessage.setContent("Successfully connected to chat service");
        confirmationMessage.setRead(true);
        confirmationMessage.setCreatedAt(java.time.OffsetDateTime.now());

        messagingTemplate.convertAndSendToUser(
                userUuid.toString(),
                "/queue/chat",
                confirmationMessage
        );

        log.debug("Chat subscription confirmation sent to user: {}", userUuid);
    }

    /**
     * Handles client requests to check chat connection status.
     * This endpoint allows clients to verify their WebSocket connection is active for chat.
     */
    @MessageMapping("/chat/ping")
    @SendToUser("/queue/chat-status")
    public String handleChatPing(Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated ping request received for chat");
            throw new ChatWebSocketException("Authentication required for chat ping requests");
        }

        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        UUID userUuid = UUID.fromString(userId);
        boolean isConnected = webSocketService.isUserConnected(userUuid);

        log.debug("Chat ping received from user {}, connection status: {}", userUuid, isConnected);
        String status = isConnected ? "Connected" : "Disconnected";
        int activeConnections = webSocketService.getActiveConnectionCount();

        return String.format("Chat %s (Active connections: %d)", status, activeConnections);
    }

    /**
     * Handles client requests to mark messages as read via WebSocket.
     * This provides an alternative to the REST API for marking messages as read.
     */
    @MessageMapping("/chat/mark-read")
    public void markMessageAsRead(@Payload String messageId, Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated user attempted to mark chat message as read");
            throw new ChatWebSocketException("Authentication required for mark-read requests");
        }

        if (messageId == null || messageId.trim().isEmpty()) {
            log.warn("User {} attempted to mark message as read with empty message ID", principal.getName());
            throw new IllegalArgumentException("Message ID cannot be empty");
        }

        String userId = WebSocketUtil.extractUserIdFromPrincipal(principal);
        UUID userUuid = UUID.fromString(userId);
        UUID messageUuid = UUID.fromString(messageId.trim());

        log.debug("User {} requested to mark chat message {} as read via WebSocket", userUuid, messageUuid);

        if (!webSocketService.isUserConnected(userUuid)) {
            log.warn("User {} attempted mark-read but is not properly connected", userUuid);
            throw new ChatWebSocketException("WebSocket connection not properly established");
        }

        // Send acknowledgment (actual mark-read logic would be handled by ChatService)
        messagingTemplate.convertAndSendToUser(
                userUuid.toString(),
                "/queue/chat-status",
                "Chat message mark-read request received for: " + messageId
        );

        log.debug("Chat mark-read acknowledgment sent to user: {}", userUuid);
    }

    /**
     * Sends a chat message directly to a specific user's WebSocket connection.
     * This method is used internally by the chat service for real-time message delivery.
     */
    public void sendMessageToUser(UUID userId, MessageResponseDTO message) {
        if (userId == null || message == null) {
            log.warn("Cannot send chat message via WebSocket: userId or message is null");
            throw new IllegalArgumentException("UserId and message cannot be null");
        }

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            log.warn("Attempted to send chat message with empty content to user: {}", userId);
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        if (!webSocketService.isUserConnected(userId)) {
            log.debug("User {} is not connected, cannot send WebSocket chat message", userId);
            return;
        }

        String topicDestination = "/topic/chat-" + userId;
        messagingTemplate.convertAndSend(topicDestination, message);

        log.debug("Chat message sent to user {} via WebSocket topic: {}", userId, message.getContent());
    }

    /**
     * Sends an unread count update to a specific user's WebSocket connection.
     * This method is used to notify users of changes in their unread message count.
     */
    public void sendUnreadCountUpdate(UUID userId, UnreadCountResponseDTO unreadCount) {
        if (userId == null || unreadCount == null) {
            log.warn("Cannot send unread count update via WebSocket: userId or unreadCount is null");
            throw new IllegalArgumentException("UserId and unreadCount cannot be null");
        }

        if (!webSocketService.isUserConnected(userId)) {
            log.debug("User {} is not connected, cannot send WebSocket unread count update", userId);
            return;
        }

        String topicDestination = "/topic/chat-unread-" + userId;
        messagingTemplate.convertAndSend(topicDestination, unreadCount);

        log.debug("Unread count update sent to user {} via WebSocket: {}", userId, unreadCount.getTotalUnreadCount());
    }

    /**
     * Sends a read status update to a specific user's WebSocket connection.
     * This method notifies the sender when their message has been read.
     */
    public void sendReadStatusUpdate(UUID senderId, UUID messageId, boolean isRead) {
        if (senderId == null || messageId == null) {
            log.warn("Cannot send read status update via WebSocket: senderId or messageId is null");
            throw new IllegalArgumentException("SenderId and messageId cannot be null");
        }

        if (!webSocketService.isUserConnected(senderId)) {
            log.debug("Sender {} is not connected, cannot send WebSocket read status update", senderId);
            return;
        }

        ReadStatusUpdateDTO readStatusUpdate = new ReadStatusUpdateDTO(messageId, isRead);
        String topicDestination = "/topic/chat-read-status-" + senderId;
        messagingTemplate.convertAndSend(topicDestination, readStatusUpdate);

        log.debug("Read status update sent to sender {} for message {}: read={}", senderId, messageId, isRead);
    }

    /**
     * Handles WebSocket-specific exceptions and sends error messages back to the client.
     * This ensures that WebSocket errors are properly communicated to the client.
     */
    @MessageExceptionHandler
    @SendToUser("/queue/chat-errors")
    public String handleException(Exception exception, Principal principal) {
        String userId = principal != null ? principal.getName() : "unknown";
        String sessionInfo = principal != null ? " (user: " + userId + ")" : " (unauthenticated)";

        if (exception instanceof IllegalArgumentException) {
            log.warn("Invalid request format in chat WebSocket message{}: {}", sessionInfo, exception.getMessage());
            return "Invalid request format. Please check your request parameters.";
        } else if (exception instanceof ChatWebSocketException) {
            log.warn("Chat WebSocket error{}: {}", sessionInfo, exception.getMessage());
            return exception.getMessage();
        } else if (exception instanceof SecurityException) {
            log.warn("Security error in chat WebSocket message{}: {}", sessionInfo, exception.getMessage());
            return "Access denied. You don't have permission to perform this action.";
        } else if (exception instanceof NullPointerException) {
            log.error("Null pointer exception in chat WebSocket message{}: {}", sessionInfo, exception.getMessage(), exception);
            return "Internal error occurred. Please try again.";
        } else {
            log.error("Unexpected chat WebSocket message handling error{}: {}", sessionInfo, exception.getMessage(), exception);
            return "An unexpected error occurred while processing your chat request. Please try again.";
        }
    }

    /**
     * DTO for read status updates sent via WebSocket.
     */
    public static class ReadStatusUpdateDTO {
        private UUID messageId;
        private boolean isRead;

        public ReadStatusUpdateDTO(UUID messageId, boolean isRead) {
            this.messageId = messageId;
            this.isRead = isRead;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean read) {
            isRead = read;
        }
    }

    /**
     * Custom exception for chat WebSocket operations.
     */
    public static class ChatWebSocketException extends RuntimeException {
        public ChatWebSocketException(String message) {
            super(message);
        }

        public ChatWebSocketException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}