package com.javajedis.legalconnect.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletePaymentDTO {
    @NotNull(message = "Session ID is required")
    private String sessionId;
}
