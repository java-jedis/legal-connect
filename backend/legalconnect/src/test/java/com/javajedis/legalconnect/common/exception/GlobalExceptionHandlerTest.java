package com.javajedis.legalconnect.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.javajedis.legalconnect.chat.exception.ChatDeliveryException;
import com.javajedis.legalconnect.chat.exception.ConversationNotFoundException;
import com.javajedis.legalconnect.chat.exception.MessageNotFoundException;
import com.javajedis.legalconnect.common.dto.ApiResponse;

import jakarta.servlet.ServletException;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests the exception handling behavior for various exception types.
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleAccessDeniedException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied. You don't have permission to access this resource.", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle AuthenticationException")
    void shouldHandleAuthenticationException() {
        // Given
        AuthenticationException exception = new BadCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleAuthenticationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle BadCredentialsException")
    void shouldHandleBadCredentialsException() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleBadCredentials(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle DisabledException")
    void shouldHandleDisabledException() {
        // Given
        DisabledException exception = new DisabledException("Account disabled");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleDisabledAccount(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Account is disabled. Please contact support.", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle LockedException")
    void shouldHandleLockedException() {
        // Given
        LockedException exception = new LockedException("Account locked");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleLockedAccount(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Account is locked. Please contact support.", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle UsernameNotFoundException")
    void shouldHandleUsernameNotFoundException() {
        // Given
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleUsernameNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found with the provided credentials", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle EmailNotVerifiedException")
    void shouldHandleEmailNotVerifiedException() {
        // Given
        EmailNotVerifiedException exception = new EmailNotVerifiedException("Email not verified");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleEmailNotVerified(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Email not verified", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle LawyerNotVerifiedException")
    void shouldHandleLawyerNotVerifiedException() {
        // Given
        LawyerNotVerifiedException exception = new LawyerNotVerifiedException("Lawyer not verified");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleLawyerNotVerified(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Lawyer not verified", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle UserNotFoundException")
    void shouldHandleUserNotFoundException() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User not found with ID: 123");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle ServletException")
    void shouldHandleServletException() {
        // Given
        ServletException exception = new ServletException("Servlet error");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleServletException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error during request processing", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle IOException")
    void shouldHandleIOException() {
        // Given
        IOException exception = new IOException("I/O error");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleIOException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error during I/O operations", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getError().getMessage().contains("Invalid request parameters"));
        assertTrue(response.getBody().getError().getMessage().contains("Invalid argument"));
    }

    @Test
    @DisplayName("Should handle RuntimeException")
    void shouldHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Runtime error");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected runtime error occurred", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        FieldError fieldError1 = new FieldError("object", "field1", "Field 1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field 2 is invalid");
        List<FieldError> fieldErrors = List.of(fieldError1, fieldError2);
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        String errorMessage = response.getBody().getError().getMessage();
        assertTrue(errorMessage.contains("Field 1 is required"));
        assertTrue(errorMessage.contains("Field 2 is invalid"));
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new Exception("Generic error");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle null exception message")
    void shouldHandleNullExceptionMessage() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected runtime error occurred", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle empty exception message")
    void shouldHandleEmptyExceptionMessage() {
        // Given
        RuntimeException exception = new RuntimeException("");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected runtime error occurred", response.getBody().getError().getMessage());
    }

    // Chat Exception Tests

    @Test
    @DisplayName("Should handle ConversationNotFoundException")
    void shouldHandleConversationNotFoundException() {
        // Given
        ConversationNotFoundException exception = new ConversationNotFoundException("Conversation not found");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleConversationNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conversation not found", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle MessageNotFoundException")
    void shouldHandleMessageNotFoundException() {
        // Given
        MessageNotFoundException exception = new MessageNotFoundException("Message not found");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleMessageNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message not found", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle ChatDeliveryException")
    void shouldHandleChatDeliveryException() {
        // Given
        ChatDeliveryException exception = new ChatDeliveryException("Chat delivery failed");

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleChatDelivery(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message delivery failed, but the message was saved", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle ConversationNotFoundException with null message")
    void shouldHandleConversationNotFoundExceptionWithNullMessage() {
        // Given
        ConversationNotFoundException exception = new ConversationNotFoundException(null);

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleConversationNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(null, response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle MessageNotFoundException with cause")
    void shouldHandleMessageNotFoundExceptionWithCause() {
        // Given
        Throwable cause = new RuntimeException("Database error");
        MessageNotFoundException exception = new MessageNotFoundException("Message not found", cause);

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleMessageNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message not found", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should handle ChatDeliveryException with cause")
    void shouldHandleChatDeliveryExceptionWithCause() {
        // Given
        Throwable cause = new RuntimeException("WebSocket connection failed");
        ChatDeliveryException exception = new ChatDeliveryException("Chat delivery failed", cause);

        // When
        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleChatDelivery(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message delivery failed, but the message was saved", response.getBody().getError().getMessage());
    }
} 