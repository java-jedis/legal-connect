package com.javajedis.legalconnect.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.exception.VerificationCodeException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating, storing, and validating verification codes (OTPs).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private static final String VERIFICATION_PREFIX = "verification:";
    private static final String EMAIL_VERIFICATION_TYPE = "email:";
    private static final String REDIS_KEY_FORMAT = "%s%s%s:%s";
    private static final int OTP_EXPIRATION = 5;
    private static final Random random = new SecureRandom();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    /**
     * Generate a 6-digit OTP code.
     */
    public String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.format("%06d", otp);
    }

    /**
     * Generate and store OTP for a user and email.
     */
    public String generateAndStoreOTP(UUID userId, String email) {
        String otp = generateOTP();
        storeVerificationCode(userId, email, otp);
        return otp;
    }

    /**
     * Store verification code in Redis.
     */
    public void storeVerificationCode(UUID userId, String email, String code) {
        String key = String.format(REDIS_KEY_FORMAT,
                VERIFICATION_PREFIX,
                EMAIL_VERIFICATION_TYPE,
                userId.toString(),
                email);

        VerificationCodeData codeData = new VerificationCodeData(
                code,
                System.currentTimeMillis(),
                email);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(codeData),
                    OTP_EXPIRATION,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.error("Error storing verification code for user {}: {}", userId, e.getMessage());
            throw new VerificationCodeException("Failed to store verification code", e);
        }
    }

    /**
     * Get verification code data from Redis.
     */
    public VerificationCodeData getVerificationCode(UUID userId, String email) {
        String key = String.format(REDIS_KEY_FORMAT,
                VERIFICATION_PREFIX,
                EMAIL_VERIFICATION_TYPE,
                userId.toString(),
                email);

        String data = redisTemplate.opsForValue().get(key);
        if (data == null) return null;

        try {
            return objectMapper.readValue(data, VerificationCodeData.class);
        } catch (JsonProcessingException e) {
            log.error("Error reading verification code for user {}: {}", userId, e.getMessage());
            throw new VerificationCodeException("Failed to read verification code", e);
        }
    }

    /**
     * Validate if the code is correct for the user and email.
     */
    public boolean isVerificationCodeValid(UUID userId, String email, String code) {
        VerificationCodeData codeData = getVerificationCode(userId, email);
        if (codeData == null) {
            return false;
        }
        return codeData.getCode().equals(code);
    }

    /**
     * Remove verification code from Redis.
     */
    public void removeVerificationCode(UUID userId, String email) {
        String key = String.format(REDIS_KEY_FORMAT,
                VERIFICATION_PREFIX,
                EMAIL_VERIFICATION_TYPE,
                userId.toString(),
                email);
        redisTemplate.delete(key);
    }

    /**
     * Data class for verification code info.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class VerificationCodeData {
        private String code;
        private long generatedAt;
        private String email;
    }
}
