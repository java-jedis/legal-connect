package com.javajedis.legalconnect.chat.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatDeliveryException.
 */
class ChatDeliveryExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Chat delivery failed";
        ChatDeliveryException exception = new ChatDeliveryException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Chat delivery failed";
        Throwable cause = new RuntimeException("WebSocket error");
        ChatDeliveryException exception = new ChatDeliveryException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ChatDeliveryException exception = new ChatDeliveryException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionWithNullMessage() {
        ChatDeliveryException exception = new ChatDeliveryException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String message = "";
        ChatDeliveryException exception = new ChatDeliveryException(message);
        
        assertEquals(message, exception.getMessage());
    }
}