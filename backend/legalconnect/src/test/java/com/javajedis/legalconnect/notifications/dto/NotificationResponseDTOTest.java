package com.javajedis.legalconnect.notifications.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationResponseDTO Tests")
class NotificationResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        String content = "Test notification content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        // Act
        NotificationResponseDTO dto = new NotificationResponseDTO(id, content, isRead, createdAt);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(content, dto.getContent());
        assertTrue(dto.getRead()); // Use the custom getter method
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        NotificationResponseDTO dto = new NotificationResponseDTO();

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getContent());
        assertFalse(dto.getRead());
        assertNull(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        NotificationResponseDTO dto = new NotificationResponseDTO();
        UUID id = UUID.randomUUID();
        String content = "Updated content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        // Act
        dto.setId(id);
        dto.setContent(content);
        dto.setRead(isRead);
        dto.setCreatedAt(createdAt);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(content, dto.getContent());
        assertTrue(dto.getRead());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void testNullValues() {
        // Act
        NotificationResponseDTO dto = new NotificationResponseDTO(null, null, false, null);

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getContent());
        assertFalse(dto.getRead());
        assertNull(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        UUID id = UUID.randomUUID();
        String content = "Test content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        NotificationResponseDTO dto1 = new NotificationResponseDTO(id, content, isRead, createdAt);
        NotificationResponseDTO dto2 = new NotificationResponseDTO(id, content, isRead, createdAt);
        NotificationResponseDTO dto3 = new NotificationResponseDTO(UUID.randomUUID(), content, isRead, createdAt);

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
        UUID id = UUID.randomUUID();
        String content = "Test content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        NotificationResponseDTO dto = new NotificationResponseDTO(id, content, isRead, createdAt);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("NotificationResponseDTO"));
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains(content));
        assertTrue(toString.contains("true"));
    }

    @Test
    @DisplayName("Should handle boolean read field edge cases")
    void testBooleanReadFieldEdgeCases() {
        // Arrange
        NotificationResponseDTO dto = new NotificationResponseDTO();

        // Test default value
        assertFalse(dto.getRead());

        // Test setting to true
        dto.setRead(true);
        assertTrue(dto.getRead());

        // Test setting back to false
        dto.setRead(false);
        assertFalse(dto.getRead());
    }

    @Test
    @DisplayName("Should handle read field consistently across constructor and setter")
    void testReadFieldConsistency() {
        // Test consistency between constructor and setter
        UUID id = UUID.randomUUID();
        String content = "Test content";
        OffsetDateTime createdAt = OffsetDateTime.now();
        
        // Create DTO with read=true via constructor
        NotificationResponseDTO dto1 = new NotificationResponseDTO(id, content, true, createdAt);
        assertTrue(dto1.getRead());
        
        // Create DTO with read=false via constructor
        NotificationResponseDTO dto2 = new NotificationResponseDTO(id, content, false, createdAt);
        assertFalse(dto2.getRead());
        
        // Create DTO with no-args constructor and set via setter
        NotificationResponseDTO dto3 = new NotificationResponseDTO();
        dto3.setRead(true);
        assertTrue(dto3.getRead());
        
        // Verify that constructor and setter produce same result
        assertEquals(dto1.getRead(), dto3.getRead());
    }
}