package com.javajedis.legalconnect.common.exception;

/**
 * Exception thrown when a user tries to access a resource that requires email verification
 * but their email is not verified.
 */
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }

    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
