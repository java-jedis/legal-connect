package com.javajedis.legalconnect.scheduling.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOAuthCalendarTokenDTO {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Access token is required")
    private String accessToken;
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    @NotNull(message = "Access token expiry is required")
    private OffsetDateTime accessExpiry;
    
    @NotNull(message = "Refresh token expiry is required")
    private OffsetDateTime refreshExpiry;
} 