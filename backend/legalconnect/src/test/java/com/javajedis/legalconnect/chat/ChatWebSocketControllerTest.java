package com.javajedis.legalconnect.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.javajedis.legalconnect.chat.ChatWebSocketController.ChatWebSocketException;
import com.javajedis.legalconnect.chat.dto.MessageResponseDTO;
import com.javajedis.legalconnect.chat.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.common.utility.WebSocketUtil;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private Principal principal;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    private UUID testUserId;
    private String testUserIdString;
    private String testSessionId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserIdString = testUserId.toString();
        testSessionId = "test-session-123";
    }

    @Test
    void subscribeToChat_Success() {
        // Arrange
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        try (MockedStatic<WebSocketUtil> webSocketUtilMock = mockStatic(WebSocketUtil.class)) {
            webSocketUtilMock.when(() -> WebSocketUtil.extractUserIdFromPrincipal(principal))
                    .thenReturn(testUserIdString);

            // Act
            chatWebSocketController.subscribeToChat(principal, headerAccessor);

            // Assert
            verify(messagingTemplate).convertAndSendToUser(
                    eq(testUserIdString),
                    eq("/queue/chat"),
                    any(MessageResponseDTO.class)
            );
        }
    }

    @Test
    void subscribeToChat_UnauthenticatedUser_ThrowsException() {
        // Act & Assert
        ChatWebSocketException exception = assertThrows(ChatWebSocketException.class, () ->
                chatWebSocketController.subscribeToChat(null, headerAccessor)
        );

        assertEquals("Authentication required for chat subscription", exception.getMessage());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void subscribeToChat_UserNotConnected_ThrowsException() {
        // Arrange
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        try (MockedStatic<WebSocketUtil> webSocketUtilMock = mockStatic(WebSocketUtil.class)) {
            webSocketUtilMock.when(() -> WebSocketUtil.extractUserIdFromPrincipal(principal))
                    .thenReturn(testUserIdString);

            // Act & Assert
            ChatWebSocketException exception = assertThrows(ChatWebSocketException.class, () ->
                    chatWebSocketController.subscribeToChat(principal, headerAccessor)
            );

            assertEquals("WebSocket connection not properly established", exception.getMessage());
        }
    }

    @Test
    void handleChatPing_Success() {
        // Arrange
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);
        when(webSocketService.getActiveConnectionCount()).thenReturn(5);

        try (MockedStatic<WebSocketUtil> webSocketUtilMock = mockStatic(WebSocketUtil.class)) {
            webSocketUtilMock.when(() -> WebSocketUtil.extractUserIdFromPrincipal(principal))
                    .thenReturn(testUserIdString);

            // Act
            String result = chatWebSocketController.handleChatPing(principal);

            // Assert
            assertEquals("Chat Connected (Active connections: 5)", result);
        }
    }

    @Test
    void handleChatPing_UserDisconnected() {
        // Arrange
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);
        when(webSocketService.getActiveConnectionCount()).thenReturn(3);

        try (MockedStatic<WebSocketUtil> webSocketUtilMock = mockStatic(WebSocketUtil.class)) {
            webSocketUtilMock.when(() -> WebSocketUtil.extractUserIdFromPrincipal(principal))
                    .thenReturn(testUserIdString);

            // Act
            String result = chatWebSocketController.handleChatPing(principal);

            // Assert
            assertEquals("Chat Disconnected (Active connections: 3)", result);
        }
    }

    @Test
    void handleChatPing_UnauthenticatedUser_ThrowsException() {
        // Act & Assert
        ChatWebSocketException exception = assertThrows(ChatWebSocketException.class, () ->
                chatWebSocketController.handleChatPing(null)
        );

        assertEquals("Authentication required for chat ping requests", exception.getMessage());
    }

    @Test
    void markMessageAsRead_Success() {
        // Arrange
        String messageId = UUID.randomUUID().toString();
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        try (MockedStatic<WebSocketUtil> webSocketUtilMock = mockStatic(WebSocketUtil.class)) {
            webSocketUtilMock.when(() -> WebSocketUtil.extractUserIdFromPrincipal(principal))
                    .thenReturn(testUserIdString);

            // Act
            chatWebSocketController.markMessageAsRead(messageId, principal);

            // Assert
            verify(messagingTemplate).convertAndSendToUser(
                    testUserIdString,
                    "/queue/chat-status",
                    "Chat message mark-read request received for: " + messageId
            );
        }
    }

    @Test
    void markMessageAsRead_EmptyMessageId_ThrowsException() {
        // Arrange
        when(principal.getName()).thenReturn(testUserIdString);

        try (MockedStatic<WebSocketUtil> webSocketUtilMock = mockStatic(WebSocketUtil.class)) {
            webSocketUtilMock.when(() -> WebSocketUtil.extractUserIdFromPrincipal(principal))
                    .thenReturn(testUserIdString);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    chatWebSocketController.markMessageAsRead("", principal)
            );

            assertEquals("Message ID cannot be empty", exception.getMessage());
        }
    }

    @Test
    void markMessageAsRead_UnauthenticatedUser_ThrowsException() {
        // Act & Assert
        ChatWebSocketException exception = assertThrows(ChatWebSocketException.class, () ->
                chatWebSocketController.markMessageAsRead("some-id", null)
        );

        assertEquals("Authentication required for mark-read requests", exception.getMessage());
    }

    @Test
    void sendMessageToUser_Success() {
        // Arrange
        MessageResponseDTO message = new MessageResponseDTO();
        message.setId(UUID.randomUUID());
        message.setContent("Test message");
        message.setCreatedAt(OffsetDateTime.now());

        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        chatWebSocketController.sendMessageToUser(testUserId, message);

        // Assert
        verify(messagingTemplate).convertAndSend(
                "/topic/chat-" + testUserId,
                message
        );
    }

    @Test
    void sendMessageToUser_UserNotConnected_SkipsDelivery() {
        // Arrange
        MessageResponseDTO message = new MessageResponseDTO();
        message.setId(UUID.randomUUID());
        message.setContent("Test message");

        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        // Act
        chatWebSocketController.sendMessageToUser(testUserId, message);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void sendMessageToUser_NullUserId_ThrowsException() {
        // Arrange
        MessageResponseDTO message = new MessageResponseDTO();
        message.setContent("Test message");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatWebSocketController.sendMessageToUser(null, message)
        );

        assertEquals("UserId and message cannot be null", exception.getMessage());
    }

    @Test
    void sendMessageToUser_NullMessage_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatWebSocketController.sendMessageToUser(testUserId, null)
        );

        assertEquals("UserId and message cannot be null", exception.getMessage());
    }

    @Test
    void sendMessageToUser_EmptyContent_ThrowsException() {
        // Arrange
        MessageResponseDTO message = new MessageResponseDTO();
        message.setId(UUID.randomUUID());
        message.setContent("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatWebSocketController.sendMessageToUser(testUserId, message)
        );

        assertEquals("Message content cannot be empty", exception.getMessage());
    }

    @Test
    void sendUnreadCountUpdate_Success() {
        // Arrange
        UnreadCountResponseDTO unreadCount = new UnreadCountResponseDTO(5L);
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        chatWebSocketController.sendUnreadCountUpdate(testUserId, unreadCount);

        // Assert
        verify(messagingTemplate).convertAndSend(
                "/topic/chat-unread-" + testUserId,
                unreadCount
        );
    }

    @Test
    void sendUnreadCountUpdate_UserNotConnected_SkipsDelivery() {
        // Arrange
        UnreadCountResponseDTO unreadCount = new UnreadCountResponseDTO(5L);
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        // Act
        chatWebSocketController.sendUnreadCountUpdate(testUserId, unreadCount);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void sendUnreadCountUpdate_NullUserId_ThrowsException() {
        // Arrange
        UnreadCountResponseDTO unreadCount = new UnreadCountResponseDTO(5L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatWebSocketController.sendUnreadCountUpdate(null, unreadCount)
        );

        assertEquals("UserId and unreadCount cannot be null", exception.getMessage());
    }

    @Test
    void sendReadStatusUpdate_Success() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        chatWebSocketController.sendReadStatusUpdate(testUserId, messageId, true);

        // Assert
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chat-read-status-" + testUserId),
                any(ChatWebSocketController.ReadStatusUpdateDTO.class)
        );
    }

    @Test
    void sendReadStatusUpdate_UserNotConnected_SkipsDelivery() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        // Act
        chatWebSocketController.sendReadStatusUpdate(testUserId, messageId, true);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void sendReadStatusUpdate_NullSenderId_ThrowsException() {
        // Arrange
        UUID messageId = UUID.randomUUID();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatWebSocketController.sendReadStatusUpdate(null, messageId, true)
        );

        assertEquals("SenderId and messageId cannot be null", exception.getMessage());
    }

    @Test
    void handleException_IllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        String result = chatWebSocketController.handleException(exception, principal);

        // Assert
        assertEquals("Invalid request format. Please check your request parameters.", result);
    }

    @Test
    void handleException_ChatWebSocketException() {
        // Arrange
        ChatWebSocketException exception = new ChatWebSocketException("Chat error");

        // Act
        String result = chatWebSocketController.handleException(exception, principal);

        // Assert
        assertEquals("Chat error", result);
    }

    @Test
    void handleException_SecurityException() {
        // Arrange
        SecurityException exception = new SecurityException("Access denied");

        // Act
        String result = chatWebSocketController.handleException(exception, principal);

        // Assert
        assertEquals("Access denied. You don't have permission to perform this action.", result);
    }

    @Test
    void handleException_NullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null pointer");

        // Act
        String result = chatWebSocketController.handleException(exception, principal);

        // Assert
        assertEquals("Internal error occurred. Please try again.", result);
    }

    @Test
    void handleException_UnexpectedException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");

        // Act
        String result = chatWebSocketController.handleException(exception, principal);

        // Assert
        assertEquals("An unexpected error occurred while processing your chat request. Please try again.", result);
    }

    @Test
    void handleException_UnauthenticatedUser() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        String result = chatWebSocketController.handleException(exception, null);

        // Assert
        assertEquals("Invalid request format. Please check your request parameters.", result);
    }

    @Test
    void readStatusUpdateDTO_GettersAndSetters() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        ChatWebSocketController.ReadStatusUpdateDTO dto = new ChatWebSocketController.ReadStatusUpdateDTO(messageId, true);

        // Act & Assert
        assertEquals(messageId, dto.getMessageId());
        assertTrue(dto.isRead());

        UUID newMessageId = UUID.randomUUID();
        dto.setMessageId(newMessageId);
        dto.setRead(false);

        assertEquals(newMessageId, dto.getMessageId());
        assertFalse(dto.isRead());
    }

    @Test
    void chatWebSocketException_WithMessage() {
        // Arrange
        String message = "Test error message";

        // Act
        ChatWebSocketException exception = new ChatWebSocketException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void chatWebSocketException_WithMessageAndCause() {
        // Arrange
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        ChatWebSocketException exception = new ChatWebSocketException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}