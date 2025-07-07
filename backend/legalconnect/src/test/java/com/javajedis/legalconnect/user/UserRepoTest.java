package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserRepoTest {
    @Mock
    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByEmail_userExists_returnsUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userRepo.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_userNotExists_returnsEmpty() {
        when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        Optional<User> result = userRepo.findByEmail("notfound@example.com");
        assertFalse(result.isPresent());
    }
} 