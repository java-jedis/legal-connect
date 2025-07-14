package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UpdateDocumentDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDefaultConstructor() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO();
        assertNull(dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getPrivacy());
    }

    @Test
    void testAllArgsConstructor() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Test Document",
            "Test document description",
            AssetPrivacy.SHARED
        );

        assertEquals("Test Document", dto.getTitle());
        assertEquals("Test document description", dto.getDescription());
        assertEquals(AssetPrivacy.SHARED, dto.getPrivacy());
    }

    @Test
    void testSettersAndGetters() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO();
        
        dto.setTitle("Updated Title");
        dto.setDescription("Updated description");
        dto.setPrivacy(AssetPrivacy.PRIVATE);

        assertEquals("Updated Title", dto.getTitle());
        assertEquals("Updated description", dto.getDescription());
        assertEquals(AssetPrivacy.PRIVATE, dto.getPrivacy());
    }

    @Test
    void testValidUpdateDocumentDTO() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Valid Title",
            "Valid description",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @ParameterizedTest
    @MethodSource("titleValidationTestData")
    void testTitleValidation(String title, boolean shouldHaveViolations, String expectedMessageFragment) {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            title,
            "Valid description",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        
        if (shouldHaveViolations) {
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> 
                v.getPropertyPath().toString().equals("title") && 
                (expectedMessageFragment == null || v.getMessage().contains(expectedMessageFragment))));
        } else {
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("title")));
        }
    }

    static Stream<Arguments> titleValidationTestData() {
        return Stream.of(
            Arguments.of(null, true, null),
            Arguments.of("", true, null),
            Arguments.of("A", true, "between 2 and 255"),
            Arguments.of("T".repeat(256), true, "between 2 and 255"),
            Arguments.of("AB", false, null),
            Arguments.of("T".repeat(255), false, null)
        );
    }

    @Test
    void testDescriptionValidation_Null() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Valid Title",
            null,
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testDescriptionValidation_Blank() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Valid Title",
            "",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testDescriptionValidation_TooShort() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Valid Title",
            "A",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("description") && 
            v.getMessage().contains("between 2 and 10000")));
    }

    @Test
    void testDescriptionValidation_TooLong() {
        String longDescription = "D".repeat(10001);
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Valid Title",
            longDescription,
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("description") && 
            v.getMessage().contains("between 2 and 10000")));
    }

    @Test
    void testDescriptionValidation_BoundaryValues() {
        // Test minimum valid length
        UpdateDocumentDTO dto1 = new UpdateDocumentDTO(
            "Valid Title",
            "AB",
            AssetPrivacy.SHARED
        );
        Set<ConstraintViolation<UpdateDocumentDTO>> violations1 = validator.validate(dto1);
        assertTrue(violations1.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")));

        // Test maximum valid length
        String maxDescription = "D".repeat(10000);
        UpdateDocumentDTO dto2 = new UpdateDocumentDTO(
            "Valid Title",
            maxDescription,
            AssetPrivacy.SHARED
        );
        Set<ConstraintViolation<UpdateDocumentDTO>> violations2 = validator.validate(dto2);
        assertTrue(violations2.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testPrivacyValidation_Null() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Valid Title",
            "Valid description",
            null
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("privacy") && 
            v.getMessage().equals("Privacy setting is required")));
    }

    @Test
    void testPrivacyValidation_AllValues() {
        for (AssetPrivacy privacy : AssetPrivacy.values()) {
            UpdateDocumentDTO dto = new UpdateDocumentDTO(
                "Valid Title",
                "Valid description",
                privacy
            );

            Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("privacy")));
        }
    }

    @Test
    void testEqualsAndHashCode() {
        UpdateDocumentDTO dto1 = new UpdateDocumentDTO(
            "Test Title",
            "Test description",
            AssetPrivacy.SHARED
        );
        
        UpdateDocumentDTO dto2 = new UpdateDocumentDTO(
            "Test Title",
            "Test description",
            AssetPrivacy.SHARED
        );
        
        UpdateDocumentDTO dto3 = new UpdateDocumentDTO(
            "Different Title",
            "Different description",
            AssetPrivacy.PRIVATE
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Test Title",
            "Test description",
            AssetPrivacy.SHARED
        );

        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Title"));
        assertTrue(toString.contains("Test description"));
        assertTrue(toString.contains("SHARED"));
    }

    @Test
    void testSettersWithNullValues() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "Initial Title",
            "Initial description",
            AssetPrivacy.SHARED
        );

        dto.setTitle(null);
        dto.setDescription(null);
        dto.setPrivacy(null);

        assertNull(dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getPrivacy());
    }

    @Test
    void testSettersWithEmptyValues() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO();
        dto.setTitle("");
        dto.setDescription("");

        assertEquals("", dto.getTitle());
        assertEquals("", dto.getDescription());
    }

    @Test
    void testMultipleValidationErrors() {
        UpdateDocumentDTO dto = new UpdateDocumentDTO(
            "",
            "",
            null
        );

        Set<ConstraintViolation<UpdateDocumentDTO>> violations = validator.validate(dto);
        assertEquals(5, violations.size()); // title(2: @NotBlank + @Size), description(2: @NotBlank + @Size), privacy(1)
    }
} 