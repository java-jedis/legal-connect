package com.javajedis.legalconnect.caseassets.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteListResponseDTO {
    private List<NoteResponseDTO> notes;
} 