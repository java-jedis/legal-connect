package com.javajedis.legalconnect.lawyer;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerProfileDTO {
    
    @NotBlank(message = "Firm name is required")
    @Size(min = 2, max = 255, message = "Firm name must be between 2 and 255 characters")
    private String firm;
    
    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience must be at least 0")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    private Integer yearsOfExperience;
    
    @NotBlank(message = "Bar certificate number is required")
    @Size(min = 5, max = 50, message = "Bar certificate number must be between 5 and 50 characters")
    private String barCertificateNumber;
    
    @NotNull(message = "Practicing court is required")
    private PracticingCourt practicingCourt;
    
    @NotNull(message = "Division is required")
    private Division division;
    
    @NotNull(message = "District is required")
    private District district;
    
    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    private String bio;
    
    @Size(min = 1, message = "At least one specialization is required")
    private List<SpecializationType> specializations;
} 