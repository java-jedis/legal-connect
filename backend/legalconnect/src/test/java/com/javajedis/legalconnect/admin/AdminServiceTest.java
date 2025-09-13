package com.javajedis.legalconnect.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerSpecializationRepo;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class AdminServiceTest {

    @Mock
    private LawyerRepo lawyerRepo;

    @Mock
    private LawyerSpecializationRepo lawyerSpecializationRepo;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User createMockUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("email@example.com");
        user.setRole(Role.LAWYER);
        user.setEmailVerified(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        user.setProfilePictureUrl("https://example.com/profile-full.jpg");
        user.setProfilePictureThumbnailUrl("https://example.com/profile-thumb.jpg");
        user.setProfilePicturePublicId("profile-pic-id");
        return user;
    }

    private User createMockUserWithoutProfilePicture() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("email@example.com");
        user.setRole(Role.LAWYER);
        user.setEmailVerified(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        // No profile picture data
        return user;
    }

    @Test
    void getLawyersByVerificationStatus_shouldReturnLawyers() {
        Lawyer lawyer = new Lawyer();
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        lawyer.setUser(createMockUser());
        List<Lawyer> lawyers = List.of(lawyer);
        when(lawyerRepo.findByVerificationStatus(eq(VerificationStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(lawyers));
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = adminService.getLawyersByVerificationStatus(VerificationStatus.PENDING, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void getLawyersByVerificationStatus_withNullStatus_shouldReturnAllLawyers() {
        Lawyer lawyer1 = new Lawyer();
        lawyer1.setUser(createMockUser());
        Lawyer lawyer2 = new Lawyer();
        lawyer2.setUser(createMockUser());
        List<Lawyer> lawyers = List.of(lawyer1, lawyer2);
        when(lawyerRepo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(lawyers));
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = adminService.getLawyersByVerificationStatus(null, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getLawyers()).hasSize(2);
    }

    @Test
    void getLawyersByVerificationStatus_emptyPage() {
        when(lawyerRepo.findByVerificationStatus(eq(VerificationStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = adminService.getLawyersByVerificationStatus(VerificationStatus.PENDING, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getLawyers()).isEmpty();
    }

    @Test
    void updateLawyerVerificationStatus_shouldUpdateStatus() {
        UUID lawyerId = UUID.randomUUID();
        Lawyer lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        lawyer.setUser(createMockUser());
        when(lawyerRepo.findById(lawyerId)).thenReturn(Optional.of(lawyer));
        when(lawyerRepo.save(any(Lawyer.class))).thenReturn(lawyer);
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.APPROVED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void updateLawyerVerificationStatus_shouldUpdateToRejected() {
        UUID lawyerId = UUID.randomUUID();
        Lawyer lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        lawyer.setUser(createMockUser());
        when(lawyerRepo.findById(lawyerId)).thenReturn(Optional.of(lawyer));
        when(lawyerRepo.save(any(Lawyer.class))).thenReturn(lawyer);
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.REJECTED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void updateLawyerVerificationStatus_shouldReturnNotFound() {
        UUID lawyerId = UUID.randomUUID();
        when(lawyerRepo.findById(lawyerId)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.APPROVED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getLawyersByVerificationStatus_shouldIncludeProfilePicture() {
        // Arrange
        Lawyer lawyer = new Lawyer();
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        lawyer.setUser(createMockUser());
        List<Lawyer> lawyers = List.of(lawyer);
        when(lawyerRepo.findByVerificationStatus(eq(VerificationStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(lawyers));
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = 
            adminService.getLawyersByVerificationStatus(VerificationStatus.PENDING, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getLawyers()).hasSize(1);
        
        AdminLawyerDTO lawyerDTO = response.getBody().getData().getLawyers().get(0);
        assertThat(lawyerDTO.getLawyer().getProfilePicture()).isNotNull();
        assertThat(lawyerDTO.getLawyer().getProfilePicture().getFullPictureUrl())
            .isEqualTo("https://example.com/profile-full.jpg");
        assertThat(lawyerDTO.getLawyer().getProfilePicture().getThumbnailPictureUrl())
            .isEqualTo("https://example.com/profile-thumb.jpg");
        assertThat(lawyerDTO.getLawyer().getProfilePicture().getPublicId())
            .isEqualTo("profile-pic-id");
    }

    @Test
    void getLawyersByVerificationStatus_shouldHandleNullProfilePicture() {
        // Arrange
        Lawyer lawyer = new Lawyer();
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        lawyer.setUser(createMockUserWithoutProfilePicture());
        List<Lawyer> lawyers = List.of(lawyer);
        when(lawyerRepo.findByVerificationStatus(eq(VerificationStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(lawyers));
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = 
            adminService.getLawyersByVerificationStatus(VerificationStatus.PENDING, 0, 10);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getLawyers()).hasSize(1);
        
        AdminLawyerDTO lawyerDTO = response.getBody().getData().getLawyers().get(0);
        assertThat(lawyerDTO.getLawyer().getProfilePicture()).isNull();
    }

    @Test
    void updateLawyerVerificationStatus_shouldPreserveProfilePicture() {
        // Arrange
        UUID lawyerId = UUID.randomUUID();
        Lawyer lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        lawyer.setUser(createMockUser());
        when(lawyerRepo.findById(lawyerId)).thenReturn(Optional.of(lawyer));
        when(lawyerRepo.save(any(Lawyer.class))).thenReturn(lawyer);
        when(lawyerSpecializationRepo.findByLawyer(any())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = 
            adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.APPROVED);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        
        AdminLawyerDTO lawyerDTO = response.getBody().getData();
        assertThat(lawyerDTO.getLawyer().getProfilePicture()).isNotNull();
        assertThat(lawyerDTO.getLawyer().getProfilePicture().getFullPictureUrl())
            .isEqualTo("https://example.com/profile-full.jpg");
    }
}