package com.javajedis.legalconnect.auth;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return authService.loginUser(loginData.getEmail(), loginData.getPassword());
    }
}