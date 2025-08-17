package com.javajedis.legalconnect.lawyerdirectory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyerdirectory.dto.CreateLawyerReviewDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.FindLawyersDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerReviewListResponseDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerReviewResponseDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerSearchResultDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.UpdateLawyerReviewDTO;

class LawyerDirectoryControllerTest {

    @Mock
    private LawyerDirectoryService lawyerDirectoryService;

    @InjectMocks
    private LawyerDirectoryController lawyerDirectoryController;

    private FindLawyersDTO findLawyersDTO;
    private CreateLawyerReviewDTO createLawyerReviewDTO;
    private UpdateLawyerReviewDTO updateLawyerReviewDTO;
    private LawyerSearchResultDTO lawyerSearchResultDTO;
    private LawyerReviewResponseDTO lawyerReviewResponseDTO;
    private UUID testLawyerId;
    private UUID testReviewId;
    private UUID testCaseId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testLawyerId = UUID.randomUUID();
        testReviewId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();

        findLawyersDTO = new FindLawyersDTO();
        findLawyersDTO.setMinExperience(2);
        findLawyersDTO.setMaxExperience(10);
        findLawyersDTO.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        findLawyersDTO.setDivision(Division.DHAKA);
        findLawyersDTO.setDistrict(District.DHAKA);
        findLawyersDTO.setSpecialization(SpecializationType.CRIMINAL_LAW);

        createLawyerReviewDTO = new CreateLawyerReviewDTO();
        createLawyerReviewDTO.setCaseId(testCaseId);
        createLawyerReviewDTO.setRating((short) 5);
        createLawyerReviewDTO.setReview("Excellent lawyer, very professional");

        updateLawyerReviewDTO = new UpdateLawyerReviewDTO();
        updateLawyerReviewDTO.setRating((short) 4);
        updateLawyerReviewDTO.setReview("Good lawyer, satisfied with service");

        lawyerSearchResultDTO = LawyerSearchResultDTO.builder()
                .lawyerId(testLawyerId)
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .firm("Doe & Associates")
                .yearsOfExperience(5)
                .practicingCourt(PracticingCourt.HIGH_COURT_DIVISION)
                .division(Division.DHAKA)
                .district(District.DHAKA)
                .bio("Experienced criminal lawyer")
                .specializations(List.of(SpecializationType.CRIMINAL_LAW))
                .averageRating(4.5)
                .build();

