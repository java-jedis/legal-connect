package com.javajedis.legalconnect.chat.exception;

/**
 * Exception thrown when chat message delivery fails.
 * This is used for WebSocket delivery failures and other delivery-related errors.
 */
public class ChatDeliveryException extends RuntimeException {

    public ChatDeliveryException(String message) {
        super(message);
    }

    public ChatDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}