package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class UserDetailsServiceImplTest {
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_userFound_returnsUserDetails() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.LAWYER);
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        assertNotNull(userDetailsService.loadUserByUsername("test@example.com"));
    }

    @Test
    void loadUserByUsername_userNotFound_throwsException() {
        when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("notfound@example.com"));
    }
} 