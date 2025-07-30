package com.javajedis.legalconnect.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Message Entity Tests")
class MessageTest {

    private Message message;
    private UUID testMessageId;
    private UUID testConversationId;
    private UUID testSenderId;
    private String testContent;
    private OffsetDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        testMessageId = UUID.randomUUID();
        testConversationId = UUID.randomUUID();
        testSenderId = UUID.randomUUID();
        testContent = "Test message content";
        testCreatedAt = OffsetDateTime.now();

        message = new Message();
        message.setId(testMessageId);
        message.setConversationId(testConversationId);
        message.setSenderId(testSenderId);
        message.setContent(testContent);
        message.setRead(false);
        message.setCreatedAt(testCreatedAt);
    }

    @Test
    @DisplayName("Should create message with default constructor")
    void testDefaultConstructor() {
        Message defaultMessage = new Message();

        assertNotNull(defaultMessage);
        assertNull(defaultMessage.getId());
        assertNull(defaultMessage.getConversationId());
        assertNull(defaultMessage.getSenderId());
        assertNull(defaultMessage.getContent());
        assertFalse(defaultMessage.isRead()); // Default value should be false
        assertNull(defaultMessage.getCreatedAt());
    }

    @Test
    @DisplayName("Should create message with all args constructor")
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Message constructedMessage = new Message(
            testMessageId, testConversationId, testSenderId, testContent, true, now
        );

        assertEquals(testMessageId, constructedMessage.getId());
        assertEquals(testConversationId, constructedMessage.getConversationId());
        assertEquals(testSenderId, constructedMessage.getSenderId());
        assertEquals(testContent, constructedMessage.getContent());
        assertTrue(constructedMessage.isRead());
        assertEquals(now, constructedMessage.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle id getter and setter")
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        message.setId(newId);
        assertEquals(newId, message.getId());
    }

    @Test
    @DisplayName("Should handle conversationId getter and setter")
    void testConversationIdGetterAndSetter() {
        UUID newConversationId = UUID.randomUUID();
        message.setConversationId(newConversationId);
        assertEquals(newConversationId, message.getConversationId());
    }

    @Test
    @DisplayName("Should handle senderId getter and setter")
    void testSenderIdGetterAndSetter() {
        UUID newSenderId = UUID.randomUUID();
        message.setSenderId(newSenderId);
        assertEquals(newSenderId, message.getSenderId());
    }

    @Test
    @DisplayName("Should handle content getter and setter")
    void testContentGetterAndSetter() {
        String newContent = "Updated message content with more details";
        message.setContent(newContent);
        assertEquals(newContent, message.getContent());
    }

    @Test
    @DisplayName("Should handle content with null value")
    void testContentWithNullValue() {
        message.setContent(null);
        assertNull(message.getContent());
    }

    @Test
    @DisplayName("Should handle content with empty string")
    void testContentWithEmptyString() {
        message.setContent("");
        assertEquals("", message.getContent());
    }

    @Test
    @DisplayName("Should handle isRead getter and setter")
    void testIsReadGetterAndSetter() {
        // Test setting to true
        message.setRead(true);
        assertTrue(message.isRead());

        // Test setting to false
        message.setRead(false);
        assertFalse(message.isRead());
    }

    @Test
    @DisplayName("Should handle createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        message.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, message.getCreatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Message message1 = new Message();
        message1.setId(testMessageId);
        message1.setConversationId(testConversationId);
        message1.setSenderId(testSenderId);
        message1.setContent("Test content");
        message1.setRead(false);

        Message message2 = new Message();
        message2.setId(testMessageId);
        message2.setConversationId(testConversationId);
        message2.setSenderId(testSenderId);
        message2.setContent("Test content");
        message2.setRead(false);

        Message message3 = new Message();
        message3.setId(UUID.randomUUID());
        message3.setConversationId(UUID.randomUUID());
        message3.setSenderId(UUID.randomUUID());
        message3.setContent("Different content");
        message3.setRead(true);

        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1, message3);
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with null values")
    void testEqualsWithNullValues() {
        Message message1 = new Message();
        Message message2 = new Message();

        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        String toString = message.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Test message content"));
        assertTrue(toString.contains("false")); // isRead value
        assertTrue(toString.contains(testConversationId.toString()));
        assertTrue(toString.contains(testSenderId.toString()));
    }

    @Test
    @DisplayName("Should handle default isRead value")
    void testDefaultIsReadValue() {
        Message newMessage = new Message();
        assertFalse(newMessage.isRead());
    }

    @Test
    @DisplayName("Should handle message with long content")
    void testMessageWithLongContent() {
        String longContent = "M".repeat(10000); // Long content
        message.setContent(longContent);
        assertEquals(longContent, message.getContent());
        assertEquals(10000, message.getContent().length());
    }

    @Test
    @DisplayName("Should handle message with special characters in content")
    void testMessageWithSpecialCharacters() {
        String specialContent = "Message with Special Characters: @#$%^&*() ñáéíóú 中文 العربية";
        message.setContent(specialContent);
        assertEquals(specialContent, message.getContent());
    }

    @Test
    @DisplayName("Should handle message read status transitions")
    void testMessageReadStatusTransitions() {
        // Start with unread (default)
        assertFalse(message.isRead());

        // Mark as read
        message.setRead(true);
        assertTrue(message.isRead());

        // Mark as unread again
        message.setRead(false);
        assertFalse(message.isRead());
    }

    @Test
    @DisplayName("Should handle message with minimal data")
    void testMessageWithMinimalData() {
        Message minimalMessage = new Message();
        minimalMessage.setConversationId(testConversationId);
        minimalMessage.setSenderId(testSenderId);
        minimalMessage.setContent("M"); // Minimum content length

        assertEquals(testConversationId, minimalMessage.getConversationId());
        assertEquals(testSenderId, minimalMessage.getSenderId());
        assertEquals("M", minimalMessage.getContent());
        assertFalse(minimalMessage.isRead()); // Default value
        assertNull(minimalMessage.getId()); // Not set
        assertNull(minimalMessage.getCreatedAt()); // Not set
    }

    @Test
    @DisplayName("Should handle message entity fields validation")
    void testMessageEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(message.getId());
        assertNotNull(message.getConversationId());
        assertNotNull(message.getSenderId());
        assertNotNull(message.getContent());
        assertNotNull(message.getCreatedAt());
        // isRead is primitive boolean, so it's never null
    }

    @Test
    @DisplayName("Should handle message with various content types")
    void testMessageWithVariousContentTypes() {
        String[] contentTypes = {
            "Simple text message",
            "Message with numbers: 123456",
            "Message with HTML: <b>Bold</b> text",
            "Message with JSON: {\"key\": \"value\"}",
            "Message with URL: https://example.com/path?param=value",
            "Multi-line\nmessage\ncontent"
        };

        for (String content : contentTypes) {
            message.setContent(content);
            assertEquals(content, message.getContent());
        }
    }

    @Test
    @DisplayName("Should handle message timestamps")
    void testMessageWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(1);
        OffsetDateTime future = now.plusHours(1);

        // Test with current time
        message.setCreatedAt(now);
        assertEquals(now, message.getCreatedAt());

        // Test with past time
        message.setCreatedAt(past);
        assertEquals(past, message.getCreatedAt());

        // Test with future time
        message.setCreatedAt(future);
        assertEquals(future, message.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle message equality with different read states")
    void testMessageEqualityWithDifferentReadStates() {
        Message message1 = new Message();
        message1.setId(testMessageId);
        message1.setConversationId(testConversationId);
        message1.setSenderId(testSenderId);
        message1.setContent("Test content");
        message1.setRead(false);

        Message message2 = new Message();
        message2.setId(testMessageId);
        message2.setConversationId(testConversationId);
        message2.setSenderId(testSenderId);
        message2.setContent("Test content");
        message2.setRead(true); // Different read state

        // With Lombok @Data, objects with different field values are not equal
        assertNotEquals(message1, message2);
    }

    @Test
    @DisplayName("Should handle message with null IDs")
    void testMessageWithNullIds() {
        message.setConversationId(null);
        message.setSenderId(null);
        
        assertNull(message.getConversationId());
        assertNull(message.getSenderId());
    }

    @Test
    @DisplayName("Should handle message content edge cases")
    void testMessageContentEdgeCases() {
        // Test with whitespace-only content
        message.setContent("   ");
        assertEquals("   ", message.getContent());

        // Test with tab and newline characters
        message.setContent("\t\n\r");
        assertEquals("\t\n\r", message.getContent());

        // Test with single character
        message.setContent("A");
        assertEquals("A", message.getContent());
    }
}