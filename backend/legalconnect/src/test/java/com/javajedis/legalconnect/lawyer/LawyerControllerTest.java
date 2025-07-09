package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.lawyer.dto.BarCertificateUploadResponseDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotListResponseDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotResponseDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerProfileDTO;
import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;

class LawyerControllerTest {

    @Mock
    private LawyerService lawyerService;

    @Mock
    private LawyerAvailabilitySlotService lawyerAvailabilitySlotService;

    @InjectMocks
    private LawyerController lawyerController;

    private LawyerProfileDTO testLawyerProfileDTO;
    private LawyerInfoDTO testLawyerInfoDTO;
    private LawyerAvailabilitySlotDTO testAvailabilitySlotDTO;
    private LawyerAvailabilitySlotResponseDTO testAvailabilitySlotResponseDTO;
    private LawyerAvailabilitySlotListResponseDTO testAvailabilitySlotListResponseDTO;
    private BarCertificateUploadResponseDTO testBarCertificateUploadResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test DTOs
        testLawyerProfileDTO = new LawyerProfileDTO();
        testLawyerProfileDTO.setFirm("Test Law Firm");
        testLawyerProfileDTO.setYearsOfExperience(5);
        testLawyerProfileDTO.setBarCertificateNumber("BAR123456");
        
        testLawyerInfoDTO = new LawyerInfoDTO();
        testLawyerInfoDTO.setFirm("Test Law Firm");
        testLawyerInfoDTO.setYearsOfExperience(5);
        testLawyerInfoDTO.setBarCertificateNumber("BAR123456");
        
        testAvailabilitySlotDTO = new LawyerAvailabilitySlotDTO();
        testAvailabilitySlotDTO.setDay(DayOfWeek.MON);
        testAvailabilitySlotDTO.setStartTime(LocalTime.of(9, 0));
        testAvailabilitySlotDTO.setEndTime(LocalTime.of(17, 0));
        
        testAvailabilitySlotResponseDTO = new LawyerAvailabilitySlotResponseDTO();
        testAvailabilitySlotResponseDTO.setId(UUID.randomUUID());
        testAvailabilitySlotResponseDTO.setDay(DayOfWeek.MON);
        testAvailabilitySlotResponseDTO.setStartTime(LocalTime.of(9, 0));
        testAvailabilitySlotResponseDTO.setEndTime(LocalTime.of(17, 0));
        
        testAvailabilitySlotListResponseDTO = new LawyerAvailabilitySlotListResponseDTO();
        testAvailabilitySlotListResponseDTO.setSlots(List.of(testAvailabilitySlotResponseDTO));
        
