package com.javajedis.legalconnect.notifications.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.notifications.NotificationType;

@DisplayName("NotificationPreferenceResponseDTO Tests")
class NotificationPreferenceResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        NotificationType type = NotificationType.CASE_CREATE;
        String displayName = "Case Creation";
        boolean emailEnabled = true;
        boolean webPushEnabled = false;

        // Act
        NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO(
            type, displayName, emailEnabled, webPushEnabled
        );

        // Assert
        assertEquals(type, dto.getType());
        assertEquals(displayName, dto.getDisplayName());
        assertTrue(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO();

        // Assert
        assertNull(dto.getType());
        assertNull(dto.getDisplayName());
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO();
        NotificationType type = NotificationType.EVENT_ADD;
        String displayName = "Event Addition";
        boolean emailEnabled = true;
        boolean webPushEnabled = true;

        // Act
        dto.setType(type);
        dto.setDisplayName(displayName);
        dto.setEmailEnabled(emailEnabled);
        dto.setWebPushEnabled(webPushEnabled);

        // Assert
        assertEquals(type, dto.getType());
        assertEquals(displayName, dto.getDisplayName());
        assertTrue(dto.isEmailEnabled());
        assertTrue(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void testNullValues() {
        // Act
        NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO(
            null, null, false, false
        );

        // Assert
        assertNull(dto.getType());
        assertNull(dto.getDisplayName());
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        NotificationType type = NotificationType.SCHEDULE_REMINDER;
        String displayName = "Schedule Reminder";
        boolean emailEnabled = true;
        boolean webPushEnabled = false;

        NotificationPreferenceResponseDTO dto1 = new NotificationPreferenceResponseDTO(
            type, displayName, emailEnabled, webPushEnabled
        );
        NotificationPreferenceResponseDTO dto2 = new NotificationPreferenceResponseDTO(
            type, displayName, emailEnabled, webPushEnabled
        );
        NotificationPreferenceResponseDTO dto3 = new NotificationPreferenceResponseDTO(
            NotificationType.DOC_UPLOAD, displayName, emailEnabled, webPushEnabled
        );

        // Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals(null, dto1);
        assertNotEquals("string", dto1);
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        // Arrange
        NotificationType type = NotificationType.NOTE_CREATE;
        String displayName = "Note Creation";
        boolean emailEnabled = true;
        boolean webPushEnabled = true;

        NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO(
            type, displayName, emailEnabled, webPushEnabled
        );

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("NotificationPreferenceResponseDTO"));
        assertTrue(toString.contains("NOTE_CREATE"));
        assertTrue(toString.contains(displayName));
        assertTrue(toString.contains("true"));
    }

    @Test
    @DisplayName("Should handle all notification types")
    void testAllNotificationTypes() {
        // Test each notification type
        for (NotificationType type : NotificationType.values()) {
            // Act
            NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO(
                type, type.getDisplayName(), true, false
            );

            // Assert
            assertEquals(type, dto.getType());
            assertEquals(type.getDisplayName(), dto.getDisplayName());
            assertTrue(dto.isEmailEnabled());
            assertFalse(dto.isWebPushEnabled());
        }
    }

    @Test
    @DisplayName("Should handle boolean field edge cases")
    void testBooleanFieldEdgeCases() {
        // Arrange
        NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO();

        // Test default values
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());

        // Test setting both to true
        dto.setEmailEnabled(true);
        dto.setWebPushEnabled(true);
        assertTrue(dto.isEmailEnabled());
        assertTrue(dto.isWebPushEnabled());

        // Test setting back to false
        dto.setEmailEnabled(false);
        dto.setWebPushEnabled(false);
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle display name consistency with notification type")
    void testDisplayNameConsistency() {
        // Test that display name matches the notification type's display name
        for (NotificationType type : NotificationType.values()) {
            // Act
            NotificationPreferenceResponseDTO dto = new NotificationPreferenceResponseDTO(
                type, type.getDisplayName(), true, true
            );

            // Assert
            assertEquals(type.getDisplayName(), dto.getDisplayName());
        }
    }
}