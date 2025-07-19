package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.api.services.calendar.model.Event;
import com.javajedis.legalconnect.caseassets.CaseAssetUtility;
import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.exception.GoogleCalendarException;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.scheduling.dto.CreateCalendarEventDTO;
import com.javajedis.legalconnect.scheduling.dto.CreateScheduleDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleListResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateCalendarEventDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateScheduleDTO;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

/**
 * Comprehensive unit tests for SchedulingService.
 * Tests all service methods with various scenarios including success cases, error handling,
 * and Google Calendar integration.
 */
@DisplayName("SchedulingService Tests")
class SchedulingServiceTest {

    @Mock
    private ScheduleRepo scheduleRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CaseRepo caseRepo;

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private OAuthService oAuthService;

    @Mock
    private ScheduleGoogleCalendarEventRepo scheduleGoogleCalendarEventRepo;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferenceService notificationPreferenceService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SchedulingService schedulingService;

    // Test data
    private User lawyerUser;
    private User clientUser;
    private Lawyer lawyer;
    private Case testCase;
    private Schedule testSchedule;
    private CreateScheduleDTO createScheduleDTO;
    private UpdateScheduleDTO updateScheduleDTO;
    private UUID scheduleId;
    private UUID caseId;
    private UUID lawyerId;
    private UUID clientUserId;
    private Event mockGoogleEvent;
    private ScheduleGoogleCalendarEvent mockScheduleGoogleCalendarEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        setupTestUsers();
        setupTestLawyer();
        setupTestCase();
        setupTestSchedule();
        setupTestDTOs();
        setupGoogleCalendarMocks();
        
