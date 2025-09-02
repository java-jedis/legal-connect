package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.doThrow;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Comprehensive unit tests for OAuthService.
 * Tests all service methods with various scenarios including success cases and error handling.
 */
@DisplayName("OAuthService Tests")
class OAuthServiceTest {

    @Mock
    private OAuthCalendarTokenRepo oAuthCalendarTokenRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private OAuthService oAuthService;

    // Test data
    private User testUser;
    private OAuthCalendarToken testToken;
    private UUID testUserId;
    private String testAccessToken;
    private String testRefreshToken;
    private String testAuthorizationCode;
    private String testStateParameter;
    private OffsetDateTime testAccessExpiry;
    private OffsetDateTime testRefreshExpiry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test configuration properties
        ReflectionTestUtils.setField(oAuthService, "clientId", "test_client_id");
        ReflectionTestUtils.setField(oAuthService, "clientSecret", "test_client_secret");
        ReflectionTestUtils.setField(oAuthService, "redirectUri", "http://localhost:8080/schedule/oauth/callback");
        ReflectionTestUtils.setField(oAuthService, "scope", "https://www.googleapis.com/auth/calendar");
        ReflectionTestUtils.setField(oAuthService, "frontendUrl", "http://localhost:5173");
        
        // Setup test data
        setupTestUser();
        setupTestToken();
        setupTestData();
    }

    private void setupTestUser() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.LAWYER);
        testUser.setEmailVerified(true);
    }

    private void setupTestToken() {
        testAccessToken = "test_access_token_123";
        testRefreshToken = "test_refresh_token_456";
        testAccessExpiry = OffsetDateTime.now().plusHours(1);
        testRefreshExpiry = OffsetDateTime.now().plusMonths(6);
        
        testToken = new OAuthCalendarToken();
        testToken.setId(UUID.randomUUID());
        testToken.setUser(testUser);
        testToken.setAccessToken(testAccessToken);
        testToken.setRefreshToken(testRefreshToken);
        testToken.setAccessExpiry(testAccessExpiry);
        testToken.setRefreshExpiry(testRefreshExpiry);
        testToken.setCreatedAt(OffsetDateTime.now().minusDays(1));
        testToken.setUpdatedAt(OffsetDateTime.now());
    }

    private void setupTestData() {
        testAuthorizationCode = "test_authorization_code_789";
        testStateParameter = testUserId.toString();
    }

    @Test
    @DisplayName("Should create OAuth authorization URL successfully")
    void oAuthAuthorize_Success_ReturnsUrlInJsonResponse() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            // Arrange
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            // Act
            ResponseEntity<ApiResponse<String>> result = oAuthService.oAuthAuthorize();

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            String url = result.getBody().getData();
            assertNotNull(url);
            assertTrue(url.contains("https://accounts.google.com/o/oauth2/v2/auth"));
            assertTrue(url.contains("client_id=test_client_id"));
            assertTrue(url.contains("redirect_uri=http://localhost:8080/schedule/oauth/callback"));
            assertTrue(url.contains("response_type=code"));
            assertTrue(url.contains("scope=https://www.googleapis.com/auth/calendar"));
            assertTrue(url.contains("access_type=offline"));
            assertTrue(url.contains("prompt=consent"));
            assertTrue(url.contains("state=" + testUserId.toString()));
            assertEquals("OAuth Redirect Url Sent ", result.getBody().getMessage());
        }
    }

    @Test
    @DisplayName("Should handle OAuth authorization when user not authenticated")
    void oAuthAuthorize_UserNotAuthenticated_ReturnsUnauthorized() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            // Arrange
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<String>> result = oAuthService.oAuthAuthorize();

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    @DisplayName("Should handle OAuth callback successfully for new user")
    void callback_NewUser_Success_ReturnsSuccessResponse() {
        try (
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleAuthorizationCodeTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleAuthorizationCodeTokenRequest.class, (mock, context) -> {
                GoogleTokenResponse response = new GoogleTokenResponse();
                response.setAccessToken("new_access_token");
                response.setRefreshToken("new_refresh_token");
                response.setExpiresInSeconds(3600L);
                when(mock.execute()).thenReturn(response);
            })
        ) {
            // Arrange
            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(oAuthCalendarTokenRepo.save(any(OAuthCalendarToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, testStateParameter);

            // Assert
            assertEquals(HttpStatus.FOUND, result.getStatusCode());
            assertNotNull(result.getHeaders().getLocation());
            String location = result.getHeaders().getLocation().toString();
            String expectedUrl = "http://localhost:5173/dashboard/" + testUser.getRole().toString().toLowerCase();
            assertEquals(expectedUrl, location);

            ArgumentCaptor<OAuthCalendarToken> tokenCaptor = ArgumentCaptor.forClass(OAuthCalendarToken.class);
            verify(oAuthCalendarTokenRepo).save(tokenCaptor.capture());
            OAuthCalendarToken savedToken = tokenCaptor.getValue();
            assertEquals(testUser, savedToken.getUser());
            assertEquals("new_access_token", savedToken.getAccessToken());
            assertEquals("new_refresh_token", savedToken.getRefreshToken());
            assertNotNull(savedToken.getAccessExpiry());
            assertNotNull(savedToken.getRefreshExpiry());
        }
    }

    @Test
    @DisplayName("Should handle OAuth callback successfully for existing user")
    void callback_ExistingUser_Success_ReturnsSuccessResponse() {
        try (
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleAuthorizationCodeTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleAuthorizationCodeTokenRequest.class, (mock, context) -> {
                GoogleTokenResponse response = new GoogleTokenResponse();
                response.setAccessToken("updated_access_token");
                response.setExpiresInSeconds(3600L);
                // No new refresh token
                when(mock.execute()).thenReturn(response);
            })
        ) {
            // Arrange
            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));
            when(oAuthCalendarTokenRepo.save(any(OAuthCalendarToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, testStateParameter);

            // Assert
            assertEquals(HttpStatus.FOUND, result.getStatusCode());
            assertNotNull(result.getHeaders().getLocation());
            String location = result.getHeaders().getLocation().toString();
            String expectedUrl = "http://localhost:5173/dashboard/" + testUser.getRole().toString().toLowerCase();
            assertEquals(expectedUrl, location);

            ArgumentCaptor<OAuthCalendarToken> tokenCaptor = ArgumentCaptor.forClass(OAuthCalendarToken.class);
            verify(oAuthCalendarTokenRepo).save(tokenCaptor.capture());
            OAuthCalendarToken savedToken = tokenCaptor.getValue();
            assertEquals("updated_access_token", savedToken.getAccessToken());
            assertEquals(testRefreshToken, savedToken.getRefreshToken()); // Original refresh token preserved
            assertNotNull(savedToken.getAccessExpiry());
            assertEquals(testRefreshExpiry, savedToken.getRefreshExpiry()); // Original expiry preserved
        }
    }

    @Test
    @DisplayName("Should handle OAuth callback with invalid state parameter")
    void callback_InvalidState_ReturnsBadRequest() {
        // Arrange
        String invalidState = "invalid_uuid_format";

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, invalidState);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid state parameter", result.getBody().getError().getMessage());
        
        verify(userRepo, never()).findById(any(UUID.class));
        verify(oAuthCalendarTokenRepo, never()).save(any(OAuthCalendarToken.class));
    }

    @Test
    @DisplayName("Should handle OAuth callback with user not found")
    void callback_UserNotFound_ReturnsNotFound() {
        // Arrange
        when(userRepo.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, testStateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody().getError().getMessage());
        
        verify(oAuthCalendarTokenRepo, never()).save(any(OAuthCalendarToken.class));
    }

    @Test
    @DisplayName("Should refresh access token successfully")
    void refreshAccessToken_Success_ReturnsTrue() {
        try (
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleRefreshTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleRefreshTokenRequest.class, (mock, context) -> {
                GoogleTokenResponse response = new GoogleTokenResponse();
                response.setAccessToken("refreshed_access_token");
                response.setExpiresInSeconds(3600L);
                when(mock.execute()).thenReturn(response);
            })
        ) {
            // Arrange
            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));
            when(oAuthCalendarTokenRepo.save(any(OAuthCalendarToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            boolean result = oAuthService.refreshAccessToken(testUserId);

            // Assert
            assertTrue(result);

            ArgumentCaptor<OAuthCalendarToken> tokenCaptor = ArgumentCaptor.forClass(OAuthCalendarToken.class);
            verify(oAuthCalendarTokenRepo).save(tokenCaptor.capture());
            OAuthCalendarToken savedToken = tokenCaptor.getValue();
            assertEquals("refreshed_access_token", savedToken.getAccessToken());
            assertEquals(testRefreshToken, savedToken.getRefreshToken());
            assertNotNull(savedToken.getAccessExpiry());
        }
    }

    @Test
    @DisplayName("Should refresh access token with new refresh token")
    void refreshAccessToken_WithNewRefreshToken_UpdatesRefreshToken() {
        try (
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleRefreshTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleRefreshTokenRequest.class, (mock, context) -> {
                GoogleTokenResponse response = new GoogleTokenResponse();
                response.setAccessToken("refreshed_access");
                response.setRefreshToken("new_refresh_token");
                response.setExpiresInSeconds(3600L);
                when(mock.execute()).thenReturn(response);
            })
        ) {
            // Arrange
            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));
            when(oAuthCalendarTokenRepo.save(any(OAuthCalendarToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            boolean result = oAuthService.refreshAccessToken(testUserId);

            // Assert
            assertTrue(result);

            ArgumentCaptor<OAuthCalendarToken> tokenCaptor = ArgumentCaptor.forClass(OAuthCalendarToken.class);
            verify(oAuthCalendarTokenRepo).save(tokenCaptor.capture());
            OAuthCalendarToken savedToken = tokenCaptor.getValue();
            assertEquals("refreshed_access", savedToken.getAccessToken());
            assertEquals("new_refresh_token", savedToken.getRefreshToken());
            assertNotNull(savedToken.getRefreshExpiry());
        }
    }

    @Test
    @DisplayName("Should handle refresh access token failure")
    void refreshAccessToken_Failure_ReturnsFalse() {
        try (
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleRefreshTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleRefreshTokenRequest.class, (mock, context) -> {
                doThrow(new IOException("Refresh failed")).when(mock).execute();
            })
        ) {
            // Arrange
            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));

            // Act
            boolean result = oAuthService.refreshAccessToken(testUserId);

            // Assert
            assertFalse(result);
            verify(oAuthCalendarTokenRepo, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should check and refresh when token expired and refresh succeeds")
    void checkAndRefreshAccessToken_ExpiredToken_RefreshSucceeds_ReturnsTrue() {
        try (
            MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class);
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleRefreshTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleRefreshTokenRequest.class, (mock, context) -> {
                GoogleTokenResponse response = new GoogleTokenResponse();
                response.setAccessToken("refreshed_access_token");
                response.setExpiresInSeconds(3600L);
                when(mock.execute()).thenReturn(response);
            })
        ) {
            // Arrange
            testToken.setAccessExpiry(OffsetDateTime.now().minusHours(1));
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));
            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(oAuthCalendarTokenRepo.save(any(OAuthCalendarToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            // Act
            boolean result = oAuthService.checkAndRefreshAccessToken();

            // Assert
            assertTrue(result);
            verify(oAuthCalendarTokenRepo, times(2)).findByUserId(testUserId);
            verify(oAuthCalendarTokenRepo).save(any(OAuthCalendarToken.class));
        }
    }

    @Test
    @DisplayName("Should check and refresh when token expired but refresh fails")
    void checkAndRefreshAccessToken_ExpiredToken_RefreshFails_ReturnsFalse() {
        try (
            MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class);
            MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
            MockedStatic<GsonFactory> mockedJsonFactory = mockStatic(GsonFactory.class);
            MockedConstruction<GoogleRefreshTokenRequest> mockedRequest = Mockito.mockConstruction(GoogleRefreshTokenRequest.class, (mock, context) -> {
                doThrow(new IOException("Refresh failed")).when(mock).execute();
            })
        ) {
            // Arrange
            testToken.setAccessExpiry(OffsetDateTime.now().minusHours(1));
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));
            when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));

            NetHttpTransport testTransport = Mockito.mock(NetHttpTransport.class);
            GsonFactory testJsonFactory = Mockito.mock(GsonFactory.class);
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport).thenReturn(testTransport);
            mockedJsonFactory.when(GsonFactory::getDefaultInstance).thenReturn(testJsonFactory);

            // Act
            boolean result = oAuthService.checkAndRefreshAccessToken();

            // Assert
            assertFalse(result);
            verify(oAuthCalendarTokenRepo, times(2)).findByUserId(testUserId);
            verify(oAuthCalendarTokenRepo, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle parse user ID from state parameter successfully")
    void parseUserIdFromState_ValidUUID_ReturnsUUID() {
        // This tests the private method indirectly through callback
        // Arrange
        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, testStateParameter);

        // Assert - Google API calls fail in test environment, but UUID parsing works
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        
        verify(userRepo).findById(testUserId);
    }

    @Test
    @DisplayName("Should handle exception during OAuth callback")
    void callback_Exception_ReturnsInternalServerError() {
        // Arrange
        when(userRepo.findById(testUserId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, testStateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().getError().getMessage().contains("OAuth authentication failed:"));
        
        verify(oAuthCalendarTokenRepo, never()).save(any(OAuthCalendarToken.class));
    }

    @Test
    @DisplayName("Should handle exception during check and refresh access token")
    void checkAndRefreshAccessToken_Exception_ReturnsFalse() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            // Arrange
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenThrow(new RuntimeException("Auth error"));

            // Act
            boolean result = oAuthService.checkAndRefreshAccessToken();

            // Assert
            assertFalse(result);
            
            verify(oAuthCalendarTokenRepo, never()).findByUserId(any(UUID.class));
        }
    }

    @Test
    @DisplayName("Should handle callback scenarios - Google API call fails in test environment")
    void callback_GoogleAPIFails_ReturnsInternalServerError() {
        // Arrange
        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(oAuthCalendarTokenRepo.findByUserId(testUserId)).thenReturn(Optional.of(testToken));

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthService.callback(testAuthorizationCode, testStateParameter);

        // Assert - Google API calls fail in test environment
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        
        verify(userRepo).findById(testUserId);
    }

    @Test
    @DisplayName("Should verify all configuration properties are used in authorization URL")
    void oAuthAuthorize_ConfigurationProperties_AllPropertiesUsedInURL() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            // Arrange
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            // Act
            ResponseEntity<ApiResponse<String>> result = oAuthService.oAuthAuthorize();

            // Assert
            assertNotNull(result);
            assertNotNull(result.getBody());
            String url = result.getBody().getData();
            // Verify all configuration properties are present in the URL
            assertTrue(url.contains("client_id=test_client_id"));
            assertTrue(url.contains("redirect_uri=http://localhost:8080/schedule/oauth/callback"));
            assertTrue(url.contains("scope=https://www.googleapis.com/auth/calendar"));
            assertTrue(url.contains("state=" + testUserId.toString()));
        }
    }
} 