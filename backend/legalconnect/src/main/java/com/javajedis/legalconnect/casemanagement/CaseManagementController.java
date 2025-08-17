package com.javajedis.legalconnect.casemanagement;

import com.javajedis.legalconnect.casemanagement.dto.*;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.common.security.RequireVerifiedLawyer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "3. Case Management", description = "Case management endpoints")
@RestController
@RequestMapping("/case")
@RequiredArgsConstructor
public class CaseManagementController {
    private final CaseManagementService caseManagementService;

    /**
     * Create a new case for the authenticated verified lawyer.
     */
    @Operation(summary = "Create new case", description = "Creates a new case for the authenticated verified lawyer.")
    @RequireVerifiedLawyer
    @PostMapping("/")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> createCase(@Valid @RequestBody CreateCaseDTO caseData) {
        log.info("POST /case/ called");
        return caseManagementService.createCase(caseData);
    }

    /**
     * Get a single case by ID for authenticated user (lawyer or client).
     */
    @Operation(summary = "Get case by ID", description = "Retrieves a case by ID for the authenticated user (lawyer or client).")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> getCaseById(@PathVariable UUID caseId) {
        log.info("GET /case/{} called", caseId);
        return caseManagementService.getCaseById(caseId);
    }

    /**
     * Get all cases for authenticated user with pagination, filtering, and sorting.
     */
    @Operation(summary = "Get all user cases", description = "Retrieves all cases for the authenticated user with pagination, filtering by status, and sorting by updated date.")
    @RequireUserOrVerifiedLawyer
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
    @RequireVerifiedLawyer
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
    @RequireVerifiedLawyer
    @PutMapping("/{caseId}/status")
    public ResponseEntity<ApiResponse<CaseResponseDTO>> updateCaseStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody UpdateCaseStatusDTO statusData) {
        log.info("PUT /case/{}/status called", caseId);
        return caseManagementService.updateCaseStatus(caseId, statusData);
    }
}
