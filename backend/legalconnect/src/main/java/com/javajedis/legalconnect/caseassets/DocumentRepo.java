package com.javajedis.legalconnect.caseassets;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepo extends JpaRepository<Document, UUID> {
    
    /**
     * Find all documents for a specific case ordered by creation date (newest first)
     */
    List<Document> findByCaseEntityIdOrderByCreatedAtDesc(UUID caseId);
    
    /**
     * Find all documents for a specific case with pagination ordered by creation date (newest first)
     */
    Page<Document> findByCaseEntityId(UUID caseId, Pageable pageable);
    
    /**
     * Find all documents for a specific case with privacy filter ordered by creation date
     */
    List<Document> findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(UUID caseId, AssetPrivacy privacy);
    
    /**
     * Find all documents for a specific case with privacy filter and pagination
     */
    Page<Document> findByCaseEntityIdAndPrivacy(UUID caseId, AssetPrivacy privacy, Pageable pageable);
    
    /**
     * Find all documents uploaded by a specific user for a case
     */
    List<Document> findByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc(UUID caseId, UUID uploadedById);
    
    /**
     * Find all documents uploaded by a specific user for a case with pagination
     */
    Page<Document> findByCaseEntityIdAndUploadedById(UUID caseId, UUID uploadedById, Pageable pageable);
} 