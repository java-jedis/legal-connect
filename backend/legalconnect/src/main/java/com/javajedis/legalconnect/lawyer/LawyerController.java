package com.javajedis.legalconnect.lawyer;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.common.security.RequireVerifiedLawyer;
import com.javajedis.legalconnect.lawyer.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Tag(name = "3. Lawyer", description = "Lawyer profile management endpoints")
@RestController
@RequestMapping("/lawyer")
public class LawyerController {

    private final LawyerService lawyerService;
    private final LawyerAvailabilitySlotService lawyerAvailabilitySlotService;

    public LawyerController(LawyerService lawyerService, LawyerAvailabilitySlotService lawyerAvailabilitySlotService) {
        this.lawyerService = lawyerService;
        this.lawyerAvailabilitySlotService = lawyerAvailabilitySlotService;
    }

    /**
     * Create a new lawyer profile.
     */
    @Operation(summary = "Create lawyer profile", description = "Creates a new lawyer profile for the authenticated user.")
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<LawyerInfoDTO>> createLawyerProfile(@Valid @RequestBody LawyerProfileDTO lawyerProfileDTO) {
        log.info("POST /lawyer/profile called");
        return lawyerService.createLawyerProfile(lawyerProfileDTO);
    }

    /**
     * Get lawyer info by email or current user.
     */
    @Operation(summary = "Get lawyer info", description = "Retrieves lawyer information by email or current authenticated user.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<LawyerInfoDTO>> getLawyerInfo(@RequestParam(required = false) String email) {
        if (email != null) {
            log.info("GET /lawyer/profile?email={} called", email);
        } else {
            log.info("GET /lawyer/profile called (current user)");
        }
        return lawyerService.getLawyerInfo(email);
    }

    /**
     * Update the profile information of the authenticated lawyer.
     */
    @Operation(summary = "Update lawyer profile", description = "Updates the lawyer profile for the authenticated user, including firm, experience, practicing court, division, district, bio, and specializations.")
    @RequireVerifiedLawyer
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<LawyerInfoDTO>> updateLawyerProfile(@Valid @RequestBody LawyerProfileDTO lawyerProfileDTO) {
        log.info("PUT /lawyer/profile called");
        return lawyerService.updateLawyerProfile(lawyerProfileDTO);
    }

    /**
     * Upload bar certificate file.
     */
    @Operation(summary = "Upload lawyer credentials (bar certificate)", description = "Uploads a bar certificate file for the authenticated lawyer.")
    @PostMapping(value = "/upload-credentials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> uploadCredentials(@RequestPart("file") MultipartFile file) {
        log.info("POST /lawyer/upload-credentials called, file: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        return lawyerService.uploadLawyerCredentials(file);
    }

    /**
     * Download bar certificate file by email or current user.
     */
    @Operation(summary = "View lawyer credentials (bar certificate)", description = "Returns the bar certificate file for a lawyer or current authenticated lawyer.")
    @GetMapping("/view-credentials")
    public ResponseEntity<byte[]> viewCredentials(@RequestParam(required = false) String email) {
        if (email != null) {
            log.info("GET /lawyer/view-credentials?email={} called", email);
        } else {
            log.info("GET /lawyer/view-credentials called (current user)");
        }
        return lawyerService.viewLawyerCredentials(email);
    }

    /**
     * Create a new availability slot for the authenticated lawyer.
     */
    @Operation(summary = "Create availability slot", description = "Creates a new availability slot for the authenticated lawyer.")
    @RequireVerifiedLawyer
    @PostMapping("/availability-slots")
    public ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> createAvailabilitySlot(@Valid @RequestBody LawyerAvailabilitySlotDTO lawyerAvailabilitySlotDTO) {
        log.info("POST /lawyer/availability-slots called");
        return lawyerAvailabilitySlotService.createSlot(lawyerAvailabilitySlotDTO);
    }

    /**
     * Get all availability slots for the authenticated lawyer or by email.
     */
    @Operation(summary = "Get all availability slots", description = "Retrieves all availability slots for the authenticated lawyer or by email.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/availability-slots")
    public ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> getAllAvailabilitySlots(@RequestParam(required = false) String email) {
        if (email != null) {
            log.info("GET /lawyer/availability-slots?email={} called", email);
        } else {
            log.info("GET /lawyer/availability-slots called (current user)");
        }
        return lawyerAvailabilitySlotService.getAllSlots(email);
    }

    /**
     * Update an existing availability slot for the authenticated lawyer.
     */
    @Operation(summary = "Update availability slot", description = "Updates an existing availability slot for the authenticated lawyer.")
    @RequireVerifiedLawyer
    @PutMapping("/availability-slots/{slotId}")
    public ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> updateAvailabilitySlot(@PathVariable UUID slotId, @Valid @RequestBody LawyerAvailabilitySlotDTO lawyerAvailabilitySlotDTO) {
        log.info("PUT /lawyer/availability-slots/{} called", slotId);
        return lawyerAvailabilitySlotService.updateSlot(slotId, lawyerAvailabilitySlotDTO);
    }

    /**
     * Delete an availability slot for the authenticated lawyer.
     */
    @Operation(summary = "Delete availability slot", description = "Deletes an availability slot for the authenticated lawyer.")
    @RequireVerifiedLawyer
    @DeleteMapping("/availability-slots/{slotId}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailabilitySlot(@PathVariable UUID slotId) {
        log.info("DELETE /lawyer/availability-slots/{} called", slotId);
        return lawyerAvailabilitySlotService.deleteSlot(slotId);
    }

} 