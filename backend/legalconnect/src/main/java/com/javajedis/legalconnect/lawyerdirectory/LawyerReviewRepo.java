package com.javajedis.legalconnect.lawyerdirectory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface LawyerReviewRepo extends JpaRepository<LawyerReview, UUID> {
    Page<LawyerReview> findByLawyer_Id(UUID lawyerId, Pageable pageable);
}
