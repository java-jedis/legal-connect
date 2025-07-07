package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.user.Role;

class AuthResponseDTOTest {
    @Test
    void testAllArgsAndGettersSetters() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        AuthResponseDTO dto = new AuthResponseDTO("token", id, "First", "Last", "email@test.com", Role.USER, true, now, now);
        assertEquals("token", dto.getToken());
        assertEquals(id, dto.getId());
        assertEquals("First", dto.getFirstName());
        assertEquals("Last", dto.getLastName());
        assertEquals("email@test.com", dto.getEmail());
        assertEquals(Role.USER, dto.getRole());
        assertTrue(dto.isEmailVerified());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }
} 