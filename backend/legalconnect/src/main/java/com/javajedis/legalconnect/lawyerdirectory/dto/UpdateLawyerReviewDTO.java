package com.javajedis.legalconnect.lawyerdirectory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLawyerReviewDTO {
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private short rating;

    @Size(max = 5000, message = "Review must not exceed 5000 characters")
    private String review;
}
