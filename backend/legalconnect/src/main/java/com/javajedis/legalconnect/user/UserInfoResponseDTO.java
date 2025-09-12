package com.javajedis.legalconnect.user;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserInfoResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean emailVerified;
    private ProfilePictureDTO profilePicture;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
