package com.javajedis.legalconnect.casemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.javajedis.legalconnect.casemanagement.dto.CaseListResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CreateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseStatusDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;

/**
 * Comprehensive unit tests for CaseManagementController.
 * Tests all endpoints with various scenarios including success cases and error handling.
 */
@DisplayName("CaseManagementController Tests")
class CaseManagementControllerTest {

    @Mock
    private CaseManagementService caseManagementService;

    @InjectMocks
    private CaseManagementController caseManagementController;

    // Test data
    private CreateCaseDTO createCaseDTO;
    private UpdateCaseDTO updateCaseDTO;
    private UpdateCaseStatusDTO updateCaseStatusDTO;
    private CaseResponseDTO caseResponseDTO;
    private CaseListResponseDTO caseListResponseDTO;
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
        createCaseDTO = new CreateCaseDTO();
        createCaseDTO.setClientEmail("client@example.com");
        createCaseDTO.setTitle("Test Case Title");
        createCaseDTO.setDescription("Test case description");

        updateCaseDTO = new UpdateCaseDTO();
        updateCaseDTO.setTitle("Updated Case Title");
        updateCaseDTO.setDescription("Updated case description");

        updateCaseStatusDTO = new UpdateCaseStatusDTO();
        updateCaseStatusDTO.setStatus(CaseStatus.RESOLVED);

