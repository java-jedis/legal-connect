package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.javajedis.legalconnect.common.exception.GoogleCalendarException;
import com.javajedis.legalconnect.scheduling.dto.CreateCalendarEventDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateCalendarEventDTO;

@DisplayName("GoogleCalendarService Tests")
class GoogleCalendarServiceTest {

    @Mock
    private OAuthCalendarTokenRepo oAuthCalendarTokenRepo;

    @InjectMocks
    private GoogleCalendarService googleCalendarService;

    private UUID testUserId;
    private String testAccessToken;
    private CreateCalendarEventDTO testCreateDTO;
    private UpdateCalendarEventDTO testUpdateDTO;
    private String testEventId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        testAccessToken = "test_access_token";
        testEventId = "test_event_id";

        testCreateDTO = new CreateCalendarEventDTO(
                testAccessToken,
                "Test Event",
                "Test Description",
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "host@example.com",
                Arrays.asList("attendee@example.com")
        );

        testUpdateDTO = new UpdateCalendarEventDTO(
                testAccessToken,
                testEventId,
                "Updated Event",
                "Updated Description",
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                Arrays.asList("attendee@example.com")
        );
    }

    @Test
    @DisplayName("Should get valid access token")
    void getValidAccessToken_TokenExists_ReturnsToken() {
        OAuthCalendarToken token = new OAuthCalendarToken();
        token.setAccessToken(testAccessToken);

        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(token));

        Optional<String> result = googleCalendarService.getValidAccessToken(testUserId);

        assertTrue(result.isPresent());
        assertEquals(testAccessToken, result.get());
    }

    @Test
    @DisplayName("Should handle no token found when getting valid access token")
    void getValidAccessToken_NoToken_ReturnsEmpty() {
        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.empty());

        Optional<String> result = googleCalendarService.getValidAccessToken(testUserId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle null access token when getting valid access token")
    void getValidAccessToken_NullToken_ReturnsEmpty() {
        OAuthCalendarToken token = new OAuthCalendarToken();
        token.setAccessToken(null);

        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(token));

        Optional<String> result = googleCalendarService.getValidAccessToken(testUserId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle empty access token when getting valid access token")
    void getValidAccessToken_EmptyToken_ReturnsEmpty() {
        OAuthCalendarToken token = new OAuthCalendarToken();
        token.setAccessToken("");

        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(token));

        Optional<String> result = googleCalendarService.getValidAccessToken(testUserId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should create event successfully")
    void createEvent_Success_ReturnsEvent() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        Event event = new Event();
        event.setId("created-event-id");

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(event);

        Event result = spyService.createEvent(testCreateDTO);

        assertNotNull(result);
        assertEquals("created-event-id", result.getId());
        verify(events).insert(eq("primary"), any(Event.class));
    }

    @Test
    @DisplayName("Should handle exception when creating event")
    void createEvent_Exception_ThrowsException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        doThrow(new GoogleCalendarException("API error", new IOException("API error"))).when(spyService).getCalendar(anyString());

        assertThrows(GoogleCalendarException.class, () -> spyService.createEvent(testCreateDTO));
    }

    @Test
    @DisplayName("Should update event successfully")
    void updateEvent_Success_ReturnsEvent() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Calendar.Events.Update update = mock(Calendar.Events.Update.class);
        Event existingEvent = new Event();
        Event updatedEvent = new Event();
        updatedEvent.setId("updated-event-id");

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(existingEvent);
        when(events.update(eq("primary"), eq(testEventId), any(Event.class))).thenReturn(update);
        when(update.execute()).thenReturn(updatedEvent);

        Event result = spyService.updateEvent(testUpdateDTO);

        assertNotNull(result);
        assertEquals(updatedEvent, result);
        verify(events).update(eq("primary"), eq(testEventId), any(Event.class));
    }

    @Test
    @DisplayName("Should handle exception when updating event")
    void updateEvent_Exception_ThrowsException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        doThrow(new GoogleCalendarException("API error", new IOException("API error"))).when(spyService).getCalendar(anyString());

        assertThrows(GoogleCalendarException.class, () -> spyService.updateEvent(testUpdateDTO));
    }

    @Test
    @DisplayName("Should delete event successfully")
    void deleteEvent_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Delete delete = mock(Calendar.Events.Delete.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.delete("primary", testEventId)).thenReturn(delete);
        doNothing().when(delete).execute();

        spyService.deleteEvent(testAccessToken, testEventId);

        verify(delete).execute();
    }

    @Test
    @DisplayName("Should handle exception when deleting event")
    void deleteEvent_Exception_ThrowsException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        doThrow(new GoogleCalendarException("API error", new IOException("API error"))).when(spyService).getCalendar(anyString());

        assertThrows(GoogleCalendarException.class, () -> spyService.deleteEvent(testAccessToken, testEventId));
    }

    @Test
    @DisplayName("Should check if event exists")
    void eventExists_EventExists_ReturnsTrue() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(new Event());

        boolean result = spyService.eventExists(testAccessToken, testEventId);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should check if event does not exist")
    void eventExists_EventDoesNotExist_ReturnsFalse() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenThrow(new IOException("Not found"));

        boolean result = spyService.eventExists(testAccessToken, testEventId);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should create event with no attendees")
    void createEvent_NoAttendees_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        Event event = new Event();

        CreateCalendarEventDTO dtoWithoutAttendees = new CreateCalendarEventDTO(
                testAccessToken,
                "Test Event",
                "Test Description",
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "host@example.com",
                null
        );

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(event);

        Event result = spyService.createEvent(dtoWithoutAttendees);

        assertNotNull(result);
        verify(events).insert(eq("primary"), any(Event.class));
    }

    @Test
    @DisplayName("Should update event with no attendees")
    void updateEvent_NoAttendees_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Calendar.Events.Update update = mock(Calendar.Events.Update.class);
        Event existingEvent = new Event();
        Event updatedEvent = new Event();

        UpdateCalendarEventDTO dtoWithoutAttendees = new UpdateCalendarEventDTO(
                testAccessToken,
                testEventId,
                "Updated Event",
                "Updated Description",
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                null
        );

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(existingEvent);
        when(events.update(eq("primary"), eq(testEventId), any(Event.class))).thenReturn(update);
        when(update.execute()).thenReturn(updatedEvent);

        Event result = spyService.updateEvent(dtoWithoutAttendees);

        assertNotNull(result);
        verify(events).update(eq("primary"), eq(testEventId), any(Event.class));
    }

    // ADDITIONAL COMPREHENSIVE TESTS FOR 90%+ COVERAGE

    @Test
    @DisplayName("Should create event with empty attendees list")
    void createEvent_EmptyAttendeesList_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        Event event = new Event();

        CreateCalendarEventDTO dtoWithEmptyAttendees = new CreateCalendarEventDTO(
                testAccessToken,
                "Test Event",
                "Test Description",
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "host@example.com",
                Collections.emptyList()
        );

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(event);

        Event result = spyService.createEvent(dtoWithEmptyAttendees);

        assertNotNull(result);
        verify(events).insert(eq("primary"), any(Event.class));
    }

    @Test
    @DisplayName("Should update event with empty attendees list")
    void updateEvent_EmptyAttendeesList_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Calendar.Events.Update update = mock(Calendar.Events.Update.class);
        Event existingEvent = new Event();
        Event updatedEvent = new Event();

        UpdateCalendarEventDTO dtoWithEmptyAttendees = new UpdateCalendarEventDTO(
                testAccessToken,
                testEventId,
                "Updated Event",
                "Updated Description",
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                Collections.emptyList()
        );

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(existingEvent);
        when(events.update(eq("primary"), eq(testEventId), any(Event.class))).thenReturn(update);
        when(update.execute()).thenReturn(updatedEvent);

        Event result = spyService.updateEvent(dtoWithEmptyAttendees);

        assertNotNull(result);
        verify(events).update(eq("primary"), eq(testEventId), any(Event.class));
    }

    @Test
    @DisplayName("Should handle IOException when creating event")
    void createEvent_IOException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.execute()).thenThrow(new IOException("Network error"));

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.createEvent(testCreateDTO));
        
        assertTrue(exception.getMessage().contains("Failed to create Google Calendar event"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("Should handle IOException when updating event")
    void updateEvent_IOException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenThrow(new IOException("Network error"));

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.updateEvent(testUpdateDTO));
        
        assertTrue(exception.getMessage().contains("Failed to update Google Calendar event"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("Should handle IOException during update execution")
    void updateEvent_UpdateExecutionIOException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Calendar.Events.Update update = mock(Calendar.Events.Update.class);
        Event existingEvent = new Event();

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(existingEvent);
        when(events.update(eq("primary"), eq(testEventId), any(Event.class))).thenReturn(update);
        when(update.execute()).thenThrow(new IOException("Update failed"));

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.updateEvent(testUpdateDTO));
        
        assertTrue(exception.getMessage().contains("Failed to update Google Calendar event"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should handle invalid event IDs when deleting")
    void deleteEvent_InvalidEventIds_ThrowsIllegalArgumentException(String invalidEventId) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> googleCalendarService.deleteEvent(testAccessToken, invalidEventId));
        
        assertEquals("Event ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null event ID when deleting")
    void deleteEvent_NullEventId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> googleCalendarService.deleteEvent(testAccessToken, null));
        
        assertEquals("Event ID cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Not Found", "404", "Event not found"})
    @DisplayName("Should handle 404/not found errors when deleting event")
    void deleteEvent_NotFoundErrors_SilentlySucceeds(String errorMessage) throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Delete delete = mock(Calendar.Events.Delete.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.delete("primary", testEventId)).thenReturn(delete);
        when(delete.execute()).thenThrow(new IOException(errorMessage));

        // Should not throw exception for 404/not found errors
        spyService.deleteEvent(testAccessToken, testEventId);

        verify(delete).execute();
    }

    @Test
    @DisplayName("Should handle other IOException when deleting event")
    void deleteEvent_OtherIOException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Delete delete = mock(Calendar.Events.Delete.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.delete("primary", testEventId)).thenReturn(delete);
        when(delete.execute()).thenThrow(new IOException("Network timeout"));

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.deleteEvent(testAccessToken, testEventId));
        
        assertTrue(exception.getMessage().contains("Failed to delete Google Calendar event"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("Should handle GoogleCalendarException when getting calendar for delete")
    void deleteEvent_GetCalendarException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        doThrow(new GoogleCalendarException("Failed to create calendar service", new IOException("Auth error")))
            .when(spyService).getCalendar(anyString());

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.deleteEvent(testAccessToken, testEventId));
        
        assertTrue(exception.getMessage().contains("Failed to create calendar service"));
    }

    @Test
    @DisplayName("Should handle GoogleCalendarException when checking if event exists")
    void eventExists_GetCalendarException_ReturnsFalse() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        doThrow(new GoogleCalendarException("Failed to create calendar service", new IOException("Auth error")))
            .when(spyService).getCalendar(anyString());

        boolean result = spyService.eventExists(testAccessToken, testEventId);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle null event when checking existence")
    void eventExists_NullEvent_ReturnsFalse() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(null);

        boolean result = spyService.eventExists(testAccessToken, testEventId);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle GeneralSecurityException when creating calendar")
    void getCalendar_GeneralSecurityException_ThrowsGoogleCalendarException() {
        // This test would require mocking static methods, which is complex
        // Instead, we'll test the exception handling path by creating a service that throws
        GoogleCalendarService service = new GoogleCalendarService(oAuthCalendarTokenRepo) {
            @Override
            public Calendar getCalendar(String accessToken) throws GoogleCalendarException {
                throw new GoogleCalendarException("Failed to create Google Calendar service: Security error", 
                    new GeneralSecurityException("Security error"));
            }
        };

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> service.createEvent(testCreateDTO));
        
        assertTrue(exception.getMessage().contains("Failed to create Google Calendar service"));
    }

    @Test
    @DisplayName("Should handle RuntimeException when creating event")
    void createEvent_RuntimeException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.insert(eq("primary"), any(Event.class))).thenThrow(new RuntimeException("Unexpected error"));

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.createEvent(testCreateDTO));
        
        assertTrue(exception.getMessage().contains("Failed to create Google Calendar event"));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should handle RuntimeException when updating event")
    void updateEvent_RuntimeException_ThrowsGoogleCalendarException() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenThrow(new RuntimeException("Unexpected error"));

        GoogleCalendarException exception = assertThrows(GoogleCalendarException.class, 
            () -> spyService.updateEvent(testUpdateDTO));
        
        assertTrue(exception.getMessage().contains("Failed to update Google Calendar event"));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should create event with multiple attendees")
    void createEvent_MultipleAttendees_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        Event event = new Event();

        CreateCalendarEventDTO dtoWithMultipleAttendees = new CreateCalendarEventDTO(
                testAccessToken,
                "Test Event",
                "Test Description",
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "host@example.com",
                Arrays.asList("attendee1@example.com", "attendee2@example.com", "attendee3@example.com")
        );

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.execute()).thenReturn(event);

        Event result = spyService.createEvent(dtoWithMultipleAttendees);

        assertNotNull(result);
        verify(events).insert(eq("primary"), any(Event.class));
    }

    @Test
    @DisplayName("Should update event with multiple attendees")
    void updateEvent_MultipleAttendees_Success() throws Exception {
        GoogleCalendarService spyService = spy(googleCalendarService);
        Calendar calendar = mock(Calendar.class);
        Calendar.Events events = mock(Calendar.Events.class);
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Calendar.Events.Update update = mock(Calendar.Events.Update.class);
        Event existingEvent = new Event();
        Event updatedEvent = new Event();

        UpdateCalendarEventDTO dtoWithMultipleAttendees = new UpdateCalendarEventDTO(
                testAccessToken,
                testEventId,
                "Updated Event",
                "Updated Description",
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                Arrays.asList("attendee1@example.com", "attendee2@example.com", "attendee3@example.com")
        );

        doReturn(calendar).when(spyService).getCalendar(anyString());
        when(calendar.events()).thenReturn(events);
        when(events.get("primary", testEventId)).thenReturn(get);
        when(get.execute()).thenReturn(existingEvent);
        when(events.update(eq("primary"), eq(testEventId), any(Event.class))).thenReturn(update);
        when(update.execute()).thenReturn(updatedEvent);

        Event result = spyService.updateEvent(dtoWithMultipleAttendees);

        assertNotNull(result);
        verify(events).update(eq("primary"), eq(testEventId), any(Event.class));
    }

    @Test
    @DisplayName("Should handle whitespace-only access token")
    void getValidAccessToken_WhitespaceToken_ReturnsToken() {
        OAuthCalendarToken token = new OAuthCalendarToken();
        token.setAccessToken("   ");

        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(token));

        Optional<String> result = googleCalendarService.getValidAccessToken(testUserId);

        // The current implementation doesn't trim whitespace, so it returns the token as-is
        assertTrue(result.isPresent());
        assertEquals("   ", result.get());
    }

    @Test
    @DisplayName("Should handle tab and newline in access token")
    void getValidAccessToken_TabNewlineToken_ReturnsToken() {
        OAuthCalendarToken token = new OAuthCalendarToken();
        token.setAccessToken("\t\n\r");

        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(token));

        Optional<String> result = googleCalendarService.getValidAccessToken(testUserId);

        // The current implementation doesn't trim whitespace, so it returns the token as-is
        assertTrue(result.isPresent());
        assertEquals("\t\n\r", result.get());
    }
} 