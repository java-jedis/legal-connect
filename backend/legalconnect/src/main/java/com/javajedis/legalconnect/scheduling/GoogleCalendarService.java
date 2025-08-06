package com.javajedis.legalconnect.scheduling;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.javajedis.legalconnect.common.exception.GoogleCalendarException;
import com.javajedis.legalconnect.scheduling.dto.CreateCalendarEventDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateCalendarEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "LegalConnect";
    private static final String TIME_ZONE = "Asia/Dhaka";
    private static final String CALENDAR_ID = "primary";

    private final OAuthCalendarTokenRepo oAuthCalendarTokenRepo;

    /**
     * Creates a Google Calendar service instance with the user's access token.
     */
    public Calendar getCalendar(String accessToken) throws GoogleCalendarException {
        log.debug("Creating Google Calendar service with access token");

        try {
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(accessToken);

            return new Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create Google Calendar service: {}", e.getMessage());
            throw new GoogleCalendarException("Failed to create Google Calendar service: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the access token for a user from the database.
     */
    public Optional<String> getValidAccessToken(UUID userId) {
        log.debug("Getting access token for user ID: {}", userId);

        Optional<OAuthCalendarToken> tokenOptional = oAuthCalendarTokenRepo.findByUserId(userId);

        if (tokenOptional.isEmpty()) {
            log.debug("No OAuth tokens found for user: {}", userId);
            return Optional.empty();
        }

        OAuthCalendarToken token = tokenOptional.get();

        if (token.getAccessToken() == null || token.getAccessToken().isEmpty()) {
            log.debug("No access token present for user: {}", userId);
            return Optional.empty();
        }

        return Optional.of(token.getAccessToken());
    }

    /**
     * Creates a calendar event for a legal meeting.
     */
    public Event createEvent(CreateCalendarEventDTO eventData) throws GoogleCalendarException {

        log.debug("Creating Google Calendar event: {} for date: {}", eventData.getTitle(), eventData.getDate());

        try {
            Calendar service = getCalendar(eventData.getAccessToken());

            Event event = new Event()
                    .setSummary(eventData.getTitle())
                    .setDescription(eventData.getDescription())
                    .setLocation("Online Meeting - LegalConnect");

            ZonedDateTime startDateTime = ZonedDateTime.of(eventData.getDate(), eventData.getStartTime(), ZoneId.of(TIME_ZONE));
            ZonedDateTime endDateTime = ZonedDateTime.of(eventData.getDate(), eventData.getEndTime(), ZoneId.of(TIME_ZONE));

            DateTime googleStartDateTime = new DateTime(startDateTime.toInstant().toEpochMilli());
            DateTime googleEndDateTime = new DateTime(endDateTime.toInstant().toEpochMilli());

            EventDateTime start = new EventDateTime()
                    .setDateTime(googleStartDateTime);
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(googleEndDateTime);
            event.setEnd(end);

            if (eventData.getAttendeeEmails() != null && !eventData.getAttendeeEmails().isEmpty()) {
                List<EventAttendee> attendees = eventData.getAttendeeEmails().stream()
                        .map(email -> new EventAttendee().setEmail(email))
                        .toList();
                event.setAttendees(attendees);
            }

            EventReminder[] reminderOverrides = new EventReminder[]{
                    new EventReminder().setMethod("email").setMinutes(24 * 60), // 1 day before
                    new EventReminder().setMethod("popup").setMinutes(60),      // 1 hour before
                    new EventReminder().setMethod("popup").setMinutes(15),      // 15 minutes before
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            Event createdEvent = service.events().insert(CALENDAR_ID, event).execute();
            log.info("Successfully created Google Calendar event with ID: {}", createdEvent.getId());

            return createdEvent;
        } catch (GoogleCalendarException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create Google Calendar event: {}", e.getMessage());
            throw new GoogleCalendarException("Failed to create Google Calendar event: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing calendar event.
     */
    public Event updateEvent(UpdateCalendarEventDTO eventData) throws GoogleCalendarException {

        log.debug("Updating Google Calendar event ID: {}", eventData.getEventId());

        try {
            Calendar service = getCalendar(eventData.getAccessToken());
            Event event = service.events().get(CALENDAR_ID, eventData.getEventId()).execute();

            event.setSummary(eventData.getTitle());
            event.setDescription(eventData.getDescription());
            event.setLocation("Online Meeting - LegalConnect");

            ZonedDateTime startDateTime = ZonedDateTime.of(eventData.getDate(), eventData.getStartTime(), ZoneId.of(TIME_ZONE));
            ZonedDateTime endDateTime = ZonedDateTime.of(eventData.getDate(), eventData.getEndTime(), ZoneId.of(TIME_ZONE));

            DateTime googleStartDateTime = new DateTime(startDateTime.toInstant().toEpochMilli());
            DateTime googleEndDateTime = new DateTime(endDateTime.toInstant().toEpochMilli());

            event.setStart(new EventDateTime().setDateTime(googleStartDateTime));
            event.setEnd(new EventDateTime().setDateTime(googleEndDateTime));

            if (eventData.getAttendeeEmails() != null && !eventData.getAttendeeEmails().isEmpty()) {
                List<EventAttendee> attendees = eventData.getAttendeeEmails().stream()
                        .map(email -> new EventAttendee().setEmail(email))
                        .toList();
                event.setAttendees(attendees);
            }

            Event updatedEvent = service.events().update(CALENDAR_ID, eventData.getEventId(), event).execute();
            log.info("Successfully updated Google Calendar event with ID: {}", eventData.getEventId());

            return updatedEvent;
        } catch (GoogleCalendarException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update Google Calendar event {}: {}", eventData.getEventId(), e.getMessage());
            throw new GoogleCalendarException("Failed to update Google Calendar event " + eventData.getEventId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a calendar event.
     */
    public void deleteEvent(String accessToken, String eventId) throws GoogleCalendarException {
        log.debug("Deleting Google Calendar event ID: {}", eventId);

        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }

        Calendar service = getCalendar(accessToken);
        
        try {
            service.events().delete(CALENDAR_ID, eventId).execute();
            log.info("Successfully deleted Google Calendar event with ID: {}", eventId);
        } catch (Exception e) {
            if (e.getMessage() != null && 
                (e.getMessage().contains("Not Found") || 
                 e.getMessage().contains("404") ||
                 e.getMessage().contains("not found"))) {
                log.warn("Google Calendar event {} was already deleted or doesn't exist", eventId);
                return;
            }
            
            log.error("Failed to delete Google Calendar event {}: {}", eventId, e.getMessage());
            throw new GoogleCalendarException("Failed to delete Google Calendar event: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a Google Calendar event exists.
     */
    public boolean eventExists(String accessToken, String eventId) {
        try {
            Calendar service = getCalendar(accessToken);
            Event event = service.events().get(CALENDAR_ID, eventId).execute();
            return event != null;
        } catch (Exception e) {
            log.debug("Event with ID {} does not exist or is not accessible: {}", eventId, e.getMessage());
            return false;
        }
    }
} 