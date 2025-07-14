package com.javajedis.legalconnect.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.auth.dto.AuthResponseDTO;
import com.javajedis.legalconnect.auth.dto.EmailVerifyDTO;
import com.javajedis.legalconnect.auth.dto.LoginDTO;
import com.javajedis.legalconnect.auth.dto.RequestOTPDTO;
import com.javajedis.legalconnect.auth.dto.ResetPasswordDTO;
import com.javajedis.legalconnect.auth.dto.UserRegisterDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "1. Auth", description = "Authentication endpoints for user registration and login")
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Constructor for AuthController.
     *
     * @param authService the authentication service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param userData the user data for registration
     * @return the response entity containing the auth response (token + user details) or an error status
     */
    @Operation(summary = "Register a new user", tags = {"1. Auth"})
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody UserRegisterDTO userData) {
        log.info("POST /auth/register called for email: {}", userData.getEmail());
        return authService.registerUser(userData);
    }

    /**
     * Authenticates a user.
     *
     * @param loginData the login credentials
     * @return the response entity containing the auth response (token + user details) or an error status
     */
    @Operation(summary = "Login user", tags = {"1. Auth"})
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginDTO loginData) {
        log.info("POST /auth/login called for email: {}", loginData.getEmail());
        return authService.loginUser(loginData.getEmail(), loginData.getPassword());
    }

    /**
     * Sends a verification code (OTP) to the user's email for verification.
     *
     * @param email the email address to send the verification code to
     * @return the response entity containing a success message or error status
     */
    @Operation(summary = "Send verification code (OTP)", description = "Sends a verification code to the user's email for verification.", tags = {"1. Auth"})
    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(@Valid @RequestBody RequestOTPDTO email) {
        log.info("POST /auth/send-verification-code called for email: {}", email.getEmail());
        return authService.sendVerificationCode(email.getEmail());
    }

    /**
     * Verifies the user's email using the provided OTP code.
     *
     * @param verificationData the email and OTP code for verification
     * @return the response entity indicating whether the email was successfully verified
     */
    @Operation(summary = "Verify email with OTP", description = "Verifies the user's email address using the provided OTP code.", tags = {"1. Auth"})
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(@Valid @RequestBody EmailVerifyDTO verificationData) {
        log.info("POST /auth/verify-email called for email: {}", verificationData.getEmail());
        return authService.verifyEmail(verificationData);
    }

    /**
     * Resets the user's password using OTP verification.
     *
     * @param data the reset password data containing email, OTP, and new password
     * @return the response entity indicating whether the password was successfully reset
     */
    @Operation(summary = "Reset password with OTP", description = "Resets the user's password using OTP verification.", tags = {"1. Auth"})
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@Valid @RequestBody ResetPasswordDTO data) {
        log.info("POST /auth/reset-password called for email: {}", data.getEmail());
        return authService.resetPassword(data);
    }

}