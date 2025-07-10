package com.javajedis.legalconnect.admin;

import java.util.UUID;

import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.user.UserInfoResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLawyerDTO {
    private UUID userId;
    private UserInfoResponseDTO user;
    private UUID lawyerId;
    private LawyerInfoDTO lawyer;
    private String barCertificateFileUrl;
} 