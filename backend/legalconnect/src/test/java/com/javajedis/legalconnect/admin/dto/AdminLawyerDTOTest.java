package com.javajedis.legalconnect.admin.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.admin.AdminLawyerDTO;
import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.UserInfoResponseDTO;

class AdminLawyerDTOTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        UUID userId = UUID.randomUUID();
        UUID lawyerId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = List.of(SpecializationType.CIVIL_LAW, SpecializationType.CRIMINAL_LAW);

        UserInfoResponseDTO user = new UserInfoResponseDTO();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("email@example.com");
        user.setRole(Role.LAWYER);
        user.setEmailVerified(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        LawyerInfoDTO lawyer = new LawyerInfoDTO(
                lawyerId, "First", "Last", "email@example.com",
                "Firm", 10, "BAR123", PracticingCourt.SUPREME_COURT,
                Division.DHAKA, District.DHAKA, "Bio", VerificationStatus.PENDING,
                now, now, specializations
        );

        AdminLawyerDTO dto = new AdminLawyerDTO(
                userId, user, lawyerId, lawyer, "url"
        );

        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getUser()).isEqualTo(user);
        assertThat(dto.getLawyerId()).isEqualTo(lawyerId);
        assertThat(dto.getLawyer()).isEqualTo(lawyer);
        assertThat(dto.getBarCertificateFileUrl()).isEqualTo("url");
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AdminLawyerDTO dto = new AdminLawyerDTO();
        dto.setBarCertificateFileUrl("test-url");
        assertThat(dto.getBarCertificateFileUrl()).isEqualTo("test-url");
    }
}