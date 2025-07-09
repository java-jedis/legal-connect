package com.javajedis.legalconnect.user;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.common.utility.JWTUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String INVALID_TOKEN_MSG = "Invalid token";
    private static final String OLD_PASSWORD_INCORRECT_MSG = "Old password is incorrect";
    private static final String OLD_NEW_PASSWORD_SAME_MSG = "Old and new password cannot be the same";
    private final UserRepo userRepo;
    private final StringRedisTemplate redisTemplate;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo,
                       StringRedisTemplate redisTemplate,
                       JWTUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves the current authenticated user's information.
     *
     * @return ResponseEntity containing the user's information or error status
     */
    public ResponseEntity<ApiResponse<UserInfoResponseDTO>> getUserInfo() {
        log.debug("Attempting to retrieve current user info");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User is not authenticated when requesting user info");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }
        Map<String, Object> userInfo = GetUserUtil.getCurrentUserInfo(userRepo);
        if (userInfo.isEmpty()) {
            log.error("User information not found in the context");
            return ApiResponse.error(USER_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }
        log.info("User info retrieved for email: {}", userInfo.get("email"));

        UserInfoResponseDTO info = new UserInfoResponseDTO();
        info.setFirstName((String) userInfo.get("firstName"));
        info.setLastName((String) userInfo.get("lastName"));
        info.setEmail((String) userInfo.get("email"));
        info.setRole((Role) userInfo.get("role"));
        info.setEmailVerified((Boolean) userInfo.get("emailVerified"));
        info.setCreatedAt((OffsetDateTime) userInfo.get("createdAt"));
        info.setUpdatedAt((OffsetDateTime) userInfo.get("updatedAt"));

        return ApiResponse.success(info, HttpStatus.OK, "User info retrieved successfully");
    }

    /**
     * Logs out the current user by blacklisting the JWT token in Redis.
     *
     * @return ResponseEntity with logout status
     */
    public ResponseEntity<ApiResponse<String>> logout() {
        log.debug("Attempting to logout current user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User is not authenticated when attempting logout");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }
        Object credentials = authentication.getCredentials();
        if (credentials == null) {
            log.error("JWT token not found in authentication context during logout");
            return ApiResponse.error("JWT token not found in authentication context", HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = credentials.toString();
        
        // Blacklist the JWT token for the remaining validity period
        Date expiration = jwtUtil.extractExpiration(jwtToken);
        long now = System.currentTimeMillis();
        long exp = expiration.getTime();
        long ttl = Math.max(0, exp - now);
        if (ttl > 0) {
            String blacklistKey = "blacklist:jwt:" + jwtToken;
            redisTemplate.opsForValue().set(blacklistKey, "true", ttl, TimeUnit.MILLISECONDS);
        }
        log.info("User logged out successfully. JWT token blacklisted.");
        return ApiResponse.success("Logout successful", HttpStatus.OK, "Logout successful");
    }

    /**
     * Changes the current user's password.
     *
     * @param data the change password data containing old and new password
     * @return ResponseEntity indicating whether the password was successfully changed
     */
    public ResponseEntity<ApiResponse<Boolean>> changePassword(ChangePasswordReqDTO data) {
        log.debug("Attempting to change password for current user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User is not authenticated when attempting to change password");
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        if (email == null) {
            log.error("Email not found in authentication context during password change");
            return ApiResponse.error(INVALID_TOKEN_MSG, HttpStatus.UNAUTHORIZED);
        }
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {} during password change", email);
                    return new UsernameNotFoundException(USER_NOT_FOUND_MSG);
                });
        if (!passwordEncoder.matches(data.getOldPassword(), user.getPassword())) {
            log.warn("Incorrect old password for email: {}", email);
            return ApiResponse.error(OLD_PASSWORD_INCORRECT_MSG, HttpStatus.BAD_REQUEST);
        }
        if (data.getOldPassword().equals(data.getPassword())) {
            log.warn("Old and new password are the same for email: {}", email);
            return ApiResponse.error(OLD_NEW_PASSWORD_SAME_MSG, HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        userRepo.save(user);
        log.info("Password changed successfully for email: {}", email);
        return ApiResponse.success(true, HttpStatus.OK, "Password changed successfully");
    }

}
