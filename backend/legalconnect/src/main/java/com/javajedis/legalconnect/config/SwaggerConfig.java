package com.javajedis.legalconnect.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customConfig() {
        return new OpenAPI()
                .info(new Info()
                        .title("LegalConnect API Documentation")
                        .description("""
                                A comprehensive platform to make legal help easy to get. This API provides endpoints for user authentication, case management, document handling, and more.
                                
                                ## 🔐 Authentication
                                Most endpoints require authentication via JWT token in the Authorization header:
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```
                                
                                ## 📋 API Features
                                - **User Authentication**: Register, login, email verification, password reset
                                - **Profile Management**: Lawyer profile creation and updates
                                - **Case Management**: Create, update, and manage legal cases
                                - **Document Management**: Upload and manage case documents
                                - **Appointment Scheduling**: Schedule meetings and hearings
                                - **Chat & Video Calls**: Real-time communication features
                                - **Admin Console**: User verification and system management
                                
                                ## 🚀 Getting Started
                                1. Register a new account using `/auth/register`
                                2. Verify your email using the OTP sent to your email
                                3. Login using `/auth/login` to get your JWT token
                                4. Use the token in the Authorization header for subsequent requests
                                
                                ## 📝 API Versioning
                                Current API version: v1
                                
                                Base URL: [https://core.legalconnect.live/v1](https://core.legalconnect.live/v1)
                                
                                ## 👨💻 Developers
                                
                                **Majedul Islam**
                                
                                GitHub: [@mr-majed7](https://github.com/mr-majed7)
                                
                                LinkedIn: [Majedul Islam](https://www.linkedin.com/in/majedul-islam-041637220/)
                                
                                **Shakil Ahmed**
                                
                                GitHub: [@ahmedmshakil](https://github.com/ahmedmshakil)
                                
                                LinkedIn: [Shakil Ahmed](https://www.linkedin.com/in/ahmedmshakil/)
                                
                                ## 🔗 Related Links
                                • [LegalConnect Website](https://app.legalconnect.live)
                                """)
                        .contact(new Contact()
                                .name("LegalConnect Support Team")
                                .email("support@legalconnect.live")
                                .url("https://app.legalconnect.live/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                        .url("http://localhost:8080/v1")
                        .description("Local Development Server"),
                        new Server()
                                .url("https://core.legalconnect.live/v1")
                                .description("Production Server")
   
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT token for authentication")));
    }
}
