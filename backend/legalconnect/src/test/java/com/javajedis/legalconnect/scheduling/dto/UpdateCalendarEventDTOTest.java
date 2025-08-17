package com.javajedis.legalconnect.scheduling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UpdateCalendarEventDTOTest {

    private Validator validator;
    private UpdateCalendarEventDTO updateCalendarEventDTO;
    private LocalDate testDate;
    private LocalTime testStartTime;
    private LocalTime testEndTime;
    private List<String> testAttendeeEmails;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        updateCalendarEventDTO = new UpdateCalendarEventDTO();
        testDate = LocalDate.of(2024, 12, 25);
        testStartTime = LocalTime.of(10, 0);
        testEndTime = LocalTime.of(12, 0);
        testAttendeeEmails = Arrays.asList("attendee1@example.com", "attendee2@example.com");
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(updateCalendarEventDTO);
        assertNull(updateCalendarEventDTO.getAccessToken());
        assertNull(updateCalendarEventDTO.getEventId());
        assertNull(updateCalendarEventDTO.getTitle());
        assertNull(updateCalendarEventDTO.getDescription());
        assertNull(updateCalendarEventDTO.getDate());
        assertNull(updateCalendarEventDTO.getStartTime());
        assertNull(updateCalendarEventDTO.getEndTime());
        assertNull(updateCalendarEventDTO.getAttendeeEmails());
    }

    @Test
    void testAllArgsConstructor() {
        String accessToken = "test_access_token";
        String eventId = "event_123";
        String title = "Test Meeting";
        String description = "Test meeting description";

        UpdateCalendarEventDTO dto = new UpdateCalendarEventDTO(
            accessToken, eventId, title, description, testDate, testStartTime, testEndTime, testAttendeeEmails
        );

        assertEquals(accessToken, dto.getAccessToken());
        assertEquals(eventId, dto.getEventId());
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(testDate, dto.getDate());
        assertEquals(testStartTime, dto.getStartTime());
        assertEquals(testEndTime, dto.getEndTime());
        assertEquals(testAttendeeEmails, dto.getAttendeeEmails());
    }

    @Test
    void testSettersAndGetters() {
        String accessToken = "test_access_token";
        String eventId = "event_123";
        String title = "Test Meeting";
        String description = "Test meeting description";

        updateCalendarEventDTO.setAccessToken(accessToken);
        updateCalendarEventDTO.setEventId(eventId);
        updateCalendarEventDTO.setTitle(title);
        updateCalendarEventDTO.setDescription(description);
        updateCalendarEventDTO.setDate(testDate);
        updateCalendarEventDTO.setStartTime(testStartTime);
        updateCalendarEventDTO.setEndTime(testEndTime);
        updateCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        assertEquals(accessToken, updateCalendarEventDTO.getAccessToken());
        assertEquals(eventId, updateCalendarEventDTO.getEventId());
        assertEquals(title, updateCalendarEventDTO.getTitle());
        assertEquals(description, updateCalendarEventDTO.getDescription());
        assertEquals(testDate, updateCalendarEventDTO.getDate());
        assertEquals(testStartTime, updateCalendarEventDTO.getStartTime());
        assertEquals(testEndTime, updateCalendarEventDTO.getEndTime());
        assertEquals(testAttendeeEmails, updateCalendarEventDTO.getAttendeeEmails());
    }

    @Test
    void testValidUpdateCalendarEventDTO() {
        updateCalendarEventDTO.setAccessToken("valid_access_token");
        updateCalendarEventDTO.setEventId("valid_event_id");
        updateCalendarEventDTO.setTitle("Valid Meeting");
        updateCalendarEventDTO.setDescription("Valid meeting description");
        updateCalendarEventDTO.setDate(testDate);
        updateCalendarEventDTO.setStartTime(testStartTime);
        updateCalendarEventDTO.setEndTime(testEndTime);
        updateCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        Set<ConstraintViolation<UpdateCalendarEventDTO>> violations = validator.validate(updateCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmptyAttendeeEmails() {
        updateCalendarEventDTO.setAccessToken("test_access_token");
        updateCalendarEventDTO.setEventId("event_123");
        updateCalendarEventDTO.setTitle("Test Meeting");
        updateCalendarEventDTO.setDescription("Test description");
        updateCalendarEventDTO.setDate(testDate);
        updateCalendarEventDTO.setStartTime(testStartTime);
        updateCalendarEventDTO.setEndTime(testEndTime);
        updateCalendarEventDTO.setAttendeeEmails(Collections.emptyList());

        Set<ConstraintViolation<UpdateCalendarEventDTO>> violations = validator.validate(updateCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullAttendeeEmails() {
        updateCalendarEventDTO.setAccessToken("test_access_token");
        updateCalendarEventDTO.setEventId("event_123");
        updateCalendarEventDTO.setTitle("Test Meeting");
        updateCalendarEventDTO.setDescription("Test description");
        updateCalendarEventDTO.setDate(testDate);
        updateCalendarEventDTO.setStartTime(testStartTime);
        updateCalendarEventDTO.setEndTime(testEndTime);
        updateCalendarEventDTO.setAttendeeEmails(null);

        Set<ConstraintViolation<UpdateCalendarEventDTO>> violations = validator.validate(updateCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTimeValidation() {
        updateCalendarEventDTO.setAccessToken("test_access_token");
        updateCalendarEventDTO.setEventId("event_123");
        updateCalendarEventDTO.setTitle("Test Meeting");
        updateCalendarEventDTO.setDescription("Test description");
        updateCalendarEventDTO.setDate(testDate);
        updateCalendarEventDTO.setStartTime(LocalTime.of(14, 0)); // 2 PM
        updateCalendarEventDTO.setEndTime(LocalTime.of(12, 0)); // 12 PM (earlier than start)
        updateCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        // Note: This DTO doesn't have time validation constraints, so it would be valid even with end time before start time
        Set<ConstraintViolation<UpdateCalendarEventDTO>> violations = validator.validate(updateCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        UpdateCalendarEventDTO dto1 = new UpdateCalendarEventDTO(
            "token", "event_123", "Meeting", "Description", testDate, testStartTime, testEndTime, testAttendeeEmails
        );
        UpdateCalendarEventDTO dto2 = new UpdateCalendarEventDTO(
            "token", "event_123", "Meeting", "Description", testDate, testStartTime, testEndTime, testAttendeeEmails
        );
        UpdateCalendarEventDTO dto3 = new UpdateCalendarEventDTO(
            "different_token", "event_123", "Meeting", "Description", testDate, testStartTime, testEndTime, testAttendeeEmails
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        updateCalendarEventDTO.setAccessToken("test_access_token");
        updateCalendarEventDTO.setEventId("event_123");
        updateCalendarEventDTO.setTitle("Test Meeting");
        updateCalendarEventDTO.setDescription("Test description");
        updateCalendarEventDTO.setDate(testDate);
        updateCalendarEventDTO.setStartTime(testStartTime);
        updateCalendarEventDTO.setEndTime(testEndTime);
        updateCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        String toString = updateCalendarEventDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Meeting"));
        assertTrue(toString.contains("test_access_token"));
        assertTrue(toString.contains("event_123"));
    }

    @Test
    void testSetNullValues() {
        // Set all values to null
        updateCalendarEventDTO.setAccessToken(null);
        updateCalendarEventDTO.setEventId(null);
        updateCalendarEventDTO.setTitle(null);
        updateCalendarEventDTO.setDescription(null);
        updateCalendarEventDTO.setDate(null);
        updateCalendarEventDTO.setStartTime(null);
        updateCalendarEventDTO.setEndTime(null);
        updateCalendarEventDTO.setAttendeeEmails(null);

        assertNull(updateCalendarEventDTO.getAccessToken());
        assertNull(updateCalendarEventDTO.getEventId());
        assertNull(updateCalendarEventDTO.getTitle());
        assertNull(updateCalendarEventDTO.getDescription());
        assertNull(updateCalendarEventDTO.getDate());
        assertNull(updateCalendarEventDTO.getStartTime());
        assertNull(updateCalendarEventDTO.getEndTime());
        assertNull(updateCalendarEventDTO.getAttendeeEmails());
    }

    @Test
    void testMultipleAttendeeEmails() {
        List<String> multipleEmails = Arrays.asList(
            "attendee1@example.com", 
            "attendee2@example.com", 
            "attendee3@example.com",
            "attendee4@example.com"
        );
        
        updateCalendarEventDTO.setAttendeeEmails(multipleEmails);
        
        assertEquals(multipleEmails, updateCalendarEventDTO.getAttendeeEmails());
        assertEquals(4, updateCalendarEventDTO.getAttendeeEmails().size());
        assertTrue(updateCalendarEventDTO.getAttendeeEmails().contains("attendee1@example.com"));
        assertTrue(updateCalendarEventDTO.getAttendeeEmails().contains("attendee4@example.com"));
    }

    @Test
    void testSingleAttendeeEmail() {
        List<String> singleEmail = Arrays.asList("single@example.com");
        
        updateCalendarEventDTO.setAttendeeEmails(singleEmail);
        
        assertEquals(singleEmail, updateCalendarEventDTO.getAttendeeEmails());
        assertEquals(1, updateCalendarEventDTO.getAttendeeEmails().size());
        assertEquals("single@example.com", updateCalendarEventDTO.getAttendeeEmails().get(0));
    }

    @Test
    void testEventIdHandling() {
        String eventId = "unique_event_id_123";
        updateCalendarEventDTO.setEventId(eventId);
        
        assertEquals(eventId, updateCalendarEventDTO.getEventId());
        
        // Test with different event ID formats
        updateCalendarEventDTO.setEventId("event-with-dashes");
        assertEquals("event-with-dashes", updateCalendarEventDTO.getEventId());
        
        updateCalendarEventDTO.setEventId("event_with_underscores");
        assertEquals("event_with_underscores", updateCalendarEventDTO.getEventId());
    }

    @Test
    void testCopyConstructor() {
        UpdateCalendarEventDTO original = new UpdateCalendarEventDTO(
            "token", "event_123", "Meeting", "Description", testDate, testStartTime, testEndTime, testAttendeeEmails
        );
        
        UpdateCalendarEventDTO copy = new UpdateCalendarEventDTO(
            original.getAccessToken(),
            original.getEventId(),
            original.getTitle(),
            original.getDescription(),
            original.getDate(),
            original.getStartTime(),
            original.getEndTime(),
            original.getAttendeeEmails()
        );
        
        assertEquals(original, copy);
        assertEquals(original.getAccessToken(), copy.getAccessToken());
        assertEquals(original.getEventId(), copy.getEventId());
        assertEquals(original.getTitle(), copy.getTitle());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.getDate(), copy.getDate());
        assertEquals(original.getStartTime(), copy.getStartTime());
        assertEquals(original.getEndTime(), copy.getEndTime());
        assertEquals(original.getAttendeeEmails(), copy.getAttendeeEmails());
    }

    @Test
    void testPartialUpdate() {
        // Test updating only some fields (common use case for update DTOs)
        updateCalendarEventDTO.setAccessToken("access_token");
        updateCalendarEventDTO.setEventId("event_123");
        updateCalendarEventDTO.setTitle("Updated Title");
        // Leave other fields null for partial update
        
        assertEquals("access_token", updateCalendarEventDTO.getAccessToken());
        assertEquals("event_123", updateCalendarEventDTO.getEventId());
        assertEquals("Updated Title", updateCalendarEventDTO.getTitle());
        assertNull(updateCalendarEventDTO.getDescription());
        assertNull(updateCalendarEventDTO.getDate());
        assertNull(updateCalendarEventDTO.getStartTime());
        assertNull(updateCalendarEventDTO.getEndTime());
        assertNull(updateCalendarEventDTO.getAttendeeEmails());
    }
} 