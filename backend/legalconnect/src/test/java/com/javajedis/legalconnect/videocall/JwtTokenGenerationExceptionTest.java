package com.javajedis.legalconnect.videocall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JwtTokenGenerationException.
 */
@DisplayName("JwtTokenGenerationException Tests")
class JwtTokenGenerationExceptionTest {

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        // Arrange
        String message = "JWT token generation failed";
        Throwable cause = new RuntimeException("Underlying error");

        // Act
        JwtTokenGenerationException exception = new JwtTokenGenerationException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message only")
    void testExceptionWithMessageOnly() {
        // Arrange
        String message = "JWT token generation failed";

        // Act
        JwtTokenGenerationException exception = new JwtTokenGenerationException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should be instance of Exception")
    void testExceptionInheritance() {
        // Arrange
        String message = "Test exception";

        // Act
        JwtTokenGenerationException exception = new JwtTokenGenerationException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(Exception.class, exception.getClass().getSuperclass());
    }

    @Test
    @DisplayName("Should handle null message")
    void testExceptionWithNullMessage() {
        // Act
        JwtTokenGenerationException exception = new JwtTokenGenerationException(null);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle null cause")
    void testExceptionWithNullCause() {
        // Arrange
        String message = "Test message";

        // Act
        JwtTokenGenerationException exception = new JwtTokenGenerationException(message, null);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}