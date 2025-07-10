package com.javajedis.legalconnect.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class AdminLawyerListResponseDTOTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        AdminLawyerDTO lawyer = new AdminLawyerDTO();
        AdminLawyerListResponseDTO dto = new AdminLawyerListResponseDTO(List.of(lawyer));

        assertThat(dto.getLawyers()).hasSize(1);
        assertThat(dto.getLawyers().get(0)).isEqualTo(lawyer);
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AdminLawyerListResponseDTO dto = new AdminLawyerListResponseDTO();
        AdminLawyerDTO lawyer = new AdminLawyerDTO();
        dto.setLawyers(List.of(lawyer));

        assertThat(dto.getLawyers()).hasSize(1);
        assertThat(dto.getLawyers().get(0)).isEqualTo(lawyer);
    }
} 