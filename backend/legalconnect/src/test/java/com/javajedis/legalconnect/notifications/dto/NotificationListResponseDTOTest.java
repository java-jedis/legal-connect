package com.javajedis.legalconnect.notifications.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationListResponseDTO Tests")
class NotificationListResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        List<NotificationResponseDTO> notifications = createTestNotifications();

        // Act
        NotificationListResponseDTO dto = new NotificationListResponseDTO(notifications);

        // Assert
        assertEquals(notifications, dto.getNotifications());
        assertEquals(2, dto.getNotifications().size());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        NotificationListResponseDTO dto = new NotificationListResponseDTO();

        // Assert
        assertNull(dto.getNotifications());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        NotificationListResponseDTO dto = new NotificationListResponseDTO();
        List<NotificationResponseDTO> notifications = createTestNotifications();

        // Act
        dto.setNotifications(notifications);

        // Assert
        assertEquals(notifications, dto.getNotifications());
        assertEquals(2, dto.getNotifications().size());
    }

    @Test
    @DisplayName("Should handle null notifications list")
    void testNullNotificationsList() {
        // Act
        NotificationListResponseDTO dto = new NotificationListResponseDTO(null);

        // Assert
        assertNull(dto.getNotifications());
    }

    @Test
    @DisplayName("Should handle empty notifications list")
    void testEmptyNotificationsList() {
        // Arrange
        List<NotificationResponseDTO> emptyList = new ArrayList<>();

        // Act
        NotificationListResponseDTO dto = new NotificationListResponseDTO(emptyList);

        // Assert
        assertNotNull(dto.getNotifications());
        assertTrue(dto.getNotifications().isEmpty());
        assertEquals(0, dto.getNotifications().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        List<NotificationResponseDTO> notifications = createTestNotifications();
        NotificationListResponseDTO dto1 = new NotificationListResponseDTO(notifications);
        NotificationListResponseDTO dto2 = new NotificationListResponseDTO(notifications);
        NotificationListResponseDTO dto3 = new NotificationListResponseDTO(new ArrayList<>());

        // Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals(null,dto1);
        assertNotEquals("string",dto1);
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        // Arrange
        List<NotificationResponseDTO> notifications = createTestNotifications();
        NotificationListResponseDTO dto = new NotificationListResponseDTO(notifications);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("NotificationListResponseDTO"));
        assertTrue(toString.contains("notifications"));
    }

    @Test
    @DisplayName("Should handle list modification after construction")
    void testListModificationAfterConstruction() {
        // Arrange
        List<NotificationResponseDTO> notifications = new ArrayList<>(createTestNotifications());
        NotificationListResponseDTO dto = new NotificationListResponseDTO(notifications);

        // Act - modify original list
        notifications.clear();

        // Assert - DTO should still have the original reference
        assertNotNull(dto.getNotifications());
        assertTrue(dto.getNotifications().isEmpty()); // Since it's the same reference
    }

    @Test
    @DisplayName("Should handle setting notifications to null")
    void testSettingNotificationsToNull() {
        // Arrange
        NotificationListResponseDTO dto = new NotificationListResponseDTO(createTestNotifications());

        // Act
        dto.setNotifications(null);

        // Assert
        assertNull(dto.getNotifications());
    }

    private List<NotificationResponseDTO> createTestNotifications() {
        NotificationResponseDTO notification1 = new NotificationResponseDTO(
            UUID.randomUUID(),
            "First notification",
            false,
            OffsetDateTime.now()
        );

        NotificationResponseDTO notification2 = new NotificationResponseDTO(
            UUID.randomUUID(),
            "Second notification",
            true,
            OffsetDateTime.now().minusHours(1)
        );

        return Arrays.asList(notification1, notification2);
    }
}