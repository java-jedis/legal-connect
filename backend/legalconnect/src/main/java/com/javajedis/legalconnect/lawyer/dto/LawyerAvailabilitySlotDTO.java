package com.javajedis.legalconnect.lawyer.dto;

import java.time.LocalTime;

import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;

import lombok.Data;

@Data
public class LawyerAvailabilitySlotDTO {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
} 