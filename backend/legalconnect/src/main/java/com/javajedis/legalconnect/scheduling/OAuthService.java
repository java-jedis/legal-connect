package com.javajedis.legalconnect.scheduling;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    private final OAuthCalendarTokenRepo oAuthCalendarTokenRepo;
    private final UserRepo userRepo;

    @Value("${google.oauth.client-id}")
    private String clientId;
    @Value("${google.oauth.client-secret}")
    private String clientSecret;
    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;
    @Value("${google.oauth.scope}")
    private String scope;
    @Value("${frontend.url}")
    private String frontendUrl;


    /**
     * Generates the Google OAuth2 authorization URL for the authenticated user.
     */
    public ResponseEntity<ApiResponse<String>> oAuthAuthorize() {
        User user = GetUserUtil.getAuthenticatedUser(userRepo);
        if (user == null) {
            log.warn("Unauthorized OAuth authorize attempt");
            return ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String url = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scope)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", user.getId().toString())
                .build().toUriString();

        return ApiResponse.success(url, HttpStatus.OK, "OAuth Redirect Url Sent ");
    }


    /**
     * Handles the OAuth2 callback from Google, exchanges the authorization code for tokens,
     * and stores them for the user.
     */
    public ResponseEntity<ApiResponse<String>> callback(@RequestParam("code") String code, @RequestParam("state") String state) {
        log.debug("Processing OAuth callback with authorization code for user ID: {}", state);

        try {
            UUID userId = parseUserIdFromState(state);
            if (userId == null) {
                return ApiResponse.error("Invalid state parameter", HttpStatus.BAD_REQUEST);
            }

            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found for OAuth callback: {}", userId);
                return ApiResponse.error("User not found", HttpStatus.NOT_FOUND);
            }

            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    transport,
                    jsonFactory,
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    code,
                    redirectUri)
                    .execute();

            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();

            OffsetDateTime accessExpiry = null;
            if (tokenResponse.getExpiresInSeconds() != null) {
                accessExpiry = OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());
            }

            // Refresh tokens typically don't expire or have a very long expiry (6 months)
            // Google doesn't provide refresh token expiry info, so we'll set it to 6 months from now
            OffsetDateTime refreshExpiry = null;
            if (refreshToken != null) {
                refreshExpiry = OffsetDateTime.now().plusMonths(6);
            }

            Optional<OAuthCalendarToken> existingToken = oAuthCalendarTokenRepo.findByUserId(user.getId());
            OAuthCalendarToken oAuthCalendarToken;

            if (existingToken.isPresent()) {
                oAuthCalendarToken = existingToken.get();
                oAuthCalendarToken.setAccessToken(accessToken);
                oAuthCalendarToken.setAccessExpiry(accessExpiry);
                if (refreshToken != null) {
                    oAuthCalendarToken.setRefreshToken(refreshToken);
                    oAuthCalendarToken.setRefreshExpiry(refreshExpiry);
                }
                log.info("Updated OAuth tokens for user: {} with expiry: {}", user.getEmail(), accessExpiry);
            } else {
                oAuthCalendarToken = new OAuthCalendarToken();
                oAuthCalendarToken.setUser(user);
                oAuthCalendarToken.setAccessToken(accessToken);
                oAuthCalendarToken.setAccessExpiry(accessExpiry);
                oAuthCalendarToken.setRefreshToken(refreshToken);
                oAuthCalendarToken.setRefreshExpiry(refreshExpiry);
                log.info("Created new OAuth tokens for user: {} with expiry: {}", user.getEmail(), accessExpiry);
            }

            oAuthCalendarTokenRepo.save(oAuthCalendarToken);

            String userType = user.getRole().toString().toLowerCase();
            String redirectUrl = frontendUrl + "/dashboard/" + userType;
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();

        } catch (Exception e) {
            log.error("Error processing OAuth callback", e);
            return ApiResponse.error("OAuth authentication failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Parses user ID from the OAuth state parameter.
     * Returns null if the state parameter is invalid.
     *
     * @param state the state parameter containing user ID
     * @return UUID of the user, or null if invalid
     */
    private UUID parseUserIdFromState(String state) {
        try {
            return UUID.fromString(state);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid state parameter in OAuth callback: {}", state);
            return null;
        }
    }

    /**
     * Refreshes the access token using the stored refresh token for a specific user.
     * Internal method that returns boolean for success/failure.
     *
     * @param userId the ID of the user whose token should be refreshed
     * @return true if token refresh was successful, false otherwise
     */
    public boolean refreshAccessToken(UUID userId) {
        log.debug("Attempting to refresh access token for user ID: {}", userId);

        try {
            Optional<User> userOptional = userRepo.findById(userId);
            if (userOptional.isEmpty()) {
                log.warn("User not found with ID: {}", userId);
                return false;
            }

            User user = userOptional.get();
            Optional<OAuthCalendarToken> tokenOptional = oAuthCalendarTokenRepo.findByUserId(user.getId());
            if (tokenOptional.isEmpty()) {
                log.warn("No OAuth tokens found for user: {}", user.getEmail());
                return false;
            }

            OAuthCalendarToken storedToken = tokenOptional.get();
            if (storedToken.getRefreshToken() == null || storedToken.getRefreshToken().isEmpty()) {
                log.warn("No refresh token available for user: {}", user.getEmail());
                return false;
            }

            if (storedToken.getRefreshExpiry() != null && storedToken.getRefreshExpiry().isBefore(OffsetDateTime.now())) {
                log.warn("Refresh token expired for user: {}", user.getEmail());
                return false;
            }

            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                    transport,
                    jsonFactory,
                    storedToken.getRefreshToken(),
                    clientId,
                    clientSecret)
                    .execute();

            String newAccessToken = tokenResponse.getAccessToken();
            OffsetDateTime newAccessExpiry = null;
            if (tokenResponse.getExpiresInSeconds() != null) {
                newAccessExpiry = OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());
            }

            storedToken.setAccessToken(newAccessToken);
            storedToken.setAccessExpiry(newAccessExpiry);

            if (tokenResponse.getRefreshToken() != null) {
                storedToken.setRefreshToken(tokenResponse.getRefreshToken());
                storedToken.setRefreshExpiry(OffsetDateTime.now().plusMonths(6));
                log.info("Updated both access and refresh tokens for user: {}", user.getEmail());
            } else {
                log.info("Updated access token for user: {}", user.getEmail());
            }

            oAuthCalendarTokenRepo.save(storedToken);

            log.info("Successfully refreshed access token for user: {} with new expiry: {}",
                    user.getEmail(), newAccessExpiry);
            return true;

        } catch (Exception e) {
            log.error("Error refreshing access token for user ID: {}", userId, e);
            return false;
        }
    }

    /**
     * Checks if the current authenticated user has a valid access token and refreshes it if expired.
     *
     * @return true if user has valid access token (or successfully refreshed), false if no token exists or refresh failed
     */
    public boolean checkAndRefreshAccessToken() {
        log.debug("Checking access token validity for current authenticated user");

        try {
            User user = GetUserUtil.getAuthenticatedUser(userRepo);
            if (user == null) {
                log.warn("User is not authenticated");
                return false;
            }

            UUID userId = user.getId();
            log.debug("Checking access token for user ID: {}", userId);

            Optional<OAuthCalendarToken> tokenOptional = oAuthCalendarTokenRepo.findByUserId(user.getId());

            if (tokenOptional.isEmpty()) {
                log.debug("No OAuth tokens found for user: {}", user.getEmail());
                return false;
            }

            OAuthCalendarToken storedToken = tokenOptional.get();

            if (storedToken.getAccessToken() == null || storedToken.getAccessToken().isEmpty()) {
                log.debug("No access token present for user: {}", user.getEmail());
                return false;
            }

            if (storedToken.getAccessExpiry() != null && storedToken.getAccessExpiry().isBefore(OffsetDateTime.now())) {
                log.debug("Access token expired for user: {}, attempting to refresh", user.getEmail());

                return refreshAccessToken(userId);
            }

            log.debug("Access token is valid for user: {}", user.getEmail());
            return true;

        } catch (Exception e) {
            log.error("Error checking access token for current authenticated user", e);
            return false;
        }
    }

}
