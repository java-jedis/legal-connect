package com.javajedis.legalconnect.auth;

import com.javajedis.legalconnect.auth.dto.*;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "0. Auth", description = "Authentication endpoints for user registration and login")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Registers a new user.
     */
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody UserRegisterDTO userData) {
        log.info("POST /auth/register called for email: {}", userData.getEmail());
        return authService.registerUser(userData);
    }

    /**
     * Authenticates a user.
     */
    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginDTO loginData) {
        log.info("POST /auth/login called for email: {}", loginData.getEmail());
        return authService.loginUser(loginData.getEmail(), loginData.getPassword());
    }

    /**
     * Sends a verification code (OTP) to the user's email for verification.
     */
    @Operation(summary = "Send verification code (OTP)", description = "Sends a verification code to the user's email for verification.")
    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(@Valid @RequestBody RequestOTPDTO email) {
        log.info("POST /auth/send-verification-code called for email: {}", email.getEmail());
        return authService.sendVerificationCode(email.getEmail());
    }

    /**
     * Verifies the user's email using the provided OTP code.
     */
    @Operation(summary = "Verify email with OTP", description = "Verifies the user's email address using the provided OTP code.")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(@Valid @RequestBody EmailVerifyDTO verificationData) {
        log.info("POST /auth/verify-email called for email: {}", verificationData.getEmail());
        return authService.verifyEmail(verificationData);
    }

    /**
     * Resets the user's password using OTP verification.
     */
    @Operation(summary = "Reset password with OTP", description = "Resets the user's password using OTP verification.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@Valid @RequestBody ResetPasswordDTO data) {
        log.info("POST /auth/reset-password called for email: {}", data.getEmail());
        return authService.resetPassword(data);
    }

}