package com.javajedis.legalconnect.lawyer.dto;

import java.util.List;

import lombok.Data;

@Data
public class LawyerAvailabilitySlotListResponseDTO {
    private List<LawyerAvailabilitySlotResponseDTO> slots;
} 