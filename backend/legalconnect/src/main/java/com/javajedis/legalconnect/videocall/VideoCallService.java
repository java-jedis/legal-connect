package com.javajedis.legalconnect.videocall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.jobscheduler.EmailJobDTO;
import com.javajedis.legalconnect.jobscheduler.JobSchedulerService;
import com.javajedis.legalconnect.jobscheduler.WebPushJobDTO;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.notifications.NotificationType;
import com.javajedis.legalconnect.payment.Payment;
import com.javajedis.legalconnect.payment.PaymentRepo;
import com.javajedis.legalconnect.payment.PaymentService;
import com.javajedis.legalconnect.payment.PaymentStatus;
import com.javajedis.legalconnect.payment.dto.CreatePaymentDTO;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import com.javajedis.legalconnect.videocall.dto.MeetingResponseDTO;
import com.javajedis.legalconnect.videocall.dto.MeetingTokenDTO;
import com.javajedis.legalconnect.videocall.dto.ScheduleMeetingDTO;
import com.javajedis.legalconnect.videocall.dto.UpdateMeetingDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoCallService {

    private static final String NOT_AUTHENTICATED_STRING = "User is not authenticated";
    private static final String LAWYER_NOT_FOUND = "Lawyer not found with the provided ID";
    private static final String CLIENT_NOT_FOUND = "Client not found with the provided ID";
    private static final String LAWYER_PROFILE_NOT_FOUND = "Lawyer profile not found";
    private static final String INVALID_MEETING_TIME = "Meeting end time must be after start time";
    private static final String MEETING_IN_PAST = "Meeting cannot be scheduled in the past";
    private static final String LAWYER_NOT_VERIFIED = "Lawyer is not verified";
    private static final String NO_HOURLY_RATE = "Lawyer has not set an hourly rate";
    private static final String MEETING_NOT_FOUND = "Meeting not found with the provided ID";
    private static final String MEETING_NOT_FOUND_LOG = "Meeting not found with id: {}";
    private static final String NOT_AUTHORIZED_STRING = "You are not authorized to ";
    private static final String MEETING_ALREADY_PAID = "Cannot modify meeting that has already been paid";
    private static final String MEETING_ALREADY_STARTED = "Cannot modify meeting that has already started";
    private static final String PAYMENT_NOT_COMPLETED = "Payment must be completed before joining the meeting";
    private static final String MEETING_NOT_STARTED = "Meeting has not started yet";
    private static final String MEETING_ENDED = "Meeting has already ended";
    private static final String NOTIFICATION_TYPE = "notificationType";
    private static final String CONTENT = "content";
    private static final String EMAIL_TEMPLATE = "notification-email";
    private static final int REMINDER_MINUTES_BEFORE = 5;
    private final MeetingRepo meetingRepo;
    private final UserRepo userRepo;
    private final LawyerRepo lawyerRepo;
    private final PaymentService paymentService;
    private final PaymentRepo paymentRepo;
    private final JitsiJwtGenerator jaasJwtGenerator;
    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final EmailService emailService;
    private final JobSchedulerService jobSchedulerService;

    /**
     * Schedules a new meeting between a client and lawyer, and creates a payment.
     */
    @Transactional
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> scheduleMeeting(@Valid ScheduleMeetingDTO scheduleMeetingDTO) {
        User lawyerUser = GetUserUtil.getAuthenticatedUser(userRepo);

        log.debug("Scheduling meeting '{}' between client: {} and lawyer: {} from {} to {}",
                scheduleMeetingDTO.getTitle(), scheduleMeetingDTO.getEmail(), lawyerUser.getId(),
                scheduleMeetingDTO.getStartDateTime(), scheduleMeetingDTO.getEndDateTime());

        User client = userRepo.findByEmail(scheduleMeetingDTO.getEmail()).orElse(null);
        if (client == null) {
            log.warn("Client not found with email: {}", scheduleMeetingDTO.getEmail());
            return ApiResponse.error(CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        ResponseEntity<ApiResponse<MeetingResponseDTO>> validationResult = validateMeetingData(
                client.getId(), lawyerUser.getId(),
                scheduleMeetingDTO.getStartDateTime(), scheduleMeetingDTO.getEndDateTime());
        if (validationResult != null) {
            return validationResult;
        }

        Lawyer lawyer = lawyerRepo.findByUser(lawyerUser).orElse(null);

        Duration duration = Duration.between(scheduleMeetingDTO.getStartDateTime(), scheduleMeetingDTO.getEndDateTime());
        BigDecimal durationInHours = BigDecimal.valueOf(duration.toMinutes()).divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP);
        BigDecimal totalAmount = lawyer.getHourlyCharge().multiply(durationInHours);

        Meeting meeting = new Meeting();
        meeting.setRoomName(scheduleMeetingDTO.getTitle());
        meeting.setClient(client);
        meeting.setLawyer(lawyerUser);
        meeting.setStartTimestamp(scheduleMeetingDTO.getStartDateTime());
        meeting.setEndTimestamp(scheduleMeetingDTO.getEndDateTime());
        meeting.setPaid(false);

        Meeting savedMeeting = meetingRepo.save(meeting);
        log.info("Meeting '{}' created successfully with id: {} and room: {}", 
                savedMeeting.getRoomName(), savedMeeting.getId(), savedMeeting.getRoomName());

        CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO();
        createPaymentDTO.setPayerId(client.getId());
        createPaymentDTO.setPayeeId(lawyerUser.getId());
        createPaymentDTO.setMeetingId(savedMeeting.getId());
        createPaymentDTO.setAmount(totalAmount);

        boolean paymentCreated = paymentService.createPayment(createPaymentDTO);
        if (!paymentCreated) {
            log.error("Failed to create payment for meeting: {}", savedMeeting.getId());
            return ApiResponse.error("Failed to create payment for the meeting", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        com.javajedis.legalconnect.payment.Payment createdPayment = paymentRepo.findBymeetingId(savedMeeting.getId());
        if (createdPayment != null) {
            savedMeeting.setPayment(createdPayment);
            meetingRepo.save(savedMeeting);
        }

        log.info("Payment created successfully for meeting: {} with amount: {}", savedMeeting.getId(), totalAmount);

        sendMeetingNotifications(savedMeeting, client, lawyerUser, "scheduled", "Meeting Scheduled");

        MeetingResponseDTO responseDTO = mapToMeetingResponseDTO(savedMeeting, client, lawyerUser);
        return ApiResponse.success(responseDTO, HttpStatus.CREATED, "Meeting scheduled successfully");
    }

    /**
     * Updates an existing meeting.
     */
    @Transactional
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> updateMeeting(@Valid UpdateMeetingDTO updateMeetingDTO) {
        log.debug("Updating meeting with id: {} from {} to {}",
                updateMeetingDTO.getMeetingId(), updateMeetingDTO.getStartDateTime(), updateMeetingDTO.getEndDateTime());

        ResponseEntity<ApiResponse<MeetingResponseDTO>> validationResult = validateMeetingAccessForLawyer(updateMeetingDTO.getMeetingId(), "update");
        if (validationResult != null) {
            return validationResult;
        }

        Meeting meeting = meetingRepo.findById(updateMeetingDTO.getMeetingId()).orElse(null);

        if (meeting.isPaid()) {
            log.warn("Attempt to update paid meeting: {}", meeting.getId());
            return ApiResponse.error(MEETING_ALREADY_PAID, HttpStatus.BAD_REQUEST);
        }

        if (meeting.getStartTimestamp().isBefore(OffsetDateTime.now())) {
            log.warn("Attempt to update meeting that has already started: {}", meeting.getId());
            return ApiResponse.error(MEETING_ALREADY_STARTED, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<ApiResponse<MeetingResponseDTO>> timeValidation = validateMeetingTimes(
                updateMeetingDTO.getStartDateTime(), updateMeetingDTO.getEndDateTime());
        if (timeValidation != null) {
            return timeValidation;
        }

        meeting.setStartTimestamp(updateMeetingDTO.getStartDateTime());
        meeting.setEndTimestamp(updateMeetingDTO.getEndDateTime());

        Meeting updatedMeeting = meetingRepo.save(meeting);
        log.info("Meeting updated successfully with id: {}", updatedMeeting.getId());

        // Update payment amount if meeting duration changed
        Lawyer lawyer = lawyerRepo.findByUser(meeting.getLawyer()).orElse(null);
        if (lawyer != null && lawyer.getHourlyCharge() != null) {
            Duration duration = Duration.between(updateMeetingDTO.getStartDateTime(), updateMeetingDTO.getEndDateTime());
            BigDecimal durationInHours = BigDecimal.valueOf(duration.toMinutes()).divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP);
            BigDecimal newAmount = lawyer.getHourlyCharge().multiply(durationInHours);
            
            boolean paymentUpdated = paymentService.updatePaymentAmount(meeting.getId(), newAmount);
            if (paymentUpdated) {
                com.javajedis.legalconnect.payment.Payment updatedPayment = paymentRepo.findBymeetingId(meeting.getId());
                if (updatedPayment != null) {
                    updatedMeeting.setPayment(updatedPayment);
                    meetingRepo.save(updatedMeeting);
                }
                log.info("Payment amount updated for meeting id: {} to amount: {}", meeting.getId(), newAmount);
            } else {
                log.warn("Failed to update payment amount for meeting id: {}", meeting.getId());
            }
        }

        jobSchedulerService.deleteAllJobsForTask(updatedMeeting.getId());

        sendMeetingNotifications(updatedMeeting, updatedMeeting.getClient(), updatedMeeting.getLawyer(), "updated", "Meeting Updated");

        MeetingResponseDTO responseDTO = mapToMeetingResponseDTO(updatedMeeting,
                updatedMeeting.getClient(), updatedMeeting.getLawyer());

        return ApiResponse.success(responseDTO, HttpStatus.OK, "Meeting updated successfully");
    }

    /**
     * Deletes a meeting.
     */
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteMeeting(UUID meetingId) {
        log.debug("Deleting meeting with id: {}", meetingId);

        ResponseEntity<ApiResponse<MeetingResponseDTO>> validationResult = validateMeetingAccessForLawyer(meetingId, "delete");
        if (validationResult != null) {
            return ApiResponse.error(validationResult.getBody().getError().getMessage(), (HttpStatus) validationResult.getStatusCode());
        }

        Meeting meeting = meetingRepo.findById(meetingId).orElse(null);
        if (meeting.isPaid()) {
            log.warn("Attempt to delete paid meeting: {}", meeting.getId());
            return ApiResponse.error(MEETING_ALREADY_PAID, HttpStatus.BAD_REQUEST);
        }

        if (meeting.getStartTimestamp().isBefore(OffsetDateTime.now())) {
            log.warn("Attempt to delete meeting that has already started: {}", meeting.getId());
            return ApiResponse.error(MEETING_ALREADY_STARTED, HttpStatus.BAD_REQUEST);
        }

        sendMeetingCancellationNotifications(meeting, meeting.getClient());

        meeting.setPayment(null);
        meetingRepo.save(meeting);
        
        boolean paymentDeleted = paymentService.deletePaymentByMeetingId(meetingId);
        if (paymentDeleted) {
            log.info("Payment deleted for meeting id: {}", meetingId);
        } else {
            log.warn("Failed to delete payment for meeting id: {}", meetingId);
        }

        jobSchedulerService.deleteAllJobsForTask(meeting.getId());
        meetingRepo.delete(meeting);
        log.info("Meeting deleted successfully with id: {}", meetingId);

        return ApiResponse.success("Meeting deleted successfully", HttpStatus.OK, "Meeting deleted");
    }

    /**
     * Retrieves a specific meeting by ID.
     */
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> getMeeting(UUID meetingId) {
        log.debug("Retrieving meeting with id: {}", meetingId);

        ResponseEntity<ApiResponse<MeetingResponseDTO>> validationResult = validateMeetingAccessForParticipant(meetingId, "view");
        if (validationResult != null) {
            return validationResult;
        }

        Meeting meeting = meetingRepo.findById(meetingId).orElse(null);
        MeetingResponseDTO responseDTO = mapToMeetingResponseDTO(meeting, meeting.getClient(), meeting.getLawyer());

        log.debug("Meeting retrieved successfully with id: {}", meetingId);
        return ApiResponse.success(responseDTO, HttpStatus.OK, "Meeting retrieved successfully");
    }

    /**
     * Retrieves all meetings for the authenticated user.
     */
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getMeetings() {
        log.debug("Retrieving meetings for authenticated user");

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("Unauthenticated user attempted to retrieve meetings");
            return ApiResponse.error(NOT_AUTHENTICATED_STRING, HttpStatus.UNAUTHORIZED);
        }

        List<Meeting> meetings = meetingRepo.findByClientOrLawyer(currentUser);
        List<MeetingResponseDTO> responseDTOs = meetings.stream()
                .map(meeting -> mapToMeetingResponseDTO(meeting, meeting.getClient(), meeting.getLawyer()))
                .toList();

        log.info("Retrieved {} meetings for user: {}", responseDTOs.size(), currentUser.getEmail());
        return ApiResponse.success(responseDTOs, HttpStatus.OK, "Meetings retrieved successfully");
    }

    /**
     * Generates a JWT token for joining a meeting.
     */
    public ResponseEntity<ApiResponse<MeetingTokenDTO>> generateMeetingToken(UUID meetingId) {
        log.debug("Generating meeting token for meeting id: {}", meetingId);

        ResponseEntity<ApiResponse<MeetingResponseDTO>> validationResult = validateMeetingAccessForParticipant(meetingId, "join");
        if (validationResult != null) {
            return ApiResponse.error(validationResult.getBody().getError().getMessage(), (HttpStatus) validationResult.getStatusCode());
        }

        Meeting meeting = meetingRepo.findById(meetingId).orElse(null);
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        OffsetDateTime now = OffsetDateTime.now();
        if (meeting.getStartTimestamp().isAfter(now)) {
            log.warn("Attempt to join meeting that has not started yet: {}", meeting.getId());
            return ApiResponse.error(MEETING_NOT_STARTED, HttpStatus.BAD_REQUEST);
        }

        if (meeting.getEndTimestamp().isBefore(now)) {
            log.warn("Attempt to join meeting that has already ended: {}", meeting.getId());
            return ApiResponse.error(MEETING_ENDED, HttpStatus.BAD_REQUEST);
        }

        if (currentUser.equals(meeting.getClient())) {
            Payment paidPayment = paymentRepo.findBymeetingIdAndStatus(meetingId, PaymentStatus.PAID);
            Payment releasedPayment = paymentRepo.findBymeetingIdAndStatus(meetingId, PaymentStatus.RELEASED);

            if (paidPayment == null && releasedPayment == null) {
                log.warn("Client {} attempted to join meeting {} without valid payment",
                        currentUser.getEmail(), meeting.getId());
                return ApiResponse.error(PAYMENT_NOT_COMPLETED, HttpStatus.PAYMENT_REQUIRED);
            }
        }

        try {
            String jwtToken;
            String userName = currentUser.getFirstName() + " " + currentUser.getLastName();

            if (currentUser.equals(meeting.getLawyer())) {
                // Lawyer gets moderator token
                jwtToken = jaasJwtGenerator.generateLawyerToken(userName, currentUser.getEmail(), meeting.getRoomName());
                log.info("Generated lawyer token (moderator) for user: {} in meeting: {}", currentUser.getEmail(), meeting.getRoomName());
            } else {
                // Client gets client token (but now also has moderator privileges)
                jwtToken = jaasJwtGenerator.generateClientToken(userName, currentUser.getEmail(), meeting.getRoomName());
                log.info("Generated client token (moderator) for user: {} in meeting: {}", currentUser.getEmail(), meeting.getRoomName());
            }
            
            MeetingTokenDTO tokenDTO = new MeetingTokenDTO();
            tokenDTO.setJwtToken(jwtToken);
            tokenDTO.setMeetingId(meetingId);
            tokenDTO.setRoomName(meeting.getRoomName());

            log.info("JWT token generated successfully for user {} in meeting {}",
                    currentUser.getEmail(), meeting.getId());

            return ApiResponse.success(tokenDTO, HttpStatus.OK, "Meeting token generated successfully");

        } catch (Exception e) {
            log.error("Error generating JWT token for meeting {}: {}", meetingId, e.getMessage(), e);
            return ApiResponse.error("Failed to generate meeting token: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sends notifications and schedules reminders for meeting events.
     */
    private void sendMeetingNotifications(Meeting meeting, User client, User lawyer, String action, String notificationType) {
        String creatorName = lawyer.getFirstName() + " " + lawyer.getLastName();

        String subject = "Video Meeting " + notificationType;
        String content = String.format("%s %s a video meeting on %s from %s to %s",
                creatorName,
                action,
                meeting.getStartTimestamp().toLocalDate(),
                meeting.getStartTimestamp().toLocalTime(),
                meeting.getEndTimestamp().toLocalTime());

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put(NOTIFICATION_TYPE, notificationType);
        templateVariables.put(CONTENT, content);

        if (notificationPreferenceService.checkWebPushEnabled(client.getId(), NotificationType.EVENT_ADD)) {
            notificationService.sendNotification(client.getId(), content);
        }

        if (notificationPreferenceService.checkEmailEnabled(client.getId(), NotificationType.EVENT_ADD)) {
            emailService.sendTemplateEmail(
                    client.getEmail(),
                    subject,
                    EMAIL_TEMPLATE,
                    templateVariables
            );
        }

        scheduleMeetingReminders(meeting, client, lawyer, content, subject, templateVariables);
    }

    /**
     * Schedules reminder notifications for meeting participants.
     */
    private void scheduleMeetingReminders(Meeting meeting, User client, User lawyer, String content, String subject, Map<String, Object> templateVariables) {
        OffsetDateTime reminderTime = meeting.getStartTimestamp().minusMinutes(REMINDER_MINUTES_BEFORE);

        if (notificationPreferenceService.checkWebPushEnabled(client.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleWebPushNotification(new WebPushJobDTO(
                    meeting.getId(),
                    client.getId(),
                    content,
                    reminderTime
            ));
        }

        if (notificationPreferenceService.checkWebPushEnabled(lawyer.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleWebPushNotification(new WebPushJobDTO(
                    meeting.getId(),
                    lawyer.getId(),
                    content,
                    reminderTime
            ));
        }

        if (notificationPreferenceService.checkEmailEnabled(client.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleEmailNotification(new EmailJobDTO(
                    meeting.getId(),
                    EMAIL_TEMPLATE,
                    client.getEmail(),
                    subject,
                    templateVariables,
                    reminderTime
            ));
        }

        if (notificationPreferenceService.checkEmailEnabled(lawyer.getId(), NotificationType.SCHEDULE_REMINDER)) {
            jobSchedulerService.scheduleEmailNotification(new EmailJobDTO(
                    meeting.getId(),
                    EMAIL_TEMPLATE,
                    lawyer.getEmail(),
                    subject,
                    templateVariables,
                    reminderTime
            ));
        }
    }

    /**
     * Sends cancellation notifications for meeting deletion.
     */
    private void sendMeetingCancellationNotifications(Meeting meeting, User client) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        String deleterName = currentUser.getFirstName() + " " + currentUser.getLastName();

        String subject = "Video Meeting Cancelled";
        String content = String.format("%s cancelled the video meeting scheduled for %s from %s to %s",
                deleterName,
                meeting.getStartTimestamp().toLocalDate(),
                meeting.getStartTimestamp().toLocalTime(),
                meeting.getEndTimestamp().toLocalTime());

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put(NOTIFICATION_TYPE, "Meeting Cancelled");
        templateVariables.put(CONTENT, content);

        if (notificationPreferenceService.checkWebPushEnabled(client.getId(), NotificationType.EVENT_ADD)) {
            notificationService.sendNotification(client.getId(), content);
        }

        if (notificationPreferenceService.checkEmailEnabled(client.getId(), NotificationType.EVENT_ADD)) {
            emailService.sendTemplateEmail(
                    client.getEmail(),
                    subject,
                    EMAIL_TEMPLATE,
                    templateVariables
            );
        }

    }

    /**
     * Validates meeting data including users, lawyer profile, and times.
     */
    private ResponseEntity<ApiResponse<MeetingResponseDTO>> validateMeetingData(
            UUID clientId, UUID lawyerId, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {

        ResponseEntity<ApiResponse<MeetingResponseDTO>> timeValidation = validateMeetingTimes(startDateTime, endDateTime);
        if (timeValidation != null) {
            return timeValidation;
        }

        User client = userRepo.findById(clientId).orElse(null);
        if (client == null) {
            log.warn("Client not found with id: {}", clientId);
            return ApiResponse.error(CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        User lawyerUser = userRepo.findById(lawyerId).orElse(null);
        if (lawyerUser == null) {
            log.warn("Lawyer user not found with id: {}", lawyerId);
            return ApiResponse.error(LAWYER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Lawyer lawyer = lawyerRepo.findByUser(lawyerUser).orElse(null);
        if (lawyer == null) {
            log.warn("Lawyer profile not found for user: {}", lawyerUser.getEmail());
            return ApiResponse.error(LAWYER_PROFILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (lawyer.getVerificationStatus() != com.javajedis.legalconnect.lawyer.enums.VerificationStatus.APPROVED) {
            log.warn("Attempt to schedule meeting with unverified lawyer: {}", lawyerUser.getEmail());
            return ApiResponse.error(LAWYER_NOT_VERIFIED, HttpStatus.BAD_REQUEST);
        }

        if (lawyer.getHourlyCharge() == null || lawyer.getHourlyCharge().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Lawyer {} has not set a valid hourly rate", lawyerUser.getEmail());
            return ApiResponse.error(NO_HOURLY_RATE, HttpStatus.BAD_REQUEST);
        }

        return null;
    }

    /**
     * Validates meeting start and end times.
     */
    private ResponseEntity<ApiResponse<MeetingResponseDTO>> validateMeetingTimes(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        OffsetDateTime now = OffsetDateTime.now();

        if (startDateTime.isBefore(now)) {
            log.warn("Attempt to schedule meeting in the past: {}", startDateTime);
            return ApiResponse.error(MEETING_IN_PAST, HttpStatus.BAD_REQUEST);
        }

        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            log.warn("Invalid meeting time range: start={}, end={}", startDateTime, endDateTime);
            return ApiResponse.error(INVALID_MEETING_TIME, HttpStatus.BAD_REQUEST);
        }

        return null;
    }

    /**
     * Validates meeting access for lawyer operations (update, delete).
     */
    private ResponseEntity<ApiResponse<MeetingResponseDTO>> validateMeetingAccessForLawyer(UUID meetingId, String operation) {
        Meeting meeting = meetingRepo.findById(meetingId).orElse(null);
        if (meeting == null) {
            log.warn(MEETING_NOT_FOUND_LOG, meetingId);
            return ApiResponse.error(MEETING_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("Unauthenticated user attempted to {} meeting", operation);
            return ApiResponse.error(NOT_AUTHENTICATED_STRING, HttpStatus.UNAUTHORIZED);
        }

        if (!currentUser.equals(meeting.getLawyer())) {
            log.warn("User {} not authorized to {} meeting {}", currentUser.getEmail(), operation, meeting.getId());
            return ApiResponse.error(NOT_AUTHORIZED_STRING + operation + " this meeting", HttpStatus.FORBIDDEN);
        }

        return null;
    }

    /**
     * Validates meeting access for any participant (client or lawyer).
     */
    private ResponseEntity<ApiResponse<MeetingResponseDTO>> validateMeetingAccessForParticipant(UUID meetingId, String operation) {
        Meeting meeting = meetingRepo.findById(meetingId).orElse(null);
        if (meeting == null) {
            log.warn(MEETING_NOT_FOUND_LOG, meetingId);
            return ApiResponse.error(MEETING_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("Unauthenticated user attempted to {} meeting", operation);
            return ApiResponse.error(NOT_AUTHENTICATED_STRING, HttpStatus.UNAUTHORIZED);
        }

        if (!currentUser.equals(meeting.getClient()) && !currentUser.equals(meeting.getLawyer())) {
            log.warn("User {} not authorized to {} meeting {}", currentUser.getEmail(), operation, meeting.getId());
            return ApiResponse.error(NOT_AUTHORIZED_STRING + operation + " this meeting", HttpStatus.FORBIDDEN);
        }

        return null;
    }



    /**
     * Maps a Meeting entity to MeetingResponseDTO.
     */
    private MeetingResponseDTO mapToMeetingResponseDTO(Meeting meeting, User client, User lawyer) {
        MeetingResponseDTO responseDTO = new MeetingResponseDTO();
        responseDTO.setId(meeting.getId());
        responseDTO.setRoomName(meeting.getRoomName());
        responseDTO.setStartTimestamp(meeting.getStartTimestamp());
        responseDTO.setEndTimestamp(meeting.getEndTimestamp());
        responseDTO.setPaid(meeting.isPaid());

        MeetingResponseDTO.UserInfo clientInfo = new MeetingResponseDTO.UserInfo();
        clientInfo.setId(client.getId());
        clientInfo.setFirstName(client.getFirstName());
        clientInfo.setLastName(client.getLastName());
        clientInfo.setEmail(client.getEmail());
        responseDTO.setClient(clientInfo);

        MeetingResponseDTO.UserInfo lawyerInfo = new MeetingResponseDTO.UserInfo();
        lawyerInfo.setId(lawyer.getId());
        lawyerInfo.setFirstName(lawyer.getFirstName());
        lawyerInfo.setLastName(lawyer.getLastName());
        lawyerInfo.setEmail(lawyer.getEmail());
        responseDTO.setLawyer(lawyerInfo);
        
        if (meeting.getPayment() != null) {
            MeetingResponseDTO.PaymentInfo paymentInfo = new MeetingResponseDTO.PaymentInfo();
            paymentInfo.setId(meeting.getPayment().getId());
            paymentInfo.setAmount(meeting.getPayment().getAmount());
            paymentInfo.setStatus(meeting.getPayment().getStatus().toString());
            responseDTO.setPayment(paymentInfo);
        }

        return responseDTO;
    }
}
