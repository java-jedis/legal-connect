package com.javajedis.legalconnect.lawyer.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;

import lombok.Data;

@Data
public class LawyerAvailabilitySlotResponseDTO {
    private UUID id;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
} 