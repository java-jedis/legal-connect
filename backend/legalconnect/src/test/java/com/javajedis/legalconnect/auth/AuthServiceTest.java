package com.javajedis.legalconnect.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.javajedis.legalconnect.auth.dto.AuthResponseDTO;
import com.javajedis.legalconnect.auth.dto.EmailVerifyDTO;
import com.javajedis.legalconnect.auth.dto.LoginDTO;
import com.javajedis.legalconnect.auth.dto.ResetPasswordDTO;
import com.javajedis.legalconnect.auth.dto.UserRegisterDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.service.VerificationCodeService;
import com.javajedis.legalconnect.common.utility.JWTUtil;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepo userRepo;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private NotificationPreferenceService notificationPreferenceService;

    @InjectMocks
    private AuthService authService;

    private UserRegisterDTO userRegisterDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("test@test.com");
        userRegisterDTO.setPassword("Password123!");
        userRegisterDTO.setFirstName("Test");
        userRegisterDTO.setLastName("User");
        userRegisterDTO.setRole(Role.USER);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setEmailVerified(false);
    }

    @Test
    @DisplayName("Should register user successfully")
    void registerUser_Success() {
        when(userRepo.findByEmail(userRegisterDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRegisterDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("test_token");

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authService.registerUser(userRegisterDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User Registered Successfully", response.getBody().getMessage());
        assertEquals("test_token", response.getBody().getData().getToken());

        verify(notificationPreferenceService, times(1))
                .initializeDefaultPreferencesForUser(user.getId());
    }

    @Test
    @DisplayName("Should return conflict when user already exists")
    void registerUser_Conflict() {
        when(userRepo.findByEmail(userRegisterDTO.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authService.registerUser(userRegisterDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with this email already exists", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should login user successfully")
    void loginUser_Success() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("password");

        when(userRepo.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("test_token");

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authService.loginUser(loginDTO.getEmail(), loginDTO.getPassword());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User login successful", response.getBody().getMessage());
        assertEquals("test_token", response.getBody().getData().getToken());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found on login")
    void loginUser_UserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("password");

        when(userRepo.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.loginUser(email, password);
        });
    }

    @Test
    @DisplayName("Should send verification code successfully")
    void sendVerificationCode_Success() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(verificationCodeService.generateAndStoreOTP(user.getId(), user.getEmail())).thenReturn("123456");

        ResponseEntity<ApiResponse<String>> response = authService.sendVerificationCode("test@test.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Verification code sent successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found on send verification code")
    void sendVerificationCode_UserNotFound() {
        String email = "test@test.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            authService.sendVerificationCode(email);
        });
    }

    @Test
    @DisplayName("Should verify email successfully")
    void verifyEmail_Success() {
        EmailVerifyDTO emailVerifyDTO = new EmailVerifyDTO();
        emailVerifyDTO.setEmail("test@test.com");
        emailVerifyDTO.setOtp("123456");

        when(userRepo.findByEmail(emailVerifyDTO.getEmail())).thenReturn(Optional.of(user));
        when(verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), emailVerifyDTO.getOtp())).thenReturn(true);

        ResponseEntity<ApiResponse<Boolean>> response = authService.verifyEmail(emailVerifyDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData());
        assertEquals("Email verified successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should return bad request when email already verified")
    void verifyEmail_AlreadyVerified() {
        user.setEmailVerified(true);
        EmailVerifyDTO emailVerifyDTO = new EmailVerifyDTO();
        emailVerifyDTO.setEmail("test@test.com");
        emailVerifyDTO.setOtp("123456");

        when(userRepo.findByEmail(emailVerifyDTO.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<ApiResponse<Boolean>> response = authService.verifyEmail(emailVerifyDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Email is already verified", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should return bad request for invalid OTP")
    void verifyEmail_InvalidOTP() {
        EmailVerifyDTO emailVerifyDTO = new EmailVerifyDTO();
        emailVerifyDTO.setEmail("test@test.com");
        emailVerifyDTO.setOtp("123456");

        when(userRepo.findByEmail(emailVerifyDTO.getEmail())).thenReturn(Optional.of(user));
        when(verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), emailVerifyDTO.getOtp())).thenReturn(false);

        ResponseEntity<ApiResponse<Boolean>> response = authService.verifyEmail(emailVerifyDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid or expired verification code", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should reset password successfully")
    void resetPassword_Success() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setEmail("test@test.com");
        resetPasswordDTO.setOtp("123456");
        resetPasswordDTO.setPassword("newPassword");

        when(userRepo.findByEmail(resetPasswordDTO.getEmail())).thenReturn(Optional.of(user));
        when(verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), resetPasswordDTO.getOtp())).thenReturn(true);
        when(passwordEncoder.encode(resetPasswordDTO.getPassword())).thenReturn("newEncodedPassword");

        ResponseEntity<ApiResponse<Boolean>> response = authService.resetPassword(resetPasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData());
        assertEquals("Password reset successful", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should return bad request for invalid OTP on reset password")
    void resetPassword_InvalidOTP() {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setEmail("test@test.com");
        resetPasswordDTO.setOtp("123456");
        resetPasswordDTO.setPassword("newPassword");

        when(userRepo.findByEmail(resetPasswordDTO.getEmail())).thenReturn(Optional.of(user));
        when(verificationCodeService.isVerificationCodeValid(user.getId(), user.getEmail(), resetPasswordDTO.getOtp())).thenReturn(false);

        ResponseEntity<ApiResponse<Boolean>> response = authService.resetPassword(resetPasswordDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Otp is Invalid or expired", response.getBody().getError().getMessage());
    }
}
