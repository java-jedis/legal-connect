package com.javajedis.legalconnect.chat.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

@DisplayName("MessageListResponseDTO Tests")
class MessageListResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        List<MessageResponseDTO> messages = createSampleMessages();

        // Act
        MessageListResponseDTO dto = new MessageListResponseDTO(messages);

        // Assert
        assertEquals(messages, dto.getMessages());
        assertEquals(3, dto.getMessages().size());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        MessageListResponseDTO dto = new MessageListResponseDTO();

        // Assert
        assertNull(dto.getMessages());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        MessageListResponseDTO dto = new MessageListResponseDTO();
        List<MessageResponseDTO> messages = createSampleMessages();

        // Act
        dto.setMessages(messages);

        // Assert
        assertEquals(messages, dto.getMessages());
        assertEquals(3, dto.getMessages().size());
    }

    @Test
    @DisplayName("Should handle empty list")
    void testEmptyList() {
        // Arrange
        List<MessageResponseDTO> emptyList = new ArrayList<>();
        MessageListResponseDTO dto = new MessageListResponseDTO(emptyList);

        // Assert
        assertNotNull(dto.getMessages());
        assertTrue(dto.getMessages().isEmpty());
        assertEquals(0, dto.getMessages().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        List<MessageResponseDTO> messages = createSampleMessages();
        List<MessageResponseDTO> differentMessages = Arrays.asList(
            createSampleMessage("Different content", true)
        );

        MessageListResponseDTO dto1 = new MessageListResponseDTO(messages);
        MessageListResponseDTO dto2 = new MessageListResponseDTO(messages);
        MessageListResponseDTO dto3 = new MessageListResponseDTO(differentMessages);

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
        List<MessageResponseDTO> messages = createSampleMessages();
        MessageListResponseDTO dto = new MessageListResponseDTO(messages);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("MessageListResponseDTO"));
    }

    @Test
    @DisplayName("Should handle null list")
    void testNullList() {
        // Act
        MessageListResponseDTO dto = new MessageListResponseDTO(null);

        // Assert
        assertNull(dto.getMessages());
    }

    @Test
    @DisplayName("Should handle single message")
    void testSingleMessage() {
        // Arrange
        List<MessageResponseDTO> singleMessage = Arrays.asList(
            createSampleMessage("Single message", false)
        );
        MessageListResponseDTO dto = new MessageListResponseDTO(singleMessage);

        // Assert
        assertEquals(1, dto.getMessages().size());
        assertEquals("Single message", dto.getMessages().get(0).getContent());
        assertFalse(dto.getMessages().get(0).isRead());
    }

    @Test
    @DisplayName("Should maintain message order")
    void testMessageOrder() {
        // Arrange
        MessageResponseDTO first = createSampleMessage("First message", true);
        MessageResponseDTO second = createSampleMessage("Second message", false);
        MessageResponseDTO third = createSampleMessage("Third message", true);
        
        List<MessageResponseDTO> orderedList = Arrays.asList(first, second, third);
        MessageListResponseDTO dto = new MessageListResponseDTO(orderedList);

        // Assert
        assertEquals(3, dto.getMessages().size());
        assertEquals("First message", dto.getMessages().get(0).getContent());
        assertEquals("Second message", dto.getMessages().get(1).getContent());
        assertEquals("Third message", dto.getMessages().get(2).getContent());
        assertTrue(dto.getMessages().get(0).isRead());
        assertFalse(dto.getMessages().get(1).isRead());
        assertTrue(dto.getMessages().get(2).isRead());
    }

    @Test
    @DisplayName("Should handle mixed read status messages")
    void testMixedReadStatus() {
        // Arrange
        List<MessageResponseDTO> mixedMessages = Arrays.asList(
            createSampleMessage("Read message", true),
            createSampleMessage("Unread message", false),
            createSampleMessage("Another read message", true)
        );
        MessageListResponseDTO dto = new MessageListResponseDTO(mixedMessages);

        // Assert
        assertEquals(3, dto.getMessages().size());
        assertTrue(dto.getMessages().get(0).isRead());
        assertFalse(dto.getMessages().get(1).isRead());
        assertTrue(dto.getMessages().get(2).isRead());
    }

    private List<MessageResponseDTO> createSampleMessages() {
        return Arrays.asList(
            createSampleMessage("First message", true),
            createSampleMessage("Second message", false),
            createSampleMessage("Third message", true)
        );
    }

    private MessageResponseDTO createSampleMessage(String content, boolean isRead) {
        return new MessageResponseDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            content,
            isRead,
            OffsetDateTime.now()
        );
    }
}