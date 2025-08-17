package com.javajedis.legalconnect.lawyerdirectory.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateLawyerReviewDTOTest {
    private Validator validator;
    private CreateLawyerReviewDTO dto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        dto = new CreateLawyerReviewDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(dto);
        assertNull(dto.getCaseId());
        assertEquals(0, dto.getRating());
        assertNull(dto.getReview());
    }

    @Test
    void testAllArgsConstructor() {
        UUID caseId = UUID.randomUUID();
        short rating = 4;
        String review = "Great lawyer!";
        CreateLawyerReviewDTO dto2 = new CreateLawyerReviewDTO(caseId, rating, review);
        assertEquals(caseId, dto2.getCaseId());
        assertEquals(rating, dto2.getRating());
        assertEquals(review, dto2.getReview());
    }

    @Test
    void testSettersAndGetters() {
        UUID caseId = UUID.randomUUID();
        short rating = 5;
        String review = "Excellent!";
        dto.setCaseId(caseId);
        dto.setRating(rating);
        dto.setReview(review);
        assertEquals(caseId, dto.getCaseId());
        assertEquals(rating, dto.getRating());
        assertEquals(review, dto.getReview());
    }

    @Test
    void testValidation() {
        dto.setCaseId(null);
        dto.setRating((short) 0);
        dto.setReview("A");
        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID caseId = UUID.randomUUID();
        short rating = 3;
        String review = "Good";
        CreateLawyerReviewDTO dto1 = new CreateLawyerReviewDTO(caseId, rating, review);
        CreateLawyerReviewDTO dto2 = new CreateLawyerReviewDTO(caseId, rating, review);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        dto.setReview("Test review");
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test review"));
    }
} 