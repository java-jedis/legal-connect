package com.javajedis.legalconnect.scheduling.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CreateScheduleDTO extends BaseScheduleDTO {
    
    @NotNull(message = "Case ID is required")
    private UUID caseId;
} 