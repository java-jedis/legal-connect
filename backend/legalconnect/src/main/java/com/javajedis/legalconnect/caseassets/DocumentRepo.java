package com.javajedis.legalconnect.caseassets;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	/**
	 * Find all documents uploaded by a specific user (across all cases) ordered by creation date
	 */
	List<Document> findByUploadedByIdOrderByCreatedAtDesc(UUID uploadedById);

	/**
	 * Find all documents uploaded by a specific user (across all cases) with pagination
	 */
	Page<Document> findByUploadedById(UUID uploadedById, Pageable pageable);

	/**
	 * Find all documents the user can see across all their cases, respecting privacy
	 */
	@Query("SELECT d FROM Document d WHERE (d.caseEntity.client.id = :userId OR d.caseEntity.lawyer.user.id = :userId) AND (d.privacy = :shared OR d.uploadedBy.id = :userId)")
	Page<Document> findVisibleDocumentsForUser(@Param("userId") UUID userId, @Param("shared") AssetPrivacy shared, Pageable pageable);
} 