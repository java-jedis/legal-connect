package com.javajedis.legalconnect.videocall.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeetingDTO {
    
    @NotNull(message = "Meeting ID is required")
    private UUID meetingId;
    
    @NotNull(message = "Start datetime is required")
    private OffsetDateTime startDateTime;
    
    @NotNull(message = "End datetime is required")
    private OffsetDateTime endDateTime;
} 