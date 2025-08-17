package com.javajedis.legalconnect.videocall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.jobscheduler.EmailJobDTO;
import com.javajedis.legalconnect.jobscheduler.JobSchedulerService;
import com.javajedis.legalconnect.jobscheduler.WebPushJobDTO;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.notifications.NotificationType;
import com.javajedis.legalconnect.payment.Payment;
import com.javajedis.legalconnect.payment.PaymentRepo;
import com.javajedis.legalconnect.payment.PaymentService;
import com.javajedis.legalconnect.payment.PaymentStatus;
import com.javajedis.legalconnect.payment.dto.CreatePaymentDTO;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import com.javajedis.legalconnect.videocall.dto.MeetingResponseDTO;
import com.javajedis.legalconnect.videocall.dto.MeetingTokenDTO;
import com.javajedis.legalconnect.videocall.dto.ScheduleMeetingDTO;
import com.javajedis.legalconnect.videocall.dto.UpdateMeetingDTO;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoCallService Tests")
class VideoCallServiceTest {

    @Mock
    private MeetingRepo meetingRepo;
    
    @Mock
    private UserRepo userRepo;
    
    @Mock
    private LawyerRepo lawyerRepo;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private PaymentRepo paymentRepo;
    
    @Mock
    private JitsiJwtGenerator jaasJwtGenerator;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private NotificationPreferenceService notificationPreferenceService;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private JobSchedulerService jobSchedulerService;

    @InjectMocks
    private VideoCallService videoCallService;

    private User testClient;
    private User testLawyer;
    private Lawyer testLawyerProfile;
    private Meeting testMeeting;
    private Payment testPayment;
    private ScheduleMeetingDTO scheduleMeetingDTO;
    private UpdateMeetingDTO updateMeetingDTO;

    @BeforeEach
    void setUp() {
        // Create test client
        testClient = new User();
        testClient.setId(UUID.randomUUID());
        testClient.setEmail("client@test.com");
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setRole(Role.USER);
        testClient.setEmailVerified(true);

        // Create test lawyer user
        testLawyer = new User();
        testLawyer.setId(UUID.randomUUID());
        testLawyer.setEmail("lawyer@test.com");
        testLawyer.setFirstName("Jane");
        testLawyer.setLastName("Smith");
        testLawyer.setRole(Role.LAWYER);
        testLawyer.setEmailVerified(true);

        // Create test lawyer profile
        testLawyerProfile = new Lawyer();
        testLawyerProfile.setId(UUID.randomUUID());
        testLawyerProfile.setUser(testLawyer);
        testLawyerProfile.setVerificationStatus(VerificationStatus.APPROVED);
        testLawyerProfile.setHourlyCharge(new BigDecimal("100.00"));

        // Create test meeting
        testMeeting = new Meeting();
        testMeeting.setId(UUID.randomUUID());
        testMeeting.setRoomName("Test Meeting Room");
        testMeeting.setClient(testClient);
        testMeeting.setLawyer(testLawyer);
        testMeeting.setStartTimestamp(OffsetDateTime.now().plusHours(1));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(2));
        testMeeting.setPaid(false);

        // Create test payment
        testPayment = new Payment();
        testPayment.setId(UUID.randomUUID());
        testPayment.setPayer(testClient);
        testPayment.setPayee(testLawyer);
        testPayment.setMeetingId(testMeeting.getId());
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setCreatedAt(OffsetDateTime.now());

        testMeeting.setPayment(testPayment);

        // Create test DTOs
        scheduleMeetingDTO = new ScheduleMeetingDTO();
        scheduleMeetingDTO.setTitle("Test Meeting");
        scheduleMeetingDTO.setEmail("client@test.com");
        scheduleMeetingDTO.setStartDateTime(OffsetDateTime.now().plusHours(1));
        scheduleMeetingDTO.setEndDateTime(OffsetDateTime.now().plusHours(2));

