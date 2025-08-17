package com.javajedis.legalconnect.notifications.exception;

/**
 * Exception thrown when notification delivery fails.
 * This is used for WebSocket delivery failures and other delivery-related errors.
 */
public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(String message) {
        super(message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}