package com.javajedis.legalconnect.scheduling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.scheduling.dto.OAuthCalendarTokenResponseDTO.UserSummaryDTO;

class OAuthCalendarTokenResponseDTOTest {

    private OAuthCalendarTokenResponseDTO oAuthCalendarTokenResponseDTO;
    private UUID testId;
    private UUID testUserId;
    private OffsetDateTime testAccessExpiry;
    private OffsetDateTime testRefreshExpiry;
    private OffsetDateTime testCreatedAt;
    private OffsetDateTime testUpdatedAt;
    private UserSummaryDTO testUserSummaryDTO;

    @BeforeEach
    void setUp() {
        oAuthCalendarTokenResponseDTO = new OAuthCalendarTokenResponseDTO();
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testAccessExpiry = OffsetDateTime.now().plusHours(1);
        testRefreshExpiry = OffsetDateTime.now().plusDays(30);
        testCreatedAt = OffsetDateTime.now().minusDays(1);
        testUpdatedAt = OffsetDateTime.now();
        
        testUserSummaryDTO = new UserSummaryDTO();
        testUserSummaryDTO.setId(testUserId);
        testUserSummaryDTO.setFirstName("John");
        testUserSummaryDTO.setLastName("Doe");
        testUserSummaryDTO.setEmail("john.doe@example.com");
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(oAuthCalendarTokenResponseDTO);
        assertNull(oAuthCalendarTokenResponseDTO.getId());
        assertNull(oAuthCalendarTokenResponseDTO.getUser());
        assertFalse(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertFalse(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());
        assertNull(oAuthCalendarTokenResponseDTO.getAccessExpiry());
        assertNull(oAuthCalendarTokenResponseDTO.getRefreshExpiry());
        assertNull(oAuthCalendarTokenResponseDTO.getCreatedAt());
        assertNull(oAuthCalendarTokenResponseDTO.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OAuthCalendarTokenResponseDTO dto = new OAuthCalendarTokenResponseDTO(
            testId,
            testUserSummaryDTO,
            true,  // isAccessTokenValid
            false, // isRefreshTokenValid
            testAccessExpiry,
            testRefreshExpiry,
            testCreatedAt,
            testUpdatedAt
        );

        assertEquals(testId, dto.getId());
        assertEquals(testUserSummaryDTO, dto.getUser());
        assertTrue(dto.isAccessTokenValid());
        assertFalse(dto.isRefreshTokenValid());
        assertEquals(testAccessExpiry, dto.getAccessExpiry());
        assertEquals(testRefreshExpiry, dto.getRefreshExpiry());
        assertEquals(testCreatedAt, dto.getCreatedAt());
        assertEquals(testUpdatedAt, dto.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        oAuthCalendarTokenResponseDTO.setId(testId);
        oAuthCalendarTokenResponseDTO.setUser(testUserSummaryDTO);
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(true);
        oAuthCalendarTokenResponseDTO.setRefreshTokenValid(true);
        oAuthCalendarTokenResponseDTO.setAccessExpiry(testAccessExpiry);
        oAuthCalendarTokenResponseDTO.setRefreshExpiry(testRefreshExpiry);
        oAuthCalendarTokenResponseDTO.setCreatedAt(testCreatedAt);
        oAuthCalendarTokenResponseDTO.setUpdatedAt(testUpdatedAt);

        assertEquals(testId, oAuthCalendarTokenResponseDTO.getId());
        assertEquals(testUserSummaryDTO, oAuthCalendarTokenResponseDTO.getUser());
        assertTrue(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertTrue(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());
        assertEquals(testAccessExpiry, oAuthCalendarTokenResponseDTO.getAccessExpiry());
        assertEquals(testRefreshExpiry, oAuthCalendarTokenResponseDTO.getRefreshExpiry());
        assertEquals(testCreatedAt, oAuthCalendarTokenResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, oAuthCalendarTokenResponseDTO.getUpdatedAt());
    }

    @Test
    void testTokenValidityFlags() {
        // Test both flags false
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(false);
        oAuthCalendarTokenResponseDTO.setRefreshTokenValid(false);
        assertFalse(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertFalse(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());

        // Test access token valid, refresh token invalid
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(true);
        oAuthCalendarTokenResponseDTO.setRefreshTokenValid(false);
        assertTrue(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertFalse(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());

        // Test access token invalid, refresh token valid
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(false);
        oAuthCalendarTokenResponseDTO.setRefreshTokenValid(true);
        assertFalse(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertTrue(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());

        // Test both flags true
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(true);
        oAuthCalendarTokenResponseDTO.setRefreshTokenValid(true);
        assertTrue(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertTrue(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());
    }

    @Test
    void testSettersWithNullValues() {
        oAuthCalendarTokenResponseDTO.setId(null);
        oAuthCalendarTokenResponseDTO.setUser(null);
        oAuthCalendarTokenResponseDTO.setAccessExpiry(null);
        oAuthCalendarTokenResponseDTO.setRefreshExpiry(null);
        oAuthCalendarTokenResponseDTO.setCreatedAt(null);
        oAuthCalendarTokenResponseDTO.setUpdatedAt(null);

        assertNull(oAuthCalendarTokenResponseDTO.getId());
        assertNull(oAuthCalendarTokenResponseDTO.getUser());
        assertNull(oAuthCalendarTokenResponseDTO.getAccessExpiry());
        assertNull(oAuthCalendarTokenResponseDTO.getRefreshExpiry());
        assertNull(oAuthCalendarTokenResponseDTO.getCreatedAt());
        assertNull(oAuthCalendarTokenResponseDTO.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        OAuthCalendarTokenResponseDTO dto1 = new OAuthCalendarTokenResponseDTO(
            testId, testUserSummaryDTO, true, false, testAccessExpiry, testRefreshExpiry, testCreatedAt, testUpdatedAt
        );
        
        OAuthCalendarTokenResponseDTO dto2 = new OAuthCalendarTokenResponseDTO(
            testId, testUserSummaryDTO, true, false, testAccessExpiry, testRefreshExpiry, testCreatedAt, testUpdatedAt
        );
        
        OAuthCalendarTokenResponseDTO dto3 = new OAuthCalendarTokenResponseDTO(
            UUID.randomUUID(), testUserSummaryDTO, false, true, testAccessExpiry.plusHours(1), testRefreshExpiry.plusDays(1), testCreatedAt, testUpdatedAt
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        oAuthCalendarTokenResponseDTO.setId(testId);
        oAuthCalendarTokenResponseDTO.setUser(testUserSummaryDTO);
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(true);

        String toString = oAuthCalendarTokenResponseDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(testId.toString()));
        assertTrue(toString.contains("john.doe@example.com"));
        assertTrue(toString.contains("true"));
    }

    // Tests for nested UserSummaryDTO
    @Test
    void testUserSummaryDTODefaultConstructor() {
        UserSummaryDTO userSummary = new UserSummaryDTO();
        assertNotNull(userSummary);
        assertNull(userSummary.getId());
        assertNull(userSummary.getFirstName());
        assertNull(userSummary.getLastName());
        assertNull(userSummary.getEmail());
    }

    @Test
    void testUserSummaryDTOAllArgsConstructor() {
        UserSummaryDTO userSummary = new UserSummaryDTO(testUserId, "Jane", "Smith", "jane.smith@example.com");

        assertEquals(testUserId, userSummary.getId());
        assertEquals("Jane", userSummary.getFirstName());
        assertEquals("Smith", userSummary.getLastName());
        assertEquals("jane.smith@example.com", userSummary.getEmail());
    }

    @Test
    void testUserSummaryDTOSettersAndGetters() {
        UserSummaryDTO userSummary = new UserSummaryDTO();
        
        userSummary.setId(testUserId);
        userSummary.setFirstName("Alice");
        userSummary.setLastName("Johnson");
        userSummary.setEmail("alice.johnson@example.com");

        assertEquals(testUserId, userSummary.getId());
        assertEquals("Alice", userSummary.getFirstName());
        assertEquals("Johnson", userSummary.getLastName());
        assertEquals("alice.johnson@example.com", userSummary.getEmail());
    }

    @Test
    void testUserSummaryDTOEqualsAndHashCode() {
        UserSummaryDTO user1 = new UserSummaryDTO(testUserId, "John", "Doe", "john.doe@example.com");
        UserSummaryDTO user2 = new UserSummaryDTO(testUserId, "John", "Doe", "john.doe@example.com");
        UserSummaryDTO user3 = new UserSummaryDTO(UUID.randomUUID(), "Jane", "Smith", "jane.smith@example.com");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testUserSummaryDTOToString() {
        UserSummaryDTO userSummary = new UserSummaryDTO(testUserId, "John", "Doe", "john.doe@example.com");

        String toString = userSummary.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("john.doe@example.com"));
        assertTrue(toString.contains(testUserId.toString()));
    }

    @Test
    void testUserSummaryDTOWithNullValues() {
        UserSummaryDTO userSummary = new UserSummaryDTO();
        userSummary.setId(null);
        userSummary.setFirstName(null);
        userSummary.setLastName(null);
        userSummary.setEmail(null);

        assertNull(userSummary.getId());
        assertNull(userSummary.getFirstName());
        assertNull(userSummary.getLastName());
        assertNull(userSummary.getEmail());
    }

    @Test
    void testUserSummaryDTOWithEmptyValues() {
        UserSummaryDTO userSummary = new UserSummaryDTO();
        userSummary.setFirstName("");
        userSummary.setLastName("");
        userSummary.setEmail("");

        assertEquals("", userSummary.getFirstName());
        assertEquals("", userSummary.getLastName());
        assertEquals("", userSummary.getEmail());
    }

    @Test
    void testNestedUserSummaryDTO() {
        oAuthCalendarTokenResponseDTO.setUser(testUserSummaryDTO);
        
        UserSummaryDTO retrievedUser = oAuthCalendarTokenResponseDTO.getUser();
        assertNotNull(retrievedUser);
        assertEquals(testUserId, retrievedUser.getId());
        assertEquals("John", retrievedUser.getFirstName());
        assertEquals("Doe", retrievedUser.getLastName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());
    }

    @Test
    void testTokenExpiryDates() {
        OffsetDateTime pastDate = OffsetDateTime.now().minusHours(1);
        OffsetDateTime futureDate = OffsetDateTime.now().plusHours(1);

        // Test with past expiry dates (expired tokens)
        oAuthCalendarTokenResponseDTO.setAccessExpiry(pastDate);
        oAuthCalendarTokenResponseDTO.setRefreshExpiry(pastDate);

        assertEquals(pastDate, oAuthCalendarTokenResponseDTO.getAccessExpiry());
        assertEquals(pastDate, oAuthCalendarTokenResponseDTO.getRefreshExpiry());

        // Test with future expiry dates (valid tokens)
        oAuthCalendarTokenResponseDTO.setAccessExpiry(futureDate);
        oAuthCalendarTokenResponseDTO.setRefreshExpiry(futureDate);

        assertEquals(futureDate, oAuthCalendarTokenResponseDTO.getAccessExpiry());
        assertEquals(futureDate, oAuthCalendarTokenResponseDTO.getRefreshExpiry());
    }

    @Test
    void testAuditDates() {
        OffsetDateTime createdDate = OffsetDateTime.now().minusHours(24);
        OffsetDateTime updatedDate = OffsetDateTime.now();

        oAuthCalendarTokenResponseDTO.setCreatedAt(createdDate);
        oAuthCalendarTokenResponseDTO.setUpdatedAt(updatedDate);

        assertEquals(createdDate, oAuthCalendarTokenResponseDTO.getCreatedAt());
        assertEquals(updatedDate, oAuthCalendarTokenResponseDTO.getUpdatedAt());
        
        // Verify updated date is after created date
        assertTrue(updatedDate.isAfter(createdDate));
    }

    @Test
    void testCompleteOAuthTokenResponse() {
        // Create a complete OAuth token response
        oAuthCalendarTokenResponseDTO.setId(testId);
        oAuthCalendarTokenResponseDTO.setUser(testUserSummaryDTO);
        oAuthCalendarTokenResponseDTO.setAccessTokenValid(true);
        oAuthCalendarTokenResponseDTO.setRefreshTokenValid(false);
        oAuthCalendarTokenResponseDTO.setAccessExpiry(testAccessExpiry);
        oAuthCalendarTokenResponseDTO.setRefreshExpiry(testRefreshExpiry);
        oAuthCalendarTokenResponseDTO.setCreatedAt(testCreatedAt);
        oAuthCalendarTokenResponseDTO.setUpdatedAt(testUpdatedAt);

        // Verify all fields are set correctly
        assertEquals(testId, oAuthCalendarTokenResponseDTO.getId());
        assertEquals(testUserSummaryDTO, oAuthCalendarTokenResponseDTO.getUser());
        assertTrue(oAuthCalendarTokenResponseDTO.isAccessTokenValid());
        assertFalse(oAuthCalendarTokenResponseDTO.isRefreshTokenValid());
        assertEquals(testAccessExpiry, oAuthCalendarTokenResponseDTO.getAccessExpiry());
        assertEquals(testRefreshExpiry, oAuthCalendarTokenResponseDTO.getRefreshExpiry());
        assertEquals(testCreatedAt, oAuthCalendarTokenResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, oAuthCalendarTokenResponseDTO.getUpdatedAt());

        // Verify nested user object
        UserSummaryDTO user = oAuthCalendarTokenResponseDTO.getUser();
        assertEquals(testUserId, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
    }
} 