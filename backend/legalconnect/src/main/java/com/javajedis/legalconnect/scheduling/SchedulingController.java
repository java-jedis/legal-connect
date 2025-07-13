package com.javajedis.legalconnect.scheduling;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.scheduling.dto.CreateScheduleDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleListResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.ScheduleResponseDTO;
import com.javajedis.legalconnect.scheduling.dto.UpdateScheduleDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "6. Scheduling", description = "Scheduling and calendar management endpoints")
@RestController
@RequireUserOrVerifiedLawyer
@RequestMapping("/schedule")
public class SchedulingController {
    
    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    /**
     * Create a new schedule event for a case.
     */
    @Operation(summary = "Create schedule event", description = "Creates a new schedule event for a case. User must have access to the case.")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> createSchedule(@Valid @RequestBody CreateScheduleDTO scheduleData) {
        log.info("POST /schedule/ called for case: {}", scheduleData.getCaseId());
        return schedulingService.createSchedule(scheduleData);
    }

    /**
     * Get a single schedule event by ID.
     */
    @Operation(summary = "Get schedule by ID", description = "Retrieves a schedule event by ID. User must have access to the associated case.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> getScheduleById(@PathVariable UUID scheduleId) {
        log.info("GET /schedule/{} called", scheduleId);
        return schedulingService.getScheduleById(scheduleId);
    }

    /**
     * Update an existing schedule event.
     */
    @Operation(summary = "Update schedule", description = "Updates an existing schedule event. User must have access to the associated case.")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> updateSchedule(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateScheduleDTO updateData) {
        log.info("PUT /schedule/{} called", scheduleId);
        return schedulingService.updateSchedule(scheduleId, updateData);
    }

    /**
     * Delete a schedule event.
     */
    @Operation(summary = "Delete schedule", description = "Deletes a schedule event. User must have access to the associated case.")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable UUID scheduleId) {
        log.info("DELETE /schedule/{} called", scheduleId);
        return schedulingService.deleteSchedule(scheduleId);
    }

    /**
     * Get all schedule events for the authenticated user with pagination.
     */
    @Operation(summary = "Get all user schedules", description = "Retrieves all schedule events for the authenticated user (as lawyer or client) with pagination and sorting.")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>> getAllUserSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /schedule/ called with page={}, size={}, sortDirection={}", page, size, sortDirection);
        return schedulingService.getAllUserSchedules(page, size, sortDirection);
    }

    /**
     * Get all schedule events for a specific case with pagination.
     */
    @Operation(summary = "Get all schedules for case", description = "Retrieves all schedule events for a specific case with pagination and sorting. User must have access to the case.")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>> getAllSchedulesForCase(
            @PathVariable UUID caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /schedule/case/{} called with page={}, size={}, sortDirection={}", caseId, page, size, sortDirection);
        return schedulingService.getAllSchedulesForCase(caseId, page, size, sortDirection);
    }
}
