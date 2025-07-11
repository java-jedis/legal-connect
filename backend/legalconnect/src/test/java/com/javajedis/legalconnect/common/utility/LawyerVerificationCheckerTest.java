package com.javajedis.legalconnect.common.utility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.common.security.LawyerVerificationChecker;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
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
    private User testNonLawyerUser;
    private Lawyer testLawyer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lawyerVerificationChecker = new LawyerVerificationChecker(userRepo, lawyerRepo);
        
        // Setup test lawyer user
        testUser = new User();
        testUser.setId(java.util.UUID.randomUUID());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@law.com");
        testUser.setRole(Role.LAWYER);
        testUser.setEmailVerified(true);
        
        // Setup test non-lawyer user
        testNonLawyerUser = new User();
        testNonLawyerUser.setId(java.util.UUID.randomUUID());
        testNonLawyerUser.setFirstName("Jane");
        testNonLawyerUser.setLastName("Smith");
        testNonLawyerUser.setEmail("jane.smith@client.com");
        testNonLawyerUser.setRole(Role.USER);
        testNonLawyerUser.setEmailVerified(true);
        
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
    @DisplayName("Should return false when user does not have LAWYER role")
    void shouldReturnFalseWhenUserDoesNotHaveLawyerRole() {
        // Given
        setupAuthenticationWithRole("ROLE_USER");

        // When
        boolean result = lawyerVerificationChecker.isVerifiedLawyer();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user has LAWYER role but database role is USER")
    void shouldReturnFalseWhenUserHasLawyerRoleButDatabaseRoleIsUser() {
        // Given
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testNonLawyerUser);

            // When
            boolean result = lawyerVerificationChecker.isVerifiedLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when authenticated user not found in database")
    void shouldReturnFalseWhenAuthenticatedUserNotFoundInDatabase() {
        // Given
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should return false when user does not have LAWYER role in isLawyer")
    void shouldReturnFalseWhenUserDoesNotHaveLawyerRoleInIsLawyer() {
        // Given
        setupAuthenticationWithRole("ROLE_USER");

        // When
        boolean result = lawyerVerificationChecker.isLawyer();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when authenticated user not found in isLawyer")
    void shouldReturnFalseWhenAuthenticatedUserNotFoundInIsLawyer() {
        // Given
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should return false when database user role is not LAWYER in isLawyer")
    void shouldReturnFalseWhenDatabaseUserRoleIsNotLawyerInIsLawyer() {
        // Given
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testNonLawyerUser);

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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
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
        setupAuthenticationWithRole("ROLE_LAWYER");
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));

            // When
            boolean result = lawyerVerificationChecker.isLawyer();

            // Then
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should return false when authentication is null in isLawyer")
    void shouldReturnFalseWhenAuthenticationIsNullInIsLawyer() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = lawyerVerificationChecker.isLawyer();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when user is not authenticated in isLawyer")
    void shouldReturnFalseWhenUserNotAuthenticatedInIsLawyer() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = lawyerVerificationChecker.isLawyer();

        // Then
        assertFalse(result);
    }

    private void setupAuthenticationWithRole(String role) {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        org.mockito.Mockito.doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);
    }
} 