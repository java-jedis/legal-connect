package com.javajedis.legalconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Comprehensive unit tests for CorsConfig.
 * Tests CORS configuration source, origins, methods, headers, and all CORS properties.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CorsConfig Tests")
class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        setupDefaultTestProperties();
    }

    private void setupDefaultTestProperties() {
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000,https://app.example.com");
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", "GET,POST,PUT,DELETE,OPTIONS");
        ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "Content-Type,Authorization,X-Requested-With");
        ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);
        ReflectionTestUtils.setField(corsConfig, "maxAge", 3600L);
        ReflectionTestUtils.setField(corsConfig, "exposedHeaders", "X-Total-Count,X-Page-Count");
    }

    // CORS CONFIGURATION SOURCE TESTS

    @Test
    @DisplayName("Should create CORS configuration source successfully")
    void testCorsConfigurationSourceCreation() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        // Assert
        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);
    }

    @Test
    @DisplayName("Should configure CORS for all paths")
    void testCorsConfigurationForAllPaths() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        // Assert
        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);
        assertNotNull(urlSource.getCorsConfigurations());
        assertTrue(urlSource.getCorsConfigurations().containsKey("/**"));
    }

    // ALLOWED ORIGINS TESTS

    @Test
    @DisplayName("Should configure multiple allowed origins correctly")
    void testMultipleAllowedOrigins() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedOriginPatterns = config.getAllowedOriginPatterns();
        assertNotNull(allowedOriginPatterns);
        assertEquals(2, allowedOriginPatterns.size());
        assertTrue(allowedOriginPatterns.contains("http://localhost:3000"));
        assertTrue(allowedOriginPatterns.contains("https://app.example.com"));
    }

    // ALLOWED METHODS TESTS - Parameterized test replacing multiple similar tests

    private static Stream<String> httpMethodProvider() {
        return Stream.of("GET", "POST", "PUT");
    }

    @ParameterizedTest
    @MethodSource("httpMethodProvider")
    @DisplayName("Should configure individual HTTP methods correctly")
    void testIndividualHttpMethods(String method) {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", method);

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedMethods = config.getAllowedMethods();
        assertEquals(1, allowedMethods.size());
        assertTrue(allowedMethods.contains(method));
    }

    @Test
    @DisplayName("Should configure multiple allowed methods correctly")
    void testMultipleAllowedMethods() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedMethods = config.getAllowedMethods();
        assertNotNull(allowedMethods);
        assertEquals(5, allowedMethods.size());
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("PUT"));
        assertTrue(allowedMethods.contains("DELETE"));
        assertTrue(allowedMethods.contains("OPTIONS"));
    }

    @Test
    @DisplayName("Should handle all HTTP methods")
    void testAllHttpMethods() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", "GET,POST,PUT,DELETE,PATCH,OPTIONS,HEAD");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedMethods = config.getAllowedMethods();
        assertEquals(7, allowedMethods.size());
        assertTrue(allowedMethods.contains("PATCH"));
        assertTrue(allowedMethods.contains("HEAD"));
    }

    // ALLOWED HEADERS TESTS

    @Test
    @DisplayName("Should configure multiple allowed headers correctly")
    void testMultipleAllowedHeaders() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedHeaders = config.getAllowedHeaders();
        assertNotNull(allowedHeaders);
        assertEquals(3, allowedHeaders.size());
        assertTrue(allowedHeaders.contains("Content-Type"));
        assertTrue(allowedHeaders.contains("Authorization"));
        assertTrue(allowedHeaders.contains("X-Requested-With"));
    }

    @Test
    @DisplayName("Should handle wildcard allowed headers")
    void testWildcardAllowedHeaders() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "*");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedHeaders = config.getAllowedHeaders();
        assertEquals(1, allowedHeaders.size());
        assertTrue(allowedHeaders.contains("*"));
    }

    @Test
    @DisplayName("Should handle custom headers")
    void testCustomAllowedHeaders() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "X-Custom-Header,X-API-Key,X-Client-Version");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedHeaders = config.getAllowedHeaders();
        assertEquals(3, allowedHeaders.size());
        assertTrue(allowedHeaders.contains("X-Custom-Header"));
        assertTrue(allowedHeaders.contains("X-API-Key"));
        assertTrue(allowedHeaders.contains("X-Client-Version"));
    }

    // ALLOW CREDENTIALS TESTS

    @Test
    @DisplayName("Should set allow credentials to true")
    void testAllowCredentialsTrue() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertTrue(config.getAllowCredentials());
    }

    @Test
    @DisplayName("Should set allow credentials to false")
    void testAllowCredentialsFalse() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowCredentials", false);

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertFalse(config.getAllowCredentials());
    }

    // MAX AGE TESTS

    @Test
    @DisplayName("Should configure max age correctly")
    void testMaxAgeConfiguration() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertEquals(3600L, config.getMaxAge());
    }

    @Test
    @DisplayName("Should handle different max age values")
    void testDifferentMaxAgeValues() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "maxAge", 7200L);

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertEquals(7200L, config.getMaxAge());
    }

    @Test
    @DisplayName("Should handle zero max age")
    void testZeroMaxAge() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "maxAge", 0L);

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertEquals(0L, config.getMaxAge());
    }

    // EXPOSED HEADERS TESTS

    @Test
    @DisplayName("Should configure multiple exposed headers correctly")
    void testMultipleExposedHeaders() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> exposedHeaders = config.getExposedHeaders();
        assertNotNull(exposedHeaders);
        assertEquals(2, exposedHeaders.size());
        assertTrue(exposedHeaders.contains("X-Total-Count"));
        assertTrue(exposedHeaders.contains("X-Page-Count"));
    }

    @Test
    @DisplayName("Should handle single exposed header")
    void testSingleExposedHeader() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "exposedHeaders", "X-Rate-Limit");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> exposedHeaders = config.getExposedHeaders();
        assertEquals(1, exposedHeaders.size());
        assertTrue(exposedHeaders.contains("X-Rate-Limit"));
    }

    @Test
    @DisplayName("Should handle custom exposed headers")
    void testCustomExposedHeaders() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "exposedHeaders", "X-Custom-Response,X-Server-Time,X-Request-Id");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> exposedHeaders = config.getExposedHeaders();
        assertEquals(3, exposedHeaders.size());
        assertTrue(exposedHeaders.contains("X-Custom-Response"));
        assertTrue(exposedHeaders.contains("X-Server-Time"));
        assertTrue(exposedHeaders.contains("X-Request-Id"));
    }

    // EDGE CASES AND ERROR HANDLING

    @Test
    @DisplayName("Should handle empty allowed origins")
    void testEmptyAllowedOrigins() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedOriginPatterns = config.getAllowedOriginPatterns();
        assertEquals(1, allowedOriginPatterns.size());
        assertTrue(allowedOriginPatterns.contains(""));
    }

    @Test
    @DisplayName("Should handle empty allowed methods")
    void testEmptyAllowedMethods() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", "");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedMethods = config.getAllowedMethods();
        assertEquals(1, allowedMethods.size());
        assertTrue(allowedMethods.contains(""));
    }

    @Test
    @DisplayName("Should handle whitespace in configuration values")
    void testWhitespaceInConfigurationValues() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", " http://localhost:3000 , https://app.example.com ");
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", " GET , POST , PUT ");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        List<String> allowedOriginPatterns = config.getAllowedOriginPatterns();
        List<String> allowedMethods = config.getAllowedMethods();
        
        assertTrue(allowedOriginPatterns.contains(" http://localhost:3000 "));
        assertTrue(allowedOriginPatterns.contains(" https://app.example.com "));
        assertTrue(allowedMethods.contains(" GET "));
        assertTrue(allowedMethods.contains(" POST "));
        assertTrue(allowedMethods.contains(" PUT "));
    }

    // OBJECT CREATION TESTS

    @Test
    @DisplayName("Should create CorsConfig instance successfully")
    void testCorsConfigCreation() {
        // Act
        CorsConfig config = new CorsConfig();

        // Assert
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should create multiple CorsConfig instances independently")
    void testMultipleCorsConfigInstances() {
        // Act
        CorsConfig config1 = new CorsConfig();
        CorsConfig config2 = new CorsConfig();

        // Assert
        assertNotNull(config1);
        assertNotNull(config2);
    }

    // INTEGRATION TESTS

    @Test
    @DisplayName("Should create complete CORS configuration with all properties")
    void testCompleteConfiguration() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertNotNull(config.getAllowedOriginPatterns());
        assertNotNull(config.getAllowedMethods());
        assertNotNull(config.getAllowedHeaders());
        assertNotNull(config.getAllowCredentials());
        assertNotNull(config.getMaxAge());
        assertNotNull(config.getExposedHeaders());
        
        assertTrue(config.getAllowedOriginPatterns().size() > 0);
        assertTrue(config.getAllowedMethods().size() > 0);
        assertTrue(config.getAllowedHeaders().size() > 0);
        assertTrue(config.getExposedHeaders().size() > 0);
    }

    @Test
    @DisplayName("Should handle configuration with minimal settings")
    void testMinimalConfiguration() {
        // Arrange
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", "GET");
        ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "*");
        ReflectionTestUtils.setField(corsConfig, "allowCredentials", false);
        ReflectionTestUtils.setField(corsConfig, "maxAge", 0L);
        ReflectionTestUtils.setField(corsConfig, "exposedHeaders", "");

        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        // Assert
        assertNotNull(config);
        assertEquals(1, config.getAllowedOriginPatterns().size());
        assertEquals(1, config.getAllowedMethods().size());
        assertEquals(1, config.getAllowedHeaders().size());
        assertFalse(config.getAllowCredentials());
        assertEquals(0L, config.getMaxAge());
    }
} 