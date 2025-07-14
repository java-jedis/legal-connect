package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
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

class CreateNoteDTOTest {

    private Validator validator;
    private UUID testCaseId;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testCaseId = UUID.randomUUID();
    }

    @Test
    void testAllArgsConstructor() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Test Note",
            "Test note content",
            AssetPrivacy.SHARED
        );

        assertEquals(testCaseId, dto.getCaseId());
        assertEquals("Test Note", dto.getTitle());
        assertEquals("Test note content", dto.getContent());
        assertEquals(AssetPrivacy.SHARED, dto.getPrivacy());
    }

    @Test
    void testSettersAndGetters() {
        CreateNoteDTO dto = new CreateNoteDTO(null, null, null, null);
        
        dto.setCaseId(testCaseId);
        dto.setTitle("Updated Title");
        dto.setContent("Updated content");
        dto.setPrivacy(AssetPrivacy.PRIVATE);

        assertEquals(testCaseId, dto.getCaseId());
        assertEquals("Updated Title", dto.getTitle());
        assertEquals("Updated content", dto.getContent());
        assertEquals(AssetPrivacy.PRIVATE, dto.getPrivacy());
    }

    @Test
    void testValidCreateNoteDTO() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            "Valid content",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    void testCaseIdValidation_Null() {
        CreateNoteDTO dto = new CreateNoteDTO(
            null,
            "Valid Title",
            "Valid content",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("caseId") && 
            v.getMessage().equals("Case ID is required")));
    }

    @ParameterizedTest
    @MethodSource("titleValidationTestData")
    void testTitleValidation(String title, boolean shouldHaveViolations, String expectedMessageFragment) {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            title,
            "Valid content",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        
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
    void testContentValidation_Null() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            null,
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testContentValidation_Blank() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            "",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testContentValidation_TooLong() {
        String longContent = "C".repeat(10001);
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            longContent,
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("content") && 
            v.getMessage().contains("between 1 and 10000")));
    }

    @Test
    void testContentValidation_BoundaryValues() {
        // Test minimum valid length (1 character)
        CreateNoteDTO dto1 = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            "A",
            AssetPrivacy.SHARED
        );
        Set<ConstraintViolation<CreateNoteDTO>> violations1 = validator.validate(dto1);
        assertTrue(violations1.stream().noneMatch(v -> v.getPropertyPath().toString().equals("content")));

        // Test maximum valid length
        String maxContent = "C".repeat(10000);
        CreateNoteDTO dto2 = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            maxContent,
            AssetPrivacy.SHARED
        );
        Set<ConstraintViolation<CreateNoteDTO>> violations2 = validator.validate(dto2);
        assertTrue(violations2.stream().noneMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testPrivacyValidation_Null() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Valid Title",
            "Valid content",
            null
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("privacy") && 
            v.getMessage().equals("Privacy setting is required")));
    }

    @Test
    void testPrivacyValidation_AllValues() {
        for (AssetPrivacy privacy : AssetPrivacy.values()) {
            CreateNoteDTO dto = new CreateNoteDTO(
                testCaseId,
                "Valid Title",
                "Valid content",
                privacy
            );

            Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("privacy")));
        }
    }

    @Test
    void testEqualsAndHashCode() {
        CreateNoteDTO dto1 = new CreateNoteDTO(
            testCaseId,
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );
        
        CreateNoteDTO dto2 = new CreateNoteDTO(
            testCaseId,
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );
        
        CreateNoteDTO dto3 = new CreateNoteDTO(
            UUID.randomUUID(),
            "Different Title",
            "Different content",
            AssetPrivacy.PRIVATE
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );

        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Title"));
        assertTrue(toString.contains("Test content"));
        assertTrue(toString.contains("SHARED"));
    }

    @Test
    void testSettersWithNullValues() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "Initial Title",
            "Initial content",
            AssetPrivacy.SHARED
        );

        dto.setCaseId(null);
        dto.setTitle(null);
        dto.setContent(null);
        dto.setPrivacy(null);

        assertNull(dto.getCaseId());
        assertNull(dto.getTitle());
        assertNull(dto.getContent());
        assertNull(dto.getPrivacy());
    }

    @Test
    void testMultipleValidationErrors() {
        CreateNoteDTO dto = new CreateNoteDTO(
            null,
            "",
            "",
            null
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertEquals(6, violations.size()); // caseId(1), title(2: @NotBlank + @Size), content(2: @NotBlank + @Size), privacy(1)
    }

    @Test
    void testMinimalValidNote() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "AB", // minimum title length
            "C",  // minimum content length
            AssetPrivacy.PRIVATE
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Should have no validation violations for minimal valid note");
    }

    @Test
    void testMaximalValidNote() {
        CreateNoteDTO dto = new CreateNoteDTO(
            testCaseId,
            "T".repeat(255), // maximum title length
            "C".repeat(10000), // maximum content length
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<CreateNoteDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Should have no validation violations for maximal valid note");
    }
} 