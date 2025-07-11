package com.javajedis.legalconnect.caseassets.dtos;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentDTO {
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 10000, message = "Description must be between 2 and 10000 characters")
    private String description;
    
    @NotNull(message = "Privacy setting is required")
    private AssetPrivacy privacy;
} 