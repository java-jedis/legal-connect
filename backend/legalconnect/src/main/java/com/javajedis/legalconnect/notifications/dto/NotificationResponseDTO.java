package com.javajedis.legalconnect.notifications.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
    private UUID id;
    private String content;
    private boolean isRead;
    private OffsetDateTime createdAt;
    
    // Additional convenience methods for testing
    public boolean getRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        this.isRead = read;
    }
}