package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.common.exception.EmailNotVerifiedException;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class EmailVerificationFilterTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private EmailVerificationFilter emailVerificationFilter;

    private User testUser;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail(testEmail);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.USER);
        testUser.setEmailVerified(false);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"/v1/public/health", "/auth/login", "/public/login", "/public/sign-up"})
    void shouldNotFilter_PublicOrAuthPaths_ReturnsTrue(String uri) {
        // Arrange
        when(request.getRequestURI()).thenReturn(uri);
        // Act
        boolean result = emailVerificationFilter.shouldNotFilter(request);
        // Assert
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_ProtectedPath_ReturnsFalse() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/user/profile");

        // Act
        boolean result = emailVerificationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void doFilterInternal_NoAuthentication_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userRepo, never()).findByEmail(anyString());
    }

    @Test
    void doFilterInternal_NotAuthenticated_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userRepo, never()).findByEmail(anyString());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullAndEmptySource
    @org.junit.jupiter.params.provider.ValueSource(strings = {"   "})
    void doFilterInternal_InvalidEmail_SendsErrorResponse(String email) throws ServletException, IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(response.getWriter()).thenReturn(printWriter);
        
        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("application/json");
        verify(printWriter).write(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_UserNotFound_SendsErrorResponse() throws ServletException, IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("application/json");
        verify(printWriter).write(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmailNotVerified_ThrowsEmailNotVerifiedException() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);
        // Act & Assert
        assertThrows(EmailNotVerifiedException.class, this::runDoFilterInternal);
    }

    @Test
    void doFilterInternal_EmailVerified_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        testUser.setEmailVerified(true);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_ExceptionOccurs_SendsErrorResponse() throws ServletException, IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepo.findByEmail(testEmail)).thenThrow(new RuntimeException("Database error"));
        SecurityContextHolder.setContext(securityContext);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("application/json");
        verify(printWriter).write(anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmailNotVerifiedException_ReThrowsException() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepo.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);
        // Act & Assert
        EmailNotVerifiedException exception = assertThrows(EmailNotVerifiedException.class, this::runDoFilterInternal);
        assertEquals("Please verify your email before accessing this resource", exception.getMessage());
    }

    @Test
    void doFilterInternal_UserWithDifferentEmail_ChecksCorrectEmail() throws ServletException, IOException {
        // Arrange
        String differentEmail = "different@example.com";
        testUser.setEmailVerified(true);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(differentEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepo.findByEmail(differentEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        // Act
        emailVerificationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(userRepo).findByEmail(differentEmail);
        verify(filterChain).doFilter(request, response);
    }

    // Helper method to wrap checked exceptions in RuntimeException for lambda use
    private void runDoFilterInternal() {
        try {
            emailVerificationFilter.doFilterInternal(request, response, filterChain);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
