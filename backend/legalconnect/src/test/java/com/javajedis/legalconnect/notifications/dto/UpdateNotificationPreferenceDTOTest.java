package com.javajedis.legalconnect.notifications.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("UpdateNotificationPreferenceDTO Tests")
class UpdateNotificationPreferenceDTOTest {

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
        boolean emailEnabled = true;
        boolean webPushEnabled = false;

        // Act
        UpdateNotificationPreferenceDTO dto = new UpdateNotificationPreferenceDTO(emailEnabled, webPushEnabled);

        // Assert
        assertTrue(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        UpdateNotificationPreferenceDTO dto = new UpdateNotificationPreferenceDTO();

        // Assert
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        UpdateNotificationPreferenceDTO dto = new UpdateNotificationPreferenceDTO();

        // Act
        dto.setEmailEnabled(true);
        dto.setWebPushEnabled(true);

        // Assert
        assertTrue(dto.isEmailEnabled());
        assertTrue(dto.isWebPushEnabled());

        // Act - set to false
        dto.setEmailEnabled(false);
        dto.setWebPushEnabled(false);

        // Assert
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should pass validation with valid boolean values")
    void testValidationSuccess() {
        // Test all combinations of boolean values
        UpdateNotificationPreferenceDTO dto1 = new UpdateNotificationPreferenceDTO(true, true);
        UpdateNotificationPreferenceDTO dto2 = new UpdateNotificationPreferenceDTO(true, false);
        UpdateNotificationPreferenceDTO dto3 = new UpdateNotificationPreferenceDTO(false, true);
        UpdateNotificationPreferenceDTO dto4 = new UpdateNotificationPreferenceDTO(false, false);

        // Act & Assert
        assertTrue(validator.validate(dto1).isEmpty());
        assertTrue(validator.validate(dto2).isEmpty());
        assertTrue(validator.validate(dto3).isEmpty());
        assertTrue(validator.validate(dto4).isEmpty());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        UpdateNotificationPreferenceDTO dto1 = new UpdateNotificationPreferenceDTO(true, false);
        UpdateNotificationPreferenceDTO dto2 = new UpdateNotificationPreferenceDTO(true, false);
        UpdateNotificationPreferenceDTO dto3 = new UpdateNotificationPreferenceDTO(false, true);

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
        UpdateNotificationPreferenceDTO dto = new UpdateNotificationPreferenceDTO(true, false);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("UpdateNotificationPreferenceDTO"));
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("false"));
    }

    @Test
    @DisplayName("Should handle all boolean combinations")
    void testAllBooleanCombinations() {
        // Test case 1: both true
        UpdateNotificationPreferenceDTO dto1 = new UpdateNotificationPreferenceDTO(true, true);
        assertTrue(dto1.isEmailEnabled());
        assertTrue(dto1.isWebPushEnabled());

        // Test case 2: email true, webPush false
        UpdateNotificationPreferenceDTO dto2 = new UpdateNotificationPreferenceDTO(true, false);
        assertTrue(dto2.isEmailEnabled());
        assertFalse(dto2.isWebPushEnabled());

        // Test case 3: email false, webPush true
        UpdateNotificationPreferenceDTO dto3 = new UpdateNotificationPreferenceDTO(false, true);
        assertFalse(dto3.isEmailEnabled());
        assertTrue(dto3.isWebPushEnabled());

        // Test case 4: both false
        UpdateNotificationPreferenceDTO dto4 = new UpdateNotificationPreferenceDTO(false, false);
        assertFalse(dto4.isEmailEnabled());
        assertFalse(dto4.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle boolean field modifications")
    void testBooleanFieldModifications() {
        // Arrange
        UpdateNotificationPreferenceDTO dto = new UpdateNotificationPreferenceDTO();

        // Initially both should be false (default)
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());

        // Enable email only
        dto.setEmailEnabled(true);
        assertTrue(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());

        // Enable web push only
        dto.setEmailEnabled(false);
        dto.setWebPushEnabled(true);
        assertFalse(dto.isEmailEnabled());
        assertTrue(dto.isWebPushEnabled());

        // Enable both
        dto.setEmailEnabled(true);
        assertTrue(dto.isEmailEnabled());
        assertTrue(dto.isWebPushEnabled());

        // Disable both
        dto.setEmailEnabled(false);
        dto.setWebPushEnabled(false);
        assertFalse(dto.isEmailEnabled());
        assertFalse(dto.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should maintain state consistency")
    void testStateConsistency() {
        // Arrange
        UpdateNotificationPreferenceDTO dto = new UpdateNotificationPreferenceDTO();

        // Test multiple state changes
        for (int i = 0; i < 10; i++) {
            boolean emailState = i % 2 == 0;
            boolean webPushState = i % 3 == 0;

            dto.setEmailEnabled(emailState);
            dto.setWebPushEnabled(webPushState);

            assertEquals(emailState, dto.isEmailEnabled());
            assertEquals(webPushState, dto.isWebPushEnabled());
        }
    }

    @Test
    @DisplayName("Should handle equals with different boolean combinations")
    void testEqualsWithDifferentBooleanCombinations() {
        // Create DTOs with all possible boolean combinations
        UpdateNotificationPreferenceDTO dtoTrueTrue = new UpdateNotificationPreferenceDTO(true, true);
        UpdateNotificationPreferenceDTO dtoTrueFalse = new UpdateNotificationPreferenceDTO(true, false);
        UpdateNotificationPreferenceDTO dtoFalseTrue = new UpdateNotificationPreferenceDTO(false, true);
        UpdateNotificationPreferenceDTO dtoFalseFalse = new UpdateNotificationPreferenceDTO(false, false);

        // Create duplicates
        UpdateNotificationPreferenceDTO dtoTrueTrueDup = new UpdateNotificationPreferenceDTO(true, true);
        UpdateNotificationPreferenceDTO dtoTrueFalseDup = new UpdateNotificationPreferenceDTO(true, false);
        UpdateNotificationPreferenceDTO dtoFalseTrueDup = new UpdateNotificationPreferenceDTO(false, true);
        UpdateNotificationPreferenceDTO dtoFalseFalseDup = new UpdateNotificationPreferenceDTO(false, false);

        // Test equality with duplicates
        assertEquals(dtoTrueTrue, dtoTrueTrueDup);
        assertEquals(dtoTrueFalse, dtoTrueFalseDup);
        assertEquals(dtoFalseTrue, dtoFalseTrueDup);
        assertEquals(dtoFalseFalse, dtoFalseFalseDup);

        // Test inequality between different combinations
        assertNotEquals(dtoTrueTrue, dtoTrueFalse);
        assertNotEquals(dtoTrueTrue, dtoFalseTrue);
        assertNotEquals(dtoTrueTrue, dtoFalseFalse);
        assertNotEquals(dtoTrueFalse, dtoFalseTrue);
        assertNotEquals(dtoTrueFalse, dtoFalseFalse);
        assertNotEquals(dtoFalseTrue, dtoFalseFalse);
    }

    @Test
    @DisplayName("Should handle toString with different boolean combinations")
    void testToStringWithDifferentBooleanCombinations() {
        // Test all boolean combinations
        UpdateNotificationPreferenceDTO dtoTrueTrue = new UpdateNotificationPreferenceDTO(true, true);
        UpdateNotificationPreferenceDTO dtoTrueFalse = new UpdateNotificationPreferenceDTO(true, false);
        UpdateNotificationPreferenceDTO dtoFalseTrue = new UpdateNotificationPreferenceDTO(false, true);
        UpdateNotificationPreferenceDTO dtoFalseFalse = new UpdateNotificationPreferenceDTO(false, false);

        // All toString results should be different and contain the class name
        String strTrueTrue = dtoTrueTrue.toString();
        String strTrueFalse = dtoTrueFalse.toString();
        String strFalseTrue = dtoFalseTrue.toString();
        String strFalseFalse = dtoFalseFalse.toString();

        // All should contain class name
        assertTrue(strTrueTrue.contains("UpdateNotificationPreferenceDTO"));
        assertTrue(strTrueFalse.contains("UpdateNotificationPreferenceDTO"));
        assertTrue(strFalseTrue.contains("UpdateNotificationPreferenceDTO"));
        assertTrue(strFalseFalse.contains("UpdateNotificationPreferenceDTO"));

        // All should be different
        assertNotEquals(strTrueTrue, strTrueFalse);
        assertNotEquals(strTrueTrue, strFalseTrue);
        assertNotEquals(strTrueTrue, strFalseFalse);
        assertNotEquals(strTrueFalse, strFalseTrue);
        assertNotEquals(strTrueFalse, strFalseFalse);
        assertNotEquals(strFalseTrue, strFalseFalse);
    }
}