package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class OAuthCalendarTokenTest {

    private OAuthCalendarToken oAuthToken;
    private User user;
    private UUID testTokenId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testTokenId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        // Setup user
        user = new User();
        user.setId(testUserId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(Role.LAWYER);
        user.setEmailVerified(true);
        
        // Setup OAuth token
        oAuthToken = new OAuthCalendarToken();
        oAuthToken.setId(testTokenId);
        oAuthToken.setUser(user);
        oAuthToken.setAccessToken("access_token_123");
        oAuthToken.setRefreshToken("refresh_token_456");
        oAuthToken.setAccessExpiry(OffsetDateTime.now().plusHours(1));
        oAuthToken.setRefreshExpiry(OffsetDateTime.now().plusDays(30));
    }

    @Test
    void testDefaultConstructor() {
        OAuthCalendarToken defaultToken = new OAuthCalendarToken();
        
        assertNotNull(defaultToken);
        assertNull(defaultToken.getId());
        assertNull(defaultToken.getUser());
        assertNull(defaultToken.getAccessToken());
        assertNull(defaultToken.getRefreshToken());
        assertNull(defaultToken.getAccessExpiry());
        assertNull(defaultToken.getRefreshExpiry());
        assertNull(defaultToken.getCreatedAt());
        assertNull(defaultToken.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime accessExpiry = now.plusHours(1);
        OffsetDateTime refreshExpiry = now.plusDays(30);
        
        OAuthCalendarToken constructedToken = new OAuthCalendarToken(
            testTokenId, user, "constructor_access_token", "constructor_refresh_token",
            accessExpiry, refreshExpiry, now, now
        );
        
        assertEquals(testTokenId, constructedToken.getId());
        assertEquals(user, constructedToken.getUser());
        assertEquals("constructor_access_token", constructedToken.getAccessToken());
        assertEquals("constructor_refresh_token", constructedToken.getRefreshToken());
        assertEquals(accessExpiry, constructedToken.getAccessExpiry());
        assertEquals(refreshExpiry, constructedToken.getRefreshExpiry());
        assertEquals(now, constructedToken.getCreatedAt());
        assertEquals(now, constructedToken.getUpdatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        oAuthToken.setId(newId);
        assertEquals(newId, oAuthToken.getId());
    }

    @Test
    void testUserGetterAndSetter() {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail("newuser@example.com");
        newUser.setRole(Role.USER);
        
        oAuthToken.setUser(newUser);
        assertEquals(newUser, oAuthToken.getUser());
        assertEquals("newuser@example.com", oAuthToken.getUser().getEmail());
    }

    @Test
    void testAccessTokenGetterAndSetter() {
        String newAccessToken = "new_access_token_789";
        oAuthToken.setAccessToken(newAccessToken);
        assertEquals(newAccessToken, oAuthToken.getAccessToken());
    }

    @Test
    void testRefreshTokenGetterAndSetter() {
        String newRefreshToken = "new_refresh_token_abc";
        oAuthToken.setRefreshToken(newRefreshToken);
        assertEquals(newRefreshToken, oAuthToken.getRefreshToken());
    }

    @Test
    void testAccessExpiryGetterAndSetter() {
        OffsetDateTime newAccessExpiry = OffsetDateTime.now().plusHours(2);
        oAuthToken.setAccessExpiry(newAccessExpiry);
        assertEquals(newAccessExpiry, oAuthToken.getAccessExpiry());
    }

    @Test
    void testRefreshExpiryGetterAndSetter() {
        OffsetDateTime newRefreshExpiry = OffsetDateTime.now().plusDays(60);
        oAuthToken.setRefreshExpiry(newRefreshExpiry);
        assertEquals(newRefreshExpiry, oAuthToken.getRefreshExpiry());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        oAuthToken.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, oAuthToken.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        oAuthToken.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, oAuthToken.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        OAuthCalendarToken token1 = new OAuthCalendarToken();
        token1.setId(testTokenId);
        token1.setAccessToken("access_token");
        token1.setRefreshToken("refresh_token");
        
        OAuthCalendarToken token2 = new OAuthCalendarToken();
        token2.setId(testTokenId);
        token2.setAccessToken("access_token");
        token2.setRefreshToken("refresh_token");
        
        OAuthCalendarToken token3 = new OAuthCalendarToken();
        token3.setId(UUID.randomUUID());
        token3.setAccessToken("different_access_token");
        token3.setRefreshToken("different_refresh_token");
        
        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
        assertNotEquals(token1, token3);
        assertNotEquals(token1.hashCode(), token3.hashCode());
    }

    @Test
    void testEqualsWithNullValues() {
        OAuthCalendarToken token1 = new OAuthCalendarToken();
        OAuthCalendarToken token2 = new OAuthCalendarToken();
        
        assertEquals(token1, token2);
        
        token1.setId(testTokenId);
        assertNotEquals(token1, token2);
    }

    @Test
    void testToString() {
        String toStringResult = oAuthToken.toString();
        
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("OAuthCalendarToken"));
        assertTrue(toStringResult.contains("access_token_123"));
        assertTrue(toStringResult.contains("refresh_token_456"));
    }

    @Test
    void testTokenWithLongAccessToken() {
        String longAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        oAuthToken.setAccessToken(longAccessToken);
        assertEquals(longAccessToken, oAuthToken.getAccessToken());
    }

    @Test
    void testTokenWithLongRefreshToken() {
        String longRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyNDI2MjJ9.H_5vEQJrNBtDGdJGnBJPUNpKRJ8CgN7MXjNgYsqGEzI";
        oAuthToken.setRefreshToken(longRefreshToken);
        assertEquals(longRefreshToken, oAuthToken.getRefreshToken());
    }

    @Test
    void testTokenUserRelationship() {
        assertNotNull(oAuthToken.getUser());
        assertEquals(user, oAuthToken.getUser());
        assertEquals(testUserId, oAuthToken.getUser().getId());
        assertEquals("john.doe@example.com", oAuthToken.getUser().getEmail());
    }

    @Test
    void testTokenWithTimestamps() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        
        oAuthToken.setCreatedAt(createdAt);
        oAuthToken.setUpdatedAt(updatedAt);
        
        assertEquals(createdAt, oAuthToken.getCreatedAt());
        assertEquals(updatedAt, oAuthToken.getUpdatedAt());
        assertTrue(oAuthToken.getUpdatedAt().isAfter(oAuthToken.getCreatedAt()));
    }

    @Test
    void testTokenExpiryDates() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime accessExpiry = now.plusHours(1);
        OffsetDateTime refreshExpiry = now.plusDays(30);
        
        oAuthToken.setAccessExpiry(accessExpiry);
        oAuthToken.setRefreshExpiry(refreshExpiry);
        
        assertEquals(accessExpiry, oAuthToken.getAccessExpiry());
        assertEquals(refreshExpiry, oAuthToken.getRefreshExpiry());
        assertTrue(oAuthToken.getRefreshExpiry().isAfter(oAuthToken.getAccessExpiry()));
    }

    @Test
    void testTokenWithMinimalData() {
        OAuthCalendarToken minimalToken = new OAuthCalendarToken();
        minimalToken.setAccessToken("minimal_access_token");
        minimalToken.setRefreshToken("minimal_refresh_token");
        minimalToken.setAccessExpiry(OffsetDateTime.now().plusHours(1));
        minimalToken.setRefreshExpiry(OffsetDateTime.now().plusDays(30));
        
        assertEquals("minimal_access_token", minimalToken.getAccessToken());
        assertEquals("minimal_refresh_token", minimalToken.getRefreshToken());
        assertNotNull(minimalToken.getAccessExpiry());
        assertNotNull(minimalToken.getRefreshExpiry());
    }

    @Test
    void testTokenEntityFields() {
        assertNotNull(oAuthToken.getId());
        assertNotNull(oAuthToken.getUser());
        assertNotNull(oAuthToken.getAccessToken());
        assertNotNull(oAuthToken.getRefreshToken());
        assertNotNull(oAuthToken.getAccessExpiry());
        assertNotNull(oAuthToken.getRefreshExpiry());
        
        assertEquals(testTokenId, oAuthToken.getId());
        assertEquals("access_token_123", oAuthToken.getAccessToken());
        assertEquals("refresh_token_456", oAuthToken.getRefreshToken());
    }

    @Test
    void testTokenExpiry() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime pastExpiry = now.minusHours(1);
        OffsetDateTime futureExpiry = now.plusHours(1);
        
        oAuthToken.setAccessExpiry(pastExpiry);
        assertTrue(oAuthToken.getAccessExpiry().isBefore(now));
        
        oAuthToken.setAccessExpiry(futureExpiry);
        assertTrue(oAuthToken.getAccessExpiry().isAfter(now));
    }

    @Test
    void testTokenUniqueConstraint() {
        // Test that the token is properly associated with a unique user
        assertNotNull(oAuthToken.getUser());
        assertEquals(testUserId, oAuthToken.getUser().getId());
        
        // The unique constraint would be enforced at the database level
        // This test verifies the relationship is correctly set
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setEmail("another@example.com");
        
        oAuthToken.setUser(anotherUser);
        assertEquals(anotherUser, oAuthToken.getUser());
    }
} 