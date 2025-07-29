package com.javajedis.legalconnect.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreatePaymentDTO {
    private UUID payerId;
    private UUID payeeId;
    private UUID refId;
    private BigDecimal amount;
}
