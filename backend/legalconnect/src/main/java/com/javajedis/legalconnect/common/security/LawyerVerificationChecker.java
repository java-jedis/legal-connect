package com.javajedis.legalconnect.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.LawyerRepo;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
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
     * @return true if the user is a lawyer with APPROVED verification status, false otherwise
     */
    public boolean isVerifiedLawyer() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.debug("No authenticated user found");
                return false;
            }
            
            // First check if user has LAWYER role using Spring Security authorities
            boolean hasLawyerRole = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_LAWYER"));
            
            if (!hasLawyerRole) {
                log.debug("User does not have LAWYER role");
                return false;
            }
            
            // Get authenticated user safely
            User user = GetUserUtil.getAuthenticatedUser(userRepo);
            if (user == null) {
                log.debug("Could not find authenticated user in database");
                return false;
            }
            
            // Double-check role from database
            if (user.getRole() != Role.LAWYER) {
                log.debug("User {} does not have LAWYER role in database", user.getEmail());
                return false;
            }
            
            // Find lawyer profile
            Lawyer lawyer = lawyerRepo.findByUser(user).orElse(null);
            if (lawyer == null) {
                log.debug("No lawyer profile found for user {}", user.getEmail());
                return false;
            }
            
            // Check verification status
            boolean isVerified = lawyer.getVerificationStatus() == VerificationStatus.APPROVED;
            log.debug("Lawyer verification check for {}: {}", user.getEmail(), isVerified);
            
            return isVerified;
            
        } catch (Exception e) {
            log.warn("Error checking lawyer verification status: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if the authenticated user is a lawyer (regardless of verification status).
     * 
     * @return true if the user is a lawyer, false otherwise
     */
    public boolean isLawyer() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }
            
            // Check role from Spring Security authorities
            boolean hasLawyerRole = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_LAWYER"));
            
            if (!hasLawyerRole) {
                return false;
            }
            
            User user = GetUserUtil.getAuthenticatedUser(userRepo);
            return user != null && 
                   user.getRole() == Role.LAWYER && 
                   lawyerRepo.findByUser(user).isPresent();
                   
        } catch (Exception e) {
            log.warn("Error checking lawyer status: {}", e.getMessage());
            return false;
        }
    }
} 