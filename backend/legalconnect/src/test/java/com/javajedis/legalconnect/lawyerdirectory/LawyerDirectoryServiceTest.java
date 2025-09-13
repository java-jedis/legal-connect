package com.javajedis.legalconnect.lawyerdirectory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyerdirectory.dto.CreateLawyerReviewDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.FindLawyersDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerReviewListResponseDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerReviewResponseDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerSearchResultDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.UpdateLawyerReviewDTO;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class LawyerDirectoryServiceTest {
    @Mock
    private LawyerRepo lawyerRepo;
    @Mock
    private LawyerReviewRepo lawyerReviewRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private CaseRepo caseRepo;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationPreferenceService notificationPreferenceService;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private LawyerDirectoryService lawyerDirectoryService;

    private User testUser;
    private User testLawyerUser;
    private Case testCase;
    private LawyerReview testReview;
    private UUID testUserId;
    private UUID testLawyerId;
    private UUID testCaseId;
    private UUID testReviewId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserId = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testReviewId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setFirstName("Client");
        testUser.setLastName("User");
        testUser.setEmail("client@example.com");
        testLawyerUser = new User();
        testLawyerUser.setId(testLawyerId);
        testLawyerUser.setFirstName("Lawyer");
        testLawyerUser.setLastName("User");
        testLawyerUser.setEmail("lawyer@example.com");
        com.javajedis.legalconnect.lawyer.Lawyer lawyer = mock(com.javajedis.legalconnect.lawyer.Lawyer.class);
        when(lawyer.getUser()).thenReturn(testLawyerUser);
        testCase = new Case();
        testCase.setId(testCaseId);
        testCase.setClient(testUser);
        testCase.setLawyer(lawyer);
        testCase.setStatus(CaseStatus.RESOLVED);
        testReview = new LawyerReview();
        testReview.setId(testReviewId);
        testReview.setClient(testUser);
        testReview.setLawyer(testLawyerUser);
        testReview.setCaseE(testCase);
        testReview.setRating((short)5);
        testReview.setReview("Excellent");
        testReview.setCreatedAt(OffsetDateTime.now());
        testReview.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void findLawyers_success_returnsLawyerList() {
        FindLawyersDTO dto = new FindLawyersDTO();
        dto.setMinExperience(1);
        dto.setMaxExperience(20);
        dto.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        dto.setDivision(Division.DHAKA);
        dto.setDistrict(District.DHAKA);
        dto.setSpecialization(com.javajedis.legalconnect.lawyer.enums.SpecializationType.CRIMINAL_LAW);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Object[] row = new Object[] {
            testLawyerId, testUserId, "Lawyer", "User", "lawyer@example.com", "Firm", 10,
            PracticingCourt.SUPREME_COURT.name(), Division.DHAKA.name(), District.DHAKA.name(), "CRIMINAL_LAW", "CRIMINAL_LAW", 4.5,
            "https://example.com/lawyer-full.jpg", "https://example.com/lawyer-thumb.jpg", "lawyer-profile-pic"
        };
        List<Object[]> content = new ArrayList<>();
        content.add(row);
        Page<Object[]> page = new org.springframework.data.domain.PageImpl<>(content, pageable, 1L);
        when(lawyerRepo.findVerifiedLawyerRawResultsByCriteria(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(page);
        ResponseEntity<ApiResponse<List<LawyerSearchResultDTO>>> result = lawyerDirectoryService.findLawyers(dto, 0, 10, "DESC");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody().getData());
        assertEquals(1, result.getBody().getData().size());
        
        // Verify profile picture data is included
        LawyerSearchResultDTO lawyerResult = result.getBody().getData().get(0);
        assertNotNull(lawyerResult.getProfilePicture());
        assertEquals("https://example.com/lawyer-full.jpg", lawyerResult.getProfilePicture().getFullPictureUrl());
        assertEquals("https://example.com/lawyer-thumb.jpg", lawyerResult.getProfilePicture().getThumbnailPictureUrl());
        assertEquals("lawyer-profile-pic", lawyerResult.getProfilePicture().getPublicId());
    }

    @Test
    void addReview_success_returnsCreatedReview() {
        CreateLawyerReviewDTO reviewDTO = new CreateLawyerReviewDTO();
        reviewDTO.setCaseId(testCaseId);
        reviewDTO.setRating((short)5);
        reviewDTO.setReview("Excellent");
        when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(testCase));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            when(lawyerReviewRepo.save(any(LawyerReview.class))).thenReturn(testReview);
            ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.addReview(reviewDTO);
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertEquals("Review added successfully", result.getBody().getMessage());
        }
    }

    @Test
    void addReview_caseNotFound_returnsNotFound() {
        CreateLawyerReviewDTO reviewDTO = new CreateLawyerReviewDTO();
        reviewDTO.setCaseId(UUID.randomUUID());
        when(caseRepo.findById(any())).thenReturn(Optional.empty());
        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.addReview(reviewDTO);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Case not found", result.getBody().getError().getMessage());
    }

    @Test
    void addReview_notAuthorized_returnsForbidden() {
        CreateLawyerReviewDTO reviewDTO = new CreateLawyerReviewDTO();
        reviewDTO.setCaseId(testCaseId);
        reviewDTO.setRating((short)5);
        reviewDTO.setReview("Excellent");
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(testCase));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(anotherUser);
            ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.addReview(reviewDTO);
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertEquals("You are not authorized to create review for this case", result.getBody().getError().getMessage());
        }
    }

    @Test
    void addReview_caseNotResolved_returnsBadRequest() {
        testCase.setStatus(CaseStatus.IN_PROGRESS);
        CreateLawyerReviewDTO reviewDTO = new CreateLawyerReviewDTO();
        reviewDTO.setCaseId(testCaseId);
        reviewDTO.setRating((short)5);
        reviewDTO.setReview("Excellent");
        when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(testCase));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.addReview(reviewDTO);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertEquals("Can not create review for non resolved cases", result.getBody().getError().getMessage());
        }
    }

    @Test
    void updateReview_success_returnsUpdatedReview() {
        UpdateLawyerReviewDTO reviewDTO = new UpdateLawyerReviewDTO();
        reviewDTO.setRating((short)4);
        reviewDTO.setReview("Good");
        when(lawyerReviewRepo.findById(testReviewId)).thenReturn(Optional.of(testReview));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            when(lawyerReviewRepo.save(any(LawyerReview.class))).thenReturn(testReview);
            ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.updateReview(testReviewId, reviewDTO);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Review updated successfully", result.getBody().getMessage());
        }
    }

    @Test
    void updateReview_reviewNotFound_returnsNotFound() {
        UpdateLawyerReviewDTO reviewDTO = new UpdateLawyerReviewDTO();
        when(lawyerReviewRepo.findById(testReviewId)).thenReturn(Optional.empty());
        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.updateReview(testReviewId, reviewDTO);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Review not found", result.getBody().getError().getMessage());
    }

    @Test
    void updateReview_notOwner_returnsForbidden() {
        UpdateLawyerReviewDTO reviewDTO = new UpdateLawyerReviewDTO();
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        when(lawyerReviewRepo.findById(testReviewId)).thenReturn(Optional.of(testReview));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(anotherUser);
            ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.updateReview(testReviewId, reviewDTO);
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertEquals("You are not authorized to update this review", result.getBody().getError().getMessage());
        }
    }

    @Test
    void deleteReview_success_returnsSuccessMessage() {
        when(lawyerReviewRepo.findById(testReviewId)).thenReturn(Optional.of(testReview));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            ResponseEntity<ApiResponse<String>> result = lawyerDirectoryService.deleteReview(testReviewId);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Review deleted successfully", result.getBody().getMessage());
            verify(lawyerReviewRepo).delete(testReview);
        }
    }

    @Test
    void deleteReview_reviewNotFound_returnsNotFound() {
        when(lawyerReviewRepo.findById(testReviewId)).thenReturn(Optional.empty());
        ResponseEntity<ApiResponse<String>> result = lawyerDirectoryService.deleteReview(testReviewId);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Review not found", result.getBody().getError().getMessage());
    }

    @Test
    void deleteReview_notOwner_returnsForbidden() {
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        when(lawyerReviewRepo.findById(testReviewId)).thenReturn(Optional.of(testReview));
        try (MockedStatic<GetUserUtil> mockedUtil = mockStatic(GetUserUtil.class)) {
            mockedUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(anotherUser);
            ResponseEntity<ApiResponse<String>> result = lawyerDirectoryService.deleteReview(testReviewId);
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertEquals("You are not authorized to delete this review", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getReview_success_returnsReview() {
        when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(lawyerReviewRepo.findByCaseE(testCase)).thenReturn(testReview);
        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.getReview(testCaseId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Review retrieved successfully", result.getBody().getMessage());
        assertNotNull(result.getBody().getData());
    }

    @Test
    void getReview_reviewNotFound_returnsNotFound() {
        when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(lawyerReviewRepo.findByCaseE(testCase)).thenReturn(null);
        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> result = lawyerDirectoryService.getReview(testCaseId);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Review not found", result.getBody().getError().getMessage());
    }

    @Test
    void getReviews_success_returnsReviewList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LawyerReview> page = new PageImpl<>(List.of(testReview), pageable, 1);
        when(lawyerReviewRepo.findByLawyer_Id(testLawyerId, pageable)).thenReturn(page);
        ResponseEntity<ApiResponse<LawyerReviewListResponseDTO>> result = lawyerDirectoryService.getReviews(testLawyerId, 0, 10, "DESC");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Reviews retrieved successfully", result.getBody().getMessage());
        assertNotNull(result.getBody().getData());
        assertEquals(1, result.getBody().getData().getReviews().size());
    }
}
