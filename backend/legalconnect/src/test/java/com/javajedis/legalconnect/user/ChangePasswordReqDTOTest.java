package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChangePasswordReqDTOTest {
    @Test
    void testGettersSetters() {
        ChangePasswordReqDTO dto = new ChangePasswordReqDTO();
        dto.setOldPassword("old");
        dto.setPassword("new");
        assertEquals("old", dto.getOldPassword());
        assertEquals("new", dto.getPassword());
    }
} 