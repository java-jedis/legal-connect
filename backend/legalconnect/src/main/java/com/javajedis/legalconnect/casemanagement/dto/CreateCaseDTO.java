package com.javajedis.legalconnect.casemanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseDTO {
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;
    
    @NotBlank(message = "Client email is required")
    @Email(message = "Invalid email format")
    private String clientEmail;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
} 