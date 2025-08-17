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

    @Test
    void getAuthenticatedUser_authenticatedUser_returnsUser() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setEmailVerified(true);
        
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        User result = GetUserUtil.getAuthenticatedUser(userRepo);

        // Then
        assertEquals(user, result);
        assertEquals("John", result.getFirstName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getAuthenticatedUser_notAuthenticated_returnsNull() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        User result = GetUserUtil.getAuthenticatedUser(userRepo);

        // Then
        assertEquals(null, result);
    }

    @Test
    void getAuthenticatedUser_noAuthentication_returnsNull() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(null);

        // When
        User result = GetUserUtil.getAuthenticatedUser(userRepo);

        // Then
        assertEquals(null, result);
    }

    @Test
    void getAuthenticatedUser_userNotFound_returnsNull() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("notfound@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When
        User result = GetUserUtil.getAuthenticatedUser(userRepo);

        // Then
        assertEquals(null, result);
    }
} 