package com.javajedis.legalconnect.common.exception;

/**
 * Exception thrown when a lawyer attempts to perform an action that requires verification
 * but their verification status is not APPROVED.
 */
public class LawyerNotVerifiedException extends RuntimeException {

    public LawyerNotVerifiedException(String message) {
        super(message);
    }

    public LawyerNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
} 