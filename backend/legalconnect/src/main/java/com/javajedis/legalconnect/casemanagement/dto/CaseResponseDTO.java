package com.javajedis.legalconnect.casemanagement.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.casemanagement.CaseStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseResponseDTO {
    private UUID caseId;
    private LawyerSummaryDTO lawyer;
    private ClientSummaryDTO client;
    private String title;
    private String description;
    private CaseStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LawyerSummaryDTO {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String firm;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClientSummaryDTO {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
