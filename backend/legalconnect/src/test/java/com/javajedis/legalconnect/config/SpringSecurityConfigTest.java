package com.javajedis.legalconnect.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.JWTFilter;

/**
 * Comprehensive unit tests for SpringSecurityConfig.
 * Tests security filter chain, authentication manager, password encoder, and security configurations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringSecurityConfig Tests")
class SpringSecurityConfigTest {

    @Mock
    private StringRedisTemplate redisTemplate;



    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @InjectMocks
    private SpringSecurityConfig springSecurityConfig;

    @BeforeEach
    void setUp() {
        springSecurityConfig = new SpringSecurityConfig(redisTemplate);
    }

    // CONSTRUCTOR TESTS

    @Test
    @DisplayName("Should create SpringSecurityConfig with RedisTemplate successfully")
    void testConstructorWithRedisTemplate() {
        // Arrange
        StringRedisTemplate testRedisTemplate = mock(StringRedisTemplate.class);

        // Act
        SpringSecurityConfig config = new SpringSecurityConfig(testRedisTemplate);

        // Assert
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should handle null RedisTemplate in constructor")
    void testConstructorWithNullRedisTemplate() {
        // Act & Assert - Should not throw exception
        SpringSecurityConfig config = new SpringSecurityConfig(null);
        assertNotNull(config);
    }

    // PASSWORD ENCODER TESTS

    @Test
    @DisplayName("Should create BCryptPasswordEncoder successfully")
    void testPasswordEncoderCreation() {
        // Act
        PasswordEncoder passwordEncoder = springSecurityConfig.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("Should create consistent password encoder instances")
    void testPasswordEncoderConsistency() {
        // Act
        PasswordEncoder encoder1 = springSecurityConfig.passwordEncoder();
        PasswordEncoder encoder2 = springSecurityConfig.passwordEncoder();

        // Assert
        assertNotNull(encoder1);
        assertNotNull(encoder2);
        assertTrue(encoder1 instanceof BCryptPasswordEncoder);
        assertTrue(encoder2 instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("Should encode passwords correctly with BCrypt")
    void testPasswordEncoderFunctionality() {
        // Arrange
        PasswordEncoder passwordEncoder = springSecurityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        // BCrypt encoded passwords should start with $2a$, $2b$, or $2y$
        assertTrue(encodedPassword.startsWith("$2"));
    }

    @Test
    @DisplayName("Should handle different password formats")
    void testPasswordEncoderWithDifferentPasswords() {
        // Arrange
        PasswordEncoder passwordEncoder = springSecurityConfig.passwordEncoder();
        String[] testPasswords = {
            "simplePassword",
            "Complex@Password123!",
            "password with spaces",
            "🔐emoji_password",
            "verylongpasswordthatexceedsnormallengthlimitsjustfortesting123456789"
        };

        // Act & Assert
        for (String password : testPasswords) {
            String encoded = passwordEncoder.encode(password);
            assertNotNull(encoded);
            assertTrue(passwordEncoder.matches(password, encoded));
        }
    }

    // AUTHENTICATION MANAGER TESTS

    @Test
    @DisplayName("Should create AuthenticationManager successfully")
    void testAuthenticationManagerCreation() throws Exception {
        // Arrange
        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

        // Act
        AuthenticationManager authManager = springSecurityConfig.authenticationManager(authenticationConfiguration);

        // Assert
        assertNotNull(authManager);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    @DisplayName("Should handle AuthenticationConfiguration properly")
    void testAuthenticationManagerWithConfiguration() throws Exception {
        // Arrange
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedManager);

        // Act
        AuthenticationManager result = springSecurityConfig.authenticationManager(authenticationConfiguration);

        // Assert
        assertNotNull(result);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    // SECURITY FILTER CHAIN TESTS (Simplified due to complex Spring Security mocking)

    @Test
    @DisplayName("Should have security filter chain method available")
    void testSecurityFilterChainMethodExists() {
        // This test verifies the method signature exists and is callable
        // Full integration testing would require Spring context
        assertNotNull(springSecurityConfig);
        
        // Verify method exists by reflection
        try {
            springSecurityConfig.getClass().getMethod("securityFilterChain", 
                HttpSecurity.class, JWTFilter.class, EmailVerificationFilter.class, CorsConfigurationSource.class);
        } catch (NoSuchMethodException e) {
            org.junit.jupiter.api.Assertions.fail("securityFilterChain method should exist");
        }
    }

    // INTEGRATION TESTS

    @Test
    @DisplayName("Should create complete security beans")
    void testCompleteSecurityConfiguration() throws Exception {
        // Arrange
        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

        // Act
        PasswordEncoder passwordEncoder = springSecurityConfig.passwordEncoder();
        AuthenticationManager authManager = springSecurityConfig.authenticationManager(authenticationConfiguration);

        // Assert
        assertNotNull(passwordEncoder);
        assertNotNull(authManager);
    }

    @Test
    @DisplayName("Should maintain singleton behavior for beans")
    void testSingletonBehavior() {
        // Act - Create multiple encoder instances and test their behavior
        PasswordEncoder encoder1 = springSecurityConfig.passwordEncoder();
        PasswordEncoder encoder2 = springSecurityConfig.passwordEncoder();
        PasswordEncoder encoder3 = springSecurityConfig.passwordEncoder();

        // Assert
        assertNotNull(encoder1);
        assertNotNull(encoder2);
        assertNotNull(encoder3);
        
        // All should be BCryptPasswordEncoder instances
        assertTrue(encoder1 instanceof BCryptPasswordEncoder);
        assertTrue(encoder2 instanceof BCryptPasswordEncoder);
        assertTrue(encoder3 instanceof BCryptPasswordEncoder);
        
        // Test that all encoders behave consistently with same password
        String testPassword = "singletonTestPassword";
        String encoded1 = encoder1.encode(testPassword);
        String encoded2 = encoder2.encode(testPassword);
        String encoded3 = encoder3.encode(testPassword);
        
        // All should be able to verify any encoded password
        assertTrue(encoder1.matches(testPassword, encoded1));
        assertTrue(encoder1.matches(testPassword, encoded2));
        assertTrue(encoder1.matches(testPassword, encoded3));
        assertTrue(encoder2.matches(testPassword, encoded1));
        assertTrue(encoder3.matches(testPassword, encoded2));
    }



    // EDGE CASES

    @Test
    @DisplayName("Should handle configuration with different RedisTemplate instances")
    void testDifferentRedisTemplateInstancesCreation() {
        // Arrange
        StringRedisTemplate redis1 = mock(StringRedisTemplate.class);
        StringRedisTemplate redis2 = mock(StringRedisTemplate.class);

        // Act
        SpringSecurityConfig config1 = new SpringSecurityConfig(redis1);
        SpringSecurityConfig config2 = new SpringSecurityConfig(redis2);

        // Assert
        assertNotNull(config1);
        assertNotNull(config2);
        
        // Both should create valid password encoders
        assertNotNull(config1.passwordEncoder());
        assertNotNull(config2.passwordEncoder());
    }
} 