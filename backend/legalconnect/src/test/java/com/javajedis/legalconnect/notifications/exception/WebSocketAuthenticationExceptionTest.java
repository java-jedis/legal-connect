package com.javajedis.legalconnect.notifications.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for WebSocketAuthenticationException.
 * Tests the exception's constructors and behavior.
 */
@DisplayName("WebSocketAuthenticationException Tests")
class WebSocketAuthenticationExceptionTest {

    private static final String TEST_MESSAGE = "WebSocket authentication failed";
    private static final String CAUSE_MESSAGE = "Invalid JWT token";

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // When
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException(TEST_MESSAGE);

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
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException(TEST_MESSAGE, cause);

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
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException(null);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null cause")
    void shouldCreateExceptionWithNullCause() {
        // When
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException(TEST_MESSAGE, null);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(null, exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // When & Then
        assertThrows(WebSocketAuthenticationException.class, () -> {
            throw new WebSocketAuthenticationException(TEST_MESSAGE);
        });
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // When
        WebSocketAuthenticationException exception = new WebSocketAuthenticationException("");

        // Then
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }
}