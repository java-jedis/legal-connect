package com.javajedis.legalconnect.scheduling;

import com.google.api.services.calendar.model.Event;
import com.javajedis.legalconnect.caseassets.CaseAssetUtility;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.exception.GoogleCalendarException;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.jobscheduler.EmailJobDTO;
import com.javajedis.legalconnect.jobscheduler.JobSchedulerService;
import com.javajedis.legalconnect.jobscheduler.WebPushJobDTO;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.notifications.NotificationType;
import com.javajedis.legalconnect.scheduling.dto.*;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulingService {

    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String SCHEDULE_NOT_FOUND_LOG = "Schedule not found with ID: {}";
    private static final String SCHEDULE_NOT_FOUND_MSG = "Schedule not found";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String NO_VALID_ACCESS_TOKEN_LOG = "No valid access token found for user: {}";
    private static final String NOTIFICATION_TYPE = "notificationType";
    private static final String CONTENT = "content";
    private static final String EMAIIL_TEMPLATE = "notification-email";
    private static final int REMINDER_MINUTES_BEFORE = 1;

    private final ScheduleRepo scheduleRepo;
    private final UserRepo userRepo;
    private final CaseRepo caseRepo;
    private final GoogleCalendarService googleCalendarService;
    private final OAuthService oAuthService;
    private final ScheduleGoogleCalendarEventRepo scheduleGoogleCalendarEventRepo;
    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final EmailService emailService;
    private final JobSchedulerService jobSchedulerService;

    /**
     * Create a new schedule event for a case.
     */
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> createSchedule(CreateScheduleDTO eventData) {
        log.debug("Creating schedule for case ID: {}", eventData.getCaseId());

        CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        eventData.getCaseId(),
                        "create schedule",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        User client = validation.caseEntity().getClient();
        User lawyer = validation.caseEntity().getLawyer().getUser();
        User currentUser = validation.user();

        Schedule schedule = new Schedule();
        schedule.setCaseEntity(validation.caseEntity());
        schedule.setClient(client);
        schedule.setLawyer(lawyer);
        schedule.setTitle(eventData.getTitle());
        schedule.setType(eventData.getType());
        schedule.setDescription(eventData.getDescription());
        schedule.setDate(eventData.getDate());
        schedule.setStartTime(eventData.getStartTime());
        schedule.setEndTime(eventData.getEndTime());

        Schedule savedSchedule = scheduleRepo.save(schedule);

        createGoogleCalendarEvent(savedSchedule, currentUser, client, lawyer);

        User recipient = currentUser.getId().equals(client.getId()) ? lawyer : client;
        UUID recipientId = recipient.getId();
        String creatorName = currentUser.getFirstName() + " " + currentUser.getLastName();

        String subject = "New Schedule Created";
        String content = String.format("%s scheduled '%s' on %s from %s to %s in case: %s",
                creatorName,
                savedSchedule.getTitle(),
                savedSchedule.getDate(),
                savedSchedule.getStartTime(),
                savedSchedule.getEndTime(),
                savedSchedule.getCaseEntity().getTitle());

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put(NOTIFICATION_TYPE, "Schedule Created");
        templateVariables.put(CONTENT, content);

        if (notificationPreferenceService.checkWebPushEnabled(recipientId, NotificationType.EVENT_ADD)) {
            notificationService.sendNotification(recipientId, content);
        }

        if (notificationPreferenceService.checkEmailEnabled(recipientId, NotificationType.EVENT_ADD)) {
            emailService.sendTemplateEmail(
                    recipient.getEmail(),
                    subject,
                    EMAIIL_TEMPLATE,
                    templateVariables
            );
        }

        if (notificationPreferenceService.checkWebPushEnabled(client.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleWebPushNotification(new WebPushJobDTO(savedSchedule.getId(),
                    client.getId(), content,
                    savedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        if (notificationPreferenceService.checkWebPushEnabled(lawyer.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleWebPushNotification(new WebPushJobDTO(savedSchedule.getId(),
                    lawyer.getId(), content,
                    savedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        if (notificationPreferenceService.checkEmailEnabled(client.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleEmailNotification(new EmailJobDTO(savedSchedule.getId(),
                    EMAIIL_TEMPLATE,
                    client.getEmail(),
                    subject,
                    templateVariables,
                    savedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        if (notificationPreferenceService.checkEmailEnabled(lawyer.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleEmailNotification(new EmailJobDTO(savedSchedule.getId(),
                    EMAIIL_TEMPLATE,
                    lawyer.getEmail(),
                    subject,
                    templateVariables,
                    savedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        log.info("Schedule created for case {} by user: {}", eventData.getCaseId(), currentUser.getEmail());

        ScheduleResponseDTO scheduleResponse = mapToScheduleResponseDTO(savedSchedule);
        return ApiResponse.success(scheduleResponse, HttpStatus.CREATED, "Schedule created successfully");
    }

    /**
     * Update an existing schedule event.
     */
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> updateSchedule(UUID scheduleId, UpdateScheduleDTO updateData) {
        log.debug("Updating schedule with ID: {}", scheduleId);

        Schedule existingSchedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (existingSchedule == null) {
            log.warn(SCHEDULE_NOT_FOUND_LOG, scheduleId);
            return ApiResponse.error(SCHEDULE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validation =
                validateCaseAccess(existingSchedule.getCaseEntity().getId(), "update schedule");

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        User client = validation.caseEntity().getClient();
        User lawyer = validation.caseEntity().getLawyer().getUser();

        existingSchedule.setTitle(updateData.getTitle());
        existingSchedule.setType(updateData.getType());
        existingSchedule.setDescription(updateData.getDescription());
        existingSchedule.setDate(updateData.getDate());
        existingSchedule.setStartTime(updateData.getStartTime());
        existingSchedule.setEndTime(updateData.getEndTime());

        updateGoogleCalendarEvent(existingSchedule, validation.user(), client, lawyer);

        Schedule updatedSchedule = scheduleRepo.save(existingSchedule);

        User recipient = validation.user().getId().equals(client.getId()) ? lawyer : client;
        UUID recipientId = recipient.getId();
        String updaterName = validation.user().getFirstName() + " " + validation.user().getLastName();

        String subject = "Schedule Updated";
        String content = String.format("%s changed '%s' to %s from %s to %s in case: %s",
                updaterName,
                updatedSchedule.getTitle(),
                updatedSchedule.getDate(),
                updatedSchedule.getStartTime(),
                updatedSchedule.getEndTime(),
                updatedSchedule.getCaseEntity().getTitle());

        // Create template variables for both immediate and scheduled notifications
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put(NOTIFICATION_TYPE, "Schedule Updated");
        templateVariables.put(CONTENT, content);

        if (notificationPreferenceService.checkWebPushEnabled(recipientId, NotificationType.EVENT_ADD)) {
            notificationService.sendNotification(recipientId, content);
        }

        if (notificationPreferenceService.checkEmailEnabled(recipientId, NotificationType.EVENT_ADD)) {
            emailService.sendTemplateEmail(
                    recipient.getEmail(),
                    subject,
                    EMAIIL_TEMPLATE,
                    templateVariables
            );
        }

        // Delete existing scheduled notifications for this schedule
        jobSchedulerService.deleteAllJobsForTask(updatedSchedule.getId());

        // Schedule new reminder notifications with updated time
        if (notificationPreferenceService.checkWebPushEnabled(client.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleWebPushNotification(new WebPushJobDTO(updatedSchedule.getId(),
                    client.getId(), content,
                    updatedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        if (notificationPreferenceService.checkWebPushEnabled(lawyer.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleWebPushNotification(new WebPushJobDTO(updatedSchedule.getId(),
                    lawyer.getId(), content,
                    updatedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        if (notificationPreferenceService.checkEmailEnabled(client.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleEmailNotification(new EmailJobDTO(updatedSchedule.getId(),
                    EMAIIL_TEMPLATE,
                    client.getEmail(),
                    subject,
                    templateVariables,
                    updatedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        if (notificationPreferenceService.checkEmailEnabled(lawyer.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleEmailNotification(new EmailJobDTO(updatedSchedule.getId(),
                    EMAIIL_TEMPLATE,
                    lawyer.getEmail(),
                    subject,
                    templateVariables,
                    updatedSchedule.getStartTime().minusMinutes(REMINDER_MINUTES_BEFORE)));
        }

        log.info("Schedule {} updated by user: {}", scheduleId, validation.user().getEmail());

        ScheduleResponseDTO scheduleResponse = mapToScheduleResponseDTO(updatedSchedule);
        return ApiResponse.success(scheduleResponse, HttpStatus.OK, "Schedule updated successfully");
    }

    /**
     * Delete a schedule event.
     */
    public ResponseEntity<ApiResponse<String>> deleteSchedule(UUID scheduleId) {
        log.debug("Deleting schedule with ID: {}", scheduleId);

        Schedule existingSchedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (existingSchedule == null) {
            log.warn(SCHEDULE_NOT_FOUND_LOG, scheduleId);
            return ApiResponse.error(SCHEDULE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<String> validation =
                validateCaseAccess(existingSchedule.getCaseEntity().getId(), "delete schedule");

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        // Send notifications before deleting
        User client = existingSchedule.getCaseEntity().getClient();
        User lawyer = existingSchedule.getCaseEntity().getLawyer().getUser();
        User recipient = validation.user().getId().equals(client.getId()) ? lawyer : client;
        UUID recipientId = recipient.getId();
        String deleterName = validation.user().getFirstName() + " " + validation.user().getLastName();

        String subject = "Schedule Cancelled";
        String content = String.format("%s cancelled '%s' scheduled for %s in case: %s",
                deleterName,
                existingSchedule.getTitle(),
                existingSchedule.getDate(),
                existingSchedule.getCaseEntity().getTitle());

        // Create template variables for immediate notifications
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put(NOTIFICATION_TYPE, "Schedule Cancelled");
        templateVariables.put(CONTENT, content);

        if (notificationPreferenceService.checkWebPushEnabled(recipientId, NotificationType.EVENT_ADD)) {
            notificationService.sendNotification(recipientId, content);
        }

        if (notificationPreferenceService.checkEmailEnabled(recipientId, NotificationType.EVENT_ADD)) {
            emailService.sendTemplateEmail(
                    recipient.getEmail(),
                    subject,
                    EMAIIL_TEMPLATE,
                    templateVariables
            );
        }

        // Delete all scheduled notifications for this schedule
        jobSchedulerService.deleteAllJobsForTask(existingSchedule.getId());

        deleteGoogleCalendarEvent(existingSchedule, validation.user());

        scheduleRepo.delete(existingSchedule);
        log.info("Schedule {} deleted by user: {}", scheduleId, validation.user().getEmail());

        return ApiResponse.success("Schedule deleted successfully", HttpStatus.OK, "Schedule deleted successfully");
    }

    /**
     * Get a single schedule by ID.
     */
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> getScheduleById(UUID scheduleId) {
        log.debug("Getting schedule with ID: {}", scheduleId);

        Schedule schedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (schedule == null) {
            log.warn(SCHEDULE_NOT_FOUND_LOG, scheduleId);
            return ApiResponse.error(SCHEDULE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        schedule.getCaseEntity().getId(),
                        "view schedule",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        log.info("Schedule {} retrieved by user: {}", scheduleId, validation.user().getEmail());
        ScheduleResponseDTO scheduleResponse = mapToScheduleResponseDTO(schedule);
        return ApiResponse.success(scheduleResponse, HttpStatus.OK, "Schedule retrieved successfully");
    }

    /**
     * Get all schedules for a case with pagination.
     */
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>> getAllSchedulesForCase(
            UUID caseId, int page, int size, String sortDirection) {
        log.debug("Getting schedules for case ID: {} with page={}, size={}, sort={}", caseId, page, size, sortDirection);

        CaseAssetUtility.CaseAssetValidationResult<ScheduleListResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        caseId,
                        "view case schedules",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Schedule> schedulePage = scheduleRepo.findByCaseEntityId(caseId, pageable);

        List<ScheduleResponseDTO> scheduleResponses = schedulePage.getContent().stream()
                .map(this::mapToScheduleResponseDTO)
                .toList();

        ScheduleListResponseDTO responseData = new ScheduleListResponseDTO(scheduleResponses);

        Map<String, Object> metadata = buildPaginationMetadata(
                schedulePage,
                sortDirection,
                Map.of("caseId", caseId.toString())
        );

        log.info("Retrieved {} schedules for case {} for user: {} (page {}/{})",
                scheduleResponses.size(), caseId, validation.user().getEmail(), page + 1, schedulePage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, "Schedules retrieved successfully", metadata);
    }

    /**
     * Get all schedules for the authenticated user with pagination.
     */
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>> getAllUserSchedules(
            int page, int size, String sortDirection) {
        log.debug("Getting schedules for user with page={}, size={}, sort={}", page, size, sortDirection);

        User user = GetUserUtil.getAuthenticatedUser(userRepo);
        if (user == null) {
            log.warn("Unauthorized schedule list access attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Schedule> schedulePage = scheduleRepo.findByLawyerIdOrClientId(user.getId(), pageable);

        List<ScheduleResponseDTO> scheduleResponses = schedulePage.getContent().stream()
                .map(this::mapToScheduleResponseDTO)
                .toList();

        ScheduleListResponseDTO responseData = new ScheduleListResponseDTO(scheduleResponses);

        Map<String, Object> metadata = buildPaginationMetadata(schedulePage, sortDirection, null);

        log.info("Retrieved {} schedules for user: {} (page {}/{})",
                scheduleResponses.size(), user.getEmail(), page + 1, schedulePage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, "Schedules retrieved successfully", metadata);
    }

    /**
     * Maps a Schedule entity to ScheduleResponseDTO.
     *
     * @param schedule the Schedule entity to map
     * @return the mapped ScheduleResponseDTO
     */
    private ScheduleResponseDTO mapToScheduleResponseDTO(Schedule schedule) {
        ScheduleResponseDTO.CaseSummaryDTO caseSummaryDTO = new ScheduleResponseDTO.CaseSummaryDTO(
                schedule.getCaseEntity().getId(),
                schedule.getCaseEntity().getTitle(),
                schedule.getCaseEntity().getStatus()
        );

        ScheduleResponseDTO.LawyerSummaryDTO lawyerSummaryDTO = new ScheduleResponseDTO.LawyerSummaryDTO(
                schedule.getLawyer().getId(),
                schedule.getLawyer().getFirstName(),
                schedule.getLawyer().getLastName(),
                schedule.getLawyer().getEmail()
        );

        ScheduleResponseDTO.ClientSummaryDTO clientSummaryDTO = new ScheduleResponseDTO.ClientSummaryDTO(
                schedule.getClient().getId(),
                schedule.getClient().getFirstName(),
                schedule.getClient().getLastName(),
                schedule.getClient().getEmail()
        );

        ScheduleResponseDTO scheduleResponseDTO = new ScheduleResponseDTO();
        scheduleResponseDTO.setId(schedule.getId());
        scheduleResponseDTO.setCaseInfo(caseSummaryDTO);
        scheduleResponseDTO.setLawyerInfo(lawyerSummaryDTO);
        scheduleResponseDTO.setClientInfo(clientSummaryDTO);
        scheduleResponseDTO.setTitle(schedule.getTitle());
        scheduleResponseDTO.setType(schedule.getType());
        scheduleResponseDTO.setDescription(schedule.getDescription());
        scheduleResponseDTO.setDate(schedule.getDate());
        scheduleResponseDTO.setStartTime(schedule.getStartTime());
        scheduleResponseDTO.setEndTime(schedule.getEndTime());
        scheduleResponseDTO.setCreatedAt(schedule.getCreatedAt());
        scheduleResponseDTO.setUpdatedAt(schedule.getUpdatedAt());

        return scheduleResponseDTO;
    }

    /**
     * Helper method to validate user access to a schedule through case validation.
     *
     * @param caseId    the case ID to validate access for
     * @param operation the operation being performed (for logging)
     * @return CaseAssetValidationResult containing validation results
     */
    private <T> CaseAssetUtility.CaseAssetValidationResult<T> validateCaseAccess(UUID caseId, String operation) {
        return CaseAssetUtility.validateUserAndCaseAccess(caseId, operation, userRepo, caseRepo);
    }

    /**
     * Helper method to build pagination metadata for schedule list responses.
     *
     * @param schedulePage      the page of schedules
     * @param sortDirection     the sort direction applied
     * @param additionalFilters any additional filters applied
     * @return Map containing pagination metadata
     */
    private Map<String, Object> buildPaginationMetadata(Page<Schedule> schedulePage, String sortDirection, Map<String, String> additionalFilters) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", schedulePage.getTotalElements());
        metadata.put("pageNumber", schedulePage.getNumber());
        metadata.put("pageSize", schedulePage.getSize());
        metadata.put("totalPages", schedulePage.getTotalPages());
        metadata.put("hasNext", schedulePage.hasNext());
        metadata.put("hasPrevious", schedulePage.hasPrevious());
        metadata.put("isFirst", schedulePage.isFirst());
        metadata.put("isLast", schedulePage.isLast());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", CREATED_AT_FIELD);
        metadata.put("appliedFilters", additionalFilters != null ? additionalFilters : Map.of());
        return metadata;
    }

    /**
     * Creates Google Calendar event for a schedule.
     */
    private void createGoogleCalendarEvent(Schedule schedule, User currentUser, User client, User lawyer) {
        log.debug("Attempting to create Google Calendar event for schedule: {}", schedule.getId());

        if (!oAuthService.checkAndRefreshAccessToken()) {
            log.info("User {} does not have Google Calendar integration, skipping calendar event creation",
                    currentUser.getEmail());
            return;
        }

        try {
            Optional<String> accessTokenOpt = validateAndGetAccessToken(currentUser);
            if (accessTokenOpt.isEmpty()) {
                return;
            }

            String accessToken = accessTokenOpt.get();
            String hostEmail = currentUser.getEmail();
            List<String> attendeeEmails = determineAndValidateAttendees(currentUser, client, lawyer, "");

            CreateCalendarEventDTO eventData = new CreateCalendarEventDTO(
                    accessToken,
                    schedule.getTitle(),
                    buildEventDescription(schedule, client, lawyer),
                    schedule.getDate(),
                    schedule.getStartTime().toLocalTime(),
                    schedule.getEndTime().toLocalTime(),
                    hostEmail,
                    attendeeEmails
            );

            Event googleEvent = googleCalendarService.createEvent(eventData);

            ScheduleGoogleCalendarEvent calendarEvent = new ScheduleGoogleCalendarEvent();
            calendarEvent.setSchedule(schedule);
            calendarEvent.setGoogleCalendarEventId(googleEvent.getId());
            scheduleGoogleCalendarEventRepo.save(calendarEvent);

            log.info("Successfully created Google Calendar event {} for schedule {}",
                    googleEvent.getId(), schedule.getId());

        } catch (Exception e) {
            log.error("Failed to create Google Calendar event for schedule {}: {}",
                    schedule.getId(), e.getMessage(), e);
            throw new GoogleCalendarException("Failed to create Google Calendar event for schedule " + schedule.getId(), e);
        }
    }

    /**
     * Updates Google Calendar event for a schedule.
     */
    private void updateGoogleCalendarEvent(Schedule schedule, User currentUser, User client, User lawyer) {
        log.debug("Attempting to update Google Calendar event for schedule: {}", schedule.getId());

        executeGoogleCalendarOperation(schedule, currentUser, "update", (accessToken, googleCalendarEventId) -> {
            try {
                List<String> attendeeEmails = determineAndValidateAttendees(currentUser, client, lawyer, " update");

                UpdateCalendarEventDTO eventData = new UpdateCalendarEventDTO(
                        accessToken,
                        googleCalendarEventId,
                        schedule.getTitle(),
                        buildEventDescription(schedule, client, lawyer),
                        schedule.getDate(),
                        schedule.getStartTime().toLocalTime(),
                        schedule.getEndTime().toLocalTime(),
                        attendeeEmails
                );

                googleCalendarService.updateEvent(eventData);

                log.info("Successfully updated Google Calendar event {} for schedule {}",
                        googleCalendarEventId, schedule.getId());
            } catch (Exception e) {
                throw new GoogleCalendarException("Failed to update Google Calendar event " + googleCalendarEventId + " for schedule " + schedule.getId(), e);
            }
        });
    }

    /**
     * Deletes Google Calendar event for a schedule.
     */
    private void deleteGoogleCalendarEvent(Schedule schedule, User currentUser) {
        log.debug("Attempting to delete Google Calendar event for schedule: {}", schedule.getId());

        executeGoogleCalendarOperation(schedule, currentUser, "delete", (accessToken, googleCalendarEventId) -> {
            try {
                googleCalendarService.deleteEvent(accessToken, googleCalendarEventId);

                scheduleGoogleCalendarEventRepo.deleteByScheduleId(schedule.getId());

                log.info("Successfully deleted Google Calendar event {} for schedule {}",
                        googleCalendarEventId, schedule.getId());
            } catch (Exception e) {
                throw new GoogleCalendarException("Failed to delete Google Calendar event " + googleCalendarEventId + " for schedule " + schedule.getId(), e);
            }
        });
    }

    /**
     * Executes a Google Calendar operation with common validation and error handling.
     */
    private void executeGoogleCalendarOperation(Schedule schedule, User currentUser, String operationType, GoogleCalendarOperation operation) {
        Optional<String> googleCalendarEventIdOpt = scheduleGoogleCalendarEventRepo
                .findGoogleCalendarEventIdByScheduleId(schedule.getId());

        if (googleCalendarEventIdOpt.isEmpty()) {
            log.debug("No Google Calendar event ID found for schedule: {}", schedule.getId());
            return;
        }

        if (!oAuthService.checkAndRefreshAccessToken()) {
            log.info("User {} does not have Google Calendar integration, skipping calendar event {}",
                    currentUser.getEmail(), operationType);
            return;
        }

        try {
            Optional<String> accessTokenOpt = validateAndGetAccessToken(currentUser);
            if (accessTokenOpt.isEmpty()) {
                return;
            }

            String accessToken = accessTokenOpt.get();
            String googleCalendarEventId = googleCalendarEventIdOpt.get();

            operation.execute(accessToken, googleCalendarEventId);

        } catch (GoogleCalendarException e) {
            log.error("Failed to {} Google Calendar event for schedule {}: {}",
                    operationType, schedule.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Validates current user access token and returns it if valid.
     */
    private Optional<String> validateAndGetAccessToken(User currentUser) {
        Optional<String> accessTokenOpt = googleCalendarService.getValidAccessToken(currentUser.getId());
        if (accessTokenOpt.isEmpty()) {
            log.warn(NO_VALID_ACCESS_TOKEN_LOG, currentUser.getEmail());
        }
        return accessTokenOpt;
    }

    /**
     * Determines attendee based on current user role and validates their Google Calendar integration.
     */
    private List<String> determineAndValidateAttendees(User currentUser, User client, User lawyer, String operationSuffix) {
        List<String> attendeeEmails = new ArrayList<>();

        User attendeeUser;
        if (currentUser.getId().equals(client.getId())) {
            attendeeUser = lawyer;
        } else {
            attendeeUser = client;
        }

        Optional<String> attendeeAccessToken = googleCalendarService.getValidAccessToken(attendeeUser.getId());
        if (attendeeAccessToken.isPresent()) {
            attendeeEmails.add(attendeeUser.getEmail());
            log.debug("Adding attendee {} to Google Calendar event{} (has integration)", attendeeUser.getEmail(), operationSuffix);
        } else {
            log.debug("Skipping attendee {} - no Google Calendar integration", attendeeUser.getEmail());
        }

        return attendeeEmails;
    }

    /**
     * Builds event description for Google Calendar.
     */
    private String buildEventDescription(Schedule schedule, User client, User lawyer) {
        StringBuilder description = new StringBuilder();
        description.append("LegalConnect Meeting\n\n");
        description.append("Case: ").append(schedule.getCaseEntity().getTitle()).append("\n");
        description.append("Type: ").append(schedule.getType()).append("\n\n");

        if (schedule.getDescription() != null && !schedule.getDescription().trim().isEmpty()) {
            description.append("Description: ").append(schedule.getDescription()).append("\n\n");
        }

        description.append("Participants:\n");
        description.append("Client: ").append(client.getFirstName()).append(" ").append(client.getLastName())
                .append(" (").append(client.getEmail()).append(")\n");
        description.append("Lawyer: ").append(lawyer.getFirstName()).append(" ").append(lawyer.getLastName())
                .append(" (").append(lawyer.getEmail()).append(")\n\n");
        description.append("Generated by LegalConnect");

        return description.toString();
    }

    /**
     * Functional interface for Google Calendar operations.
     */
    @FunctionalInterface
    private interface GoogleCalendarOperation {
        void execute(String accessToken, String googleCalendarEventId) throws GoogleCalendarException;
    }
}
