package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class UserInfoResponseDTOTest {
    @Test
    void testGettersSetters() {
        UserInfoResponseDTO dto = new UserInfoResponseDTO();
        dto.setFirstName("First");
        dto.setLastName("Last");
        dto.setEmail("test@example.com");
        dto.setRole(Role.ADMIN);
        dto.setEmailVerified(true);
        
        ProfilePictureDTO profilePicture = new ProfilePictureDTO(
            "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_123.jpg",
            "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_123.jpg",
            "profile_pictures/user_123"
        );
        dto.setProfilePicture(profilePicture);
        
        OffsetDateTime now = OffsetDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        
        assertEquals("First", dto.getFirstName());
        assertEquals("Last", dto.getLastName());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals(Role.ADMIN, dto.getRole());
        assertTrue(dto.isEmailVerified());
        assertEquals(profilePicture, dto.getProfilePicture());
        assertEquals("https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_123.jpg", dto.getProfilePicture().getFullPictureUrl());
        assertEquals("https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_123.jpg", dto.getProfilePicture().getThumbnailPictureUrl());
        assertEquals("profile_pictures/user_123", dto.getProfilePicture().getPublicId());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testGettersSettersWithNullProfilePicture() {
        UserInfoResponseDTO dto = new UserInfoResponseDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setRole(Role.USER);
        dto.setEmailVerified(false);
        dto.setProfilePicture(null);
        
        OffsetDateTime now = OffsetDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(Role.USER, dto.getRole());
        assertEquals(false, dto.isEmailVerified());
        assertEquals(null, dto.getProfilePicture());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }
} 