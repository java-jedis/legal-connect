package com.javajedis.legalconnect.casemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.casemanagement.dto.CaseListResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CreateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseStatusDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.exception.UserNotFoundException;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerUtil;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

/**
 * Comprehensive unit tests for CaseManagementService.
 * Tests all CRUD operations, authentication, authorization, and edge cases.
 */
@DisplayName("CaseManagementService Tests")
class CaseManagementServiceTest {

    @Mock
    private CaseRepo caseRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private LawyerRepo lawyerRepo;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferenceService notificationPreferenceService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CaseManagementService caseManagementService;

    // Test data
    private User lawyerUser;
    private User clientUser;
    private Lawyer lawyer;
    private Case testCase;
    private CreateCaseDTO createCaseDTO;
    private UpdateCaseDTO updateCaseDTO;
    private UpdateCaseStatusDTO updateCaseStatusDTO;
    private UUID caseId;
    private UUID lawyerId;
    private UUID clientUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        setupTestUsers();
        setupTestLawyer();
        setupTestCase();
        setupTestDTOs();
    }

    private void setupTestUsers() {
        // Setup lawyer user
        lawyerUser = new User();
        lawyerUser.setId(UUID.randomUUID());
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
        lawyerId = UUID.randomUUID();
        lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setUser(lawyerUser);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyer.setDivision(Division.DHAKA);
        lawyer.setDistrict(District.DHAKA);
        lawyer.setBio("Experienced lawyer");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);
        lawyer.setCreatedAt(OffsetDateTime.now());
        lawyer.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestCase() {
        caseId = UUID.randomUUID();
        testCase = new Case();
        testCase.setId(caseId);
        testCase.setLawyer(lawyer);
        testCase.setClient(clientUser);
        testCase.setTitle("Test Case Title");
        testCase.setDescription("Test case description");
        testCase.setStatus(CaseStatus.IN_PROGRESS);
        testCase.setCreatedAt(OffsetDateTime.now());
        testCase.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestDTOs() {
        createCaseDTO = new CreateCaseDTO();
        createCaseDTO.setClientEmail("client@example.com");
        createCaseDTO.setTitle("New Case Title");
        createCaseDTO.setDescription("New case description");

        updateCaseDTO = new UpdateCaseDTO();
        updateCaseDTO.setTitle("Updated Case Title");
        updateCaseDTO.setDescription("Updated case description");

        updateCaseStatusDTO = new UpdateCaseStatusDTO();
        updateCaseStatusDTO.setStatus(CaseStatus.RESOLVED);
    }

    // CREATE CASE TESTS

    @Test
    @DisplayName("Should create case successfully for authenticated verified lawyer")
    void createCase_Success_ReturnsCreatedResponse() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(userRepo.findByEmail("client@example.com")).thenReturn(Optional.of(clientUser));
            when(caseRepo.save(any(Case.class))).thenReturn(testCase);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.createCase(createCaseDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case created successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(testCase.getTitle(), result.getBody().getData().getTitle());
            assertEquals(testCase.getDescription(), result.getBody().getData().getDescription());
            
            verify(caseRepo).save(any(Case.class));
        }
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated for case creation")
    void createCase_NotAuthenticated_ReturnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.createCase(createCaseDTO);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return not found when lawyer profile does not exist")
    void createCase_LawyerProfileNotFound_ReturnsNotFound() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.createCase(createCaseDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile does not exist", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when client email is not found")
    void createCase_ClientNotFound_ThrowsUserNotFoundException() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(userRepo.findByEmail("client@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            org.junit.jupiter.api.Assertions.assertThrows(UserNotFoundException.class, () -> {
                caseManagementService.createCase(createCaseDTO);
            });
        }
    }

    @Test
    @DisplayName("Should handle empty description by setting it to null")
    void createCase_EmptyDescription_SetsDescriptionToNull() {
        // Arrange
        CreateCaseDTO emptyCaseDTO = new CreateCaseDTO();
        emptyCaseDTO.setClientEmail("client@example.com");
        emptyCaseDTO.setTitle("Test Case");
        emptyCaseDTO.setDescription("   "); // Empty/whitespace description

        Case savedCase = new Case();
        savedCase.setId(caseId);
        savedCase.setLawyer(lawyer);
        savedCase.setClient(clientUser);
        savedCase.setTitle("Test Case");
        savedCase.setDescription(null);
        savedCase.setStatus(CaseStatus.IN_PROGRESS);
        savedCase.setCreatedAt(OffsetDateTime.now());
        savedCase.setUpdatedAt(OffsetDateTime.now());

        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(userRepo.findByEmail("client@example.com")).thenReturn(Optional.of(clientUser));
            when(caseRepo.save(any(Case.class))).thenReturn(savedCase);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.createCase(emptyCaseDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            org.junit.jupiter.api.Assertions.assertNull(result.getBody().getData().getDescription());
        }
    }

    // UPDATE CASE TESTS

    @Test
    @DisplayName("Should update case successfully for case owner")
    void updateCase_Success_ReturnsUpdatedCase() {
        // Arrange
        Case updatedCase = new Case();
        updatedCase.setId(caseId);
        updatedCase.setLawyer(lawyer);
        updatedCase.setClient(clientUser);
        updatedCase.setTitle("Updated Case Title");
        updatedCase.setDescription("Updated case description");
        updatedCase.setStatus(CaseStatus.IN_PROGRESS);
        updatedCase.setCreatedAt(testCase.getCreatedAt());
        updatedCase.setUpdatedAt(OffsetDateTime.now());

        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));
            when(caseRepo.save(any(Case.class))).thenReturn(updatedCase);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.updateCase(caseId, updateCaseDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case updated successfully", result.getBody().getMessage());
            assertEquals("Updated Case Title", result.getBody().getData().getTitle());
            assertEquals("Updated case description", result.getBody().getData().getDescription());
            
            verify(caseRepo).save(any(Case.class));
        }
    }

    @Test
    @DisplayName("Should return not found when updating non-existent case")
    void updateCase_CaseNotFound_ReturnsNotFound() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(caseRepo.findById(caseId)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.updateCase(caseId, updateCaseDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case not found", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return forbidden when trying to update case not owned by lawyer")
    void updateCase_NotCaseOwner_ReturnsForbidden() {
        // Arrange
        Lawyer otherLawyer = new Lawyer();
        otherLawyer.setId(UUID.randomUUID());
        otherLawyer.setUser(lawyerUser);

        Case otherCase = new Case();
        otherCase.setId(caseId);
        otherCase.setLawyer(otherLawyer);
        otherCase.setClient(clientUser);
        otherCase.setTitle("Other Case");
        otherCase.setStatus(CaseStatus.IN_PROGRESS);

        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(otherCase));

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.updateCase(caseId, updateCaseDTO);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("You can only update your own cases", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should handle empty description update by setting it to null")
    void updateCase_EmptyDescription_SetsDescriptionToNull() {
        // Arrange
        UpdateCaseDTO emptyUpdateDTO = new UpdateCaseDTO();
        emptyUpdateDTO.setTitle("Updated Title");
        emptyUpdateDTO.setDescription("   "); // Empty/whitespace description

        Case updatedCase = new Case();
        updatedCase.setId(caseId);
        updatedCase.setLawyer(lawyer);
        updatedCase.setClient(clientUser);
        updatedCase.setTitle("Updated Title");
        updatedCase.setDescription(null);
        updatedCase.setStatus(CaseStatus.IN_PROGRESS);
        updatedCase.setCreatedAt(testCase.getCreatedAt());
        updatedCase.setUpdatedAt(OffsetDateTime.now());

        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));
            when(caseRepo.save(any(Case.class))).thenReturn(updatedCase);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.updateCase(caseId, emptyUpdateDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            org.junit.jupiter.api.Assertions.assertNull(result.getBody().getData().getDescription());
        }
    }

    // UPDATE CASE STATUS TESTS

    @Test
    @DisplayName("Should update case status successfully")
    void updateCaseStatus_Success_ReturnsUpdatedCase() {
        // Arrange
        Case updatedCase = new Case();
        updatedCase.setId(caseId);
        updatedCase.setLawyer(lawyer);
        updatedCase.setClient(clientUser);
        updatedCase.setTitle(testCase.getTitle());
        updatedCase.setDescription(testCase.getDescription());
        updatedCase.setStatus(CaseStatus.RESOLVED);
        updatedCase.setCreatedAt(testCase.getCreatedAt());
        updatedCase.setUpdatedAt(OffsetDateTime.now());

        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(lawyerUser);
            
            when(lawyerRepo.findByUser(lawyerUser)).thenReturn(Optional.of(lawyer));
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));
            when(caseRepo.save(any(Case.class))).thenReturn(updatedCase);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.updateCaseStatus(caseId, updateCaseStatusDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case status updated successfully", result.getBody().getMessage());
            assertEquals(CaseStatus.RESOLVED, result.getBody().getData().getStatus());
            
            verify(caseRepo).save(any(Case.class));
        }
    }

    // GET CASE BY ID TESTS

    @Test
    @DisplayName("Should get case by ID successfully for case lawyer")
    void getCaseById_AsLawyer_Success_ReturnsCase() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.getCaseById(caseId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case retrieved successfully", result.getBody().getMessage());
            assertEquals(testCase.getId(), result.getBody().getData().getCaseId());
            assertEquals(testCase.getTitle(), result.getBody().getData().getTitle());
        }
    }

    @Test
    @DisplayName("Should get case by ID successfully for case client")
    void getCaseById_AsClient_Success_ReturnsCase() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(clientUser);
            
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.getCaseById(caseId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case retrieved successfully", result.getBody().getMessage());
            assertEquals(testCase.getId(), result.getBody().getData().getCaseId());
        }
    }

    @Test
    @DisplayName("Should return not found when case does not exist")
    void getCaseById_CaseNotFound_ReturnsNotFound() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findById(caseId)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.getCaseById(caseId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Case not found", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return forbidden when user has no access to case")
    void getCaseById_NoAccess_ReturnsForbidden() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.USER);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.getCaseById(caseId);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("You don't have permission to view this case", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated")
    void getCaseById_NotAuthenticated_ReturnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.getCaseById(caseId);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    // GET ALL USER CASES TESTS

    @Test
    @DisplayName("Should get all user cases with pagination successfully")
    void getAllUserCases_Success_ReturnsPaginatedCases() {
        // Arrange
        List<Case> cases = Arrays.asList(testCase);
        Page<Case> casePage = new PageImpl<>(cases, PageRequest.of(0, 10), 1);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClient(eq(lawyerUser), any(Pageable.class))).thenReturn(casePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 10, null, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Cases retrieved successfully", result.getBody().getMessage());
            assertEquals(1, result.getBody().getData().getCases().size());
            
            // Check metadata
            Map<String, Object> metadata = result.getBody().getMetadata();
            assertEquals(1L, metadata.get("totalCount"));
            assertEquals(0, metadata.get("pageNumber"));
            assertEquals(10, metadata.get("pageSize"));
            assertEquals(1, metadata.get("totalPages"));
            assertEquals(false, metadata.get("hasNext"));
            assertEquals(false, metadata.get("hasPrevious"));
            assertEquals(true, metadata.get("isFirst"));
            assertEquals(true, metadata.get("isLast"));
        }
    }

    @Test
    @DisplayName("Should get filtered cases by status successfully")
    void getAllUserCases_WithStatusFilter_ReturnsFilteredCases() {
        // Arrange
        List<Case> resolvedCases = Arrays.asList(testCase);
        Page<Case> casePage = new PageImpl<>(resolvedCases, PageRequest.of(0, 10), 1);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClientAndStatus(eq(lawyerUser), eq(CaseStatus.RESOLVED), any(Pageable.class)))
                .thenReturn(casePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 10, CaseStatus.RESOLVED, "ASC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(1, result.getBody().getData().getCases().size());
            
            // Check applied filters in metadata
            Map<String, Object> metadata = result.getBody().getMetadata();
            @SuppressWarnings("unchecked")
            Map<String, String> appliedFilters = (Map<String, String>) metadata.get("appliedFilters");
            assertEquals("RESOLVED", appliedFilters.get("status"));
            assertEquals("ASC", metadata.get("sortDirection"));
        }
    }

    @Test
    @DisplayName("Should handle ascending sort direction correctly")
    void getAllUserCases_AscendingSort_ReturnsCorrectlySortedCases() {
        // Arrange
        List<Case> cases = Arrays.asList(testCase);
        Page<Case> casePage = new PageImpl<>(cases, PageRequest.of(0, 10), 1);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClient(eq(lawyerUser), any(Pageable.class))).thenReturn(casePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 10, null, "ASC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            
            // Verify sort direction in metadata
            Map<String, Object> metadata = result.getBody().getMetadata();
            assertEquals("ASC", metadata.get("sortDirection"));
            assertEquals("updatedAt", metadata.get("sortField"));
        }
    }

    @Test
    @DisplayName("Should return empty list when user has no cases")
    void getAllUserCases_NoCases_ReturnsEmptyList() {
        // Arrange
        Page<Case> emptyCasePage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClient(eq(lawyerUser), any(Pageable.class))).thenReturn(emptyCasePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 10, null, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(0, result.getBody().getData().getCases().size());
            
            Map<String, Object> metadata = result.getBody().getMetadata();
            assertEquals(0L, metadata.get("totalCount"));
        }
    }

    @Test
    @DisplayName("Should return unauthorized when getting cases for unauthenticated user")
    void getAllUserCases_NotAuthenticated_ReturnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 10, null, "DESC");

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    // MAPPING TESTS

    @Test
    @DisplayName("Should map Case entity to CaseResponseDTO correctly")
    void mapCaseToCaseResponseDTO_Success_ReturnsCorrectMapping() {
        // Arrange & Act
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(testCase));

            ResponseEntity<ApiResponse<CaseResponseDTO>> result = caseManagementService.getCaseById(caseId);

            // Assert
            CaseResponseDTO responseDTO = result.getBody().getData();
            assertNotNull(responseDTO);
            assertEquals(testCase.getId(), responseDTO.getCaseId());
            assertEquals(testCase.getTitle(), responseDTO.getTitle());
            assertEquals(testCase.getDescription(), responseDTO.getDescription());
            assertEquals(testCase.getStatus(), responseDTO.getStatus());
            assertEquals(testCase.getCreatedAt(), responseDTO.getCreatedAt());
            assertEquals(testCase.getUpdatedAt(), responseDTO.getUpdatedAt());

            // Check lawyer summary
            CaseResponseDTO.LawyerSummaryDTO lawyerSummary = responseDTO.getLawyer();
            assertNotNull(lawyerSummary);
            assertEquals(lawyer.getId(), lawyerSummary.getId());
            assertEquals(lawyerUser.getFirstName(), lawyerSummary.getFirstName());
            assertEquals(lawyerUser.getLastName(), lawyerSummary.getLastName());
            assertEquals(lawyerUser.getEmail(), lawyerSummary.getEmail());
            assertEquals(lawyer.getFirm(), lawyerSummary.getFirm());

            // Check client summary
            CaseResponseDTO.ClientSummaryDTO clientSummary = responseDTO.getClient();
            assertNotNull(clientSummary);
            assertEquals(clientUser.getId(), clientSummary.getId());
            assertEquals(clientUser.getFirstName(), clientSummary.getFirstName());
            assertEquals(clientUser.getLastName(), clientSummary.getLastName());
            assertEquals(clientUser.getEmail(), clientSummary.getEmail());
        }
    }

    // PAGINATION AND SORTING EDGE CASES

    @Test
    @DisplayName("Should handle large page size correctly")
    void getAllUserCases_LargePageSize_HandlesCorrectly() {
        // Arrange
        List<Case> cases = Arrays.asList(testCase);
        Page<Case> casePage = new PageImpl<>(cases, PageRequest.of(0, 100), 1);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClient(eq(lawyerUser), any(Pageable.class))).thenReturn(casePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 100, null, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            Map<String, Object> metadata = result.getBody().getMetadata();
            assertEquals(100, metadata.get("pageSize"));
        }
    }

    @Test
    @DisplayName("Should handle invalid sort direction by defaulting to DESC")
    void getAllUserCases_InvalidSortDirection_DefaultsToDesc() {
        // Arrange
        List<Case> cases = Arrays.asList(testCase);
        Page<Case> casePage = new PageImpl<>(cases, PageRequest.of(0, 10), 1);

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClient(eq(lawyerUser), any(Pageable.class))).thenReturn(casePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(0, 10, null, "INVALID");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            Map<String, Object> metadata = result.getBody().getMetadata();
            assertEquals("INVALID", metadata.get("sortDirection")); // Should preserve the input for metadata
        }
    }

    // MULTI-PAGE PAGINATION TESTS

    @Test
    @DisplayName("Should handle multi-page results correctly")
    void getAllUserCases_MultiPage_ReturnsCorrectMetadata() {
        // Arrange
        List<Case> cases = Arrays.asList(testCase);
        Page<Case> casePage = new PageImpl<>(cases, PageRequest.of(1, 5), 15); // Page 1 of 3 (total 15 items)

        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(lawyerUser);
            
            when(caseRepo.findByUserAsLawyerOrClient(eq(lawyerUser), any(Pageable.class))).thenReturn(casePage);

            // Act
            ResponseEntity<ApiResponse<CaseListResponseDTO>> result = caseManagementService.getAllUserCases(1, 5, null, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            
            Map<String, Object> metadata = result.getBody().getMetadata();
            assertEquals(15L, metadata.get("totalCount"));
            assertEquals(1, metadata.get("pageNumber"));
            assertEquals(5, metadata.get("pageSize"));
            assertEquals(3, metadata.get("totalPages"));
            assertEquals(true, metadata.get("hasNext"));
            assertEquals(true, metadata.get("hasPrevious"));
            assertEquals(false, metadata.get("isFirst"));
            assertEquals(false, metadata.get("isLast"));
        }
    }
} 