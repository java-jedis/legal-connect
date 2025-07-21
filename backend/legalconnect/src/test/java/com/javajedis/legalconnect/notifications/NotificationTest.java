package com.javajedis.legalconnect.notifications;

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

@DisplayName("Notification Entity Tests")
class NotificationTest {

    private Notification notification;
    private UUID testNotificationId;
    private UUID testReceiverId;
    private String testContent;
    private OffsetDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        testNotificationId = UUID.randomUUID();
        testReceiverId = UUID.randomUUID();
        testContent = "Test notification content";
        testCreatedAt = OffsetDateTime.now();

        notification = new Notification();
        notification.setId(testNotificationId);
        notification.setReceiverId(testReceiverId);
        notification.setContent(testContent);
        notification.setRead(false);
        notification.setCreatedAt(testCreatedAt);
    }

    @Test
    @DisplayName("Should create notification with default constructor")
    void testDefaultConstructor() {
        Notification defaultNotification = new Notification();

        assertNotNull(defaultNotification);
        assertNull(defaultNotification.getId());
        assertNull(defaultNotification.getReceiverId());
        assertNull(defaultNotification.getContent());
        assertFalse(defaultNotification.isRead()); // Default value should be false
        assertNull(defaultNotification.getCreatedAt());
    }

    @Test
    @DisplayName("Should create notification with all args constructor")
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Notification constructedNotification = new Notification(
            testNotificationId, testReceiverId, testContent, true, now
        );

        assertEquals(testNotificationId, constructedNotification.getId());
        assertEquals(testReceiverId, constructedNotification.getReceiverId());
        assertEquals(testContent, constructedNotification.getContent());
        assertTrue(constructedNotification.isRead());
        assertEquals(now, constructedNotification.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle id getter and setter")
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        notification.setId(newId);
        assertEquals(newId, notification.getId());
    }

    @Test
    @DisplayName("Should handle receiverId getter and setter")
    void testReceiverIdGetterAndSetter() {
        UUID newReceiverId = UUID.randomUUID();
        notification.setReceiverId(newReceiverId);
        assertEquals(newReceiverId, notification.getReceiverId());
    }

    @Test
    @DisplayName("Should handle content getter and setter")
    void testContentGetterAndSetter() {
        String newContent = "Updated notification content with more details";
        notification.setContent(newContent);
        assertEquals(newContent, notification.getContent());
    }

    @Test
    @DisplayName("Should handle content with null value")
    void testContentWithNullValue() {
        notification.setContent(null);
        assertNull(notification.getContent());
    }

    @Test
    @DisplayName("Should handle content with empty string")
    void testContentWithEmptyString() {
        notification.setContent("");
        assertEquals("", notification.getContent());
    }

    @Test
    @DisplayName("Should handle isRead getter and setter")
    void testIsReadGetterAndSetter() {
        // Test setting to true
        notification.setRead(true);
        assertTrue(notification.isRead());

        // Test setting to false
        notification.setRead(false);
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should handle createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        notification.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, notification.getCreatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Notification notification1 = new Notification();
        notification1.setId(testNotificationId);
        notification1.setReceiverId(testReceiverId);
        notification1.setContent("Test content");
        notification1.setRead(false);

        Notification notification2 = new Notification();
        notification2.setId(testNotificationId);
        notification2.setReceiverId(testReceiverId);
        notification2.setContent("Test content");
        notification2.setRead(false);

        Notification notification3 = new Notification();
        notification3.setId(UUID.randomUUID());
        notification3.setReceiverId(UUID.randomUUID());
        notification3.setContent("Different content");
        notification3.setRead(true);

        assertEquals(notification1, notification2);
        assertEquals(notification1.hashCode(), notification2.hashCode());
        assertNotEquals(notification1, notification3);
        assertNotEquals(notification1.hashCode(), notification3.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with null values")
    void testEqualsWithNullValues() {
        Notification notification1 = new Notification();
        Notification notification2 = new Notification();

        assertEquals(notification1, notification2);
        assertEquals(notification1.hashCode(), notification2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        String toString = notification.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Test notification content"));
        assertTrue(toString.contains("false")); // isRead value
        assertTrue(toString.contains(testReceiverId.toString()));
    }

    @Test
    @DisplayName("Should handle default isRead value")
    void testDefaultIsReadValue() {
        Notification newNotification = new Notification();
        assertFalse(newNotification.isRead());
    }

    @Test
    @DisplayName("Should handle notification with long content")
    void testNotificationWithLongContent() {
        String longContent = "C".repeat(10000); // Long content
        notification.setContent(longContent);
        assertEquals(longContent, notification.getContent());
        assertEquals(10000, notification.getContent().length());
    }

    @Test
    @DisplayName("Should handle notification with special characters in content")
    void testNotificationWithSpecialCharacters() {
        String specialContent = "Notification with Special Characters: @#$%^&*() ñáéíóú 中文 العربية";
        notification.setContent(specialContent);
        assertEquals(specialContent, notification.getContent());
    }

    @Test
    @DisplayName("Should handle notification read status transitions")
    void testNotificationReadStatusTransitions() {
        // Start with unread (default)
        assertFalse(notification.isRead());

        // Mark as read
        notification.setRead(true);
        assertTrue(notification.isRead());

        // Mark as unread again
        notification.setRead(false);
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should handle notification with minimal data")
    void testNotificationWithMinimalData() {
        Notification minimalNotification = new Notification();
        minimalNotification.setReceiverId(testReceiverId);
        minimalNotification.setContent("M"); // Minimum content length

        assertEquals(testReceiverId, minimalNotification.getReceiverId());
        assertEquals("M", minimalNotification.getContent());
        assertFalse(minimalNotification.isRead()); // Default value
        assertNull(minimalNotification.getId()); // Not set
        assertNull(minimalNotification.getCreatedAt()); // Not set
    }

    @Test
    @DisplayName("Should handle notification entity fields validation")
    void testNotificationEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(notification.getId());
        assertNotNull(notification.getReceiverId());
        assertNotNull(notification.getContent());
        assertNotNull(notification.getCreatedAt());
        // isRead is primitive boolean, so it's never null
    }

    @Test
    @DisplayName("Should handle notification with various content types")
    void testNotificationWithVariousContentTypes() {
        String[] contentTypes = {
            "Simple text notification",
            "Notification with numbers: 123456",
            "Notification with HTML: <b>Bold</b> text",
            "Notification with JSON: {\"key\": \"value\"}",
            "Notification with URL: https://example.com/path?param=value",
            "Multi-line\nnotification\ncontent"
        };

        for (String content : contentTypes) {
            notification.setContent(content);
            assertEquals(content, notification.getContent());
        }
    }

    @Test
    @DisplayName("Should handle notification timestamps")
    void testNotificationWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(1);
        OffsetDateTime future = now.plusHours(1);

        // Test with current time
        notification.setCreatedAt(now);
        assertEquals(now, notification.getCreatedAt());

        // Test with past time
        notification.setCreatedAt(past);
        assertEquals(past, notification.getCreatedAt());

        // Test with future time
        notification.setCreatedAt(future);
        assertEquals(future, notification.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle notification equality with different read states")
    void testNotificationEqualityWithDifferentReadStates() {
        Notification notification1 = new Notification();
        notification1.setId(testNotificationId);
        notification1.setReceiverId(testReceiverId);
        notification1.setContent("Test content");
        notification1.setRead(false);

        Notification notification2 = new Notification();
        notification2.setId(testNotificationId);
        notification2.setReceiverId(testReceiverId);
        notification2.setContent("Test content");
        notification2.setRead(true); // Different read state

        // With Lombok @Data, objects with different field values are not equal
        // This tests the actual behavior of the Lombok-generated equals method
        assertNotEquals(notification1, notification2);
    }

    @Test
    @DisplayName("Should handle notification with null receiver ID")
    void testNotificationWithNullReceiverId() {
        notification.setReceiverId(null);
        assertNull(notification.getReceiverId());
    }

    @Test
    @DisplayName("Should handle notification content edge cases")
    void testNotificationContentEdgeCases() {
        // Test with whitespace-only content
        notification.setContent("   ");
        assertEquals("   ", notification.getContent());

        // Test with tab and newline characters
        notification.setContent("\t\n\r");
        assertEquals("\t\n\r", notification.getContent());

        // Test with single character
        notification.setContent("A");
        assertEquals("A", notification.getContent());
    }
}