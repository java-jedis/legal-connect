package com.javajedis.legalconnect.casemanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.javajedis.legalconnect.casemanagement.dto.CaseListResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CreateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseStatusDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.exception.UserNotFoundException;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CaseManagementService {
    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String CASE_NOT_FOUND_LOG = "Case not found with ID: {}";
    private static final String CASE_NOT_FOUND_MSG = "Case not found";
    private static final String NO_LAWYER_PROFILE_LOG = "No lawyer profile found for user: {}";
    private static final String LAWYER_PROFILE_NOT_EXIST_MSG = "Lawyer profile does not exist";
    private static final String CASE_OWNERSHIP_ERROR_MSG = "You can only update your own cases";

    private final CaseRepo caseRepo;
    private final UserRepo userRepo;
    private final LawyerRepo lawyerRepo;

    public CaseManagementService(CaseRepo caseRepo,
                                 UserRepo userRepo,
                                 LawyerRepo lawyerRepo) {
        this.caseRepo = caseRepo;
        this.userRepo = userRepo;
        this.lawyerRepo = lawyerRepo;
    }

    /**
     * Create a case for the authenticated lawyer.
     */
    public ResponseEntity<ApiResponse<CaseResponseDTO>> createCase(CreateCaseDTO caseData) {
        log.debug("Creating case");
        User user = LawyerUtil.getAuthenticatedLawyerUser(userRepo);

        if (user == null) {
            log.warn("Unauthorized case creation attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);

        if (lawyer == null) {
            log.warn(NO_LAWYER_PROFILE_LOG, user.getEmail());
            return ApiResponse.error(LAWYER_PROFILE_NOT_EXIST_MSG, HttpStatus.NOT_FOUND);
        }

        User client = userRepo.findByEmail(caseData.getClientEmail()).orElseThrow(() -> {
            log.warn("Client could not be found with email: {}", caseData.getClientEmail());
            return new UserNotFoundException("No client is found with this email address");
        });

        Case newCase = new Case();
        newCase.setLawyer(lawyer);
        newCase.setClient(client);
        newCase.setTitle(caseData.getTitle());

        String description = caseData.getDescription();
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }
        newCase.setDescription(description);

        Case savedCase = caseRepo.save(newCase);
        log.info("New case created for lawyer: {} client: {}", user.getEmail(), client.getEmail());

        CaseResponseDTO caseResponse = mapCaseToCaseResponseDTO(savedCase);
        return ApiResponse.success(caseResponse, HttpStatus.CREATED, "Case created successfully");
    }

    /**
     * Updates case title and description for the authenticated lawyer.
     */
    public ResponseEntity<ApiResponse<CaseResponseDTO>> updateCase(UUID caseId, UpdateCaseDTO updateData) {
        log.debug("Updating case with ID: {}", caseId);
        
        LawyerCaseValidationResult validation = validateLawyerAndCase(caseId, "update");
        if (validation.hasError()) {
            return validation.errorResponse;
        }

        Case existingCase = validation.caseEntity;
        existingCase.setTitle(updateData.getTitle());
        String description = updateData.getDescription();
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }
        existingCase.setDescription(description);

        Case updatedCase = caseRepo.save(existingCase);
        log.info("Case {} updated by lawyer: {}", caseId, validation.user.getEmail());

        CaseResponseDTO caseResponse = mapCaseToCaseResponseDTO(updatedCase);
        return ApiResponse.success(caseResponse, HttpStatus.OK, "Case updated successfully");
    }

    /**
     * Updates case status for the authenticated lawyer.
     */
    public ResponseEntity<ApiResponse<CaseResponseDTO>> updateCaseStatus(UUID caseId, UpdateCaseStatusDTO statusData) {
        log.debug("Updating case status with ID: {}", caseId);
        
        LawyerCaseValidationResult validation = validateLawyerAndCase(caseId, "update case status");
        if (validation.hasError()) {
            return validation.errorResponse;
        }

        Case existingCase = validation.caseEntity;
        existingCase.setStatus(statusData.getStatus());

        Case updatedCase = caseRepo.save(existingCase);
        log.info("Case {} status updated to {} by lawyer: {}", caseId, statusData.getStatus(), validation.user.getEmail());

        CaseResponseDTO caseResponse = mapCaseToCaseResponseDTO(updatedCase);
        return ApiResponse.success(caseResponse, HttpStatus.OK, "Case status updated successfully");
    }

    /**
     * Gets a single case by ID for authenticated user (lawyer or client).
     *
     * @param caseId the ID of the case to retrieve
     * @return ResponseEntity with case data or error
     */
    public ResponseEntity<ApiResponse<CaseResponseDTO>> getCaseById(UUID caseId) {
        log.debug("Getting case with ID: {}", caseId);
        User user = GetUserUtil.getAuthenticatedUser(userRepo);

        if (user == null) {
            log.warn("Unauthorized case access attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Case caseEntity = caseRepo.findById(caseId).orElse(null);
        if (caseEntity == null) {
            log.warn(CASE_NOT_FOUND_LOG, caseId);
            return ApiResponse.error(CASE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        boolean hasAccess = caseEntity.getClient().getId().equals(user.getId())
                || caseEntity.getLawyer().getUser().getId().equals(user.getId());

        if (!hasAccess) {
            log.warn("User {} attempted to access case {} without permission", user.getEmail(), caseId);
            return ApiResponse.error("You don't have permission to view this case", HttpStatus.FORBIDDEN);
        }

        CaseResponseDTO caseResponse = mapCaseToCaseResponseDTO(caseEntity);
        log.info("Case {} retrieved by user: {}", caseId, user.getEmail());
        return ApiResponse.success(caseResponse, HttpStatus.OK, "Case retrieved successfully");
    }

    /**
     * Gets all cases for authenticated user with pagination, filtering, and sorting.
     *
     * @param page          page number (0-based)
     * @param size          page size
     * @param status        optional status filter
     * @param sortDirection sort direction (ASC/DESC)
     * @return ResponseEntity with paginated case list and metadata
     */
    public ResponseEntity<ApiResponse<CaseListResponseDTO>> getAllUserCases(
            int page, int size, CaseStatus status, String sortDirection) {
        log.debug("Getting cases for user with page={}, size={}, status={}, sort={}", page, size, status, sortDirection);

        User user = GetUserUtil.getAuthenticatedUser(userRepo);

        if (user == null) {
            log.warn("Unauthorized case list access attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "updatedAt")
                .and(Sort.by(Sort.Direction.ASC, "status"));

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Case> casePage;
        if (status != null) {
            casePage = caseRepo.findByUserAsLawyerOrClientAndStatus(user, status, pageable);
        } else {
            casePage = caseRepo.findByUserAsLawyerOrClient(user, pageable);
        }

        List<CaseResponseDTO> caseResponses = casePage.getContent().stream()
                .map(this::mapCaseToCaseResponseDTO)
                .toList();

        CaseListResponseDTO responseData = new CaseListResponseDTO(caseResponses);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", casePage.getTotalElements());
        metadata.put("pageNumber", casePage.getNumber());
        metadata.put("pageSize", casePage.getSize());
        metadata.put("totalPages", casePage.getTotalPages());
        metadata.put("hasNext", casePage.hasNext());
        metadata.put("hasPrevious", casePage.hasPrevious());
        metadata.put("isFirst", casePage.isFirst());
        metadata.put("isLast", casePage.isLast());
        metadata.put("appliedFilters", status != null ? Map.of("status", status.name()) : Map.of());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", "updatedAt");

        log.info("Retrieved {} cases for user: {} (page {}/{})",
                caseResponses.size(), user.getEmail(), page + 1, casePage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, "Cases retrieved successfully", metadata);
    }

    /**
     * Maps a Case entity to CaseResponseDTO.
     *
     * @param caseEntity the Case entity to map
     * @return the mapped CaseResponseDTO
     */
    private CaseResponseDTO mapCaseToCaseResponseDTO(Case caseEntity) {
        CaseResponseDTO.LawyerSummaryDTO lawyerSummary = new CaseResponseDTO.LawyerSummaryDTO(
                caseEntity.getLawyer().getId(),
                caseEntity.getLawyer().getUser().getFirstName(),
                caseEntity.getLawyer().getUser().getLastName(),
                caseEntity.getLawyer().getUser().getEmail(),
                caseEntity.getLawyer().getFirm()
        );

        CaseResponseDTO.ClientSummaryDTO clientSummary = new CaseResponseDTO.ClientSummaryDTO(
                caseEntity.getClient().getId(),
                caseEntity.getClient().getFirstName(),
                caseEntity.getClient().getLastName(),
                caseEntity.getClient().getEmail()
        );

        return new CaseResponseDTO(
                caseEntity.getId(),
                lawyerSummary,
                clientSummary,
                caseEntity.getTitle(),
                caseEntity.getDescription(),
                caseEntity.getStatus(),
                caseEntity.getCreatedAt(),
                caseEntity.getUpdatedAt()
        );
    }

    /**
     * Validates lawyer authentication and case ownership for update operations.
     *
     * @param caseId the ID of the case to validate
     * @param operation the operation being performed (for logging)
     * @return LawyerCaseValidationResult containing validation results
     */
    private LawyerCaseValidationResult validateLawyerAndCase(UUID caseId, String operation) {
        User user = LawyerUtil.getAuthenticatedLawyerUser(userRepo);

        if (user == null) {
            log.warn("Unauthorized case {} attempt", operation);
            return new LawyerCaseValidationResult(null, null, null, 
                ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED));
        }

        Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);

        if (lawyer == null) {
            log.warn(NO_LAWYER_PROFILE_LOG, user.getEmail());
            return new LawyerCaseValidationResult(user, null, null,
                ApiResponse.error(LAWYER_PROFILE_NOT_EXIST_MSG, HttpStatus.NOT_FOUND));
        }

        Case existingCase = caseRepo.findById(caseId).orElse(null);
        if (existingCase == null) {
            log.warn(CASE_NOT_FOUND_LOG, caseId);
            return new LawyerCaseValidationResult(user, lawyer, null,
                ApiResponse.error(CASE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));
        }

        if (!existingCase.getLawyer().getId().equals(lawyer.getId())) {
            log.warn("Lawyer {} attempted to {} case {} that doesn't belong to them", user.getEmail(), operation, caseId);
            return new LawyerCaseValidationResult(user, lawyer, existingCase,
                ApiResponse.error(CASE_OWNERSHIP_ERROR_MSG, HttpStatus.FORBIDDEN));
        }

        return new LawyerCaseValidationResult(user, lawyer, existingCase, null);
    }

    /**
     * Helper record to hold lawyer and case validation results.
     *
     * @param user the authenticated user
     * @param lawyer the lawyer entity
     * @param caseEntity the case entity
     * @param errorResponse the error response if validation failed
     */
    private record LawyerCaseValidationResult(
            User user,
            Lawyer lawyer,
            Case caseEntity,
            ResponseEntity<ApiResponse<CaseResponseDTO>> errorResponse
    ) {
        boolean hasError() {
            return errorResponse != null;
        }
    }

}
