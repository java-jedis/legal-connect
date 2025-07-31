package com.javajedis.legalconnect.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponseDTO {
    private UUID id;
    private UUID otherParticipantId;
    private String otherParticipantName;
    private MessageResponseDTO latestMessage;
    private int unreadCount;
    private OffsetDateTime updatedAt;
}