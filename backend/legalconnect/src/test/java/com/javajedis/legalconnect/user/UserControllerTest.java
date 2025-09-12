package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.javajedis.legalconnect.common.dto.ApiResponse;

class UserControllerTest {
    @Mock
    private UserService userService;
    
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserInfo_returnsUserInfo() {
        // Set up authentication context
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserInfoResponseDTO dto = new UserInfoResponseDTO();
        ApiResponse<UserInfoResponseDTO> apiResponse = ApiResponse.success(dto, HttpStatus.OK, "User info retrieved successfully").getBody();
        when(userService.getUserInfo()).thenReturn(ResponseEntity.ok(apiResponse));
        ResponseEntity<ApiResponse<UserInfoResponseDTO>> result = userController.getUserInfo();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(dto, result.getBody().getData());
    }

    @Test
    void logout_withValidJwt_returnsSuccess() {
        // Set up authentication context
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getCredentials()).thenReturn("valid.jwt.token");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        ApiResponse<String> apiResponse = ApiResponse.success("Logout successful", HttpStatus.OK, "Logout successful").getBody();
        when(userService.logout()).thenReturn(ResponseEntity.ok(apiResponse));
        ResponseEntity<ApiResponse<String>> result = userController.logout();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Logout successful", result.getBody().getData());
    }

    @Test
    void logout_withMissingHeader_returnsError() {
        // No authentication context set up - should return unauthorized
        ApiResponse<String> apiResponse = ApiResponse.<String>error("Not authenticated", HttpStatus.UNAUTHORIZED).getBody();
        when(userService.logout()).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));
        ResponseEntity<ApiResponse<String>> result = userController.logout();
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void logout_withInvalidHeader_returnsError() {
        // Set up authentication but with null credentials
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getCredentials()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        ApiResponse<String> apiResponse = ApiResponse.<String>error("JWT token not found in authentication context", HttpStatus.UNAUTHORIZED).getBody();
        when(userService.logout()).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));
        ResponseEntity<ApiResponse<String>> result = userController.logout();
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void changePassword_withValidJwt_returnsSuccess() {
        // Set up authentication context
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getCredentials()).thenReturn("valid.jwt.token");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ApiResponse<Boolean> apiResponse = ApiResponse.success(true, HttpStatus.OK, "Password changed successfully").getBody();
        when(userService.changePassword(req)).thenReturn(ResponseEntity.ok(apiResponse));
        ResponseEntity<ApiResponse<Boolean>> result = userController.changePassword(req);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getData());
    }

    @Test
    void changePassword_withMissingHeader_returnsError() {
        // No authentication context set up - should return unauthorized
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>error("Not authenticated", HttpStatus.UNAUTHORIZED).getBody();
        when(userService.changePassword(req)).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));
        ResponseEntity<ApiResponse<Boolean>> result = userController.changePassword(req);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void changePassword_withInvalidHeader_returnsError() {
        // Set up authentication but with null credentials
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getCredentials()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>error("JWT token not found in authentication context", HttpStatus.UNAUTHORIZED).getBody();
        when(userService.changePassword(req)).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));
        ResponseEntity<ApiResponse<Boolean>> result = userController.changePassword(req);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void uploadProfilePicture_withValidFile_returnsSuccess() {
        // Set up authentication context
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO(
            "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_123.jpg",
            "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_123.jpg",
            "profile_pictures/user_123"
        );
        
        ApiResponse<ProfilePictureDTO> apiResponse = ApiResponse.success(profilePictureDTO, HttpStatus.OK, "Profile picture uploaded successfully").getBody();
        when(userService.uploadProfilePicture(file)).thenReturn(ResponseEntity.ok(apiResponse));
        
        ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userController.uploadProfilePicture(file);
        
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(profilePictureDTO, result.getBody().getData());
        assertEquals("Profile picture uploaded successfully", result.getBody().getMessage());
    }

    @Test
    void uploadProfilePicture_withAuthenticationError_returnsUnauthorized() {
        // No authentication context set up
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        ApiResponse<ProfilePictureDTO> apiResponse = ApiResponse.<ProfilePictureDTO>error("User is not authenticated", HttpStatus.UNAUTHORIZED).getBody();
        when(userService.uploadProfilePicture(file)).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));
        
        ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userController.uploadProfilePicture(file);
        
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void uploadProfilePicture_withInvalidFile_returnsError() {
        // Set up authentication context
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "not an image".getBytes());
        
        ApiResponse<ProfilePictureDTO> apiResponse = ApiResponse.<ProfilePictureDTO>error("Failed to upload profile picture", HttpStatus.INTERNAL_SERVER_ERROR).getBody();
        when(userService.uploadProfilePicture(file)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse));
        
        ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userController.uploadProfilePicture(file);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void uploadProfilePicture_withEmptyFile_returnsError() {
        // Set up authentication context
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        MultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        
        ApiResponse<ProfilePictureDTO> apiResponse = ApiResponse.<ProfilePictureDTO>error("Failed to upload profile picture", HttpStatus.INTERNAL_SERVER_ERROR).getBody();
        when(userService.uploadProfilePicture(file)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse));
        
        ResponseEntity<ApiResponse<ProfilePictureDTO>> result = userController.uploadProfilePicture(file);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }
} 