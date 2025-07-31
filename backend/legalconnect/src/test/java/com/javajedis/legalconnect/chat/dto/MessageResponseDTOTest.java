package com.javajedis.legalconnect.chat.dto;

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

@DisplayName("MessageResponseDTO Tests")
class MessageResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String content = "Test message content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        // Act
        MessageResponseDTO dto = new MessageResponseDTO(id, conversationId, senderId, content, isRead, createdAt);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(conversationId, dto.getConversationId());
        assertEquals(senderId, dto.getSenderId());
        assertEquals(content, dto.getContent());
        assertTrue(dto.isRead());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        MessageResponseDTO dto = new MessageResponseDTO();

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getConversationId());
        assertNull(dto.getSenderId());
        assertNull(dto.getContent());
        assertFalse(dto.isRead());
        assertNull(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        MessageResponseDTO dto = new MessageResponseDTO();
        UUID id = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String content = "Updated content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        // Act
        dto.setId(id);
        dto.setConversationId(conversationId);
        dto.setSenderId(senderId);
        dto.setContent(content);
        dto.setRead(isRead);
        dto.setCreatedAt(createdAt);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(conversationId, dto.getConversationId());
        assertEquals(senderId, dto.getSenderId());
        assertEquals(content, dto.getContent());
        assertTrue(dto.isRead());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String content = "Test content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();

        MessageResponseDTO dto1 = new MessageResponseDTO(id, conversationId, senderId, content, isRead, createdAt);
        MessageResponseDTO dto2 = new MessageResponseDTO(id, conversationId, senderId, content, isRead, createdAt);
        MessageResponseDTO dto3 = new MessageResponseDTO(UUID.randomUUID(), conversationId, senderId, content, isRead, createdAt);

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
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String content = "Test content";
        boolean isRead = true;
        OffsetDateTime createdAt = OffsetDateTime.now();
        
        MessageResponseDTO dto = new MessageResponseDTO(id, conversationId, senderId, content, isRead, createdAt);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("MessageResponseDTO"));
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains(conversationId.toString()));
        assertTrue(toString.contains(senderId.toString()));
        assertTrue(toString.contains(content));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // Act
        MessageResponseDTO dto = new MessageResponseDTO(null, null, null, null, false, null);

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getConversationId());
        assertNull(dto.getSenderId());
        assertNull(dto.getContent());
        assertFalse(dto.isRead());
        assertNull(dto.getCreatedAt());
    }
}