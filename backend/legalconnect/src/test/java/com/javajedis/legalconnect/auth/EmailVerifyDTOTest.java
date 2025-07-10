package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.auth.dto.EmailVerifyDTO;

class EmailVerifyDTOTest {
    @Test
    void testGettersSetters() {
        EmailVerifyDTO dto = new EmailVerifyDTO();
        dto.setEmail("test@example.com");
        dto.setOtp("123456");
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("123456", dto.getOtp());
    }
} 