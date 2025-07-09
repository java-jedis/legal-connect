package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.AmazonClientException;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.AwsService;
import com.javajedis.legalconnect.lawyer.dto.BarCertificateUploadResponseDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerProfileDTO;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class LawyerServiceTest {

    @Mock
    private LawyerRepo lawyerRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private LawyerSpecializationRepo lawyerSpecializationRepo;

    @Mock
    private AwsService awsService;

    @InjectMocks
    private LawyerService lawyerService;

    private User testUser;
    private Lawyer testLawyer;
    private LawyerProfileDTO testLawyerProfileDTO;
    private List<SpecializationType> testSpecializations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set bucket name using reflection
        ReflectionTestUtils.setField(lawyerService, "bucketName", "test-bucket");
        
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
        
        // Setup test DTO
        testLawyerProfileDTO = new LawyerProfileDTO();
        testLawyerProfileDTO.setFirm("Test Law Firm");
        testLawyerProfileDTO.setYearsOfExperience(5);
        testLawyerProfileDTO.setBarCertificateNumber("BAR123456");
        testLawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        testLawyerProfileDTO.setDivision(Division.DHAKA);
        testLawyerProfileDTO.setDistrict(District.DHAKA);
        testLawyerProfileDTO.setBio("Experienced lawyer specializing in corporate law");
        
        testSpecializations = List.of(SpecializationType.CORPORATE_LAW, SpecializationType.CRIMINAL_LAW);
        testLawyerProfileDTO.setSpecializations(testSpecializations);
    }

    @Test
    void createLawyerProfile_success_returnsCreatedResponse() {
        // Arrange
        when(lawyerRepo.existsByUser(testUser)).thenReturn(false);
        when(lawyerRepo.existsByBarCertificateNumber("BAR123456")).thenReturn(false);
        when(lawyerRepo.save(any(Lawyer.class))).thenReturn(testLawyer);
        when(lawyerSpecializationRepo.save(any(LawyerSpecialization.class))).thenReturn(new LawyerSpecialization());
        
        // Mock LawyerUtil.getAuthenticatedLawyerUser
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            mockStatic.when(() -> LawyerUtil.mapLawyerToLawyerInfoDTO(any(Lawyer.class), any())).thenReturn(new LawyerInfoDTO());
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.createLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile created successfully", result.getBody().getMessage());
            verify(lawyerRepo).save(any(Lawyer.class));
            verify(lawyerSpecializationRepo, times(2)).save(any(LawyerSpecialization.class));
        }
    }

    @Test
    void createLawyerProfile_userNotAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.createLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void createLawyerProfile_profileAlreadyExists_returnsConflict() {
        // Arrange
        when(lawyerRepo.existsByUser(testUser)).thenReturn(true);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.createLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile already exists for this user", result.getBody().getError().getMessage());
        }
    }

    @Test
    void createLawyerProfile_barCertificateExists_returnsConflict() {
        // Arrange
        when(lawyerRepo.existsByUser(testUser)).thenReturn(false);
        when(lawyerRepo.existsByBarCertificateNumber("BAR123456")).thenReturn(true);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.createLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Bar certificate number already exists", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getLawyerInfo_withEmail_success_returnsLawyerInfo() {
        // Arrange
        String email = "lawyer@example.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        when(lawyerSpecializationRepo.findByLawyer(testLawyer)).thenReturn(List.of());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.mapLawyerToLawyerInfoDTO(any(Lawyer.class), any())).thenReturn(new LawyerInfoDTO());
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.getLawyerInfo(email);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer info retrieved successfully", result.getBody().getMessage());
        }
    }

    @Test
    void getLawyerInfo_withEmail_userNotFound_returnsNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.getLawyerInfo(email);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User not found", result.getBody().getError().getMessage());
    }

    @Test
    void getLawyerInfo_withEmail_userNotLawyer_returnsBadRequest() {
        // Arrange
        String email = "user@example.com";
        User nonLawyerUser = new User();
        nonLawyerUser.setEmail(email);
        nonLawyerUser.setRole(Role.USER);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(nonLawyerUser));
        
        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.getLawyerInfo(email);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not a lawyer", result.getBody().getError().getMessage());
    }

    @Test
    void getLawyerInfo_withoutEmail_authenticatedUser_success_returnsLawyerInfo() {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        when(lawyerSpecializationRepo.findByLawyer(testLawyer)).thenReturn(List.of());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            mockStatic.when(() -> LawyerUtil.mapLawyerToLawyerInfoDTO(any(Lawyer.class), any())).thenReturn(new LawyerInfoDTO());
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.getLawyerInfo(null);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer info retrieved successfully", result.getBody().getMessage());
        }
    }

    @Test
    void getLawyerInfo_withoutEmail_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.getLawyerInfo(null);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getLawyerInfo_noProfileExists_returnsEmptyProfile() {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.getLawyerInfo(null);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile has not been created yet", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
        }
    }

    @Test
    void updateLawyerProfile_success_returnsUpdatedProfile() {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        when(lawyerRepo.save(any(Lawyer.class))).thenReturn(testLawyer);
        when(lawyerSpecializationRepo.findByLawyer(testLawyer)).thenReturn(List.of());
        when(lawyerSpecializationRepo.save(any(LawyerSpecialization.class))).thenReturn(new LawyerSpecialization());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            mockStatic.when(() -> LawyerUtil.mapLawyerToLawyerInfoDTO(any(Lawyer.class), any())).thenReturn(new LawyerInfoDTO());
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.updateLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile updated successfully", result.getBody().getMessage());
            verify(lawyerRepo).save(any(Lawyer.class));
        }
    }

    @Test
    void updateLawyerProfile_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.updateLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void updateLawyerProfile_profileNotExists_returnsNotFound() {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerService.updateLawyerProfile(testLawyerProfileDTO);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile does not exists", result.getBody().getError().getMessage());
        }
    }

    @Test
    void uploadLawyerCredentials_success_returnsUploadResponse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "certificate.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        when(lawyerRepo.save(any(Lawyer.class))).thenReturn(testLawyer);
        when(awsService.uploadFile(anyString(), anyString(), anyLong(), anyString(), any()))
            .thenReturn("uploaded-file-url");
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> result = lawyerService.uploadLawyerCredentials(file);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Credentials uploaded successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals("uploaded-file-url", result.getBody().getData().getFileName());
            verify(awsService).uploadFile(anyString(), anyString(), anyLong(), anyString(), any());
        }
    }

    @Test
    void uploadLawyerCredentials_fileTooLarge_returnsBadRequest() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "large-certificate.pdf", 
            "application/pdf", 
            new byte[2_000_000] // 2MB file
        );
        
        // Act
        ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> result = lawyerService.uploadLawyerCredentials(file);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("File size exceeds 1MB limit", result.getBody().getError().getMessage());
    }

    @Test
    void uploadLawyerCredentials_notAuthenticated_returnsUnauthorized() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "certificate.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> result = lawyerService.uploadLawyerCredentials(file);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void uploadLawyerCredentials_profileNotExists_returnsNotFound() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "certificate.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> result = lawyerService.uploadLawyerCredentials(file);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Lawyer profile not found", result.getBody().getError().getMessage());
        }
    }

    @Test
    void uploadLawyerCredentials_uploadFails_throwsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "certificate.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        when(awsService.uploadFile(anyString(), anyString(), anyLong(), anyString(), any()))
            .thenThrow(new AmazonClientException("Upload failed"));
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act & Assert
            AmazonClientException exception = org.junit.jupiter.api.Assertions.assertThrows(
                AmazonClientException.class,
                () -> lawyerService.uploadLawyerCredentials(file)
            );
            assertEquals("Upload failed", exception.getMessage());
        }
    }

    @Test
    void viewLawyerCredentials_withEmail_success_returnsFile() throws Exception {
        // Arrange
        String email = "lawyer@example.com";
        byte[] fileContent = "test file content".getBytes();
        
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        testLawyer.setBarCertificateFileUrl("test-file-url");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(fileContent);
        when(awsService.downloadFile(anyString(), anyString()))
            .thenReturn(outputStream);
        
        // Act
        ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(email);
        
        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertArrayEquals(fileContent, result.getBody());
        verify(awsService).downloadFile(anyString(), anyString());
    }

    @Test
    void viewLawyerCredentials_withEmail_userNotFound_returnsNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(email);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void viewLawyerCredentials_withEmail_userNotLawyer_returnsBadRequest() {
        // Arrange
        String email = "user@example.com";
        User nonLawyerUser = new User();
        nonLawyerUser.setEmail(email);
        nonLawyerUser.setRole(Role.USER);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(nonLawyerUser));
        
        // Act
        ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(email);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void viewLawyerCredentials_withoutEmail_authenticatedUser_success_returnsFile() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        testLawyer.setBarCertificateFileUrl("test-file-url");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(fileContent);
        when(awsService.downloadFile(anyString(), anyString()))
            .thenReturn(outputStream);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(null);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertArrayEquals(fileContent, result.getBody());
            verify(awsService).downloadFile(anyString(), anyString());
        }
    }

    @Test
    void viewLawyerCredentials_withoutEmail_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(null);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNull(result.getBody());
        }
    }

    @Test
    void viewLawyerCredentials_noProfileExists_returnsNotFound() {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(null);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNull(result.getBody());
        }
    }

    @Test
    void viewLawyerCredentials_noFileUrl_returnsNotFound() {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        testLawyer.setBarCertificateFileUrl(null);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(null);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNull(result.getBody());
        }
    }

    @Test
    void viewLawyerCredentials_downloadFails_returnsInternalServerError() throws Exception {
        // Arrange
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        testLawyer.setBarCertificateFileUrl("test-file-url");
        when(awsService.downloadFile(anyString(), anyString()))
            .thenThrow(new RuntimeException("Download failed"));
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            
            // Act
            ResponseEntity<byte[]> result = lawyerService.viewLawyerCredentials(null);
            
            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
            assertNull(result.getBody());
        }
    }
} 