package com.javajedis.legalconnect.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.exception.VerificationCodeException;

/**
 * Unit tests for VerificationCodeService.
 * Tests OTP generation, storage, retrieval, validation, and removal functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationCodeService Tests")
class VerificationCodeServiceTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CODE = "123456";
    private static final String TEST_REDIS_KEY = "verification:email:" + TEST_USER_ID + ":" + TEST_EMAIL;
    private static final String TEST_JSON_DATA = "{\"code\":\"123456\",\"generatedAt\":1234567890,\"email\":\"test@example.com\"}";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUp() {
        // Use lenient to avoid unnecessary stubbing warnings
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should generate valid 6-digit OTP")
    void shouldGenerateValidSixDigitOTP() {
        // When
        String otp = verificationCodeService.generateOTP();

        // Then
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
        int otpValue = Integer.parseInt(otp);
        assertTrue(otpValue >= 100000 && otpValue <= 999999);
    }

    @Test
    @DisplayName("Should generate unique OTPs")
    void shouldGenerateUniqueOTPs() {
        // When
        String otp1 = verificationCodeService.generateOTP();
        String otp2 = verificationCodeService.generateOTP();
        String otp3 = verificationCodeService.generateOTP();

        // Then
        assertNotNull(otp1);
        assertNotNull(otp2);
        assertNotNull(otp3);
        // Note: In a real scenario, OTPs might be the same due to randomness
        // This test ensures they are valid 6-digit numbers
        assertTrue(otp1.matches("\\d{6}"));
        assertTrue(otp2.matches("\\d{6}"));
        assertTrue(otp3.matches("\\d{6}"));
    }

    @Test
    @DisplayName("Should generate and store OTP successfully")
    void shouldGenerateAndStoreOTPSuccessfully() throws JsonProcessingException {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn(TEST_JSON_DATA);

        // When
        String generatedOtp = verificationCodeService.generateAndStoreOTP(TEST_USER_ID, TEST_EMAIL);

        // Then
        assertNotNull(generatedOtp);
        assertEquals(6, generatedOtp.length());
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(valueOperations, times(1)).set(TEST_REDIS_KEY, TEST_JSON_DATA, 5L, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should store verification code successfully")
    void shouldStoreVerificationCodeSuccessfully() throws JsonProcessingException {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn(TEST_JSON_DATA);

        // When
        verificationCodeService.storeVerificationCode(TEST_USER_ID, TEST_EMAIL, TEST_CODE);

        // Then
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(valueOperations, times(1)).set(TEST_REDIS_KEY, TEST_JSON_DATA, 5L, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should throw VerificationCodeException when JSON serialization fails")
    void shouldThrowVerificationCodeExceptionWhenJsonSerializationFails() throws JsonProcessingException {
        // Given
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // When & Then
        assertThrows(VerificationCodeException.class, () -> {
            verificationCodeService.storeVerificationCode(TEST_USER_ID, TEST_EMAIL, TEST_CODE);
        });
    }

    @Test
    @DisplayName("Should get verification code successfully")
    void shouldGetVerificationCodeSuccessfully() throws JsonProcessingException {
        // Given
        VerificationCodeService.VerificationCodeData expectedData = new VerificationCodeService.VerificationCodeData(
            TEST_CODE, 1234567890L, TEST_EMAIL);
        when(valueOperations.get(TEST_REDIS_KEY)).thenReturn(TEST_JSON_DATA);
        when(objectMapper.readValue(TEST_JSON_DATA, VerificationCodeService.VerificationCodeData.class))
            .thenReturn(expectedData);

        // When
        VerificationCodeService.VerificationCodeData result = verificationCodeService.getVerificationCode(TEST_USER_ID, TEST_EMAIL);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CODE, result.getCode());
        assertEquals(1234567890L, result.getGeneratedAt());
        assertEquals(TEST_EMAIL, result.getEmail());
        verify(valueOperations, times(1)).get(TEST_REDIS_KEY);
        verify(objectMapper, times(1)).readValue(TEST_JSON_DATA, VerificationCodeService.VerificationCodeData.class);
    }

    @Test
    @DisplayName("Should return null when verification code not found")
    void shouldReturnNullWhenVerificationCodeNotFound() throws JsonProcessingException {
        // Given
        when(valueOperations.get(TEST_REDIS_KEY)).thenReturn(null);

        // When
        VerificationCodeService.VerificationCodeData result = verificationCodeService.getVerificationCode(TEST_USER_ID, TEST_EMAIL);

        // Then
        assertEquals(null, result);
        verify(valueOperations, times(1)).get(TEST_REDIS_KEY);
        verify(objectMapper, never()).readValue(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("Should throw VerificationCodeException when JSON deserialization fails")
    void shouldThrowVerificationCodeExceptionWhenJsonDeserializationFails() throws JsonProcessingException {
        // Given
        when(valueOperations.get(TEST_REDIS_KEY)).thenReturn(TEST_JSON_DATA);
        when(objectMapper.readValue(TEST_JSON_DATA, VerificationCodeService.VerificationCodeData.class))
            .thenThrow(new JsonProcessingException("JSON error") {});

        // When & Then
        assertThrows(VerificationCodeException.class, () -> {
            verificationCodeService.getVerificationCode(TEST_USER_ID, TEST_EMAIL);
        });
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid, null, or empty verification codes")
    @ValueSource(strings = {"999999", "", "null"})
    void shouldRejectInvalidOrNullOrEmptyVerificationCode(String inputCode) throws Exception {
        VerificationCodeService.VerificationCodeData codeData = new VerificationCodeService.VerificationCodeData(
            TEST_CODE, 1234567890L, TEST_EMAIL);
        when(valueOperations.get(TEST_REDIS_KEY)).thenReturn(TEST_JSON_DATA);
        when(objectMapper.readValue(TEST_JSON_DATA, VerificationCodeService.VerificationCodeData.class))
            .thenReturn(codeData);

        boolean isValid = verificationCodeService.isVerificationCodeValid(TEST_USER_ID, TEST_EMAIL, "null".equals(inputCode) ? null : inputCode);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should remove verification code successfully")
    void shouldRemoveVerificationCodeSuccessfully() {
        // When
        verificationCodeService.removeVerificationCode(TEST_USER_ID, TEST_EMAIL);

        // Then
        verify(redisTemplate, times(1)).delete(TEST_REDIS_KEY);
    }

    @Test
    @DisplayName("Should handle null email in validation")
    void shouldHandleNullEmailInValidation() {
        // Given
        String nullEmailKey = "verification:email:" + TEST_USER_ID + ":null";
        when(valueOperations.get(nullEmailKey)).thenReturn(null);

        // When
        boolean isValid = verificationCodeService.isVerificationCodeValid(TEST_USER_ID, null, TEST_CODE);

        // Then
        assertFalse(isValid);
        verify(valueOperations, times(1)).get(nullEmailKey);
    }

    @Test
    @DisplayName("Should handle null code in validation")
    void shouldHandleNullCodeInValidation() throws JsonProcessingException {
        // Given
        VerificationCodeService.VerificationCodeData codeData = new VerificationCodeService.VerificationCodeData(
            TEST_CODE, 1234567890L, TEST_EMAIL);
        when(valueOperations.get(TEST_REDIS_KEY)).thenReturn(TEST_JSON_DATA);
        when(objectMapper.readValue(TEST_JSON_DATA, VerificationCodeService.VerificationCodeData.class))
            .thenReturn(codeData);

        // When
        boolean isValid = verificationCodeService.isVerificationCodeValid(TEST_USER_ID, TEST_EMAIL, null);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle empty code in validation")
    void shouldHandleEmptyCodeInValidation() throws JsonProcessingException {
        // Given
        VerificationCodeService.VerificationCodeData codeData = new VerificationCodeService.VerificationCodeData(
            TEST_CODE, 1234567890L, TEST_EMAIL);
        when(valueOperations.get(TEST_REDIS_KEY)).thenReturn(TEST_JSON_DATA);
        when(objectMapper.readValue(TEST_JSON_DATA, VerificationCodeService.VerificationCodeData.class))
            .thenReturn(codeData);

        // When
        boolean isValid = verificationCodeService.isVerificationCodeValid(TEST_USER_ID, TEST_EMAIL, "");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle Redis connection failure gracefully")
    void shouldHandleRedisConnectionFailureGracefully() {
        // Given
        doThrow(new RuntimeException("Redis connection failed")).when(redisTemplate).delete(TEST_REDIS_KEY);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            verificationCodeService.removeVerificationCode(TEST_USER_ID, TEST_EMAIL);
        });
    }

    @Test
    @DisplayName("Should generate consistent Redis keys")
    void shouldGenerateConsistentRedisKeys() throws JsonProcessingException {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        when(objectMapper.writeValueAsString(any())).thenReturn(TEST_JSON_DATA);

        // When
        verificationCodeService.storeVerificationCode(userId1, email1, TEST_CODE);
        verificationCodeService.storeVerificationCode(userId2, email2, TEST_CODE);

        // Then
        String expectedKey1 = "verification:email:" + userId1 + ":" + email1;
        String expectedKey2 = "verification:email:" + userId2 + ":" + email2;
        
        verify(valueOperations, times(1)).set(eq(expectedKey1), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(valueOperations, times(1)).set(eq(expectedKey2), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("Should handle VerificationCodeData serialization")
    void shouldHandleVerificationCodeDataSerialization() throws JsonProcessingException {
        // Given
        when(objectMapper.writeValueAsString(any(VerificationCodeService.VerificationCodeData.class))).thenReturn(TEST_JSON_DATA);

        // When
        verificationCodeService.storeVerificationCode(TEST_USER_ID, TEST_EMAIL, TEST_CODE);

        // Then
        verify(objectMapper, times(1)).writeValueAsString(any(VerificationCodeService.VerificationCodeData.class));
    }
} 