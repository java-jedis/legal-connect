package com.javajedis.legalconnect.lawyer;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.dto.LawyerInfoDTO;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

public class LawyerUtil {

    private LawyerUtil() {
        // Utility class, do not instantiate
    }

    public static User getAuthenticatedLawyerUser(UserRepo userRepo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Map<String, Object> userInfo = GetUserUtil.getCurrentUserInfo(userRepo);
        if (userInfo.isEmpty()) {
            return null;
        }

        String email = (String) userInfo.get("email");
        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            return null;
        }

        if (user.getRole() != com.javajedis.legalconnect.user.Role.LAWYER) {
            return null;
        }

        return user;
    }

    public static LawyerInfoDTO mapLawyerToLawyerInfoDTO(Lawyer lawyer, List<SpecializationType> specializations) {
        LawyerInfoDTO lawyerInfoDTO = new LawyerInfoDTO();
        lawyerInfoDTO.setFirm(lawyer.getFirm());
        lawyerInfoDTO.setYearsOfExperience(lawyer.getYearsOfExperience());
        lawyerInfoDTO.setBarCertificateNumber(lawyer.getBarCertificateNumber());
        lawyerInfoDTO.setPracticingCourt(lawyer.getPracticingCourt());
        lawyerInfoDTO.setDivision(lawyer.getDivision());
        lawyerInfoDTO.setDistrict(lawyer.getDistrict());
        lawyerInfoDTO.setBio(lawyer.getBio());
        lawyerInfoDTO.setVerificationStatus(lawyer.getVerificationStatus());
        lawyerInfoDTO.setLawyerCreatedAt(lawyer.getCreatedAt());
        lawyerInfoDTO.setLawyerUpdatedAt(lawyer.getUpdatedAt());
        lawyerInfoDTO.setSpecializations(specializations != null ? specializations : List.of());
        return lawyerInfoDTO;
    }
} 