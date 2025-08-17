package com.javajedis.legalconnect.casemanagement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseListResponseDTO {
    private List<CaseResponseDTO> cases;
} 