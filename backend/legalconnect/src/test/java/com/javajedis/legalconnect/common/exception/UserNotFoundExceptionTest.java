package com.javajedis.legalconnect.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserNotFoundException.
 * Tests the exception's constructors and behavior.
 */
@DisplayName("UserNotFoundException Tests")
class UserNotFoundExceptionTest {

    private static final String TEST_MESSAGE = "User not found with ID: 123";
    private static final String CAUSE_MESSAGE = "Database connection failed";

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // When
        UserNotFoundException exception = new UserNotFoundException(TEST_MESSAGE);

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
        UserNotFoundException exception = new UserNotFoundException(TEST_MESSAGE, cause);

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
        UserNotFoundException exception = new UserNotFoundException(null);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null cause")
    void shouldCreateExceptionWithNullCause() {
        // When
        UserNotFoundException exception = new UserNotFoundException(TEST_MESSAGE, null);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(null, exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(TEST_MESSAGE);
        });
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        UserNotFoundException exception = new UserNotFoundException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // When
        UserNotFoundException exception = new UserNotFoundException("");

        // Then
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle complex cause chain")
    void shouldHandleComplexCauseChain() {
        // Given
        Throwable rootCause = new IllegalArgumentException("Invalid argument");
        Throwable intermediateCause = new RuntimeException("Intermediate error", rootCause);
        Throwable cause = new RuntimeException(CAUSE_MESSAGE, intermediateCause);

        // When
        UserNotFoundException exception = new UserNotFoundException(TEST_MESSAGE, cause);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(intermediateCause, exception.getCause().getCause());
        assertEquals(rootCause, exception.getCause().getCause().getCause());
    }
}