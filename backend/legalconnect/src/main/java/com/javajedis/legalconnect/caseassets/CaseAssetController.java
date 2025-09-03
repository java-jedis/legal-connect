package com.javajedis.legalconnect.caseassets;

import com.javajedis.legalconnect.caseassets.dtos.*;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Tag(name = "4. Case Assets", description = "Case assets management endpoints for notes and documents")
@RestController
@RequestMapping("/case-assets")
@RequireUserOrVerifiedLawyer
@RequiredArgsConstructor
public class CaseAssetController {

    private final CaseAssetService caseAssetService;

    /**
     * Create a new note for a case.
     */
    @Operation(summary = "Create note", description = "Creates a new note for a case. User must have access to the case.")
    @PostMapping("/notes")
    public ResponseEntity<ApiResponse<NoteResponseDTO>> createNote(@Valid @RequestBody CreateNoteDTO noteData) {
        log.info("POST /case-assets/notes called for case: {}", noteData.getCaseId());
        return caseAssetService.createNote(noteData);
    }

    /**
     * Update an existing note.
     */
    @Operation(summary = "Update note", description = "Updates an existing note. User must own the note to update it.")
    @PutMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponseDTO>> updateNote(
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteDTO updateData) {
        log.info("PUT /case-assets/notes/{} called", noteId);
        return caseAssetService.updateNote(noteId, updateData);
    }

    /**
     * Delete a note.
     */
    @Operation(summary = "Delete note", description = "Deletes a note. User must own the note to delete it.")
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<String>> deleteNote(@PathVariable UUID noteId) {
        log.info("DELETE /case-assets/notes/{} called", noteId);
        return caseAssetService.deleteNote(noteId);
    }

    /**
     * Get a single note by ID.
     */
    @Operation(summary = "Get note by ID", description = "Retrieves a note by ID. Private notes are only visible to their owners.")
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponseDTO>> getNoteById(@PathVariable UUID noteId) {
        log.info("GET /case-assets/notes/{} called", noteId);
        return caseAssetService.getNoteById(noteId);
    }

    /**
     * Get all notes for a case with pagination.
     */
    @Operation(summary = "Get all notes for case", description = "Retrieves all notes for a case with pagination and privacy filtering. Returns shared notes + user's own private notes.")
    @GetMapping("/cases/{caseId}/notes")
    public ResponseEntity<ApiResponse<NoteListResponseDTO>> getAllNotesForCase(
            @PathVariable UUID caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /case-assets/cases/{}/notes called with page={}, size={}, sortDirection={}",
                caseId, page, size, sortDirection);
        return caseAssetService.getAllNotesForCase(caseId, page, size, sortDirection);
    }

    // ================== DOCUMENT ENDPOINTS ==================

    /**
     * Upload a document for a case.
     */
    @Operation(summary = "Upload document", description = "Uploads a document for a case. User must have access to the case.")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> uploadDocument(
            @RequestParam("caseId") UUID caseId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("privacy") AssetPrivacy privacy,
            @RequestPart("file") MultipartFile file) {
        log.info("POST /case-assets/documents called for case: {}", caseId);
        CreateDocumentDTO documentData = new CreateDocumentDTO(caseId, title, description, privacy);
        return caseAssetService.uploadDocument(documentData, file);
    }

    /**
     * Update an existing document.
     */
    @Operation(summary = "Update document", description = "Updates an existing document. User must own the document to update it.")
    @PutMapping("/documents/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> updateDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentDTO updateData) {
        log.info("PUT /case-assets/documents/{} called", documentId);
        return caseAssetService.updateDocument(documentId, updateData);
    }

    /**
     * View/Download a document.
     */
    @Operation(summary = "View document", description = "Downloads a document file. Private documents are only accessible to their owners.")
    @GetMapping("/documents/{documentId}/view")
    public ResponseEntity<byte[]> viewDocument(@PathVariable UUID documentId) {
        log.info("GET /case-assets/documents/{}/view called", documentId);
        return caseAssetService.viewDocument(documentId);
    }

    /**
     * Delete a document.
     */
    @Operation(summary = "Delete document", description = "Deletes a document. User must own the document to delete it.")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(@PathVariable UUID documentId) {
        log.info("DELETE /case-assets/documents/{} called", documentId);
        return caseAssetService.deleteDocument(documentId);
    }

    /**
     * Get all documents for a case with pagination.
     */
    @Operation(summary = "Get all documents for case", description = "Retrieves all documents for a case with pagination and privacy filtering. Returns shared documents + user's own private documents.")
    @GetMapping("/cases/{caseId}/documents")
    public ResponseEntity<ApiResponse<DocumentListResponseDTO>> getAllDocumentsForCase(
            @PathVariable UUID caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /case-assets/cases/{}/documents called with page={}, size={}, sortDirection={}",
                caseId, page, size, sortDirection);
        return caseAssetService.getAllDocumentsForCase(caseId, page, size, sortDirection);
    }

	/**
	 * Get all documents uploaded by the current user with pagination.
	 */
	@Operation(summary = "Get my documents", description = "Retrieves all documents uploaded by the current authenticated user with pagination.")
	@GetMapping("/users/me/documents")
	public ResponseEntity<ApiResponse<DocumentListResponseDTO>> getMyDocuments(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String sortDirection) {
		log.info("GET /case-assets/users/me/documents called with page={}, size={}, sortDirection={}", page, size, sortDirection);
		return caseAssetService.getMyDocuments(page, size, sortDirection);
	}

	/**
	 * Get all documents visible to the current user across all their cases with pagination.
	 */
	@Operation(summary = "Get visible documents", description = "Retrieves all documents the current authenticated user can see across their cases. Includes SHARED documents and user's own PRIVATE documents.")
	@GetMapping("/users/me/documents/visible")
	public ResponseEntity<ApiResponse<DocumentListResponseDTO>> getVisibleDocuments(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "DESC") String sortDirection) {
		log.info("GET /case-assets/users/me/documents/visible called with page={}, size={}, sortDirection={}", page, size, sortDirection);
		return caseAssetService.getVisibleDocumentsForCurrentUser(page, size, sortDirection);
	}
} 