        updateMeetingDTO = new UpdateMeetingDTO();
        updateMeetingDTO.setMeetingId(testMeeting.getId());
        updateMeetingDTO.setStartDateTime(OffsetDateTime.now().plusHours(2));
        updateMeetingDTO.setEndDateTime(OffsetDateTime.now().plusHours(3));
    }

    // ========== Meeting Scheduling Tests ==========

    @Test
    @DisplayName("Should schedule meeting successfully with valid data")
    void scheduleMeeting_ValidData_Success() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenReturn(true);
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(testPayment);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(true);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting scheduled successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            
            verify(meetingRepo, times(2)).save(any(Meeting.class)); // Once for initial save, once for payment association
            verify(paymentService, times(1)).createPayment(any(CreatePaymentDTO.class));
            verify(notificationService, times(1)).sendNotification(eq(testClient.getId()), anyString());
            verify(emailService, times(1)).sendTemplateEmail(eq(testClient.getEmail()), anyString(), eq("notification-email"), any(Map.class));
        }
    }

    @Test
    @DisplayName("Should return not found when client email does not exist")
    void scheduleMeeting_ClientNotFound_ReturnsNotFound() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Client not found with the provided ID", response.getBody().getError().getMessage());
            
            verify(meetingRepo, never()).save(any(Meeting.class));
            verify(paymentService, never()).createPayment(any(CreatePaymentDTO.class));
        }
    }

    @Test
    @DisplayName("Should return bad request when meeting is scheduled in the past")
    void scheduleMeeting_MeetingInPast_ReturnsBadRequest() {
        // Arrange
        scheduleMeetingDTO.setStartDateTime(OffsetDateTime.now().minusHours(1));
        scheduleMeetingDTO.setEndDateTime(OffsetDateTime.now().plusHours(1));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting cannot be scheduled in the past", response.getBody().getError().getMessage());
            
            verify(meetingRepo, never()).save(any(Meeting.class));
        }
    }

    @Test
    @DisplayName("Should return bad request when end time is before start time")
    void scheduleMeeting_InvalidTimeRange_ReturnsBadRequest() {
        // Arrange
        scheduleMeetingDTO.setStartDateTime(OffsetDateTime.now().plusHours(2));
        scheduleMeetingDTO.setEndDateTime(OffsetDateTime.now().plusHours(1));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting end time must be after start time", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when lawyer is not verified")
    void scheduleMeeting_LawyerNotVerified_ReturnsBadRequest() {
        // Arrange
        testLawyerProfile.setVerificationStatus(VerificationStatus.PENDING);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Lawyer is not verified", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when lawyer has no hourly rate")
    void scheduleMeeting_NoHourlyRate_ReturnsBadRequest() {
        // Arrange
        testLawyerProfile.setHourlyCharge(null);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Lawyer has not set an hourly rate", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return internal server error when payment creation fails")
    void scheduleMeeting_PaymentCreationFails_ReturnsInternalServerError() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Failed to create payment for the meeting", response.getBody().getError().getMessage());
        }
    }

    // ========== Meeting Update Tests ==========

    @Test
    @DisplayName("Should update meeting successfully with valid data")
    void updateMeeting_ValidData_Success() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(paymentService.updatePaymentAmount(any(UUID.class), any(BigDecimal.class))).thenReturn(true);
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(testPayment);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(true);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting updated successfully", response.getBody().getMessage());
            
            verify(meetingRepo, times(2)).save(any(Meeting.class)); // Once for meeting update, once for payment association
            verify(paymentService, times(1)).updatePaymentAmount(any(UUID.class), any(BigDecimal.class));
            verify(jobSchedulerService, times(1)).deleteAllJobsForTask(testMeeting.getId());
        }
    }

    @Test
    @DisplayName("Should return not found when meeting does not exist for update")
    void updateMeeting_MeetingNotFound_ReturnsNotFound() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting not found with the provided ID", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return forbidden when user is not the lawyer for update")
    void updateMeeting_UserNotAuthorized_ReturnsForbidden() {
        // Arrange
        User unauthorizedUser = new User();
        unauthorizedUser.setId(UUID.randomUUID());
        unauthorizedUser.setEmail("unauthorized@test.com");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(unauthorizedUser);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("You are not authorized to update this meeting", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when trying to update paid meeting")
    void updateMeeting_MeetingAlreadyPaid_ReturnsBadRequest() {
        // Arrange
        testMeeting.setPaid(true);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Cannot modify meeting that has already been paid", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when trying to update meeting that has started")
    void updateMeeting_MeetingAlreadyStarted_ReturnsBadRequest() {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusHours(1));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Cannot modify meeting that has already started", response.getBody().getError().getMessage());
        }
    }

    // ========== Meeting Deletion Tests ==========

    @Test
    @DisplayName("Should delete meeting successfully with valid data")
    void deleteMeeting_ValidData_Success() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(paymentService.deletePaymentByMeetingId(testMeeting.getId())).thenReturn(true);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(true);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<String>> response = videoCallService.deleteMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting deleted", response.getBody().getMessage());
            assertEquals("Meeting deleted successfully", response.getBody().getData());
            
            verify(meetingRepo, times(1)).save(any(Meeting.class)); // To set payment to null
            verify(meetingRepo, times(1)).delete(testMeeting);
            verify(paymentService, times(1)).deletePaymentByMeetingId(testMeeting.getId());
            verify(jobSchedulerService, times(1)).deleteAllJobsForTask(testMeeting.getId());
        }
    }

    @Test
    @DisplayName("Should return not found when meeting does not exist for deletion")
    void deleteMeeting_MeetingNotFound_ReturnsNotFound() {
        // Arrange
        UUID nonExistentMeetingId = UUID.randomUUID();
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(nonExistentMeetingId)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<String>> response = videoCallService.deleteMeeting(nonExistentMeetingId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting not found with the provided ID", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when trying to delete paid meeting")
    void deleteMeeting_MeetingAlreadyPaid_ReturnsBadRequest() {
        // Arrange
        testMeeting.setPaid(true);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<String>> response = videoCallService.deleteMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Cannot modify meeting that has already been paid", response.getBody().getError().getMessage());
        }
    }

    // ========== Meeting Retrieval Tests ==========

    @Test
    @DisplayName("Should retrieve meeting successfully with valid ID")
    void getMeeting_ValidMeetingId_Success() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.getMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting retrieved successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(testMeeting.getId(), response.getBody().getData().getId());
        }
    }

    @Test
    @DisplayName("Should return not found when meeting does not exist for retrieval")
    void getMeeting_MeetingNotFound_ReturnsNotFound() {
        // Arrange
        UUID nonExistentMeetingId = UUID.randomUUID();
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(nonExistentMeetingId)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.getMeeting(nonExistentMeetingId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting not found with the provided ID", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return forbidden when user is not participant for retrieval")
    void getMeeting_UserNotParticipant_ReturnsForbidden() {
        // Arrange
        User unauthorizedUser = new User();
        unauthorizedUser.setId(UUID.randomUUID());
        unauthorizedUser.setEmail("unauthorized@test.com");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(unauthorizedUser);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.getMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("You are not authorized to view this meeting", response.getBody().getError().getMessage());
        }
    }

    // ========== Meeting List Retrieval Tests ==========

    @Test
    @DisplayName("Should retrieve all meetings for authenticated user successfully")
    void getMeetings_AuthenticatedUser_Success() {
        // Arrange
        List<Meeting> meetings = List.of(testMeeting);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findByClientOrLawyer(testClient)).thenReturn(meetings);

            // Act
            ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> response = videoCallService.getMeetings();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meetings retrieved successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(1, response.getBody().getData().size());
            assertEquals(testMeeting.getId(), response.getBody().getData().get(0).getId());
        }
    }

    @Test
    @DisplayName("Should return unauthorized when user not authenticated for meeting list")
    void getMeetings_UserNotAuthenticated_ReturnsUnauthorized() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> response = videoCallService.getMeetings();

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("User is not authenticated", response.getBody().getError().getMessage());
        }
    }

    // ========== Meeting Token Generation Tests ==========

    @Test
    @DisplayName("Should generate meeting token successfully for lawyer")
    void generateMeetingToken_LawyerUser_Success() throws Exception {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusMinutes(5));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(1));
        String expectedToken = "test-jwt-token";
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(jaasJwtGenerator.generateLawyerToken(anyString(), anyString(), anyString())).thenReturn(expectedToken);

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting token generated successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(expectedToken, response.getBody().getData().getJwtToken());
            assertEquals(testMeeting.getId(), response.getBody().getData().getMeetingId());
            assertEquals(testMeeting.getRoomName(), response.getBody().getData().getRoomName());
        }
    }

    @Test
    @DisplayName("Should generate meeting token successfully for client with valid payment")
    void generateMeetingToken_ClientUserWithValidPayment_Success() throws Exception {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusMinutes(5));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(1));
        String expectedToken = "test-jwt-token";
        Payment paidPayment = new Payment();
        paidPayment.setStatus(PaymentStatus.PAID);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(paymentRepo.findBymeetingIdAndStatus(testMeeting.getId(), PaymentStatus.PAID)).thenReturn(paidPayment);
            when(paymentRepo.findBymeetingIdAndStatus(testMeeting.getId(), PaymentStatus.RELEASED)).thenReturn(null);
            when(jaasJwtGenerator.generateClientToken(anyString(), anyString(), anyString())).thenReturn(expectedToken);

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting token generated successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(expectedToken, response.getBody().getData().getJwtToken());
        }
    }

    @Test
    @DisplayName("Should return payment required when client has no valid payment")
    void generateMeetingToken_ClientWithoutValidPayment_ReturnsPaymentRequired() {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusMinutes(5));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(1));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(paymentRepo.findBymeetingIdAndStatus(testMeeting.getId(), PaymentStatus.PAID)).thenReturn(null);
            when(paymentRepo.findBymeetingIdAndStatus(testMeeting.getId(), PaymentStatus.RELEASED)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Payment must be completed before joining the meeting", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when meeting has not started yet")
    void generateMeetingToken_MeetingNotStarted_ReturnsBadRequest() {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().plusHours(1));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(2));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting has not started yet", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when meeting has already ended")
    void generateMeetingToken_MeetingEnded_ReturnsBadRequest() {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusHours(2));
        testMeeting.setEndTimestamp(OffsetDateTime.now().minusHours(1));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting has already ended", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return internal server error when JWT generation fails")
    void generateMeetingToken_JwtGenerationFails_ReturnsInternalServerError() throws Exception {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusMinutes(5));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(1));
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(jaasJwtGenerator.generateLawyerToken(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("JWT generation failed"));

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getError().getMessage().contains("Failed to generate meeting token"));
        }
    }

    // ========== Validation Tests ==========

    @ParameterizedTest
    @EnumSource(value = VerificationStatus.class, names = {"PENDING", "REJECTED"})
    @DisplayName("Should return bad request for non-approved lawyer verification status")
    void scheduleMeeting_NonApprovedLawyerStatus_ReturnsBadRequest(VerificationStatus status) {
        // Arrange
        testLawyerProfile.setVerificationStatus(status);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Lawyer is not verified", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return not found when lawyer profile does not exist")
    void scheduleMeeting_LawyerProfileNotFound_ReturnsNotFound() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Lawyer profile not found", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when hourly charge is zero or negative")
    void scheduleMeeting_ZeroOrNegativeHourlyCharge_ReturnsBadRequest() {
        // Arrange
        testLawyerProfile.setHourlyCharge(BigDecimal.ZERO);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Lawyer has not set an hourly rate", response.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return bad request when start and end times are equal")
    void scheduleMeeting_EqualStartEndTimes_ReturnsBadRequest() {
        // Arrange
        OffsetDateTime sameTime = OffsetDateTime.now().plusHours(1);
        scheduleMeetingDTO.setStartDateTime(sameTime);
        scheduleMeetingDTO.setEndDateTime(sameTime);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting end time must be after start time", response.getBody().getError().getMessage());
        }
    }

    // ========== Notification Tests ==========

    @Test
    @DisplayName("Should send notifications when web push and email are enabled")
    void scheduleMeeting_NotificationsEnabled_SendsNotifications() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenReturn(true);
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(testPayment);
            when(notificationPreferenceService.checkWebPushEnabled(testClient.getId(), NotificationType.EVENT_ADD)).thenReturn(true);
            when(notificationPreferenceService.checkEmailEnabled(testClient.getId(), NotificationType.EVENT_ADD)).thenReturn(true);
            when(notificationPreferenceService.checkWebPushEnabled(testClient.getId(), NotificationType.SCHEDULE_REMINDER)).thenReturn(true);
            when(notificationPreferenceService.checkWebPushEnabled(testLawyer.getId(), NotificationType.SCHEDULE_REMINDER)).thenReturn(true);
            when(notificationPreferenceService.checkEmailEnabled(testClient.getId(), NotificationType.SCHEDULE_REMINDER)).thenReturn(true);
            when(notificationPreferenceService.checkEmailEnabled(testLawyer.getId(), NotificationType.SCHEDULE_REMINDER)).thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            
            // Verify immediate notifications
            verify(notificationService, times(1)).sendNotification(eq(testClient.getId()), anyString());
            verify(emailService, times(1)).sendTemplateEmail(eq(testClient.getEmail()), anyString(), eq("notification-email"), any(Map.class));
            
            // Verify scheduled reminder notifications
            verify(jobSchedulerService, times(2)).scheduleWebPushNotification(any(WebPushJobDTO.class)); // Client and lawyer
            verify(jobSchedulerService, times(2)).scheduleEmailNotification(any(EmailJobDTO.class)); // Client and lawyer
        }
    }

    @Test
    @DisplayName("Should not send notifications when preferences are disabled")
    void scheduleMeeting_NotificationsDisabled_DoesNotSendNotifications() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenReturn(true);
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(testPayment);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            
            // Verify no notifications are sent
            verify(notificationService, never()).sendNotification(any(UUID.class), anyString());
            verify(emailService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), any(Map.class));
            verify(jobSchedulerService, never()).scheduleWebPushNotification(any(WebPushJobDTO.class));
            verify(jobSchedulerService, never()).scheduleEmailNotification(any(EmailJobDTO.class));
        }
    }

    // ========== DTO Mapping Tests (tested indirectly through public methods) ==========

    @Test
    @DisplayName("Should map meeting entity to response DTO correctly through getMeeting")
    void getMeeting_ValidMeeting_MapsAllFieldsCorrectly() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.getMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            MeetingResponseDTO result = response.getBody().getData();
            
            assertNotNull(result);
            assertEquals(testMeeting.getId(), result.getId());
            assertEquals(testMeeting.getRoomName(), result.getRoomName());
            assertEquals(testMeeting.getStartTimestamp(), result.getStartTimestamp());
            assertEquals(testMeeting.getEndTimestamp(), result.getEndTimestamp());
            assertEquals(testMeeting.isPaid(), result.isPaid());
            
            // Verify client info
            assertNotNull(result.getClient());
            assertEquals(testClient.getId(), result.getClient().getId());
            assertEquals(testClient.getFirstName(), result.getClient().getFirstName());
            assertEquals(testClient.getLastName(), result.getClient().getLastName());
            assertEquals(testClient.getEmail(), result.getClient().getEmail());
            
            // Verify lawyer info
            assertNotNull(result.getLawyer());
            assertEquals(testLawyer.getId(), result.getLawyer().getId());
            assertEquals(testLawyer.getFirstName(), result.getLawyer().getFirstName());
            assertEquals(testLawyer.getLastName(), result.getLawyer().getLastName());
            assertEquals(testLawyer.getEmail(), result.getLawyer().getEmail());
            
            // Verify payment info
            assertNotNull(result.getPayment());
            assertEquals(testPayment.getId(), result.getPayment().getId());
            assertEquals(testPayment.getAmount(), result.getPayment().getAmount());
            assertEquals(testPayment.getStatus().toString(), result.getPayment().getStatus());
        }
    }

    @Test
    @DisplayName("Should map meeting entity without payment correctly through getMeeting")
    void getMeeting_MeetingWithoutPayment_MapsCorrectly() {
        // Arrange
        testMeeting.setPayment(null);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.getMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            MeetingResponseDTO result = response.getBody().getData();
            
            assertNotNull(result);
            assertEquals(testMeeting.getId(), result.getId());
            assertEquals(testMeeting.getRoomName(), result.getRoomName());
            assertEquals(testMeeting.getStartTimestamp(), result.getStartTimestamp());
            assertEquals(testMeeting.getEndTimestamp(), result.getEndTimestamp());
            assertEquals(testMeeting.isPaid(), result.isPaid());
            
            // Verify payment info is null
            assertEquals(null, result.getPayment());
        }
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Should handle empty meeting list correctly")
    void getMeetings_EmptyMeetingList_ReturnsEmptyList() {
        // Arrange
        List<Meeting> emptyMeetings = List.of();
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findByClientOrLawyer(testClient)).thenReturn(emptyMeetings);

            // Act
            ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> response = videoCallService.getMeetings();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meetings retrieved successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(0, response.getBody().getData().size());
        }
    }

    @Test
    @DisplayName("Should handle payment update failure gracefully")
    void updateMeeting_PaymentUpdateFails_ContinuesSuccessfully() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(paymentService.updatePaymentAmount(any(UUID.class), any(BigDecimal.class))).thenReturn(false);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting updated successfully", response.getBody().getMessage());
            
            verify(paymentService, times(1)).updatePaymentAmount(any(UUID.class), any(BigDecimal.class));
            verify(paymentRepo, never()).findBymeetingId(any(UUID.class)); // Should not try to find payment if update failed
        }
    }

    @Test
    @DisplayName("Should handle payment deletion failure gracefully")
    void deleteMeeting_PaymentDeletionFails_ContinuesSuccessfully() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(paymentService.deletePaymentByMeetingId(testMeeting.getId())).thenReturn(false);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<String>> response = videoCallService.deleteMeeting(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting deleted", response.getBody().getMessage());
            
            verify(paymentService, times(1)).deletePaymentByMeetingId(testMeeting.getId());
            verify(meetingRepo, times(1)).delete(testMeeting);
        }
    }

    @Test
    @DisplayName("Should calculate meeting duration and payment amount correctly")
    void scheduleMeeting_ValidDuration_CalculatesAmountCorrectly() {
        // Arrange
        // Set up a 2 hour meeting (120 minutes) to avoid rounding issues
        OffsetDateTime startTime = OffsetDateTime.now().plusHours(1);
        OffsetDateTime endTime = startTime.plusMinutes(120);
        scheduleMeetingDTO.setStartDateTime(startTime);
        scheduleMeetingDTO.setEndDateTime(endTime);
        
        // Expected amount: 100.00 * 2.0 = 200.00
        BigDecimal expectedAmount = new BigDecimal("200.00");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenAnswer(invocation -> {
                CreatePaymentDTO dto = invocation.getArgument(0);
                // Verify the calculated amount matches expected value
                BigDecimal actualAmount = dto.getAmount();
                assertEquals(expectedAmount, actualAmount, "Payment amount should match calculated duration * hourly rate");
                return true;
            });
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(testPayment);
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(paymentService, times(1)).createPayment(any(CreatePaymentDTO.class));
        }
    }

    @Test
    @DisplayName("Should handle null payment when scheduling meeting")
    void scheduleMeeting_NullPaymentReturned_ContinuesSuccessfully() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(userRepo.findByEmail(scheduleMeetingDTO.getEmail())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testClient.getId())).thenReturn(Optional.of(testClient));
            when(userRepo.findById(testLawyer.getId())).thenReturn(Optional.of(testLawyer));
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(paymentService.createPayment(any(CreatePaymentDTO.class))).thenReturn(true);
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(null); // Return null payment
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.scheduleMeeting(scheduleMeetingDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting scheduled successfully", response.getBody().getMessage());
            
            verify(meetingRepo, times(1)).save(any(Meeting.class)); // Only called once since payment is null
            verify(paymentService, times(1)).createPayment(any(CreatePaymentDTO.class));
        }
    }

    @Test
    @DisplayName("Should handle null lawyer profile when updating meeting")
    void updateMeeting_NullLawyerProfile_ContinuesSuccessfully() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.empty()); // Return empty lawyer profile
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting updated successfully", response.getBody().getMessage());
            
            verify(meetingRepo, times(1)).save(any(Meeting.class)); // Only called once since no payment update
            verify(paymentService, never()).updatePaymentAmount(any(UUID.class), any(BigDecimal.class));
            verify(jobSchedulerService, times(1)).deleteAllJobsForTask(testMeeting.getId());
        }
    }

    @Test
    @DisplayName("Should handle null hourly charge when updating meeting")
    void updateMeeting_NullHourlyCharge_ContinuesSuccessfully() {
        // Arrange
        testLawyerProfile.setHourlyCharge(null); // Set hourly charge to null
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting updated successfully", response.getBody().getMessage());
            
            verify(meetingRepo, times(1)).save(any(Meeting.class)); // Only called once since no payment update
            verify(paymentService, never()).updatePaymentAmount(any(UUID.class), any(BigDecimal.class));
            verify(jobSchedulerService, times(1)).deleteAllJobsForTask(testMeeting.getId());
        }
    }

    @Test
    @DisplayName("Should handle null updated payment when updating meeting")
    void updateMeeting_NullUpdatedPayment_ContinuesSuccessfully() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testLawyer);
            when(meetingRepo.findById(updateMeetingDTO.getMeetingId())).thenReturn(Optional.of(testMeeting));
            when(meetingRepo.save(any(Meeting.class))).thenReturn(testMeeting);
            when(lawyerRepo.findByUser(testLawyer)).thenReturn(Optional.of(testLawyerProfile));
            when(paymentService.updatePaymentAmount(any(UUID.class), any(BigDecimal.class))).thenReturn(true);
            when(paymentRepo.findBymeetingId(testMeeting.getId())).thenReturn(null); // Return null payment
            when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);
            when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), any(NotificationType.class))).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<MeetingResponseDTO>> response = videoCallService.updateMeeting(updateMeetingDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting updated successfully", response.getBody().getMessage());
            
            verify(meetingRepo, times(1)).save(any(Meeting.class)); // Only called once since payment is null
            verify(paymentService, times(1)).updatePaymentAmount(any(UUID.class), any(BigDecimal.class));
            verify(jobSchedulerService, times(1)).deleteAllJobsForTask(testMeeting.getId());
        }
    }

    @Test
    @DisplayName("Should generate meeting token successfully for client with released payment")
    void generateMeetingToken_ClientUserWithReleasedPayment_Success() throws Exception {
        // Arrange
        testMeeting.setStartTimestamp(OffsetDateTime.now().minusMinutes(5));
        testMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(1));
        String expectedToken = "test-jwt-token";
        Payment releasedPayment = new Payment();
        releasedPayment.setStatus(PaymentStatus.RELEASED);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testClient);
            when(meetingRepo.findById(testMeeting.getId())).thenReturn(Optional.of(testMeeting));
            when(paymentRepo.findBymeetingIdAndStatus(testMeeting.getId(), PaymentStatus.PAID)).thenReturn(null);
            when(paymentRepo.findBymeetingIdAndStatus(testMeeting.getId(), PaymentStatus.RELEASED)).thenReturn(releasedPayment);
            when(jaasJwtGenerator.generateClientToken(anyString(), anyString(), anyString())).thenReturn(expectedToken);

            // Act
            ResponseEntity<ApiResponse<MeetingTokenDTO>> response = videoCallService.generateMeetingToken(testMeeting.getId());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Meeting token generated successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(expectedToken, response.getBody().getData().getJwtToken());
        }
    }
}