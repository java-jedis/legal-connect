package com.javajedis.legalconnect.lawyer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.AwsService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LawyerService {
    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String LAWYER_PROFILE_EXISTS_MSG = "Lawyer profile already exists for this user";
    private static final String BAR_CERTIFICATE_EXISTS_MSG = "Bar certificate number already exists";

    private final LawyerRepo lawyerRepo;
    private final UserRepo userRepo;
    private final LawyerSpecializationRepo lawyerSpecializationRepo;
    private final AwsService awsService;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public LawyerService(LawyerRepo lawyerRepo, UserRepo userRepo, LawyerSpecializationRepo lawyerSpecializationRepo, AwsService awsService) {
        this.lawyerRepo = lawyerRepo;
        this.userRepo = userRepo;
        this.lawyerSpecializationRepo = lawyerSpecializationRepo;
        this.awsService = awsService;
    }

    /**
     * Create a new lawyer profile.
     */
    public ResponseEntity<ApiResponse<LawyerInfoDTO>> createLawyerProfile(LawyerProfileDTO lawyerProfileDTO) {
        log.debug("Creating lawyer profile");
        User user = getAuthenticatedLawyerUser();
        if (user == null) {
            log.warn("Unauthorized profile creation attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }
        if (lawyerRepo.existsByUser(user)) {
            log.warn("Profile already exists for user: {}", user.getEmail());
            return ApiResponse.error(LAWYER_PROFILE_EXISTS_MSG, HttpStatus.CONFLICT);
        }
        if (lawyerRepo.existsByBarCertificateNumber(lawyerProfileDTO.getBarCertificateNumber())) {
            log.warn("Bar certificate number exists: {}", lawyerProfileDTO.getBarCertificateNumber());
            return ApiResponse.error(BAR_CERTIFICATE_EXISTS_MSG, HttpStatus.CONFLICT);
        }
        Lawyer lawyer = new Lawyer();
        lawyer.setUser(user);
        lawyer.setFirm(lawyerProfileDTO.getFirm());
        lawyer.setYearsOfExperience(lawyerProfileDTO.getYearsOfExperience());
        lawyer.setBarCertificateNumber(lawyerProfileDTO.getBarCertificateNumber());
        lawyer.setPracticingCourt(lawyerProfileDTO.getPracticingCourt());
        lawyer.setDivision(lawyerProfileDTO.getDivision());
        lawyer.setDistrict(lawyerProfileDTO.getDistrict());
        lawyer.setBio(lawyerProfileDTO.getBio());
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        Lawyer savedLawyer = lawyerRepo.save(lawyer);
        log.info("Profile created for user: {}", user.getEmail());
        saveLawyerSpecializations(savedLawyer, lawyerProfileDTO.getSpecializations());
        LawyerInfoDTO lawyerInfoDTO = mapLawyerToLawyerInfoDTO(savedLawyer, lawyerProfileDTO.getSpecializations());
        return ApiResponse.success(lawyerInfoDTO, HttpStatus.CREATED, "Lawyer profile created successfully");
    }

    /**
     * Get lawyer info by email or current user.
     */
    public ResponseEntity<ApiResponse<LawyerInfoDTO>> getLawyerInfo(String email) {
        log.debug("Getting lawyer info for email: {}", email);
        User user;
        if (email != null) {
            user = userRepo.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("User not found for email: {}", email);
                return ApiResponse.error("User not found", HttpStatus.NOT_FOUND);
            }
            if (user.getRole() != com.javajedis.legalconnect.user.Role.LAWYER) {
                log.warn("User is not a lawyer: {}", email);
                return ApiResponse.error("User is not a lawyer", HttpStatus.BAD_REQUEST);
            }
        } else {
            user = getAuthenticatedLawyerUser();
            if (user == null) {
                log.warn("Unauthorized info request");
                return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
            }
        }
        Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);
        if (lawyer == null) {
            log.info("No profile found for user: {}", user.getEmail());
            LawyerInfoDTO emptyLawyerInfoDTO = new LawyerInfoDTO();
            return ApiResponse.success(emptyLawyerInfoDTO, HttpStatus.OK, "Lawyer profile has not been created yet");
        }
        List<SpecializationType> specializations = loadLawyerSpecializations(lawyer);
        LawyerInfoDTO lawyerInfoDTO = mapLawyerToLawyerInfoDTO(lawyer, specializations);
        log.info("Profile info returned for user: {}", user.getEmail());
        return ApiResponse.success(lawyerInfoDTO, HttpStatus.OK, "Lawyer info retrieved successfully");
    }

    /**
     * Upload bar certificate file.
     */
    public ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> uploadLawyerCredentials(MultipartFile file) {
        log.debug("Uploading bar certificate file: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        if (file.getSize() > 1_048_576) {
            log.warn("File too large: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            return ApiResponse.error("File size exceeds 1MB limit", HttpStatus.BAD_REQUEST);
        }
        User user = getAuthenticatedLawyerUser();
        if (user == null) {
            log.warn("Unauthorized credentials upload attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }
        Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);
        if (lawyer == null) {
            log.warn("No profile found for credentials upload, user: {}", user.getEmail());
            return ApiResponse.error("Lawyer profile not found", HttpStatus.NOT_FOUND);
        }
        String keyName = "lawyer-credentials/" + user.getId() + "/" + file.getOriginalFilename();
        try {
            String storedFileName = awsService.uploadFile(
                bucketName,
                keyName,
                file.getSize(),
                file.getContentType(),
                file.getInputStream()
            );
            lawyer.setBarCertificateFileUrl(storedFileName);
            lawyerRepo.save(lawyer);
            log.info("Bar certificate uploaded for user: {}", user.getEmail());
            return ApiResponse.success(new BarCertificateUploadResponseDTO(storedFileName), HttpStatus.OK, "Credentials uploaded successfully");
        } catch (IOException e) {
            log.error("Failed to upload credentials for user: {}", user.getEmail(), e);
            return ApiResponse.error("Failed to upload credentials", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Download bar certificate file by email or current user.
     */
    public ResponseEntity<byte[]> viewLawyerCredentials(String email) {
        log.debug("Downloading bar certificate file for email: {}", email);
        User user;
        if (email != null) {
            user = userRepo.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("User not found for email: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (user.getRole() != com.javajedis.legalconnect.user.Role.LAWYER) {
                log.warn("User is not a lawyer: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            user = getAuthenticatedLawyerUser();
            if (user == null) {
                log.warn("Unauthorized credentials download attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);
        if (lawyer == null || lawyer.getBarCertificateFileUrl() == null) {
            log.warn("No credentials found for user: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            byte[] fileBytes = awsService.downloadFile(bucketName, lawyer.getBarCertificateFileUrl()).toByteArray();
            log.info("Bar certificate downloaded for user: {}", user.getEmail());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + lawyer.getBarCertificateFileUrl());
            return ResponseEntity.ok().headers(headers).body(fileBytes);
        } catch (Exception e) {
            log.error("Failed to download credentials for user: {}", user.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validates and retrieves the authenticated lawyer user.
     *
     * @return the authenticated lawyer user, or null if validation fails
     */
    private User getAuthenticatedLawyerUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Map<String, Object> userInfo = GetUserUtil.getCurrentUserInfo(userRepo);
        if (userInfo.isEmpty()) {
            log.error("User information not found in the context");
            return null;
        }

        String email = (String) userInfo.get("email");
        User user = userRepo.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return null;
        }

        if (user.getRole() != com.javajedis.legalconnect.user.Role.LAWYER) {
            return null;
        }

        return user;
    }

    /**
     * Saves specializations for a lawyer.
     *
     * @param lawyer          the lawyer entity
     * @param specializations the list of specializations to save
     */
    private void saveLawyerSpecializations(Lawyer lawyer, List<SpecializationType> specializations) {
        if (specializations != null && !specializations.isEmpty()) {
            for (SpecializationType specializationType : specializations) {
                LawyerSpecialization specialization = new LawyerSpecialization();
                specialization.setLawyer(lawyer);
                specialization.setSpecializationType(specializationType);
                lawyerSpecializationRepo.save(specialization);
            }
            log.info("Specializations saved successfully for lawyer: {}", lawyer.getId());
        }
    }

    /**
     * Loads specializations for a lawyer.
     *
     * @param lawyer the lawyer entity
     * @return the list of specialization types
     */
    private List<SpecializationType> loadLawyerSpecializations(Lawyer lawyer) {
        List<LawyerSpecialization> lawyerSpecializations = lawyerSpecializationRepo.findByLawyer(lawyer);
        return lawyerSpecializations.stream()
                .map(LawyerSpecialization::getSpecializationType)
                .toList();
    }

    /**
     * Maps a Lawyer entity to LawyerInfoDTO.
     *
     * @param lawyer          the lawyer entity to map
     * @param specializations the list of specializations for the lawyer
     * @return the mapped LawyerInfoDTO
     */
    private LawyerInfoDTO mapLawyerToLawyerInfoDTO(Lawyer lawyer, List<SpecializationType> specializations) {
        LawyerInfoDTO lawyerInfoDTO = new LawyerInfoDTO();
        lawyerInfoDTO.setFirm(lawyer.getFirm());
        lawyerInfoDTO.setYearsOfExperience(lawyer.getYearsOfExperience());
        lawyerInfoDTO.setBarCertificateNumber(lawyer.getBarCertificateNumber());
        lawyerInfoDTO.setPracticingCourt(lawyer.getPracticingCourt());
        lawyerInfoDTO.setDivision(lawyer.getDivision());
        lawyerInfoDTO.setDistrict(lawyer.getDistrict());
        lawyerInfoDTO.setBio(lawyer.getBio());
        lawyerInfoDTO.setVerificationStatus(lawyer.getVerificationStatus());
        lawyerInfoDTO.setLawyerCreatedAt(lawyer.getCreatedAt());
        lawyerInfoDTO.setLawyerUpdatedAt(lawyer.getUpdatedAt());
        lawyerInfoDTO.setSpecializations(specializations != null ? specializations : List.of());
        return lawyerInfoDTO;
    }

} 