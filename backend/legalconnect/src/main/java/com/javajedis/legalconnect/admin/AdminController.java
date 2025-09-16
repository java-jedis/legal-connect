package com.javajedis.legalconnect.admin;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "D. Admin", description = "Admin endpoints for system management")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    /**
     * Get all lawyers with PENDING verification status with pagination.
     */
    @Operation(summary = "Get pending lawyers", description = "Retrieves all lawyers with PENDING verification status with pagination support.")
    @GetMapping("/lawyers/pending")
    public ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> getPendingLawyers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /admin/lawyers/pending called with page={}, size={}", page, size);

        return adminService.getLawyersByVerificationStatus(
                VerificationStatus.PENDING, page, size);
    }

    /**
     * Get all lawyers by verification status with pagination.
     */
    @Operation(summary = "Get lawyers by verification status", description = "Retrieves all lawyers filtered by verification status with pagination support.")
    @GetMapping("/lawyers")
    public ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> getLawyersByStatus(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /admin/lawyers called with status={}, page={}, size={}", status, page, size);

        return adminService.getLawyersByVerificationStatus(status, page, size);
    }

    /**
     * Approve a lawyer's verification.
     */
    @Operation(summary = "Approve lawyer verification", description = "Approves a lawyer's verification status from PENDING to APPROVED.")
    @PutMapping("/lawyers/{lawyerId}/approve")
    public ResponseEntity<ApiResponse<AdminLawyerDTO>> approveLawyer(@PathVariable UUID lawyerId) {
        log.info("PUT /admin/lawyers/{}/approve called", lawyerId);
        return adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.APPROVED);
    }

    /**
     * Reject a lawyer's verification.
     */
    @Operation(summary = "Reject lawyer verification", description = "Rejects a lawyer's verification status from PENDING to REJECTED.")
    @PutMapping("/lawyers/{lawyerId}/reject")
    public ResponseEntity<ApiResponse<AdminLawyerDTO>> rejectLawyer(@PathVariable UUID lawyerId) {
        log.info("PUT /admin/lawyers/{}/reject called", lawyerId);
        return adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.REJECTED);
    }

} 