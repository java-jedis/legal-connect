package com.javajedis.legalconnect.lawyerdirectory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLawyerReviewDTO {
    @NotNull(message = "Case ID cannot be null")
    private UUID caseId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private short rating;

    @Size(max = 5000, message = "Review must not exceed 5000 characters")
    private String review;
}
