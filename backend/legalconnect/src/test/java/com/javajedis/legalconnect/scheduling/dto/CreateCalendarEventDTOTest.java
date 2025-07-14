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

class CreateCalendarEventDTOTest {

    private Validator validator;
    private CreateCalendarEventDTO createCalendarEventDTO;
    private LocalDate testDate;
    private LocalTime testStartTime;
    private LocalTime testEndTime;
    private List<String> testAttendeeEmails;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        createCalendarEventDTO = new CreateCalendarEventDTO();
        testDate = LocalDate.of(2024, 12, 25);
        testStartTime = LocalTime.of(10, 0);
        testEndTime = LocalTime.of(12, 0);
        testAttendeeEmails = Arrays.asList("attendee1@example.com", "attendee2@example.com");
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(createCalendarEventDTO);
        assertNull(createCalendarEventDTO.getAccessToken());
        assertNull(createCalendarEventDTO.getTitle());
        assertNull(createCalendarEventDTO.getDescription());
        assertNull(createCalendarEventDTO.getDate());
        assertNull(createCalendarEventDTO.getStartTime());
        assertNull(createCalendarEventDTO.getEndTime());
        assertNull(createCalendarEventDTO.getHostEmail());
        assertNull(createCalendarEventDTO.getAttendeeEmails());
    }

    @Test
    void testAllArgsConstructor() {
        String accessToken = "test_access_token";
        String title = "Test Meeting";
        String description = "Test meeting description";
        String hostEmail = "host@example.com";

        CreateCalendarEventDTO dto = new CreateCalendarEventDTO(
            accessToken, title, description, testDate, testStartTime, testEndTime, hostEmail, testAttendeeEmails
        );

        assertEquals(accessToken, dto.getAccessToken());
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(testDate, dto.getDate());
        assertEquals(testStartTime, dto.getStartTime());
        assertEquals(testEndTime, dto.getEndTime());
        assertEquals(hostEmail, dto.getHostEmail());
        assertEquals(testAttendeeEmails, dto.getAttendeeEmails());
    }

    @Test
    void testSettersAndGetters() {
        String accessToken = "test_access_token";
        String title = "Test Meeting";
        String description = "Test meeting description";
        String hostEmail = "host@example.com";

        createCalendarEventDTO.setAccessToken(accessToken);
        createCalendarEventDTO.setTitle(title);
        createCalendarEventDTO.setDescription(description);
        createCalendarEventDTO.setDate(testDate);
        createCalendarEventDTO.setStartTime(testStartTime);
        createCalendarEventDTO.setEndTime(testEndTime);
        createCalendarEventDTO.setHostEmail(hostEmail);
        createCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        assertEquals(accessToken, createCalendarEventDTO.getAccessToken());
        assertEquals(title, createCalendarEventDTO.getTitle());
        assertEquals(description, createCalendarEventDTO.getDescription());
        assertEquals(testDate, createCalendarEventDTO.getDate());
        assertEquals(testStartTime, createCalendarEventDTO.getStartTime());
        assertEquals(testEndTime, createCalendarEventDTO.getEndTime());
        assertEquals(hostEmail, createCalendarEventDTO.getHostEmail());
        assertEquals(testAttendeeEmails, createCalendarEventDTO.getAttendeeEmails());
    }

    @Test
    void testValidCreateCalendarEventDTO() {
        createCalendarEventDTO.setAccessToken("valid_access_token");
        createCalendarEventDTO.setTitle("Valid Meeting");
        createCalendarEventDTO.setDescription("Valid meeting description");
        createCalendarEventDTO.setDate(testDate);
        createCalendarEventDTO.setStartTime(testStartTime);
        createCalendarEventDTO.setEndTime(testEndTime);
        createCalendarEventDTO.setHostEmail("host@example.com");
        createCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        Set<ConstraintViolation<CreateCalendarEventDTO>> violations = validator.validate(createCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmptyAttendeeEmails() {
        createCalendarEventDTO.setAccessToken("test_access_token");
        createCalendarEventDTO.setTitle("Test Meeting");
        createCalendarEventDTO.setDescription("Test description");
        createCalendarEventDTO.setDate(testDate);
        createCalendarEventDTO.setStartTime(testStartTime);
        createCalendarEventDTO.setEndTime(testEndTime);
        createCalendarEventDTO.setHostEmail("host@example.com");
        createCalendarEventDTO.setAttendeeEmails(Collections.emptyList());

        Set<ConstraintViolation<CreateCalendarEventDTO>> violations = validator.validate(createCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullAttendeeEmails() {
        createCalendarEventDTO.setAccessToken("test_access_token");
        createCalendarEventDTO.setTitle("Test Meeting");
        createCalendarEventDTO.setDescription("Test description");
        createCalendarEventDTO.setDate(testDate);
        createCalendarEventDTO.setStartTime(testStartTime);
        createCalendarEventDTO.setEndTime(testEndTime);
        createCalendarEventDTO.setHostEmail("host@example.com");
        createCalendarEventDTO.setAttendeeEmails(null);

        Set<ConstraintViolation<CreateCalendarEventDTO>> violations = validator.validate(createCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTimeValidation() {
        createCalendarEventDTO.setAccessToken("test_access_token");
        createCalendarEventDTO.setTitle("Test Meeting");
        createCalendarEventDTO.setDescription("Test description");
        createCalendarEventDTO.setDate(testDate);
        createCalendarEventDTO.setStartTime(LocalTime.of(14, 0)); // 2 PM
        createCalendarEventDTO.setEndTime(LocalTime.of(12, 0)); // 12 PM (earlier than start)
        createCalendarEventDTO.setHostEmail("host@example.com");
        createCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        // Note: This DTO doesn't have time validation constraints, so it would be valid even with end time before start time
        Set<ConstraintViolation<CreateCalendarEventDTO>> violations = validator.validate(createCalendarEventDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        CreateCalendarEventDTO dto1 = new CreateCalendarEventDTO(
            "token", "Meeting", "Description", testDate, testStartTime, testEndTime, "host@example.com", testAttendeeEmails
        );
        CreateCalendarEventDTO dto2 = new CreateCalendarEventDTO(
            "token", "Meeting", "Description", testDate, testStartTime, testEndTime, "host@example.com", testAttendeeEmails
        );
        CreateCalendarEventDTO dto3 = new CreateCalendarEventDTO(
            "different_token", "Meeting", "Description", testDate, testStartTime, testEndTime, "host@example.com", testAttendeeEmails
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        createCalendarEventDTO.setAccessToken("test_access_token");
        createCalendarEventDTO.setTitle("Test Meeting");
        createCalendarEventDTO.setDescription("Test description");
        createCalendarEventDTO.setDate(testDate);
        createCalendarEventDTO.setStartTime(testStartTime);
        createCalendarEventDTO.setEndTime(testEndTime);
        createCalendarEventDTO.setHostEmail("host@example.com");
        createCalendarEventDTO.setAttendeeEmails(testAttendeeEmails);

        String toString = createCalendarEventDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Meeting"));
        assertTrue(toString.contains("test_access_token"));
        assertTrue(toString.contains("host@example.com"));
    }

    @Test
    void testSetNullValues() {
        // Set all values to null
        createCalendarEventDTO.setAccessToken(null);
        createCalendarEventDTO.setTitle(null);
        createCalendarEventDTO.setDescription(null);
        createCalendarEventDTO.setDate(null);
        createCalendarEventDTO.setStartTime(null);
        createCalendarEventDTO.setEndTime(null);
        createCalendarEventDTO.setHostEmail(null);
        createCalendarEventDTO.setAttendeeEmails(null);

        assertNull(createCalendarEventDTO.getAccessToken());
        assertNull(createCalendarEventDTO.getTitle());
        assertNull(createCalendarEventDTO.getDescription());
        assertNull(createCalendarEventDTO.getDate());
        assertNull(createCalendarEventDTO.getStartTime());
        assertNull(createCalendarEventDTO.getEndTime());
        assertNull(createCalendarEventDTO.getHostEmail());
        assertNull(createCalendarEventDTO.getAttendeeEmails());
    }

    @Test
    void testMultipleAttendeeEmails() {
        List<String> multipleEmails = Arrays.asList(
            "attendee1@example.com", 
            "attendee2@example.com", 
            "attendee3@example.com",
            "attendee4@example.com"
        );
        
        createCalendarEventDTO.setAttendeeEmails(multipleEmails);
        
        assertEquals(multipleEmails, createCalendarEventDTO.getAttendeeEmails());
        assertEquals(4, createCalendarEventDTO.getAttendeeEmails().size());
        assertTrue(createCalendarEventDTO.getAttendeeEmails().contains("attendee1@example.com"));
        assertTrue(createCalendarEventDTO.getAttendeeEmails().contains("attendee4@example.com"));
    }

    @Test
    void testSingleAttendeeEmail() {
        List<String> singleEmail = Arrays.asList("single@example.com");
        
        createCalendarEventDTO.setAttendeeEmails(singleEmail);
        
        assertEquals(singleEmail, createCalendarEventDTO.getAttendeeEmails());
        assertEquals(1, createCalendarEventDTO.getAttendeeEmails().size());
        assertEquals("single@example.com", createCalendarEventDTO.getAttendeeEmails().get(0));
    }

    @Test
    void testCopyConstructor() {
        CreateCalendarEventDTO original = new CreateCalendarEventDTO(
            "token", "Meeting", "Description", testDate, testStartTime, testEndTime, "host@example.com", testAttendeeEmails
        );
        
        CreateCalendarEventDTO copy = new CreateCalendarEventDTO(
            original.getAccessToken(),
            original.getTitle(),
            original.getDescription(),
            original.getDate(),
            original.getStartTime(),
            original.getEndTime(),
            original.getHostEmail(),
            original.getAttendeeEmails()
        );
        
        assertEquals(original, copy);
        assertEquals(original.getAccessToken(), copy.getAccessToken());
        assertEquals(original.getTitle(), copy.getTitle());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.getDate(), copy.getDate());
        assertEquals(original.getStartTime(), copy.getStartTime());
        assertEquals(original.getEndTime(), copy.getEndTime());
        assertEquals(original.getHostEmail(), copy.getHostEmail());
        assertEquals(original.getAttendeeEmails(), copy.getAttendeeEmails());
    }
} 