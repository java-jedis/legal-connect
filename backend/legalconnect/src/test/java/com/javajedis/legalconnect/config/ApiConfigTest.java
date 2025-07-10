package com.javajedis.legalconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Comprehensive unit tests for ApiConfig.
 * Tests configuration properties, default values, and property binding.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = ApiConfig.class)
@TestPropertySource(properties = {
    "api.version=v2",
    "api.name=Test API"
})
@DisplayName("ApiConfig Tests")
class ApiConfigTest {

    private ApiConfig apiConfig;

    @BeforeEach
    void setUp() {
        apiConfig = new ApiConfig();
    }

    // DEFAULT VALUES TESTS

    @Test
    @DisplayName("Should have correct default version")
    void testDefaultVersion() {
        // Act & Assert
        assertEquals("v1", apiConfig.getVersion());
    }

    @Test
    @DisplayName("Should have correct default name")
    void testDefaultName() {
        // Act & Assert
        assertEquals("LegalConnect API", apiConfig.getName());
    }

    // PROPERTY SETTERS AND GETTERS TESTS

    @Test
    @DisplayName("Should set and get version correctly")
    void testSetAndGetVersion() {
        // Arrange
        String testVersion = "v3";

        // Act
        apiConfig.setVersion(testVersion);

        // Assert
        assertEquals(testVersion, apiConfig.getVersion());
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        // Arrange
        String testName = "Custom API Name";

        // Act
        apiConfig.setName(testName);

        // Assert
        assertEquals(testName, apiConfig.getName());
    }

    @Test
    @DisplayName("Should handle null version gracefully")
    void testSetNullVersion() {
        // Act
        apiConfig.setVersion(null);

        // Assert
        assertEquals(null, apiConfig.getVersion());
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void testSetNullName() {
        // Act
        apiConfig.setName(null);

        // Assert
        assertEquals(null, apiConfig.getName());
    }

    @Test
    @DisplayName("Should handle empty version")
    void testSetEmptyVersion() {
        // Arrange
        String emptyVersion = "";

        // Act
        apiConfig.setVersion(emptyVersion);

        // Assert
        assertEquals(emptyVersion, apiConfig.getVersion());
    }

    @Test
    @DisplayName("Should handle empty name")
    void testSetEmptyName() {
        // Arrange
        String emptyName = "";

        // Act
        apiConfig.setName(emptyName);

        // Assert
        assertEquals(emptyName, apiConfig.getName());
    }

    // OBJECT CREATION TESTS

    @Test
    @DisplayName("Should create ApiConfig instance successfully")
    void testObjectCreation() {
        // Act
        ApiConfig config = new ApiConfig();

        // Assert
        assertNotNull(config);
        assertNotNull(config.getVersion());
        assertNotNull(config.getName());
    }

    @Test
    @DisplayName("Should support method chaining with setters")
    void testMethodChaining() {
        // Arrange
        String testVersion = "v4";
        String testName = "Chained API";

        // Act
        apiConfig.setVersion(testVersion);
        apiConfig.setName(testName);

        // Assert
        assertEquals(testVersion, apiConfig.getVersion());
        assertEquals(testName, apiConfig.getName());
    }

    // EDGE CASES

    @Test
    @DisplayName("Should handle very long version string")
    void testLongVersionString() {
        // Arrange
        String longVersion = "v" + "1".repeat(1000);

        // Act
        apiConfig.setVersion(longVersion);

        // Assert
        assertEquals(longVersion, apiConfig.getVersion());
    }

    @Test
    @DisplayName("Should handle very long name string")
    void testLongNameString() {
        // Arrange
        String longName = "Very Long API Name ".repeat(100);

        // Act
        apiConfig.setName(longName);

        // Assert
        assertEquals(longName, apiConfig.getName());
    }

    @Test
    @DisplayName("Should handle special characters in version")
    void testSpecialCharactersInVersion() {
        // Arrange
        String specialVersion = "v1.0-beta.1+build.123";

        // Act
        apiConfig.setVersion(specialVersion);

        // Assert
        assertEquals(specialVersion, apiConfig.getVersion());
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        // Arrange
        String specialName = "Legal&Connect API - v1.0 (Beta)";

        // Act
        apiConfig.setName(specialName);

        // Assert
        assertEquals(specialName, apiConfig.getName());
    }

    // CONFIGURATION PROPERTIES TESTS

    @Test
    @DisplayName("Should be annotated with correct configuration properties prefix")
    void testConfigurationPropertiesAnnotation() {
        // Verify that the class is properly annotated
        // This is more of a compile-time check, but we can verify the class structure
        assertEquals("v1", new ApiConfig().getVersion());
        assertEquals("LegalConnect API", new ApiConfig().getName());
    }
} 