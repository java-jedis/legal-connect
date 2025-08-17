package com.javajedis.legalconnect.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${spring.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${spring.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${spring.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${spring.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${spring.cors.max-age}")
    private long maxAge;

    @Value("${spring.cors.exposed-headers}")
    private String exposedHeaders;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins (split by comma if multiple)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        // Parse allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Set allowed headers
        if ("*".equals(allowedHeaders)) {
            configuration.setAllowedHeaders(Arrays.asList("*"));
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        
        // Set other CORS properties
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        // Set exposed headers
        List<String> exposed = Arrays.asList(exposedHeaders.split(","));
        configuration.setExposedHeaders(exposed);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 