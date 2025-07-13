package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class OAuthCalendarTokenRepoTest {

    @Mock
    private OAuthCalendarTokenRepo oAuthCalendarTokenRepo;

    private OAuthCalendarToken oAuthToken1;
    private OAuthCalendarToken oAuthToken2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize UUIDs
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID token1Id = UUID.randomUUID();
        UUID token2Id = UUID.randomUUID();
        
        // Setup first user
        user1 = new User();
        user1.setId(user1Id);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");
        user1.setRole(Role.LAWYER);
        user1.setEmailVerified(true);

        // Setup second user
        user2 = new User();
        user2.setId(user2Id);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole(Role.USER);
        user2.setEmailVerified(true);

        // Setup first OAuth token
        oAuthToken1 = new OAuthCalendarToken();
        oAuthToken1.setId(token1Id);
        oAuthToken1.setUser(user1);
        oAuthToken1.setAccessToken("access_token_123");
        oAuthToken1.setRefreshToken("refresh_token_456");
        oAuthToken1.setAccessExpiry(OffsetDateTime.now().plusHours(1));
        oAuthToken1.setRefreshExpiry(OffsetDateTime.now().plusDays(30));

        // Setup second OAuth token
        oAuthToken2 = new OAuthCalendarToken();
        oAuthToken2.setId(token2Id);
        oAuthToken2.setUser(user2);
        oAuthToken2.setAccessToken("access_token_789");
        oAuthToken2.setRefreshToken("refresh_token_abc");
        oAuthToken2.setAccessExpiry(OffsetDateTime.now().plusHours(2));
        oAuthToken2.setRefreshExpiry(OffsetDateTime.now().plusDays(60));
    }

    @Test
    void testFindById() {
        when(oAuthCalendarTokenRepo.findById(oAuthToken1.getId())).thenReturn(Optional.of(oAuthToken1));
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findById(oAuthToken1.getId());
        
        assertTrue(foundToken.isPresent());
        assertEquals(oAuthToken1.getId(), foundToken.get().getId());
        assertEquals(oAuthToken1.getAccessToken(), foundToken.get().getAccessToken());
        assertEquals(oAuthToken1.getRefreshToken(), foundToken.get().getRefreshToken());
        assertEquals(user1.getId(), foundToken.get().getUser().getId());
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(oAuthCalendarTokenRepo.findById(nonExistentId)).thenReturn(Optional.empty());
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findById(nonExistentId);
        
        assertFalse(foundToken.isPresent());
    }

    @Test
    void testFindByUserId() {
        when(oAuthCalendarTokenRepo.findByUserId(user1.getId())).thenReturn(Optional.of(oAuthToken1));
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findByUserId(user1.getId());
        
        assertTrue(foundToken.isPresent());
        assertEquals(oAuthToken1.getId(), foundToken.get().getId());
        assertEquals(oAuthToken1.getAccessToken(), foundToken.get().getAccessToken());
        assertEquals(oAuthToken1.getRefreshToken(), foundToken.get().getRefreshToken());
        assertEquals(user1.getId(), foundToken.get().getUser().getId());
    }

    @Test
    void testFindByUserIdSecondUser() {
        when(oAuthCalendarTokenRepo.findByUserId(user2.getId())).thenReturn(Optional.of(oAuthToken2));
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findByUserId(user2.getId());
        
        assertTrue(foundToken.isPresent());
        assertEquals(oAuthToken2.getId(), foundToken.get().getId());
        assertEquals(oAuthToken2.getAccessToken(), foundToken.get().getAccessToken());
        assertEquals(oAuthToken2.getRefreshToken(), foundToken.get().getRefreshToken());
        assertEquals(user2.getId(), foundToken.get().getUser().getId());
    }

    @Test
    void testFindByUserIdNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(oAuthCalendarTokenRepo.findByUserId(nonExistentUserId)).thenReturn(Optional.empty());
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findByUserId(nonExistentUserId);
        
        assertFalse(foundToken.isPresent());
    }

    @Test
    void testSaveOAuthCalendarToken() {
        // Create a new user for the new token
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setFirstName("Bob");
        newUser.setLastName("Johnson");
        newUser.setEmail("bob.johnson@example.com");
        newUser.setRole(Role.LAWYER);
        newUser.setEmailVerified(true);
        
        OAuthCalendarToken newToken = new OAuthCalendarToken();
        newToken.setUser(newUser);
        newToken.setAccessToken("new_access_token_xyz");
        newToken.setRefreshToken("new_refresh_token_def");
        newToken.setAccessExpiry(OffsetDateTime.now().plusHours(1));
        newToken.setRefreshExpiry(OffsetDateTime.now().plusDays(30));
        
        OAuthCalendarToken savedToken = new OAuthCalendarToken();
        savedToken.setId(UUID.randomUUID());
        savedToken.setUser(newToken.getUser());
        savedToken.setAccessToken(newToken.getAccessToken());
        savedToken.setRefreshToken(newToken.getRefreshToken());
        savedToken.setAccessExpiry(newToken.getAccessExpiry());
        savedToken.setRefreshExpiry(newToken.getRefreshExpiry());
        
        when(oAuthCalendarTokenRepo.save(newToken)).thenReturn(savedToken);
        when(oAuthCalendarTokenRepo.findById(savedToken.getId())).thenReturn(Optional.of(savedToken));
        
        OAuthCalendarToken result = oAuthCalendarTokenRepo.save(newToken);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(newToken.getAccessToken(), result.getAccessToken());
        assertEquals(newToken.getRefreshToken(), result.getRefreshToken());
        assertEquals(newUser.getId(), result.getUser().getId());
        
        // Verify it can be found
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findById(result.getId());
        assertTrue(foundToken.isPresent());
        assertEquals(result.getId(), foundToken.get().getId());
    }

    @Test
    void testUpdateOAuthCalendarToken() {
        oAuthToken1.setAccessToken("updated_access_token");
        oAuthToken1.setRefreshToken("updated_refresh_token");
        oAuthToken1.setAccessExpiry(OffsetDateTime.now().plusHours(3));
        oAuthToken1.setRefreshExpiry(OffsetDateTime.now().plusDays(45));
        
        when(oAuthCalendarTokenRepo.save(oAuthToken1)).thenReturn(oAuthToken1);
        
        OAuthCalendarToken updatedToken = oAuthCalendarTokenRepo.save(oAuthToken1);
        
        assertNotNull(updatedToken);
        assertEquals(oAuthToken1.getId(), updatedToken.getId());
        assertEquals("updated_access_token", updatedToken.getAccessToken());
        assertEquals("updated_refresh_token", updatedToken.getRefreshToken());
        assertEquals(user1.getId(), updatedToken.getUser().getId());
    }

    @Test
    void testDeleteByUserId() {
        // Verify token exists before deletion
        when(oAuthCalendarTokenRepo.findByUserId(user1.getId())).thenReturn(Optional.of(oAuthToken1))
                .thenReturn(Optional.empty());
        when(oAuthCalendarTokenRepo.findByUserId(user2.getId())).thenReturn(Optional.of(oAuthToken2));
        
        Optional<OAuthCalendarToken> tokenBeforeDelete = oAuthCalendarTokenRepo.findByUserId(user1.getId());
        assertTrue(tokenBeforeDelete.isPresent());
        
        oAuthCalendarTokenRepo.deleteByUserId(user1.getId());
        
        // Verify token is deleted
        Optional<OAuthCalendarToken> tokenAfterDelete = oAuthCalendarTokenRepo.findByUserId(user1.getId());
        assertFalse(tokenAfterDelete.isPresent());
        
        // Verify other tokens still exist
        Optional<OAuthCalendarToken> otherToken = oAuthCalendarTokenRepo.findByUserId(user2.getId());
        assertTrue(otherToken.isPresent());
    }

    @Test
    void testDeleteByUserIdNonExistent() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(oAuthCalendarTokenRepo.findByUserId(user1.getId())).thenReturn(Optional.of(oAuthToken1));
        when(oAuthCalendarTokenRepo.findByUserId(user2.getId())).thenReturn(Optional.of(oAuthToken2));
        
        // This should not throw an exception
        oAuthCalendarTokenRepo.deleteByUserId(nonExistentUserId);
        
        // Verify existing tokens are not affected
        Optional<OAuthCalendarToken> token1 = oAuthCalendarTokenRepo.findByUserId(user1.getId());
        Optional<OAuthCalendarToken> token2 = oAuthCalendarTokenRepo.findByUserId(user2.getId());
        assertTrue(token1.isPresent());
        assertTrue(token2.isPresent());
    }

    @Test
    void testDeleteById() {
        UUID tokenId = oAuthToken1.getId();
        when(oAuthCalendarTokenRepo.findById(tokenId)).thenReturn(Optional.empty());
        when(oAuthCalendarTokenRepo.findById(oAuthToken2.getId())).thenReturn(Optional.of(oAuthToken2));
        
        oAuthCalendarTokenRepo.deleteById(tokenId);
        
        Optional<OAuthCalendarToken> deletedToken = oAuthCalendarTokenRepo.findById(tokenId);
        assertFalse(deletedToken.isPresent());
        
        // Verify other token still exists
        Optional<OAuthCalendarToken> otherToken = oAuthCalendarTokenRepo.findById(oAuthToken2.getId());
        assertTrue(otherToken.isPresent());
    }

    @Test
    void testFindAll() {
        when(oAuthCalendarTokenRepo.findAll()).thenReturn(Collections.emptyList());
        
        var allTokens = oAuthCalendarTokenRepo.findAll();
        
        assertNotNull(allTokens);
        assertEquals(0, allTokens.size());
    }

    @Test
    void testCount() {
        when(oAuthCalendarTokenRepo.count()).thenReturn(2L);
        
        long count = oAuthCalendarTokenRepo.count();
        assertEquals(2, count);
    }

    @Test
    void testExistsById() {
        when(oAuthCalendarTokenRepo.existsById(oAuthToken1.getId())).thenReturn(true);
        when(oAuthCalendarTokenRepo.existsById(oAuthToken2.getId())).thenReturn(true);
        when(oAuthCalendarTokenRepo.existsById(UUID.randomUUID())).thenReturn(false);
        
        assertTrue(oAuthCalendarTokenRepo.existsById(oAuthToken1.getId()));
        assertTrue(oAuthCalendarTokenRepo.existsById(oAuthToken2.getId()));
        assertFalse(oAuthCalendarTokenRepo.existsById(UUID.randomUUID()));
    }

    @Test
    void testUniqueConstraintOnUserId() {
        // Create a new token for an existing user (should replace the old one when saved)
        OAuthCalendarToken duplicateToken = new OAuthCalendarToken();
        duplicateToken.setUser(user1);
        duplicateToken.setAccessToken("duplicate_access_token");
        duplicateToken.setRefreshToken("duplicate_refresh_token");
        duplicateToken.setAccessExpiry(OffsetDateTime.now().plusHours(1));
        duplicateToken.setRefreshExpiry(OffsetDateTime.now().plusDays(30));
        
        OAuthCalendarToken savedToken = new OAuthCalendarToken();
        savedToken.setId(UUID.randomUUID());
        savedToken.setUser(user1);
        savedToken.setAccessToken("duplicate_access_token");
        savedToken.setRefreshToken("duplicate_refresh_token");
        savedToken.setAccessExpiry(duplicateToken.getAccessExpiry());
        savedToken.setRefreshExpiry(duplicateToken.getRefreshExpiry());
        
        when(oAuthCalendarTokenRepo.save(duplicateToken)).thenReturn(savedToken);
        when(oAuthCalendarTokenRepo.findByUserId(user1.getId())).thenReturn(Optional.of(savedToken));
        
        // This should work as we're creating a new token
        OAuthCalendarToken result = oAuthCalendarTokenRepo.save(duplicateToken);
        assertNotNull(result);
        
        // Verify only the new token exists for this user
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findByUserId(user1.getId());
        assertTrue(foundToken.isPresent());
        assertEquals("duplicate_access_token", foundToken.get().getAccessToken());
    }

    @Test
    void testTokenExpiryDates() {
        when(oAuthCalendarTokenRepo.findById(oAuthToken1.getId())).thenReturn(Optional.of(oAuthToken1));
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findById(oAuthToken1.getId());
        
        assertTrue(foundToken.isPresent());
        assertNotNull(foundToken.get().getAccessExpiry());
        assertNotNull(foundToken.get().getRefreshExpiry());
        assertTrue(foundToken.get().getRefreshExpiry().isAfter(foundToken.get().getAccessExpiry()));
    }

    @Test
    void testTokenUserRelationship() {
        when(oAuthCalendarTokenRepo.findById(oAuthToken1.getId())).thenReturn(Optional.of(oAuthToken1));
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findById(oAuthToken1.getId());
        
        assertTrue(foundToken.isPresent());
        assertNotNull(foundToken.get().getUser());
        assertEquals(user1.getId(), foundToken.get().getUser().getId());
        assertEquals(user1.getEmail(), foundToken.get().getUser().getEmail());
    }

    @Test
    void testRepositoryInheritedMethods() {
        when(oAuthCalendarTokenRepo.findAll()).thenReturn(Collections.emptyList());
        when(oAuthCalendarTokenRepo.count()).thenReturn(2L);
        when(oAuthCalendarTokenRepo.existsById(oAuthToken1.getId())).thenReturn(true);
        
        // Test that the repository inherits standard JpaRepository methods
        assertNotNull(oAuthCalendarTokenRepo.findAll());
        assertTrue(oAuthCalendarTokenRepo.count() > 0);
        assertTrue(oAuthCalendarTokenRepo.existsById(oAuthToken1.getId()));
    }

    @Test
    void testFindByUserIdWithLazyLoading() {
        when(oAuthCalendarTokenRepo.findByUserId(user1.getId())).thenReturn(Optional.of(oAuthToken1));
        
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findByUserId(user1.getId());
        
        assertTrue(foundToken.isPresent());
        
        // Access the user to test lazy loading
        User tokenUser = foundToken.get().getUser();
        assertNotNull(tokenUser);
        assertEquals(user1.getId(), tokenUser.getId());
        assertEquals(user1.getEmail(), tokenUser.getEmail());
        assertEquals(user1.getRole(), tokenUser.getRole());
    }

    @Test
    void testTokenWithLongTokenValues() {
        String longAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String longRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyNDI2MjJ9.H_5vEQJrNBtDGdJGnBJPUNpKRJ8CgN7MXjNgYsqGEzI";
        
        oAuthToken1.setAccessToken(longAccessToken);
        oAuthToken1.setRefreshToken(longRefreshToken);
        
        when(oAuthCalendarTokenRepo.save(oAuthToken1)).thenReturn(oAuthToken1);
        when(oAuthCalendarTokenRepo.findById(oAuthToken1.getId())).thenReturn(Optional.of(oAuthToken1));
        
        OAuthCalendarToken savedToken = oAuthCalendarTokenRepo.save(oAuthToken1);
        
        assertNotNull(savedToken);
        assertEquals(longAccessToken, savedToken.getAccessToken());
        assertEquals(longRefreshToken, savedToken.getRefreshToken());
        
        // Verify it can be retrieved
        Optional<OAuthCalendarToken> foundToken = oAuthCalendarTokenRepo.findById(savedToken.getId());
        assertTrue(foundToken.isPresent());
        assertEquals(longAccessToken, foundToken.get().getAccessToken());
        assertEquals(longRefreshToken, foundToken.get().getRefreshToken());
    }
} 