package com.javajedis.legalconnect.common.utility;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

/**
 * Utility class for extracting user information from JWT tokens or authentication context.
 */
public class GetUserUtil {
    // Private constructor to prevent instantiation
    private GetUserUtil() {
    }

    /**
     * Extracts user information from a JWT token using JWTUtil.
     *
     * @param token   the JWT token
     * @param jwtUtil the JWT utility
     * @return a map containing user info (excluding password)
     */
    public static Map<String, Object> getUserInfoFromJwt(String token, JWTUtil jwtUtil) {
        // This already excludes password, as JWT does not contain it
        return jwtUtil.extractAllUserInfo(token);
    }

    /**
     * Extracts the current authenticated user's information from the security context and DB (excluding password).
     *
     * @param userRepo the user repository
     * @return a map containing user info (excluding password), or null if not authenticated
     */
    public static Map<String, Object> getCurrentUserInfo(UserRepo userRepo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new HashMap<>();
        }
        String email = authentication.getName();
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return new HashMap<>();
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("emailVerified", user.isEmailVerified());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        return userInfo;
    }

    /**
     * Gets the currently authenticated user entity from the security context.
     *
     * @param userRepo the user repository
     * @return the authenticated user entity or null if not authenticated
     */
    public static User getAuthenticatedUser(UserRepo userRepo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepo.findByEmail(email).orElse(null);
    }
}
