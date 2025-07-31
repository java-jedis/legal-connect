package com.javajedis.legalconnect.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String content;
    private boolean read;
    private OffsetDateTime createdAt;
}