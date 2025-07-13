package com.javajedis.legalconnect.scheduling.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.javajedis.legalconnect.scheduling.ScheduleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseScheduleDTO {
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;
    
    @NotNull(message = "Schedule type is required")
    private ScheduleType type;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;
    
    @NotNull(message = "End time is required")
    private OffsetDateTime endTime;
} 