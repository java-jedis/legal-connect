package com.javajedis.legalconnect.auth.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.user.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean emailVerified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}