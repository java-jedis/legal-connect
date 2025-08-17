package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.auth.dto.UserRegisterDTO;
import com.javajedis.legalconnect.user.Role;

class UserRegisterDTOTest {
    @Test
    void testGettersSetters() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFirstName("First");
        dto.setLastName("Last");
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setRole(Role.LAWYER);
        assertEquals("First", dto.getFirstName());
        assertEquals("Last", dto.getLastName());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password", dto.getPassword());
        assertEquals(Role.LAWYER, dto.getRole());
    }
} 