        lawyerReviewResponseDTO = new LawyerReviewResponseDTO(
                testReviewId,
                testLawyerId,
                "John Doe",
                UUID.randomUUID(),
                "Jane Smith",
                testCaseId,
                (short) 5,
                "Excellent lawyer, very professional",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    void findLawyers_shouldReturnOk() {
        List<LawyerSearchResultDTO> searchResults = List.of(lawyerSearchResultDTO);
        ApiResponse<List<LawyerSearchResultDTO>> apiResponse = ApiResponse.success(searchResults, HttpStatus.OK, "Lawyers found successfully").getBody();
        when(lawyerDirectoryService.findLawyers(any(FindLawyersDTO.class), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<List<LawyerSearchResultDTO>>> response = 
                lawyerDirectoryController.findLawyers(findLawyersDTO, 0, 10, "DESC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getLawyerId()).isEqualTo(testLawyerId);
    }

    @Test
    void findLawyers_withCustomPagination_shouldReturnOk() {
        List<LawyerSearchResultDTO> searchResults = List.of(lawyerSearchResultDTO);
        ApiResponse<List<LawyerSearchResultDTO>> apiResponse = ApiResponse.success(searchResults, HttpStatus.OK, "Lawyers found successfully").getBody();
        when(lawyerDirectoryService.findLawyers(any(FindLawyersDTO.class), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<List<LawyerSearchResultDTO>>> response = 
                lawyerDirectoryController.findLawyers(findLawyersDTO, 1, 5, "ASC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void addReview_shouldReturnCreated() {
        ApiResponse<LawyerReviewResponseDTO> apiResponse = ApiResponse.success(lawyerReviewResponseDTO, HttpStatus.CREATED, "Review added successfully").getBody();
        when(lawyerDirectoryService.addReview(any(CreateLawyerReviewDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> response = 
                lawyerDirectoryController.addReview(createLawyerReviewDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getRating()).isEqualTo((short) 5);
        assertThat(response.getBody().getData().getCaseId()).isEqualTo(testCaseId);
    }

    @Test
    void updateReview_shouldReturnOk() {
        LawyerReviewResponseDTO updatedReview = new LawyerReviewResponseDTO(
                testReviewId,
                testLawyerId,
                "John Doe",
                UUID.randomUUID(),
                "Jane Smith",
                testCaseId,
                (short) 4,
                "Good lawyer, satisfied with service",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        
        ApiResponse<LawyerReviewResponseDTO> apiResponse = ApiResponse.success(updatedReview, HttpStatus.OK, "Review updated successfully").getBody();
        when(lawyerDirectoryService.updateReview(any(UUID.class), any(UpdateLawyerReviewDTO.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> response = 
                lawyerDirectoryController.updateReview(testReviewId, updateLawyerReviewDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getRating()).isEqualTo((short) 4);
        assertThat(response.getBody().getData().getReview()).isEqualTo("Good lawyer, satisfied with service");
    }

    @Test
    void deleteReview_shouldReturnOk() {
        ApiResponse<String> apiResponse = ApiResponse.success("Review deleted successfully", HttpStatus.OK, "Review deleted successfully").getBody();
        when(lawyerDirectoryService.deleteReview(testReviewId))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<String>> response = 
                lawyerDirectoryController.deleteReview(testReviewId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo("Review deleted successfully");
    }

    @Test
    void getReview_shouldReturnOk() {
        ApiResponse<LawyerReviewResponseDTO> apiResponse = ApiResponse.success(lawyerReviewResponseDTO, HttpStatus.OK, "Review retrieved successfully").getBody();
        when(lawyerDirectoryService.getReview(testReviewId))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> response = 
                lawyerDirectoryController.getReview(testReviewId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo(testReviewId);
        assertThat(response.getBody().getData().getLawyerId()).isEqualTo(testLawyerId);
    }

    @Test
    void getReviews_shouldReturnOk() {
        LawyerReviewListResponseDTO reviewListResponse = new LawyerReviewListResponseDTO(List.of(lawyerReviewResponseDTO));
        ApiResponse<LawyerReviewListResponseDTO> apiResponse = ApiResponse.success(reviewListResponse, HttpStatus.OK, "Reviews retrieved successfully").getBody();
        when(lawyerDirectoryService.getReviews(any(UUID.class), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewListResponseDTO>> response = 
                lawyerDirectoryController.getReviews(testLawyerId, 0, 10, "DESC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getReviews()).hasSize(1);
        assertThat(response.getBody().getData().getReviews().get(0).getLawyerId()).isEqualTo(testLawyerId);
    }

    @Test
    void getReviews_withCustomPagination_shouldReturnOk() {
        LawyerReviewListResponseDTO reviewListResponse = new LawyerReviewListResponseDTO(List.of(lawyerReviewResponseDTO));
        ApiResponse<LawyerReviewListResponseDTO> apiResponse = ApiResponse.success(reviewListResponse, HttpStatus.OK, "Reviews retrieved successfully").getBody();
        when(lawyerDirectoryService.getReviews(any(UUID.class), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewListResponseDTO>> response = 
                lawyerDirectoryController.getReviews(testLawyerId, 2, 5, "ASC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void getReview_shouldReturnNotFound() {
        @SuppressWarnings("unchecked")
        ApiResponse<LawyerReviewResponseDTO> apiResponse = (ApiResponse<LawyerReviewResponseDTO>) (ApiResponse<?>) ApiResponse.error("Review not found", HttpStatus.NOT_FOUND).getBody();
        when(lawyerDirectoryService.getReview(testReviewId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> response = 
                lawyerDirectoryController.getReview(testReviewId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isNotNull();
    }

    @Test
    void updateReview_shouldReturnNotFound() {
        @SuppressWarnings("unchecked")
        ApiResponse<LawyerReviewResponseDTO> apiResponse = (ApiResponse<LawyerReviewResponseDTO>) (ApiResponse<?>) ApiResponse.error("Review not found", HttpStatus.NOT_FOUND).getBody();
        when(lawyerDirectoryService.updateReview(any(UUID.class), any(UpdateLawyerReviewDTO.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> response = 
                lawyerDirectoryController.updateReview(testReviewId, updateLawyerReviewDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isNotNull();
    }

    @Test
    void deleteReview_shouldReturnNotFound() {
        @SuppressWarnings("unchecked")
        ApiResponse<String> apiResponse = (ApiResponse<String>) (ApiResponse<?>) ApiResponse.error("Review not found", HttpStatus.NOT_FOUND).getBody();
        when(lawyerDirectoryService.deleteReview(testReviewId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        ResponseEntity<ApiResponse<String>> response = 
                lawyerDirectoryController.deleteReview(testReviewId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isNotNull();
    }

    @Test
    void findLawyers_shouldReturnEmptyList() {
        List<LawyerSearchResultDTO> emptyResults = List.of();
        ApiResponse<List<LawyerSearchResultDTO>> apiResponse = ApiResponse.success(emptyResults, HttpStatus.OK, "No lawyers found").getBody();
        when(lawyerDirectoryService.findLawyers(any(FindLawyersDTO.class), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<List<LawyerSearchResultDTO>>> response = 
                lawyerDirectoryController.findLawyers(findLawyersDTO, 0, 10, "DESC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData()).isEmpty();
    }

    @Test
    void getReviews_shouldReturnEmptyList() {
        LawyerReviewListResponseDTO emptyReviewList = new LawyerReviewListResponseDTO(List.of());
        ApiResponse<LawyerReviewListResponseDTO> apiResponse = ApiResponse.success(emptyReviewList, HttpStatus.OK, "No reviews found").getBody();
        when(lawyerDirectoryService.getReviews(any(UUID.class), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<LawyerReviewListResponseDTO>> response = 
                lawyerDirectoryController.getReviews(testLawyerId, 0, 10, "DESC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getReviews()).isEmpty();
    }
}