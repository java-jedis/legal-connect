package com.javajedis.legalconnect.notifications.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceListResponseDTO {
    
    private List<NotificationPreferenceResponseDTO> preferences;
} 