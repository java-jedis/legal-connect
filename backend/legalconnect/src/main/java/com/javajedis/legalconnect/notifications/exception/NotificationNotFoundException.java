package com.javajedis.legalconnect.notifications.exception;

/**
 * Exception thrown when a notification is not found in the system.
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}