package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.scheduling.dto.CreateScheduleDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleListResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateScheduleDTO;

/**
 * Comprehensive unit tests for SchedulingController.
 * Tests all endpoints with various scenarios including success cases, error handling,
 * and Google Calendar integration error responses.
 */
@DisplayName("SchedulingController Tests")
class SchedulingControllerTest {

    @Mock
    private SchedulingService schedulingService;

    @InjectMocks
    private SchedulingController schedulingController;

    // Test data
    private CreateScheduleDTO createScheduleDTO;
    private UpdateScheduleDTO updateScheduleDTO;
    private ScheduleResponseDTO scheduleResponseDTO;
    private ScheduleListResponseDTO scheduleListResponseDTO;
    private UUID scheduleId;
    private UUID caseId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        setupTestDTOs();
        setupTestResponses();
    }

    private void setupTestDTOs() {
        // Setup request DTOs
        caseId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

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

    private void setupTestResponses() {
        // Setup response DTOs
        scheduleResponseDTO = new ScheduleResponseDTO();
        scheduleResponseDTO.setId(scheduleId);
        scheduleResponseDTO.setTitle("Initial Consultation");
        scheduleResponseDTO.setType(ScheduleType.MEETING);
        scheduleResponseDTO.setDescription("First meeting with client");
        scheduleResponseDTO.setDate(LocalDate.now().plusDays(1));
        scheduleResponseDTO.setStartTime(OffsetDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0));
        scheduleResponseDTO.setEndTime(OffsetDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0));
        scheduleResponseDTO.setCreatedAt(OffsetDateTime.now());
        scheduleResponseDTO.setUpdatedAt(OffsetDateTime.now());

        // Setup case info
        ScheduleResponseDTO.CaseSummaryDTO caseSummaryDTO = new ScheduleResponseDTO.CaseSummaryDTO(
                caseId, "Test Case", CaseStatus.IN_PROGRESS
        );
        scheduleResponseDTO.setCaseInfo(caseSummaryDTO);

        // Setup lawyer info
        ScheduleResponseDTO.LawyerSummaryDTO lawyerSummaryDTO = new ScheduleResponseDTO.LawyerSummaryDTO(
                UUID.randomUUID(), "John", "Doe", "lawyer@example.com"
        );
        scheduleResponseDTO.setLawyerInfo(lawyerSummaryDTO);

        // Setup client info
        ScheduleResponseDTO.ClientSummaryDTO clientSummaryDTO = new ScheduleResponseDTO.ClientSummaryDTO(
                UUID.randomUUID(), "Jane", "Smith", "client@example.com"
        );
        scheduleResponseDTO.setClientInfo(clientSummaryDTO);

        // Setup list response
        scheduleListResponseDTO = new ScheduleListResponseDTO(Arrays.asList(scheduleResponseDTO));
    }

    @Test
    @DisplayName("Should create schedule successfully")
    void createSchedule_Success_ReturnsCreatedResponse() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        ApiResponse<ScheduleResponseDTO> apiResponse = ApiResponse.success(
                scheduleResponseDTO, 
                HttpStatus.CREATED, 
                "Schedule created successfully",
                metadata
        ).getBody();
        
        when(schedulingService.createSchedule(any(CreateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.createSchedule(createScheduleDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule created successfully", result.getBody().getMessage());
        assertEquals(scheduleResponseDTO, result.getBody().getData());
        assertEquals(scheduleId, result.getBody().getData().getId());
        assertEquals("Initial Consultation", result.getBody().getData().getTitle());
        assertEquals(ScheduleType.MEETING, result.getBody().getData().getType());
        
        verify(schedulingService).createSchedule(createScheduleDTO);
    }

    @Test
    @DisplayName("Should handle create schedule error")
    void createSchedule_Error_ReturnsErrorResponse() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Case not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(schedulingService.createSchedule(any(CreateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.createSchedule(createScheduleDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case not found", result.getBody().getError().getMessage());
        
        verify(schedulingService).createSchedule(createScheduleDTO);
    }

    @Test
    @DisplayName("Should handle Google Calendar integration error during create")
    void createSchedule_GoogleCalendarError_ReturnsInternalServerError() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Failed to create Google Calendar event for schedule",
                HttpStatus.INTERNAL_SERVER_ERROR
        ).getBody();
        
        when(schedulingService.createSchedule(any(CreateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.createSchedule(createScheduleDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Failed to create Google Calendar event for schedule", result.getBody().getError().getMessage());
        
        verify(schedulingService).createSchedule(createScheduleDTO);
    }

    @Test
    @DisplayName("Should get schedule by ID successfully")
    void getScheduleById_Success_ReturnsSchedule() {
        // Arrange
        ApiResponse<ScheduleResponseDTO> apiResponse = ApiResponse.success(
                scheduleResponseDTO, 
                HttpStatus.OK, 
                "Schedule retrieved successfully"
        ).getBody();
        
        when(schedulingService.getScheduleById(scheduleId))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.getScheduleById(scheduleId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule retrieved successfully", result.getBody().getMessage());
        assertEquals(scheduleResponseDTO, result.getBody().getData());
        assertEquals(scheduleId, result.getBody().getData().getId());
        
        verify(schedulingService).getScheduleById(scheduleId);
    }

    @Test
    @DisplayName("Should handle schedule not found")
    void getScheduleById_NotFound_ReturnsNotFound() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Schedule not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(schedulingService.getScheduleById(scheduleId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.getScheduleById(scheduleId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule not found", result.getBody().getError().getMessage());
        
        verify(schedulingService).getScheduleById(scheduleId);
    }

    @Test
    @DisplayName("Should update schedule successfully")
    void updateSchedule_Success_ReturnsUpdatedSchedule() {
        // Arrange
        ScheduleResponseDTO updatedScheduleResponse = new ScheduleResponseDTO();
        updatedScheduleResponse.setId(scheduleId);
        updatedScheduleResponse.setTitle("Updated Consultation");
        updatedScheduleResponse.setType(ScheduleType.COURT_HEARING);
        updatedScheduleResponse.setDescription("Updated meeting description");
        
        ApiResponse<ScheduleResponseDTO> apiResponse = ApiResponse.success(
                updatedScheduleResponse, 
                HttpStatus.OK, 
                "Schedule updated successfully"
        ).getBody();
        
        when(schedulingService.updateSchedule(eq(scheduleId), any(UpdateScheduleDTO.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.updateSchedule(scheduleId, updateScheduleDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule updated successfully", result.getBody().getMessage());
        assertEquals(updatedScheduleResponse, result.getBody().getData());
        assertEquals("Updated Consultation", result.getBody().getData().getTitle());
        assertEquals(ScheduleType.COURT_HEARING, result.getBody().getData().getType());
        
        verify(schedulingService).updateSchedule(scheduleId, updateScheduleDTO);
    }

    @Test
    @DisplayName("Should handle update schedule error")
    void updateSchedule_Error_ReturnsErrorResponse() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Schedule not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(schedulingService.updateSchedule(eq(scheduleId), any(UpdateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.updateSchedule(scheduleId, updateScheduleDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule not found", result.getBody().getError().getMessage());
        
        verify(schedulingService).updateSchedule(scheduleId, updateScheduleDTO);
    }

    @Test
    @DisplayName("Should handle Google Calendar integration error during update")
    void updateSchedule_GoogleCalendarError_ReturnsInternalServerError() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Failed to update Google Calendar event for schedule",
                HttpStatus.INTERNAL_SERVER_ERROR
        ).getBody();
        
        when(schedulingService.updateSchedule(eq(scheduleId), any(UpdateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.updateSchedule(scheduleId, updateScheduleDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Failed to update Google Calendar event for schedule", result.getBody().getError().getMessage());
        
        verify(schedulingService).updateSchedule(scheduleId, updateScheduleDTO);
    }

    @Test
    @DisplayName("Should delete schedule successfully")
    void deleteSchedule_Success_ReturnsSuccessMessage() {
        // Arrange
        ApiResponse<String> apiResponse = ApiResponse.success(
                "Schedule deleted successfully", 
                HttpStatus.OK, 
                "Schedule deleted successfully"
        ).getBody();
        
        when(schedulingService.deleteSchedule(scheduleId))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = schedulingController.deleteSchedule(scheduleId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule deleted successfully", result.getBody().getMessage());
        assertEquals("Schedule deleted successfully", result.getBody().getData());
        
        verify(schedulingService).deleteSchedule(scheduleId);
    }

    @Test
    @DisplayName("Should handle delete schedule error")
    void deleteSchedule_Error_ReturnsErrorResponse() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<String> apiResponse = (ApiResponse<String>) (ApiResponse<?>) ApiResponse.error(
                "Schedule not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(schedulingService.deleteSchedule(scheduleId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = schedulingController.deleteSchedule(scheduleId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedule not found", result.getBody().getError().getMessage());
        
        verify(schedulingService).deleteSchedule(scheduleId);
    }

    @Test
    @DisplayName("Should handle Google Calendar integration error during delete")
    void deleteSchedule_GoogleCalendarError_ReturnsInternalServerError() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<String> apiResponse = (ApiResponse<String>) (ApiResponse<?>) ApiResponse.error(
                "Failed to delete Google Calendar event for schedule",
                HttpStatus.INTERNAL_SERVER_ERROR
        ).getBody();
        
        when(schedulingService.deleteSchedule(scheduleId))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = schedulingController.deleteSchedule(scheduleId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Failed to delete Google Calendar event for schedule", result.getBody().getError().getMessage());
        
        verify(schedulingService).deleteSchedule(scheduleId);
    }

    @Test
    @DisplayName("Should get all user schedules successfully")
    void getAllUserSchedules_Success_ReturnsScheduleList() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 1L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 1);
        metadata.put("hasNext", false);
        metadata.put("hasPrevious", false);
        metadata.put("isFirst", true);
        metadata.put("isLast", true);
        metadata.put("sortDirection", "DESC");
        metadata.put("sortField", "createdAt");
        metadata.put("appliedFilters", Map.of());
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                scheduleListResponseDTO, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllUserSchedules(0, 10, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllUserSchedules(0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
        assertEquals(scheduleListResponseDTO, result.getBody().getData());
        assertNotNull(result.getBody().getMetadata());
        assertEquals(1L, result.getBody().getMetadata().get("totalCount"));
        assertEquals(0, result.getBody().getMetadata().get("pageNumber"));
        assertEquals(10, result.getBody().getMetadata().get("pageSize"));
        
        verify(schedulingService).getAllUserSchedules(0, 10, "DESC");
    }

    @Test
    @DisplayName("Should get all user schedules with default parameters")
    void getAllUserSchedules_DefaultParameters_ReturnsScheduleList() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 1L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 1);
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                scheduleListResponseDTO, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllUserSchedules(0, 10, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllUserSchedules(0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
        assertEquals(scheduleListResponseDTO, result.getBody().getData());
        assertEquals(1, result.getBody().getData().getSchedules().size());
        
        verify(schedulingService).getAllUserSchedules(0, 10, "DESC");
    }

    @Test
    @DisplayName("Should get all schedules for case successfully")
    void getAllSchedulesForCase_Success_ReturnsScheduleList() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 1L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 1);
        metadata.put("hasNext", false);
        metadata.put("hasPrevious", false);
        metadata.put("isFirst", true);
        metadata.put("isLast", true);
        metadata.put("sortDirection", "DESC");
        metadata.put("sortField", "createdAt");
        metadata.put("appliedFilters", Map.of("caseId", caseId.toString()));
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                scheduleListResponseDTO, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllSchedulesForCase(caseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
        assertEquals(scheduleListResponseDTO, result.getBody().getData());
        assertNotNull(result.getBody().getMetadata());
        assertEquals(1L, result.getBody().getMetadata().get("totalCount"));
        assertEquals(Map.of("caseId", caseId.toString()), result.getBody().getMetadata().get("appliedFilters"));
        
        verify(schedulingService).getAllSchedulesForCase(caseId, 0, 10, "DESC");
    }

    @Test
    @DisplayName("Should handle get schedules for case error")
    void getAllSchedulesForCase_Error_ReturnsErrorResponse() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleListResponseDTO> apiResponse = (ApiResponse<ScheduleListResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Case not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC"))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllSchedulesForCase(caseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case not found", result.getBody().getError().getMessage());
        
        verify(schedulingService).getAllSchedulesForCase(caseId, 0, 10, "DESC");
    }

    @Test
    @DisplayName("Should handle unauthorized access")
    void getAllUserSchedules_Unauthorized_ReturnsUnauthorized() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleListResponseDTO> apiResponse = (ApiResponse<ScheduleListResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "User is not authenticated",
                HttpStatus.UNAUTHORIZED
        ).getBody();
        
        when(schedulingService.getAllUserSchedules(0, 10, "DESC"))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllUserSchedules(0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        
        verify(schedulingService).getAllUserSchedules(0, 10, "DESC");
    }

    @Test
    @DisplayName("Should handle pagination parameters correctly")
    void testPaginationParameters() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 25L);
        metadata.put("pageNumber", 2);
        metadata.put("pageSize", 5);
        metadata.put("totalPages", 5);
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                scheduleListResponseDTO, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllUserSchedules(2, 5, "ASC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllUserSchedules(2, 5, "ASC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getMetadata().get("pageNumber"));
        assertEquals(5, result.getBody().getMetadata().get("pageSize"));
        assertEquals(25L, result.getBody().getMetadata().get("totalCount"));
        assertEquals(5, result.getBody().getMetadata().get("totalPages"));
        
        verify(schedulingService).getAllUserSchedules(2, 5, "ASC");
    }

    @Test
    @DisplayName("Should handle validation errors for create schedule")
    void createSchedule_ValidationError_ReturnsBadRequest() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Validation failed: Title is required",
                HttpStatus.BAD_REQUEST
        ).getBody();
        
        when(schedulingService.createSchedule(any(CreateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.createSchedule(createScheduleDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Validation failed: Title is required", result.getBody().getError().getMessage());
        
        verify(schedulingService).createSchedule(createScheduleDTO);
    }

    @Test
    @DisplayName("Should handle validation errors for update schedule")
    void updateSchedule_ValidationError_ReturnsBadRequest() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleResponseDTO> apiResponse = (ApiResponse<ScheduleResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Validation failed: End time must be after start time",
                HttpStatus.BAD_REQUEST
        ).getBody();
        
        when(schedulingService.updateSchedule(eq(scheduleId), any(UpdateScheduleDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleResponseDTO>> result = schedulingController.updateSchedule(scheduleId, updateScheduleDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Validation failed: End time must be after start time", result.getBody().getError().getMessage());
        
        verify(schedulingService).updateSchedule(scheduleId, updateScheduleDTO);
    }

    @Test
    @DisplayName("Should handle access denied error")
    void getAllSchedulesForCase_AccessDenied_ReturnsForbidden() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<ScheduleListResponseDTO> apiResponse = (ApiResponse<ScheduleListResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Access denied: You don't have permission to view this case",
                HttpStatus.FORBIDDEN
        ).getBody();
        
        when(schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC"))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllSchedulesForCase(caseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Access denied: You don't have permission to view this case", result.getBody().getError().getMessage());
        
        verify(schedulingService).getAllSchedulesForCase(caseId, 0, 10, "DESC");
    }

    @Test
    @DisplayName("Should handle empty results for user schedules")
    void getAllUserSchedules_EmptyResults_ReturnsEmptyList() {
        // Arrange
        ScheduleListResponseDTO emptyListResponse = new ScheduleListResponseDTO(Arrays.asList());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 0L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 0);
        metadata.put("hasNext", false);
        metadata.put("hasPrevious", false);
        metadata.put("isFirst", true);
        metadata.put("isLast", true);
        metadata.put("sortDirection", "DESC");
        metadata.put("sortField", "createdAt");
        metadata.put("appliedFilters", Map.of());
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                emptyListResponse, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllUserSchedules(0, 10, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllUserSchedules(0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
        assertEquals(0, result.getBody().getData().getSchedules().size());
        assertEquals(0L, result.getBody().getMetadata().get("totalCount"));
        
        verify(schedulingService).getAllUserSchedules(0, 10, "DESC");
    }

    @Test
    @DisplayName("Should handle different sort directions")
    void getAllUserSchedules_DifferentSortDirections_ReturnsCorrectOrder() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 1L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 1);
        metadata.put("sortDirection", "ASC");
        metadata.put("sortField", "createdAt");
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                scheduleListResponseDTO, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllUserSchedules(0, 10, "ASC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllUserSchedules(0, 10, "ASC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
        assertEquals("ASC", result.getBody().getMetadata().get("sortDirection"));
        
        verify(schedulingService).getAllUserSchedules(0, 10, "ASC");
    }

    @Test
    @DisplayName("Should handle case-specific schedule filtering")
    void getAllSchedulesForCase_WithFilters_ReturnsFilteredResults() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 1L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 1);
        metadata.put("sortDirection", "DESC");
        metadata.put("sortField", "createdAt");
        metadata.put("appliedFilters", Map.of("caseId", caseId.toString()));
        
        ApiResponse<ScheduleListResponseDTO> apiResponse = ApiResponse.success(
                scheduleListResponseDTO, 
                HttpStatus.OK, 
                "Schedules retrieved successfully",
                metadata
        ).getBody();
        
        when(schedulingService.getAllSchedulesForCase(caseId, 0, 10, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<ScheduleListResponseDTO>> result = schedulingController.getAllSchedulesForCase(caseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Schedules retrieved successfully", result.getBody().getMessage());
        
        @SuppressWarnings("unchecked")
        Map<String, String> appliedFilters = (Map<String, String>) result.getBody().getMetadata().get("appliedFilters");
        assertEquals(caseId.toString(), appliedFilters.get("caseId"));
        
        verify(schedulingService).getAllSchedulesForCase(caseId, 0, 10, "DESC");
    }
} 