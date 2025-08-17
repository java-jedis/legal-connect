package com.javajedis.legalconnect.scheduling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.scheduling.ScheduleType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UpdateScheduleDTOTest {

    private Validator validator;
    private UpdateScheduleDTO updateScheduleDTO;
    private LocalDate testDate;
    private OffsetDateTime testStartTime;
    private OffsetDateTime testEndTime;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        updateScheduleDTO = new UpdateScheduleDTO();
        testDate = LocalDate.of(2024, 12, 25);
        testStartTime = OffsetDateTime.now();
        testEndTime = testStartTime.plusHours(2);
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(updateScheduleDTO);
        // Test inherited properties from BaseScheduleDTO
        assertNull(updateScheduleDTO.getTitle());
        assertNull(updateScheduleDTO.getType());
        assertNull(updateScheduleDTO.getDescription());
        assertNull(updateScheduleDTO.getDate());
        assertNull(updateScheduleDTO.getStartTime());
        assertNull(updateScheduleDTO.getEndTime());
    }

    @Test
    void testSettersAndGetters() {
        updateScheduleDTO.setTitle("Updated Schedule");
        updateScheduleDTO.setType(ScheduleType.COURT_HEARING);
        updateScheduleDTO.setDescription("Updated description");
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        assertEquals("Updated Schedule", updateScheduleDTO.getTitle());
        assertEquals(ScheduleType.COURT_HEARING, updateScheduleDTO.getType());
        assertEquals("Updated description", updateScheduleDTO.getDescription());
        assertEquals(testDate, updateScheduleDTO.getDate());
        assertEquals(testStartTime, updateScheduleDTO.getStartTime());
        assertEquals(testEndTime, updateScheduleDTO.getEndTime());
    }

    @Test
    void testValidUpdateScheduleDTO() {
        updateScheduleDTO.setTitle("Valid Updated Title");
        updateScheduleDTO.setType(ScheduleType.MEDIATION_SESSION);
        updateScheduleDTO.setDescription("Valid updated description");
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    void testTitleValidation_Null() {
        updateScheduleDTO.setTitle(null);
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().equals("Title is required")));
    }

    @Test
    void testTitleValidation_Empty() {
        updateScheduleDTO.setTitle("");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().equals("Title is required")));
    }

    @Test
    void testTitleValidation_TooShort() {
        updateScheduleDTO.setTitle("A"); // Only 1 character
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().contains("between 2 and 255")));
    }

    @Test
    void testTitleValidation_TooLong() {
        updateScheduleDTO.setTitle("T".repeat(256)); // Too long
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().contains("between 2 and 255")));
    }

    @Test
    void testTypeValidation_Null() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(null);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("type") && 
            v.getMessage().equals("Schedule type is required")));
    }

    @Test
    void testDateValidation_Null() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(null);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("date") && 
            v.getMessage().equals("Date is required")));
    }

    @Test
    void testStartTimeValidation_Null() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(null);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startTime") && 
            v.getMessage().equals("Start time is required")));
    }

    @Test
    void testEndTimeValidation_Null() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(null);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endTime") && 
            v.getMessage().equals("End time is required")));
    }

    @Test
    void testDescriptionValidation_Optional() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDescription(null); // Description is optional
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void testDescriptionValidation_TooLong() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDescription("D".repeat(2001)); // Too long
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("description") && 
            v.getMessage().contains("cannot exceed 2000")));
    }

    @Test
    void testAllScheduleTypeValues() {
        for (ScheduleType type : ScheduleType.values()) {
            updateScheduleDTO.setTitle("Valid Title");
            updateScheduleDTO.setType(type);
            updateScheduleDTO.setDate(testDate);
            updateScheduleDTO.setStartTime(testStartTime);
            updateScheduleDTO.setEndTime(testEndTime);

            Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
            assertTrue(violations.isEmpty(), 
                "Should have no violations for schedule type: " + type);
        }
    }

    @Test
    void testEqualsAndHashCode() {
        UpdateScheduleDTO dto1 = new UpdateScheduleDTO();
        dto1.setTitle("Test Title");
        dto1.setType(ScheduleType.MEETING);
        dto1.setDescription("Test description");
        dto1.setDate(testDate);
        dto1.setStartTime(testStartTime);
        dto1.setEndTime(testEndTime);
        
        UpdateScheduleDTO dto2 = new UpdateScheduleDTO();
        dto2.setTitle("Test Title");
        dto2.setType(ScheduleType.MEETING);
        dto2.setDescription("Test description");
        dto2.setDate(testDate);
        dto2.setStartTime(testStartTime);
        dto2.setEndTime(testEndTime);
        
        UpdateScheduleDTO dto3 = new UpdateScheduleDTO();
        dto3.setTitle("Different Title");
        dto3.setType(ScheduleType.COURT_HEARING);
        dto3.setDescription("Different description");
        dto3.setDate(testDate.plusDays(1));
        dto3.setStartTime(testStartTime.plusHours(1));
        dto3.setEndTime(testEndTime.plusHours(1));

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        updateScheduleDTO.setTitle("Test Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDescription("Test description");

        String toString = updateScheduleDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes class name and field values
        assertTrue(toString.contains("UpdateScheduleDTO"));
    }

    @Test
    void testSettersWithNullValues() {
        updateScheduleDTO.setTitle(null);
        updateScheduleDTO.setType(null);
        updateScheduleDTO.setDescription(null);
        updateScheduleDTO.setDate(null);
        updateScheduleDTO.setStartTime(null);
        updateScheduleDTO.setEndTime(null);

        assertNull(updateScheduleDTO.getTitle());
        assertNull(updateScheduleDTO.getType());
        assertNull(updateScheduleDTO.getDescription());
        assertNull(updateScheduleDTO.getDate());
        assertNull(updateScheduleDTO.getStartTime());
        assertNull(updateScheduleDTO.getEndTime());
    }

    @Test
    void testSettersWithEmptyValues() {
        updateScheduleDTO.setTitle("");
        updateScheduleDTO.setDescription("");

        assertEquals("", updateScheduleDTO.getTitle());
        assertEquals("", updateScheduleDTO.getDescription());
    }

    @Test
    void testValidMinimumTitle() {
        updateScheduleDTO.setTitle("AB"); // Minimum valid length
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testValidMaximumTitle() {
        updateScheduleDTO.setTitle("T".repeat(255)); // Maximum valid length
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testValidMaximumDescription() {
        updateScheduleDTO.setTitle("Valid Title");
        updateScheduleDTO.setType(ScheduleType.MEETING);
        updateScheduleDTO.setDescription("D".repeat(2000)); // Maximum valid length
        updateScheduleDTO.setDate(testDate);
        updateScheduleDTO.setStartTime(testStartTime);
        updateScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<UpdateScheduleDTO>> violations = validator.validate(updateScheduleDTO);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")));
    }
} 