package com.javajedis.legalconnect.lawyer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.User;

@Repository
public interface LawyerRepo extends JpaRepository<Lawyer, UUID> {
    Optional<Lawyer> findByUser(User user);
    Optional<Lawyer> findByUserId(UUID userId);
    Optional<Lawyer> findByBarCertificateNumber(String barCertificateNumber);
    boolean existsByBarCertificateNumber(String barCertificateNumber);
    boolean existsByUser(User user);
    boolean existsByUserId(UUID userId);
    
    // Pagination method for admin operations
    Page<Lawyer> findByVerificationStatus(VerificationStatus status, Pageable pageable);
} 