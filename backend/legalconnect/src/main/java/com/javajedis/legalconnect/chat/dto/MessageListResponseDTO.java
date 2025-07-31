package com.javajedis.legalconnect.chat.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResponseDTO {
    private List<MessageResponseDTO> messages;
}