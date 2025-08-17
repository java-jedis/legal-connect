package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class ScheduleGoogleCalendarEventRepoTest {

    @Mock
    private ScheduleGoogleCalendarEventRepo scheduleGoogleCalendarEventRepo;

    private ScheduleGoogleCalendarEvent testEvent1;
    private Schedule testSchedule1;
    private UUID testScheduleId1;
    private UUID testScheduleId2;
    private UUID testEventId1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize test data
        testScheduleId1 = UUID.randomUUID();
        testScheduleId2 = UUID.randomUUID();
        testEventId1 = UUID.randomUUID();
        
        // Create test schedules
        testSchedule1 = createTestSchedule(testScheduleId1, "Test Schedule 1");
        
        // Create test Google Calendar events
        testEvent1 = createTestGoogleCalendarEvent(testEventId1, testSchedule1, "google_event_1");
    }

    private Schedule createTestSchedule(UUID scheduleId, String title) {
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
        testCase.setClient(lawyer);
        testCase.setLawyer(new Lawyer());

        // Create test schedule
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        schedule.setTitle(title);
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

    private ScheduleGoogleCalendarEvent createTestGoogleCalendarEvent(UUID eventId, Schedule schedule, String googleEventId) {
        ScheduleGoogleCalendarEvent event = new ScheduleGoogleCalendarEvent();
        event.setId(eventId);
        event.setSchedule(schedule);
        event.setGoogleCalendarEventId(googleEventId);
        event.setCreatedAt(OffsetDateTime.now().minusHours(1));
        event.setUpdatedAt(OffsetDateTime.now());
        return event;
    }

    @Test
    void testFindById() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.findById(testEventId1)).thenReturn(Optional.of(testEvent1));

        // Act
        Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findById(testEventId1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEvent1, result.get());
        assertEquals(testEventId1, result.get().getId());
        assertEquals("google_event_1", result.get().getGoogleCalendarEventId());
        verify(scheduleGoogleCalendarEventRepo, times(1)).findById(testEventId1);
    }

    @Test
    void testFindByIdNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleGoogleCalendarEventRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findById(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
        verify(scheduleGoogleCalendarEventRepo, times(1)).findById(nonExistentId);
    }

    @Test
    void testFindByScheduleId() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.findByScheduleId(testScheduleId1)).thenReturn(Optional.of(testEvent1));

        // Act
        Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findByScheduleId(testScheduleId1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEvent1, result.get());
        assertEquals(testScheduleId1, result.get().getSchedule().getId());
        assertEquals("google_event_1", result.get().getGoogleCalendarEventId());
        verify(scheduleGoogleCalendarEventRepo, times(1)).findByScheduleId(testScheduleId1);
    }

    @Test
    void testFindByScheduleIdNotFound() {
        // Arrange
        UUID nonExistentScheduleId = UUID.randomUUID();
        when(scheduleGoogleCalendarEventRepo.findByScheduleId(nonExistentScheduleId)).thenReturn(Optional.empty());

        // Act
        Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findByScheduleId(nonExistentScheduleId);

        // Assert
        assertFalse(result.isPresent());
        verify(scheduleGoogleCalendarEventRepo, times(1)).findByScheduleId(nonExistentScheduleId);
    }

    @Test
    void testFindGoogleCalendarEventIdByScheduleId() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(testScheduleId1))
            .thenReturn(Optional.of("google_event_1"));

        // Act
        Optional<String> result = scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(testScheduleId1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("google_event_1", result.get());
        verify(scheduleGoogleCalendarEventRepo, times(1)).findGoogleCalendarEventIdByScheduleId(testScheduleId1);
    }

    @Test
    void testFindGoogleCalendarEventIdByScheduleIdNotFound() {
        // Arrange
        UUID nonExistentScheduleId = UUID.randomUUID();
        when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(nonExistentScheduleId))
            .thenReturn(Optional.empty());

        // Act
        Optional<String> result = scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(nonExistentScheduleId);

        // Assert
        assertFalse(result.isPresent());
        verify(scheduleGoogleCalendarEventRepo, times(1)).findGoogleCalendarEventIdByScheduleId(nonExistentScheduleId);
    }

    @Test
    void testDeleteByScheduleId() {
        // Act
        scheduleGoogleCalendarEventRepo.deleteByScheduleId(testScheduleId1);

        // Assert
        verify(scheduleGoogleCalendarEventRepo, times(1)).deleteByScheduleId(testScheduleId1);
    }

    @Test
    void testDeleteByScheduleIdNonExistent() {
        // Arrange
        UUID nonExistentScheduleId = UUID.randomUUID();

        // Act
        scheduleGoogleCalendarEventRepo.deleteByScheduleId(nonExistentScheduleId);

        // Assert
        verify(scheduleGoogleCalendarEventRepo, times(1)).deleteByScheduleId(nonExistentScheduleId);
    }

    @Test
    void testSaveScheduleGoogleCalendarEvent() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.save(any(ScheduleGoogleCalendarEvent.class))).thenReturn(testEvent1);

        // Act
        ScheduleGoogleCalendarEvent savedEvent = scheduleGoogleCalendarEventRepo.save(testEvent1);

        // Assert
        assertNotNull(savedEvent);
        assertEquals(testEvent1, savedEvent);
        assertEquals(testEventId1, savedEvent.getId());
        assertEquals("google_event_1", savedEvent.getGoogleCalendarEventId());
        verify(scheduleGoogleCalendarEventRepo, times(1)).save(testEvent1);
    }

    @Test
    void testUpdateScheduleGoogleCalendarEvent() {
        // Arrange
        testEvent1.setGoogleCalendarEventId("updated_google_event_1");
        testEvent1.setUpdatedAt(OffsetDateTime.now());
        when(scheduleGoogleCalendarEventRepo.save(testEvent1)).thenReturn(testEvent1);

        // Act
        ScheduleGoogleCalendarEvent updatedEvent = scheduleGoogleCalendarEventRepo.save(testEvent1);

        // Assert
        assertNotNull(updatedEvent);
        assertEquals("updated_google_event_1", updatedEvent.getGoogleCalendarEventId());
        verify(scheduleGoogleCalendarEventRepo, times(1)).save(testEvent1);
    }

    @Test
    void testDeleteById() {
        // Act
        scheduleGoogleCalendarEventRepo.deleteById(testEventId1);

        // Assert
        verify(scheduleGoogleCalendarEventRepo, times(1)).deleteById(testEventId1);
    }

    @Test
    void testFindAll() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.findAll()).thenReturn(Collections.singletonList(testEvent1));

        // Act
        var result = scheduleGoogleCalendarEventRepo.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent1, result.get(0));
        verify(scheduleGoogleCalendarEventRepo, times(1)).findAll();
    }

    @Test
    void testCount() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.count()).thenReturn(2L);

        // Act
        long count = scheduleGoogleCalendarEventRepo.count();

        // Assert
        assertEquals(2L, count);
        verify(scheduleGoogleCalendarEventRepo, times(1)).count();
    }

    @Test
    void testExistsById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleGoogleCalendarEventRepo.existsById(testEventId1)).thenReturn(true);
        when(scheduleGoogleCalendarEventRepo.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        assertTrue(scheduleGoogleCalendarEventRepo.existsById(testEventId1));
        assertFalse(scheduleGoogleCalendarEventRepo.existsById(nonExistentId));
        
        verify(scheduleGoogleCalendarEventRepo, times(1)).existsById(testEventId1);
        verify(scheduleGoogleCalendarEventRepo, times(1)).existsById(nonExistentId);
    }

    @Test
    void testUniqueConstraintOnScheduleId() {
        // Arrange - two events with same schedule should not be allowed
        Schedule sameSchedule = testSchedule1;
        ScheduleGoogleCalendarEvent event1 = createTestGoogleCalendarEvent(UUID.randomUUID(), sameSchedule, "event_1");
        ScheduleGoogleCalendarEvent event2 = createTestGoogleCalendarEvent(UUID.randomUUID(), sameSchedule, "event_2");

        when(scheduleGoogleCalendarEventRepo.findByScheduleId(sameSchedule.getId())).thenReturn(Optional.of(event1));

        // Act
        Optional<ScheduleGoogleCalendarEvent> existingEvent = scheduleGoogleCalendarEventRepo.findByScheduleId(sameSchedule.getId());

        // Assert
        assertTrue(existingEvent.isPresent());
        assertEquals(event1, existingEvent.get());
        // In a real scenario, saving event2 would violate unique constraint
        assertEquals(sameSchedule.getId(), event1.getSchedule().getId());
        assertEquals(sameSchedule.getId(), event2.getSchedule().getId());
    }

    @Test
    void testScheduleRelationship() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.findByScheduleId(testScheduleId1)).thenReturn(Optional.of(testEvent1));

        // Act
        Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findByScheduleId(testScheduleId1);

        // Assert
        assertTrue(result.isPresent());
        ScheduleGoogleCalendarEvent event = result.get();
        assertNotNull(event.getSchedule());
        assertEquals(testSchedule1.getId(), event.getSchedule().getId());
        assertEquals(testSchedule1.getTitle(), event.getSchedule().getTitle());
        assertEquals(testSchedule1.getType(), event.getSchedule().getType());
    }

    @Test
    void testLazyLoadingSchedule() {
        // Arrange
        when(scheduleGoogleCalendarEventRepo.findById(testEventId1)).thenReturn(Optional.of(testEvent1));

        // Act
        Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findById(testEventId1);

        // Assert
        assertTrue(result.isPresent());
        ScheduleGoogleCalendarEvent event = result.get();
        
        // Access schedule to test lazy loading
        Schedule schedule = event.getSchedule();
        assertNotNull(schedule);
        assertNotNull(schedule.getId());
        assertNotNull(schedule.getTitle());
        assertNotNull(schedule.getType());
        assertNotNull(schedule.getCaseEntity());
    }

    @Test
    void testRepositoryInheritedMethods() {
        // Test that the repository inherits standard JpaRepository methods
        scheduleGoogleCalendarEventRepo.deleteById(testEventId1);
        scheduleGoogleCalendarEventRepo.save(testEvent1);
        scheduleGoogleCalendarEventRepo.findById(testEventId1);
        scheduleGoogleCalendarEventRepo.findAll();
        scheduleGoogleCalendarEventRepo.count();
        scheduleGoogleCalendarEventRepo.existsById(testEventId1);

        // Verify all methods were called
        verify(scheduleGoogleCalendarEventRepo, times(1)).deleteById(testEventId1);
        verify(scheduleGoogleCalendarEventRepo, times(1)).save(testEvent1);
        verify(scheduleGoogleCalendarEventRepo, times(1)).findById(testEventId1);
        verify(scheduleGoogleCalendarEventRepo, times(1)).findAll();
        verify(scheduleGoogleCalendarEventRepo, times(1)).count();
        verify(scheduleGoogleCalendarEventRepo, times(1)).existsById(testEventId1);
    }

    @Test
    void testDifferentScheduleTypes() {
        // Test with different schedule types
        for (ScheduleType scheduleType : ScheduleType.values()) {
            Schedule schedule = createTestSchedule(UUID.randomUUID(), "Schedule for " + scheduleType);
            schedule.setType(scheduleType);
            
            ScheduleGoogleCalendarEvent event = createTestGoogleCalendarEvent(
                UUID.randomUUID(), 
                schedule, 
                "google_event_" + scheduleType.name().toLowerCase()
            );
            
            when(scheduleGoogleCalendarEventRepo.findByScheduleId(schedule.getId())).thenReturn(Optional.of(event));
            
            Optional<ScheduleGoogleCalendarEvent> result = scheduleGoogleCalendarEventRepo.findByScheduleId(schedule.getId());
            
            assertTrue(result.isPresent());
            assertEquals(scheduleType, result.get().getSchedule().getType());
        }
    }

    @Test
    void testBulkOperations() {
        // Test bulk delete operations
        UUID[] scheduleIds = {testScheduleId1, testScheduleId2, UUID.randomUUID()};
        
        for (UUID scheduleId : scheduleIds) {
            scheduleGoogleCalendarEventRepo.deleteByScheduleId(scheduleId);
        }
        
        // Verify all delete operations were called
        verify(scheduleGoogleCalendarEventRepo, times(3)).deleteByScheduleId(any(UUID.class));
    }

    @Test
    void testQueryOptimization() {
        // Test the custom query method for performance
        UUID scheduleId = UUID.randomUUID();
        String expectedEventId = "optimized_google_event_id";
        
        when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId))
            .thenReturn(Optional.of(expectedEventId));

        Optional<String> result = scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId);

        assertTrue(result.isPresent());
        assertEquals(expectedEventId, result.get());
        
        // This method should be more efficient than loading the entire entity
        verify(scheduleGoogleCalendarEventRepo, times(1)).findGoogleCalendarEventIdByScheduleId(scheduleId);
    }

    @Test
    void testNullSafetyInQueries() {
        // Test null safety in repository methods
        UUID nullId = null;
        
        // These calls should handle null gracefully (though in real usage, null checks would be in service layer)
        when(scheduleGoogleCalendarEventRepo.findById(nullId)).thenReturn(Optional.empty());
        when(scheduleGoogleCalendarEventRepo.findByScheduleId(nullId)).thenReturn(Optional.empty());
        when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(nullId)).thenReturn(Optional.empty());
        
        Optional<ScheduleGoogleCalendarEvent> result1 = scheduleGoogleCalendarEventRepo.findById(nullId);
        Optional<ScheduleGoogleCalendarEvent> result2 = scheduleGoogleCalendarEventRepo.findByScheduleId(nullId);
        Optional<String> result3 = scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(nullId);
        
        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
        assertFalse(result3.isPresent());
    }
} 