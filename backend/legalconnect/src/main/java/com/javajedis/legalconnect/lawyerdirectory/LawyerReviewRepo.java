package com.javajedis.legalconnect.lawyerdirectory;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.javajedis.legalconnect.casemanagement.Case;

@Repository
public interface LawyerReviewRepo extends JpaRepository<LawyerReview, UUID> {
    Page<LawyerReview> findByLawyer_Id(UUID lawyerId, Pageable pageable);
    LawyerReview findByCaseE(Case caseE);
}
