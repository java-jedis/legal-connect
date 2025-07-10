package com.javajedis.legalconnect.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.auth.dto.AuthResponseDTO;
import com.javajedis.legalconnect.auth.dto.EmailVerifyDTO;
import com.javajedis.legalconnect.auth.dto.LoginDTO;
import com.javajedis.legalconnect.auth.dto.RequestOTPDTO;
import com.javajedis.legalconnect.auth.dto.ResetPasswordDTO;
import com.javajedis.legalconnect.auth.dto.UserRegisterDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.JWTFilter;
import com.javajedis.legalconnect.user.Role;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {EmailVerificationFilter.class, JWTFilter.class}))
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegisterDTO userRegisterDTO;
    private AuthResponseDTO authResponseDTO;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return mock(AuthService.class);
        }
    }

    @BeforeEach
    void setUp() {
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("test@test.com");
        userRegisterDTO.setPassword("Password123!");
        userRegisterDTO.setFirstName("Test");
        userRegisterDTO.setLastName("User");
        userRegisterDTO.setRole(Role.USER);

        authResponseDTO = new AuthResponseDTO(
                "test_token",
                UUID.randomUUID(),
                "Test",
                "User",
                "test@test.com",
                Role.USER,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("Should register user and return token")
    void registerUser() throws Exception {
        ResponseEntity<ApiResponse<AuthResponseDTO>> responseEntity = ApiResponse.success(authResponseDTO, HttpStatus.CREATED, "User Registered Successfully");
        when(authService.registerUser(any(UserRegisterDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").value("test_token"));
    }

    @Test
    @DisplayName("Should login user and return token")
    void loginUser() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("Password123!");

        ResponseEntity<ApiResponse<AuthResponseDTO>> responseEntity = ApiResponse.success(authResponseDTO, HttpStatus.OK, "User login successful");
        when(authService.loginUser(loginDTO.getEmail(), loginDTO.getPassword())).thenReturn(responseEntity);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test_token"));
    }

    @Test
    @DisplayName("Should send verification code")
    void sendVerificationCode() throws Exception {
        RequestOTPDTO requestOTPDTO = new RequestOTPDTO();
        requestOTPDTO.setEmail("test@test.com");

        ResponseEntity<ApiResponse<String>> responseEntity = ApiResponse.success("Verification code sent to your email", HttpStatus.OK, "Verification code sent successfully");
        when(authService.sendVerificationCode(requestOTPDTO.getEmail())).thenReturn(responseEntity);

        mockMvc.perform(post("/auth/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestOTPDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code sent successfully"));
    }

    @Test
    @DisplayName("Should verify email")
    void verifyEmail() throws Exception {
        EmailVerifyDTO emailVerifyDTO = new EmailVerifyDTO();
        emailVerifyDTO.setEmail("test@test.com");
        emailVerifyDTO.setOtp("123456");

        ResponseEntity<ApiResponse<Boolean>> responseEntity = ApiResponse.success(true, HttpStatus.OK, "Email verified successfully");
        when(authService.verifyEmail(any(EmailVerifyDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailVerifyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Should reset password")
    void resetPassword() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setEmail("test@test.com");
        resetPasswordDTO.setOtp("123456");
        resetPasswordDTO.setPassword("NewPassword123!");

        ResponseEntity<ApiResponse<Boolean>> responseEntity = ApiResponse.success(true, HttpStatus.OK, "Password reset successful");
        when(authService.resetPassword(any(ResetPasswordDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }
}
