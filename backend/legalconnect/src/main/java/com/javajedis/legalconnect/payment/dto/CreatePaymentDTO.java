package com.javajedis.legalconnect.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentDTO {
    @NotNull(message = "Payer ID is required")
    private UUID payerId;
    
    @NotNull(message = "Payee ID is required")
    private UUID payeeId;
    
    @NotNull(message = "Reference ID is required")
    private UUID refId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 digits before decimal and 2 digits after")
    private BigDecimal amount;
}
