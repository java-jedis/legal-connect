package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
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
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.exception.NotificationDeliveryException;
import com.javajedis.legalconnect.notifications.exception.WebSocketAuthenticationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationWebSocketController Tests")
class NotificationWebSocketControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private Principal principal;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationWebSocketController controller;

    private UUID testUserId;
    private String testSessionId;
    private NotificationResponseDTO testNotification;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testSessionId = "test-session-123";
        
        testNotification = new NotificationResponseDTO();
        testNotification.setId(UUID.randomUUID());
        testNotification.setContent("Test notification content");
        testNotification.setRead(false);
        testNotification.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should handle subscription to notifications successfully")
    void subscribeToNotifications_Success() {
        // Arrange
        when(principal.getName()).thenReturn(testUserId.toString());
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> controller.subscribeToNotifications(principal, headerAccessor));

        // Assert
        verify(messagingTemplate).convertAndSendToUser(
            eq(testUserId.toString()),
            eq("/queue/notifications"),
            any(NotificationResponseDTO.class)
        );
    }

    @Test
    @DisplayName("Should throw exception when subscribing without authentication")
    void subscribeToNotifications_NoAuthentication() {
        // Act & Assert
        WebSocketAuthenticationException exception = assertThrows(
            WebSocketAuthenticationException.class,
            () -> controller.subscribeToNotifications(null, headerAccessor)
        );
        assertEquals("Authentication required for notification subscription", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user is not properly connected")
    void subscribeToNotifications_UserNotConnected() {
        // Arrange
        when(principal.getName()).thenReturn(testUserId.toString());
        when(headerAccessor.getSessionId()).thenReturn(testSessionId);
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        // Act & Assert
        NotificationDeliveryException exception = assertThrows(
            NotificationDeliveryException.class,
            () -> controller.subscribeToNotifications(principal, headerAccessor)
        );
        assertEquals("WebSocket connection not properly established", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle subscription with null header accessor")
    void subscribeToNotifications_NullHeaderAccessor() {
        // Arrange
        when(principal.getName()).thenReturn(testUserId.toString());
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> controller.subscribeToNotifications(principal, null));

        // Assert
        verify(messagingTemplate).convertAndSendToUser(
            eq(testUserId.toString()),
            eq("/queue/notifications"),
            any(NotificationResponseDTO.class)
        );
    }

    @Test
    @DisplayName("Should handle ping request successfully")
    void handlePing_Success() {
        // Arrange
        when(principal.getName()).thenReturn(testUserId.toString());
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);
        when(webSocketService.getActiveConnectionCount()).thenReturn(5);

        // Act
        String result = controller.handlePing(principal);

        // Assert
        assertEquals("Connected (Active connections: 5)", result);
    }

    @Test
    @DisplayName("Should handle ping request for disconnected user")
    void handlePing_UserDisconnected() {
        // Arrange
        when(principal.getName()).thenReturn(testUserId.toString());
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);
        when(webSocketService.getActiveConnectionCount()).thenReturn(3);

        // Act
        String result = controller.handlePing(principal);

        // Assert
        assertEquals("Disconnected (Active connections: 3)", result);
    }

    @Test
    @DisplayName("Should throw exception when ping without authentication")
    void handlePing_NoAuthentication() {
        // Act & Assert
        WebSocketAuthenticationException exception = assertThrows(
            WebSocketAuthenticationException.class,
            () -> controller.handlePing(null)
        );
        assertEquals("Authentication required for ping requests", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void markNotificationAsRead_Success() {
        // Arrange
        String notificationId = UUID.randomUUID().toString();
        when(principal.getName()).thenReturn(testUserId.toString());
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> controller.markNotificationAsRead(notificationId, principal));

        // Assert
        verify(notificationService).markAsRead(UUID.fromString(notificationId), testUserId);
        verify(messagingTemplate).convertAndSendToUser(
            testUserId.toString(),
            "/queue/status",
            "Notification mark-read request received for: " + notificationId
        );
    }

    @Test
    @DisplayName("Should throw exception when marking notification as read without authentication")
    void markNotificationAsRead_NoAuthentication() {
        // Arrange
        String notificationId = UUID.randomUUID().toString();

        // Act & Assert
        WebSocketAuthenticationException exception = assertThrows(
            WebSocketAuthenticationException.class,
            () -> controller.markNotificationAsRead(notificationId, null)
        );
        assertEquals("Authentication required for mark-read requests", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when marking notification with empty ID")
    void markNotificationAsRead_EmptyNotificationId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.markNotificationAsRead("", principal)
        );
        assertEquals("Notification ID cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when marking notification with null ID")
    void markNotificationAsRead_NullNotificationId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.markNotificationAsRead(null, principal)
        );
        assertEquals("Notification ID cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user is not properly connected for mark-read")
    void markNotificationAsRead_UserNotConnected() {
        // Arrange
        String notificationId = UUID.randomUUID().toString();
        when(principal.getName()).thenReturn(testUserId.toString());
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        // Act & Assert
        NotificationDeliveryException exception = assertThrows(
            NotificationDeliveryException.class,
            () -> controller.markNotificationAsRead(notificationId, principal)
        );
        assertEquals("WebSocket connection not properly established", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException in exception handler")
    void handleException_IllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid request");

        // Act
        String result = controller.handleException(exception, principal);

        // Assert
        assertEquals("Invalid request format. Please check your request parameters.", result);
    }

    @Test
    @DisplayName("Should handle WebSocketAuthenticationException in exception handler")
    void handleException_WebSocketAuthenticationException() {
        // Arrange
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException("Auth failed");

        // Act
        String result = controller.handleException(exception, principal);

        // Assert
        assertEquals("Authentication required. Please reconnect with valid credentials.", result);
    }

    @Test
    @DisplayName("Should handle NotificationDeliveryException in exception handler")
    void handleException_NotificationDeliveryException() {
        // Arrange
        NotificationDeliveryException exception = new NotificationDeliveryException("Delivery failed");

        // Act
        String result = controller.handleException(exception, principal);

        // Assert
        assertEquals("Failed to deliver notification. Please try again or use the REST API.", result);
    }

    @Test
    @DisplayName("Should handle SecurityException in exception handler")
    void handleException_SecurityException() {
        // Arrange
        SecurityException exception = new SecurityException("Access denied");

        // Act
        String result = controller.handleException(exception, principal);

        // Assert
        assertEquals("Access denied. You don't have permission to perform this action.", result);
    }

    @Test
    @DisplayName("Should handle NullPointerException in exception handler")
    void handleException_NullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null pointer");

        // Act
        String result = controller.handleException(exception, principal);

        // Assert
        assertEquals("Internal error occurred. Please try again.", result);
    }

    @Test
    @DisplayName("Should handle generic exception in exception handler")
    void handleException_GenericException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");

        // Act
        String result = controller.handleException(exception, principal);

        // Assert
        assertEquals("An unexpected error occurred while processing your request. Please try again.", result);
    }

    @Test
    @DisplayName("Should handle exception with null principal")
    void handleException_NullPrincipal() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid request");

        // Act
        String result = controller.handleException(exception, null);

        // Assert
        assertEquals("Invalid request format. Please check your request parameters.", result);
    }

    @Test
    @DisplayName("Should send notification to user successfully")
    void sendNotificationToUser_Success() {
        // Arrange
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> controller.sendNotificationToUser(testUserId, testNotification));

        // Assert
        verify(messagingTemplate).convertAndSendToUser(
            testUserId.toString(),
            "/queue/notifications",
            testNotification
        );
    }

    @Test
    @DisplayName("Should throw exception when sending notification with null userId")
    void sendNotificationToUser_NullUserId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.sendNotificationToUser(null, testNotification)
        );
        assertEquals("UserId and notification cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sending null notification")
    void sendNotificationToUser_NullNotification() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.sendNotificationToUser(testUserId, null)
        );
        assertEquals("UserId and notification cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sending notification with empty content")
    void sendNotificationToUser_EmptyContent() {
        // Arrange
        testNotification.setContent("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.sendNotificationToUser(testUserId, testNotification)
        );
        assertEquals("Notification content cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sending notification with null content")
    void sendNotificationToUser_NullContent() {
        // Arrange
        testNotification.setContent(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.sendNotificationToUser(testUserId, testNotification)
        );
        assertEquals("Notification content cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user is not connected for direct send")
    void sendNotificationToUser_UserNotConnected() {
        // Arrange
        when(webSocketService.isUserConnected(testUserId)).thenReturn(false);

        // Act & Assert
        NotificationDeliveryException exception = assertThrows(
            NotificationDeliveryException.class,
            () -> controller.sendNotificationToUser(testUserId, testNotification)
        );
        assertEquals("User is not connected via WebSocket", exception.getMessage());
    }

    @Test
    @DisplayName("Should broadcast notification successfully")
    void broadcastNotification_Success() {
        // Arrange
        when(webSocketService.getActiveConnectionCount()).thenReturn(3);

        // Act
        assertDoesNotThrow(() -> controller.broadcastNotification(testNotification));

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), (Object) eq(testNotification));
    }

    @Test
    @DisplayName("Should throw exception when broadcasting null notification")
    void broadcastNotification_NullNotification() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.broadcastNotification(null)
        );
        assertEquals("Notification cannot be null for broadcast", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when broadcasting notification with empty content")
    void broadcastNotification_EmptyContent() {
        // Arrange
        testNotification.setContent("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.broadcastNotification(testNotification)
        );
        assertEquals("Notification content cannot be empty for broadcast", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when broadcasting notification with null content")
    void broadcastNotification_NullContent() {
        // Arrange
        testNotification.setContent(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.broadcastNotification(testNotification)
        );
        assertEquals("Notification content cannot be empty for broadcast", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle broadcast with no active connections")
    void broadcastNotification_NoActiveConnections() {
        // Arrange
        when(webSocketService.getActiveConnectionCount()).thenReturn(0);

        // Act
        assertDoesNotThrow(() -> controller.broadcastNotification(testNotification));

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @DisplayName("Should extract user ID from valid UUID principal name")
    void extractUserIdFromPrincipal_ValidUUID() {
        // This test verifies the private method behavior through public method calls
        // Arrange
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> controller.sendNotificationToUser(testUserId, testNotification));
    }

    @Test
    @DisplayName("Should extract user ID from UsernamePasswordAuthenticationToken with details")
    void extractUserIdFromPrincipal_AuthenticationTokenWithDetails() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("userId", testUserId.toString());
        
        UsernamePasswordAuthenticationToken authToken = mock(UsernamePasswordAuthenticationToken.class);
        when(authToken.getName()).thenReturn("invalid-uuid-format");
        when(authToken.getDetails()).thenReturn(details);
        when(webSocketService.isUserConnected(testUserId)).thenReturn(true);

        // Act & Assert - Should not throw exception when using the authToken as principal
        assertDoesNotThrow(() -> controller.subscribeToNotifications(authToken, headerAccessor));
    }

    @Test
    @DisplayName("Should throw exception when unable to extract user ID from principal")
    void extractUserIdFromPrincipal_InvalidPrincipal() {
        // Arrange
        Principal invalidPrincipal = mock(Principal.class);
        when(invalidPrincipal.getName()).thenReturn("invalid-format");

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> controller.subscribeToNotifications(invalidPrincipal, headerAccessor)
        );
    }
}