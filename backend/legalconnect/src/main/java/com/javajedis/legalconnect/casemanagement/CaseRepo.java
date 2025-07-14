package com.javajedis.legalconnect.casemanagement;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.user.User;

public interface CaseRepo extends JpaRepository<Case, UUID> {
    
    /**
     * Find all cases for a specific lawyer with optional status filtering
     */
    Page<Case> findByLawyer(Lawyer lawyer, Pageable pageable);
    
    Page<Case> findByLawyerAndStatus(Lawyer lawyer, CaseStatus status, Pageable pageable);
    
    /**
     * Find all cases for a specific client with optional status filtering
     */
    Page<Case> findByClient(User client, Pageable pageable);
    
    Page<Case> findByClientAndStatus(User client, CaseStatus status, Pageable pageable);
    
    /**
     * Custom query to find cases for a user (as either lawyer or client) with optional status filtering
     */
    @Query("SELECT c FROM Case c WHERE (c.lawyer.user = :user OR c.client = :user)")
    Page<Case> findByUserAsLawyerOrClient(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT c FROM Case c WHERE (c.lawyer.user = :user OR c.client = :user) AND c.status = :status")
    Page<Case> findByUserAsLawyerOrClientAndStatus(@Param("user") User user, @Param("status") CaseStatus status, Pageable pageable);
}
