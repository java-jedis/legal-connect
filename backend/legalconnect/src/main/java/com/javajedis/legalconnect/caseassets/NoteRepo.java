package com.javajedis.legalconnect.caseassets;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepo extends JpaRepository<Note, UUID> {
    
    /**
     * Find all notes for a specific case ordered by creation date (newest first)
     */
    List<Note> findByCaseEntityIdOrderByCreatedAtDesc(UUID caseId);
    
    /**
     * Find all notes for a specific case with pagination ordered by creation date (newest first)
     */
    Page<Note> findByCaseEntityId(UUID caseId, Pageable pageable);
    
    /**
     * Find all notes for a specific case with privacy filter ordered by creation date
     */
    List<Note> findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(UUID caseId, AssetPrivacy privacy);
    
    /**
     * Find all notes for a specific case with privacy filter and pagination
     */
    Page<Note> findByCaseEntityIdAndPrivacy(UUID caseId, AssetPrivacy privacy, Pageable pageable);
    
    /**
     * Find all notes owned by a specific user for a case
     */
    List<Note> findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(UUID caseId, UUID ownerId);
    
    /**
     * Find all notes owned by a specific user for a case with pagination
     */
    Page<Note> findByCaseEntityIdAndOwnerId(UUID caseId, UUID ownerId, Pageable pageable);
}
