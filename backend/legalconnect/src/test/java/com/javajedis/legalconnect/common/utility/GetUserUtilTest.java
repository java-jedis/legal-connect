package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class GetUserUtilTest {
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private UserRepo userRepo;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserInfoFromJwt_returnsUserInfo() {
        String token = "jwt.token";
        Map<String, Object> expected = new HashMap<>();
        expected.put("email", "test@example.com");
        when(jwtUtil.extractAllUserInfo(token)).thenReturn(expected);
        Map<String, Object> result = GetUserUtil.getUserInfoFromJwt(token, jwtUtil);
        assertEquals(expected, result);
    }

    @Test
    void getCurrentUserInfo_authenticatedUser_returnsUserInfo() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setRole(null);
        user.setEmailVerified(true);
        OffsetDateTime now = OffsetDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Map<String, Object> result = GetUserUtil.getCurrentUserInfo(userRepo);
        assertEquals(user.getId(), result.get("id"));
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
        assertEquals("test@example.com", result.get("email"));
        assertEquals(true, result.get("emailVerified"));
        assertEquals(now, result.get("createdAt"));
        assertEquals(now, result.get("updatedAt"));
    }

    @Test
    void getCurrentUserInfo_notAuthenticated_returnsEmptyMap() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Map<String, Object> result = GetUserUtil.getCurrentUserInfo(userRepo);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserInfo_userNotFound_returnsEmptyMap() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("notfound@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        Map<String, Object> result = GetUserUtil.getCurrentUserInfo(userRepo);
        assertTrue(result.isEmpty());
    }
} 