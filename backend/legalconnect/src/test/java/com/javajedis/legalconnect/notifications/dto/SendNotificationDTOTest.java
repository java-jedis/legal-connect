package com.javajedis.legalconnect.notifications.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("SendNotificationDTO Tests")
class SendNotificationDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        UUID receiverId = UUID.randomUUID();
        String content = "Test notification content";

        // Act
        SendNotificationDTO dto = new SendNotificationDTO(receiverId, content);

        // Assert
        assertEquals(receiverId, dto.getReceiverId());
        assertEquals(content, dto.getContent());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        SendNotificationDTO dto = new SendNotificationDTO();

        // Assert
        assertNull(dto.getReceiverId());
        assertNull(dto.getContent());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO();
        UUID receiverId = UUID.randomUUID();
        String content = "Updated content";

        // Act
        dto.setReceiverId(receiverId);
        dto.setContent(content);

        // Assert
        assertEquals(receiverId, dto.getReceiverId());
        assertEquals(content, dto.getContent());
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidationSuccess() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO(
            UUID.randomUUID(),
            "Valid notification content"
        );

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when receiver ID is null")
    void testValidationFailureNullReceiverId() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO(null, "Valid content");

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendNotificationDTO> violation = violations.iterator().next();
        assertEquals("Receiver ID is required", violation.getMessage());
        assertEquals("receiverId", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should fail validation when content is null")
    void testValidationFailureNullContent() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), null);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendNotificationDTO> violation = violations.iterator().next();
        assertEquals("Content is required", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should fail validation when content is blank")
    void testValidationFailureBlankContent() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), "   ");

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendNotificationDTO> violation = violations.iterator().next();
        assertEquals("Content is required", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should fail validation when content is empty")
    void testValidationFailureEmptyContent() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), "");

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 1);
        // Should have both @NotBlank and @Size violations
        boolean hasNotBlankViolation = violations.stream()
            .anyMatch(v -> "Content is required".equals(v.getMessage()));
        boolean hasSizeViolation = violations.stream()
            .anyMatch(v -> "Content must be between 1 and 1000 characters".equals(v.getMessage()));
        assertTrue(hasNotBlankViolation || hasSizeViolation);
    }

    @Test
    @DisplayName("Should fail validation when content exceeds maximum length")
    void testValidationFailureContentTooLong() {
        // Arrange
        String longContent = "a".repeat(1001); // 1001 characters
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), longContent);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendNotificationDTO> violation = violations.iterator().next();
        assertEquals("Content must be between 1 and 1000 characters", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should pass validation with content at maximum length")
    void testValidationSuccessContentAtMaxLength() {
        // Arrange
        String maxContent = "a".repeat(1000); // Exactly 1000 characters
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), maxContent);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation with content at minimum length")
    void testValidationSuccessContentAtMinLength() {
        // Arrange
        String minContent = "a"; // Exactly 1 character
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), minContent);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation with multiple constraint violations")
    void testValidationFailureMultipleViolations() {
        // Arrange
        SendNotificationDTO dto = new SendNotificationDTO(null, null);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
        
        boolean hasReceiverIdViolation = violations.stream()
            .anyMatch(v -> "Receiver ID is required".equals(v.getMessage()));
        boolean hasContentViolation = violations.stream()
            .anyMatch(v -> "Content is required".equals(v.getMessage()));
        
        assertTrue(hasReceiverIdViolation);
        assertTrue(hasContentViolation);
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        UUID receiverId = UUID.randomUUID();
        String content = "Test content";

        SendNotificationDTO dto1 = new SendNotificationDTO(receiverId, content);
        SendNotificationDTO dto2 = new SendNotificationDTO(receiverId, content);
        SendNotificationDTO dto3 = new SendNotificationDTO(UUID.randomUUID(), content);

        // Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals( null, dto1);
        assertNotEquals("string", dto1);
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        // Arrange
        UUID receiverId = UUID.randomUUID();
        String content = "Test content";
        SendNotificationDTO dto = new SendNotificationDTO(receiverId, content);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("SendNotificationDTO"));
        assertTrue(toString.contains(receiverId.toString()));
        assertTrue(toString.contains(content));
    }

    @Test
    @DisplayName("Should handle special characters in content")
    void testSpecialCharactersInContent() {
        // Arrange
        String specialContent = "Test with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), specialContent);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(specialContent, dto.getContent());
    }

    @Test
    @DisplayName("Should handle unicode characters in content")
    void testUnicodeCharactersInContent() {
        // Arrange
        String unicodeContent = "Test with unicode: 你好世界 🌍 ñáéíóú";
        SendNotificationDTO dto = new SendNotificationDTO(UUID.randomUUID(), unicodeContent);

        // Act
        Set<ConstraintViolation<SendNotificationDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(unicodeContent, dto.getContent());
    }
}