package com.javajedis.legalconnect.common.exception;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.javajedis.legalconnect.common.dto.ApiResponse;

import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for centralized error processing.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles authentication errors (invalid credentials).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ApiResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles bad credentials specifically.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials provided: {}", ex.getMessage());
        return ApiResponse.error("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles disabled account exceptions.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<String>> handleDisabledAccount(DisabledException ex) {
        log.warn("Disabled account login attempt: {}", ex.getMessage());
        return ApiResponse.error("Account is disabled. Please contact support.", HttpStatus.FORBIDDEN);
    }

    /**
     * Handles locked account exceptions.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<String>> handleLockedAccount(LockedException ex) {
        log.warn("Locked account login attempt: {}", ex.getMessage());
        return ApiResponse.error("Account is locked. Please contact support.", HttpStatus.FORBIDDEN);
    }

    /**
     * Handles authentication user not found errors.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ApiResponse.error("User not found with the provided credentials", HttpStatus.NOT_FOUND);
    }

    /**
     * Handles email verification requirement errors.
     * This is needed because EmailNotVerifiedException is thrown from filters.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<String>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        log.warn("Email not verified: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles lawyer verification requirement errors.
     */
    @ExceptionHandler(LawyerNotVerifiedException.class)
    public ResponseEntity<ApiResponse<String>> handleLawyerNotVerified(LawyerNotVerifiedException ex) {
        log.warn("Lawyer not verified: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles servlet operation errors.
     */
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ApiResponse<String>> handleServletException(ServletException ex) {
        log.error("Servlet error occurred: ", ex);
        return ApiResponse.error("Internal server error during request processing", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles I/O operation errors.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<String>> handleIOException(IOException ex) {
        log.error("I/O error occurred: ", ex);
        return ApiResponse.error("Internal server error during I/O operations", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles invalid argument errors.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument provided: {}", ex.getMessage());
        return ApiResponse.error("Invalid request parameters: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles runtime errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error occurred: ", ex);
        return ApiResponse.error("An unexpected runtime error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        String errorMessage = String.join(", ", errors);
        log.warn("Validation failed: {}", errorMessage);

        return ApiResponse.error(errorMessage, HttpStatus.BAD_REQUEST);

    }

    /**
     * Handles generic unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return ApiResponse.error("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
