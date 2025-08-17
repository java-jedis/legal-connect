package com.javajedis.legalconnect.chat.exception;

/**
 * Exception thrown when a message is not found in the system.
 */
public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(String message) {
        super(message);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}