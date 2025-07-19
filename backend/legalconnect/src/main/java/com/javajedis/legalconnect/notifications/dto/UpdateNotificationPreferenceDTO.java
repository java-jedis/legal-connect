package com.javajedis.legalconnect.notifications.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating notification preferences.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationPreferenceDTO {
    
    @NotNull(message = "Email enabled status is required")
    private boolean emailEnabled;
    
    @NotNull(message = "Web push enabled status is required")
    private boolean webPushEnabled;
} 