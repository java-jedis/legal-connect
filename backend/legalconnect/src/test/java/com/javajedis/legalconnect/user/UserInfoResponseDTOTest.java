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
        OffsetDateTime now = OffsetDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        assertEquals("First", dto.getFirstName());
        assertEquals("Last", dto.getLastName());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals(Role.ADMIN, dto.getRole());
        assertTrue(dto.isEmailVerified());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }
} 