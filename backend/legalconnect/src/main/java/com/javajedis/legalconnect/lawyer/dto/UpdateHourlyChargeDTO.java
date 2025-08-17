package com.javajedis.legalconnect.lawyer.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHourlyChargeDTO {
    
    @DecimalMin(value = "0.01", message = "Hourly charge must be positive")
    @Digits(integer = 8, fraction = 2, message = "Hourly charge must have at most 8 digits before decimal and 2 digits after")
    private BigDecimal hourlyCharge;
}