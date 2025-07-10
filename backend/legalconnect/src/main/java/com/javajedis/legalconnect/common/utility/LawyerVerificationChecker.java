package com.javajedis.legalconnect.common.utility;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.LawyerUtil;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("lawyerVerificationChecker")
public class LawyerVerificationChecker {
    
    private final UserRepo userRepo;
    private final LawyerRepo lawyerRepo;
    
    public LawyerVerificationChecker(UserRepo userRepo, LawyerRepo lawyerRepo) {
        this.userRepo = userRepo;
        this.lawyerRepo = lawyerRepo;
    }
    
    /**
     * Checks if the authenticated user is a verified lawyer.
     * 
     * @return true if the user is a lawyer with APPROVED verification status
     */
    public boolean isVerifiedLawyer() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }
            
            User user = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            if (user == null) {
                return false;
            }
            
            Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);
            if (lawyer == null) {
                return false;
            }
            
            boolean isVerified = lawyer.getVerificationStatus() == VerificationStatus.APPROVED;
            log.debug("Lawyer verification check for {}: {}", user.getEmail(), isVerified);
            
            return isVerified;
            
        } catch (Exception e) {
            log.warn("Error checking lawyer verification status", e);
            return false;
        }
    }
    
    /**
     * Checks if the authenticated user is a lawyer (regardless of verification status).
     * 
     * @return true if the user is a lawyer
     */
    public boolean isLawyer() {
        try {
            User user = LawyerUtil.getAuthenticatedLawyerUser(userRepo);
            return user != null && lawyerRepo.findByUser(user).isPresent();
        } catch (Exception e) {
            log.warn("Error checking lawyer status", e);
            return false;
        }
    }
} 