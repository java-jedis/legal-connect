package com.javajedis.legalconnect.scheduling.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthCalendarTokenResponseDTO {
    private UUID id;
    private UserSummaryDTO user;
    private boolean isAccessTokenValid;
    private boolean isRefreshTokenValid;
    private OffsetDateTime accessExpiry;
    private OffsetDateTime refreshExpiry;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummaryDTO {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
} 