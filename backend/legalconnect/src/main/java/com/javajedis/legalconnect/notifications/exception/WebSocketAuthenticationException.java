package com.javajedis.legalconnect.notifications.exception;

/**
 * Exception thrown when WebSocket authentication fails.
 */
public class WebSocketAuthenticationException extends RuntimeException {

    public WebSocketAuthenticationException(String message) {
        super(message);
    }

    public WebSocketAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}