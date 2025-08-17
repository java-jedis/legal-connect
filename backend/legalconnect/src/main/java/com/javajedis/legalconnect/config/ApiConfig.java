package com.javajedis.legalconnect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "api")
@Data
public class ApiConfig {
    private String version = "v1";
    private String name = "LegalConnect API";
} 