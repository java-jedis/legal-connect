package com.javajedis.legalconnect.common.exception;

/**
 * Exception thrown when an error occurs during verification code operations.
 * This includes errors during generation, storage, retrieval, and validation of verification codes.
 */
public class VerificationCodeException extends RuntimeException {

    /**
     * Constructs a new VerificationCodeException with the specified detail message.
     *
     * @param message the detail message
     */
    public VerificationCodeException(String message) {
        super(message);
    }

    /**
     * Constructs a new VerificationCodeException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public VerificationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
