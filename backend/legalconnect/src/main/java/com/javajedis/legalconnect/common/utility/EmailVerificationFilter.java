package com.javajedis.legalconnect.common.utility;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.exception.EmailNotVerifiedException;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationFilter extends OncePerRequestFilter {
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error during email verification check";

    private final UserRepo userRepo;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip email verification check for public endpoints and email verification endpoints
        return path.startsWith("/v1/public/")
                || path.startsWith("/auth/")
                || path.equals("/public/login")
                || path.equals("/public/sign-up");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip filter for unauthenticated requests
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get email from authentication
        String email = authentication.getName();
        if (email == null || email.trim().isEmpty()) {
            log.error("Email is null or empty in authentication");
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVER_ERROR_MESSAGE);
            return;
        }

        try {
            // Look up user by email
            User user = userRepo.findByEmail(email)
                    .orElse(null);

            // Handle user not found
            if (user == null) {
                log.error("User not found in email verification filter: {}", email);
                sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                        INTERNAL_SERVER_ERROR_MESSAGE);
                return;
            }

            // Check email verification
            if (!user.isEmailVerified()) {
                log.warn("Unauthorized access attempt: User {} has not verified their email", email);
                throw new EmailNotVerifiedException("Please verify your email before accessing this resource");
            }

            // User is verified, continue the filter chain
            filterChain.doFilter(request, response);
        } catch (EmailNotVerifiedException e) {
            // Re-throw EmailNotVerifiedException to be handled by GlobalExceptionHandler
            throw e;
        } catch (Exception e) {
            log.error("Error in email verification filter: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                        ApiResponse.error(message, status).getBody()
                )
        );
    }
}
