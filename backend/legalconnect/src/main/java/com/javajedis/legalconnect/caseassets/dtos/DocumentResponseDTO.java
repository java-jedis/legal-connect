package com.javajedis.legalconnect.caseassets.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {
    private UUID caseId;
    private String caseTitle;
    private UUID documentId;
    private String title;
    private String description;
    private UUID uploadedById;
    private String uploadedByName;
    private AssetPrivacy privacy;
    private OffsetDateTime createdAt;
} 