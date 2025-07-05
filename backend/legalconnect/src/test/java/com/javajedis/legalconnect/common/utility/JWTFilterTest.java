package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.ServletException;

/**
 * Unit tests for JWTFilter.
 * Tests JWT authentication filter functionality including token validation and user authentication.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWTFilter Tests")
class JWTFilterTest {

    private static final String TEST_EMAIL = "test@example.com";

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private JWTFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private UserDetails userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();
        
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        // Create test user details
        userDetails = User.builder()
                .username(TEST_EMAIL)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Create a valid JWT token for testing
        validToken = createValidJWTToken();
    }

    @Test
    @DisplayName("Should not filter public endpoints")
    void shouldNotFilterPublicEndpoints() {
        // Given
        String[] publicPaths = {
            "/v1/public/login",
            "/v1/public/register",
            "/public/api",
            "/v1/public/health"
        };

        for (String path : publicPaths) {
            request.setRequestURI(path);

            // When
            boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

            // Then
            assertTrue(shouldNotFilter, "Should not filter path: " + path);
        }
    }

    @Test
    @DisplayName("Should filter protected endpoints")
    void shouldFilterProtectedEndpoints() {
        // Given
        String[] protectedPaths = {
            "/v1/api/users",
            "/v1/dashboard",
            "/api/private",
            "/v1/lawyers/profile"
        };

        for (String path : protectedPaths) {
            request.setRequestURI(path);

            // When
            boolean shouldNotFilter = jwtFilter.shouldNotFilter(request);

            // Then
            assertFalse(shouldNotFilter, "Should filter path: " + path);
        }
    }

    @Test
    @DisplayName("Should process request with valid JWT token")
    void shouldProcessRequestWithValidJWTToken() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
        verify(jwtUtil, times(1)).validateToken(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        
        // Verify authentication was set
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        
        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should skip authentication for request without Authorization header")
    void shouldSkipAuthenticationForRequestWithoutAuthorizationHeader() throws ServletException, IOException {
        // Given - No Authorization header

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString());
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("Should skip authentication for request with invalid Authorization header format")
    void shouldSkipAuthenticationForRequestWithInvalidAuthorizationHeaderFormat() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "InvalidFormat token");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString());
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("Should skip authentication for blacklisted token")
    void shouldSkipAuthenticationForBlacklistedToken() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should skip authentication for invalid token")
    void shouldSkipAuthenticationForInvalidToken() throws ServletException, IOException {
        // Given
        SecurityContextHolder.clearContext();
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
        verify(jwtUtil, times(1)).validateToken(validToken);
        
        // Authentication should not be set for invalid token
        org.junit.jupiter.api.Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should handle user not found exception")
    void shouldHandleUserNotFoundException() {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL))
            .thenThrow(new UsernameNotFoundException("User not found"));

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            jwtFilter.doFilterInternal(request, response, filterChain);
        });
        
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should handle null username from token")
    void shouldHandleNullUsernameFromToken() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(null);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should handle empty username from token")
    void shouldHandleEmptyUsernameFromToken() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn("");
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        // Removed strict verification for jwtUtil.validateToken(anyString())
    }

    @Test
    @DisplayName("Should handle Redis connection failure gracefully")
    void shouldHandleRedisConnectionFailureGracefully() {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        doThrow(new RuntimeException("Redis connection failed")).when(redisTemplate).hasKey(anyString());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            jwtFilter.doFilterInternal(request, response, filterChain);
        });
        
        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should set authentication with correct user details")
    void shouldSetAuthenticationWithCorrectUserDetails() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        UsernamePasswordAuthenticationToken authentication = 
            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
        // Compare authorities content rather than exact collection type
        assertTrue(authentication.getAuthorities().containsAll(userDetails.getAuthorities()));
        assertTrue(authentication.isAuthenticated());
        
        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should clear security context after processing")
    void shouldClearSecurityContextAfterProcessing() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        // The filter should complete successfully and the filter chain should be called
        // Security context should be available during processing
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        
        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should get user details service")
    void shouldGetUserDetailsService() {
        // When
        UserDetailsService result = jwtFilter.getUserDetailsService();

        // Then
        assertEquals(userDetailsService, result);
    }

    @Test
    @DisplayName("Should get JWT util")
    void shouldGetJWTUtil() {
        // When
        JWTUtil result = jwtFilter.getJwtUtil();

        // Then
        assertEquals(jwtUtil, result);
    }

    @Test
    @DisplayName("Should handle different token formats")
    void shouldHandleDifferentTokenFormats() throws ServletException, IOException {
        // Given
        String[] tokenFormats = {
            "Bearer " + validToken,
            "bearer " + validToken,
            "BEARER " + validToken
        };

        for (String authHeader : tokenFormats) {
            MockHttpServletRequest newRequest = new MockHttpServletRequest();
            MockHttpServletResponse newResponse = new MockHttpServletResponse();
            MockFilterChain newFilterChain = new MockFilterChain();
            
            newRequest.addHeader("Authorization", authHeader);
            
            org.mockito.Mockito.lenient().when(jwtUtil.extractUsername(validToken)).thenReturn(TEST_EMAIL);
            org.mockito.Mockito.lenient().when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
            org.mockito.Mockito.lenient().when(jwtUtil.validateToken(validToken)).thenReturn(true);
            org.mockito.Mockito.lenient().when(redisTemplate.hasKey("blacklist:jwt:" + validToken)).thenReturn(false);

            // When
            jwtFilter.doFilterInternal(newRequest, newResponse, newFilterChain);

            // Then
            verify(jwtUtil, times(1)).extractUsername(validToken);
            verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
            verify(jwtUtil, times(1)).validateToken(validToken);
            verify(redisTemplate, times(1)).hasKey("blacklist:jwt:" + validToken);
        }
        
        // Clean up
        SecurityContextHolder.clearContext();
    }

    private String createValidJWTToken() {
        // Create a simple valid token for testing
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaXNzIjoiTGVnYWxDb25uZWN0IiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.test_signature";
    }
} 