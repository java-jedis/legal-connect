package com.javajedis.legalconnect.common.utility;

import com.javajedis.legalconnect.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {
    // Constants for JWT claims
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_FIRST_NAME = "firstName";
    public static final String CLAIM_LAST_NAME = "lastName";
    public static final String CLAIM_EMAIL_VERIFIED = "emailVerified";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    @Value("${spring.custom.security.jwtsecret}")
    private String secretKey;

    @Value("${spring.application.name:LegalConnect}")
    private String issuer;

    /**
     * Retrieves the signing key used for JWT operations.
     *
     * @return the secret key used for signing JWTs
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Extracts the username from the provided JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * Extracts the expiration date from the provided JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if the provided JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a new JWT token for the provided username (legacy method).
     *
     * @param username the username for which the token is generated
     * @return the generated JWT token
     * @deprecated Use generateToken(User user) instead
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Generates a new JWT token with enhanced claims for the provided user.
     *
     * @param user the user entity
     * @return the generated JWT token
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getId().toString());
        claims.put("role", user.getRole().name());
        claims.put(CLAIM_FIRST_NAME, user.getFirstName());
        claims.put(CLAIM_LAST_NAME, user.getLastName());
        claims.put(CLAIM_EMAIL_VERIFIED, user.isEmailVerified());
        claims.put(CLAIM_TOKEN_TYPE, "ACCESS");

        return createToken(claims, user.getEmail());
    }

    /**
     * Creates a new JWT token with the provided claims and subject.
     *
     * @param claims  the claims to be included in the token
     * @param subject the subject of the token (email)
     * @return the created JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .audience().add("LegalConnect-API").and()
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration time
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Extracts user ID from the JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(CLAIM_USER_ID, String.class);
    }

    /**
     * Extracts user role from the JWT token.
     *
     * @param token the JWT token
     * @return the user role
     */
    public String extractUserRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extracts token type from the JWT token.
     *
     * @param token the JWT token
     * @return the token type
     */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * Extracts first name from the JWT token.
     *
     * @param token the JWT token
     * @return the first name
     */
    public String extractFirstName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(CLAIM_FIRST_NAME, String.class);
    }

    /**
     * Extracts last name from the JWT token.
     *
     * @param token the JWT token
     * @return the last name
     */
    public String extractLastName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(CLAIM_LAST_NAME, String.class);
    }

    /**
     * Extracts email verification status from the JWT token.
     *
     * @param token the JWT token
     * @return the email verification status
     */
    public Boolean extractEmailVerified(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(CLAIM_EMAIL_VERIFIED, Boolean.class);
    }

    /**
     * Extracts all user information from the JWT token.
     *
     * @param token the JWT token
     * @return map containing all user information
     */
    public Map<String, Object> extractAllUserInfo(String token) {
        Claims claims = extractAllClaims(token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(CLAIM_USER_ID, claims.get(CLAIM_USER_ID, String.class));
        userInfo.put("email", claims.getSubject());
        userInfo.put("role", claims.get("role", String.class));
        userInfo.put(CLAIM_FIRST_NAME, claims.get(CLAIM_FIRST_NAME, String.class));
        userInfo.put(CLAIM_LAST_NAME, claims.get(CLAIM_LAST_NAME, String.class));
        userInfo.put(CLAIM_EMAIL_VERIFIED, claims.get(CLAIM_EMAIL_VERIFIED, Boolean.class));
        userInfo.put(CLAIM_TOKEN_TYPE, claims.get(CLAIM_TOKEN_TYPE, String.class));
        return userInfo;
    }
}
