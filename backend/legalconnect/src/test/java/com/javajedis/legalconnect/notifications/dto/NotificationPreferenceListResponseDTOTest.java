package com.javajedis.legalconnect.notifications.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.notifications.NotificationType;

@DisplayName("NotificationPreferenceListResponseDTO Tests")
class NotificationPreferenceListResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        List<NotificationPreferenceResponseDTO> preferences = createTestPreferences();

        // Act
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(preferences);

        // Assert
        assertEquals(preferences, dto.getPreferences());
        assertEquals(2, dto.getPreferences().size());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO();

        // Assert
        assertNull(dto.getPreferences());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO();
        List<NotificationPreferenceResponseDTO> preferences = createTestPreferences();

        // Act
        dto.setPreferences(preferences);

        // Assert
        assertEquals(preferences, dto.getPreferences());
        assertEquals(2, dto.getPreferences().size());
    }

    @Test
    @DisplayName("Should handle null preferences list")
    void testNullPreferencesList() {
        // Act
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(null);

        // Assert
        assertNull(dto.getPreferences());
    }

    @Test
    @DisplayName("Should handle empty preferences list")
    void testEmptyPreferencesList() {
        // Arrange
        List<NotificationPreferenceResponseDTO> emptyList = new ArrayList<>();

        // Act
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(emptyList);

        // Assert
        assertNotNull(dto.getPreferences());
        assertTrue(dto.getPreferences().isEmpty());
        assertEquals(0, dto.getPreferences().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        List<NotificationPreferenceResponseDTO> preferences = createTestPreferences();
        NotificationPreferenceListResponseDTO dto1 = new NotificationPreferenceListResponseDTO(preferences);
        NotificationPreferenceListResponseDTO dto2 = new NotificationPreferenceListResponseDTO(preferences);
        NotificationPreferenceListResponseDTO dto3 = new NotificationPreferenceListResponseDTO(new ArrayList<>());

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
        List<NotificationPreferenceResponseDTO> preferences = createTestPreferences();
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(preferences);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("NotificationPreferenceListResponseDTO"));
        assertTrue(toString.contains("preferences"));
    }

    @Test
    @DisplayName("Should handle list modification after construction")
    void testListModificationAfterConstruction() {
        // Arrange
        List<NotificationPreferenceResponseDTO> preferences = new ArrayList<>(createTestPreferences());
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(preferences);

        // Act - modify original list
        preferences.clear();

        // Assert - DTO should still have the original reference
        assertNotNull(dto.getPreferences());
        assertTrue(dto.getPreferences().isEmpty()); // Since it's the same reference
    }

    @Test
    @DisplayName("Should handle setting preferences to null")
    void testSettingPreferencesToNull() {
        // Arrange
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(createTestPreferences());

        // Act
        dto.setPreferences(null);

        // Assert
        assertNull(dto.getPreferences());
    }

    @Test
    @DisplayName("Should handle large list of preferences")
    void testLargePreferencesList() {
        // Arrange
        List<NotificationPreferenceResponseDTO> largeList = new ArrayList<>();
        for (NotificationType type : NotificationType.values()) {
            largeList.add(new NotificationPreferenceResponseDTO(
                type, type.getDisplayName(), true, false
            ));
            largeList.add(new NotificationPreferenceResponseDTO(
                type, type.getDisplayName(), false, true
            ));
        }

        // Act
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(largeList);

        // Assert
        assertEquals(largeList.size(), dto.getPreferences().size());
        assertEquals(NotificationType.values().length * 2, dto.getPreferences().size());
    }

    @Test
    @DisplayName("Should handle preferences with different configurations")
    void testPreferencesWithDifferentConfigurations() {
        // Arrange
        List<NotificationPreferenceResponseDTO> preferences = Arrays.asList(
            new NotificationPreferenceResponseDTO(NotificationType.CASE_CREATE, "Case Creation", true, true),
            new NotificationPreferenceResponseDTO(NotificationType.EVENT_ADD, "Event Addition", false, true),
            new NotificationPreferenceResponseDTO(NotificationType.DOC_UPLOAD, "Document Upload", true, false),
            new NotificationPreferenceResponseDTO(NotificationType.NOTE_CREATE, "Note Creation", false, false)
        );

        // Act
        NotificationPreferenceListResponseDTO dto = new NotificationPreferenceListResponseDTO(preferences);

        // Assert
        assertEquals(4, dto.getPreferences().size());
        
        // Verify each preference configuration
        NotificationPreferenceResponseDTO firstPref = dto.getPreferences().get(0);
        assertTrue(firstPref.isEmailEnabled() && firstPref.isWebPushEnabled());
        
        NotificationPreferenceResponseDTO secondPref = dto.getPreferences().get(1);
        assertFalse(secondPref.isEmailEnabled());
        assertTrue(secondPref.isWebPushEnabled());
        
        NotificationPreferenceResponseDTO thirdPref = dto.getPreferences().get(2);
        assertTrue(thirdPref.isEmailEnabled());
        assertFalse(thirdPref.isWebPushEnabled());
        
        NotificationPreferenceResponseDTO fourthPref = dto.getPreferences().get(3);
        assertFalse(fourthPref.isEmailEnabled() || fourthPref.isWebPushEnabled());
    }

    private List<NotificationPreferenceResponseDTO> createTestPreferences() {
        NotificationPreferenceResponseDTO preference1 = new NotificationPreferenceResponseDTO(
            NotificationType.CASE_CREATE,
            "Case Creation",
            true,
            false
        );

        NotificationPreferenceResponseDTO preference2 = new NotificationPreferenceResponseDTO(
            NotificationType.EVENT_ADD,
            "Event Addition",
            false,
            true
        );

        return Arrays.asList(preference1, preference2);
    }
}