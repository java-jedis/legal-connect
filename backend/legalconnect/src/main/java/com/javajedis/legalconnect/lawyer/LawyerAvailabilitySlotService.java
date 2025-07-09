package com.javajedis.legalconnect.lawyer;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotListResponseDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotResponseDTO;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LawyerAvailabilitySlotService {
    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String NO_PROFILE_FOUND_MSG = "No profile found for user: {}";

    private final UserRepo userRepo;
    private final LawyerRepo lawyerRepo;
    private final LawyerAvailabilitySlotRepo lawyerAvailabilitySlotRepo;

    public LawyerAvailabilitySlotService(UserRepo userRepo,
                                         LawyerRepo lawyerRepo,
                                         LawyerAvailabilitySlotRepo lawyerAvailabilitySlotRepo) {
        this.userRepo = userRepo;
        this.lawyerRepo = lawyerRepo;
        this.lawyerAvailabilitySlotRepo = lawyerAvailabilitySlotRepo;
    }

    /**
     * Create a new availability slot for the authenticated lawyer.
     */
    public ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> createSlot(LawyerAvailabilitySlotDTO lawyerAvailabilitySlotData) {
        log.debug("Creating lawyer availability slot");
        Lawyer lawyer = getAuthenticatedLawyerOrError();
        if (lawyer == null) {
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        LawyerAvailabilitySlot lawyerAvailabilitySlot = new LawyerAvailabilitySlot();
        lawyerAvailabilitySlot.setLawyer(lawyer);
        lawyerAvailabilitySlot.setDay(lawyerAvailabilitySlotData.getDay());
        lawyerAvailabilitySlot.setStartTime(lawyerAvailabilitySlotData.getStartTime());
        lawyerAvailabilitySlot.setEndTime(lawyerAvailabilitySlotData.getEndTime());
        LawyerAvailabilitySlot savedSlot = lawyerAvailabilitySlotRepo.save(lawyerAvailabilitySlot);
        log.info("Slot created for lawyer: {}", lawyer.getUser().getEmail());

        LawyerAvailabilitySlotResponseDTO responseDTO = mapToResponseDTO(savedSlot);
        return ApiResponse.success(responseDTO, HttpStatus.CREATED, "Availability slot created successfully");
    }

    /**
     * Update an existing availability slot for the authenticated lawyer.
     */
    public ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> updateSlot(java.util.UUID slotId, LawyerAvailabilitySlotDTO lawyerAvailabilitySlotData) {
        log.debug("Updating lawyer availability slot");
        Lawyer lawyer = getAuthenticatedLawyerOrError();
        if (lawyer == null) {
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Optional<LawyerAvailabilitySlot> slotOpt = lawyerAvailabilitySlotRepo.findById(slotId);
        if (slotOpt.isEmpty() || !slotOpt.get().getLawyer().getId().equals(lawyer.getId())) {
            log.warn("Slot not found or not owned by user: {}", lawyer.getUser().getEmail());
            return ApiResponse.error("Slot not found or not owned by user", HttpStatus.NOT_FOUND);
        }
        LawyerAvailabilitySlot slot = slotOpt.get();
        slot.setDay(lawyerAvailabilitySlotData.getDay());
        slot.setStartTime(lawyerAvailabilitySlotData.getStartTime());
        slot.setEndTime(lawyerAvailabilitySlotData.getEndTime());
        LawyerAvailabilitySlot updatedSlot = lawyerAvailabilitySlotRepo.save(slot);
        log.info("Slot updated for lawyer: {}", lawyer.getUser().getEmail());

        LawyerAvailabilitySlotResponseDTO responseDTO = mapToResponseDTO(updatedSlot);
        return ApiResponse.success(responseDTO, HttpStatus.OK, "Availability slot updated successfully");
    }

    /**
     * Delete an availability slot for the authenticated lawyer.
     */
    public ResponseEntity<ApiResponse<Void>> deleteSlot(java.util.UUID slotId) {
        log.debug("Deleting lawyer availability slot");
        Lawyer lawyer = getAuthenticatedLawyerOrError();
        if (lawyer == null) {
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Optional<LawyerAvailabilitySlot> slotOpt = lawyerAvailabilitySlotRepo.findById(slotId);
        if (slotOpt.isEmpty() || !slotOpt.get().getLawyer().getId().equals(lawyer.getId())) {
            log.warn("Slot not found or not owned by user: {}", lawyer.getUser().getEmail());
            return ApiResponse.error("Slot not found or not owned by user", HttpStatus.NOT_FOUND);
        }
        lawyerAvailabilitySlotRepo.delete(slotOpt.get());
        log.info("Slot deleted for lawyer: {}", lawyer.getUser().getEmail());
        return ApiResponse.success(null, HttpStatus.NO_CONTENT, "Availability slot deleted successfully");
    }

    /**
     * Get all availability slots for the authenticated lawyer or by email.
     */
    public ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> getAllSlots(String email) {
        log.debug("Getting lawyer availability slots for email: {}", email);
        Lawyer lawyer = getLawyerByEmailOrAuthenticatedOrError(email);
        if (lawyer == null) {
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        java.util.List<LawyerAvailabilitySlot> slots = lawyerAvailabilitySlotRepo.findByLawyerId(lawyer.getId());
        java.util.List<LawyerAvailabilitySlotResponseDTO> slotDTOs = slots.stream()
                .map(this::mapToResponseDTO)
                .toList();
        LawyerAvailabilitySlotListResponseDTO responseDTO = new LawyerAvailabilitySlotListResponseDTO();
        responseDTO.setSlots(slotDTOs);
        log.info("Retrieved {} slots for lawyer: {}", slots.size(), lawyer.getUser().getEmail());
        return ApiResponse.success(responseDTO, HttpStatus.OK, "Availability slots retrieved successfully");
    }

    /**
     * Gets the authenticated lawyer or returns null if error.
     *
     * @return the lawyer if found, null if error
     */
    private Lawyer getAuthenticatedLawyerOrError() {
        User user = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
        if (user == null) {
            log.warn("Unauthorized attempt");
            return null;
        }
        Optional<Lawyer> lawyer = lawyerRepo.findByUser(user);
        if (lawyer.isEmpty()) {
            log.warn(NO_PROFILE_FOUND_MSG, user.getEmail());
            return null;
        }
        return lawyer.get();
    }

    /**
     * Gets a lawyer by email or the authenticated lawyer.
     *
     * @param email the email to search for, or null for authenticated user
     * @return the lawyer if found, null if error
     */
    private Lawyer getLawyerByEmailOrAuthenticatedOrError(String email) {
        User user;
        if (email != null) {
            user = userRepo.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("User not found for email: {}", email);
                return null;
            }
            if (user.getRole() != com.javajedis.legalconnect.user.Role.LAWYER) {
                log.warn("User is not a lawyer: {}", email);
                return null;
            }
        } else {
            user = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            if (user == null) {
                log.warn("Unauthorized slots retrieval attempt");
                return null;
            }
        }
        Optional<Lawyer> lawyer = lawyerRepo.findByUser(user);
        if (lawyer.isEmpty()) {
            log.warn(NO_PROFILE_FOUND_MSG, user.getEmail());
            return null;
        }
        return lawyer.get();
    }

    /**
     * Maps a LawyerAvailabilitySlot entity to LawyerAvailabilitySlotResponseDTO.
     *
     * @param slot the slot entity to map
     * @return the mapped response DTO
     */
    private LawyerAvailabilitySlotResponseDTO mapToResponseDTO(LawyerAvailabilitySlot slot) {
        LawyerAvailabilitySlotResponseDTO dto = new LawyerAvailabilitySlotResponseDTO();
        dto.setId(slot.getId());
        dto.setDay(slot.getDay());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        return dto;
    }
}
