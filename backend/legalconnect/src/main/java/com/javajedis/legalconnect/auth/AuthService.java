package com.javajedis.legalconnect.auth;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.JWTUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling user authentication and registration operations.
 */
@Service
@Slf4j
public class AuthService {
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs AuthService with required dependencies.
     */
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepo userRepo,
            JWTUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the provided data and returns JWT token with user details.
     *
     * @param userData registration data
     * @return ResponseEntity with auth response (token + user details) or error
     */
    public ResponseEntity<ApiResponse<AuthResponseDTO>> registerUser(UserRegisterDTO userData) {
        if (userRepo.findByEmail(userData.getEmail()).isPresent()) {
            log.warn("Registration attempt with existing email: {}", userData.getEmail());
            return ApiResponse.error("User with this email already exists", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setFirstName(userData.getFirstName());
        user.setLastName(userData.getLastName());
        user.setEmail(userData.getEmail());
        user.setPassword(passwordEncoder.encode(userData.getPassword()));
        user.setRole(userData.getRole());
        user.setEmailVerified(false);

        User savedUser = userRepo.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate JWT token with enhanced claims
        String token = jwtUtil.generateToken(savedUser);

        AuthResponseDTO responseDTO = new AuthResponseDTO(
                token,
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.isEmailVerified(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );

        return ApiResponse.success(responseDTO, HttpStatus.CREATED, "User Registered Successfully");
    }

    /**
     * Authenticates user and returns JWT token with user details.
     *
     * @param email    user email
     * @param password user password
     * @return ResponseEntity with auth response (token + user details) or error
     */
    public ResponseEntity<ApiResponse<AuthResponseDTO>> loginUser(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MSG));

        // Generate JWT token with enhanced claims
        String token = jwtUtil.generateToken(user);

        AuthResponseDTO responseDTO = new AuthResponseDTO(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        log.info("User logged in successfully: {}", user.getEmail());
        return ApiResponse.success(responseDTO, HttpStatus.OK, "User login successful");
    }
}