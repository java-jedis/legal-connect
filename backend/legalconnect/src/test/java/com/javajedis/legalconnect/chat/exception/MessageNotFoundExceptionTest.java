package com.javajedis.legalconnect.chat.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageNotFoundException.
 */
class MessageNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Message not found";
        MessageNotFoundException exception = new MessageNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Message not found";
        Throwable cause = new RuntimeException("Database error");
        MessageNotFoundException exception = new MessageNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        MessageNotFoundException exception = new MessageNotFoundException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionWithNullMessage() {
        MessageNotFoundException exception = new MessageNotFoundException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String message = "";
        MessageNotFoundException exception = new MessageNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }
}