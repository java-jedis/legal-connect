package com.javajedis.legalconnect.admin;

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

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerSpecialization;
import com.javajedis.legalconnect.lawyer.LawyerSpecializationRepo;
import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.user.UserInfoResponseDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminService {

    private final LawyerRepo lawyerRepo;
    private final LawyerSpecializationRepo lawyerSpecializationRepo;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public AdminService(LawyerRepo lawyerRepo,
                        LawyerSpecializationRepo lawyerSpecializationRepo,
                        NotificationService notificationService,
                        EmailService emailService) {
        this.lawyerRepo = lawyerRepo;
        this.lawyerSpecializationRepo = lawyerSpecializationRepo;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Get lawyers by verification status with pagination support.
     */
    public ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> getLawyersByVerificationStatus(
            VerificationStatus status, int page, int size) {

        // Always sort by updatedAt with older records first (ascending)
        Sort sort = Sort.by(Sort.Direction.ASC, "updatedAt");

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Lawyer> lawyerPage;
        if (status != null) {
            lawyerPage = lawyerRepo.findByVerificationStatus(status, pageable);
        } else {
            lawyerPage = lawyerRepo.findAll(pageable);
        }

        List<AdminLawyerDTO> lawyerDTOs = lawyerPage.getContent().stream()
                .map(this::convertToAdminLawyerDTO)
                .toList();

        AdminLawyerListResponseDTO response = new AdminLawyerListResponseDTO(lawyerDTOs);

        Map<String, Object> metadata = Map.of(
                "totalCount", lawyerPage.getTotalElements(),
                "pageNumber", lawyerPage.getNumber(),
                "pageSize", lawyerPage.getSize(),
                "totalPages", lawyerPage.getTotalPages(),
                "hasNext", lawyerPage.hasNext(),
                "hasPrevious", lawyerPage.hasPrevious()
        );

        String message = status != null
                ? String.format("Retrieved %d lawyers with %s verification status", lawyerDTOs.size(), status)
                : String.format("Retrieved %d lawyers", lawyerDTOs.size());

        return ApiResponse.success(response, HttpStatus.OK, message, metadata);
    }

    /**
     * Update lawyer verification status (approve/reject).
     */
    public ResponseEntity<ApiResponse<AdminLawyerDTO>> updateLawyerVerificationStatus(
            UUID lawyerId, VerificationStatus status) {

        Lawyer lawyer = lawyerRepo.findById(lawyerId)
                .orElse(null);

        if (lawyer == null) {
            return ApiResponse.error("Lawyer not found", HttpStatus.NOT_FOUND);
        }

        lawyer.setVerificationStatus(status);
        Lawyer updatedLawyer = lawyerRepo.save(lawyer);
        
        UUID lawyerUserId = lawyer.getUser().getId();
        String lawyerEmail = lawyer.getUser().getEmail();
        
        String subject = "Lawyer Verification Status Updated";
        String content = String.format("Your lawyer verification status has been updated to %s by the admin.", status);
        
  
        notificationService.sendNotification(lawyerUserId, content);
        log.info("Web push notification sent to lawyer: {} for verification status update", lawyerEmail);
   
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("notificationType", "Verification Status Update");
        templateVariables.put("content", content);
            
        emailService.sendTemplateEmail(
            lawyerEmail,
            subject,
            "notification-email",
            templateVariables
            );

        AdminLawyerDTO lawyerDTO = convertToAdminLawyerDTO(updatedLawyer);

        String message = String.format("Lawyer verification status updated to %s", status);
        return ApiResponse.success(lawyerDTO, HttpStatus.OK, message);
    }

    /**
     * Convert Lawyer entity to AdminLawyerDTO.
     */
    private AdminLawyerDTO convertToAdminLawyerDTO(Lawyer lawyer) {
        UserInfoResponseDTO userDto = null;
        if (lawyer.getUser() != null) {
            userDto = new UserInfoResponseDTO();
            userDto.setFirstName(lawyer.getUser().getFirstName());
            userDto.setLastName(lawyer.getUser().getLastName());
            userDto.setEmail(lawyer.getUser().getEmail());
            userDto.setRole(lawyer.getUser().getRole());
            userDto.setEmailVerified(lawyer.getUser().isEmailVerified());
            userDto.setCreatedAt(lawyer.getUser().getCreatedAt());
            userDto.setUpdatedAt(lawyer.getUser().getUpdatedAt());
        }

        LawyerInfoDTO lawyerDto = new LawyerInfoDTO(
                lawyer.getUser().getId(),
                lawyer.getUser().getFirstName(),
                lawyer.getUser().getLastName(),
                lawyer.getUser().getEmail(),
                lawyer.getFirm(),
                lawyer.getYearsOfExperience(),
                lawyer.getBarCertificateNumber(),
                lawyer.getPracticingCourt(),
                lawyer.getDivision(),
                lawyer.getDistrict(),
                lawyer.getBio(),
                lawyer.getVerificationStatus(),
                lawyer.getCreatedAt(),
                lawyer.getUpdatedAt(),
                lawyerSpecializationRepo.findByLawyer(lawyer)
                        .stream()
                        .map(LawyerSpecialization::getSpecializationType)
                        .toList(),
                lawyer.getHourlyCharge(),
                lawyer.getCompleteProfile()
        );

        return new AdminLawyerDTO(
                lawyer.getUser() != null ? lawyer.getUser().getId() : null,
                userDto,
                lawyer.getId(),
                lawyerDto,
                lawyer.getBarCertificateFileUrl()
        );
    }
} 
