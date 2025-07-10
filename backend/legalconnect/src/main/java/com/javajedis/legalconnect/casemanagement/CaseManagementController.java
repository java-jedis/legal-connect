package com.javajedis.legalconnect.casemanagement;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.casemanagement.dto.CaseListResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO;
import com.javajedis.legalconnect.casemanagement.dto.CreateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseDTO;
import com.javajedis.legalconnect.casemanagement.dto.UpdateCaseStatusDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "4. Case Management", description = "Case management endpoints")
@RestController
@RequestMapping("/case")
public class CaseManagementController {
    private final CaseManagementService caseManagementService;

    public CaseManagementController(CaseManagementService caseManagementService) {
        this.caseManagementService = caseManagementService;
    }

    /**
     * Create a new case for the authenticated verified lawyer.
     */
    @Operation(summary = "Create new case", description = "Creates a new case for the authenticated verified lawyer.")
    @PreAuthorize("hasRole('LAWYER') and @lawyerVerificationChecker.isVerifiedLawyer()")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> createCase(@Valid @RequestBody CreateCaseDTO caseData) {
        log.info("POST /case/ called");
        return caseManagementService.createCase(caseData);
    }

    /**
     * Get a single case by ID for authenticated user (lawyer or client).
     */
    @Operation(summary = "Get case by ID", description = "Retrieves a case by ID for the authenticated user (lawyer or client).")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> getCaseById(@PathVariable UUID caseId) {
        log.info("GET /case/{} called", caseId);
        return caseManagementService.getCaseById(caseId);
    }

    /**
     * Get all cases for authenticated user with pagination, filtering, and sorting.
     */
    @Operation(summary = "Get all user cases", description = "Retrieves all cases for the authenticated user with pagination, filtering by status, and sorting by updated date.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<CaseListResponseDTO>> getAllUserCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /case/ called with page={}, size={}, status={}, sortDirection={}", page, size, status, sortDirection);
        return caseManagementService.getAllUserCases(page, size, status, sortDirection);
    }

    /**
     * Update case title and description for the authenticated verified lawyer.
     */
    @Operation(summary = "Update case", description = "Updates the title and description of an existing case for the authenticated verified lawyer.")
    @PreAuthorize("hasRole('LAWYER') and @lawyerVerificationChecker.isVerifiedLawyer()")
    @PutMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> updateCase(
            @PathVariable UUID caseId,
            @Valid @RequestBody UpdateCaseDTO updateData) {
        log.info("PUT /case/{} called", caseId);
        return caseManagementService.updateCase(caseId, updateData);
    }

    /**
     * Update case status for the authenticated verified lawyer.
     */
    @Operation(summary = "Update case status", description = "Updates the status of an existing case for the authenticated verified lawyer.")
    @PreAuthorize("hasRole('LAWYER') and @lawyerVerificationChecker.isVerifiedLawyer()")
    @PutMapping("/{caseId}/status")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> updateCaseStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody UpdateCaseStatusDTO statusData) {
        log.info("PUT /case/{}/status called", caseId);
        return caseManagementService.updateCaseStatus(caseId, statusData);
    }
}
