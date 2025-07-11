package com.javajedis.legalconnect.caseassets.dtos;

import java.util.UUID;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteResponseDTO {
    private UUID caseId;
    private String caseTitle;
    private UUID noteId;
    private UUID ownerId;
    private String title;
    private String content;
    private AssetPrivacy privacy;


}
