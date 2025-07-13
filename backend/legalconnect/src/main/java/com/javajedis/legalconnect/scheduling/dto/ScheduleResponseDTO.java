package com.javajedis.legalconnect.scheduling.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.scheduling.ScheduleType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponseDTO {
    private UUID id;
    private CaseSummaryDTO caseInfo;
    private LawyerSummaryDTO lawyerInfo;
    private ClientSummaryDTO clientInfo;
    private String title;
    private ScheduleType type;
    private String description;
    private LocalDate date;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CaseSummaryDTO {
        private UUID id;
        private String title;
        private CaseStatus status;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LawyerSummaryDTO {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
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