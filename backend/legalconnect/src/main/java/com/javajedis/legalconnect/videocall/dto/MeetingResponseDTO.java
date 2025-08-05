package com.javajedis.legalconnect.videocall.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponseDTO {
    private UUID id;
    private String roomName;
    private UserInfo client;
    private UserInfo lawyer;
    private OffsetDateTime startTimestamp;
    private OffsetDateTime endTimestamp;
    private boolean isPaid;
    private PaymentInfo payment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private UUID id;
        private java.math.BigDecimal amount;
        private String status;
    }
}