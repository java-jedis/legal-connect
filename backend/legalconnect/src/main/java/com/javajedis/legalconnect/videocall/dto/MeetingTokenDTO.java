package com.javajedis.legalconnect.videocall.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingTokenDTO {
    private UUID meetingId;
    private String roomName;
    private String jwtToken;
}
