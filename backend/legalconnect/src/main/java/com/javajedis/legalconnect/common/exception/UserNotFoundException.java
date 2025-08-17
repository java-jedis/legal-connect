package com.javajedis.legalconnect.common.exception;

/**
 * Exception thrown when a user is not found in the system.
 * This is different from UsernameNotFoundException which is used for authentication.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}