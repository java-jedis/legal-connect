package com.javajedis.legalconnect.auth;

import com.javajedis.legalconnect.auth.dto.AuthResponseDTO;
import com.javajedis.legalconnect.auth.dto.EmailVerifyDTO;
import com.javajedis.legalconnect.auth.dto.ResetPasswordDTO;
import com.javajedis.legalconnect.auth.dto.UserRegisterDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.service.VerificationCodeService;
import com.javajedis.legalconnect.common.utility.JWTUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for handling user authentication and registration operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String USER_NOT_FOUND_LOG_MSG = "User not found for email: {}";
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;

    /**
     * Registers a new user with the provided data and returns JWT token with user details.
     */
    public ResponseEntity<ApiResponse<AuthResponseDTO>> registerUser(UserRegisterDTO userData) {
        log.debug("Attempting to register user with email: {}", userData.getEmail());
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
     */
    public ResponseEntity<ApiResponse<AuthResponseDTO>> loginUser(String email, String password) {
        log.debug("Attempting login for email: {}", email);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (Exception ex) {
            log.error("Authentication failed for email: {}", email, ex);
            return ApiResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG_MSG, email);
                    return new UsernameNotFoundException(USER_NOT_FOUND_MSG);
                });

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

    /**
     * Sends a verification code (OTP) to the user's email for verification.
     */
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(String email) {
        log.debug("Sending verification code to email: {}", email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG_MSG, email);
                    return new UsernameNotFoundException(USER_NOT_FOUND_MSG);
                });

        String otp = verificationCodeService.generateAndStoreOTP(user.getId(), user.getEmail());
        Map<String, Object> variables = Map.of(
                "otp", otp,
                "expirationMinutes", 5
        );
        emailService.sendTemplateEmail(
                user.getEmail(),
                "Your LegalConnect Email Verification Code",
                "otp-verification",
                variables
        );
        log.info("Verification code sent to email: {}", user.getEmail());
        return ApiResponse.success("Verification code sent to your email", HttpStatus.OK, "Verification code sent successfully");
    }

    /**
     * Verifies the user's email using the provided OTP code.
     */
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(EmailVerifyDTO verificationData) {
        log.debug("Verifying email: {} with OTP", verificationData.getEmail());
        User user = userRepo.findByEmail(verificationData.getEmail())
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG_MSG, verificationData.getEmail());
                    return new UsernameNotFoundException(USER_NOT_FOUND_MSG);
                });

        if (user.isEmailVerified()) {
            log.info("Email already verified: {}", user.getEmail());
            return ApiResponse.error("Email is already verified", HttpStatus.BAD_REQUEST);
        }

        boolean valid = verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), verificationData.getOtp());
        if (!valid) {
            log.warn("Invalid or expired verification code for email: {}", user.getEmail());
            return ApiResponse.error("Invalid or expired verification code", HttpStatus.BAD_REQUEST);
        }

        user.setEmailVerified(true);
        userRepo.save(user);
        verificationCodeService.removeVerificationCode(user.getId(), user.getEmail());
        log.info("Email verified successfully: {}", user.getEmail());
        return ApiResponse.success(true, HttpStatus.OK, "Email verified successfully");
    }

    /**
     * Resets the user's password using OTP verification.
     */
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(ResetPasswordDTO data) {
        log.debug("Resetting password for email: {}", data.getEmail());
        User user = userRepo.findByEmail(data.getEmail())
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG_MSG, data.getEmail());
                    return new UsernameNotFoundException(USER_NOT_FOUND_MSG);
                });

        boolean valid = verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), data.getOtp());
        if (!valid) {
            log.warn("Invalid or expired OTP for password reset for email: {}", user.getEmail());
            return ApiResponse.error("Otp is Invalid or expired", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        userRepo.save(user);
        verificationCodeService.removeVerificationCode(user.getId(), user.getEmail());
        log.info("Password reset successful for email: {}", user.getEmail());
        return ApiResponse.success(true, HttpStatus.OK, "Password reset successful");
    }
}