package com.javajedis.legalconnect.common.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

/**
 * Unit tests for EmailService.
 * Tests email sending functionality including validation, simple emails, and template emails.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String NULL_EMAIL = null;
    private static final String EMPTY_EMAIL = "";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_BODY = "Test email body";
    private static final String TEST_TEMPLATE = "test-template";
    private static final String FROM_EMAIL = "noreply@legalconnect.live";

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Initialize EmailService with mocked dependencies
        emailService = new EmailService(mailSender, templateEngine, FROM_EMAIL);
    }

    @Test
    @DisplayName("Should send simple email with valid email address")
    void shouldSendSimpleEmailWithValidEmail() {
        // When
        assertDoesNotThrow(() -> emailService.sendSimpleEmail(VALID_EMAIL, TEST_SUBJECT, TEST_BODY));

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not send simple email with invalid email address")
    void shouldNotSendSimpleEmailWithInvalidEmail() {
        // When
        emailService.sendSimpleEmail(INVALID_EMAIL, TEST_SUBJECT, TEST_BODY);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not send simple email with null email address")
    void shouldNotSendSimpleEmailWithNullEmail() {
        // When
        emailService.sendSimpleEmail(NULL_EMAIL, TEST_SUBJECT, TEST_BODY);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not send simple email with empty email address")
    void shouldNotSendSimpleEmailWithEmptyEmail() {
        // When
        emailService.sendSimpleEmail(EMPTY_EMAIL, TEST_SUBJECT, TEST_BODY);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send template email with valid email address")
    void shouldSendTemplateEmailWithValidEmail() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");
        variables.put("otp", "123456");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEST_TEMPLATE), any(Context.class))).thenReturn("<html>Test content</html>");

        // When
        assertDoesNotThrow(() -> emailService.sendTemplateEmail(VALID_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables));

        // Then
        verify(templateEngine, times(1)).process(eq(TEST_TEMPLATE), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send template email with null variables")
    void shouldSendTemplateEmailWithNullVariables() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEST_TEMPLATE), any(Context.class))).thenReturn("<html>Test content</html>");

        // When
        assertDoesNotThrow(() -> emailService.sendTemplateEmail(VALID_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, null));

        // Then
        verify(templateEngine, times(1)).process(eq(TEST_TEMPLATE), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should not send template email with invalid email address")
    void shouldNotSendTemplateEmailWithInvalidEmail() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");

        // When
        emailService.sendTemplateEmail(INVALID_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should not send template email with null email address")
    void shouldNotSendTemplateEmailWithNullEmail() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");

        // When
        emailService.sendTemplateEmail(NULL_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should not send template email with empty email address")
    void shouldNotSendTemplateEmailWithEmptyEmail() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");

        // When
        emailService.sendTemplateEmail(EMPTY_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should handle MailException in template email")
    void shouldHandleMailExceptionInTemplateEmail() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEST_TEMPLATE), any(Context.class))).thenReturn("<html>Test content</html>");
        doThrow(new org.springframework.mail.MailException("Mail server error") {}).when(mailSender).send(any(MimeMessage.class));

        // When & Then
        assertDoesNotThrow(() -> emailService.sendTemplateEmail(VALID_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables));
    }

    @Test
    @DisplayName("Should validate various email formats")
    void shouldValidateVariousEmailFormats() {
        // Given
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user123@example.co.uk",
            "user@subdomain.example.com"
        };

        String[] invalidEmails = {
            "invalid-email",
            "@example.com",
            "user@",
            "user.example.com",
            "",
            null
        };

        // When & Then
        for (String email : validEmails) {
            emailService.sendSimpleEmail(email, TEST_SUBJECT, TEST_BODY);
        }

        for (String email : invalidEmails) {
            emailService.sendSimpleEmail(email, TEST_SUBJECT, TEST_BODY);
        }

        // Verify that send was called only for valid emails
        verify(mailSender, times(validEmails.length)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle empty template content")
    void shouldHandleEmptyTemplateContent() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEST_TEMPLATE), any(Context.class))).thenReturn("");

        // When
        assertDoesNotThrow(() -> emailService.sendTemplateEmail(VALID_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables));

        // Then
        verify(templateEngine, times(1)).process(eq(TEST_TEMPLATE), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should handle null template content")
    void shouldHandleNullTemplateContent() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEST_TEMPLATE), any(Context.class))).thenReturn(null);

        // When
        assertDoesNotThrow(() -> emailService.sendTemplateEmail(VALID_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, variables));

        // Then
        verify(templateEngine, times(1)).process(eq(TEST_TEMPLATE), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
} 