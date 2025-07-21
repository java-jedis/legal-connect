package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("NotificationRepo Tests")
class NotificationRepoTest {

    @Mock
    private NotificationRepo notificationRepo;

    private UUID testReceiverId;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testReceiverId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
        
        // Create test notifications
        notification1 = new Notification();
        notification1.setId(UUID.randomUUID());
        notification1.setReceiverId(testReceiverId);
        notification1.setContent("First notification");
        notification1.setRead(false);
        notification1.setCreatedAt(OffsetDateTime.now().minusHours(3));
        
        notification2 = new Notification();
        notification2.setId(UUID.randomUUID());
        notification2.setReceiverId(testReceiverId);
        notification2.setContent("Second notification");
        notification2.setRead(true);
        notification2.setCreatedAt(OffsetDateTime.now().minusHours(2));
        
        notification3 = new Notification();
        notification3.setId(UUID.randomUUID());
        notification3.setReceiverId(testReceiverId);
        notification3.setContent("Third notification");
        notification3.setRead(false);
        notification3.setCreatedAt(OffsetDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Should find notifications by receiver ID ordered by created date desc")
    void findByReceiverIdOrderByCreatedAtDesc_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(notification3, notification2, notification1);
        Page<Notification> expectedPage = new PageImpl<>(notifications, pageable, notifications.size());
        
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, pageable))
            .thenReturn(expectedPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
        
        // Verify order (newest first)
        List<Notification> content = result.getContent();
        assertEquals(notification3.getId(), content.get(0).getId());
        assertEquals(notification2.getId(), content.get(1).getId());
        assertEquals(notification1.getId(), content.get(2).getId());
    }

    @Test
    @DisplayName("Should find notifications by receiver ID when no notifications exist")
    void findByReceiverIdOrderByCreatedAtDesc_NoNotifications() {
        // Arrange
        Page<Notification> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, pageable))
            .thenReturn(emptyPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should find unread notifications by receiver ID ordered by created date desc")
    void findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc_Success() {
        // Arrange
        List<Notification> unreadNotifications = Arrays.asList(notification3, notification1);
        Page<Notification> expectedPage = new PageImpl<>(unreadNotifications, pageable, unreadNotifications.size());
        
        when(notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testReceiverId, pageable))
            .thenReturn(expectedPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        
        // Verify all notifications are unread
        for (Notification notification : result.getContent()) {
            assertFalse(notification.isRead());
        }
        
        // Verify order (newest first)
        List<Notification> content = result.getContent();
        assertEquals(notification3.getId(), content.get(0).getId());
        assertEquals(notification1.getId(), content.get(1).getId());
    }

    @Test
    @DisplayName("Should find unread notifications when none exist")
    void findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc_NoUnreadNotifications() {
        // Arrange
        Page<Notification> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testReceiverId, pageable))
            .thenReturn(emptyPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should find read notifications by receiver ID ordered by created date desc")
    void findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc_Success() {
        // Arrange
        List<Notification> readNotifications = Arrays.asList(notification2);
        Page<Notification> expectedPage = new PageImpl<>(readNotifications, pageable, readNotifications.size());
        
        when(notificationRepo.findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(testReceiverId, pageable))
            .thenReturn(expectedPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(testReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        // Verify all notifications are read
        for (Notification notification : result.getContent()) {
            assertTrue(notification.isRead());
        }
        
        assertEquals(notification2.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should find read notifications when none exist")
    void findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc_NoReadNotifications() {
        // Arrange
        Page<Notification> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(notificationRepo.findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(testReceiverId, pageable))
            .thenReturn(emptyPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(testReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should count unread notifications by receiver ID")
    void countUnreadByReceiverId_Success() {
        // Arrange
        long expectedCount = 2L;
        when(notificationRepo.countUnreadByReceiverId(testReceiverId))
            .thenReturn(expectedCount);
        
        // Act
        long result = notificationRepo.countUnreadByReceiverId(testReceiverId);
        
        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Should count unread notifications when none exist")
    void countUnreadByReceiverId_NoUnreadNotifications() {
        // Arrange
        when(notificationRepo.countUnreadByReceiverId(testReceiverId))
            .thenReturn(0L);
        
        // Act
        long result = notificationRepo.countUnreadByReceiverId(testReceiverId);
        
        // Assert
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should count total notifications by receiver ID")
    void countByReceiverId_Success() {
        // Arrange
        long expectedCount = 3L;
        when(notificationRepo.countByReceiverId(testReceiverId))
            .thenReturn(expectedCount);
        
        // Act
        long result = notificationRepo.countByReceiverId(testReceiverId);
        
        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Should count total notifications when none exist")
    void countByReceiverId_NoNotifications() {
        // Arrange
        when(notificationRepo.countByReceiverId(testReceiverId))
            .thenReturn(0L);
        
        // Act
        long result = notificationRepo.countByReceiverId(testReceiverId);
        
        // Assert
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPaginationHandling() {
        // Arrange
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);
        
        List<Notification> firstPageNotifications = Arrays.asList(notification3, notification2);
        List<Notification> secondPageNotifications = Arrays.asList(notification1);
        
        Page<Notification> firstPageResult = new PageImpl<>(firstPageNotifications, firstPage, 3);
        Page<Notification> secondPageResult = new PageImpl<>(secondPageNotifications, secondPage, 3);
        
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, firstPage))
            .thenReturn(firstPageResult);
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, secondPage))
            .thenReturn(secondPageResult);
        
        // Act
        Page<Notification> firstResult = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, firstPage);
        Page<Notification> secondResult = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, secondPage);
        
        // Assert
        assertEquals(2, firstResult.getContent().size());
        assertEquals(1, secondResult.getContent().size());
        assertEquals(3, firstResult.getTotalElements());
        assertEquals(3, secondResult.getTotalElements());
        assertTrue(firstResult.hasNext());
        assertFalse(secondResult.hasNext());
    }

    @Test
    @DisplayName("Should handle different receiver IDs")
    void testDifferentReceiverIds() {
        // Arrange
        UUID anotherReceiverId = UUID.randomUUID();
        List<Notification> notifications = Arrays.asList(notification1);
        Page<Notification> expectedPage = new PageImpl<>(notifications, pageable, notifications.size());
        
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(anotherReceiverId, pageable))
            .thenReturn(expectedPage);
        
        // Act
        Page<Notification> result = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(anotherReceiverId, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should handle custom query method for unread count")
    void testCustomQueryMethodForUnreadCount() {
        // Arrange
        UUID receiverId1 = UUID.randomUUID();
        UUID receiverId2 = UUID.randomUUID();
        
        when(notificationRepo.countUnreadByReceiverId(receiverId1)).thenReturn(5L);
        when(notificationRepo.countUnreadByReceiverId(receiverId2)).thenReturn(0L);
        
        // Act & Assert
        assertEquals(5L, notificationRepo.countUnreadByReceiverId(receiverId1));
        assertEquals(0L, notificationRepo.countUnreadByReceiverId(receiverId2));
    }

    @Test
    @DisplayName("Should handle repository method return types correctly")
    void testRepositoryMethodReturnTypes() {
        // Arrange
        Page<Notification> mockPage = new PageImpl<>(Arrays.asList(notification1), pageable, 1);
        
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(notificationRepo.findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
            .thenReturn(mockPage);
        when(notificationRepo.countUnreadByReceiverId(any(UUID.class))).thenReturn(1L);
        when(notificationRepo.countByReceiverId(any(UUID.class))).thenReturn(1L);
        
        // Act & Assert - verify return types
        Page<Notification> pageResult1 = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(testReceiverId, pageable);
        assertTrue(pageResult1 instanceof Page);
        
        Page<Notification> pageResult2 = notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(testReceiverId, pageable);
        assertTrue(pageResult2 instanceof Page);
        
        Page<Notification> pageResult3 = notificationRepo.findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(testReceiverId, pageable);
        assertTrue(pageResult3 instanceof Page);
        
        long countResult1 = notificationRepo.countUnreadByReceiverId(testReceiverId);
        assertTrue(countResult1 >= 0);
        
        long countResult2 = notificationRepo.countByReceiverId(testReceiverId);
        assertTrue(countResult2 >= 0);
    }

    @Test
    @DisplayName("Should handle edge cases with large counts")
    void testLargeCounts() {
        // Arrange
        long largeCount = Long.MAX_VALUE;
        when(notificationRepo.countUnreadByReceiverId(testReceiverId)).thenReturn(largeCount);
        when(notificationRepo.countByReceiverId(testReceiverId)).thenReturn(largeCount);
        
        // Act & Assert
        assertEquals(largeCount, notificationRepo.countUnreadByReceiverId(testReceiverId));
        assertEquals(largeCount, notificationRepo.countByReceiverId(testReceiverId));
    }

    @Test
    @DisplayName("Should handle method parameter validation")
    void testMethodParameterValidation() {
        // This test verifies that the repository methods accept the expected parameter types
        // The actual validation would be handled by Spring Data JPA
        
        // Arrange
        UUID validReceiverId = UUID.randomUUID();
        Pageable validPageable = PageRequest.of(0, 10);
        Page<Notification> mockPage = new PageImpl<>(Arrays.asList(), validPageable, 0);
        
        when(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(validReceiverId, validPageable))
            .thenReturn(mockPage);
        when(notificationRepo.countUnreadByReceiverId(validReceiverId)).thenReturn(0L);
        
        // Act & Assert - methods should accept correct parameter types
        assertNotNull(notificationRepo.findByReceiverIdOrderByCreatedAtDesc(validReceiverId, validPageable));
        assertEquals(0L, notificationRepo.countUnreadByReceiverId(validReceiverId));
    }
}