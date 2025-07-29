package com.javajedis.legalconnect.payment.dto;

import com.javajedis.legalconnect.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletePaymentDTO {
    private UUID id;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private OffsetDateTime paymentDate;
    private OffsetDateTime releaseAt;
}
