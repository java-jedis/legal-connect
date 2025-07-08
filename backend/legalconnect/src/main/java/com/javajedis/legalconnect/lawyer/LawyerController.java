package com.javajedis.legalconnect.lawyer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javajedis.legalconnect.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "3. Lawyer", description = "Lawyer profile management endpoints")
@RestController
@RequestMapping("/lawyer")
public class LawyerController {

    private final LawyerService lawyerService;

    public LawyerController(LawyerService lawyerService) {
        this.lawyerService = lawyerService;
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

} 