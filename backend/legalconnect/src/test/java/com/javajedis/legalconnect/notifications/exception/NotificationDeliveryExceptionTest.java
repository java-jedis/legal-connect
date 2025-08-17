package com.javajedis.legalconnect.notifications.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for NotificationDeliveryException.
 * Tests the exception's constructors and behavior.
 */
@DisplayName("NotificationDeliveryException Tests")
class NotificationDeliveryExceptionTest {

    private static final String TEST_MESSAGE = "Notification delivery failed";
    private static final String CAUSE_MESSAGE = "WebSocket connection error";

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // When
        NotificationDeliveryException exception = new NotificationDeliveryException(TEST_MESSAGE);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(RuntimeException.class, exception.getClass().getSuperclass());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        Throwable cause = new RuntimeException(CAUSE_MESSAGE);

        // When
        NotificationDeliveryException exception = new NotificationDeliveryException(TEST_MESSAGE, cause);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(CAUSE_MESSAGE, exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should create exception with null message")
    void shouldCreateExceptionWithNullMessage() {
        // When
        NotificationDeliveryException exception = new NotificationDeliveryException(null);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null cause")
    void shouldCreateExceptionWithNullCause() {
        // When
        NotificationDeliveryException exception = new NotificationDeliveryException(TEST_MESSAGE, null);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(null, exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // When & Then
        assertThrows(NotificationDeliveryException.class, () -> {
            throw new NotificationDeliveryException(TEST_MESSAGE);
        });
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        NotificationDeliveryException exception = new NotificationDeliveryException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // When
        NotificationDeliveryException exception = new NotificationDeliveryException("");

        // Then
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }
}