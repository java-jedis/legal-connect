package com.javajedis.legalconnect.lawyerdirectory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerReviewResponseDTO {
    private UUID id;
    private UUID lawyerId;
    private String lawyerName;
    private UUID clientId;
    private String clientName;
    private UUID caseId;
    private short rating;
    private String review;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}