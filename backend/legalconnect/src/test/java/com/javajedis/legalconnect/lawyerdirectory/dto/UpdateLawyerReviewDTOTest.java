package com.javajedis.legalconnect.lawyerdirectory.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateLawyerReviewDTOTest {
    private Validator validator;
    private UpdateLawyerReviewDTO dto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        dto = new UpdateLawyerReviewDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(dto);
        assertEquals(0, dto.getRating());
        assertNull(dto.getReview());
    }

    @Test
    void testSettersAndGetters() {
        short rating = 5;
        String review = "Updated review!";
        dto.setRating(rating);
        dto.setReview(review);
        assertEquals(rating, dto.getRating());
        assertEquals(review, dto.getReview());
    }

    @Test
    void testValidation() {
        dto.setRating((short) 0);
        dto.setReview("A");
        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        short rating = 4;
        String review = "Nice";
        UpdateLawyerReviewDTO dto1 = new UpdateLawyerReviewDTO();
        dto1.setRating(rating);
        dto1.setReview(review);
        UpdateLawyerReviewDTO dto2 = new UpdateLawyerReviewDTO();
        dto2.setRating(rating);
        dto2.setReview(review);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        dto.setReview("Test update");
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test update"));
    }
} 