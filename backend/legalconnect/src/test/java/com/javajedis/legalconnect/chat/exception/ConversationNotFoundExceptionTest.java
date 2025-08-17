package com.javajedis.legalconnect.chat.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConversationNotFoundException.
 */
class ConversationNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Conversation not found";
        ConversationNotFoundException exception = new ConversationNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Conversation not found";
        Throwable cause = new RuntimeException("Database error");
        ConversationNotFoundException exception = new ConversationNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ConversationNotFoundException exception = new ConversationNotFoundException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionWithNullMessage() {
        ConversationNotFoundException exception = new ConversationNotFoundException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String message = "";
        ConversationNotFoundException exception = new ConversationNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }
}