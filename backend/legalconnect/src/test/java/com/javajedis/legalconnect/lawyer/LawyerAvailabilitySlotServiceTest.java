package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
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

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotListResponseDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerAvailabilitySlotResponseDTO;
import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class LawyerAvailabilitySlotServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private LawyerRepo lawyerRepo;

    @Mock
    private LawyerAvailabilitySlotRepo lawyerAvailabilitySlotRepo;

    @InjectMocks
    private LawyerAvailabilitySlotService lawyerAvailabilitySlotService;

    private User testUser;
    private Lawyer testLawyer;
    private LawyerAvailabilitySlot testSlot;
    private LawyerAvailabilitySlotDTO testSlotDTO;
    private UUID testSlotId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("lawyer@example.com");
        testUser.setRole(Role.LAWYER);
        testUser.setEmailVerified(true);
        
        // Setup test lawyer
        testLawyer = new Lawyer();
        testLawyer.setId(UUID.randomUUID());
        testLawyer.setUser(testUser);
        testLawyer.setFirm("Test Law Firm");
        testLawyer.setYearsOfExperience(5);
        testLawyer.setBarCertificateNumber("BAR123456");
        
        // Setup test slot
        testSlotId = UUID.randomUUID();
        testSlot = new LawyerAvailabilitySlot();
        testSlot.setId(testSlotId);
        testSlot.setLawyer(testLawyer);
        testSlot.setDay(DayOfWeek.MON);
        testSlot.setStartTime(LocalTime.of(9, 0));
        testSlot.setEndTime(LocalTime.of(17, 0));
        
        // Setup test DTO
        testSlotDTO = new LawyerAvailabilitySlotDTO();
        testSlotDTO.setDay(DayOfWeek.MON);
        testSlotDTO.setStartTime(LocalTime.of(9, 0));
        testSlotDTO.setEndTime(LocalTime.of(17, 0));
    }

    @Test
    void createSlot_success_returnsCreatedResponse() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.save(any(LawyerAvailabilitySlot.class))).thenReturn(testSlot);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.createSlot(testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Availability slot created successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(testSlotId, result.getBody().getData().getId());
            assertEquals(DayOfWeek.MON, result.getBody().getData().getDay());
            assertEquals(LocalTime.of(9, 0), result.getBody().getData().getStartTime());
            assertEquals(LocalTime.of(17, 0), result.getBody().getData().getEndTime());
            verify(lawyerAvailabilitySlotRepo).save(any(LawyerAvailabilitySlot.class));
        }
    }

    @Test
    void createSlot_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.createSlot(testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void createSlot_noProfileExists_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.createSlot(testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void updateSlot_success_returnsUpdatedResponse() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.findById(testSlotId)).thenReturn(Optional.of(testSlot));
        when(lawyerAvailabilitySlotRepo.save(any(LawyerAvailabilitySlot.class))).thenReturn(testSlot);
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.updateSlot(testSlotId, testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Availability slot updated successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(testSlotId, result.getBody().getData().getId());
            verify(lawyerAvailabilitySlotRepo).save(any(LawyerAvailabilitySlot.class));
        }
    }

    @Test
    void updateSlot_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.updateSlot(testSlotId, testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void updateSlot_slotNotFound_returnsNotFound() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.findById(testSlotId)).thenReturn(Optional.empty());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.updateSlot(testSlotId, testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Slot not found or not owned by user", result.getBody().getError().getMessage());
        }
    }

    @Test
    void updateSlot_slotNotOwnedByUser_returnsNotFound() {
        // Arrange
        Lawyer otherLawyer = new Lawyer();
        otherLawyer.setId(UUID.randomUUID());
        otherLawyer.setUser(testUser);
        
        LawyerAvailabilitySlot otherSlot = new LawyerAvailabilitySlot();
        otherSlot.setId(testSlotId);
        otherSlot.setLawyer(otherLawyer);
        
        when(lawyerAvailabilitySlotRepo.findById(testSlotId)).thenReturn(Optional.of(otherSlot));
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotResponseDTO>> result = lawyerAvailabilitySlotService.updateSlot(testSlotId, testSlotDTO);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Slot not found or not owned by user", result.getBody().getError().getMessage());
        }
    }

    @Test
    void deleteSlot_success_returnsNoContent() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.findById(testSlotId)).thenReturn(Optional.of(testSlot));
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<Void>> result = lawyerAvailabilitySlotService.deleteSlot(testSlotId);
            
            // Assert
            assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Availability slot deleted successfully", result.getBody().getMessage());
            verify(lawyerAvailabilitySlotRepo).delete(testSlot);
        }
    }

    @Test
    void deleteSlot_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<Void>> result = lawyerAvailabilitySlotService.deleteSlot(testSlotId);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void deleteSlot_slotNotFound_returnsNotFound() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.findById(testSlotId)).thenReturn(Optional.empty());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<Void>> result = lawyerAvailabilitySlotService.deleteSlot(testSlotId);
            
            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Slot not found or not owned by user", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getAllSlots_withEmail_success_returnsSlots() {
        // Arrange
        String email = "lawyer@example.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
        when(lawyerAvailabilitySlotRepo.findByLawyerId(testLawyer.getId())).thenReturn(List.of(testSlot));
        
        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(email);
        
        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Availability slots retrieved successfully", result.getBody().getMessage());
        assertNotNull(result.getBody().getData());
        assertEquals(1, result.getBody().getData().getSlots().size());
        assertEquals(testSlotId, result.getBody().getData().getSlots().get(0).getId());
        verify(lawyerAvailabilitySlotRepo).findByLawyerId(testLawyer.getId());
    }

    @Test
    void getAllSlots_withEmail_userNotFound_returnsUnauthorized() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(email);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
    }

    @Test
    void getAllSlots_withEmail_userNotLawyer_returnsUnauthorized() {
        // Arrange
        String email = "user@example.com";
        User nonLawyerUser = new User();
        nonLawyerUser.setEmail(email);
        nonLawyerUser.setRole(Role.USER);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(nonLawyerUser));
        
        // Act
        ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(email);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User is not authenticated", result.getBody().getError().getMessage());
    }

    @Test
    void getAllSlots_withoutEmail_authenticatedUser_success_returnsSlots() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.findByLawyerId(testLawyer.getId())).thenReturn(List.of(testSlot));
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(null);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Availability slots retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(1, result.getBody().getData().getSlots().size());
            assertEquals(testSlotId, result.getBody().getData().getSlots().get(0).getId());
            verify(lawyerAvailabilitySlotRepo).findByLawyerId(testLawyer.getId());
        }
    }

    @Test
    void getAllSlots_withoutEmail_notAuthenticated_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(null);
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(null);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getAllSlots_noProfileExists_returnsUnauthorized() {
        // Arrange
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.empty());
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(null);
            
            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("User is not authenticated", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getAllSlots_emptySlots_returnsEmptyList() {
        // Arrange
        when(lawyerAvailabilitySlotRepo.findByLawyerId(testLawyer.getId())).thenReturn(List.of());
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(null);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Availability slots retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(0, result.getBody().getData().getSlots().size());
            verify(lawyerAvailabilitySlotRepo).findByLawyerId(testLawyer.getId());
        }
    }

    @Test
    void getAllSlots_multipleSlots_returnsAllSlots() {
        // Arrange
        LawyerAvailabilitySlot slot2 = new LawyerAvailabilitySlot();
        slot2.setId(UUID.randomUUID());
        slot2.setLawyer(testLawyer);
        slot2.setDay(DayOfWeek.TUE);
        slot2.setStartTime(LocalTime.of(10, 0));
        slot2.setEndTime(LocalTime.of(18, 0));
        
        when(lawyerAvailabilitySlotRepo.findByLawyerId(testLawyer.getId())).thenReturn(List.of(testSlot, slot2));
        
        try (var mockStatic = org.mockito.Mockito.mockStatic(LawyerUtil.class)) {
            mockStatic.when(() -> LawyerUtil.getAuthenticatedLawyerUser(userRepo)).thenReturn(testUser);
            when(lawyerRepo.findByUser(testUser)).thenReturn(Optional.of(testLawyer));
            
            // Act
            ResponseEntity<ApiResponse<LawyerAvailabilitySlotListResponseDTO>> result = lawyerAvailabilitySlotService.getAllSlots(null);
            
            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Availability slots retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getData());
            assertEquals(2, result.getBody().getData().getSlots().size());
            verify(lawyerAvailabilitySlotRepo).findByLawyerId(testLawyer.getId());
        }
    }
} 