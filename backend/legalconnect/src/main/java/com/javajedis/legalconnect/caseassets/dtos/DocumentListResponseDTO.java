package com.javajedis.legalconnect.caseassets.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentListResponseDTO {
    private List<DocumentResponseDTO> documents;
} 