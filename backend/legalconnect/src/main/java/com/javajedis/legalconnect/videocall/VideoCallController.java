package com.javajedis.legalconnect.videocall;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.common.security.RequireVerifiedLawyer;
import com.javajedis.legalconnect.videocall.dto.MeetingResponseDTO;
import com.javajedis.legalconnect.videocall.dto.MeetingTokenDTO;
import com.javajedis.legalconnect.videocall.dto.ScheduleMeetingDTO;
import com.javajedis.legalconnect.videocall.dto.UpdateMeetingDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Tag(name = "B. Video Calls", description = "Video call meeting management endpoints")
@RestController
@RequestMapping("/videocalls")
@RequiredArgsConstructor
@RequireUserOrVerifiedLawyer
public class VideoCallController {

    private final VideoCallService videoCallService;

    /**
     * Schedules a new video call meeting between a client and lawyer.
     */
    @Operation(summary = "Schedule meeting", description = "Schedules a new video call meeting between a client and lawyer.")
    @RequireVerifiedLawyer
    @PostMapping("/")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> scheduleMeeting(@Valid @RequestBody ScheduleMeetingDTO scheduleMeetingDTO) {
        log.info("POST /videocalls/ called for client: {}",
                scheduleMeetingDTO.getEmail(),
                scheduleMeetingDTO.getStartDateTime(), scheduleMeetingDTO.getEndDateTime());
        return videoCallService.scheduleMeeting(scheduleMeetingDTO);
    }

    /**
     * Updates an existing video call meeting.
     */
    @Operation(summary = "Update meeting", description = "Updates an existing video call meeting.")
    @RequireVerifiedLawyer
    @PutMapping("/")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> updateMeeting(@Valid @RequestBody UpdateMeetingDTO updateMeetingDTO) {
        log.info("PUT /videocalls/ called for meeting: {} from {} to {}",
                updateMeetingDTO.getMeetingId(), updateMeetingDTO.getStartDateTime(), updateMeetingDTO.getEndDateTime());
        return videoCallService.updateMeeting(updateMeetingDTO);
    }

    /**
     * Deletes a video call meeting.
     */
    @Operation(summary = "Delete meeting", description = "Deletes a video call meeting.")
    @RequireVerifiedLawyer
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<String>> deleteMeeting(@PathVariable UUID meetingId) {
        log.info("DELETE /videocalls/{} called", meetingId);
        return videoCallService.deleteMeeting(meetingId);
    }

    /**
     * Retrieves a specific video call meeting by ID.
     */
    @Operation(summary = "Get meeting by ID", description = "Retrieves a specific video call meeting by ID.")
    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> getMeeting(@PathVariable UUID meetingId) {
        log.info("GET /videocalls/{} called", meetingId);
        return videoCallService.getMeeting(meetingId);
    }

    /**
     * Retrieves all video call meetings for the authenticated user.
     */
    @Operation(summary = "Get all user meetings", description = "Retrieves all video call meetings for the authenticated user.")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getMeetings() {
        log.info("GET /videocalls/ called");
        return videoCallService.getMeetings();
    }

    /**
     * Generates a JWT token for joining a video call meeting.
     */
    @Operation(summary = "Generate meeting token", description = "Generates a JWT token for joining a video call meeting.")
    @GetMapping("/{meetingId}/token")
    public ResponseEntity<ApiResponse<MeetingTokenDTO>> generateMeetingToken(@PathVariable UUID meetingId) {
        log.info("GET /videocalls/{}/token called", meetingId);
        return videoCallService.generateMeetingToken(meetingId);
    }
} 