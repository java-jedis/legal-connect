package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class LawyerUtilTest {

    @Mock
    private UserRepo userRepo;
    
    @Mock
    private Authentication authentication;

    private User testUser;
    private Lawyer testLawyer;
    private Map<String, Object> userInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("lawyer@example.com");
        testUser.setRole(Role.LAWYER);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        
        // Setup test lawyer
        testLawyer = new Lawyer();
        testLawyer.setId(UUID.randomUUID());
        testLawyer.setUser(testUser);
        testLawyer.setFirm("Test Law Firm");
        testLawyer.setYearsOfExperience(5);
        testLawyer.setBarCertificateNumber("BAR123456");
        testLawyer.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        testLawyer.setDivision(Division.DHAKA);
        testLawyer.setDistrict(District.DHAKA);
        testLawyer.setBio("Experienced lawyer specializing in corporate law");
        testLawyer.setVerificationStatus(VerificationStatus.PENDING);
        testLawyer.setCreatedAt(OffsetDateTime.now());
        testLawyer.setUpdatedAt(OffsetDateTime.now());
        
        // Setup user info map
        userInfo = new HashMap<>();
        userInfo.put("email", "lawyer@example.com");
    }

    @Test
    void getAuthenticatedLawyerUser_authenticatedLawyer_returnsUser() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepo.findByEmail("lawyer@example.com")).thenReturn(Optional.of(testUser));
        
        // Mock GetUserUtil.getCurrentUserInfo to return our test user info
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getCurrentUserInfo(userRepo)).thenReturn(userInfo);
            
            // Act
            User result = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            
            // Assert
            assertNotNull(result);
            assertEquals(testUser.getId(), result.getId());
            assertEquals(testUser.getEmail(), result.getEmail());
            assertEquals(Role.LAWYER, result.getRole());
        }
    }

    @Test
    void getAuthenticatedLawyerUser_notAuthenticated_returnsNull() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Act
        User result = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
        
        // Assert
        assertNull(result);
    }

    @Test
    void getAuthenticatedLawyerUser_noAuthenticationContext_returnsNull() {
        // Arrange - no authentication context set
        
        // Act
        User result = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
        
        // Assert
        assertNull(result);
    }

    @Test
    void getAuthenticatedLawyerUser_userNotFound_returnsNull() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getCurrentUserInfo(userRepo)).thenReturn(userInfo);
            when(userRepo.findByEmail("lawyer@example.com")).thenReturn(Optional.empty());
            
            // Act
            User result = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            
            // Assert
            assertNull(result);
        }
    }

    @Test
    void getAuthenticatedLawyerUser_userNotLawyer_returnsNull() {
        // Arrange
        User nonLawyerUser = new User();
        nonLawyerUser.setId(UUID.randomUUID());
        nonLawyerUser.setEmail("user@example.com");
        nonLawyerUser.setRole(Role.USER);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getCurrentUserInfo(userRepo)).thenReturn(userInfo);
            when(userRepo.findByEmail("lawyer@example.com")).thenReturn(Optional.of(nonLawyerUser));
            
            // Act
            User result = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            
            // Assert
            assertNull(result);
        }
    }

    @Test
    void getAuthenticatedLawyerUser_emptyUserInfo_returnsNull() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(GetUserUtil.class)) {
            mockStatic.when(() -> GetUserUtil.getCurrentUserInfo(userRepo)).thenReturn(new HashMap<>());
            
            // Act
            User result = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            
            // Assert
            assertNull(result);
        }
    }

    @Test
    void mapLawyerToLawyerInfoDTO_withValidData_returnsCorrectDTO() {
        // Arrange
        List<SpecializationType> specializations = List.of(
            SpecializationType.CORPORATE_LAW,
            SpecializationType.CRIMINAL_LAW
        );
        
        // Act
        LawyerInfoDTO result = LawyerUtil.mapLawyerToLawyerInfoDTO(testLawyer, specializations);
        
        // Assert
        assertNotNull(result);
        assertEquals(testLawyer.getFirm(), result.getFirm());
        assertEquals(testLawyer.getYearsOfExperience(), result.getYearsOfExperience());
        assertEquals(testLawyer.getBarCertificateNumber(), result.getBarCertificateNumber());
        assertEquals(testLawyer.getPracticingCourt(), result.getPracticingCourt());
        assertEquals(testLawyer.getDivision(), result.getDivision());
        assertEquals(testLawyer.getDistrict(), result.getDistrict());
        assertEquals(testLawyer.getBio(), result.getBio());
        assertEquals(testLawyer.getVerificationStatus(), result.getVerificationStatus());
        assertEquals(testLawyer.getCreatedAt(), result.getLawyerCreatedAt());
        assertEquals(testLawyer.getUpdatedAt(), result.getLawyerUpdatedAt());
        assertEquals(specializations, result.getSpecializations());
    }

    @Test
    void mapLawyerToLawyerInfoDTO_withNullSpecializations_returnsEmptyList() {
        // Act
        LawyerInfoDTO result = LawyerUtil.mapLawyerToLawyerInfoDTO(testLawyer, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getSpecializations().isEmpty());
        assertEquals(testLawyer.getFirm(), result.getFirm());
        assertEquals(testLawyer.getYearsOfExperience(), result.getYearsOfExperience());
        assertEquals(testLawyer.getBarCertificateNumber(), result.getBarCertificateNumber());
    }

    @Test
    void mapLawyerToLawyerInfoDTO_withEmptySpecializations_returnsEmptyList() {
        // Arrange
        List<SpecializationType> emptySpecializations = List.of();
        
        // Act
        LawyerInfoDTO result = LawyerUtil.mapLawyerToLawyerInfoDTO(testLawyer, emptySpecializations);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getSpecializations().isEmpty());
        assertEquals(testLawyer.getFirm(), result.getFirm());
    }

    @Test
    void mapLawyerToLawyerInfoDTO_withNullLawyer_returnsNull() {
        // Act
        LawyerInfoDTO result = LawyerUtil.mapLawyerToLawyerInfoDTO(null, List.of());
        
        // Assert
        assertNull(result);
    }
} 