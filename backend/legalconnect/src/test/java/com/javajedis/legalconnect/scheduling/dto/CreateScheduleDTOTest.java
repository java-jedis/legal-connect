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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.scheduling.ScheduleType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateScheduleDTOTest {

    private Validator validator;
    private CreateScheduleDTO createScheduleDTO;
    private UUID testCaseId;
    private LocalDate testDate;
    private OffsetDateTime testStartTime;
    private OffsetDateTime testEndTime;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        createScheduleDTO = new CreateScheduleDTO();
        testCaseId = UUID.randomUUID();
        testDate = LocalDate.of(2024, 12, 25);
        testStartTime = OffsetDateTime.now();
        testEndTime = testStartTime.plusHours(2);
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(createScheduleDTO);
        assertNull(createScheduleDTO.getCaseId());
        // Test inherited properties
        assertNull(createScheduleDTO.getTitle());
        assertNull(createScheduleDTO.getType());
        assertNull(createScheduleDTO.getDescription());
        assertNull(createScheduleDTO.getDate());
        assertNull(createScheduleDTO.getStartTime());
        assertNull(createScheduleDTO.getEndTime());
    }

    @Test
    void testSettersAndGetters() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Test Schedule");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDescription("Test description");
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        assertEquals(testCaseId, createScheduleDTO.getCaseId());
        assertEquals("Test Schedule", createScheduleDTO.getTitle());
        assertEquals(ScheduleType.MEETING, createScheduleDTO.getType());
        assertEquals("Test description", createScheduleDTO.getDescription());
        assertEquals(testDate, createScheduleDTO.getDate());
        assertEquals(testStartTime, createScheduleDTO.getStartTime());
        assertEquals(testEndTime, createScheduleDTO.getEndTime());
    }

    @Test
    void testValidCreateScheduleDTO() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.COURT_HEARING);
        createScheduleDTO.setDescription("Valid description");
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    void testCaseIdValidation_Null() {
        createScheduleDTO.setCaseId(null);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("caseId") && 
            v.getMessage().equals("Case ID is required")));
    }

    @Test
    void testCaseIdValidation_Valid() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("caseId")));
    }

    @Test
    void testInheritedValidations_Title() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle(null); // Invalid title
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("title") && 
            v.getMessage().equals("Title is required")));
    }

    @Test
    void testInheritedValidations_Type() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(null); // Invalid type
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("type") && 
            v.getMessage().equals("Schedule type is required")));
    }

    @Test
    void testInheritedValidations_Date() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDate(null); // Invalid date
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("date") && 
            v.getMessage().equals("Date is required")));
    }

    @Test
    void testInheritedValidations_StartTime() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(null); // Invalid start time
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startTime") && 
            v.getMessage().equals("Start time is required")));
    }

    @Test
    void testInheritedValidations_EndTime() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(null); // Invalid end time

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endTime") && 
            v.getMessage().equals("End time is required")));
    }

    @Test
    void testAllScheduleTypeValues() {
        for (ScheduleType type : ScheduleType.values()) {
            createScheduleDTO.setCaseId(testCaseId);
            createScheduleDTO.setTitle("Valid Title");
            createScheduleDTO.setType(type);
            createScheduleDTO.setDate(testDate);
            createScheduleDTO.setStartTime(testStartTime);
            createScheduleDTO.setEndTime(testEndTime);

            Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
            assertTrue(violations.isEmpty(), 
                "Should have no violations for schedule type: " + type);
        }
    }

    @Test
    void testEqualsAndHashCode() {
        CreateScheduleDTO dto1 = new CreateScheduleDTO();
        dto1.setCaseId(testCaseId);
        dto1.setTitle("Test Title");
        dto1.setType(ScheduleType.MEETING);
        dto1.setDescription("Test description");
        dto1.setDate(testDate);
        dto1.setStartTime(testStartTime);
        dto1.setEndTime(testEndTime);
        
        CreateScheduleDTO dto2 = new CreateScheduleDTO();
        dto2.setCaseId(testCaseId);
        dto2.setTitle("Test Title");
        dto2.setType(ScheduleType.MEETING);
        dto2.setDescription("Test description");
        dto2.setDate(testDate);
        dto2.setStartTime(testStartTime);
        dto2.setEndTime(testEndTime);
        
        CreateScheduleDTO dto3 = new CreateScheduleDTO();
        dto3.setCaseId(UUID.randomUUID());
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
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Test Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDescription("Test description");

        String toString = createScheduleDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes class name and field values
        assertTrue(toString.contains("CreateScheduleDTO"));
    }

    @Test
    void testSettersWithNullValues() {
        createScheduleDTO.setCaseId(null);
        createScheduleDTO.setTitle(null);
        createScheduleDTO.setType(null);
        createScheduleDTO.setDescription(null);
        createScheduleDTO.setDate(null);
        createScheduleDTO.setStartTime(null);
        createScheduleDTO.setEndTime(null);

        assertNull(createScheduleDTO.getCaseId());
        assertNull(createScheduleDTO.getTitle());
        assertNull(createScheduleDTO.getType());
        assertNull(createScheduleDTO.getDescription());
        assertNull(createScheduleDTO.getDate());
        assertNull(createScheduleDTO.getStartTime());
        assertNull(createScheduleDTO.getEndTime());
    }

    @Test
    void testDescriptionOptional() {
        createScheduleDTO.setCaseId(testCaseId);
        createScheduleDTO.setTitle("Valid Title");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDescription(null); // Description is optional
        createScheduleDTO.setDate(testDate);
        createScheduleDTO.setStartTime(testStartTime);
        createScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<CreateScheduleDTO>> violations = validator.validate(createScheduleDTO);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")));
    }
} 