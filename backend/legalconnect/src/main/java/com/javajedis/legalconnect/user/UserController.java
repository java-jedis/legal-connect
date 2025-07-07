package com.javajedis.legalconnect.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "2. User", description = "User profile and account management endpoints for all types of users")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final String MISSING_OR_INVALID_AUTH_HEADER = "Missing or invalid Authorization header";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private static String extractJwtFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * Retrieves the current user's information.
     *
     * @return the response entity containing the user's information
     */
    @Operation(summary = "Get user info", description = "Retrieves the current authenticated user's information.")
    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<UserInfoResponseDTO>> getUserInfo() {
        return userService.getUserInfo();
    }

    /**
     * Logs out the current user by blacklisting the JWT token.
     *
     * @param authorizationHeader the Authorization header containing the JWT
     * @return the response entity with logout status
     */
    @Operation(summary = "Logout user", description = "Logs out the current user by blacklisting the JWT token.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        if (jwt == null) {
            return ApiResponse.error(MISSING_OR_INVALID_AUTH_HEADER, HttpStatus.BAD_REQUEST);
        }
        return userService.logout(jwt);
    }

    /**
     * Changes the current user's password.
     *
     * @param data                the change password data containing old and new password
     * @param authorizationHeader the Authorization header containing the JWT
     * @return the response entity indicating whether the password was successfully changed
     */
    @Operation(summary = "Change password", description = "Changes the current user's password.")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@Valid @RequestBody ChangePasswordReqDTO data,
                                                               @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = extractJwtFromHeader(authorizationHeader);
        if (jwt == null) {
            return ApiResponse.error(MISSING_OR_INVALID_AUTH_HEADER, HttpStatus.BAD_REQUEST);
        }
        return userService.changePassword(data, jwt);
    }

}
