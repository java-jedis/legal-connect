package com.javajedis.legalconnect.notifications.dto;

import com.javajedis.legalconnect.notifications.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning a single notification preference.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponseDTO {
    
    private NotificationType type;
    private String displayName;
    private boolean emailEnabled;
    private boolean webPushEnabled;
} 