package com.javajedis.legalconnect.videocall.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMeetingDTO {
    
    @NotNull(message = "Client email is required")
    private String email;

    @NotNull(message = "Meeting title is required")
    private String title;

    @NotNull(message = "Start datetime is required")
    private OffsetDateTime startDateTime;
    
    @NotNull(message = "End datetime is required")
    private OffsetDateTime endDateTime;
}
