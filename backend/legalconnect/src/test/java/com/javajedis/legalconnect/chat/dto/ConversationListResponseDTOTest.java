package com.javajedis.legalconnect.chat.dto;

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

@DisplayName("ConversationListResponseDTO Tests")
class ConversationListResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        List<ConversationResponseDTO> conversations = createSampleConversations();

        // Act
        ConversationListResponseDTO dto = new ConversationListResponseDTO(conversations);

        // Assert
        assertEquals(conversations, dto.getConversations());
        assertEquals(2, dto.getConversations().size());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        ConversationListResponseDTO dto = new ConversationListResponseDTO();

        // Assert
        assertNull(dto.getConversations());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        ConversationListResponseDTO dto = new ConversationListResponseDTO();
        List<ConversationResponseDTO> conversations = createSampleConversations();

        // Act
        dto.setConversations(conversations);

        // Assert
        assertEquals(conversations, dto.getConversations());
        assertEquals(2, dto.getConversations().size());
    }

    @Test
    @DisplayName("Should handle empty list")
    void testEmptyList() {
        // Arrange
        List<ConversationResponseDTO> emptyList = new ArrayList<>();
        ConversationListResponseDTO dto = new ConversationListResponseDTO(emptyList);

        // Assert
        assertNotNull(dto.getConversations());
        assertTrue(dto.getConversations().isEmpty());
        assertEquals(0, dto.getConversations().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        List<ConversationResponseDTO> conversations = createSampleConversations();
        List<ConversationResponseDTO> differentConversations = Arrays.asList(
            createSampleConversation("Different User", 1)
        );

        ConversationListResponseDTO dto1 = new ConversationListResponseDTO(conversations);
        ConversationListResponseDTO dto2 = new ConversationListResponseDTO(conversations);
        ConversationListResponseDTO dto3 = new ConversationListResponseDTO(differentConversations);

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
        List<ConversationResponseDTO> conversations = createSampleConversations();
        ConversationListResponseDTO dto = new ConversationListResponseDTO(conversations);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("ConversationListResponseDTO"));
    }

    @Test
    @DisplayName("Should handle null list")
    void testNullList() {
        // Act
        ConversationListResponseDTO dto = new ConversationListResponseDTO(null);

        // Assert
        assertNull(dto.getConversations());
    }

    @Test
    @DisplayName("Should handle single conversation")
    void testSingleConversation() {
        // Arrange
        List<ConversationResponseDTO> singleConversation = Arrays.asList(
            createSampleConversation("Single User", 3)
        );
        ConversationListResponseDTO dto = new ConversationListResponseDTO(singleConversation);

        // Assert
        assertEquals(1, dto.getConversations().size());
        assertEquals("Single User", dto.getConversations().get(0).getOtherParticipantName());
        assertEquals(3, dto.getConversations().get(0).getUnreadCount());
    }

    @Test
    @DisplayName("Should maintain list order")
    void testListOrder() {
        // Arrange
        ConversationResponseDTO first = createSampleConversation("First User", 1);
        ConversationResponseDTO second = createSampleConversation("Second User", 2);
        ConversationResponseDTO third = createSampleConversation("Third User", 3);
        
        List<ConversationResponseDTO> orderedList = Arrays.asList(first, second, third);
        ConversationListResponseDTO dto = new ConversationListResponseDTO(orderedList);

        // Assert
        assertEquals(3, dto.getConversations().size());
        assertEquals("First User", dto.getConversations().get(0).getOtherParticipantName());
        assertEquals("Second User", dto.getConversations().get(1).getOtherParticipantName());
        assertEquals("Third User", dto.getConversations().get(2).getOtherParticipantName());
    }

    private List<ConversationResponseDTO> createSampleConversations() {
        return Arrays.asList(
            createSampleConversation("John Doe", 5),
            createSampleConversation("Jane Smith", 2)
        );
    }

    private ConversationResponseDTO createSampleConversation(String participantName, int unreadCount) {
        MessageResponseDTO latestMessage = new MessageResponseDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Latest message from " + participantName,
            false,
            OffsetDateTime.now()
        );

        return new ConversationResponseDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            participantName,
            latestMessage,
            unreadCount,
            OffsetDateTime.now()
        );
    }
}