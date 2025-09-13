package com.javajedis.legalconnect.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javajedis.legalconnect.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "1. User", description = "User profile and account management endpoints for all types of users")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the current user's information.
     */
    @Operation(summary = "Get user info", description = "Retrieves the current authenticated user's information.")
    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<UserInfoResponseDTO>> getUserInfo() {
        log.info("GET /user/user-info called");
        return userService.getUserInfo();
    }

    /**
     * Logs out the current user by blacklisting the JWT token.
     */
    @Operation(summary = "Logout user", description = "Logs out the current user by blacklisting the JWT token.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("POST /user/logout called");
        return userService.logout();
    }

    /**
     * Changes the current user's password.
     */
    @Operation(summary = "Change password", description = "Changes the current user's password.")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@Valid @RequestBody ChangePasswordReqDTO data) {
        log.info("PUT /user/change-password called for email: [from context]");
        return userService.changePassword(data);
    }

    /**
     * Uploads a profile picture for the current user.
     */
    @Operation(summary = "Upload profile picture", description = "Uploads a profile picture for the current authenticated user.")
    @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfilePictureDTO>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        log.info("POST /user/profile-picture called");
        return userService.uploadProfilePicture(file);
    }


}
