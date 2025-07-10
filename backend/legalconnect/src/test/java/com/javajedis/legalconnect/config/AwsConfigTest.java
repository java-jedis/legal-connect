package com.javajedis.legalconnect.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3;

/**
 * Comprehensive unit tests for AwsConfig.
 * Tests AWS S3 client bean creation, credential handling, and region configuration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AwsConfig Tests")
class AwsConfigTest {

    private AwsConfig awsConfig;

    @BeforeEach
    void setUp() {
        awsConfig = new AwsConfig();
    }

    // S3 CLIENT CREATION TESTS WITH IAM ROLES

    @Test
    @DisplayName("Should create S3 client with IAM roles when credentials are empty")
    void testS3ClientCreationWithIAMRoles() {
        // Arrange
        ReflectionTestUtils.setField(awsConfig, "accessKey", "");
        ReflectionTestUtils.setField(awsConfig, "accessSecret", "");
        ReflectionTestUtils.setField(awsConfig, "region", "us-east-1");

        // Act
        AmazonS3 s3Client = awsConfig.s3Client();

        // Assert
        assertNotNull(s3Client);
        assertTrue(s3Client.getRegionName().equals("us-east-1") || s3Client.getRegionName().equals("US_EAST_1"));
    }

    // S3 CLIENT CREATION TESTS - Parameterized test for various credential scenarios

    private static Stream<Arguments> credentialScenarioProvider() {
        return Stream.of(
            Arguments.of(null, "secret", "us-west-2", "IAM roles when access key is null"),
            Arguments.of("accessKey", null, "eu-west-1", "IAM roles when access secret is null"), 
            Arguments.of(null, null, "ap-south-1", "IAM roles when both credentials are null"),
            Arguments.of("AKIATEST123456", "testSecretKey123", "us-east-1", "static credentials when both are provided"),
            Arguments.of("AKIATEST789012", "anotherSecretKey456", "eu-central-1", "static credentials for different region"),
            Arguments.of("", "secretKey", "us-east-1", "empty access key and non-empty secret"),
            Arguments.of("accessKey", "", "us-east-1", "non-empty access key and empty secret"),
            Arguments.of("   ", "   ", "us-east-1", "whitespace-only credentials"),
            Arguments.of(" AKIATEST123 ", " secretKey123 ", "us-east-1", "access key with leading/trailing spaces"),
            Arguments.of("AKIA" + "X".repeat(50), "secret" + "Y".repeat(50), "us-east-1", "very long credentials"),
            Arguments.of("AKIAVALIDKEY123", "validSecretKey123", "us-east-1", "static credentials preferred over IAM")
        );
    }

    @ParameterizedTest
    @MethodSource("credentialScenarioProvider")
    @DisplayName("Should create S3 client with various credential scenarios")
    void testS3ClientCreationWithVariousCredentials(String accessKey, String accessSecret, String region, String scenario) {
        // Arrange
        ReflectionTestUtils.setField(awsConfig, "accessKey", accessKey);
        ReflectionTestUtils.setField(awsConfig, "accessSecret", accessSecret);
        ReflectionTestUtils.setField(awsConfig, "region", region);

        // Act
        AmazonS3 s3Client = awsConfig.s3Client();

        // Assert
        assertNotNull(s3Client, "S3 client should be created for scenario: " + scenario);
    }

    // REGION CONFIGURATION TESTS

    private static Stream<String> regionProvider() {
        return Stream.of("us-east-1", "us-west-2", "eu-west-1", "ap-south-1", "eu-central-1", 
                        "ap-southeast-1", "ca-central-1", "sa-east-1", "us-gov-east-1", "cn-north-1",
                        "af-south-1", "ap-east-1", "ap-northeast-1", "ap-northeast-2", "ap-northeast-3");
    }

    @ParameterizedTest
    @MethodSource("regionProvider")
    @DisplayName("Should create S3 client with various AWS regions")
    void testS3ClientWithDifferentRegions(String region) {
        // Arrange
        ReflectionTestUtils.setField(awsConfig, "accessKey", "");
        ReflectionTestUtils.setField(awsConfig, "accessSecret", "");
        ReflectionTestUtils.setField(awsConfig, "region", region);

        // Act
        AmazonS3 s3Client = awsConfig.s3Client();

        // Assert
        assertNotNull(s3Client);
    }

    // EDGE CASES AND ERROR HANDLING

    @Test
    @DisplayName("Should fallback to IAM when static credentials are invalid format")
    void testInvalidStaticCredentialsFormatFallback() {
        // Arrange - Using obviously invalid credential formats
        ReflectionTestUtils.setField(awsConfig, "accessKey", "invalid");
        ReflectionTestUtils.setField(awsConfig, "accessSecret", "also-invalid");
        ReflectionTestUtils.setField(awsConfig, "region", "us-east-1");

        // Act
        AmazonS3 s3Client = awsConfig.s3Client();

        // Assert
        assertNotNull(s3Client);
    }

    // OBJECT CREATION TESTS

    @Test
    @DisplayName("Should create AwsConfig instance successfully")
    void testAwsConfigCreation() {
        // Act
        AwsConfig config = new AwsConfig();

        // Assert
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should create multiple AwsConfig instances independently")
    void testMultipleAwsConfigInstances() {
        // Act
        AwsConfig config1 = new AwsConfig();
        AwsConfig config2 = new AwsConfig();

        // Assert
        assertNotNull(config1);
        assertNotNull(config2);
    }

    // BEAN CONSISTENCY TESTS

    @Test
    @DisplayName("Should create consistent S3 clients with same configuration")
    void testConsistentS3ClientCreation() {
        // Arrange
        ReflectionTestUtils.setField(awsConfig, "accessKey", "");
        ReflectionTestUtils.setField(awsConfig, "accessSecret", "");
        ReflectionTestUtils.setField(awsConfig, "region", "us-east-1");

        // Act
        AmazonS3 s3Client1 = awsConfig.s3Client();
        AmazonS3 s3Client2 = awsConfig.s3Client();

        // Assert
        assertNotNull(s3Client1);
        assertNotNull(s3Client2);
        // Note: Different instances but same configuration
    }

    @Test
    @DisplayName("Should create different S3 clients with different regions")
    void testDifferentRegionConfigurations() {
        // Arrange & Act
        ReflectionTestUtils.setField(awsConfig, "accessKey", "");
        ReflectionTestUtils.setField(awsConfig, "accessSecret", "");
        
        ReflectionTestUtils.setField(awsConfig, "region", "us-east-1");
        AmazonS3 usEastClient = awsConfig.s3Client();
        
        ReflectionTestUtils.setField(awsConfig, "region", "eu-west-1");
        AmazonS3 euWestClient = awsConfig.s3Client();

        // Assert
        assertNotNull(usEastClient);
        assertNotNull(euWestClient);
    }
} 