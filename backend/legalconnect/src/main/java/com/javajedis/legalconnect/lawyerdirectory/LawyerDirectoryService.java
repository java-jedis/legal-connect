package com.javajedis.legalconnect.lawyerdirectory;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyerdirectory.dto.*;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class LawyerDirectoryService {
    private static final String REVIEW_NOT_FOUND_MSG = "Review not found";
    private static final String CREATED_AT_FIELD = "createdAt";
    private final LawyerRepo lawyerRepo;
    private final LawyerReviewRepo lawyerReviewRepo;
    private final UserRepo userRepo;
    private final CaseRepo caseRepo;

    public LawyerDirectoryService(
            LawyerRepo lawyerRepo,
            LawyerReviewRepo lawyerReviewRepo,
            UserRepo userRepo,
            CaseRepo caseRepo) {
        this.lawyerRepo = lawyerRepo;
        this.lawyerReviewRepo = lawyerReviewRepo;
        this.userRepo = userRepo;
        this.caseRepo = caseRepo;
    }

    /**
     * Finds lawyers based on provided criteria with pagination and sorting.
     */
    public ResponseEntity<ApiResponse<List<LawyerSearchResultDTO>>> findLawyers(
            FindLawyersDTO dto,
            int page, int size, String sortDirection) {
        log.info("Finding lawyers with filters: {}, page: {}, size: {}, sort: {}", dto, page, size, sortDirection);
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Object[]> rawResults = lawyerRepo.findVerifiedLawyerRawResultsByCriteria(
                dto.getMinExperience(),
                dto.getMaxExperience(),
                dto.getPracticingCourt() != null ? dto.getPracticingCourt().name() : null,
                dto.getDivision() != null ? dto.getDivision().name() : null,
                dto.getDistrict() != null ? dto.getDistrict().name() : null,
                dto.getSpecialization() != null ? dto.getSpecialization().name() : null,
                pageable
        );
        List<LawyerSearchResultDTO> lawyerList = rawResults.stream().map(row ->
                LawyerSearchResultDTO.builder()
                        .lawyerId((UUID) row[0])
                        .userId((UUID) row[1])
                        .firstName((String) row[2])
                        .lastName((String) row[3])
                        .email((String) row[4])
                        .firm((String) row[5])
                        .yearsOfExperience((Integer) row[6])
                        .practicingCourt(row[7] != null ? PracticingCourt.valueOf(row[7].toString()) : null)
                        .division(row[8] != null ? Division.valueOf(row[8].toString()) : null)
                        .district(row[9] != null ? District.valueOf(row[9].toString()) : null)
                        .bio((String) row[10])
                        .specializations(LawyerSearchResultDTO.parseSpecializations((String) row[11]))
                        .averageRating(row[12] != null ? ((Number) row[12]).doubleValue() : null)
                        .build()
        ).toList();

        Map<String, Object> metadata = buildPaginationMetadata(
                rawResults,
                sortDirection,
                CREATED_AT_FIELD,
                Map.of(
                        "minExperience", dto.getMinExperience(),
                        "maxExperience", dto.getMaxExperience(),
                        "practicingCourt", dto.getPracticingCourt(),
                        "division", dto.getDivision(),
                        "district", dto.getDistrict(),
                        "specialization", dto.getSpecialization()
                )
        );

        return ApiResponse.success(lawyerList, HttpStatus.OK, "Retrieved lawyers with the given filter", metadata);
    }

    /**
     * Adds a review for a lawyer by a client for a resolved case.
     */
    public ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> addReview(CreateLawyerReviewDTO reviewDTO) {
        log.info("Adding review for case: {} by client", reviewDTO.getCaseId());
        Case caseEntity = caseRepo.findById(reviewDTO.getCaseId()).orElse(null);
        if (caseEntity == null) {
            return ApiResponse.error("Case not found", HttpStatus.NOT_FOUND);
        }

        User client = caseEntity.getClient();
        User lawyer = caseEntity.getLawyer().getUser();

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (!client.getId().equals(currentUser.getId())) {
            return ApiResponse.error("You are not authorized to create review for this case", HttpStatus.FORBIDDEN);
        }
        if (!caseEntity.getStatus().equals(CaseStatus.RESOLVED)) {
            return ApiResponse.error("Can not create review for non resolved cases", HttpStatus.BAD_REQUEST);
        }

        LawyerReview lawyerReview = new LawyerReview();
        lawyerReview.setClient(client);
        lawyerReview.setLawyer(lawyer);
        lawyerReview.setCaseE(caseEntity);
        lawyerReview.setRating(reviewDTO.getRating());
        lawyerReview.setReview(reviewDTO.getReview());

        LawyerReview savedReview = lawyerReviewRepo.save(lawyerReview);
        LawyerReviewResponseDTO responseDTO = mapReviewToResponseDTO(savedReview);

        return ApiResponse.success(responseDTO, HttpStatus.CREATED, "Review added successfully");
    }

    /**
     * Updates an existing review. Only the review owner can update.
     */
    public ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> updateReview(UUID reviewId, UpdateLawyerReviewDTO reviewDTO) {
        log.info("Updating review with ID: {}", reviewId);
        LawyerReview existingReview = lawyerReviewRepo.findById(reviewId).orElse(null);
        if (existingReview == null) {
            return ApiResponse.error(REVIEW_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (!existingReview.getClient().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You are not authorized to update this review", HttpStatus.FORBIDDEN);
        }

        existingReview.setRating(reviewDTO.getRating());
        existingReview.setReview(reviewDTO.getReview());

        LawyerReview updatedReview = lawyerReviewRepo.save(existingReview);
        LawyerReviewResponseDTO responseDTO = mapReviewToResponseDTO(updatedReview);

        return ApiResponse.success(responseDTO, HttpStatus.OK, "Review updated successfully");
    }

    /**
     * Deletes a review. Only the review owner can delete.
     */
    public ResponseEntity<ApiResponse<String>> deleteReview(UUID reviewId) {
        log.info("Deleting review with ID: {}", reviewId);
        LawyerReview existingReview = lawyerReviewRepo.findById(reviewId).orElse(null);
        if (existingReview == null) {
            return ApiResponse.error(REVIEW_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (!existingReview.getClient().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You are not authorized to delete this review", HttpStatus.FORBIDDEN);
        }

        lawyerReviewRepo.delete(existingReview);

        return ApiResponse.success(null, HttpStatus.OK, "Review deleted successfully");
    }

    /**
     * Retrieves a review by its ID.
     */
    public ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> getReview(UUID reviewId) {
        log.info("Retrieving review with ID: {}", reviewId);
        LawyerReview review = lawyerReviewRepo.findById(reviewId).orElse(null);
        if (review == null) {
            return ApiResponse.error(REVIEW_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        LawyerReviewResponseDTO responseDTO = mapReviewToResponseDTO(review);
        return ApiResponse.success(responseDTO, HttpStatus.OK, "Review retrieved successfully");
    }

    /**
     * Retrieves all reviews for a lawyer with pagination and metadata.
     */
    public ResponseEntity<ApiResponse<LawyerReviewListResponseDTO>> getReviews(UUID lawyerId, int page, int size, String sortDirection) {
        log.info("Getting reviews for lawyer: {} page: {}, size: {}, sort: {}", lawyerId, page, size, sortDirection);
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LawyerReview> reviewPage = lawyerReviewRepo.findByLawyer_Id(lawyerId, pageable);

        List<LawyerReviewResponseDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::mapReviewToResponseDTO)
                .toList();

        Map<String, Object> metadata = buildPaginationMetadata(
                reviewPage,
                sortDirection,
                CREATED_AT_FIELD,
                Map.of("lawyerId", lawyerId.toString())
        );
        LawyerReviewListResponseDTO responseData = new LawyerReviewListResponseDTO(reviewDTOs);

        return ApiResponse.success(responseData, HttpStatus.OK, "Reviews retrieved successfully", metadata);
    }

    /**
     * Builds pagination metadata for paginated responses.
     */
    private Map<String, Object> buildPaginationMetadata(Page<?> page, String sortDirection, String sortField, Map<String, Object> appliedFilters) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", page.getTotalElements());
        metadata.put("pageNumber", page.getNumber());
        metadata.put("pageSize", page.getSize());
        metadata.put("totalPages", page.getTotalPages());
        metadata.put("hasNext", page.hasNext());
        metadata.put("hasPrevious", page.hasPrevious());
        metadata.put("isFirst", page.isFirst());
        metadata.put("isLast", page.isLast());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", sortField);
        metadata.put("appliedFilters", appliedFilters);
        return metadata;
    }

    private LawyerReviewResponseDTO mapReviewToResponseDTO(LawyerReview review) {
        return new LawyerReviewResponseDTO(
                review.getId(),
                review.getLawyer().getId(),
                review.getLawyer().getFirstName() + " " + review.getLawyer().getLastName(),
                review.getClient().getId(),
                review.getClient().getFirstName() + " " + review.getClient().getLastName(),
                review.getCaseE().getId(),
                review.getRating(),
                review.getReview(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