        caseId = UUID.randomUUID();
    }

    private void setupTestResponses() {
        // Setup lawyer summary
        CaseResponseDTO.LawyerSummaryDTO lawyerSummary = new CaseResponseDTO.LawyerSummaryDTO(
                UUID.randomUUID(),
                "John",
                "Doe",
                "lawyer@example.com",
                "Test Law Firm"
        );

        // Setup client summary
        CaseResponseDTO.ClientSummaryDTO clientSummary = new CaseResponseDTO.ClientSummaryDTO(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "client@example.com"
        );

        // Setup case response DTO
        caseResponseDTO = new CaseResponseDTO(
                caseId,
                lawyerSummary,
                clientSummary,
                "Test Case Title",
                "Test case description",
                CaseStatus.IN_PROGRESS,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        // Setup case list response DTO
        caseListResponseDTO = new CaseListResponseDTO(Arrays.asList(caseResponseDTO));
    }

    // CREATE CASE TESTS

    @Test
    @DisplayName("Should create case successfully")
    void createCase_Success_ReturnsCreatedResponse() {
        // Arrange
        ApiResponse<CaseResponseDTO> apiResponse = ApiResponse.success(
                caseResponseDTO, 
                HttpStatus.CREATED, 
                "Case created successfully"
        ).getBody();
        
        when(caseManagementService.createCase(any(CreateCaseDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.createCase(createCaseDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case created successfully", result.getBody().getMessage());
        assertEquals(caseResponseDTO, result.getBody().getData());
        assertEquals(caseId, result.getBody().getData().getCaseId());
        assertEquals("Test Case Title", result.getBody().getData().getTitle());
        
        verify(caseManagementService).createCase(createCaseDTO);
    }

    @Test
    @DisplayName("Should handle case creation validation errors")
    void createCase_ValidationError_ReturnsBadRequest() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Validation failed",
                HttpStatus.BAD_REQUEST
        ).getBody();
        
        when(caseManagementService.createCase(any(CreateCaseDTO.class)))
                .thenReturn(ResponseEntity.badRequest().body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.createCase(createCaseDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Validation failed", result.getBody().getError().getMessage());
        
        verify(caseManagementService).createCase(createCaseDTO);
    }

    @Test
    @DisplayName("Should handle unauthorized case creation")
    void createCase_Unauthorized_ReturnsUnauthorized() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "User is not authenticated",
                HttpStatus.UNAUTHORIZED
        ).getBody();
        
        when(caseManagementService.createCase(any(CreateCaseDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.createCase(createCaseDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        
        verify(caseManagementService).createCase(createCaseDTO);
    }

    @Test
    @DisplayName("Should handle case creation with empty description")
    void createCase_EmptyDescription_ReturnsSuccess() {
        // Arrange
        CreateCaseDTO emptyCaseDTO = new CreateCaseDTO();
        emptyCaseDTO.setClientEmail("client@example.com");
        emptyCaseDTO.setTitle("Test Case");
        emptyCaseDTO.setDescription("");

        CaseResponseDTO responseWithNullDescription = new CaseResponseDTO(
                caseId,
                caseResponseDTO.getLawyer(),
                caseResponseDTO.getClient(),
                "Test Case",
                null, // Description should be null
                CaseStatus.IN_PROGRESS,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        ApiResponse<CaseResponseDTO> apiResponse = ApiResponse.success(
                responseWithNullDescription, 
                HttpStatus.CREATED, 
                "Case created successfully"
        ).getBody();
        
        when(caseManagementService.createCase(any(CreateCaseDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.createCase(emptyCaseDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        org.junit.jupiter.api.Assertions.assertNull(result.getBody().getData().getDescription());
        
        verify(caseManagementService).createCase(emptyCaseDTO);
    }

    // GET CASE BY ID TESTS

    @Test
    @DisplayName("Should get case by ID successfully")
    void getCaseById_Success_ReturnsCase() {
        // Arrange
        ApiResponse<CaseResponseDTO> apiResponse = ApiResponse.success(
                caseResponseDTO, 
                HttpStatus.OK, 
                "Case retrieved successfully"
        ).getBody();
        
        when(caseManagementService.getCaseById(caseId))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.getCaseById(caseId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case retrieved successfully", result.getBody().getMessage());
        assertEquals(caseResponseDTO, result.getBody().getData());
        assertEquals(caseId, result.getBody().getData().getCaseId());
        
        verify(caseManagementService).getCaseById(caseId);
    }

    @Test
    @DisplayName("Should handle case not found")
    void getCaseById_NotFound_ReturnsNotFound() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Case not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(caseManagementService.getCaseById(caseId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.getCaseById(caseId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case not found", result.getBody().getError().getMessage());
        
        verify(caseManagementService).getCaseById(caseId);
    }

    @Test
    @DisplayName("Should handle forbidden access to case")
    void getCaseById_Forbidden_ReturnsForbidden() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "You don't have permission to view this case",
                HttpStatus.FORBIDDEN
        ).getBody();
        
        when(caseManagementService.getCaseById(caseId))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.getCaseById(caseId);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("You don't have permission to view this case", result.getBody().getError().getMessage());
        
        verify(caseManagementService).getCaseById(caseId);
    }

    // GET ALL USER CASES TESTS

    @Test
    @DisplayName("Should get all user cases with default parameters")
    void getAllUserCases_DefaultParameters_ReturnsSuccess() {
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
        metadata.put("appliedFilters", Map.of());
        metadata.put("sortDirection", "DESC");
        metadata.put("sortField", "updatedAt");

        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                caseListResponseDTO, 
                HttpStatus.OK, 
                "Cases retrieved successfully",
                metadata
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 10, null, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 10, null, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Cases retrieved successfully", result.getBody().getMessage());
        assertEquals(1, result.getBody().getData().getCases().size());
        assertNotNull(result.getBody().getMetadata());
        assertEquals(1L, result.getBody().getMetadata().get("totalCount"));
        
        verify(caseManagementService).getAllUserCases(0, 10, null, "DESC");
    }

    @Test
    @DisplayName("Should get filtered cases by status")
    void getAllUserCases_WithStatusFilter_ReturnsFilteredCases() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 1L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 1);
        metadata.put("appliedFilters", Map.of("status", "RESOLVED"));
        metadata.put("sortDirection", "DESC");
        metadata.put("sortField", "updatedAt");

        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                caseListResponseDTO, 
                HttpStatus.OK, 
                "Cases retrieved successfully",
                metadata
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 10, CaseStatus.RESOLVED, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 10, CaseStatus.RESOLVED, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Cases retrieved successfully", result.getBody().getMessage());
        @SuppressWarnings("unchecked")
        Map<String, String> appliedFilters = (Map<String, String>) result.getBody().getMetadata().get("appliedFilters");
        assertEquals("RESOLVED", appliedFilters.get("status"));
        
        verify(caseManagementService).getAllUserCases(0, 10, CaseStatus.RESOLVED, "DESC");
    }

    @Test
    @DisplayName("Should get cases with custom pagination")
    void getAllUserCases_CustomPagination_ReturnsSuccess() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 25L);
        metadata.put("pageNumber", 2);
        metadata.put("pageSize", 5);
        metadata.put("totalPages", 5);
        metadata.put("hasNext", true);
        metadata.put("hasPrevious", true);
        metadata.put("isFirst", false);
        metadata.put("isLast", false);

        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                caseListResponseDTO, 
                HttpStatus.OK, 
                "Cases retrieved successfully",
                metadata
        ).getBody();
        
        when(caseManagementService.getAllUserCases(2, 5, null, "ASC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(2, 5, null, "ASC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getMetadata().get("pageNumber"));
        assertEquals(5, result.getBody().getMetadata().get("pageSize"));
        assertEquals(25L, result.getBody().getMetadata().get("totalCount"));
        
        verify(caseManagementService).getAllUserCases(2, 5, null, "ASC");
    }

    @Test
    @DisplayName("Should handle ascending sort direction")
    void getAllUserCases_AscendingSort_ReturnsSuccess() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sortDirection", "ASC");
        metadata.put("sortField", "updatedAt");

        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                caseListResponseDTO, 
                HttpStatus.OK, 
                "Cases retrieved successfully",
                metadata
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 10, null, "ASC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 10, null, "ASC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("ASC", result.getBody().getMetadata().get("sortDirection"));
        assertEquals("updatedAt", result.getBody().getMetadata().get("sortField"));
        
        verify(caseManagementService).getAllUserCases(0, 10, null, "ASC");
    }

    @Test
    @DisplayName("Should handle empty case list")
    void getAllUserCases_EmptyList_ReturnsEmptyResult() {
        // Arrange
        CaseListResponseDTO emptyListResponse = new CaseListResponseDTO(Arrays.asList());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", 0L);
        metadata.put("pageNumber", 0);
        metadata.put("pageSize", 10);
        metadata.put("totalPages", 0);

        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                emptyListResponse, 
                HttpStatus.OK, 
                "Cases retrieved successfully",
                metadata
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 10, null, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 10, null, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getData().getCases().size());
        assertEquals(0L, result.getBody().getMetadata().get("totalCount"));
        
        verify(caseManagementService).getAllUserCases(0, 10, null, "DESC");
    }

    @Test
    @DisplayName("Should handle unauthorized access to cases list")
    void getAllUserCases_Unauthorized_ReturnsUnauthorized() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseListResponseDTO> apiResponse = (ApiResponse<CaseListResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "User is not authenticated",
                HttpStatus.UNAUTHORIZED
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 10, null, "DESC"))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 10, null, "DESC");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        
        verify(caseManagementService).getAllUserCases(0, 10, null, "DESC");
    }

    // UPDATE CASE TESTS

    @Test
    @DisplayName("Should update case successfully")
    void updateCase_Success_ReturnsUpdatedCase() {
        // Arrange
        CaseResponseDTO updatedCaseResponse = new CaseResponseDTO(
                caseId,
                caseResponseDTO.getLawyer(),
                caseResponseDTO.getClient(),
                "Updated Case Title",
                "Updated case description",
                CaseStatus.IN_PROGRESS,
                caseResponseDTO.getCreatedAt(),
                OffsetDateTime.now()
        );

        ApiResponse<CaseResponseDTO> apiResponse = ApiResponse.success(
                updatedCaseResponse, 
                HttpStatus.OK, 
                "Case updated successfully"
        ).getBody();
        
        when(caseManagementService.updateCase(eq(caseId), any(UpdateCaseDTO.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.updateCase(caseId, updateCaseDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case updated successfully", result.getBody().getMessage());
        assertEquals("Updated Case Title", result.getBody().getData().getTitle());
        assertEquals("Updated case description", result.getBody().getData().getDescription());
        
        verify(caseManagementService).updateCase(caseId, updateCaseDTO);
    }

    @Test
    @DisplayName("Should handle case update not found")
    void updateCase_NotFound_ReturnsNotFound() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Case not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(caseManagementService.updateCase(eq(caseId), any(UpdateCaseDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.updateCase(caseId, updateCaseDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case not found", result.getBody().getError().getMessage());
        
        verify(caseManagementService).updateCase(caseId, updateCaseDTO);
    }

    @Test
    @DisplayName("Should handle forbidden case update")
    void updateCase_Forbidden_ReturnsForbidden() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "You can only update your own cases",
                HttpStatus.FORBIDDEN
        ).getBody();
        
        when(caseManagementService.updateCase(eq(caseId), any(UpdateCaseDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.updateCase(caseId, updateCaseDTO);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("You can only update your own cases", result.getBody().getError().getMessage());
        
        verify(caseManagementService).updateCase(caseId, updateCaseDTO);
    }

    // UPDATE CASE STATUS TESTS

    @Test
    @DisplayName("Should update case status successfully")
    void updateCaseStatus_Success_ReturnsUpdatedCase() {
        // Arrange
        CaseResponseDTO resolvedCaseResponse = new CaseResponseDTO(
                caseId,
                caseResponseDTO.getLawyer(),
                caseResponseDTO.getClient(),
                caseResponseDTO.getTitle(),
                caseResponseDTO.getDescription(),
                CaseStatus.RESOLVED,
                caseResponseDTO.getCreatedAt(),
                OffsetDateTime.now()
        );

        ApiResponse<CaseResponseDTO> apiResponse = ApiResponse.success(
                resolvedCaseResponse, 
                HttpStatus.OK, 
                "Case status updated successfully"
        ).getBody();
        
        when(caseManagementService.updateCaseStatus(eq(caseId), any(UpdateCaseStatusDTO.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.updateCaseStatus(caseId, updateCaseStatusDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case status updated successfully", result.getBody().getMessage());
        assertEquals(CaseStatus.RESOLVED, result.getBody().getData().getStatus());
        
        verify(caseManagementService).updateCaseStatus(caseId, updateCaseStatusDTO);
    }

    @Test
    @DisplayName("Should handle case status update not found")
    void updateCaseStatus_NotFound_ReturnsNotFound() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Case not found",
                HttpStatus.NOT_FOUND
        ).getBody();
        
        when(caseManagementService.updateCaseStatus(eq(caseId), any(UpdateCaseStatusDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.updateCaseStatus(caseId, updateCaseStatusDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Case not found", result.getBody().getError().getMessage());
        
        verify(caseManagementService).updateCaseStatus(caseId, updateCaseStatusDTO);
    }

    @Test
    @DisplayName("Should handle unauthorized case status update")
    void updateCaseStatus_Unauthorized_ReturnsUnauthorized() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "User is not authenticated",
                HttpStatus.UNAUTHORIZED
        ).getBody();
        
        when(caseManagementService.updateCaseStatus(eq(caseId), any(UpdateCaseStatusDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.updateCaseStatus(caseId, updateCaseStatusDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        
        verify(caseManagementService).updateCaseStatus(caseId, updateCaseStatusDTO);
    }

    // EDGE CASE TESTS

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void getAllUserCases_NullParameters_HandlesGracefully() {
        // Arrange
        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                caseListResponseDTO, 
                HttpStatus.OK, 
                "Cases retrieved successfully"
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 10, null, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 10, null, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        
        verify(caseManagementService).getAllUserCases(0, 10, null, "DESC");
    }

    @Test
    @DisplayName("Should handle large page size requests")
    void getAllUserCases_LargePageSize_HandlesCorrectly() {
        // Arrange
        ApiResponse<CaseListResponseDTO> apiResponse = ApiResponse.success(
                caseListResponseDTO, 
                HttpStatus.OK, 
                "Cases retrieved successfully"
        ).getBody();
        
        when(caseManagementService.getAllUserCases(0, 100, null, "DESC"))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementController.getAllUserCases(0, 100, null, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        
        verify(caseManagementService).getAllUserCases(0, 100, null, "DESC");
    }

    @Test
    @DisplayName("Should handle service layer exceptions gracefully")
    void createCase_ServiceException_ReturnsInternalServerError() {
        // Arrange
        @SuppressWarnings("unchecked")
        ApiResponse<CaseResponseDTO> apiResponse = (ApiResponse<CaseResponseDTO>) (ApiResponse<?>) ApiResponse.error(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR
        ).getBody();
        
        when(caseManagementService.createCase(any(CreateCaseDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementController.createCase(createCaseDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Internal server error", result.getBody().getError().getMessage());
        
        verify(caseManagementService).createCase(createCaseDTO);
    }
} 