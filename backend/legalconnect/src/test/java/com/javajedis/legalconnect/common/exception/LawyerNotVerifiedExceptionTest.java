package com.javajedis.legalconnect.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LawyerNotVerifiedException.
 * Tests the exception's constructors and behavior.
 */
@DisplayName("LawyerNotVerifiedException Tests")
class LawyerNotVerifiedExceptionTest {

    private static final String TEST_MESSAGE = "Lawyer verification is required to perform this action";
    private static final String CAUSE_MESSAGE = "Verification status check failed";

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        // When
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(TEST_MESSAGE);

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
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(TEST_MESSAGE, cause);

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
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(null);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with empty message")
    void shouldCreateExceptionWithEmptyMessage() {
        // When
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException("");

        // Then
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null message and cause")
    void shouldCreateExceptionWithNullMessageAndCause() {
        // Given
        Throwable cause = new RuntimeException(CAUSE_MESSAGE);

        // When
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(null, cause);

        // Then
        assertNotNull(exception);
        assertEquals(null, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(CAUSE_MESSAGE, exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should create exception with message and null cause")
    void shouldCreateExceptionWithMessageAndNullCause() {
        // When
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(TEST_MESSAGE, null);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(null, exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable as RuntimeException")
    void shouldBeThrowableAsRuntimeException() {
        // When & Then
        assertThrows(LawyerNotVerifiedException.class, () -> {
            throw new LawyerNotVerifiedException(TEST_MESSAGE);
        });
    }

    @Test
    @DisplayName("Should be throwable as RuntimeException with cause")
    void shouldBeThrowableAsRuntimeExceptionWithCause() {
        // Given
        Throwable cause = new RuntimeException(CAUSE_MESSAGE);

        // When & Then
        LawyerNotVerifiedException exception = assertThrows(LawyerNotVerifiedException.class, () -> {
            throw new LawyerNotVerifiedException(TEST_MESSAGE, cause);
        });

        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should maintain exception hierarchy")
    void shouldMaintainExceptionHierarchy() {
        // Given
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(TEST_MESSAGE);

        // Then
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    @DisplayName("Should work with different message types")
    void shouldWorkWithDifferentMessageTypes() {
        // Test with various message formats
        String[] messages = {
            "Simple message",
            "Message with numbers: 123",
            "Message with special chars: !@#$%^&*()",
            "Very long message that contains multiple sentences. This is to test how the exception handles longer text content that might be used in real-world scenarios.",
            "Message\nwith\nnewlines",
            "Message\twith\ttabs"
        };

        for (String message : messages) {
            LawyerNotVerifiedException exception = new LawyerNotVerifiedException(message);
            assertEquals(message, exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should preserve stack trace information")
    void shouldPreserveStackTraceInformation() {
        // When
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        
        // Check that our test method appears in the stack trace
        boolean foundTestMethod = false;
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.getMethodName().contains("shouldPreserveStackTraceInformation")) {
                foundTestMethod = true;
                break;
            }
        }
        assertTrue(foundTestMethod, "Test method should appear in stack trace");
    }

    @Test
    @DisplayName("Should handle chained exceptions properly")
    void shouldHandleChainedExceptionsProperly() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalStateException intermediateCause = new IllegalStateException("Intermediate cause", rootCause);

        // When
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException(TEST_MESSAGE, intermediateCause);

        // Then
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals("Intermediate cause", exception.getCause().getMessage());
        assertEquals(rootCause, exception.getCause().getCause());
        assertEquals("Root cause", exception.getCause().getCause().getMessage());
    }
} 