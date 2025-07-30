package com.javajedis.legalconnect.chat.exception;

/**
 * Exception thrown when a conversation is not found in the system.
 */
public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(String message) {
        super(message);
    }

    public ConversationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}