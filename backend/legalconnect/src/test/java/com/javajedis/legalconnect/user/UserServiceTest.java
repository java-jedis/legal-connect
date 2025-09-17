package com.javajedis.legalconnect.user;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.CloudinaryService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.common.utility.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private CloudinaryService cloudinaryService;
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
        userInfo.put("profilePictureUrl", "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_123.jpg");
        userInfo.put("profilePictureThumbnailUrl", "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_123.jpg");
        userInfo.put("profilePicturePublicId", "profile_pictures/user_123");
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
        when(authentication.getCredentials()).thenReturn("jwt.token");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        when(jwtUtil.extractExpiration(jwt)).thenReturn(expiration);
        ResponseEntity<ApiResponse<String>> result = userService.logout();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(redisTemplate.opsForValue(), atLeastOnce()).set(startsWith("blacklist:jwt:"), eq("true"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void logout_notAuthenticated_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResponseEntity<ApiResponse<String>> result = userService.logout();
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void logout_nullCredentials_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getCredentials()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResponseEntity<ApiResponse<String>> result = userService.logout();
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void changePassword_success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("oldHash");

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(user);

            ChangePasswordReqDTO req = new ChangePasswordReqDTO();
            req.setOldPassword("old");
            req.setPassword("new");
            when(passwordEncoder.matches("old", "oldHash")).thenReturn(true);
            when(passwordEncoder.encode("new")).thenReturn("newHash");

            ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertTrue(result.getBody().getData());
            verify(userRepo).save(user);
        }
    }

    @Test
    void changePassword_wrongOldPassword_returnsError() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("oldHash");

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(user);

            ChangePasswordReqDTO req = new ChangePasswordReqDTO();
            req.setOldPassword("wrong");
            req.setPassword("new");
            when(passwordEncoder.matches("wrong", "oldHash")).thenReturn(false);

            ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody().getError());
        }
    }

    @Test
    void changePassword_sameOldAndNewPassword_returnsError() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("oldHash");

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(user);

            ChangePasswordReqDTO req = new ChangePasswordReqDTO();
            req.setOldPassword("same");
            req.setPassword("same");
            when(passwordEncoder.matches("same", "oldHash")).thenReturn(true);

            ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody().getError());
        }
    }

    @Test
    void changePassword_notAuthenticated_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void changePassword_invalidToken_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(null);

            ChangePasswordReqDTO req = new ChangePasswordReqDTO();
            ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        }
    }

    @Test
    void getUserInfo_authenticated_userNotFound_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getCurrentUserInfo(any())).thenReturn(new HashMap<>());
            ResponseEntity<ApiResponse<UserInfoResponseDTO>> result = userService.getUserInfo();
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        }
    }

    @Test
    void logout_authenticated_tokenExpired_doesNotBlacklist() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getCredentials()).thenReturn("jwt.token");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "jwt.token";
        Date expiration = new Date(System.currentTimeMillis() - 10000); // already expired
        when(jwtUtil.extractExpiration(jwt)).thenReturn(expiration);
        ResponseEntity<ApiResponse<String>> result = userService.logout();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        // No call to valueOperations.set should be made
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void changePassword_userNotFound_returnsUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("notfound@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(null);

            ChangePasswordReqDTO req = new ChangePasswordReqDTO();
            req.setOldPassword("old");
            req.setPassword("new");

            ResponseEntity<ApiResponse<Boolean>> result = userService.changePassword(req);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        }
    }

    @Test
    void uploadProfilePicture_success() throws IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setProfilePicturePublicId(null); // No existing profile picture

        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO(
                "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_" + userId + ".jpg",
                "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_" + userId + ".jpg",
                "profile_pictures/user_" + userId
        );

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(user);
            when(cloudinaryService.uploadProfilePicture(file, userId.toString())).thenReturn(profilePictureDTO);

            // Act
            ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userService.uploadProfilePicture(file);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(profilePictureDTO, result.getBody().getData());
            assertEquals("Profile picture uploaded successfully", result.getBody().getMessage());

            // Verify user entity is updated
            assertEquals(profilePictureDTO.getFullPictureUrl(), user.getProfilePictureUrl());
            assertEquals(profilePictureDTO.getThumbnailPictureUrl(), user.getProfilePictureThumbnailUrl());
            assertEquals(profilePictureDTO.getPublicId(), user.getProfilePicturePublicId());
            verify(userRepo).save(user);
        }
    }

    @Test
    void uploadProfilePicture_replacesExistingPicture() throws IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setProfilePicturePublicId("old_profile_pictures/user_old"); // Existing profile picture

        MultipartFile file = new MockMultipartFile("file", "new.jpg", "image/jpeg", "new image content".getBytes());
        ProfilePictureDTO newProfilePictureDTO = new ProfilePictureDTO(
                "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_" + userId + ".jpg",
                "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_" + userId + ".jpg",
                "profile_pictures/user_" + userId
        );

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(user);
            when(cloudinaryService.uploadProfilePicture(file, userId.toString())).thenReturn(newProfilePictureDTO);

            // Act
            ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userService.uploadProfilePicture(file);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(newProfilePictureDTO, result.getBody().getData());

            // Verify old picture is deleted and new one is saved
            verify(cloudinaryService).deleteProfilePicture("old_profile_pictures/user_old");
            verify(cloudinaryService).uploadProfilePicture(file, userId.toString());
            verify(userRepo).save(user);
        }
    }

    @Test
    void uploadProfilePicture_notAuthenticated_returnsUnauthorized() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());

        // Act
        ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userService.uploadProfilePicture(file);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void uploadProfilePicture_userNotFound_returnsUnauthorized() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("notfound@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userService.uploadProfilePicture(file);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertNotNull(result.getBody().getError());
        }
    }

    @Test
    void uploadProfilePicture_cloudinaryError_returnsError() throws IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");

        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(user);
            when(cloudinaryService.uploadProfilePicture(file, userId.toString())).thenThrow(new IOException("Cloudinary error"));

            // Act
            ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userService.uploadProfilePicture(file);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
            assertNotNull(result.getBody());
            assertNotNull(result.getBody().getError());
            assertEquals("Failed to upload profile picture", result.getBody().getError().getMessage());
        }
    }

    @Test
    void uploadProfilePicture_nullEmail_returnsUnauthorized() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());

        try (org.mockito.MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(any())).thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userService.uploadProfilePicture(file);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertNotNull(result.getBody().getError());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }
} 