        testBarCertificateUploadResponseDTO = new BarCertificateUploadResponseDTO("uploaded-file-url");
    }

    @Test
    void createLawyerProfile_returnsSuccess() {
        // Arrange
        ApiResponse<LawyerInfoDTO> apiResponse = ApiResponse.success(testLawyerInfoDTO, HttpStatus.CREATED, "Lawyer profile created successfully").getBody();
        when(lawyerService.createLawyerProfile(any(LawyerProfileDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerController.createLawyerProfile(testLawyerProfileDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testLawyerInfoDTO, result.getBody().getData());
        assertEquals("Lawyer profile created successfully", result.getBody().getMessage());
        verify(lawyerService).createLawyerProfile(testLawyerProfileDTO);
    }

    @Test
    void getLawyerInfo_withEmail_returnsSuccess() {
        // Arrange
        String email = "lawyer@example.com";
        ApiResponse<LawyerInfoDTO> apiResponse = ApiResponse.success(testLawyerInfoDTO, HttpStatus.OK, "Lawyer info retrieved successfully").getBody();
        when(lawyerService.getLawyerInfo(email))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerController.getLawyerInfo(email);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testLawyerInfoDTO, result.getBody().getData());
        assertEquals("Lawyer info retrieved successfully", result.getBody().getMessage());
        verify(lawyerService).getLawyerInfo(email);
    }

    @Test
    void getLawyerInfo_withoutEmail_returnsSuccess() {
        // Arrange
        ApiResponse<LawyerInfoDTO> apiResponse = ApiResponse.success(testLawyerInfoDTO, HttpStatus.OK, "Lawyer info retrieved successfully").getBody();
        when(lawyerService.getLawyerInfo(null))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerController.getLawyerInfo(null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testLawyerInfoDTO, result.getBody().getData());
        assertEquals("Lawyer info retrieved successfully", result.getBody().getMessage());
        verify(lawyerService).getLawyerInfo(null);
    }

    @Test
    void updateLawyerProfile_returnsSuccess() {
        // Arrange
        ApiResponse<LawyerInfoDTO> apiResponse = ApiResponse.success(testLawyerInfoDTO, HttpStatus.OK, "Lawyer profile updated successfully").getBody();
        when(lawyerService.updateLawyerProfile(any(LawyerProfileDTO.class)))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerController.updateLawyerProfile(testLawyerProfileDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testLawyerInfoDTO, result.getBody().getData());
        assertEquals("Lawyer profile updated successfully", result.getBody().getMessage());
        verify(lawyerService).updateLawyerProfile(testLawyerProfileDTO);
    }

    @Test
    void uploadCredentials_returnsSuccess() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "certificate.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        ApiResponse<BarCertificateUploadResponseDTO> apiResponse = ApiResponse.success(testBarCertificateUploadResponseDTO, HttpStatus.OK, "Credentials uploaded successfully").getBody();
        when(lawyerService.uploadLawyerCredentials(any()))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> result = lawyerController.uploadCredentials(file);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testBarCertificateUploadResponseDTO, result.getBody().getData());
        assertEquals("Credentials uploaded successfully", result.getBody().getMessage());
        verify(lawyerService).uploadLawyerCredentials(file);
    }

    @Test
    void viewCredentials_withEmail_returnsSuccess() {
        // Arrange
        String email = "lawyer@example.com";
        byte[] fileContent = "test file content".getBytes();
        when(lawyerService.viewLawyerCredentials(email))
            .thenReturn(ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=certificate.pdf")
                .body(fileContent));

        // Act
        ResponseEntity<byte[]> result = lawyerController.viewCredentials(email);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(fileContent, result.getBody());
        verify(lawyerService).viewLawyerCredentials(email);
    }

    @Test
    void viewCredentials_withoutEmail_returnsSuccess() {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        when(lawyerService.viewLawyerCredentials(null))
            .thenReturn(ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=certificate.pdf")
                .body(fileContent));

        // Act
        ResponseEntity<byte[]> result = lawyerController.viewCredentials(null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(fileContent, result.getBody());
        verify(lawyerService).viewLawyerCredentials(null);
    }

    @Test
    void createAvailabilitySlot_returnsSuccess() {
        // Arrange
        ApiResponse<LawyerAvailabilitySlotResponseDTO> apiResponse = ApiResponse.success(testAvailabilitySlotResponseDTO, HttpStatus.CREATED, "Availability slot created successfully").getBody();
        when(lawyerAvailabilitySlotService.createSlot(any(LawyerAvailabilitySlotDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerController.createAvailabilitySlot(testAvailabilitySlotDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testAvailabilitySlotResponseDTO, result.getBody().getData());
        assertEquals("Availability slot created successfully", result.getBody().getMessage());
        verify(lawyerAvailabilitySlotService).createSlot(testAvailabilitySlotDTO);
    }

    @Test
    void getAllAvailabilitySlots_withEmail_returnsSuccess() {
        // Arrange
        String email = "lawyer@example.com";
        ApiResponse<LawyerAvailabilitySlotListResponseDTO> apiResponse = ApiResponse.success(testAvailabilitySlotListResponseDTO, HttpStatus.OK, "Availability slots retrieved successfully").getBody();
        when(lawyerAvailabilitySlotService.getAllSlots(email))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerController.getAllAvailabilitySlots(email);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testAvailabilitySlotListResponseDTO, result.getBody().getData());
        assertEquals("Availability slots retrieved successfully", result.getBody().getMessage());
        verify(lawyerAvailabilitySlotService).getAllSlots(email);
    }

    @Test
    void getAllAvailabilitySlots_withoutEmail_returnsSuccess() {
        // Arrange
        ApiResponse<LawyerAvailabilitySlotListResponseDTO> apiResponse = ApiResponse.success(testAvailabilitySlotListResponseDTO, HttpStatus.OK, "Availability slots retrieved successfully").getBody();
        when(lawyerAvailabilitySlotService.getAllSlots(null))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerController.getAllAvailabilitySlots(null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testAvailabilitySlotListResponseDTO, result.getBody().getData());
        assertEquals("Availability slots retrieved successfully", result.getBody().getMessage());
        verify(lawyerAvailabilitySlotService).getAllSlots(null);
    }

    @Test
    void updateAvailabilitySlot_returnsSuccess() {
        // Arrange
        UUID slotId = UUID.randomUUID();
        ApiResponse<LawyerAvailabilitySlotResponseDTO> apiResponse = ApiResponse.success(testAvailabilitySlotResponseDTO, HttpStatus.OK, "Availability slot updated successfully").getBody();
        when(lawyerAvailabilitySlotService.updateSlot(eq(slotId), any(LawyerAvailabilitySlotDTO.class)))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerController.updateAvailabilitySlot(slotId, testAvailabilitySlotDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testAvailabilitySlotResponseDTO, result.getBody().getData());
        assertEquals("Availability slot updated successfully", result.getBody().getMessage());
        verify(lawyerAvailabilitySlotService).updateSlot(slotId, testAvailabilitySlotDTO);
    }

    @Test
    void deleteAvailabilitySlot_returnsSuccess() {
        // Arrange
        UUID slotId = UUID.randomUUID();
        ApiResponse<Void> apiResponse = ApiResponse.<Void>success(null, HttpStatus.OK, "Availability slot deleted successfully").getBody();
        when(lawyerAvailabilitySlotService.deleteSlot(slotId))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<Void>> result = lawyerController.deleteAvailabilitySlot(slotId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Availability slot deleted successfully", result.getBody().getMessage());
        verify(lawyerAvailabilitySlotService).deleteSlot(slotId);
    }

    @Test
    void createLawyerProfile_withError_returnsError() {
        // Arrange
        ApiResponse<LawyerInfoDTO> apiResponse = ApiResponse.<LawyerInfoDTO>error("User is not authenticated", HttpStatus.UNAUTHORIZED).getBody();
        when(lawyerService.createLawyerProfile(any(LawyerProfileDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<LawyerInfoDTO>> result = lawyerController.createLawyerProfile(testLawyerProfileDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        verify(lawyerService).createLawyerProfile(testLawyerProfileDTO);
    }

    @Test
    void uploadCredentials_withError_returnsError() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "certificate.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
        
        ApiResponse<BarCertificateUploadResponseDTO> apiResponse = ApiResponse.<BarCertificateUploadResponseDTO>error("File size exceeds 1MB limit", HttpStatus.BAD_REQUEST).getBody();
        when(lawyerService.uploadLawyerCredentials(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<BarCertificateUploadResponseDTO>> result = lawyerController.uploadCredentials(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("File size exceeds 1MB limit", result.getBody().getError().getMessage());
        verify(lawyerService).uploadLawyerCredentials(file);
    }
} 