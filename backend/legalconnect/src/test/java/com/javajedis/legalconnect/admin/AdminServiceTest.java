package com.javajedis.legalconnect.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerSpecializationRepo;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;

class AdminServiceTest {

    @Mock
    private LawyerRepo lawyerRepo;

    @Mock
    private LawyerSpecializationRepo lawyerSpecializationRepo;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLawyersByVerificationStatus_shouldReturnLawyers() {
        Lawyer lawyer = new Lawyer();
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
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
        Lawyer lawyer2 = new Lawyer();
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
} 