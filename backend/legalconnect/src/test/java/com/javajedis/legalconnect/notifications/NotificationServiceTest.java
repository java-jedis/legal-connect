package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.notifications.dto.NotificationListResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.dto.SendNotificationDTO;
import com.javajedis.legalconnect.notifications.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.notifications.exception.NotificationDeliveryException;
import com.javajedis.legalconnect.notifications.exception.NotificationNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private NotificationService notificationService;

    private SendNotificationDTO sendNotificationDTO;
    private Notification notification;
    private NotificationResponseDTO notificationResponseDTO;
    private UUID testUserId;
    private UUID testNotificationId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();

        sendNotificationDTO = new SendNotificationDTO();
        sendNotificationDTO.setReceiverId(testUserId);
        sendNotificationDTO.setContent("Test notification content");

        notification = new Notification();
        notification.setId(testNotificationId);
        notification.setReceiverId(testUserId);
        notification.setContent("Test notification content");
        notification.setRead(false);
        notification.setCreatedAt(OffsetDateTime.now());

        notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setId(testNotificationId);
        notificationResponseDTO.setContent("Test notification content");
        notificationResponseDTO.setRead(false);
        notificationResponseDTO.setCreatedAt(notification.getCreatedAt());
    }

    @Test
    @DisplayName("Should send notification successfully with DTO")
    void sendNotification_WithDTO_Success() {
        when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
        when(webSocketService.sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class))).thenReturn(true);

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = notificationService.sendNotification(sendNotificationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification sent successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(testNotificationId, response.getBody().getData().getId());
        assertEquals("Test notification content", response.getBody().getData().getContent());
        assertFalse(response.getBody().getData().getRead());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(webSocketService, times(1)).sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should send notification successfully with parameters")
    void sendNotification_WithParameters_Success() {
        when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
        when(webSocketService.sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class))).thenReturn(true);

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = notificationService.sendNotification(testUserId, "Test content");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification sent successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(testNotificationId, response.getBody().getData().getId());
        assertEquals("Test notification content", response.getBody().getData().getContent());
        assertFalse(response.getBody().getData().getRead());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(webSocketService, times(1)).sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should handle WebSocket delivery failure gracefully")
    void sendNotification_WebSocketDeliveryFailure_GracefulDegradation() {
        when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
        when(webSocketService.sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class)))
                .thenThrow(new NotificationDeliveryException("WebSocket delivery failed"));

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = notificationService.sendNotification(sendNotificationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification sent successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(webSocketService, times(1)).sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should handle security exception during WebSocket delivery")
    void sendNotification_SecurityException_GracefulDegradation() {
        when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
        when(webSocketService.sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class)))
                .thenThrow(new SecurityException("Authentication failed"));

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = notificationService.sendNotification(sendNotificationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification sent successfully", response.getBody().getMessage());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(webSocketService, times(1)).sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should handle unexpected exception during WebSocket delivery")
    void sendNotification_UnexpectedException_GracefulDegradation() {
        when(notificationRepo.save(any(Notification.class))).thenReturn(notification);
        when(webSocketService.sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = notificationService.sendNotification(sendNotificationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification sent successfully", response.getBody().getMessage());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(webSocketService, times(1)).sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null receiver ID")
    void sendNotification_NullReceiverId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(null, "Test content");
        });

        assertEquals("Receiver ID cannot be null", exception.getMessage());
        verify(notificationRepo, never()).save(any(Notification.class));
        verify(webSocketService, never()).sendNotificationToUser(any(UUID.class), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null content")
    void sendNotification_NullContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(testUserId, null);
        });

        assertEquals("Notification content cannot be empty", exception.getMessage());
        verify(notificationRepo, never()).save(any(Notification.class));
        verify(webSocketService, never()).sendNotificationToUser(any(UUID.class), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for empty content")
    void sendNotification_EmptyContent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(testUserId, "   ");
        });

        assertEquals("Notification content cannot be empty", exception.getMessage());
        verify(notificationRepo, never()).save(any(Notification.class));
        verify(webSocketService, never()).sendNotificationToUser(any(UUID.class), any(NotificationResponseDTO.class));
    }

    @Test
    @DisplayName("Should trim notification content before saving")
    void sendNotification_TrimsContent_Success() {
        String contentWithSpaces = "  Test content with spaces  ";
        Notification savedNotification = new Notification();
        savedNotification.setId(testNotificationId);
        savedNotification.setReceiverId(testUserId);
        savedNotification.setContent("Test content with spaces");
        savedNotification.setRead(false);
        savedNotification.setCreatedAt(OffsetDateTime.now());

        when(notificationRepo.save(any(Notification.class))).thenReturn(savedNotification);
        when(webSocketService.sendNotificationToUser(eq(testUserId), any(NotificationResponseDTO.class))).thenReturn(true);

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = notificationService.sendNotification(testUserId, contentWithSpaces);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Test content with spaces", response.getBody().getData().getContent());

        verify(notificationRepo, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should get user notifications successfully")
    void getUserNotifications_Success() {
        List<Notification> notifications = Arrays.asList(notification);
        Page<Notification> notificationPage = new PageImpl<>(notifications, PageRequest.of(0, 10), 1);

        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(eq(testUserId), any(Pageable.class)))
                .thenReturn(notificationPage);

        ResponseEntity<ApiResponse<NotificationListResponseDTO>> response = 
                notificationService.getUserNotifications(testUserId, 0, 10, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notifications retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getNotifications().size());

        Map<String, Object> metadata = response.getBody().getMetadata();
        assertNotNull(metadata);
        assertEquals(1L, metadata.get("totalCount"));
        assertEquals(0, metadata.get("pageNumber"));
        assertEquals(10, metadata.get("pageSize"));
        assertEquals(1, metadata.get("totalPages"));
        assertFalse((Boolean) metadata.get("hasNext"));
        assertFalse((Boolean) metadata.get("hasPrevious"));
        assertTrue((Boolean) metadata.get("isFirst"));
        assertTrue((Boolean) metadata.get("isLast"));

        verify(notificationRepo, times(1)).findByReceiverIdOrderByCreatedAtDesc(eq(testUserId), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get unread notifications only when unreadOnly is true")
    void getUserNotifications_UnreadOnly_Success() {
        List<Notification> unreadNotifications = Arrays.asList(notification);
        Page<Notification> notificationPage = new PageImpl<>(unreadNotifications, PageRequest.of(0, 10), 1);

        when(notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(eq(testUserId), any(Pageable.class)))
                .thenReturn(notificationPage);

        ResponseEntity<ApiResponse<NotificationListResponseDTO>> response = 
                notificationService.getUserNotifications(testUserId, 0, 10, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notifications retrieved successfully", response.getBody().getMessage());

        Map<String, Object> metadata = response.getBody().getMetadata();
        assertNotNull(metadata);
        @SuppressWarnings("unchecked")
        Map<String, Object> appliedFilters = (Map<String, Object>) metadata.get("appliedFilters");
        assertTrue((Boolean) appliedFilters.get("unreadOnly"));

        verify(notificationRepo, times(1)).findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(eq(testUserId), any(Pageable.class));
        verify(notificationRepo, never()).findByReceiverIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty list when no notifications found")
    void getUserNotifications_EmptyResult_Success() {
        Page<Notification> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(eq(testUserId), any(Pageable.class)))
                .thenReturn(emptyPage);

        ResponseEntity<ApiResponse<NotificationListResponseDTO>> response = 
                notificationService.getUserNotifications(testUserId, 0, 10, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notifications retrieved successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().getNotifications().isEmpty());

        Map<String, Object> metadata = response.getBody().getMetadata();
        assertEquals(0L, metadata.get("totalCount"));
        assertEquals(0, metadata.get("totalPages"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null receiver ID in getUserNotifications")
    void getUserNotifications_NullReceiverId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.getUserNotifications(null, 0, 10, false);
        });

        assertEquals("Invalid receiver ID", exception.getMessage());
        verify(notificationRepo, never()).findByReceiverIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should use correct sort order for notifications")
    void getUserNotifications_CorrectSortOrder_Success() {
        Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification), PageRequest.of(0, 10), 1);

        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(eq(testUserId), any(Pageable.class)))
                .thenReturn(notificationPage);

        notificationService.getUserNotifications(testUserId, 0, 10, false);

        verify(notificationRepo, times(1)).findByReceiverIdOrderByCreatedAtDesc(
                testUserId, 
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    @Test
    @DisplayName("Should get unread count successfully")
    void getUnreadCount_Success() {
        when(notificationRepo.countUnreadByReceiverId(testUserId)).thenReturn(5L);

        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = notificationService.getUnreadCount(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unread count retrieved successfully", response.getBody().getMessage());
        assertEquals(5, response.getBody().getData().getUnreadCount());

        verify(notificationRepo, times(1)).countUnreadByReceiverId(testUserId);
    }

    @Test
    @DisplayName("Should return zero unread count when no unread notifications")
    void getUnreadCount_ZeroCount_Success() {
        when(notificationRepo.countUnreadByReceiverId(testUserId)).thenReturn(0L);

        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = notificationService.getUnreadCount(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getData().getUnreadCount());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null receiver ID in getUnreadCount")
    void getUnreadCount_NullReceiverId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.getUnreadCount(null);
        });

        assertEquals("Invalid receiver ID", exception.getMessage());
        verify(notificationRepo, never()).countUnreadByReceiverId(any(UUID.class));
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void markAsRead_Success() {
        when(notificationRepo.findById(testNotificationId)).thenReturn(Optional.of(notification));
        
        Notification updatedNotification = new Notification();
        updatedNotification.setId(testNotificationId);
        updatedNotification.setReceiverId(testUserId);
        updatedNotification.setContent("Test notification content");
        updatedNotification.setRead(true);
        updatedNotification.setCreatedAt(notification.getCreatedAt());
        
        when(notificationRepo.save(any(Notification.class))).thenReturn(updatedNotification);

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = 
                notificationService.markAsRead(testNotificationId, testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Notification marked as read", response.getBody().getMessage());
        assertTrue(response.getBody().getData().getRead());

        verify(notificationRepo, times(1)).findById(testNotificationId);
        verify(notificationRepo, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should not update notification if already read")
    void markAsRead_AlreadyRead_Success() {
        Notification readNotification = new Notification();
        readNotification.setId(testNotificationId);
        readNotification.setReceiverId(testUserId);
        readNotification.setContent("Test notification content");
        readNotification.setRead(true);
        readNotification.setCreatedAt(OffsetDateTime.now());

        when(notificationRepo.findById(testNotificationId)).thenReturn(Optional.of(readNotification));

        ResponseEntity<ApiResponse<NotificationResponseDTO>> response = 
                notificationService.markAsRead(testNotificationId, testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().getRead());

        verify(notificationRepo, times(1)).findById(testNotificationId);
        verify(notificationRepo, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should throw NotificationNotFoundException when notification not found")
    void markAsRead_NotificationNotFound_ThrowsException() {
        when(notificationRepo.findById(testNotificationId)).thenReturn(Optional.empty());

        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.markAsRead(testNotificationId, testUserId);
        });

        assertEquals("Notification not found with ID: " + testNotificationId, exception.getMessage());
        verify(notificationRepo, times(1)).findById(testNotificationId);
        verify(notificationRepo, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should throw SecurityException when user tries to mark another user's notification")
    void markAsRead_UnauthorizedUser_ThrowsException() {
        UUID otherUserId = UUID.randomUUID();
        Notification otherUserNotification = new Notification();
        otherUserNotification.setId(testNotificationId);
        otherUserNotification.setReceiverId(otherUserId);
        otherUserNotification.setContent("Test notification content");
        otherUserNotification.setRead(false);

        when(notificationRepo.findById(testNotificationId)).thenReturn(Optional.of(otherUserNotification));

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            notificationService.markAsRead(testNotificationId, testUserId);
        });

        assertEquals("You can only mark your own notifications as read", exception.getMessage());
        verify(notificationRepo, times(1)).findById(testNotificationId);
        verify(notificationRepo, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null notification ID in markAsRead")
    void markAsRead_NullNotificationId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(null, testUserId);
        });

        assertEquals("Notification ID and receiver ID are required", exception.getMessage());
        verify(notificationRepo, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null receiver ID in markAsRead")
    void markAsRead_NullReceiverId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(testNotificationId, null);
        });

        assertEquals("Notification ID and receiver ID are required", exception.getMessage());
        verify(notificationRepo, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void markAllAsRead_Success() {
        List<Notification> unreadNotifications = Arrays.asList(
                createUnreadNotification(UUID.randomUUID()),
                createUnreadNotification(UUID.randomUUID()),
                createUnreadNotification(UUID.randomUUID())
        );
        Page<Notification> unreadPage = new PageImpl<>(unreadNotifications);

        when(notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testUserId, Pageable.unpaged()))
                .thenReturn(unreadPage);

        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = notificationService.markAllAsRead(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("All notifications marked as read", response.getBody().getMessage());
        assertEquals(0, response.getBody().getData().getUnreadCount());

        verify(notificationRepo, times(1)).findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testUserId, Pageable.unpaged());
        verify(notificationRepo, times(1)).saveAll(unreadNotifications);
    }

    @Test
    @DisplayName("Should handle mark all as read when no unread notifications")
    void markAllAsRead_NoUnreadNotifications_Success() {
        Page<Notification> emptyPage = new PageImpl<>(Collections.emptyList());

        when(notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testUserId, Pageable.unpaged()))
                .thenReturn(emptyPage);

        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = notificationService.markAllAsRead(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getData().getUnreadCount());

        verify(notificationRepo, times(1)).findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testUserId, Pageable.unpaged());
        verify(notificationRepo, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null receiver ID in markAllAsRead")
    void markAllAsRead_NullReceiverId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAllAsRead(null);
        });

        assertEquals("Invalid receiver ID", exception.getMessage());
        verify(notificationRepo, never()).findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class));
    }

    private Notification createUnreadNotification(UUID notificationId) {
        Notification unreadNotification = new Notification();
        unreadNotification.setId(notificationId);
        unreadNotification.setReceiverId(testUserId);
        unreadNotification.setContent("Unread notification");
        unreadNotification.setRead(false);
        unreadNotification.setCreatedAt(OffsetDateTime.now());
        return unreadNotification;
    }
}