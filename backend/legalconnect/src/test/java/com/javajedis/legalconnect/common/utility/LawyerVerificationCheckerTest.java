package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerUtil;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@DisplayName("LawyerVerificationChecker Tests")
class LawyerVerificationCheckerTest {

    @Mock
    private UserRepo userRepo;
    
    @Mock
    private LawyerRepo lawyerRepo;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    private LawyerVerificationChecker lawyerVerificationChecker;
    private User testUser;
    private Lawyer testLawyer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lawyerVerificationChecker = new LawyerVerificationChecker(userRepo, lawyerRepo);
        
        // Setup test data
        testUser = new User();
        testUser.setId(java.util.UUID.randomUUID());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@law.com");
        testUser.setRole(Role.LAWYER);
        testUser.setEmailVerified(true);
        
        testLawyer = new Lawyer();
        testLawyer.setId(java.util.UUID.randomUUID());
        testLawyer.setUser(testUser);
        testLawyer.setFirm("Doe & Associates");
        testLawyer.setVerificationStatus(VerificationStatus.APPROVED);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return true when lawyer is verified")
    void shouldReturnTrueWhenLawyerIsVerified() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should return false when lawyer is pending verification")
    void shouldReturnFalseWhenLawyerIsPending() {
        // Given
        testLawyer.setVerificationStatus(VerificationStatus.PENDING);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when lawyer is rejected")
    void shouldReturnFalseWhenLawyerIsRejected() {
        // Given
        testLawyer.setVerificationStatus(VerificationStatus.REJECTED);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when user is not authenticated")
    void shouldReturnFalseWhenUserNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = lawyerVerificationChecker.isVerifiedLawyer();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when authentication is null")
    void shouldReturnFalseWhenAuthenticationIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = lawyerVerificationChecker.isVerifiedLawyer();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user is not a lawyer")
    void shouldReturnFalseWhenUserNotLawyer() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(null);

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when lawyer profile not found")
    void shouldReturnFalseWhenLawyerProfileNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when exception occurs in isVerifiedLawyer")
    void shouldReturnFalseWhenExceptionOccursInIsVerifiedLawyer() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertFalse(result);
        }
    }

    // Tests for isLawyer() method
    @Test
    @DisplayName("Should return true when user is a lawyer")
    void shouldReturnTrueWhenUserIsLawyer() {
        // Given
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should return false when user is not a lawyer in isLawyer")
    void shouldReturnFalseWhenUserNotLawyerInIsLawyer() {
        // Given
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(null);

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when lawyer profile not found in isLawyer")
    void shouldReturnFalseWhenLawyerProfileNotFoundInIsLawyer() {
        // Given
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when exception occurs in isLawyer")
    void shouldReturnFalseWhenExceptionOccursInIsLawyer() {
        // Given
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return true for isLawyer regardless of verification status")
    void shouldReturnTrueForIsLawyerRegardlessOfVerificationStatus() {
        // Given - Test with PENDING status
        testLawyer.setVerificationStatus(VerificationStatus.PENDING);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should return true for isLawyer with rejected verification status")
    void shouldReturnTrueForIsLawyerWithRejectedStatus() {
        // Given - Test with REJECTED status
        testLawyer.setVerificationStatus(VerificationStatus.REJECTED);
        
        try (MockedStatic<LawyerUtil> mockedLawyerUtil = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockedLawyerUtil.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertTrue(result);
        }
    }
} 