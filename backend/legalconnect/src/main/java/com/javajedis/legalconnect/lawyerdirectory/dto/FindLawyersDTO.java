package com.javajedis.legalconnect.lawyerdirectory.dto;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindLawyersDTO {
    @Min(value = 0, message = "Minimum experience must be at least 0")
    @Max(value = 50, message = "Minimum experience cannot exceed 50")
    private Integer minExperience;

    @Min(value = 0, message = "Maximum experience must be at least 0")
    @Max(value = 50, message = "Maximum experience cannot exceed 50")
    private Integer maxExperience;

    private PracticingCourt practicingCourt;
    private Division division;
    private District district;
    private SpecializationType specialization;
}
