package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.common.utility.JWTUtil;

class UserServiceTest {
    @Mock
    private UserRepo userRepo;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getUserInfo_authenticated_returnsUserInfo() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", "John");
        userInfo.put("lastName", "Doe");
        userInfo.put("email", "john@example.com");
        userInfo.put("role", Role.USER);
        userInfo.put("emailVerified", true);
        userInfo.put("createdAt", OffsetDateTime.now());
        userInfo.put("updatedAt", OffsetDateTime.now());
        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getCurrentUserInfo(any())).thenReturn(userInfo);
            ResponseEntity<ApiResponse<UserInfoResponseDTO>> result = userService.getUserInfo();
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("John", result.getBody().getData().getFirstName());
        }
    }

    @Test
    void getUserInfo_notAuthenticated_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResponseEntity<ApiResponse<UserInfoResponseDTO>> result = userService.getUserInfo();
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void logout_authenticated_blacklistsToken() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        when(jwtUtil.extractExpiration(jwt)).thenReturn(expiration);
        ResponseEntity<ApiResponse<String>> result = userService.logout(jwt);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(redisTemplate.opsForValue(), atLeastOnce()).set(startsWith("blacklist:jwt:"), eq("true"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void logout_notAuthenticated_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResponseEntity<ApiResponse<String>> result = userService.logout("jwt.token");
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void changePassword_success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        String email = "john@example.com";
        when(jwtUtil.extractUsername(jwt)).thenReturn(email);
        User user = new User();
        user.setPassword("oldHash");
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        req.setOldPassword("old");
        req.setPassword("new");
        when(passwordEncoder.matches("old", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("newHash");
        ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req, jwt);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().getData());
        verify(userRepo).save(user);
    }

    @Test
    void changePassword_wrongOldPassword_returnsError() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        String email = "john@example.com";
        when(jwtUtil.extractUsername(jwt)).thenReturn(email);
        User user = new User();
        user.setPassword("oldHash");
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        req.setOldPassword("wrong");
        req.setPassword("new");
        when(passwordEncoder.matches("wrong", "oldHash")).thenReturn(false);
        ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req, jwt);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void changePassword_sameOldAndNewPassword_returnsError() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        String email = "john@example.com";
        when(jwtUtil.extractUsername(jwt)).thenReturn(email);
        User user = new User();
        user.setPassword("oldHash");
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        req.setOldPassword("same");
        req.setPassword("same");
        when(passwordEncoder.matches("same", "oldHash")).thenReturn(true);
        ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req, jwt);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void changePassword_notAuthenticated_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req, "jwt.token");
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void changePassword_invalidToken_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(jwtUtil.extractUsername(anyString())).thenReturn(null);
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req, "jwt.token");
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void getUserInfo_authenticated_userNotFound_returnsNotFound() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getCurrentUserInfo(any())).thenReturn(new HashMap<>());
            ResponseEntity<ApiResponse<UserInfoResponseDTO>> result = userService.getUserInfo();
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }
    }

    @Test
    void logout_authenticated_tokenExpired_doesNotBlacklist() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        Date expiration = new Date(System.currentTimeMillis() - 10000); // already expired
        when(jwtUtil.extractExpiration(jwt)).thenReturn(expiration);
        ResponseEntity<ApiResponse<String>> result = userService.logout(jwt);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        // No call to valueOperations.set should be made
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void changePassword_userNotFound_throwsException() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        String email = "notfound@example.com";
        when(jwtUtil.extractUsername(jwt)).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        req.setOldPassword("old");
        req.setPassword("new");
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> {
            userService.changePassword(req, jwt);
        });
    }
} 