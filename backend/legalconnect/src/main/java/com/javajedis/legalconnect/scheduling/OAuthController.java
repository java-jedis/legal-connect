package com.javajedis.legalconnect.scheduling;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "7. OAuth", description = "OAuth2 Google Calendar integration endpoints")
@RestController
@RequestMapping("/schedule/oauth")
public class OAuthController {
    
    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    /**
     * Initiates OAuth2 authorization flow with Google Calendar.
     * Redirects user to Google's authorization page.
     */
    @RequireUserOrVerifiedLawyer
    @Operation(summary = "Authorize Google Calendar", description = "Initiates OAuth2 authorization flow with Google Calendar. Redirects user to Google's authorization page.")
    @GetMapping("/authorize")
    public ResponseEntity<ApiResponse<String>> authorizeGoogleCalendar() {
        log.info("GET /schedule/oauth/authorize called");
        return oAuthService.oAuthAuthorize();
    }

    /**
     * OAuth2 callback endpoint that Google calls after user authorization.
     * This endpoint processes the authorization code and stores the tokens.
     * NO AUTHENTICATION REQUIRED - Google calls this directly.
     */
    @Operation(summary = "OAuth callback", description = "OAuth2 callback endpoint that Google calls after user authorization. Processes the authorization code and stores tokens.")
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<String>> oauthCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        log.info("GET /schedule/oauth/callback called with authorization code for user: {}", state);
        return oAuthService.callback(code, state);
    }

    /**
     * Checks if the current user has Google Calendar integration.
     * Returns true if user has valid access tokens, false otherwise.
     */
    @RequireUserOrVerifiedLawyer
    @Operation(summary = "Check Google Calendar status", description = "Checks if the current authenticated user has Google Calendar integration and valid access tokens.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkGoogleCalendarStatus() {
        log.info("GET /schedule/oauth/status called");
        
        boolean hasValidTokens = oAuthService.checkAndRefreshAccessToken();
        
        if (hasValidTokens) {
            return ApiResponse.success(true, org.springframework.http.HttpStatus.OK, "User has valid Google Calendar integration");
        } else {
            return ApiResponse.success(false, org.springframework.http.HttpStatus.OK, "User does not have Google Calendar integration or tokens are invalid");
        }
    }
} 