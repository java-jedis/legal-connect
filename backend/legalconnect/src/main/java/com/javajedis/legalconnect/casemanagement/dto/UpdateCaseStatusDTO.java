package com.javajedis.legalconnect.casemanagement.dto;

import com.javajedis.legalconnect.casemanagement.CaseStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCaseStatusDTO {
    
    @NotNull(message = "Status is required")
    private CaseStatus status;
} 