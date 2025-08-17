package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;

/**
 * Comprehensive unit tests for OAuthController.
 * Tests all endpoints with various scenarios including success cases and error handling.
 */
@DisplayName("OAuthController Tests")
class OAuthControllerTest {

    @Mock
    private OAuthService oAuthService;

    @InjectMocks
    private OAuthController oAuthController;

    private String authorizationCode;
    private String stateParameter;
    private String authorizationUrl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        authorizationCode = "test_authorization_code_123";
        stateParameter = "user_uuid_state_parameter";
        authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=test&redirect_uri=http://localhost:8080/callback";
    }

    @Test
    @DisplayName("Should initiate OAuth authorization successfully")
    void authorizeGoogleCalendar_Success_ReturnsRedirectResponse() {
        // Arrange
        ResponseEntity<ApiResponse<String>> expectedResponse = ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(authorizationUrl))
                .build();
        
        when(oAuthService.oAuthAuthorize()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.authorizeGoogleCalendar();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.FOUND, result.getStatusCode());
        assertEquals(authorizationUrl, result.getHeaders().getLocation().toString());
        
        verify(oAuthService).oAuthAuthorize();
    }

    @Test
    @DisplayName("Should handle OAuth authorization when user not authenticated")
    void authorizeGoogleCalendar_UserNotAuthenticated_ReturnsUnauthorized() {
        // Arrange
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "User is not authenticated", HttpStatus.UNAUTHORIZED);
        
        when(oAuthService.oAuthAuthorize()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.authorizeGoogleCalendar();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        
        verify(oAuthService).oAuthAuthorize();
    }

    @Test
    @DisplayName("Should handle OAuth callback successfully")
    void oauthCallback_Success_ReturnsSuccessResponse() {
        // Arrange
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.success(
                "OAuth authentication successful", 
                HttpStatus.OK, 
                "Google Calendar integration completed successfully"
        );
        
        when(oAuthService.callback(authorizationCode, stateParameter)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(authorizationCode, stateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("OAuth authentication successful", result.getBody().getData());
        assertEquals("Google Calendar integration completed successfully", result.getBody().getMessage());
        
        verify(oAuthService).callback(authorizationCode, stateParameter);
    }

    @Test
    @DisplayName("Should handle OAuth callback with invalid authorization code")
    void oauthCallback_InvalidCode_ReturnsErrorResponse() {
        // Arrange
        String invalidCode = "invalid_code";
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "OAuth authentication failed: Invalid authorization code", 
                HttpStatus.BAD_REQUEST
        );
        
        when(oAuthService.callback(invalidCode, stateParameter)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(invalidCode, stateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("OAuth authentication failed: Invalid authorization code", result.getBody().getError().getMessage());
        
        verify(oAuthService).callback(invalidCode, stateParameter);
    }

    @Test
    @DisplayName("Should handle OAuth callback with invalid state parameter")
    void oauthCallback_InvalidState_ReturnsErrorResponse() {
        // Arrange
        String invalidState = "invalid_state";
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "Invalid state parameter", 
                HttpStatus.BAD_REQUEST
        );
        
        when(oAuthService.callback(authorizationCode, invalidState)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(authorizationCode, invalidState);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid state parameter", result.getBody().getError().getMessage());
        
        verify(oAuthService).callback(authorizationCode, invalidState);
    }

    @Test
    @DisplayName("Should handle OAuth callback with user not found")
    void oauthCallback_UserNotFound_ReturnsNotFoundResponse() {
        // Arrange
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "User not found", 
                HttpStatus.NOT_FOUND
        );
        
        when(oAuthService.callback(authorizationCode, stateParameter)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(authorizationCode, stateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody().getError().getMessage());
        
        verify(oAuthService).callback(authorizationCode, stateParameter);
    }

    @Test
    @DisplayName("Should handle OAuth callback with internal server error")
    void oauthCallback_InternalServerError_ReturnsInternalServerErrorResponse() {
        // Arrange
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "OAuth authentication failed: Internal server error", 
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        
        when(oAuthService.callback(authorizationCode, stateParameter)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(authorizationCode, stateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("OAuth authentication failed: Internal server error", result.getBody().getError().getMessage());
        
        verify(oAuthService).callback(authorizationCode, stateParameter);
    }

    @Test
    @DisplayName("Should check Google Calendar status successfully when user has valid tokens")
    void checkGoogleCalendarStatus_HasValidTokens_ReturnsTrue() {
        // Arrange
        when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);

        // Act
        ResponseEntity<ApiResponse<Boolean>> result = oAuthController.checkGoogleCalendarStatus();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User has valid Google Calendar integration", result.getBody().getMessage());
        assertEquals(Boolean.TRUE, result.getBody().getData());
        
        verify(oAuthService).checkAndRefreshAccessToken();
    }

    @Test
    @DisplayName("Should check Google Calendar status when user has no valid tokens")
    void checkGoogleCalendarStatus_NoValidTokens_ReturnsFalse() {
        // Arrange
        when(oAuthService.checkAndRefreshAccessToken()).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Boolean>> result = oAuthController.checkGoogleCalendarStatus();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User does not have Google Calendar integration or tokens are invalid", result.getBody().getMessage());
        assertEquals(Boolean.FALSE, result.getBody().getData());
        
        verify(oAuthService).checkAndRefreshAccessToken();
    }



    @Test
    @DisplayName("Should handle OAuth callback with empty authorization code")
    void oauthCallback_EmptyAuthorizationCode_ReturnsErrorResponse() {
        // Arrange
        String emptyCode = "";
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "OAuth authentication failed: Authorization code is required", 
                HttpStatus.BAD_REQUEST
        );
        
        when(oAuthService.callback(emptyCode, stateParameter)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(emptyCode, stateParameter);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("OAuth authentication failed: Authorization code is required", result.getBody().getError().getMessage());
        
        verify(oAuthService).callback(emptyCode, stateParameter);
    }

    @Test
    @DisplayName("Should handle OAuth callback with empty state parameter")
    void oauthCallback_EmptyState_ReturnsErrorResponse() {
        // Arrange
        String emptyState = "";
        ResponseEntity<ApiResponse<String>> expectedResponse = ApiResponse.error(
                "Invalid state parameter", 
                HttpStatus.BAD_REQUEST
        );
        
        when(oAuthService.callback(authorizationCode, emptyState)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.oauthCallback(authorizationCode, emptyState);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid state parameter", result.getBody().getError().getMessage());
        
        verify(oAuthService).callback(authorizationCode, emptyState);
    }

    @Test
    @DisplayName("Should handle authorization with proper state parameter format")
    void authorizeGoogleCalendar_WithValidUser_ReturnsProperStateParameter() {
        // Arrange
        String expectedStateParam = "12345678-1234-1234-1234-123456789abc";
        String authUrlWithState = "https://accounts.google.com/o/oauth2/v2/auth?state=" + expectedStateParam;
        
        ResponseEntity<ApiResponse<String>> expectedResponse = ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(authUrlWithState))
                .build();
        
        when(oAuthService.oAuthAuthorize()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<String>> result = oAuthController.authorizeGoogleCalendar();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.FOUND, result.getStatusCode());
        assertEquals(authUrlWithState, result.getHeaders().getLocation().toString());
        
        verify(oAuthService).oAuthAuthorize();
    }

    @Test
    @DisplayName("Should verify service method is called exactly once for each endpoint")
    void testServiceMethodCallCounts() {
        // Arrange
        when(oAuthService.oAuthAuthorize()).thenReturn(ResponseEntity.status(HttpStatus.FOUND).location(URI.create(authorizationUrl)).build());
        when(oAuthService.callback(authorizationCode, stateParameter)).thenReturn(ApiResponse.success("Success", HttpStatus.OK, "Success"));
        when(oAuthService.checkAndRefreshAccessToken()).thenReturn(true);

        // Act
        oAuthController.authorizeGoogleCalendar();
        oAuthController.oauthCallback(authorizationCode, stateParameter);
        oAuthController.checkGoogleCalendarStatus();

        // Assert
        verify(oAuthService).oAuthAuthorize();
        verify(oAuthService).callback(authorizationCode, stateParameter);
        verify(oAuthService).checkAndRefreshAccessToken();
    }
} 