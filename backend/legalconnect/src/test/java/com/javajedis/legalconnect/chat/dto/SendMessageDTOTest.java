package com.javajedis.legalconnect.chat.dto;

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

@DisplayName("SendMessageDTO Tests")
class SendMessageDTOTest {

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
        String content = "Test message content";

        // Act
        SendMessageDTO dto = new SendMessageDTO(receiverId, content);

        // Assert
        assertEquals(receiverId, dto.getReceiverId());
        assertEquals(content, dto.getContent());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        SendMessageDTO dto = new SendMessageDTO();

        // Assert
        assertNull(dto.getReceiverId());
        assertNull(dto.getContent());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        SendMessageDTO dto = new SendMessageDTO();
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
        SendMessageDTO dto = new SendMessageDTO(
            UUID.randomUUID(),
            "Valid message content"
        );

        // Act
        Set<ConstraintViolation<SendMessageDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when receiver ID is null")
    void testValidationFailureNullReceiverId() {
        // Arrange
        SendMessageDTO dto = new SendMessageDTO(null, "Valid content");

        // Act
        Set<ConstraintViolation<SendMessageDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendMessageDTO> violation = violations.iterator().next();
        assertEquals("Receiver ID is required", violation.getMessage());
        assertEquals("receiverId", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should fail validation when content is null")
    void testValidationFailureNullContent() {
        // Arrange
        SendMessageDTO dto = new SendMessageDTO(UUID.randomUUID(), null);

        // Act
        Set<ConstraintViolation<SendMessageDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendMessageDTO> violation = violations.iterator().next();
        assertEquals("Content is required", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should fail validation when content is blank")
    void testValidationFailureBlankContent() {
        // Arrange
        SendMessageDTO dto = new SendMessageDTO(UUID.randomUUID(), "   ");

        // Act
        Set<ConstraintViolation<SendMessageDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendMessageDTO> violation = violations.iterator().next();
        assertEquals("Content is required", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should fail validation when content exceeds maximum length")
    void testValidationFailureContentTooLong() {
        // Arrange
        String longContent = "a".repeat(1001); // 1001 characters
        SendMessageDTO dto = new SendMessageDTO(UUID.randomUUID(), longContent);

        // Act
        Set<ConstraintViolation<SendMessageDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<SendMessageDTO> violation = violations.iterator().next();
        assertEquals("Content must be between 1 and 1000 characters", violation.getMessage());
        assertEquals("content", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should pass validation with content at maximum length")
    void testValidationSuccessContentAtMaxLength() {
        // Arrange
        String maxContent = "a".repeat(1000); // Exactly 1000 characters
        SendMessageDTO dto = new SendMessageDTO(UUID.randomUUID(), maxContent);

        // Act
        Set<ConstraintViolation<SendMessageDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        UUID receiverId = UUID.randomUUID();
        String content = "Test content";

        SendMessageDTO dto1 = new SendMessageDTO(receiverId, content);
        SendMessageDTO dto2 = new SendMessageDTO(receiverId, content);
        SendMessageDTO dto3 = new SendMessageDTO(UUID.randomUUID(), content);

        // Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals(null, dto1);
        assertNotEquals("string", dto1);
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        // Arrange
        UUID receiverId = UUID.randomUUID();
        String content = "Test content";
        SendMessageDTO dto = new SendMessageDTO(receiverId, content);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("SendMessageDTO"));
        assertTrue(toString.contains(receiverId.toString()));
        assertTrue(toString.contains(content));
    }
}