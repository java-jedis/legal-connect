package com.javajedis.legalconnect.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VerificationCodeException.
 * Tests the exception's constructors and behavior.
 */
@DisplayName("VerificationCodeException Tests")
class VerificationCodeExceptionTest {

    private static final String TEST_MESSAGE = "Verification code expired";
    private static final String CAUSE_MESSAGE = "Redis connection failed";

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // When
        VerificationCodeException exception = new VerificationCodeException(TEST_MESSAGE);

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
        VerificationCodeException exception = new VerificationCodeException(TEST_MESSAGE, cause);

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
        VerificationCodeException exception = new VerificationCodeException(null);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null cause")
    void shouldCreateExceptionWithNullCause() {
        // When
        VerificationCodeException exception = new VerificationCodeException(TEST_MESSAGE, null);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(null, exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // When & Then
        assertThrows(VerificationCodeException.class, () -> {
            throw new VerificationCodeException(TEST_MESSAGE);
        });
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        VerificationCodeException exception = new VerificationCodeException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // When
        VerificationCodeException exception = new VerificationCodeException("");

        // Then
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle specific verification scenarios")
    void shouldHandleSpecificVerificationScenarios() {
        // Given
        String[] scenarios = {
            "Verification code not found",
            "Verification code already used",
            "Verification code generation failed",
            "Verification code storage failed"
        };

        // When & Then
        for (String scenario : scenarios) {
            VerificationCodeException exception = new VerificationCodeException(scenario);
            assertNotNull(exception);
            assertEquals(scenario, exception.getMessage());
        }
    }
} 