package com.javajedis.legalconnect.common.exception;

/**
 * Exception thrown when Google Calendar operations fail.
 * This includes failures in creating, updating, or deleting calendar events.
 */
public class GoogleCalendarException extends RuntimeException {

    public GoogleCalendarException(String message) {
        super(message);
    }

    public GoogleCalendarException(String message, Throwable cause) {
        super(message, cause);
    }
} 