package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UpdateNoteDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDefaultConstructor() {
        UpdateNoteDTO dto = new UpdateNoteDTO();
        
        assertNotNull(dto);
        assertNull(dto.getTitle());
        assertNull(dto.getContent());
        assertNull(dto.getPrivacy());
    }

    @Test
    void testAllArgsConstructor() {
        UpdateNoteDTO dto = new UpdateNoteDTO(
            "Test Note",
            "Test note content",
            AssetPrivacy.SHARED
        );

        assertEquals("Test Note", dto.getTitle());
        assertEquals("Test note content", dto.getContent());
        assertEquals(AssetPrivacy.SHARED, dto.getPrivacy());
    }

    @Test
    void testSettersAndGetters() {
        UpdateNoteDTO dto = new UpdateNoteDTO();
        
        dto.setTitle("Updated Title");
        dto.setContent("Updated content");
        dto.setPrivacy(AssetPrivacy.PRIVATE);

        assertEquals("Updated Title", dto.getTitle());
        assertEquals("Updated content", dto.getContent());
        assertEquals(AssetPrivacy.PRIVATE, dto.getPrivacy());
    }

    @Test
    void testValidUpdateNoteDTO() {
        UpdateNoteDTO dto = new UpdateNoteDTO(
            "Valid Title",
            "Valid content",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateNoteDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    void testTitleValidation_Null() {
        UpdateNoteDTO dto = new UpdateNoteDTO(
            null,
            "Valid content",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTitleValidation_Blank() {
        UpdateNoteDTO dto = new UpdateNoteDTO(
            "",
            "Valid content",
            AssetPrivacy.SHARED
        );

        Set<ConstraintViolation<UpdateNoteDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testEqualsAndHashCode() {
        UpdateNoteDTO dto1 = new UpdateNoteDTO(
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );
        
        UpdateNoteDTO dto2 = new UpdateNoteDTO(
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );
        
        UpdateNoteDTO dto3 = new UpdateNoteDTO(
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
        UpdateNoteDTO dto = new UpdateNoteDTO(
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
} 