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

import com.javajedis.legalconnect.common.dto.ApiResponse;

class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserInfo_returnsUserInfo() {
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
        String jwt = "valid.jwt.token";
        ApiResponse<String> apiResponse = ApiResponse.success("Logout successful", HttpStatus.OK, "Logout successful").getBody();
        when(userService.logout(jwt)).thenReturn(ResponseEntity.ok(apiResponse));
        String header = "Bearer " + jwt;
        ResponseEntity<ApiResponse<String>> result = userController.logout(header);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Logout successful", result.getBody().getData());
    }

    @Test
    void logout_withMissingHeader_returnsError() {
        ResponseEntity<ApiResponse<String>> result = userController.logout(null);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void logout_withInvalidHeader_returnsError() {
        ResponseEntity<ApiResponse<String>> result = userController.logout("InvalidHeader");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void changePassword_withValidJwt_returnsSuccess() {
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        String jwt = "valid.jwt.token";
        ApiResponse<Boolean> apiResponse = ApiResponse.success(true, HttpStatus.OK, "Password changed successfully").getBody();
        when(userService.changePassword(req, jwt)).thenReturn(ResponseEntity.ok(apiResponse));
        String header = "Bearer " + jwt;
        ResponseEntity<ApiResponse<Boolean>> result = userController.changePassword(req, header);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getData());
    }

    @Test
    void changePassword_withMissingHeader_returnsError() {
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ResponseEntity<ApiResponse<Boolean>> result = userController.changePassword(req, null);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }

    @Test
    void changePassword_withInvalidHeader_returnsError() {
        ChangePasswordReqDTO req = new ChangePasswordReqDTO();
        ResponseEntity<ApiResponse<Boolean>> result = userController.changePassword(req, "InvalidHeader");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNull(result.getBody().getData());
        assertNotNull(result.getBody().getError());
    }
} 