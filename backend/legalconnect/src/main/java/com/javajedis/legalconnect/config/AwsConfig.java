package com.javajedis.legalconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AwsConfig {

    // Injecting access key from application.properties (for local development only)
    @Value("${cloud.aws.credentials.accessKey:}")
    private String accessKey;

    // Injecting secret key from application.properties (for local development only)
    @Value("${cloud.aws.credentials.secretKey:}")
    private String accessSecret;

    // Injecting region from application.properties
    @Value("${cloud.aws.region.static}")
    private String region;

    // Creating a bean for Amazon S3 client
    @Bean
    public AmazonS3 s3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(region);

        // Use IAM roles in production, fallback to access keys for local development
        if (accessKey != null && !accessKey.isEmpty() && accessSecret != null && !accessSecret.isEmpty()) {
            // Local development: Use access keys (not recommended for production)
            // SonarQube: This is intentional for local development only
            // In production, this code path should not be executed as access keys should be empty
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret); // NOSONAR
            builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        } else {
            // Production: Use IAM roles via DefaultAWSCredentialsProviderChain
            // This will automatically use IAM roles when deployed on AWS
            builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }

        return builder.build();
    }
}