        // Default Google Calendar integration mocks (simulate no integration)
        when(oAuthService.checkAndRefreshAccessToken()).thenReturn(false);
        when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(any(UUID.class)))
                .thenReturn(Optional.empty());
        when(googleCalendarService.getValidAccessToken(any(UUID.class)))
                .thenReturn(Optional.empty());
    }

    private void setupTestUsers() {
        // Setup lawyer user
        lawyerId = UUID.randomUUID();
        lawyerUser = new User();
        lawyerUser.setId(lawyerId);
        lawyerUser.setFirstName("John");
        lawyerUser.setLastName("Doe");
        lawyerUser.setEmail("lawyer@example.com");
        lawyerUser.setRole(Role.LAWYER);
        lawyerUser.setEmailVerified(true);
        lawyerUser.setCreatedAt(OffsetDateTime.now());
        lawyerUser.setUpdatedAt(OffsetDateTime.now());

        // Setup client user
        clientUserId = UUID.randomUUID();
        clientUser = new User();
        clientUser.setId(clientUserId);
        clientUser.setFirstName("Jane");
        clientUser.setLastName("Smith");
        clientUser.setEmail("client@example.com");
        clientUser.setRole(Role.USER);
        clientUser.setEmailVerified(true);
        clientUser.setCreatedAt(OffsetDateTime.now());
        clientUser.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestLawyer() {
        lawyer = new Lawyer();
        lawyer.setId(UUID.randomUUID());
        lawyer.setUser(lawyerUser);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyer.setDivision(Division.DHAKA);
        lawyer.setDistrict(District.DHAKA);
        lawyer.setBio("Experienced lawyer specializing in corporate law");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);
        lawyer.setCreatedAt(OffsetDateTime.now());
        lawyer.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestCase() {
        caseId = UUID.randomUUID();
        testCase = new Case();
        testCase.setId(caseId);
        testCase.setTitle("Test Case Title");
        testCase.setDescription("Test case description");
        testCase.setStatus(CaseStatus.IN_PROGRESS);
        testCase.setLawyer(lawyer);
        testCase.setClient(clientUser);
        testCase.setCreatedAt(OffsetDateTime.now());
        testCase.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestSchedule() {
        scheduleId = UUID.randomUUID();
        testSchedule = new Schedule();
        testSchedule.setId(scheduleId);
        testSchedule.setCaseEntity(testCase);
        testSchedule.setLawyer(lawyerUser);
        testSchedule.setClient(clientUser);
        testSchedule.setTitle("Initial Consultation");
        testSchedule.setType(ScheduleType.MEETING);
        testSchedule.setDescription("First meeting with client");
        testSchedule.setDate(LocalDate.now().plusDays(1));
        testSchedule.setStartTime(OffsetDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0));
        testSchedule.setEndTime(OffsetDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0));
        testSchedule.setCreatedAt(OffsetDateTime.now());
        testSchedule.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestDTOs() {
        createScheduleDTO = new CreateScheduleDTO();
        createScheduleDTO.setCaseId(caseId);
        createScheduleDTO.setTitle("Initial Consultation");
        createScheduleDTO.setType(ScheduleType.MEETING);
        createScheduleDTO.setDescription("First meeting with client");
        createScheduleDTO.setDate(LocalDate.now().plusDays(1));
        createScheduleDTO.setStartTime(OffsetDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0));
        createScheduleDTO.setEndTime(OffsetDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0));

        updateScheduleDTO = new UpdateScheduleDTO();
        updateScheduleDTO.setTitle("Updated Consultation");
        updateScheduleDTO.setType(ScheduleType.COURT_HEARING);
        updateScheduleDTO.setDescription("Updated meeting description");
        updateScheduleDTO.setDate(LocalDate.now().plusDays(2));
        updateScheduleDTO.setStartTime(OffsetDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0));
        updateScheduleDTO.setEndTime(OffsetDateTime.now().withHour(15).withMinute(0).withSecond(0).withNano(0));
    }

    private void setupGoogleCalendarMocks() {
        mockGoogleEvent = new Event();
        mockGoogleEvent.setId("google-event-id-123");
        mockGoogleEvent.setSummary("Test Event");

        mockScheduleGoogleCalendarEvent = new ScheduleGoogleCalendarEvent();
        mockScheduleGoogleCalendarEvent.setId(UUID.randomUUID());
        mockScheduleGoogleCalendarEvent.setSchedule(testSchedule);
        mockScheduleGoogleCalendarEvent.setGoogleCalendarEventId("google-event-id-123");
    }

    @Test
    @DisplayName("Should create schedule successfully")
    void createSchedule_Success_ReturnsCreatedResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule created successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(testSchedule.getTitle(), result.getBody().getData().getTitle());
            assertEquals(testSchedule.getType(), result.getBody().getData().getType());
            assertEquals(testSchedule.getDescription(), result.getBody().getData().getDescription());
            assertEquals(testSchedule.getDate(), result.getBody().getData().getDate());
            assertEquals(testSchedule.getStartTime(), result.getBody().getData().getStartTime());
            assertEquals(testSchedule.getEndTime(), result.getBody().getData().getEndTime());
            
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService).checkAndRefreshAccessToken();
        }
    }

    @Test
    @DisplayName("Should create schedule with Google Calendar integration successfully")
    void createSchedule_WithGoogleCalendar_Success_ReturnsCreatedResponse() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(googleCalendarService.createEvent(any(CreateCalendarEventDTO.class))).thenReturn(mockGoogleEvent);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule created successfully", result.getBody().getMessage());
            
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(googleCalendarService).createEvent(any(CreateCalendarEventDTO.class));
            verify(scheduleGoogleCalendarEventRepo).save(any(ScheduleGoogleCalendarEvent.class));
        }
    }

    @Test
    @DisplayName("Should handle Google Calendar creation failure gracefully")
    void createSchedule_GoogleCalendarFails_ThrowsGoogleCalendarException() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(googleCalendarService.createEvent(any(CreateCalendarEventDTO.class)))
                    .thenThrow(new GoogleCalendarException("Google Calendar API error"));

            // Act & Assert
            assertThrows(GoogleCalendarException.class, () -> {
                schedulingService.createSchedule(createScheduleDTO);
            });
            
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(googleCalendarService).createEvent(any(CreateCalendarEventDTO.class));
            verify(scheduleGoogleCalendarEventRepo, never()).save(any(ScheduleGoogleCalendarEvent.class));
        }
    }

    @Test
    @DisplayName("Should handle create schedule validation error")
    void createSchedule_ValidationError_ReturnsErrorResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            ApiResponse<ScheduleResponseDTO> errorResponse = ApiResponse.<ScheduleResponseDTO>error(
                    "Case not found", HttpStatus.NOT_FOUND
            ).getBody();
            
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(null, null, ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case not found", result.getBody().getError().getMessage());
            
            verify(scheduleRepo, never()).save(any(Schedule.class));
            verify(oAuthService, never()).checkAndRefreshAccessToken();
        }
    }

    @Test
    @DisplayName("Should update schedule successfully")
    void updateSchedule_Success_ReturnsUpdatedResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("update schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

            Schedule updatedSchedule = new Schedule();
            updatedSchedule.setId(scheduleId);
            updatedSchedule.setCaseEntity(testCase);
            updatedSchedule.setLawyer(lawyerUser);
            updatedSchedule.setClient(clientUser);
            updatedSchedule.setTitle("Updated Consultation");
            updatedSchedule.setType(ScheduleType.COURT_HEARING);
            updatedSchedule.setDescription("Updated meeting description");
            updatedSchedule.setDate(LocalDate.now().plusDays(2));
            updatedSchedule.setStartTime(OffsetDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0));
            updatedSchedule.setEndTime(OffsetDateTime.now().withHour(15).withMinute(0).withSecond(0).withNano(0));
            updatedSchedule.setCreatedAt(OffsetDateTime.now());
            updatedSchedule.setUpdatedAt(OffsetDateTime.now());

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(updatedSchedule);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.updateSchedule(scheduleId, updateScheduleDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule updated successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals("Updated Consultation", result.getBody().getData().getTitle());
            assertEquals(ScheduleType.COURT_HEARING, result.getBody().getData().getType());
            assertEquals("Updated meeting description", result.getBody().getData().getDescription());
            
            verify(scheduleRepo).findById(scheduleId);
            verify(scheduleRepo).save(any(Schedule.class));
        }
    }

    @Test
    @DisplayName("Should update schedule with Google Calendar integration successfully")
    void updateSchedule_WithGoogleCalendar_Success_ReturnsUpdatedResponse() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("update schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId))
                    .thenReturn(Optional.of("google-event-id-123"));
            when(googleCalendarService.updateEvent(any(UpdateCalendarEventDTO.class))).thenReturn(mockGoogleEvent);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.updateSchedule(scheduleId, updateScheduleDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule updated successfully", result.getBody().getMessage());
            
            verify(scheduleRepo).findById(scheduleId);
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(scheduleGoogleCalendarEventRepo).findGoogleCalendarEventIdByScheduleId(scheduleId);
            verify(googleCalendarService).updateEvent(any(UpdateCalendarEventDTO.class));
        }
    }

    @Test
    @DisplayName("Should handle Google Calendar update failure gracefully")
    void updateSchedule_GoogleCalendarFails_ThrowsGoogleCalendarException() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("update schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId))
                    .thenReturn(Optional.of("google-event-id-123"));
            when(googleCalendarService.updateEvent(any(UpdateCalendarEventDTO.class)))
                    .thenThrow(new GoogleCalendarException("Google Calendar API error"));

            // Act & Assert
            assertThrows(GoogleCalendarException.class, () -> {
                schedulingService.updateSchedule(scheduleId, updateScheduleDTO);
            });
            
            verify(scheduleRepo).findById(scheduleId);
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(scheduleGoogleCalendarEventRepo).findGoogleCalendarEventIdByScheduleId(scheduleId);
            verify(googleCalendarService).updateEvent(any(UpdateCalendarEventDTO.class));
            verify(scheduleRepo, never()).save(any(Schedule.class));
        }
    }

    @Test
    @DisplayName("Should handle update schedule not found")
    void updateSchedule_NotFound_ReturnsNotFoundResponse() {
        // Arrange
        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.updateSchedule(scheduleId, updateScheduleDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule not found", result.getBody().getError().getMessage());
        
        verify(scheduleRepo).findById(scheduleId);
        verify(oAuthService, never()).checkAndRefreshAccessToken();
    }

    @Test
    @DisplayName("Should handle update schedule validation error")
    void updateSchedule_ValidationError_ReturnsErrorResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            
            ApiResponse<ScheduleResponseDTO> errorResponse = ApiResponse.<ScheduleResponseDTO>error(
                    "Access denied", HttpStatus.FORBIDDEN
            ).getBody();
            
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(null, null, ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("update schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.updateSchedule(scheduleId, updateScheduleDTO);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Access denied", result.getBody().getError().getMessage());
            
            verify(scheduleRepo).findById(scheduleId);
            verify(oAuthService, never()).checkAndRefreshAccessToken();
        }
    }

    @Test
    @DisplayName("Should delete schedule successfully")
    void deleteSchedule_Success_ReturnsSuccessResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            
            CaseAssetUtility.CaseAssetValidationResult<String> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("delete schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<String>> result = schedulingService.deleteSchedule(scheduleId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule deleted successfully", result.getBody().getMessage());
            assertEquals("Schedule deleted successfully", result.getBody().getData());
            
            verify(scheduleRepo).findById(scheduleId);
            verify(scheduleRepo).delete(testSchedule);
        }
    }

    @Test
    @DisplayName("Should delete schedule with Google Calendar integration successfully")
    void deleteSchedule_WithGoogleCalendar_Success_ReturnsSuccessResponse() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            
            CaseAssetUtility.CaseAssetValidationResult<String> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("delete schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId))
                    .thenReturn(Optional.of("google-event-id-123"));
            doNothing().when(googleCalendarService).deleteEvent("valid-access-token", "google-event-id-123");

            // Act
            ResponseEntity<ApiResponse<String>> result = schedulingService.deleteSchedule(scheduleId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule deleted successfully", result.getBody().getMessage());
            
            verify(scheduleRepo).findById(scheduleId);
            verify(scheduleRepo).delete(testSchedule);
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(scheduleGoogleCalendarEventRepo).findGoogleCalendarEventIdByScheduleId(scheduleId);
            verify(googleCalendarService).deleteEvent("valid-access-token", "google-event-id-123");
        }
    }

    @Test
    @DisplayName("Should handle Google Calendar delete failure gracefully")
    void deleteSchedule_GoogleCalendarFails_ThrowsGoogleCalendarException() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            
            CaseAssetUtility.CaseAssetValidationResult<String> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("delete schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId))
                    .thenReturn(Optional.of("google-event-id-123"));
            doThrow(new GoogleCalendarException("Google Calendar API error"))
                    .when(googleCalendarService).deleteEvent("valid-access-token", "google-event-id-123");

            // Act & Assert
            assertThrows(GoogleCalendarException.class, () -> {
                schedulingService.deleteSchedule(scheduleId);
            });
            
            verify(scheduleRepo).findById(scheduleId);
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(scheduleGoogleCalendarEventRepo).findGoogleCalendarEventIdByScheduleId(scheduleId);
            verify(googleCalendarService).deleteEvent("valid-access-token", "google-event-id-123");
            verify(scheduleRepo, never()).delete(any(Schedule.class));
        }
    }

    @Test
    @DisplayName("Should handle delete schedule not found")
    void deleteSchedule_NotFound_ReturnsNotFoundResponse() {
        // Arrange
        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<String>> result = schedulingService.deleteSchedule(scheduleId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule not found", result.getBody().getError().getMessage());
        
        verify(scheduleRepo).findById(scheduleId);
        verify(oAuthService, never()).checkAndRefreshAccessToken();
    }

    @Test
    @DisplayName("Should handle OAuth token refresh failure gracefully")
    void createSchedule_OAuthTokenRefreshFails_SkipsGoogleCalendar() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule created successfully", result.getBody().getMessage());
            
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService, never()).getValidAccessToken(any(UUID.class));
            verify(googleCalendarService, never()).createEvent(any(CreateCalendarEventDTO.class));
        }
    }

    @Test
    @DisplayName("Should handle missing access token gracefully")
    void createSchedule_MissingAccessToken_SkipsGoogleCalendar() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule created successfully", result.getBody().getMessage());
            
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService).checkAndRefreshAccessToken();
            verify(googleCalendarService).getValidAccessToken(lawyerId);
            verify(googleCalendarService, never()).createEvent(any(CreateCalendarEventDTO.class));
        }
    }

    @Test
    @DisplayName("Should handle missing Google Calendar event ID for update")
    void updateSchedule_MissingGoogleCalendarEventId_SkipsGoogleCalendar() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("update schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(scheduleGoogleCalendarEventRepo.findGoogleCalendarEventIdByScheduleId(scheduleId))
                    .thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.updateSchedule(scheduleId, updateScheduleDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule updated successfully", result.getBody().getMessage());
            
            verify(scheduleRepo).findById(scheduleId);
            verify(scheduleRepo).save(any(Schedule.class));
            verify(oAuthService, never()).checkAndRefreshAccessToken();
            verify(googleCalendarService, never()).getValidAccessToken(lawyerId);
            verify(scheduleGoogleCalendarEventRepo).findGoogleCalendarEventIdByScheduleId(scheduleId);
            verify(googleCalendarService, never()).updateEvent(any(UpdateCalendarEventDTO.class));
        }
    }

    @Test
    @DisplayName("Should get schedule by ID successfully")
    void getScheduleById_Success_ReturnsScheduleResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("view schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.getScheduleById(scheduleId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedule retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(testSchedule.getId(), result.getBody().getData().getId());
            assertEquals(testSchedule.getTitle(), result.getBody().getData().getTitle());
            assertEquals(testSchedule.getType(), result.getBody().getData().getType());
            
            verify(scheduleRepo).findById(scheduleId);
        }
    }

    @Test
    @DisplayName("Should handle get schedule by ID not found")
    void getScheduleById_NotFound_ReturnsNotFoundResponse() {
        // Arrange
        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.getScheduleById(scheduleId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule not found", result.getBody().getError().getMessage());
        
        verify(scheduleRepo).findById(scheduleId);
    }

    @Test
    @DisplayName("Should get all schedules for case successfully")
    void getAllSchedulesForCase_Success_ReturnsScheduleListResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleListResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("view case schedules"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            List<Schedule> schedules = Arrays.asList(testSchedule);
            Page<Schedule> schedulePage = new PageImpl<>(schedules, PageRequest.of(0, 10), 1);
            
            when(scheduleRepo.findByCaseEntityId(eq(caseId), any(Pageable.class))).thenReturn(schedulePage);

            // Act
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(1, result.getBody().getData().getSchedules().size());
            
            // Check metadata
            assertNotNull(result.getBody().getMetadata());
            assertEquals(1L, result.getBody().getMetadata().get("totalCount"));
            assertEquals(0, result.getBody().getMetadata().get("pageNumber"));
            assertEquals(10, result.getBody().getMetadata().get("pageSize"));
            assertEquals(1, result.getBody().getMetadata().get("totalPages"));
            assertEquals("DESC", result.getBody().getMetadata().get("sortDirection"));
            assertEquals("createdAt", result.getBody().getMetadata().get("sortField"));
            
            verify(scheduleRepo).findByCaseEntityId(eq(caseId), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("Should handle get all schedules for case validation error")
    void getAllSchedulesForCase_ValidationError_ReturnsErrorResponse() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            ApiResponse<ScheduleListResponseDTO> errorResponse = ApiResponse.<ScheduleListResponseDTO>error(
                    "Case not found", HttpStatus.NOT_FOUND
            ).getBody();
            
            CaseAssetUtility.CaseAssetValidationResult<ScheduleListResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(null, null, ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("view case schedules"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case not found", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should get all user schedules successfully")
    void getAllUserSchedules_Success_ReturnsScheduleListResponse() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            List<Schedule> schedules = Arrays.asList(testSchedule);
            Page<Schedule> schedulePage = new PageImpl<>(schedules, PageRequest.of(0, 10), 1);
            
            when(scheduleRepo.findByLawyerIdOrClientId(eq(lawyerId), any(Pageable.class))).thenReturn(schedulePage);

            // Act
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllUserSchedules(0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(1, result.getBody().getData().getSchedules().size());
            
            // Check metadata
            assertNotNull(result.getBody().getMetadata());
            assertEquals(1L, result.getBody().getMetadata().get("totalCount"));
            assertEquals(0, result.getBody().getMetadata().get("pageNumber"));
            assertEquals(10, result.getBody().getMetadata().get("pageSize"));
            assertEquals("DESC", result.getBody().getMetadata().get("sortDirection"));
            
            verify(scheduleRepo).findByLawyerIdOrClientId(eq(lawyerId), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("Should handle get all user schedules unauthorized")
    void getAllUserSchedules_Unauthorized_ReturnsUnauthorizedResponse() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllUserSchedules(0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should handle pagination with different sort directions")
    void testPaginationWithDifferentSortDirections() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            List<Schedule> schedules = Arrays.asList(testSchedule);
            Page<Schedule> schedulePage = new PageImpl<>(schedules, PageRequest.of(1, 5), 10);
            
            when(scheduleRepo.findByLawyerIdOrClientId(eq(lawyerId), any(Pageable.class))).thenReturn(schedulePage);

            // Act - Test ASC sort
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllUserSchedules(1, 5, "ASC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
            
            // Check metadata
            assertNotNull(result.getBody().getMetadata());
            assertEquals(10L, result.getBody().getMetadata().get("totalCount"));
            assertEquals(1, result.getBody().getMetadata().get("pageNumber"));
            assertEquals(5, result.getBody().getMetadata().get("pageSize"));
            assertEquals(2, result.getBody().getMetadata().get("totalPages"));
            assertEquals("ASC", result.getBody().getMetadata().get("sortDirection"));
            assertFalse((Boolean) result.getBody().getMetadata().get("hasNext"));
            assertTrue((Boolean) result.getBody().getMetadata().get("hasPrevious"));
            
            verify(scheduleRepo).findByLawyerIdOrClientId(eq(lawyerId), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("Should handle empty schedule list")
    void testEmptyScheduleList() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            List<Schedule> schedules = Arrays.asList();
            Page<Schedule> schedulePage = new PageImpl<>(schedules, PageRequest.of(0, 10), 0);
            
            when(scheduleRepo.findByLawyerIdOrClientId(eq(lawyerId), any(Pageable.class))).thenReturn(schedulePage);

            // Act
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllUserSchedules(0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(0, result.getBody().getData().getSchedules().size());
            
            // Check metadata
            assertNotNull(result.getBody().getMetadata());
            assertEquals(0L, result.getBody().getMetadata().get("totalCount"));
            assertEquals(0, result.getBody().getMetadata().get("pageNumber"));
            assertEquals(0, result.getBody().getMetadata().get("totalPages"));
            
            verify(scheduleRepo).findByLawyerIdOrClientId(eq(lawyerId), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("Should test schedule response DTO mapping")
    void testScheduleResponseDTOMapping() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("view schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.getScheduleById(scheduleId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            
            ScheduleResponseDTO scheduleResponse = result.getBody().getData();
            assertNotNull(scheduleResponse);
            
            // Test case info mapping
            assertNotNull(scheduleResponse.getCaseInfo());
            assertEquals(testCase.getId(), scheduleResponse.getCaseInfo().getId());
            assertEquals(testCase.getTitle(), scheduleResponse.getCaseInfo().getTitle());
            assertEquals(testCase.getStatus(), scheduleResponse.getCaseInfo().getStatus());
            
            // Test lawyer info mapping
            assertNotNull(scheduleResponse.getLawyerInfo());
            assertEquals(lawyerUser.getId(), scheduleResponse.getLawyerInfo().getId());
            assertEquals(lawyerUser.getEmail(), scheduleResponse.getLawyerInfo().getEmail());
            
            // Test client info mapping
            assertNotNull(scheduleResponse.getClientInfo());
            assertEquals(clientUser.getId(), scheduleResponse.getClientInfo().getId());
            assertEquals(clientUser.getEmail(), scheduleResponse.getClientInfo().getEmail());
            
            // Test core schedule fields
            assertEquals(testSchedule.getId(), scheduleResponse.getId());
            assertEquals(testSchedule.getTitle(), scheduleResponse.getTitle());
            assertEquals(testSchedule.getType(), scheduleResponse.getType());
            assertEquals(testSchedule.getDescription(), scheduleResponse.getDescription());
            assertEquals(testSchedule.getDate(), scheduleResponse.getDate());
            assertEquals(testSchedule.getStartTime(), scheduleResponse.getStartTime());
            assertEquals(testSchedule.getEndTime(), scheduleResponse.getEndTime());
            assertEquals(testSchedule.getCreatedAt(), scheduleResponse.getCreatedAt());
        }
    }

    @Test
    @DisplayName("Should test pagination metadata with additional filters")
    void testPaginationMetadataWithFilters() {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleListResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("view case schedules"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            List<Schedule> schedules = Arrays.asList(testSchedule);
            Page<Schedule> schedulePage = new PageImpl<>(schedules, PageRequest.of(0, 10), 1);
            
            when(scheduleRepo.findByCaseEntityId(eq(caseId), any(Pageable.class))).thenReturn(schedulePage);

            // Act
            ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            
            // Check metadata with additional filters
            assertNotNull(result.getBody().getMetadata());
            Map<String, Object> metadata = result.getBody().getMetadata();
            
            @SuppressWarnings("unchecked")
            Map<String, String> appliedFilters = (Map<String, String>) metadata.get("appliedFilters");
            assertNotNull(appliedFilters);
            assertEquals(caseId.toString(), appliedFilters.get("caseId"));
            
            verify(scheduleRepo).findByCaseEntityId(eq(caseId), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("Should test Google Calendar attendee management for lawyer as current user")
    void testGoogleCalendarAttendeeManagement_LawyerAsCurrentUser() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(googleCalendarService.createEvent(any(CreateCalendarEventDTO.class))).thenReturn(mockGoogleEvent);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            
            verify(googleCalendarService).createEvent(any(CreateCalendarEventDTO.class));
            verify(scheduleGoogleCalendarEventRepo).save(any(ScheduleGoogleCalendarEvent.class));
        }
    }

    @Test
    @DisplayName("Should test Google Calendar attendee management for client as current user")
    void testGoogleCalendarAttendeeManagement_ClientAsCurrentUser() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(clientUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(clientUserId)).thenReturn(Optional.of("valid-access-token"));
            when(googleCalendarService.createEvent(any(CreateCalendarEventDTO.class))).thenReturn(mockGoogleEvent);

            // Act
            ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingService.createSchedule(createScheduleDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            
            verify(googleCalendarService).createEvent(any(CreateCalendarEventDTO.class));
            verify(scheduleGoogleCalendarEventRepo).save(any(ScheduleGoogleCalendarEvent.class));
        }
    }

    @Test
    @DisplayName("Should test Google Calendar integration with transactional behavior")
    void testGoogleCalendarIntegrationTransactional() throws Exception {
        // Arrange
        try (MockedStatic<CaseAssetUtility> mockedCaseAssetUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validationResult = 
                    new CaseAssetUtility.CaseAssetValidationResult<>(lawyerUser, testCase, null);
            
            mockedCaseAssetUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                    eq(caseId), 
                    eq("create schedule"), 
                    eq(userRepo), 
                    eq(caseRepo)
            )).thenReturn(validationResult);

            when(scheduleRepo.save(any(Schedule.class))).thenReturn(testSchedule);
            when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);
            when(googleCalendarService.getValidAccessToken(lawyerId)).thenReturn(Optional.of("valid-access-token"));
            when(googleCalendarService.createEvent(any(CreateCalendarEventDTO.class)))
                    .thenThrow(new RuntimeException("Network error")); // Simulate network error

            // Act & Assert
            assertThrows(GoogleCalendarException.class, () -> {
                schedulingService.createSchedule(createScheduleDTO);
            });
            
            // Verify that schedule was saved before Google Calendar error
            verify(scheduleRepo).save(any(Schedule.class));
            verify(googleCalendarService).createEvent(any(CreateCalendarEventDTO.class));
            verify(scheduleGoogleCalendarEventRepo, never()).save(any(ScheduleGoogleCalendarEvent.class));
        }
    }
} 