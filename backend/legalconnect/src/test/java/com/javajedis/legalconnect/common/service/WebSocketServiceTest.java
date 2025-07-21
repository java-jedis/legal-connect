package com.javajedis.legalconnect.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.exception.NotificationDeliveryException;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketService Tests")
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SessionConnectEvent sessionConnectEvent;

    @Mock
    private SessionDisconnectEvent sessionDisconnectEvent;

    @Mock
    private StompHeaderAccessor headerAccessor;

    @InjectMocks
    private WebSocketService webSocketService;

    private UUID testUserId;
    private String testSessionId;
    private NotificationResponseDTO testNotification;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testSessionId = "test-session-123";
        
        testNotification = new NotificationResponseDTO();
        testNotification.setId(UUID.randomUUID());
        testNotification.setContent("Test notification content");
        testNotification.setRead(false);
        testNotification.setCreatedAt(OffsetDateTime.now());

        sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", testUserId.toString());
    }

    @Test
    @DisplayName("Should send notification to connected user successfully")
    void sendNotificationToUser_Success() {
        // Arrange
        simulateUserConnection();

        // Act
        boolean result = webSocketService.sendNotificationToUser(testUserId, testNotification);

        // Assert
        assertTrue(result);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/user-" + testUserId),
            (Object) eq(testNotification)
        );
    }

    @Test
    @DisplayName("Should return false when user is not connected")
    void sendNotificationToUser_UserNotConnected() {
        // Act
        boolean result = webSocketService.sendNotificationToUser(testUserId, testNotification);

        // Assert
        assertFalse(result);
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @DisplayName("Should throw exception when userId is null")
    void sendNotificationToUser_NullUserId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> webSocketService.sendNotificationToUser(null, testNotification)
        );
        assertEquals("UserId and notification cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when notification is null")
    void sendNotificationToUser_NullNotification() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> webSocketService.sendNotificationToUser(testUserId, null)
        );
        assertEquals("UserId and notification cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when notification content is empty")
    void sendNotificationToUser_EmptyContent() {
        // Arrange
        simulateUserConnection();
        testNotification.setContent("");

        // Act & Assert
        NotificationDeliveryException exception = assertThrows(
            NotificationDeliveryException.class,
            () -> webSocketService.sendNotificationToUser(testUserId, testNotification)
        );
        assertEquals("Cannot send notification with empty content", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when notification content is null")
    void sendNotificationToUser_NullContent() {
        // Arrange
        simulateUserConnection();
        testNotification.setContent(null);

        // Act & Assert
        NotificationDeliveryException exception = assertThrows(
            NotificationDeliveryException.class,
            () -> webSocketService.sendNotificationToUser(testUserId, testNotification)
        );
        assertEquals("Cannot send notification with empty content", exception.getMessage());
    }

    @Test
    @DisplayName("Should return true when user is connected")
    void isUserConnected_UserConnected() {
        // Arrange
        simulateUserConnection();

        // Act
        boolean result = webSocketService.isUserConnected(testUserId);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not connected")
    void isUserConnected_UserNotConnected() {
        // Act
        boolean result = webSocketService.isUserConnected(testUserId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return correct active connection count")
    void getActiveConnectionCount() {
        // Arrange
        assertEquals(0, webSocketService.getActiveConnectionCount());
        
        simulateUserConnection();

        // Act & Assert
        assertEquals(1, webSocketService.getActiveConnectionCount());
    }

    @Test
    @DisplayName("Should handle WebSocket connect event successfully")
    void handleWebSocketConnectListener_Success() {
        // Arrange
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);

            // Assert
            assertTrue(webSocketService.isUserConnected(testUserId));
            assertEquals(1, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should handle WebSocket connect event with null session ID")
    void handleWebSocketConnectListener_NullSessionId() {
        // Arrange
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(null);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);

            // Assert
            assertFalse(webSocketService.isUserConnected(testUserId));
            assertEquals(0, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should handle WebSocket connect event with null session attributes")
    void handleWebSocketConnectListener_NullSessionAttributes() {
        // Arrange
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(headerAccessor.getSessionAttributes()).thenReturn(null);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);

            // Assert
            assertFalse(webSocketService.isUserConnected(testUserId));
            assertEquals(0, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should handle WebSocket connect event with null userId")
    void handleWebSocketConnectListener_NullUserId() {
        // Arrange
        sessionAttributes.put("userId", null);
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);

            // Assert
            assertFalse(webSocketService.isUserConnected(testUserId));
            assertEquals(0, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should handle WebSocket connect event with invalid userId type")
    void handleWebSocketConnectListener_InvalidUserIdType() {
        // Arrange
        sessionAttributes.put("userId", 12345); // Invalid type
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);

            // Assert
            assertFalse(webSocketService.isUserConnected(testUserId));
            assertEquals(0, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should replace existing session when user connects with new session")
    void handleWebSocketConnectListener_ReplaceExistingSession() {
        // Arrange - First connection
        simulateUserConnection();
        assertEquals(1, webSocketService.getActiveConnectionCount());

        // Arrange - Second connection with different session
        String newSessionId = "new-session-456";
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(newSessionId);
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);

            // Assert
            assertTrue(webSocketService.isUserConnected(testUserId));
            assertEquals(1, webSocketService.getActiveConnectionCount()); // Still 1, replaced
        }
    }

    @Test
    @DisplayName("Should handle WebSocket disconnect event successfully")
    void handleWebSocketDisconnectListener_Success() {
        // Arrange
        simulateUserConnection();
        assertTrue(webSocketService.isUserConnected(testUserId));

        when(sessionDisconnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketDisconnectListener(sessionDisconnectEvent);

            // Assert
            assertFalse(webSocketService.isUserConnected(testUserId));
            assertEquals(0, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should handle WebSocket disconnect event with null session ID")
    void handleWebSocketDisconnectListener_NullSessionId() {
        // Arrange
        simulateUserConnection();
        when(sessionDisconnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(null);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketDisconnectListener(sessionDisconnectEvent);

            // Assert
            assertTrue(webSocketService.isUserConnected(testUserId)); // Should still be connected
            assertEquals(1, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should handle WebSocket disconnect event for unknown session")
    void handleWebSocketDisconnectListener_UnknownSession() {
        // Arrange
        String unknownSessionId = "unknown-session";
        when(sessionDisconnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(unknownSessionId);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);

            // Act
            webSocketService.handleWebSocketDisconnectListener(sessionDisconnectEvent);

            // Assert
            assertEquals(0, webSocketService.getActiveConnectionCount());
        }
    }

    @Test
    @DisplayName("Should manually disconnect user successfully")
    void disconnectUser_Success() {
        // Arrange
        simulateUserConnection();
        assertTrue(webSocketService.isUserConnected(testUserId));

        // Act
        boolean result = webSocketService.disconnectUser(testUserId);

        // Assert
        assertTrue(result);
        assertFalse(webSocketService.isUserConnected(testUserId));
        assertEquals(0, webSocketService.getActiveConnectionCount());
    }

    @Test
    @DisplayName("Should return false when trying to disconnect non-connected user")
    void disconnectUser_UserNotConnected() {
        // Act
        boolean result = webSocketService.disconnectUser(testUserId);

        // Assert
        assertFalse(result);
        assertEquals(0, webSocketService.getActiveConnectionCount());
    }

    @Test
    @DisplayName("Should broadcast message to all users successfully")
    void broadcastToAllUsers_Success() {
        // Arrange
        simulateUserConnection();
        String broadcastMessage = "System announcement";

        // Act
        webSocketService.broadcastToAllUsers(broadcastMessage);

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/broadcast"), (Object) eq(broadcastMessage));
    }

    @Test
    @DisplayName("Should handle broadcast with no active connections")
    void broadcastToAllUsers_NoActiveConnections() {
        // Arrange
        String broadcastMessage = "System announcement";

        // Act
        webSocketService.broadcastToAllUsers(broadcastMessage);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @DisplayName("Should throw exception when broadcasting null message")
    void broadcastToAllUsers_NullMessage() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> webSocketService.broadcastToAllUsers(null)
        );
        assertEquals("Broadcast message cannot be null", exception.getMessage());
    }

    private void simulateUserConnection() {
        when(sessionConnectEvent.getMessage()).thenReturn(mock(org.springframework.messaging.Message.class));
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        
        try (var mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(any())).thenReturn(headerAccessor);
            webSocketService.handleWebSocketConnectListener(sessionConnectEvent);
        }
    }
}