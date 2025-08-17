package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.auth.dto.ResetPasswordDTO;

class ResetPasswordDTOTest {
    @Test
    void testGettersSetters() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("newpass");
        dto.setOtp("654321");
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("newpass", dto.getPassword());
        assertEquals("654321", dto.getOtp());
    }
} 