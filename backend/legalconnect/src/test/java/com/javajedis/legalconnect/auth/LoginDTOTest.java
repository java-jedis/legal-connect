package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.auth.dto.LoginDTO;

class LoginDTOTest {
    @Test
    void testGettersSetters() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password", dto.getPassword());
    }
} 