package com.javajedis.legalconnect.lawyerdirectory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerReviewListResponseDTO {
    private List<LawyerReviewResponseDTO> reviews;
}
