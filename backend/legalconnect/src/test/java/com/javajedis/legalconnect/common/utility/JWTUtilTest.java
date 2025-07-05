package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;

/**
 * Unit tests for JWTUtil.
 * Tests JWT token generation, validation, and claim extraction functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWTUtil Tests")
class JWTUtilTest {

    private static final String TEST_SECRET_KEY = "testSecretKeyForJWTTokenGenerationAndValidation123456789";
    private static final String TEST_ISSUER = "LegalConnect";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @InjectMocks
    private JWTUtil jwtUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "issuer", TEST_ISSUER);

        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setFirstName(TEST_FIRST_NAME);
        testUser.setLastName(TEST_LAST_NAME);
        testUser.setRole(Role.USER);
        testUser.setEmailVerified(true);
    }

    @Test
    @DisplayName("Should generate valid JWT token for user")
    void shouldGenerateValidJWTTokenForUser() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token can be parsed and contains expected claims
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(TEST_EMAIL, extractedUsername);
        
        String extractedUserId = jwtUtil.extractUserId(token);
        assertEquals(TEST_USER_ID.toString(), extractedUserId);
        
        String extractedRole = jwtUtil.extractUserRole(token);
        assertEquals(Role.USER.name(), extractedRole);
        
        String extractedFirstName = jwtUtil.extractFirstName(token);
        assertEquals(TEST_FIRST_NAME, extractedFirstName);
        
        String extractedLastName = jwtUtil.extractLastName(token);
        assertEquals(TEST_LAST_NAME, extractedLastName);
        
        Boolean extractedEmailVerified = jwtUtil.extractEmailVerified(token);
        assertEquals(true, extractedEmailVerified);
        
        String extractedTokenType = jwtUtil.extractTokenType(token);
        assertEquals("ACCESS", extractedTokenType);
    }

    @Test
    @DisplayName("Should generate valid JWT token for username (deprecated method)")
    void shouldGenerateValidJWTTokenForUsername() {
        // When
        @SuppressWarnings("java:S5738") // Suppress deprecation warning - will update main code later
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(TEST_EMAIL, extractedUsername);
    }

    @Test
    @DisplayName("Should validate valid JWT token")
    void shouldValidateValidJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should extract username from JWT token")
    void shouldExtractUsernameFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(TEST_EMAIL, extractedUsername);
    }

    @Test
    @DisplayName("Should extract user ID from JWT token")
    void shouldExtractUserIdFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(TEST_USER_ID.toString(), extractedUserId);
    }

    @Test
    @DisplayName("Should extract user role from JWT token")
    void shouldExtractUserRoleFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedRole = jwtUtil.extractUserRole(token);

        // Then
        assertEquals(Role.USER.name(), extractedRole);
    }

    @Test
    @DisplayName("Should extract first name from JWT token")
    void shouldExtractFirstNameFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedFirstName = jwtUtil.extractFirstName(token);

        // Then
        assertEquals(TEST_FIRST_NAME, extractedFirstName);
    }

    @Test
    @DisplayName("Should extract last name from JWT token")
    void shouldExtractLastNameFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedLastName = jwtUtil.extractLastName(token);

        // Then
        assertEquals(TEST_LAST_NAME, extractedLastName);
    }

    @Test
    @DisplayName("Should extract email verified status from JWT token")
    void shouldExtractEmailVerifiedFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean extractedEmailVerified = jwtUtil.extractEmailVerified(token);

        // Then
        assertEquals(true, extractedEmailVerified);
    }

    @Test
    @DisplayName("Should extract token type from JWT token")
    void shouldExtractTokenTypeFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedTokenType = jwtUtil.extractTokenType(token);

        // Then
        assertEquals("ACCESS", extractedTokenType);
    }

    @Test
    @DisplayName("Should extract all user info from JWT token")
    void shouldExtractAllUserInfoFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Map<String, Object> userInfo = jwtUtil.extractAllUserInfo(token);

        // Then
        assertNotNull(userInfo);
        assertEquals(TEST_USER_ID.toString(), userInfo.get(JWTUtil.CLAIM_USER_ID));
        assertEquals(TEST_EMAIL, userInfo.get("email"));
        assertEquals(Role.USER.name(), userInfo.get("role"));
        assertEquals(TEST_FIRST_NAME, userInfo.get(JWTUtil.CLAIM_FIRST_NAME));
        assertEquals(TEST_LAST_NAME, userInfo.get(JWTUtil.CLAIM_LAST_NAME));
        assertEquals(true, userInfo.get(JWTUtil.CLAIM_EMAIL_VERIFIED));
        assertEquals("ACCESS", userInfo.get(JWTUtil.CLAIM_TOKEN_TYPE));
    }

    @Test
    @DisplayName("Should extract expiration date from JWT token")
    void shouldExtractExpirationFromJWTToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should handle user with unverified email")
    void shouldHandleUserWithUnverifiedEmail() {
        // Given
        testUser.setEmailVerified(false);
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean extractedEmailVerified = jwtUtil.extractEmailVerified(token);

        // Then
        assertEquals(false, extractedEmailVerified);
    }

    @Test
    @DisplayName("Should handle user with different role")
    void shouldHandleUserWithDifferentRole() {
        // Given
        testUser.setRole(Role.LAWYER);
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedRole = jwtUtil.extractUserRole(token);

        // Then
        assertEquals(Role.LAWYER.name(), extractedRole);
    }

    @Test
    @DisplayName("Should handle null user fields")
    void shouldHandleNullUserFields() {
        // Given
        testUser.setFirstName(null);
        testUser.setLastName(null);
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedFirstName = jwtUtil.extractFirstName(token);
        String extractedLastName = jwtUtil.extractLastName(token);

        // Then
        assertEquals(null, extractedFirstName);
        assertEquals(null, extractedLastName);
    }

    @Test
    @DisplayName("Should throw exception for invalid JWT token")
    void shouldThrowExceptionForInvalidJWTToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    @DisplayName("Should throw exception for expired JWT token")
    void shouldThrowExceptionForExpiredJWTToken() {
        // Given - Create an expired token manually
        Date pastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2); // 2 hours ago
        String expiredToken = Jwts.builder()
                .subject(TEST_EMAIL)
                .issuer(TEST_ISSUER)
                .issuedAt(new Date())
                .expiration(pastDate)
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes()))
                .compact();

        // When & Then
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractUsername(expiredToken);
        });
    }

    @Test
    @DisplayName("Should handle token with missing claims")
    void shouldHandleTokenWithMissingClaims() {
        // Given - Create token with minimal claims
        String minimalToken = Jwts.builder()
                .subject(TEST_EMAIL)
                .issuer(TEST_ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes()))
                .compact();

        // When
        String extractedUserId = jwtUtil.extractUserId(minimalToken);
        String extractedRole = jwtUtil.extractUserRole(minimalToken);
        String extractedFirstName = jwtUtil.extractFirstName(minimalToken);
        String extractedLastName = jwtUtil.extractLastName(minimalToken);
        Boolean extractedEmailVerified = jwtUtil.extractEmailVerified(minimalToken);
        String extractedTokenType = jwtUtil.extractTokenType(minimalToken);

        // Then
        assertEquals(null, extractedUserId);
        assertEquals(null, extractedRole);
        assertEquals(null, extractedFirstName);
        assertEquals(null, extractedLastName);
        assertEquals(null, extractedEmailVerified);
        assertEquals(null, extractedTokenType);
    }

    @Test
    @DisplayName("Should generate tokens with consistent structure")
    void shouldGenerateTokensWithConsistentStructure() {
        // Given
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setEmail("user1@example.com");
        user1.setFirstName("Alice");
        user1.setLastName("Smith");
        user1.setRole(Role.USER);
        user1.setEmailVerified(true);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFirstName("Bob");
        user2.setLastName("Johnson");
        user2.setRole(Role.LAWYER);
        user2.setEmailVerified(false);

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(token1.length() > 0);
        assertTrue(token2.length() > 0);
        
        // Both tokens should be valid
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
        
        // Extract and verify claims
        assertEquals("user1@example.com", jwtUtil.extractUsername(token1));
        assertEquals("user2@example.com", jwtUtil.extractUsername(token2));
        assertEquals(Role.USER.name(), jwtUtil.extractUserRole(token1));
        assertEquals(Role.LAWYER.name(), jwtUtil.extractUserRole(token2));
        assertEquals(true, jwtUtil.extractEmailVerified(token1));
        assertEquals(false, jwtUtil.extractEmailVerified(token2));
    }
} 