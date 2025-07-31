package com.javajedis.legalconnect.payment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.payment.PaymentMethod;
import com.javajedis.legalconnect.payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private UUID id;
    private UUID payeeId;
    private UUID payerId;
    private UUID meetingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private OffsetDateTime paymentDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
