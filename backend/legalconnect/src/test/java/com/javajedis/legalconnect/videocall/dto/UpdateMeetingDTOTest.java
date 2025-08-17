package com.javajedis.legalconnect.videocall.dto;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("UpdateMeetingDTO Tests")
class UpdateMeetingDTOTest {

    private Validator validator;
    private UpdateMeetingDTO updateMeetingDTO;
    private UUID testMeetingId;
    private OffsetDateTime testStartDateTime;
    private OffsetDateTime testEndDateTime;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        updateMeetingDTO = new UpdateMeetingDTO();
        testMeetingId = UUID.randomUUID();
        testStartDateTime = OffsetDateTime.now().plusDays(1);
        testEndDateTime = OffsetDateTime.now().plusDays(1).plusHours(1);
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        UpdateMeetingDTO dto = new UpdateMeetingDTO();
        assertNotNull(dto);
        assertNull(dto.getMeetingId());
        assertNull(dto.getStartDateTime());
        assertNull(dto.getEndDateTime());
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        UpdateMeetingDTO dto = new UpdateMeetingDTO(
            testMeetingId, testStartDateTime, testEndDateTime
        );
        
        assertEquals(testMeetingId, dto.getMeetingId());
        assertEquals(testStartDateTime, dto.getStartDateTime());
        assertEquals(testEndDateTime, dto.getEndDateTime());
    }

    @Test
    @DisplayName("Should set and get meeting ID")
    void testMeetingIdGetterSetter() {
        updateMeetingDTO.setMeetingId(testMeetingId);
        assertEquals(testMeetingId, updateMeetingDTO.getMeetingId());
    }

    @Test
    @DisplayName("Should set and get start date time")
    void testStartDateTimeGetterSetter() {
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        assertEquals(testStartDateTime, updateMeetingDTO.getStartDateTime());
    }

    @Test
    @DisplayName("Should set and get end date time")
    void testEndDateTimeGetterSetter() {
        updateMeetingDTO.setEndDateTime(testEndDateTime);
        assertEquals(testEndDateTime, updateMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidData_NoViolations() {
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with valid data");
    }

    @Test
    @DisplayName("Should fail validation when meeting ID is null")
    void testNullMeetingId_ValidationError() {
        updateMeetingDTO.setMeetingId(null);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("meetingId") && 
            v.getMessage().equals("Meeting ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when start date time is null")
    void testNullStartDateTime_ValidationError() {
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(null);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDateTime") && 
            v.getMessage().equals("Start datetime is required")));
    }

    @Test
    @DisplayName("Should fail validation when end date time is null")
    void testNullEndDateTime_ValidationError() {
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(null);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endDateTime") && 
            v.getMessage().equals("End datetime is required")));
    }

    @Test
    @DisplayName("Should fail validation when all required fields are null")
    void testAllNullFields_ValidationErrors() {
        updateMeetingDTO.setMeetingId(null);
        updateMeetingDTO.setStartDateTime(null);
        updateMeetingDTO.setEndDateTime(null);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        
        assertEquals(3, violations.size());
        
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("meetingId") && 
            v.getMessage().equals("Meeting ID is required")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDateTime") && 
            v.getMessage().equals("Start datetime is required")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endDateTime") && 
            v.getMessage().equals("End datetime is required")));
    }

    @Test
    @DisplayName("Should handle different UUID values")
    void testDifferentUUIDValues() {
        UUID[] uuids = {
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new UUID(0L, 0L),
            new UUID(Long.MAX_VALUE, Long.MAX_VALUE),
            new UUID(Long.MIN_VALUE, Long.MIN_VALUE)
        };

        for (UUID uuid : uuids) {
            updateMeetingDTO.setMeetingId(uuid);
            updateMeetingDTO.setStartDateTime(testStartDateTime);
            updateMeetingDTO.setEndDateTime(testEndDateTime);

            Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
            assertTrue(violations.isEmpty(), "Should have no violations for UUID: " + uuid);
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
            OffsetDateTime.now().plusMonths(1),
            OffsetDateTime.now().minusDays(1), // Past date
            OffsetDateTime.now().plusYears(1)  // Far future
        };

        for (OffsetDateTime startTime : startTimes) {
            OffsetDateTime endTime = startTime.plusHours(1);
            
            updateMeetingDTO.setMeetingId(testMeetingId);
            updateMeetingDTO.setStartDateTime(startTime);
            updateMeetingDTO.setEndDateTime(endTime);

            Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
            assertTrue(violations.isEmpty(), "Should have no violations for start time: " + startTime);
        }
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void testDataIntegrity() {
        // Set initial values
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        // Verify initial values
        assertEquals(testMeetingId, updateMeetingDTO.getMeetingId());
        assertEquals(testStartDateTime, updateMeetingDTO.getStartDateTime());
        assertEquals(testEndDateTime, updateMeetingDTO.getEndDateTime());

        // Change values
        UUID newMeetingId = UUID.randomUUID();
        OffsetDateTime newStartDateTime = OffsetDateTime.now().plusDays(2);
        OffsetDateTime newEndDateTime = OffsetDateTime.now().plusDays(2).plusHours(2);

        updateMeetingDTO.setMeetingId(newMeetingId);
        updateMeetingDTO.setStartDateTime(newStartDateTime);
        updateMeetingDTO.setEndDateTime(newEndDateTime);

        // Verify new values
        assertEquals(newMeetingId, updateMeetingDTO.getMeetingId());
        assertEquals(newStartDateTime, updateMeetingDTO.getStartDateTime());
        assertEquals(newEndDateTime, updateMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should handle edge case datetime scenarios")
    void testDateTimeEdgeCases() {
        // Test with same start and end time
        OffsetDateTime sameTime = OffsetDateTime.now().plusDays(1);
        
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(sameTime);
        updateMeetingDTO.setEndDateTime(sameTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.isEmpty(), "Should pass validation even with same start and end time");

        // Test with end time before start time (validation doesn't check this at DTO level)
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.minusHours(1);
        
        updateMeetingDTO.setStartDateTime(startTime);
        updateMeetingDTO.setEndDateTime(endTime);

        violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.isEmpty(), "DTO validation should not check time logic");
    }

    @Test
    @DisplayName("Should handle update-specific scenarios")
    void testUpdateSpecificScenarios() {
        // Test updating only time while keeping same meeting ID
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.isEmpty());

        // Test updating to different times
        OffsetDateTime newStartTime = testStartDateTime.plusHours(2);
        OffsetDateTime newEndTime = testEndDateTime.plusHours(2);
        
        updateMeetingDTO.setStartDateTime(newStartTime);
        updateMeetingDTO.setEndDateTime(newEndTime);

        violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.isEmpty());
        assertEquals(testMeetingId, updateMeetingDTO.getMeetingId());
        assertEquals(newStartTime, updateMeetingDTO.getStartDateTime());
        assertEquals(newEndTime, updateMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        UpdateMeetingDTO dto1 = new UpdateMeetingDTO(
            testMeetingId, testStartDateTime, testEndDateTime
        );
        UpdateMeetingDTO dto2 = new UpdateMeetingDTO(
            testMeetingId, testStartDateTime, testEndDateTime
        );
        UpdateMeetingDTO dto3 = new UpdateMeetingDTO(
            UUID.randomUUID(), testStartDateTime, testEndDateTime
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        String toString = updateMeetingDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("UpdateMeetingDTO"));
        assertTrue(toString.contains(testMeetingId.toString()));
    }

    @Test
    @DisplayName("Should support data transfer functionality")
    void testDataTransferBehavior() {
        // Simulate data transfer by setting all fields and verifying they're preserved
        UpdateMeetingDTO sourceDTO = new UpdateMeetingDTO();
        sourceDTO.setMeetingId(testMeetingId);
        sourceDTO.setStartDateTime(testStartDateTime);
        sourceDTO.setEndDateTime(testEndDateTime);

        // Create target DTO using all-args constructor (simulating mapping)
        UpdateMeetingDTO targetDTO = new UpdateMeetingDTO(
            sourceDTO.getMeetingId(),
            sourceDTO.getStartDateTime(),
            sourceDTO.getEndDateTime()
        );

        // Verify all data was transferred correctly
        assertEquals(sourceDTO.getMeetingId(), targetDTO.getMeetingId());
        assertEquals(sourceDTO.getStartDateTime(), targetDTO.getStartDateTime());
        assertEquals(sourceDTO.getEndDateTime(), targetDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should handle field mapping consistency")
    void testFieldMappingConsistency() {
        // Test that all fields can be set and retrieved consistently
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        // Verify all fields maintain their values
        assertEquals(testMeetingId, updateMeetingDTO.getMeetingId());
        assertEquals(testStartDateTime, updateMeetingDTO.getStartDateTime());
        assertEquals(testEndDateTime, updateMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should handle validation with partial data")
    void testPartialDataValidation() {
        // Test with only meeting ID set
        updateMeetingDTO.setMeetingId(testMeetingId);
        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        assertEquals(2, violations.size()); // startDateTime, endDateTime are null

        // Test with meeting ID and startDateTime set
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        violations = validator.validate(updateMeetingDTO);
        assertEquals(1, violations.size()); // endDateTime is null

        // Test with all fields set
        updateMeetingDTO.setEndDateTime(testEndDateTime);
        violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.isEmpty()); // All required fields are set
    }

    @Test
    @DisplayName("Should handle meeting ID constraints")
    void testMeetingIdConstraints() {
        // Test that meeting ID is properly validated as required
        updateMeetingDTO.setMeetingId(null);
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<UpdateMeetingDTO> violation = violations.iterator().next();
        assertEquals("meetingId", violation.getPropertyPath().toString());
        assertEquals("Meeting ID is required", violation.getMessage());
    }

    @Test
    @DisplayName("Should handle datetime constraints")
    void testDateTimeConstraints() {
        // Test start datetime constraint
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(null);
        updateMeetingDTO.setEndDateTime(testEndDateTime);

        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDateTime")));

        // Test end datetime constraint
        updateMeetingDTO.setStartDateTime(testStartDateTime);
        updateMeetingDTO.setEndDateTime(null);

        violations = validator.validate(updateMeetingDTO);
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endDateTime")));
    }

    @Test
    @DisplayName("Should handle data type validation")
    void testDataTypeValidation() {
        // Test that UUID field accepts valid UUID objects
        UUID validUUID = UUID.randomUUID();
        updateMeetingDTO.setMeetingId(validUUID);
        assertEquals(validUUID, updateMeetingDTO.getMeetingId());

        // Test that OffsetDateTime fields accept valid OffsetDateTime objects
        OffsetDateTime validDateTime = OffsetDateTime.now();
        updateMeetingDTO.setStartDateTime(validDateTime);
        updateMeetingDTO.setEndDateTime(validDateTime.plusHours(1));
        
        assertEquals(validDateTime, updateMeetingDTO.getStartDateTime());
        assertEquals(validDateTime.plusHours(1), updateMeetingDTO.getEndDateTime());
    }

    @Test
    @DisplayName("Should handle update-specific constraints")
    void testUpdateSpecificConstraints() {
        // Verify that all required fields for update are properly validated
        UpdateMeetingDTO emptyDTO = new UpdateMeetingDTO();
        Set<ConstraintViolation<UpdateMeetingDTO>> violations = validator.validate(emptyDTO);
        
        // Should have exactly 3 violations (meetingId, startDateTime, endDateTime)
        assertEquals(3, violations.size());
        
        // Verify each required field has a violation
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("meetingId")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startDateTime")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endDateTime")));
    }
}