package com.javajedis.legalconnect.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EmailNotVerifiedException.
 * Tests the exception's constructors and behavior.
 */
@DisplayName("EmailNotVerifiedException Tests")
class EmailNotVerifiedExceptionTest {

    private static final String TEST_MESSAGE = "Email verification required";
    private static final String CAUSE_MESSAGE = "Underlying cause";

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // When
        EmailNotVerifiedException exception = new EmailNotVerifiedException(TEST_MESSAGE);

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
        EmailNotVerifiedException exception = new EmailNotVerifiedException(TEST_MESSAGE, cause);

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
        EmailNotVerifiedException exception = new EmailNotVerifiedException(null);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null cause")
    void shouldCreateExceptionWithNullCause() {
        // When
        EmailNotVerifiedException exception = new EmailNotVerifiedException(TEST_MESSAGE, null);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(null, exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // When & Then
        assertThrows(EmailNotVerifiedException.class, () -> {
            throw new EmailNotVerifiedException(TEST_MESSAGE);
        });
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        EmailNotVerifiedException exception = new EmailNotVerifiedException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // When
        EmailNotVerifiedException exception = new EmailNotVerifiedException("");

        // Then
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }
} 