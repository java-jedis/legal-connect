package com.javajedis.legalconnect.scheduling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateOAuthCalendarTokenDTOTest {

    private Validator validator;
    private CreateOAuthCalendarTokenDTO createOAuthCalendarTokenDTO;
    private UUID testUserId;
    private OffsetDateTime testAccessExpiry;
    private OffsetDateTime testRefreshExpiry;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        createOAuthCalendarTokenDTO = new CreateOAuthCalendarTokenDTO();
        testUserId = UUID.randomUUID();
        testAccessExpiry = OffsetDateTime.now().plusHours(1);
        testRefreshExpiry = OffsetDateTime.now().plusDays(30);
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(createOAuthCalendarTokenDTO);
        assertNull(createOAuthCalendarTokenDTO.getUserId());
        assertNull(createOAuthCalendarTokenDTO.getAccessToken());
        assertNull(createOAuthCalendarTokenDTO.getRefreshToken());
        assertNull(createOAuthCalendarTokenDTO.getAccessExpiry());
        assertNull(createOAuthCalendarTokenDTO.getRefreshExpiry());
    }

    @Test
    void testSettersAndGetters() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        assertEquals(testUserId, createOAuthCalendarTokenDTO.getUserId());
        assertEquals("test_access_token", createOAuthCalendarTokenDTO.getAccessToken());
        assertEquals("test_refresh_token", createOAuthCalendarTokenDTO.getRefreshToken());
        assertEquals(testAccessExpiry, createOAuthCalendarTokenDTO.getAccessExpiry());
        assertEquals(testRefreshExpiry, createOAuthCalendarTokenDTO.getRefreshExpiry());
    }

    @Test
    void testValidCreateOAuthCalendarTokenDTO() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("valid_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("valid_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    void testUserIdValidation_Null() {
        createOAuthCalendarTokenDTO.setUserId(null);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("userId") && 
            v.getMessage().equals("User ID is required")));
    }

    @Test
    void testAccessTokenValidation_Null() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken(null);
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("accessToken") && 
            v.getMessage().equals("Access token is required")));
    }

    @Test
    void testAccessTokenValidation_Empty() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("");
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("accessToken") && 
            v.getMessage().equals("Access token is required")));
    }

    @Test
    void testRefreshTokenValidation_Null() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken(null);
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("refreshToken") && 
            v.getMessage().equals("Refresh token is required")));
    }

    @Test
    void testRefreshTokenValidation_Empty() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("refreshToken") && 
            v.getMessage().equals("Refresh token is required")));
    }

    @Test
    void testAccessExpiryValidation_Null() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(null);
        createOAuthCalendarTokenDTO.setRefreshExpiry(testRefreshExpiry);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("accessExpiry") && 
            v.getMessage().equals("Access token expiry is required")));
    }

    @Test
    void testRefreshExpiryValidation_Null() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");
        createOAuthCalendarTokenDTO.setAccessExpiry(testAccessExpiry);
        createOAuthCalendarTokenDTO.setRefreshExpiry(null);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("refreshExpiry") && 
            v.getMessage().equals("Refresh token expiry is required")));
    }

    @Test
    void testEqualsAndHashCode() {
        CreateOAuthCalendarTokenDTO dto1 = new CreateOAuthCalendarTokenDTO();
        dto1.setUserId(testUserId);
        dto1.setAccessToken("test_access_token");
        dto1.setRefreshToken("test_refresh_token");
        dto1.setAccessExpiry(testAccessExpiry);
        dto1.setRefreshExpiry(testRefreshExpiry);
        
        CreateOAuthCalendarTokenDTO dto2 = new CreateOAuthCalendarTokenDTO();
        dto2.setUserId(testUserId);
        dto2.setAccessToken("test_access_token");
        dto2.setRefreshToken("test_refresh_token");
        dto2.setAccessExpiry(testAccessExpiry);
        dto2.setRefreshExpiry(testRefreshExpiry);
        
        CreateOAuthCalendarTokenDTO dto3 = new CreateOAuthCalendarTokenDTO();
        dto3.setUserId(UUID.randomUUID());
        dto3.setAccessToken("different_access_token");
        dto3.setRefreshToken("different_refresh_token");
        dto3.setAccessExpiry(testAccessExpiry.plusHours(1));
        dto3.setRefreshExpiry(testRefreshExpiry.plusDays(1));

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        createOAuthCalendarTokenDTO.setUserId(testUserId);
        createOAuthCalendarTokenDTO.setAccessToken("test_access_token");
        createOAuthCalendarTokenDTO.setRefreshToken("test_refresh_token");

        String toString = createOAuthCalendarTokenDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(testUserId.toString()));
        // Note: We should NOT expose token values in toString for security
        // This test just verifies toString doesn't crash
    }

    @Test
    void testSettersWithNullValues() {
        createOAuthCalendarTokenDTO.setUserId(null);
        createOAuthCalendarTokenDTO.setAccessToken(null);
        createOAuthCalendarTokenDTO.setRefreshToken(null);
        createOAuthCalendarTokenDTO.setAccessExpiry(null);
        createOAuthCalendarTokenDTO.setRefreshExpiry(null);

        assertNull(createOAuthCalendarTokenDTO.getUserId());
        assertNull(createOAuthCalendarTokenDTO.getAccessToken());
        assertNull(createOAuthCalendarTokenDTO.getRefreshToken());
        assertNull(createOAuthCalendarTokenDTO.getAccessExpiry());
        assertNull(createOAuthCalendarTokenDTO.getRefreshExpiry());
    }

    @Test
    void testSettersWithEmptyTokenValues() {
        createOAuthCalendarTokenDTO.setAccessToken("");
        createOAuthCalendarTokenDTO.setRefreshToken("");

        assertEquals("", createOAuthCalendarTokenDTO.getAccessToken());
        assertEquals("", createOAuthCalendarTokenDTO.getRefreshToken());
    }

    @Test
    void testTokenExpiryTimes() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime futureTime = now.plusHours(2);
        OffsetDateTime pastTime = now.minusHours(1);

        // Test with future expiry times (normal case)
        createOAuthCalendarTokenDTO.setAccessExpiry(futureTime);
        createOAuthCalendarTokenDTO.setRefreshExpiry(futureTime.plusDays(7));

        assertEquals(futureTime, createOAuthCalendarTokenDTO.getAccessExpiry());
        assertEquals(futureTime.plusDays(7), createOAuthCalendarTokenDTO.getRefreshExpiry());

        // Test with past expiry times (expired tokens)
        createOAuthCalendarTokenDTO.setAccessExpiry(pastTime);
        createOAuthCalendarTokenDTO.setRefreshExpiry(pastTime);

        assertEquals(pastTime, createOAuthCalendarTokenDTO.getAccessExpiry());
        assertEquals(pastTime, createOAuthCalendarTokenDTO.getRefreshExpiry());
    }

    @Test
    void testLongTokenValues() {
        String longAccessToken = "a".repeat(1000);
        String longRefreshToken = "r".repeat(1000);

        createOAuthCalendarTokenDTO.setAccessToken(longAccessToken);
        createOAuthCalendarTokenDTO.setRefreshToken(longRefreshToken);

        assertEquals(longAccessToken, createOAuthCalendarTokenDTO.getAccessToken());
        assertEquals(longRefreshToken, createOAuthCalendarTokenDTO.getRefreshToken());
    }

    @Test
    void testMultipleValidationErrors() {
        // Set all fields to invalid values
        createOAuthCalendarTokenDTO.setUserId(null);
        createOAuthCalendarTokenDTO.setAccessToken(null);
        createOAuthCalendarTokenDTO.setRefreshToken(null);
        createOAuthCalendarTokenDTO.setAccessExpiry(null);
        createOAuthCalendarTokenDTO.setRefreshExpiry(null);

        Set<ConstraintViolation<CreateOAuthCalendarTokenDTO>> violations = validator.validate(createOAuthCalendarTokenDTO);
        
        // Should have 5 validation errors (one for each required field)
        assertEquals(5, violations.size());
        
        // Verify each field has a validation error
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accessToken")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("refreshToken")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("accessExpiry")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("refreshExpiry")));
    }
} 