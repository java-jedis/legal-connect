package com.javajedis.legalconnect.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeSessionResponseDTO {
    private String sessionId;
    private String sessionUrl;
} 