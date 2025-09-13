package com.javajedis.legalconnect.chat.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.user.ProfilePictureDTO;

@DisplayName("ConversationResponseDTO Tests")
class ConversationResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID otherParticipantId = UUID.randomUUID();
        String otherParticipantName = "John Doe";
        ProfilePictureDTO otherParticipantProfilePicture = createSampleProfilePicture();
        MessageResponseDTO latestMessage = createSampleMessage();
        int unreadCount = 5;
        OffsetDateTime updatedAt = OffsetDateTime.now();

        // Act
        ConversationResponseDTO dto = new ConversationResponseDTO(
            id, otherParticipantId, otherParticipantName, otherParticipantProfilePicture, latestMessage, unreadCount, updatedAt
        );

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(otherParticipantId, dto.getOtherParticipantId());
        assertEquals(otherParticipantName, dto.getOtherParticipantName());
        assertEquals(otherParticipantProfilePicture, dto.getOtherParticipantProfilePicture());
        assertEquals(latestMessage, dto.getLatestMessage());
        assertEquals(unreadCount, dto.getUnreadCount());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        ConversationResponseDTO dto = new ConversationResponseDTO();

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getOtherParticipantId());
        assertNull(dto.getOtherParticipantName());
        assertNull(dto.getOtherParticipantProfilePicture());
        assertNull(dto.getLatestMessage());
        assertEquals(0, dto.getUnreadCount());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        ConversationResponseDTO dto = new ConversationResponseDTO();
        UUID id = UUID.randomUUID();
        UUID otherParticipantId = UUID.randomUUID();
        String otherParticipantName = "Jane Smith";
        ProfilePictureDTO otherParticipantProfilePicture = createSampleProfilePicture();
        MessageResponseDTO latestMessage = createSampleMessage();
        int unreadCount = 3;
        OffsetDateTime updatedAt = OffsetDateTime.now();

        // Act
        dto.setId(id);
        dto.setOtherParticipantId(otherParticipantId);
        dto.setOtherParticipantName(otherParticipantName);
        dto.setOtherParticipantProfilePicture(otherParticipantProfilePicture);
        dto.setLatestMessage(latestMessage);
        dto.setUnreadCount(unreadCount);
        dto.setUpdatedAt(updatedAt);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(otherParticipantId, dto.getOtherParticipantId());
        assertEquals(otherParticipantName, dto.getOtherParticipantName());
        assertEquals(otherParticipantProfilePicture, dto.getOtherParticipantProfilePicture());
        assertEquals(latestMessage, dto.getLatestMessage());
        assertEquals(unreadCount, dto.getUnreadCount());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID otherParticipantId = UUID.randomUUID();
        String otherParticipantName = "John Doe";
        MessageResponseDTO latestMessage = createSampleMessage();
        int unreadCount = 5;
        OffsetDateTime updatedAt = OffsetDateTime.now();

        ProfilePictureDTO profilePicture = createSampleProfilePicture();
        
        ConversationResponseDTO dto1 = new ConversationResponseDTO(
            id, otherParticipantId, otherParticipantName, profilePicture, latestMessage, unreadCount, updatedAt
        );
        ConversationResponseDTO dto2 = new ConversationResponseDTO(
            id, otherParticipantId, otherParticipantName, profilePicture, latestMessage, unreadCount, updatedAt
        );
        ConversationResponseDTO dto3 = new ConversationResponseDTO(
            UUID.randomUUID(), otherParticipantId, otherParticipantName, profilePicture, latestMessage, unreadCount, updatedAt
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
        UUID id = UUID.randomUUID();
        UUID otherParticipantId = UUID.randomUUID();
        String otherParticipantName = "John Doe";
        MessageResponseDTO latestMessage = createSampleMessage();
        int unreadCount = 5;
        OffsetDateTime updatedAt = OffsetDateTime.now();
        
        ProfilePictureDTO profilePicture = createSampleProfilePicture();
        
        ConversationResponseDTO dto = new ConversationResponseDTO(
            id, otherParticipantId, otherParticipantName, profilePicture, latestMessage, unreadCount, updatedAt
        );

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ConversationResponseDTO"));
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains(otherParticipantId.toString()));
        assertTrue(toString.contains(otherParticipantName));
        assertTrue(toString.contains(String.valueOf(unreadCount)));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // Act
        ConversationResponseDTO dto = new ConversationResponseDTO(null, null, null, null, null, 0, null);

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getOtherParticipantId());
        assertNull(dto.getOtherParticipantName());
        assertNull(dto.getOtherParticipantProfilePicture());
        assertNull(dto.getLatestMessage());
        assertEquals(0, dto.getUnreadCount());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle zero and negative unread counts")
    void testUnreadCountEdgeCases() {
        // Arrange
        ConversationResponseDTO dto = new ConversationResponseDTO();

        // Test zero unread count
        dto.setUnreadCount(0);
        assertEquals(0, dto.getUnreadCount());

        // Test negative unread count (should be allowed for flexibility)
        dto.setUnreadCount(-1);
        assertEquals(-1, dto.getUnreadCount());

        // Test large unread count
        dto.setUnreadCount(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, dto.getUnreadCount());
    }

    private MessageResponseDTO createSampleMessage() {
        return new MessageResponseDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Sample message content",
            false,
            OffsetDateTime.now()
        );
    }
    
    private ProfilePictureDTO createSampleProfilePicture() {
        return new ProfilePictureDTO(
            "https://example.com/full-picture.jpg",
            "https://example.com/thumbnail-picture.jpg",
            "cloudinary-public-id"
        );
    }
}