package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ScheduleGoogleCalendarEventTest {

    private Validator validator;
    private ScheduleGoogleCalendarEvent scheduleGoogleCalendarEvent;
    private Schedule testSchedule;
    private UUID testId;
    private String testGoogleCalendarEventId;
    private OffsetDateTime testCreatedAt;
    private OffsetDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        scheduleGoogleCalendarEvent = new ScheduleGoogleCalendarEvent();
        
        testId = UUID.randomUUID();
        testGoogleCalendarEventId = "google_calendar_event_123";
        testCreatedAt = OffsetDateTime.now().minusDays(1);
        testUpdatedAt = OffsetDateTime.now();
        
        // Create test schedule
        testSchedule = createTestSchedule();
    }

    private Schedule createTestSchedule() {
        // Create test user/lawyer
        User lawyer = new User();
        lawyer.setId(UUID.randomUUID());
        lawyer.setFirstName("John");
        lawyer.setLastName("Doe");
        lawyer.setEmail("john.doe@example.com");
        lawyer.setRole(Role.LAWYER);
        lawyer.setEmailVerified(true);

        // Create test case
        Case testCase = new Case();
        testCase.setId(UUID.randomUUID());
        testCase.setTitle("Test Case");
        testCase.setDescription("Test case description");
        testCase.setStatus(CaseStatus.IN_PROGRESS);
        testCase.setClient(lawyer); // For simplicity, using same user as client
        testCase.setLawyer(new Lawyer());

        // Create test schedule
        Schedule schedule = new Schedule();
        schedule.setId(UUID.randomUUID());
        schedule.setTitle("Test Schedule");
        schedule.setType(ScheduleType.MEETING);
        schedule.setDescription("Test schedule description");
        schedule.setDate(LocalDate.now().plusDays(1));
        schedule.setStartTime(OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
        schedule.setEndTime(OffsetDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0));
        schedule.setCaseEntity(testCase);
        schedule.setCreatedAt(OffsetDateTime.now().minusHours(1));
        schedule.setUpdatedAt(OffsetDateTime.now());

        return schedule;
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(scheduleGoogleCalendarEvent);
        assertNull(scheduleGoogleCalendarEvent.getId());
        assertNull(scheduleGoogleCalendarEvent.getSchedule());
        assertNull(scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
        assertNull(scheduleGoogleCalendarEvent.getCreatedAt());
        assertNull(scheduleGoogleCalendarEvent.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        ScheduleGoogleCalendarEvent event = new ScheduleGoogleCalendarEvent(
            testId, testSchedule, testGoogleCalendarEventId, testCreatedAt, testUpdatedAt
        );

        assertEquals(testId, event.getId());
        assertEquals(testSchedule, event.getSchedule());
        assertEquals(testGoogleCalendarEventId, event.getGoogleCalendarEventId());
        assertEquals(testCreatedAt, event.getCreatedAt());
        assertEquals(testUpdatedAt, event.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        scheduleGoogleCalendarEvent.setId(testId);
        scheduleGoogleCalendarEvent.setSchedule(testSchedule);
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId(testGoogleCalendarEventId);
        scheduleGoogleCalendarEvent.setCreatedAt(testCreatedAt);
        scheduleGoogleCalendarEvent.setUpdatedAt(testUpdatedAt);

        assertEquals(testId, scheduleGoogleCalendarEvent.getId());
        assertEquals(testSchedule, scheduleGoogleCalendarEvent.getSchedule());
        assertEquals(testGoogleCalendarEventId, scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
        assertEquals(testCreatedAt, scheduleGoogleCalendarEvent.getCreatedAt());
        assertEquals(testUpdatedAt, scheduleGoogleCalendarEvent.getUpdatedAt());
    }

    @Test
    void testValidScheduleGoogleCalendarEvent() {
        scheduleGoogleCalendarEvent.setId(testId);
        scheduleGoogleCalendarEvent.setSchedule(testSchedule);
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId(testGoogleCalendarEventId);
        scheduleGoogleCalendarEvent.setCreatedAt(testCreatedAt);
        scheduleGoogleCalendarEvent.setUpdatedAt(testUpdatedAt);

        Set<ConstraintViolation<ScheduleGoogleCalendarEvent>> violations = validator.validate(scheduleGoogleCalendarEvent);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        ScheduleGoogleCalendarEvent event1 = new ScheduleGoogleCalendarEvent(
            testId, testSchedule, testGoogleCalendarEventId, testCreatedAt, testUpdatedAt
        );
        ScheduleGoogleCalendarEvent event2 = new ScheduleGoogleCalendarEvent(
            testId, testSchedule, testGoogleCalendarEventId, testCreatedAt, testUpdatedAt
        );
        ScheduleGoogleCalendarEvent event3 = new ScheduleGoogleCalendarEvent(
            UUID.randomUUID(), testSchedule, testGoogleCalendarEventId, testCreatedAt, testUpdatedAt
        );

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1, event3);
        assertNotEquals(event1.hashCode(), event3.hashCode());
    }

    @Test
    void testToString() {
        scheduleGoogleCalendarEvent.setId(testId);
        scheduleGoogleCalendarEvent.setSchedule(testSchedule);
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId(testGoogleCalendarEventId);

        String toString = scheduleGoogleCalendarEvent.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("google_calendar_event_123"));
    }

    @Test
    void testGoogleCalendarEventIdHandling() {
        String eventId = "unique_google_event_id_123";
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId(eventId);
        
        assertEquals(eventId, scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
        
        // Test with different event ID formats
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId("event-with-dashes");
        assertEquals("event-with-dashes", scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
        
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId("event_with_underscores");
        assertEquals("event_with_underscores", scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
        
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId("eventWithCamelCase");
        assertEquals("eventWithCamelCase", scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
    }

    @Test
    void testScheduleRelationship() {
        scheduleGoogleCalendarEvent.setSchedule(testSchedule);
        
        assertEquals(testSchedule, scheduleGoogleCalendarEvent.getSchedule());
        assertEquals(testSchedule.getId(), scheduleGoogleCalendarEvent.getSchedule().getId());
        assertEquals(testSchedule.getTitle(), scheduleGoogleCalendarEvent.getSchedule().getTitle());
        assertEquals(testSchedule.getType(), scheduleGoogleCalendarEvent.getSchedule().getType());
    }

    @Test
    void testTimestampHandling() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime later = now.plusHours(1);
        
        scheduleGoogleCalendarEvent.setCreatedAt(now);
        scheduleGoogleCalendarEvent.setUpdatedAt(later);
        
        assertEquals(now, scheduleGoogleCalendarEvent.getCreatedAt());
        assertEquals(later, scheduleGoogleCalendarEvent.getUpdatedAt());
        
        assertTrue(scheduleGoogleCalendarEvent.getUpdatedAt().isAfter(scheduleGoogleCalendarEvent.getCreatedAt()));
    }

    @Test
    void testNullSchedule() {
        scheduleGoogleCalendarEvent.setSchedule(null);
        assertNull(scheduleGoogleCalendarEvent.getSchedule());
    }

    @Test
    void testNullGoogleCalendarEventId() {
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId(null);
        assertNull(scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
    }

    @Test
    void testUniqueConstraintScenario() {
        // Test scenario where each schedule should have only one Google Calendar event
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule1 = createTestSchedule();
        schedule1.setId(scheduleId);
        
        Schedule schedule2 = createTestSchedule();
        schedule2.setId(scheduleId); // Same schedule ID
        
        ScheduleGoogleCalendarEvent event1 = new ScheduleGoogleCalendarEvent();
        event1.setSchedule(schedule1);
        event1.setGoogleCalendarEventId("event_1");
        
        ScheduleGoogleCalendarEvent event2 = new ScheduleGoogleCalendarEvent();
        event2.setSchedule(schedule2);
        event2.setGoogleCalendarEventId("event_2");
        
        // Both events have the same schedule ID, which should violate unique constraint in real DB
        assertEquals(event1.getSchedule().getId(), event2.getSchedule().getId());
        assertNotEquals(event1.getGoogleCalendarEventId(), event2.getGoogleCalendarEventId());
    }

    @Test
    void testLazyLoadingScenario() {
        // Test scenario for lazy loading of schedule relationship
        scheduleGoogleCalendarEvent.setSchedule(testSchedule);
        
        // Access schedule properties to simulate lazy loading
        assertNotNull(scheduleGoogleCalendarEvent.getSchedule());
        assertNotNull(scheduleGoogleCalendarEvent.getSchedule().getId());
        assertNotNull(scheduleGoogleCalendarEvent.getSchedule().getTitle());
        assertNotNull(scheduleGoogleCalendarEvent.getSchedule().getType());
        assertNotNull(scheduleGoogleCalendarEvent.getSchedule().getCaseEntity());
    }

    @Test
    void testEntityLifecycle() {
        // Test entity lifecycle - create, update, persist
        ScheduleGoogleCalendarEvent newEvent = new ScheduleGoogleCalendarEvent();
        
        // New entity state
        assertNull(newEvent.getId());
        assertNull(newEvent.getCreatedAt());
        assertNull(newEvent.getUpdatedAt());
        
        // Simulate persistence
        newEvent.setId(UUID.randomUUID());
        newEvent.setSchedule(testSchedule);
        newEvent.setGoogleCalendarEventId("new_event_id");
        
        OffsetDateTime now = OffsetDateTime.now();
        newEvent.setCreatedAt(now);
        newEvent.setUpdatedAt(now);
        
        // Verify persistence state
        assertNotNull(newEvent.getId());
        assertNotNull(newEvent.getCreatedAt());
        assertNotNull(newEvent.getUpdatedAt());
        assertEquals(now, newEvent.getCreatedAt());
        assertEquals(now, newEvent.getUpdatedAt());
        
        // Simulate update
        OffsetDateTime updateTime = now.plusMinutes(30);
        newEvent.setUpdatedAt(updateTime);
        
        // Verify update state
        assertEquals(now, newEvent.getCreatedAt()); // Should not change
        assertEquals(updateTime, newEvent.getUpdatedAt()); // Should change
        assertTrue(newEvent.getUpdatedAt().isAfter(newEvent.getCreatedAt()));
    }

    @Test
    void testDifferentScheduleTypes() {
        // Test with different schedule types
        for (ScheduleType scheduleType : ScheduleType.values()) {
            Schedule schedule = createTestSchedule();
            schedule.setType(scheduleType);
            
            ScheduleGoogleCalendarEvent event = new ScheduleGoogleCalendarEvent();
            event.setSchedule(schedule);
            event.setGoogleCalendarEventId("event_for_" + scheduleType.name().toLowerCase());
            
            assertEquals(scheduleType, event.getSchedule().getType());
        }
    }

    @Test
    void testLongGoogleCalendarEventId() {
        // Test with very long Google Calendar event ID
        String longEventId = "a".repeat(255); // Assuming reasonable length limit
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId(longEventId);
        
        assertEquals(longEventId, scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
        assertEquals(255, scheduleGoogleCalendarEvent.getGoogleCalendarEventId().length());
    }

    @Test
    void testEmptyGoogleCalendarEventId() {
        scheduleGoogleCalendarEvent.setGoogleCalendarEventId("");
        assertEquals("", scheduleGoogleCalendarEvent.getGoogleCalendarEventId());
    }
} 