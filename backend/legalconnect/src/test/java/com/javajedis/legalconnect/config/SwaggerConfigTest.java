package com.javajedis.legalconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Comprehensive unit tests for SwaggerConfig.
 * Tests OpenAPI configuration, info, servers, security requirements, and components.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SwaggerConfig Tests")
class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
    }

    // OPENAPI CREATION TESTS

    @Test
    @DisplayName("Should create OpenAPI configuration successfully")
    void testOpenAPICreation() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();

        // Assert
        assertNotNull(openAPI);
    }

    @Test
    @DisplayName("Should create multiple OpenAPI instances independently")
    void testMultipleOpenAPICreation() {
        // Act
        OpenAPI openAPI1 = swaggerConfig.customConfig();
        OpenAPI openAPI2 = swaggerConfig.customConfig();

        // Assert
        assertNotNull(openAPI1);
        assertNotNull(openAPI2);
    }

    // INFO CONFIGURATION TESTS

    @Test
    @DisplayName("Should configure API info correctly")
    void testAPIInfoConfiguration() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Info info = openAPI.getInfo();

        // Assert
        assertNotNull(info);
        assertEquals("LegalConnect API Documentation", info.getTitle());
        assertNotNull(info.getDescription());
        assertTrue(info.getDescription().contains("LegalConnect"));
        assertTrue(info.getDescription().contains("Authentication"));
        assertTrue(info.getDescription().contains("Case Management"));
    }

    @Test
    @DisplayName("Should include comprehensive description with features")
    void testAPIDescriptionContent() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        String description = openAPI.getInfo().getDescription();

        // Assert
        assertNotNull(description);
        assertTrue(description.contains("🔐 Authentication"));
        assertTrue(description.contains("📋 API Features"));
        assertTrue(description.contains("🚀 Getting Started"));
        assertTrue(description.contains("📝 API Versioning"));
        assertTrue(description.contains("👨💻 Developers"));
        assertTrue(description.contains("🔗 Related Links"));
    }

    @Test
    @DisplayName("Should include developer information in description")
    void testDeveloperInformation() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        String description = openAPI.getInfo().getDescription();

        // Assert
        assertNotNull(description);
        assertTrue(description.contains("Majedul Islam"));
        assertTrue(description.contains("Shakil Ahmed"));
        assertTrue(description.contains("@mr-majed7"));
        assertTrue(description.contains("@ahmedmshakil"));
        assertTrue(description.contains("LinkedIn"));
        assertTrue(description.contains("GitHub"));
    }

    // CONTACT CONFIGURATION TESTS

    @Test
    @DisplayName("Should configure contact information correctly")
    void testContactConfiguration() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Contact contact = openAPI.getInfo().getContact();

        // Assert
        assertNotNull(contact);
        assertEquals("LegalConnect Support Team", contact.getName());
        assertEquals("support@legalconnect.live", contact.getEmail());
        assertEquals("https://legalconnect.live/support", contact.getUrl());
    }

    @Test
    @DisplayName("Should have valid contact email format")
    void testContactEmailFormat() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Contact contact = openAPI.getInfo().getContact();

        // Assert
        assertNotNull(contact);
        assertNotNull(contact.getEmail());
        assertTrue(contact.getEmail().contains("@"));
        assertTrue(contact.getEmail().endsWith(".live"));
    }

    @Test
    @DisplayName("Should have valid contact URL format")
    void testContactURLFormat() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Contact contact = openAPI.getInfo().getContact();

        // Assert
        assertNotNull(contact);
        assertNotNull(contact.getUrl());
        assertTrue(contact.getUrl().startsWith("https://"));
        assertTrue(contact.getUrl().contains("legalconnect.live"));
    }

    // LICENSE CONFIGURATION TESTS

    @Test
    @DisplayName("Should configure license information correctly")
    void testLicenseConfiguration() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        License license = openAPI.getInfo().getLicense();

        // Assert
        assertNotNull(license);
        assertEquals("MIT License", license.getName());
        assertEquals("https://opensource.org/licenses/MIT", license.getUrl());
    }

    @Test
    @DisplayName("Should have valid license URL")
    void testLicenseURL() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        License license = openAPI.getInfo().getLicense();

        // Assert
        assertNotNull(license);
        assertNotNull(license.getUrl());
        assertTrue(license.getUrl().startsWith("https://"));
        assertTrue(license.getUrl().contains("opensource.org"));
    }

    // SERVERS CONFIGURATION TESTS

    @Test
    @DisplayName("Should configure multiple servers correctly")
    void testServersConfiguration() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        List<Server> servers = openAPI.getServers();

        // Assert
        assertNotNull(servers);
        assertEquals(2, servers.size());
    }

    @Test
    @DisplayName("Should configure local development server")
    void testLocalDevelopmentServer() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        List<Server> servers = openAPI.getServers();

        // Assert
        assertNotNull(servers);
        assertTrue(servers.size() > 0);
        
        Server localServer = servers.stream()
            .filter(server -> server.getDescription().contains("Local"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(localServer);
        assertEquals("http://localhost:8080/v1", localServer.getUrl());
        assertEquals("Local Development Server", localServer.getDescription());
    }

    @Test
    @DisplayName("Should configure production server")
    void testProductionServer() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        List<Server> servers = openAPI.getServers();

        // Assert
        assertNotNull(servers);
        assertTrue(servers.size() > 1);
        
        Server productionServer = servers.stream()
            .filter(server -> server.getDescription().contains("Production"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(productionServer);
        assertEquals("https://api.legalconnect.live/v1", productionServer.getUrl());
        assertEquals("Production Server", productionServer.getDescription());
    }

    @Test
    @DisplayName("Should have secure production server URL")
    void testProductionServerSecurity() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        List<Server> servers = openAPI.getServers();

        // Assert
        Server productionServer = servers.stream()
            .filter(server -> server.getDescription().contains("Production"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(productionServer);
        assertTrue(productionServer.getUrl().startsWith("https://"));
    }

    // SECURITY REQUIREMENTS TESTS

    @Test
    @DisplayName("Should configure security requirements correctly")
    void testSecurityRequirements() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        List<SecurityRequirement> securityRequirements = openAPI.getSecurity();

        // Assert
        assertNotNull(securityRequirements);
        assertFalse(securityRequirements.isEmpty());
        
        SecurityRequirement bearerAuth = securityRequirements.stream()
            .filter(req -> req.containsKey("bearerAuth"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(bearerAuth);
    }

    @Test
    @DisplayName("Should configure bearer authentication requirement")
    void testBearerAuthRequirement() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        List<SecurityRequirement> securityRequirements = openAPI.getSecurity();

        // Assert
        assertNotNull(securityRequirements);
        assertTrue(securityRequirements.size() > 0);
        
        SecurityRequirement firstRequirement = securityRequirements.get(0);
        assertTrue(firstRequirement.containsKey("bearerAuth"));
    }

    // COMPONENTS CONFIGURATION TESTS

    @Test
    @DisplayName("Should configure components correctly")
    void testComponentsConfiguration() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Components components = openAPI.getComponents();

        // Assert
        assertNotNull(components);
        assertNotNull(components.getSecuritySchemes());
        assertTrue(components.getSecuritySchemes().containsKey("bearerAuth"));
    }

    @Test
    @DisplayName("Should configure bearer auth security scheme")
    void testBearerAuthSecurityScheme() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Components components = openAPI.getComponents();
        SecurityScheme bearerAuth = components.getSecuritySchemes().get("bearerAuth");

        // Assert
        assertNotNull(bearerAuth);
        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
        assertEquals(SecurityScheme.In.HEADER, bearerAuth.getIn());
        assertEquals("Authorization", bearerAuth.getName());
        assertNotNull(bearerAuth.getDescription());
        assertTrue(bearerAuth.getDescription().contains("JWT"));
    }

    @Test
    @DisplayName("Should have proper JWT token description")
    void testJWTTokenDescription() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();
        Components components = openAPI.getComponents();
        SecurityScheme bearerAuth = components.getSecuritySchemes().get("bearerAuth");

        // Assert
        assertNotNull(bearerAuth);
        assertNotNull(bearerAuth.getDescription());
        assertTrue(bearerAuth.getDescription().contains("authentication"));
    }

    // EDGE CASES AND VALIDATION TESTS

    @Test
    @DisplayName("Should handle repeated configuration calls consistently")
    void testRepeatedConfigurationCalls() {
        // Act
        OpenAPI openAPI1 = swaggerConfig.customConfig();
        OpenAPI openAPI2 = swaggerConfig.customConfig();

        // Assert
        assertNotNull(openAPI1);
        assertNotNull(openAPI2);
        
        // Both should have the same configuration
        assertEquals(openAPI1.getInfo().getTitle(), openAPI2.getInfo().getTitle());
        assertEquals(openAPI1.getServers().size(), openAPI2.getServers().size());
        assertEquals(openAPI1.getSecurity().size(), openAPI2.getSecurity().size());
    }

    @Test
    @DisplayName("Should have all required configuration elements")
    void testCompleteConfiguration() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();

        // Assert
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getInfo().getDescription());
        assertNotNull(openAPI.getInfo().getContact());
        assertNotNull(openAPI.getInfo().getLicense());
        assertNotNull(openAPI.getServers());
        assertNotNull(openAPI.getSecurity());
        assertNotNull(openAPI.getComponents());
        
        assertTrue(openAPI.getServers().size() > 0);
        assertTrue(openAPI.getSecurity().size() > 0);
    }

    @Test
    @DisplayName("Should maintain object integrity")
    void testObjectIntegrity() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();

        // Assert - Check that all nested objects are properly initialized
        assertNotNull(openAPI.getInfo().getContact().getName());
        assertNotNull(openAPI.getInfo().getContact().getEmail());
        assertNotNull(openAPI.getInfo().getContact().getUrl());
        assertNotNull(openAPI.getInfo().getLicense().getName());
        assertNotNull(openAPI.getInfo().getLicense().getUrl());
        
        for (Server server : openAPI.getServers()) {
            assertNotNull(server.getUrl());
            assertNotNull(server.getDescription());
        }
    }

    // OBJECT CREATION TESTS

    @Test
    @DisplayName("Should create SwaggerConfig instance successfully")
    void testSwaggerConfigCreation() {
        // Act
        SwaggerConfig config = new SwaggerConfig();

        // Assert
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should create multiple SwaggerConfig instances independently")
    void testMultipleSwaggerConfigInstances() {
        // Act
        SwaggerConfig config1 = new SwaggerConfig();
        SwaggerConfig config2 = new SwaggerConfig();

        // Assert
        assertNotNull(config1);
        assertNotNull(config2);
    }

    // URL VALIDATION TESTS

    @Test
    @DisplayName("Should have valid URLs in all configuration")
    void testURLValidation() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();

        // Assert
        // License URL
        assertTrue(openAPI.getInfo().getLicense().getUrl().startsWith("https://"));
        
        // Contact URL
        assertTrue(openAPI.getInfo().getContact().getUrl().startsWith("https://"));
        
        // Server URLs
        for (Server server : openAPI.getServers()) {
            assertTrue(server.getUrl().startsWith("http://") || server.getUrl().startsWith("https://"));
        }
    }

    @Test
    @DisplayName("Should have consistent domain usage")
    void testDomainConsistency() {
        // Act
        OpenAPI openAPI = swaggerConfig.customConfig();

        // Assert
        String description = openAPI.getInfo().getDescription();
        String contactUrl = openAPI.getInfo().getContact().getUrl();
        
        // Check that legalconnect.live domain is used consistently
        assertTrue(description.contains("legalconnect.live"));
        assertTrue(contactUrl.contains("legalconnect.live"));
        
        // Check production server uses the same domain
        boolean hasProductionServer = openAPI.getServers().stream()
            .anyMatch(server -> server.getUrl().contains("api.legalconnect.live"));
        assertTrue(hasProductionServer);
    }
} 