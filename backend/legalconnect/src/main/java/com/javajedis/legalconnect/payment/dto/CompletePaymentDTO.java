package com.javajedis.legalconnect.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.payment.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletePaymentDTO {
    @NotNull(message = "Payment ID is required")
    private UUID id;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotBlank(message = "Transaction ID is required")
    @Size(min = 1, max = 255, message = "Transaction ID must be between 1 and 255 characters")
    private String transactionId;
    
    @NotNull(message = "Payment date is required")
    private OffsetDateTime paymentDate;
    
    @NotNull(message = "Release date is required")
    private OffsetDateTime releaseAt;
}
