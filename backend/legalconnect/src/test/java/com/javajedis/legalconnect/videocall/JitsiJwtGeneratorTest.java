package com.javajedis.legalconnect.videocall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Comprehensive unit tests for JitsiJwtGenerator.
 * Tests JWT token generation, validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JitsiJwtGenerator Tests")
class JitsiJwtGeneratorTest {

    private JitsiJwtGenerator jitsiJwtGenerator;
    private final String testJaasAppId = "vpaas-magic-cookie-test-app-id";
    private final String testJaasApiKey = "vpaas-magic-cookie-test-api-key/test123";
    private final String testJwtSecret = "test-jwt-secret-key-for-testing-purposes-must-be-long-enough";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jitsiJwtGenerator = new JitsiJwtGenerator();
        
        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jitsiJwtGenerator, "jaasAppId", testJaasAppId);
        ReflectionTestUtils.setField(jitsiJwtGenerator, "jaasApiKey", testJaasApiKey);
        ReflectionTestUtils.setField(jitsiJwtGenerator, "jwtSecret", testJwtSecret);
    }

    // TOKEN GENERATION TESTS

    @Test
    @DisplayName("Should generate JaaS token with all parameters")
    void testGenerateJaasTokenWithAllParameters() throws Exception {
        // Arrange
        String apiKey = testJaasApiKey;
        String userName = "John Doe";
        String userEmail = "john.doe@example.com";
        String roomName = "test-room-123";
        boolean isModerator = true;

        // Act
        String token = jitsiJwtGenerator.generateJaasToken(apiKey, userName, userEmail, roomName, isModerator);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        // Verify token structure (JWT has 3 parts separated by dots)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length, "JWT should have 3 parts: header, payload, signature");
        
        // Verify token can be parsed and contains expected claims
        Claims claims = parseTokenClaims(token);
        assertEquals("chat", claims.getIssuer());
        assertEquals(testJaasAppId, claims.getSubject());
        assertEquals(roomName, claims.get("room"));
        assertTrue(claims.getAudience().contains("jitsi"));
    }

    @Test
    @DisplayName("Should generate JaaS token with minimal parameters")
    void testGenerateJaasTokenWithMinimalParameters() throws Exception {
        // Arrange
        String apiKey = testJaasApiKey;
        String userName = "Jane Smith";
        String userEmail = "jane.smith@example.com";
        String roomName = "meeting-room-456";

        // Act
        String token = jitsiJwtGenerator.generateJaasToken(apiKey, userName, userEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals("chat", claims.getIssuer());
        assertEquals(testJaasAppId, claims.getSubject());
        assertEquals(roomName, claims.get("room"));
        
        // Verify user context
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        assertNotNull(context);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        assertNotNull(user);
        assertEquals(userName, user.get("name"));
        assertEquals(userEmail, user.get("email"));
        assertEquals("true", user.get("moderator")); // Default is true
    }

    @Test
    @DisplayName("Should generate JaaS token using configured values")
    void testGenerateJaasTokenWithConfig() throws Exception {
        // Arrange
        String userName = "Config User";
        String userEmail = "config.user@example.com";
        String roomName = "config-room";

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals("chat", claims.getIssuer());
        assertEquals(testJaasAppId, claims.getSubject());
        assertEquals(roomName, claims.get("room"));
    }

    @Test
    @DisplayName("Should generate lawyer token with moderator privileges")
    void testGenerateLawyerToken() throws Exception {
        // Arrange
        String lawyerName = "Attorney Smith";
        String lawyerEmail = "attorney.smith@lawfirm.com";
        String roomName = "legal-consultation-789";

        // Act
        String token = jitsiJwtGenerator.generateLawyerToken(lawyerName, lawyerEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals(roomName, claims.get("room"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        
        assertEquals(lawyerName, user.get("name"));
        assertEquals(lawyerEmail, user.get("email"));
        assertEquals("true", user.get("moderator"));
    }

    @Test
    @DisplayName("Should generate client token with moderator privileges")
    void testGenerateClientToken() throws Exception {
        // Arrange
        String clientName = "Client Johnson";
        String clientEmail = "client.johnson@example.com";
        String roomName = "client-meeting-101";

        // Act
        String token = jitsiJwtGenerator.generateClientToken(clientName, clientEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals(roomName, claims.get("room"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        
        assertEquals(clientName, user.get("name"));
        assertEquals(clientEmail, user.get("email"));
        assertEquals("true", user.get("moderator")); // Set to true for all users
    }

    // BACKWARD COMPATIBILITY TESTS

    @Test
    @DisplayName("Should generate token with wildcard room for backward compatibility")
    void testGenerateJaasTokenWithConfigBackwardCompatibility() throws Exception {
        // Arrange
        String userName = "Legacy User";
        String userEmail = "legacy.user@example.com";

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals("*", claims.get("room")); // Wildcard for backward compatibility
    }

    @Test
    @DisplayName("Should generate lawyer token with wildcard room for backward compatibility")
    void testGenerateLawyerTokenBackwardCompatibility() throws Exception {
        // Arrange
        String lawyerName = "Legacy Lawyer";
        String lawyerEmail = "legacy.lawyer@lawfirm.com";

        // Act
        String token = jitsiJwtGenerator.generateLawyerToken(lawyerName, lawyerEmail);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals("*", claims.get("room")); // Wildcard for backward compatibility
    }

    @Test
    @DisplayName("Should generate client token with wildcard room for backward compatibility")
    void testGenerateClientTokenBackwardCompatibility() throws Exception {
        // Arrange
        String clientName = "Legacy Client";
        String clientEmail = "legacy.client@example.com";

        // Act
        String token = jitsiJwtGenerator.generateClientToken(clientName, clientEmail);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals("*", claims.get("room")); // Wildcard for backward compatibility
    }

    // TOKEN VALIDATION TESTS

    @Test
    @DisplayName("Should generate token with correct expiration time")
    void testTokenExpirationTime() throws Exception {
        // Arrange
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "expiration-test-room";
        long beforeGeneration = Instant.now().getEpochSecond();

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        Claims claims = parseTokenClaims(token);
        Date expiration = claims.getExpiration();
        assertNotNull(expiration);
        
        long expectedExpiration = beforeGeneration + JitsiJwtGenerator.EXP_TIME_DELAY_SEC;
        long actualExpiration = expiration.toInstant().getEpochSecond();
        
        // Allow for small time differences (within 5 seconds)
        assertTrue(Math.abs(actualExpiration - expectedExpiration) <= 5,
                "Token expiration should be approximately " + JitsiJwtGenerator.EXP_TIME_DELAY_SEC + " seconds from now");
    }

    @Test
    @DisplayName("Should generate token with correct not-before time")
    void testTokenNotBeforeTime() throws Exception {
        // Arrange
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "nbf-test-room";
        long beforeGeneration = Instant.now().getEpochSecond();

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        Claims claims = parseTokenClaims(token);
        Date notBefore = claims.getNotBefore();
        assertNotNull(notBefore);
        
        long expectedNotBefore = beforeGeneration - JitsiJwtGenerator.NBF_TIME_DELAY_SEC;
        long actualNotBefore = notBefore.toInstant().getEpochSecond();
        
        // Allow for small time differences (within 5 seconds)
        assertTrue(Math.abs(actualNotBefore - expectedNotBefore) <= 5,
                "Token not-before should be approximately " + JitsiJwtGenerator.NBF_TIME_DELAY_SEC + " seconds before now");
    }

    @Test
    @DisplayName("Should generate token with unique user ID")
    void testTokenContainsUniqueUserId() throws Exception {
        // Arrange
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "userid-test-room";

        // Act
        String token1 = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);
        String token2 = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        Claims claims1 = parseTokenClaims(token1);
        Claims claims2 = parseTokenClaims(token2);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context1 = (Map<String, Object>) claims1.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user1 = (Map<String, Object>) context1.get("user");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context2 = (Map<String, Object>) claims2.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user2 = (Map<String, Object>) context2.get("user");
        
        String userId1 = (String) user1.get("id");
        String userId2 = (String) user2.get("id");
        
        assertNotNull(userId1);
        assertNotNull(userId2);
        assertNotEquals(userId1, userId2, "Each token should have a unique user ID");
    }

    // ERROR HANDLING TESTS

    @Test
    @DisplayName("Should throw exception when JWT secret is invalid")
    void testInvalidJwtSecret() {
        // Arrange
        ReflectionTestUtils.setField(jitsiJwtGenerator, "jwtSecret", ""); // Empty secret
        
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "error-test-room";

        // Act & Assert
        assertThrows(JwtTokenGenerationException.class, () -> {
            jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);
        });
    }

    @Test
    @DisplayName("Should throw exception when JWT secret is null")
    void testNullJwtSecret() {
        // Arrange
        ReflectionTestUtils.setField(jitsiJwtGenerator, "jwtSecret", null);
        
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "error-test-room";

        // Act & Assert
        assertThrows(JwtTokenGenerationException.class, () -> {
            jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);
        });
    }

    // JAAS JWT BUILDER TESTS

    @Test
    @DisplayName("Should create JaaSJwtBuilder with default values")
    void testJaaSJwtBuilderWithDefaults() throws Exception {
        // Arrange
        long beforeCreation = Instant.now().getEpochSecond();
        
        // Act
        String token = JitsiJwtGenerator.JaaSJwtBuilder.builder()
                .withDefaults()
                .withApiKey(testJaasApiKey)
                .withUserName("Builder Test User")
                .withUserEmail("builder.test@example.com")
                .withRoomName("builder-test-room")
                .withAppID(testJaasAppId)
                .signWith(Keys.hmacShaKeyFor(testJwtSecret.getBytes()));

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        
        // Verify default expiration
        Date expiration = claims.getExpiration();
        long expectedExpiration = beforeCreation + JitsiJwtGenerator.EXP_TIME_DELAY_SEC;
        long actualExpiration = expiration.toInstant().getEpochSecond();
        assertTrue(Math.abs(actualExpiration - expectedExpiration) <= 5);
        
        // Verify default not-before
        Date notBefore = claims.getNotBefore();
        long expectedNotBefore = beforeCreation - JitsiJwtGenerator.NBF_TIME_DELAY_SEC;
        long actualNotBefore = notBefore.toInstant().getEpochSecond();
        assertTrue(Math.abs(actualNotBefore - expectedNotBefore) <= 5);
        
        // Verify user context has moderator set to true by default
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        assertEquals("true", user.get("moderator"));
        assertNotNull(user.get("id")); // Should have a UUID
    }

    @Test
    @DisplayName("Should create JaaSJwtBuilder with custom values")
    void testJaaSJwtBuilderWithCustomValues() throws Exception {
        // Arrange
        String customUserId = "custom-user-123";
        String avatarUrl = "https://example.com/avatar.jpg";
        long customExpTime = Instant.now().getEpochSecond() + 3600; // 1 hour
        long customNbfTime = Instant.now().getEpochSecond() - 60; // 1 minute ago
        
        // Act
        String token = JitsiJwtGenerator.JaaSJwtBuilder.builder()
                .withApiKey(testJaasApiKey)
                .withUserName("Custom User")
                .withUserEmail("custom.user@example.com")
                .withRoomName("custom-room")
                .withAppID(testJaasAppId)
                .withUserId(customUserId)
                .withUserAvatar(avatarUrl)
                .withModerator(false)
                .withExpTime(customExpTime)
                .withNbfTime(customNbfTime)
                .signWith(Keys.hmacShaKeyFor(testJwtSecret.getBytes()));

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        
        // Verify custom times
        assertEquals(customExpTime, claims.getExpiration().toInstant().getEpochSecond());
        assertEquals(customNbfTime, claims.getNotBefore().toInstant().getEpochSecond());
        
        // Verify user context
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        
        assertEquals("Custom User", user.get("name"));
        assertEquals("custom.user@example.com", user.get("email"));
        assertEquals(customUserId, user.get("id"));
        assertEquals(avatarUrl, user.get("avatar"));
        assertEquals("false", user.get("moderator"));
    }

    // EDGE CASES AND VALIDATION TESTS

    @Test
    @DisplayName("Should handle special characters in user name and email")
    void testSpecialCharactersInUserData() throws Exception {
        // Arrange
        String userName = "José María Ñoño";
        String userEmail = "josé.maría@exämple.com";
        String roomName = "special-chars-room";

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) claims.get("context");
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        
        assertEquals(userName, user.get("name"));
        assertEquals(userEmail, user.get("email"));
    }

    @Test
    @DisplayName("Should handle long room names")
    void testLongRoomName() throws Exception {
        // Arrange
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "this-is-a-very-long-room-name-that-might-cause-issues-if-not-handled-properly-in-jwt-generation";

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals(roomName, claims.get("room"));
    }

    @Test
    @DisplayName("Should handle empty room name")
    void testEmptyRoomName() throws Exception {
        // Arrange
        String userName = "Test User";
        String userEmail = "test.user@example.com";
        String roomName = "";

        // Act
        String token = jitsiJwtGenerator.generateJaasTokenWithConfig(userName, userEmail, roomName);

        // Assert
        assertNotNull(token);
        assertTrue(!token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals(roomName, claims.get("room"));
    }

    // HELPER METHODS

    /**
     * Helper method to parse JWT token claims for testing.
     * Note: This is for testing purposes only and doesn't verify the signature.
     */
    private Claims parseTokenClaims(String token) throws Exception {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(testJwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Helper method to decode JWT payload without verification (for testing structure).
     */
    @SuppressWarnings("unused")
    private Map<String, Object> decodeTokenPayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payload = parts[1];
        
        // Add padding if necessary
        StringBuilder payloadBuilder = new StringBuilder(payload);
        while (payloadBuilder.length() % 4 != 0) {
            payloadBuilder.append("=");
        }
        payload = payloadBuilder.toString();
        
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String decodedPayload = new String(decodedBytes);
        
        return objectMapper.readValue(decodedPayload, Map.class);
    }
}