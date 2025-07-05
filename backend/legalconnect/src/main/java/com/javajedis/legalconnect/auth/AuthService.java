package com.javajedis.legalconnect.auth;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.service.VerificationCodeService;
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

import java.util.Map;

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
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;

    /**
     * Constructs AuthService with required dependencies.
     */
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepo userRepo,
            JWTUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            VerificationCodeService verificationCodeService) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.verificationCodeService = verificationCodeService;
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

    /**
     * Sends a verification code (OTP) to the user's email for verification.
     *
     * @param email the email address to send the verification code to
     * @return ResponseEntity with a success message or error status
     */
    public ResponseEntity<ApiResponse<String>> sendVerificationCode(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MSG));

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
        return ApiResponse.success("Verification code sent to your email", HttpStatus.OK, "Verification code sent successfully");
    }

    /**
     * Verifies the user's email using the provided OTP code.
     *
     * @param verificationData the email and OTP code for verification
     * @return ResponseEntity indicating whether the email was successfully verified
     */
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(EmailVerifyDTO verificationData) {
        User user = userRepo.findByEmail(verificationData.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MSG));

        if (user.isEmailVerified()) {
            return ApiResponse.error("Email is already verified", HttpStatus.BAD_REQUEST);
        }

        boolean valid = verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), verificationData.getOtp());
        if (!valid) {
            return ApiResponse.error("Invalid or expired verification code", HttpStatus.BAD_REQUEST);
        }

        user.setEmailVerified(true);
        userRepo.save(user);
        verificationCodeService.removeVerificationCode(user.getId(), user.getEmail());
        return ApiResponse.success(true, HttpStatus.OK, "Email verified successfully");
    }

    /**
     * Resets the user's password using OTP verification.
     *
     * @param data the reset password data containing email, OTP, and new password
     * @return ResponseEntity indicating whether the password was successfully reset
     */
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(ResetPasswordDTO data) {
        User user = userRepo.findByEmail(data.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MSG));

        boolean valid = verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), data.getOtp());
        if (!valid) {
            return ApiResponse.error("Otp is Invalid or expired", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        userRepo.save(user);
        verificationCodeService.removeVerificationCode(user.getId(), user.getEmail());
        return ApiResponse.success(true, HttpStatus.OK, "Password reset successful");
    }
}