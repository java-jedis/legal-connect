package com.javajedis.legalconnect.videocall.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("ScheduleMeetingDTO Tests")
class ScheduleMeetingDTOTest {

    private Validator validator;
    private ScheduleMeetingDTO scheduleMeetingDTO;
    private String testEmail;
    private String testTitle;
    private OffsetDateTime testStartDateTime;
    private OffsetDateTime testEndDateTime;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        scheduleMeetingDTO = new ScheduleMeetingDTO();
        testEmail = "client@example.com";
        testTitle = "Legal Consultation";
        testStartDateTime = OffsetDateTime.now().plusDays(1);
        testEndDateTime = OffsetDateTime.now().plusDays(1).plusHours(1);
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        ScheduleMeetingDTO dto = new ScheduleMeetingDTO();
        assertNotNull(dto);
        assertNull(dto.getEmail());
        assertNull(dto.getTitle());
        assertNull(dto.getStartDateTime());
        assertNull(dto.getEndDateTime());
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        ScheduleMeetingDTO dto = new ScheduleMeetingDTO(
            testEmail, testTitle, testStartDateTime, testEndDateTime
        );
        
        assertEquals(testEmail, dto.getEmail());
        assertEquals(testTitle, dto.getTitle());
        assertEquals(testStartDateTime, dto.getStartDateTime());
        assertEquals(testEndDateTime, dto.getEndDateTime());
    }

    @Test
    @DisplayName("Should set and get email")
    void testEmailGetterSetter() {
        scheduleMeetingDTO.setEmail(testEmail);
        assertEquals(testEmail, scheduleMeetingDTO.getEmail());
    }

    @Test
    @DisplayName("Should set and get title")
    void testTitleGetterSetter() {
        scheduleMeetingDTO.setTitle(testTitle);
        assertEquals(testTitle, scheduleMeetingDTO.getTitle());
    }

    @Test
    @DisplayName("Should set and get start date time")
    void testStartDateTimeGetterSetter() {
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        assertEquals(testStartDateTime, scheduleMeetingDTO.getStartDateTime());
    }

    @Test
    @DisplayName("Should set and get end date time")
    void testEndDateTimeGetterSetter() {
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);
        assertEquals(testEndDateTime, scheduleMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidData_NoViolations() {
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with valid data");
    }

    @Test
    @DisplayName("Should fail validation when email is null")
    void testNullEmail_ValidationError() {
        scheduleMeetingDTO.setEmail(null);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") && 
            v.getMessage().equals("Client email is required")));
    }

    @Test
    @DisplayName("Should fail validation when title is null")
    void testNullTitle_ValidationError() {
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(null);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().equals("Meeting title is required")));
    }

    @Test
    @DisplayName("Should fail validation when start date time is null")
    void testNullStartDateTime_ValidationError() {
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(null);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDateTime") && 
            v.getMessage().equals("Start datetime is required")));
    }

    @Test
    @DisplayName("Should fail validation when end date time is null")
    void testNullEndDateTime_ValidationError() {
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(null);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endDateTime") && 
            v.getMessage().equals("End datetime is required")));
    }

    @Test
    @DisplayName("Should fail validation when all required fields are null")
    void testAllNullFields_ValidationErrors() {
        scheduleMeetingDTO.setEmail(null);
        scheduleMeetingDTO.setTitle(null);
        scheduleMeetingDTO.setStartDateTime(null);
        scheduleMeetingDTO.setEndDateTime(null);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        assertEquals(4, violations.size());
        
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") && 
            v.getMessage().equals("Client email is required")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().equals("Meeting title is required")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDateTime") && 
            v.getMessage().equals("Start datetime is required")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endDateTime") && 
            v.getMessage().equals("End datetime is required")));
    }

    @Test
    @DisplayName("Should handle different email formats")
    void testDifferentEmailFormats() {
        String[] emails = {
            "user@example.com",
            "test.email@domain.co.uk",
            "user+tag@example.org",
            "firstname.lastname@company.com",
            "user123@test-domain.com"
        };

        for (String email : emails) {
            scheduleMeetingDTO.setEmail(email);
            scheduleMeetingDTO.setTitle(testTitle);
            scheduleMeetingDTO.setStartDateTime(testStartDateTime);
            scheduleMeetingDTO.setEndDateTime(testEndDateTime);

            Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
            assertTrue(violations.isEmpty(), "Should have no violations for email: " + email);
        }
    }

    @Test
    @DisplayName("Should handle different title values")
    void testDifferentTitleValues() {
        String[] titles = {
            "Legal Consultation",
            "Contract Review",
            "A",
            "Very Long Meeting Title With Many Words That Could Be Used In Real Scenarios",
            "Meeting with Special Characters: @#$%^&*()",
            "Meeting 123 - Follow-up Discussion"
        };

        for (String title : titles) {
            scheduleMeetingDTO.setEmail(testEmail);
            scheduleMeetingDTO.setTitle(title);
            scheduleMeetingDTO.setStartDateTime(testStartDateTime);
            scheduleMeetingDTO.setEndDateTime(testEndDateTime);

            Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
            assertTrue(violations.isEmpty(), "Should have no violations for title: " + title);
        }
    }

    @Test
    @DisplayName("Should handle different datetime values")
    void testDifferentDateTimeValues() {
        OffsetDateTime[] startTimes = {
            OffsetDateTime.now().plusMinutes(30),
            OffsetDateTime.now().plusHours(2),
            OffsetDateTime.now().plusDays(1),
            OffsetDateTime.now().plusWeeks(1),
            OffsetDateTime.now().plusMonths(1)
        };

        for (OffsetDateTime startTime : startTimes) {
            OffsetDateTime endTime = startTime.plusHours(1);
            
            scheduleMeetingDTO.setEmail(testEmail);
            scheduleMeetingDTO.setTitle(testTitle);
            scheduleMeetingDTO.setStartDateTime(startTime);
            scheduleMeetingDTO.setEndDateTime(endTime);

            Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
            assertTrue(violations.isEmpty(), "Should have no violations for start time: " + startTime);
        }
    }

    @Test
    @DisplayName("Should handle empty string values for optional validation")
    void testEmptyStringValues() {
        scheduleMeetingDTO.setEmail("");
        scheduleMeetingDTO.setTitle("");
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        
        // Empty strings should pass @NotNull validation but might fail other validations
        // Since we only have @NotNull, empty strings should be valid
        assertTrue(violations.isEmpty(), "Empty strings should pass @NotNull validation");
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void testDataIntegrity() {
        // Set initial values
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        // Verify initial values
        assertEquals(testEmail, scheduleMeetingDTO.getEmail());
        assertEquals(testTitle, scheduleMeetingDTO.getTitle());
        assertEquals(testStartDateTime, scheduleMeetingDTO.getStartDateTime());
        assertEquals(testEndDateTime, scheduleMeetingDTO.getEndDateTime());

        // Change values
        String newEmail = "newemail@example.com";
        String newTitle = "Updated Meeting Title";
        OffsetDateTime newStartDateTime = OffsetDateTime.now().plusDays(2);
        OffsetDateTime newEndDateTime = OffsetDateTime.now().plusDays(2).plusHours(2);

        scheduleMeetingDTO.setEmail(newEmail);
        scheduleMeetingDTO.setTitle(newTitle);
        scheduleMeetingDTO.setStartDateTime(newStartDateTime);
        scheduleMeetingDTO.setEndDateTime(newEndDateTime);

        // Verify new values
        assertEquals(newEmail, scheduleMeetingDTO.getEmail());
        assertEquals(newTitle, scheduleMeetingDTO.getTitle());
        assertEquals(newStartDateTime, scheduleMeetingDTO.getStartDateTime());
        assertEquals(newEndDateTime, scheduleMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        ScheduleMeetingDTO dto1 = new ScheduleMeetingDTO(
            testEmail, testTitle, testStartDateTime, testEndDateTime
        );
        ScheduleMeetingDTO dto2 = new ScheduleMeetingDTO(
            testEmail, testTitle, testStartDateTime, testEndDateTime
        );
        ScheduleMeetingDTO dto3 = new ScheduleMeetingDTO(
            "different@example.com", testTitle, testStartDateTime, testEndDateTime
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        String toString = scheduleMeetingDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ScheduleMeetingDTO"));
        assertTrue(toString.contains(testEmail));
        assertTrue(toString.contains(testTitle));
    }

    @Test
    @DisplayName("Should support data transfer functionality")
    void testDataTransferBehavior() {
        // Simulate data transfer by setting all fields and verifying they're preserved
        ScheduleMeetingDTO sourceDTO = new ScheduleMeetingDTO();
        sourceDTO.setEmail(testEmail);
        sourceDTO.setTitle(testTitle);
        sourceDTO.setStartDateTime(testStartDateTime);
        sourceDTO.setEndDateTime(testEndDateTime);

        // Create target DTO using all-args constructor (simulating mapping)
        ScheduleMeetingDTO targetDTO = new ScheduleMeetingDTO(
            sourceDTO.getEmail(),
            sourceDTO.getTitle(),
            sourceDTO.getStartDateTime(),
            sourceDTO.getEndDateTime()
        );

        // Verify all data was transferred correctly
        assertEquals(sourceDTO.getEmail(), targetDTO.getEmail());
        assertEquals(sourceDTO.getTitle(), targetDTO.getTitle());
        assertEquals(sourceDTO.getStartDateTime(), targetDTO.getStartDateTime());
        assertEquals(sourceDTO.getEndDateTime(), targetDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should handle field mapping consistency")
    void testFieldMappingConsistency() {
        // Test that all fields can be set and retrieved consistently
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);

        // Verify all fields maintain their values
        assertEquals(testEmail, scheduleMeetingDTO.getEmail());
        assertEquals(testTitle, scheduleMeetingDTO.getTitle());
        assertEquals(testStartDateTime, scheduleMeetingDTO.getStartDateTime());
        assertEquals(testEndDateTime, scheduleMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should handle edge case datetime scenarios")
    void testDateTimeEdgeCases() {
        // Test with same start and end time
        OffsetDateTime sameTime = OffsetDateTime.now().plusDays(1);
        
        scheduleMeetingDTO.setEmail(testEmail);
        scheduleMeetingDTO.setTitle(testTitle);
        scheduleMeetingDTO.setStartDateTime(sameTime);
        scheduleMeetingDTO.setEndDateTime(sameTime);

        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        assertTrue(violations.isEmpty(), "Should pass validation even with same start and end time");

        // Test with end time before start time (validation doesn't check this at DTO level)
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.minusHours(1);
        
        scheduleMeetingDTO.setStartDateTime(startTime);
        scheduleMeetingDTO.setEndDateTime(endTime);

        violations = validator.validate(scheduleMeetingDTO);
        assertTrue(violations.isEmpty(), "DTO validation should not check time logic");
    }

    @Test
    @DisplayName("Should handle validation with partial data")
    void testPartialDataValidation() {
        // Test with only email set
        scheduleMeetingDTO.setEmail(testEmail);
        Set<ConstraintViolation<ScheduleMeetingDTO>> violations = validator.validate(scheduleMeetingDTO);
        assertEquals(3, violations.size()); // title, startDateTime, endDateTime are null

        // Test with email and title set
        scheduleMeetingDTO.setTitle(testTitle);
        violations = validator.validate(scheduleMeetingDTO);
        assertEquals(2, violations.size()); // startDateTime, endDateTime are null

        // Test with email, title, and startDateTime set
        scheduleMeetingDTO.setStartDateTime(testStartDateTime);
        violations = validator.validate(scheduleMeetingDTO);
        assertEquals(1, violations.size()); // endDateTime is null

        // Test with all fields set
        scheduleMeetingDTO.setEndDateTime(testEndDateTime);
        violations = validator.validate(scheduleMeetingDTO);
        assertTrue(violations.isEmpty()); // All required fields are set
    }
}