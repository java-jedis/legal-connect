package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoleTest {
    @Test
    void testEnumValues() {
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.LAWYER, Role.valueOf("LAWYER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(3, Role.values().length);
    }
} 