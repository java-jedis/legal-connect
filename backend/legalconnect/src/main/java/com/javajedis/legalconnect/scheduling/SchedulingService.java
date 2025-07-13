package com.javajedis.legalconnect.scheduling;

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

import com.javajedis.legalconnect.caseassets.CaseAssetUtility;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.scheduling.dto.CreateScheduleDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleListResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateScheduleDTO;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SchedulingService {

    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String SCHEDULE_NOT_FOUND_LOG = "Schedule not found with ID: {}";
    private static final String SCHEDULE_NOT_FOUND_MSG = "Schedule not found";
    private static final String CREATED_AT_FIELD = "createdAt";

    private final ScheduleRepo scheduleRepo;
    private final UserRepo userRepo;
    private final CaseRepo caseRepo;

    public SchedulingService(ScheduleRepo scheduleRepo,
                             UserRepo userRepo,
                             CaseRepo caseRepo) {
        this.scheduleRepo = scheduleRepo;
        this.userRepo = userRepo;
        this.caseRepo = caseRepo;
    }

    /**
     * Create a new schedule event for a case.
     */
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> createSchedule(CreateScheduleDTO eventData) {
        log.debug("Creating schedule for case ID: {}", eventData.getCaseId());

        CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        eventData.getCaseId(),
                        "create schedule",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        User client = validation.caseEntity().getClient();
        User lawyer = validation.caseEntity().getLawyer().getUser();

        Schedule schedule = new Schedule();
        schedule.setCaseEntity(validation.caseEntity());
        schedule.setClient(client);
        schedule.setLawyer(lawyer);
        schedule.setTitle(eventData.getTitle());
        schedule.setType(eventData.getType());
        schedule.setDescription(eventData.getDescription());
        schedule.setDate(eventData.getDate());
        schedule.setStartTime(eventData.getStartTime());
        schedule.setEndTime(eventData.getEndTime());

        Schedule savedSchedule = scheduleRepo.save(schedule);
        log.info("Schedule created for case {} by user: {}", eventData.getCaseId(), validation.user().getEmail());

        ScheduleResponseDTO scheduleResponse = mapToScheduleResponseDTO(savedSchedule);
        return ApiResponse.success(scheduleResponse, HttpStatus.CREATED, "Schedule created successfully");
    }

    /**
     * Update an existing schedule event.
     */
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> updateSchedule(UUID scheduleId, UpdateScheduleDTO updateData) {
        log.debug("Updating schedule with ID: {}", scheduleId);

        Schedule existingSchedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (existingSchedule == null) {
            log.warn(SCHEDULE_NOT_FOUND_LOG, scheduleId);
            return ApiResponse.error(SCHEDULE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validation =
                validateCaseAccess(existingSchedule.getCaseEntity().getId(), "update schedule");

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        existingSchedule.setTitle(updateData.getTitle());
        existingSchedule.setType(updateData.getType());
        existingSchedule.setDescription(updateData.getDescription());
        existingSchedule.setDate(updateData.getDate());
        existingSchedule.setStartTime(updateData.getStartTime());
        existingSchedule.setEndTime(updateData.getEndTime());

        Schedule updatedSchedule = scheduleRepo.save(existingSchedule);
        log.info("Schedule {} updated by user: {}", scheduleId, validation.user().getEmail());

        ScheduleResponseDTO scheduleResponse = mapToScheduleResponseDTO(updatedSchedule);
        return ApiResponse.success(scheduleResponse, HttpStatus.OK, "Schedule updated successfully");
    }

    /**
     * Delete a schedule event.
     */
    public ResponseEntity<ApiResponse<String>> deleteSchedule(UUID scheduleId) {
        log.debug("Deleting schedule with ID: {}", scheduleId);

        Schedule existingSchedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (existingSchedule == null) {
            log.warn(SCHEDULE_NOT_FOUND_LOG, scheduleId);
            return ApiResponse.error(SCHEDULE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<String> validation =
                validateCaseAccess(existingSchedule.getCaseEntity().getId(), "delete schedule");

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        scheduleRepo.delete(existingSchedule);
        log.info("Schedule {} deleted by user: {}", scheduleId, validation.user().getEmail());

        return ApiResponse.success("Schedule deleted successfully", HttpStatus.OK, "Schedule deleted successfully");
    }

    /**
     * Get a single schedule by ID.
     */
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> getScheduleById(UUID scheduleId) {
        log.debug("Getting schedule with ID: {}", scheduleId);

        Schedule schedule = scheduleRepo.findById(scheduleId).orElse(null);
        if (schedule == null) {
            log.warn(SCHEDULE_NOT_FOUND_LOG, scheduleId);
            return ApiResponse.error(SCHEDULE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<ScheduleResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        schedule.getCaseEntity().getId(),
                        "view schedule",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        log.info("Schedule {} retrieved by user: {}", scheduleId, validation.user().getEmail());
        ScheduleResponseDTO scheduleResponse = mapToScheduleResponseDTO(schedule);
        return ApiResponse.success(scheduleResponse, HttpStatus.OK, "Schedule retrieved successfully");
    }

    /**
     * Get all schedules for a case with pagination.
     */
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>> getAllSchedulesForCase(
            UUID caseId, int page, int size, String sortDirection) {
        log.debug("Getting schedules for case ID: {} with page={}, size={}, sort={}", caseId, page, size, sortDirection);

        CaseAssetUtility.CaseAssetValidationResult<ScheduleListResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        caseId,
                        "view case schedules",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Schedule> schedulePage = scheduleRepo.findByCaseEntityId(caseId, pageable);

        List<ScheduleResponseDTO> scheduleResponses = schedulePage.getContent().stream()
                .map(this::mapToScheduleResponseDTO)
                .toList();

        ScheduleListResponseDTO responseData = new ScheduleListResponseDTO(scheduleResponses);

        Map<String, Object> metadata = buildPaginationMetadata(
                schedulePage, 
                sortDirection, 
                Map.of("caseId", caseId.toString())
        );

        log.info("Retrieved {} schedules for case {} for user: {} (page {}/{})",
                scheduleResponses.size(), caseId, validation.user().getEmail(), page + 1, schedulePage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, "Schedules retrieved successfully", metadata);
    }

    /**
     * Get all schedules for the authenticated user with pagination.
     */
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>> getAllUserSchedules(
            int page, int size, String sortDirection) {
        log.debug("Getting schedules for user with page={}, size={}, sort={}", page, size, sortDirection);

        User user = GetUserUtil.getAuthenticatedUser(userRepo);
        if (user == null) {
            log.warn("Unauthorized schedule list access attempt");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Schedule> schedulePage = scheduleRepo.findByLawyerIdOrClientId(user.getId(), pageable);

        List<ScheduleResponseDTO> scheduleResponses = schedulePage.getContent().stream()
                .map(this::mapToScheduleResponseDTO)
                .toList();

        ScheduleListResponseDTO responseData = new ScheduleListResponseDTO(scheduleResponses);

        Map<String, Object> metadata = buildPaginationMetadata(schedulePage, sortDirection, null);

        log.info("Retrieved {} schedules for user: {} (page {}/{})",
                scheduleResponses.size(), user.getEmail(), page + 1, schedulePage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, "Schedules retrieved successfully", metadata);
    }

    /**
     * Maps a Schedule entity to ScheduleResponseDTO.
     *
     * @param schedule the Schedule entity to map
     * @return the mapped ScheduleResponseDTO
     */
    private ScheduleResponseDTO mapToScheduleResponseDTO(Schedule schedule) {
        ScheduleResponseDTO.CaseSummaryDTO caseSummaryDTO = new ScheduleResponseDTO.CaseSummaryDTO(
                schedule.getCaseEntity().getId(),
                schedule.getCaseEntity().getTitle(),
                schedule.getCaseEntity().getStatus()
        );

        ScheduleResponseDTO.LawyerSummaryDTO lawyerSummaryDTO = new ScheduleResponseDTO.LawyerSummaryDTO(
                schedule.getLawyer().getId(),
                schedule.getLawyer().getFirstName(),
                schedule.getLawyer().getLastName(),
                schedule.getLawyer().getEmail()
        );

        ScheduleResponseDTO.ClientSummaryDTO clientSummaryDTO = new ScheduleResponseDTO.ClientSummaryDTO(
                schedule.getClient().getId(),
                schedule.getClient().getFirstName(),
                schedule.getClient().getLastName(),
                schedule.getClient().getEmail()
        );

        ScheduleResponseDTO scheduleResponseDTO = new ScheduleResponseDTO();
        scheduleResponseDTO.setId(schedule.getId());
        scheduleResponseDTO.setCaseInfo(caseSummaryDTO);
        scheduleResponseDTO.setLawyerInfo(lawyerSummaryDTO);
        scheduleResponseDTO.setClientInfo(clientSummaryDTO);
        scheduleResponseDTO.setTitle(schedule.getTitle());
        scheduleResponseDTO.setType(schedule.getType());
        scheduleResponseDTO.setDescription(schedule.getDescription());
        scheduleResponseDTO.setDate(schedule.getDate());
        scheduleResponseDTO.setStartTime(schedule.getStartTime());
        scheduleResponseDTO.setEndTime(schedule.getEndTime());
        scheduleResponseDTO.setCreatedAt(schedule.getCreatedAt());
        scheduleResponseDTO.setUpdatedAt(schedule.getUpdatedAt());

        return scheduleResponseDTO;
    }

    /**
     * Helper method to validate user access to a schedule through case validation.
     *
     * @param caseId the case ID to validate access for
     * @param operation the operation being performed (for logging)
     * @return CaseAssetValidationResult containing validation results
     */
    private <T> CaseAssetUtility.CaseAssetValidationResult<T> validateCaseAccess(UUID caseId, String operation) {
        return CaseAssetUtility.validateUserAndCaseAccess(caseId, operation, userRepo, caseRepo);
    }

    /**
     * Helper method to build pagination metadata for schedule list responses.
     *
     * @param schedulePage the page of schedules
     * @param sortDirection the sort direction applied
     * @param additionalFilters any additional filters applied
     * @return Map containing pagination metadata
     */
    private Map<String, Object> buildPaginationMetadata(Page<Schedule> schedulePage, String sortDirection, Map<String, String> additionalFilters) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", schedulePage.getTotalElements());
        metadata.put("pageNumber", schedulePage.getNumber());
        metadata.put("pageSize", schedulePage.getSize());
        metadata.put("totalPages", schedulePage.getTotalPages());
        metadata.put("hasNext", schedulePage.hasNext());
        metadata.put("hasPrevious", schedulePage.hasPrevious());
        metadata.put("isFirst", schedulePage.isFirst());
        metadata.put("isLast", schedulePage.isLast());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", CREATED_AT_FIELD);
        metadata.put("appliedFilters", additionalFilters != null ? additionalFilters : Map.of());
        return metadata;
    }
}
