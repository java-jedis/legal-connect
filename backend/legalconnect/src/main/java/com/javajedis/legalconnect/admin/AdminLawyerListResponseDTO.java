package com.javajedis.legalconnect.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLawyerListResponseDTO {
    private List<AdminLawyerDTO> lawyers;
} 