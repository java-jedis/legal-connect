package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.auth.dto.RequestOTPDTO;

class RequestOTPDTOTest {
    @Test
    void testGettersSetters() {
        RequestOTPDTO dto = new RequestOTPDTO();
        dto.setEmail("test@example.com");
        assertEquals("test@example.com", dto.getEmail());
    }
} 