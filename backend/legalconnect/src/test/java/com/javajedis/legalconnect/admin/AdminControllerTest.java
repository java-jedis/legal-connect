package com.javajedis.legalconnect.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPendingLawyers_shouldReturnOk() {
        AdminLawyerListResponseDTO dto = new AdminLawyerListResponseDTO(List.of());
        ApiResponse<AdminLawyerListResponseDTO> apiResponse = ApiResponse.success(dto, HttpStatus.OK, "Success").getBody();
        when(adminService.getLawyersByVerificationStatus(VerificationStatus.PENDING, 0, 10))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = adminController.getPendingLawyers(0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void getLawyersByStatus_withStatus_shouldReturnOk() {
        AdminLawyerListResponseDTO dto = new AdminLawyerListResponseDTO(List.of());
        ApiResponse<AdminLawyerListResponseDTO> apiResponse = ApiResponse.success(dto, HttpStatus.OK, "Success").getBody();
        when(adminService.getLawyersByVerificationStatus(VerificationStatus.APPROVED, 0, 10))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = adminController.getLawyersByStatus(VerificationStatus.APPROVED, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void getLawyersByStatus_withoutStatus_shouldReturnOk() {
        AdminLawyerListResponseDTO dto = new AdminLawyerListResponseDTO(List.of());
        ApiResponse<AdminLawyerListResponseDTO> apiResponse = ApiResponse.success(dto, HttpStatus.OK, "Success").getBody();
        when(adminService.getLawyersByVerificationStatus(null, 0, 10))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerListResponseDTO>> response = adminController.getLawyersByStatus(null, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void approveLawyer_shouldReturnOk() {
        UUID lawyerId = UUID.randomUUID();
        AdminLawyerDTO dto = new AdminLawyerDTO();
        ApiResponse<AdminLawyerDTO> apiResponse = ApiResponse.success(dto, HttpStatus.OK, "Approved").getBody();
        when(adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.APPROVED))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminController.approveLawyer(lawyerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void rejectLawyer_shouldReturnOk() {
        UUID lawyerId = UUID.randomUUID();
        AdminLawyerDTO dto = new AdminLawyerDTO();
        ApiResponse<AdminLawyerDTO> apiResponse = ApiResponse.success(dto, HttpStatus.OK, "Rejected").getBody();
        when(adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.REJECTED))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminController.rejectLawyer(lawyerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    void approveLawyer_shouldReturnNotFound() {
        UUID lawyerId = UUID.randomUUID();
        @SuppressWarnings("unchecked")
        ApiResponse<AdminLawyerDTO> apiResponse = (ApiResponse<AdminLawyerDTO>) (ApiResponse<?>) ApiResponse.error("Lawyer not found", HttpStatus.NOT_FOUND).getBody();
        when(adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.APPROVED))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminController.approveLawyer(lawyerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isNotNull();
    }

    @Test
    void rejectLawyer_shouldReturnNotFound() {
        UUID lawyerId = UUID.randomUUID();
        @SuppressWarnings("unchecked")
        ApiResponse<AdminLawyerDTO> apiResponse = (ApiResponse<AdminLawyerDTO>) (ApiResponse<?>) ApiResponse.error("Lawyer not found", HttpStatus.NOT_FOUND).getBody();
        when(adminService.updateLawyerVerificationStatus(lawyerId, VerificationStatus.REJECTED))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        ResponseEntity<ApiResponse<AdminLawyerDTO>> response = adminController.rejectLawyer(lawyerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isNotNull();
    }
} 