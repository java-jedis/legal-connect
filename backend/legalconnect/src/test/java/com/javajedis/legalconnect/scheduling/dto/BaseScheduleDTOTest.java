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
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.javajedis.legalconnect.scheduling.ScheduleType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class BaseScheduleDTOTest {

    private Validator validator;
    private TestableBaseScheduleDTO baseScheduleDTO;
    private LocalDate testDate;
    private OffsetDateTime testStartTime;
    private OffsetDateTime testEndTime;

    // Concrete implementation for testing the abstract class
    private static class TestableBaseScheduleDTO extends BaseScheduleDTO {
        // No additional fields needed for testing base functionality
    }

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        baseScheduleDTO = new TestableBaseScheduleDTO();
        testDate = LocalDate.of(2024, 12, 25);
        testStartTime = OffsetDateTime.now();
        testEndTime = testStartTime.plusHours(2);
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(baseScheduleDTO);
        assertNull(baseScheduleDTO.getTitle());
        assertNull(baseScheduleDTO.getType());
        assertNull(baseScheduleDTO.getDescription());
        assertNull(baseScheduleDTO.getDate());
        assertNull(baseScheduleDTO.getStartTime());
        assertNull(baseScheduleDTO.getEndTime());
    }

    @Test
    void testSettersAndGetters() {
        baseScheduleDTO.setTitle("Test Schedule");
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDescription("Test description");
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(testEndTime);

        assertEquals("Test Schedule", baseScheduleDTO.getTitle());
        assertEquals(ScheduleType.MEETING, baseScheduleDTO.getType());
        assertEquals("Test description", baseScheduleDTO.getDescription());
        assertEquals(testDate, baseScheduleDTO.getDate());
        assertEquals(testStartTime, baseScheduleDTO.getStartTime());
        assertEquals(testEndTime, baseScheduleDTO.getEndTime());
    }

    @Test
    void testValidBaseScheduleDTO() {
        baseScheduleDTO.setTitle("Valid Title");
        baseScheduleDTO.setType(ScheduleType.COURT_HEARING);
        baseScheduleDTO.setDescription("Valid description");
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @ParameterizedTest
    @MethodSource("titleValidationTestData")
    void testTitleValidation(String title, boolean shouldHaveViolations, String expectedMessageFragment) {
        baseScheduleDTO.setTitle(title);
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        
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
            Arguments.of(null, true, "Title is required"),
            Arguments.of("", true, "Title is required"),
            Arguments.of("   ", true, "Title is required"),
            Arguments.of("A", true, "between 2 and 255"),
            Arguments.of("T".repeat(256), true, "between 2 and 255"),
            Arguments.of("AB", false, null),
            Arguments.of("Valid Title", false, null),
            Arguments.of("T".repeat(255), false, null)
        );
    }

    @Test
    void testTypeValidation_Null() {
        baseScheduleDTO.setTitle("Valid Title");
        baseScheduleDTO.setType(null);
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("type") && 
            v.getMessage().equals("Schedule type is required")));
    }

    @Test
    void testTypeValidation_AllValues() {
        for (ScheduleType type : ScheduleType.values()) {
            baseScheduleDTO.setTitle("Valid Title");
            baseScheduleDTO.setType(type);
            baseScheduleDTO.setDate(testDate);
            baseScheduleDTO.setStartTime(testStartTime);
            baseScheduleDTO.setEndTime(testEndTime);

            Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("type")));
        }
    }

    @ParameterizedTest
    @MethodSource("descriptionValidationTestData")
    void testDescriptionValidation(String description, boolean shouldHaveViolations) {
        baseScheduleDTO.setTitle("Valid Title");
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDescription(description);
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        
        if (shouldHaveViolations) {
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        } else {
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("description")));
        }
    }

    static Stream<Arguments> descriptionValidationTestData() {
        return Stream.of(
            Arguments.of(null, false), // Description is optional
            Arguments.of("", false),
            Arguments.of("Valid description", false),
            Arguments.of("D".repeat(2000), false),
            Arguments.of("D".repeat(2001), true) // Exceeds max length
        );
    }

    @Test
    void testDateValidation_Null() {
        baseScheduleDTO.setTitle("Valid Title");
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDate(null);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("date") && 
            v.getMessage().equals("Date is required")));
    }

    @Test
    void testStartTimeValidation_Null() {
        baseScheduleDTO.setTitle("Valid Title");
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(null);
        baseScheduleDTO.setEndTime(testEndTime);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("startTime") && 
            v.getMessage().equals("Start time is required")));
    }

    @Test
    void testEndTimeValidation_Null() {
        baseScheduleDTO.setTitle("Valid Title");
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDate(testDate);
        baseScheduleDTO.setStartTime(testStartTime);
        baseScheduleDTO.setEndTime(null);

        Set<ConstraintViolation<TestableBaseScheduleDTO>> violations = validator.validate(baseScheduleDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("endTime") && 
            v.getMessage().equals("End time is required")));
    }

    @Test
    void testEqualsAndHashCode() {
        TestableBaseScheduleDTO dto1 = new TestableBaseScheduleDTO();
        dto1.setTitle("Test Title");
        dto1.setType(ScheduleType.MEETING);
        dto1.setDescription("Test description");
        dto1.setDate(testDate);
        dto1.setStartTime(testStartTime);
        dto1.setEndTime(testEndTime);
        
        TestableBaseScheduleDTO dto2 = new TestableBaseScheduleDTO();
        dto2.setTitle("Test Title");
        dto2.setType(ScheduleType.MEETING);
        dto2.setDescription("Test description");
        dto2.setDate(testDate);
        dto2.setStartTime(testStartTime);
        dto2.setEndTime(testEndTime);
        
        TestableBaseScheduleDTO dto3 = new TestableBaseScheduleDTO();
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
        baseScheduleDTO.setTitle("Test Title");
        baseScheduleDTO.setType(ScheduleType.MEETING);
        baseScheduleDTO.setDescription("Test description");

        String toString = baseScheduleDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Title"));
        assertTrue(toString.contains("MEETING"));
        assertTrue(toString.contains("Test description"));
    }
} 