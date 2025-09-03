package com.javajedis.legalconnect.caseassets;

import com.javajedis.legalconnect.caseassets.dtos.*;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.AwsService;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.notifications.NotificationType;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAssetService {

    private static final String NOTE_NOT_FOUND_LOG = "Note not found with ID: {}";
    private static final String NOTE_NOT_FOUND_MSG = "Note not found";
    private static final String DOCUMENT_NOT_FOUND_LOG = "Document not found with ID: {}";
    private static final String DOCUMENT_NOT_FOUND_MSG = "Document not found";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String META_TOTAL_COUNT = "totalCount";
    private static final String META_PAGE_SIZE = "pageSize";
    private static final String META_TOTAL_PAGES = "totalPages";
    private static final String META_HAS_NEXT = "hasNext";
    private static final String META_HAS_PREVIOUS = "hasPrevious";
    private static final String META_IS_FIRST = "isFirst";
    private static final String META_IS_LAST = "isLast";
    private static final String META_PAGE_NUMBER = "pageNumber";
    private static final String META_SORT_DIRECTION = "sortDirection";
    private static final String META_SORT_FIELD = "sortField";
    private static final String META_APPLIED_FILTERS = "appliedFilters";
    private static final String DOCUMENTS_RETRIEVED_SUCCESS = "Documents retrieved successfully";

    private final NoteRepo noteRepo;
    private final UserRepo userRepo;
    private final CaseRepo caseRepo;
    private final DocumentRepo documentRepo;
    private final AwsService awsService;
    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final EmailService emailService;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    /**
     * Create note for a user in a case
     */
    public ResponseEntity<ApiResponse<NoteResponseDTO>> createNote(CreateNoteDTO noteData) {
        log.debug("Creating note for case ID: {}", noteData.getCaseId());

        CaseAssetUtility.CaseAssetValidationResult<NoteResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        noteData.getCaseId(),
                        "create note",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        Note note = new Note();
        note.setCaseEntity(validation.caseEntity());
        note.setOwner(validation.user());
        note.setTitle(noteData.getTitle());
        note.setContent(noteData.getContent());
        note.setPrivacy(noteData.getPrivacy());

        Note savedNote = noteRepo.save(note);
        log.info("Note created for case {} by user: {}", noteData.getCaseId(), validation.user().getEmail());

        User currentUser = validation.user();
        User recipient = currentUser.getId().equals(validation.caseEntity().getClient().getId())
                ? validation.caseEntity().getLawyer().getUser()
                : validation.caseEntity().getClient();

        String subject = "New note added to case";
        String content = String.format("A new note '%s' has been added to case '%s' by %s %s",
                savedNote.getTitle(),
                validation.caseEntity().getTitle(),
                currentUser.getFirstName(),
                currentUser.getLastName());

        UUID recipientId = recipient.getId();

        if (notificationPreferenceService.checkWebPushEnabled(recipientId, NotificationType.NOTE_CREATE)) {
            notificationService.sendNotification(recipientId, content);
        }

        if (notificationPreferenceService.checkEmailEnabled(recipientId, NotificationType.NOTE_CREATE)) {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("notificationType", "Case Note Added");
            templateVariables.put("content", content);

            emailService.sendTemplateEmail(
                    recipient.getEmail(),
                    subject,
                    "notification-email",
                    templateVariables
            );
        }

        NoteResponseDTO noteResponse = mapNoteToNoteResponseDTO(savedNote);
        return ApiResponse.success(noteResponse, HttpStatus.CREATED, "Note created successfully");
    }

    /**
     * Updates an existing note. User must have access to the case and own the note.
     */
    public ResponseEntity<ApiResponse<NoteResponseDTO>> updateNote(UUID noteId, UpdateNoteDTO updateData) {
        log.debug("Updating note with ID: {}", noteId);

        Note existingNote = noteRepo.findById(noteId).orElse(null);
        if (existingNote == null) {
            log.warn(NOTE_NOT_FOUND_LOG, noteId);
            return ApiResponse.error(NOTE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<NoteResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        existingNote.getCaseEntity().getId(),
                        "update note",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        boolean isOwner = CaseAssetUtility.isAssetOwner(
                existingNote.getOwner().getId(),
                validation.user().getId()
        );

        if (!isOwner) {
            log.warn("User {} attempted to update note {} they don't own",
                    validation.user().getEmail(), noteId);
            return ApiResponse.error("You can only update your own notes", HttpStatus.FORBIDDEN);
        }

        existingNote.setTitle(updateData.getTitle());
        existingNote.setContent(updateData.getContent());
        existingNote.setPrivacy(updateData.getPrivacy());

        Note updatedNote = noteRepo.save(existingNote);
        log.info("Note {} updated by user: {}", noteId, validation.user().getEmail());

        NoteResponseDTO noteResponse = mapNoteToNoteResponseDTO(updatedNote);
        return ApiResponse.success(noteResponse, HttpStatus.OK, "Note updated successfully");
    }

    /**
     * Deletes a note. User must have access to the case and own the note.
     */
    public ResponseEntity<ApiResponse<String>> deleteNote(UUID noteId) {
        log.debug("Deleting note with ID: {}", noteId);

        Note existingNote = noteRepo.findById(noteId).orElse(null);
        if (existingNote == null) {
            log.warn(NOTE_NOT_FOUND_LOG, noteId);
            return ApiResponse.error(NOTE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<String> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        existingNote.getCaseEntity().getId(),
                        "delete note",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        boolean isOwner = CaseAssetUtility.isAssetOwner(
                existingNote.getOwner().getId(),
                validation.user().getId()
        );

        if (!isOwner) {
            log.warn("User {} attempted to delete note {} they don't own",
                    validation.user().getEmail(), noteId);
            return ApiResponse.error("You can only delete your own notes", HttpStatus.FORBIDDEN);
        }

        noteRepo.delete(existingNote);
        log.info("Note {} deleted by user: {}", noteId, validation.user().getEmail());

        return ApiResponse.success("Note deleted successfully", HttpStatus.OK, "Note deleted successfully");
    }

    /**
     * Gets a single note by ID with privacy checks.
     * SHARED notes are visible to anyone with case access.
     * PRIVATE notes are only visible to their owner.
     */
    public ResponseEntity<ApiResponse<NoteResponseDTO>> getNoteById(UUID noteId) {
        log.debug("Getting note with ID: {}", noteId);

        Note note = noteRepo.findById(noteId).orElse(null);
        if (note == null) {
            log.warn(NOTE_NOT_FOUND_LOG, noteId);
            return ApiResponse.error(NOTE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<NoteResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        note.getCaseEntity().getId(),
                        "view note",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        if (note.getPrivacy() == AssetPrivacy.PRIVATE) {
            boolean isOwner = CaseAssetUtility.isAssetOwner(
                    note.getOwner().getId(),
                    validation.user().getId()
            );

            if (!isOwner) {
                log.warn("User {} attempted to view private note {} they don't own",
                        validation.user().getEmail(), noteId);
                return ApiResponse.error(NOTE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND); // Return 404 to not reveal existence
            }
        }

        log.info("Note {} retrieved by user: {}", noteId, validation.user().getEmail());
        NoteResponseDTO noteResponse = mapNoteToNoteResponseDTO(note);
        return ApiResponse.success(noteResponse, HttpStatus.OK, "Note retrieved successfully");
    }

    /**
     * Gets all notes for a case with privacy filtering and pagination.
     * Returns SHARED notes + user's own PRIVATE notes with pagination metadata.
     *
     * @return ResponseEntity with paginated filtered list of notes or error
     */
    public ResponseEntity<ApiResponse<NoteListResponseDTO>> getAllNotesForCase(
            UUID caseId, int page, int size, String sortDirection) {
        log.debug("Getting notes for case ID: {} with page={}, size={}, sort={}", caseId, page, size, sortDirection);

        CaseAssetUtility.CaseAssetValidationResult<NoteListResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        caseId,
                        "view case notes",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Note> notePage = noteRepo.findByCaseEntityId(caseId, pageable);

        List<NoteResponseDTO> filteredNotes = notePage.getContent().stream()
                .filter(note -> note.getPrivacy() == AssetPrivacy.SHARED ||
                        CaseAssetUtility.isAssetOwner(note.getOwner().getId(), validation.user().getId()))
                .map(this::mapNoteToNoteResponseDTO)
                .toList();

        NoteListResponseDTO responseData = new NoteListResponseDTO(filteredNotes);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(META_TOTAL_COUNT, notePage.getTotalElements());
        metadata.put(META_PAGE_NUMBER, notePage.getNumber());
        metadata.put(META_PAGE_SIZE, notePage.getSize());
        metadata.put(META_TOTAL_PAGES, notePage.getTotalPages());
        metadata.put(META_HAS_NEXT, notePage.hasNext());
        metadata.put(META_HAS_PREVIOUS, notePage.hasPrevious());
        metadata.put(META_IS_FIRST, notePage.isFirst());
        metadata.put(META_IS_LAST, notePage.isLast());
        metadata.put(META_SORT_DIRECTION, sortDirection);
        metadata.put(META_SORT_FIELD, CREATED_AT_FIELD);
        metadata.put(META_APPLIED_FILTERS, Map.of("caseId", caseId.toString()));

        log.info("Retrieved {} notes for case {} for user: {} (page {}/{})",
                filteredNotes.size(), caseId, validation.user().getEmail(), page + 1, notePage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, "Notes retrieved successfully", metadata);
    }

    /**
     * Maps a Note entity to NoteResponseDTO.
     *
     * @param note the Note entity to map
     * @return the mapped NoteResponseDTO
     */
    private NoteResponseDTO mapNoteToNoteResponseDTO(Note note) {
        return new NoteResponseDTO(
                note.getCaseEntity().getId(),
                note.getCaseEntity().getTitle(),
                note.getId(),
                note.getOwner().getId(),
                note.getTitle(),
                note.getContent(),
                note.getPrivacy()
        );
    }

    // ================== DOCUMENT METHODS ==================

    /**
     * Upload a document for a case.
     *
     * @param documentData
     * @param file
     * @return
     */
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> uploadDocument(CreateDocumentDTO documentData, MultipartFile file) {
        log.debug("Uploading document for case ID: {}", documentData.getCaseId());

        CaseAssetUtility.CaseAssetValidationResult<DocumentResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        documentData.getCaseId(),
                        "upload document",
                        userRepo, caseRepo);

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        if (file.getSize() > 10 * 1_048_576) {
            log.warn("File too large: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            return ApiResponse.error("File size exceeds 10MB limit", HttpStatus.BAD_REQUEST);
        }
        String keyName = "case-assets/" + documentData.getCaseId() + "/" + file.getOriginalFilename();
        String storedFileName;
        try {
            storedFileName = awsService.uploadFile(
                    bucketName,
                    keyName,
                    file.getSize(),
                    file.getContentType(),
                    file.getInputStream()
            );
            Document document = new Document();
            document.setCaseEntity(validation.caseEntity());
            document.setUploadedBy(validation.user());
            document.setFileUrl(storedFileName);
        } catch (IOException e) {
            log.error("Failed to upload document for user: {}", validation.user().getEmail(), e);
            return ApiResponse.error("Failed to upload document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Document newDocument = new Document();
        newDocument.setCaseEntity(validation.caseEntity());
        newDocument.setUploadedBy(validation.user());
        newDocument.setTitle(documentData.getTitle());
        newDocument.setDescription(documentData.getDescription());
        newDocument.setFileUrl(storedFileName);
        newDocument.setPrivacy(documentData.getPrivacy());
        Document savedDocument = documentRepo.save(newDocument);
        log.info("Document uploaded for case {} by user: {}", documentData.getCaseId(), validation.user().getEmail());

        User currentUser = validation.user();
        User recipient = currentUser.getId().equals(validation.caseEntity().getClient().getId())
                ? validation.caseEntity().getLawyer().getUser()
                : validation.caseEntity().getClient();

        String subject = "New document uploaded to case";
        String content = String.format("A new document '%s' has been uploaded to case '%s' by %s %s",
                savedDocument.getTitle(),
                validation.caseEntity().getTitle(),
                currentUser.getFirstName(),
                currentUser.getLastName());

        UUID recipientId = recipient.getId();

        if (notificationPreferenceService.checkWebPushEnabled(recipientId, NotificationType.DOC_UPLOAD)) {
            notificationService.sendNotification(recipientId, content);
        }

        if (notificationPreferenceService.checkEmailEnabled(recipientId, NotificationType.DOC_UPLOAD)) {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("notificationType", "Case Document Uploaded");
            templateVariables.put("content", content);

            emailService.sendTemplateEmail(
                    recipient.getEmail(),
                    subject,
                    "notification-email",
                    templateVariables
            );
        }
        return ApiResponse.success(mapDocumentToDocumentResponseDTO(savedDocument), HttpStatus.CREATED, "Document uploaded successfully");
    }

    /**
     * Updates an existing document. User must have access to the case and own the document.
     */
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> updateDocument(UUID documentId, UpdateDocumentDTO updateData) {
        log.debug("Updating document with ID: {}", documentId);

        Document existingDocument = documentRepo.findById(documentId).orElse(null);
        if (existingDocument == null) {
            log.warn(DOCUMENT_NOT_FOUND_LOG, documentId);
            return ApiResponse.error(DOCUMENT_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<DocumentResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        existingDocument.getCaseEntity().getId(),
                        "update document",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        boolean isOwner = CaseAssetUtility.isAssetOwner(
                existingDocument.getUploadedBy().getId(),
                validation.user().getId()
        );

        if (!isOwner) {
            log.warn("User {} attempted to update document {} they don't own",
                    validation.user().getEmail(), documentId);
            return ApiResponse.error("You can only update your own documents", HttpStatus.FORBIDDEN);
        }

        existingDocument.setTitle(updateData.getTitle());
        existingDocument.setDescription(updateData.getDescription());
        existingDocument.setPrivacy(updateData.getPrivacy());

        Document updatedDocument = documentRepo.save(existingDocument);
        log.info("Document {} updated by user: {}", documentId, validation.user().getEmail());

        DocumentResponseDTO documentResponse = mapDocumentToDocumentResponseDTO(updatedDocument);
        return ApiResponse.success(documentResponse, HttpStatus.OK, "Document updated successfully");
    }

    /**
     * View a document with privacy validation
     */
    public ResponseEntity<byte[]> viewDocument(UUID documentId) {
        log.debug("Viewing document with ID: {}", documentId);

        Document document = documentRepo.findById(documentId).orElse(null);
        if (document == null) {
            log.warn(DOCUMENT_NOT_FOUND_LOG, documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CaseAssetUtility.CaseAssetValidationResult<byte[]> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        document.getCaseEntity().getId(),
                        "view document",
                        userRepo,
                        caseRepo
                );

        if (validation.hasError()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (document.getPrivacy() == AssetPrivacy.PRIVATE) {
            boolean isOwner = CaseAssetUtility.isAssetOwner(
                    document.getUploadedBy().getId(),
                    validation.user().getId()
            );
            if (!isOwner) {
                log.warn("User {} attempted to view private document {} they don't own",
                        validation.user().getEmail(), documentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        try {
            byte[] fileBytes = awsService.downloadFile(bucketName, document.getFileUrl()).toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getFileUrl());
            return ResponseEntity.ok().headers(headers).body(fileBytes);
        } catch (IOException e) {
            log.error("Failed to download document for user: {}", validation.user().getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all documents in a case with pagination and privacy filtering
     */
    public ResponseEntity<ApiResponse<DocumentListResponseDTO>> getAllDocumentsForCase(
            UUID caseId, int page, int size, String sortDirection) {
        log.debug("Getting all documents for case ID: {} with page={}, size={}, sort={}", caseId, page, size, sortDirection);

        CaseAssetUtility.CaseAssetValidationResult<DocumentListResponseDTO> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        caseId,
                        "view case documents",
                        userRepo,
                        caseRepo);

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, CREATED_AT_FIELD);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Document> documentPage = documentRepo.findByCaseEntityId(caseId, pageable);

        List<DocumentResponseDTO> filteredDocuments = documentPage.getContent().stream()
                .filter(document -> document.getPrivacy() == AssetPrivacy.SHARED
                        || CaseAssetUtility.isAssetOwner(document.getUploadedBy().getId(), validation.user().getId()))
                .map(this::mapDocumentToDocumentResponseDTO)
                .toList();
        DocumentListResponseDTO documentResponse = new DocumentListResponseDTO(filteredDocuments);

        Map<String, Object> metadata = buildPaginationMetadata(
            documentPage,
            sortDirection,
            CREATED_AT_FIELD,
            Map.of("caseId", caseId.toString())
        );

        log.info("Retrieved {} documents for case {} for user: {} (page {}/{})",
                filteredDocuments.size(), caseId, validation.user().getEmail(), page + 1, documentPage.getTotalPages());

        return ApiResponse.success(documentResponse, HttpStatus.OK, DOCUMENTS_RETRIEVED_SUCCESS, metadata);
    }

    /**
     * Delete a document with ownership validation
     */
    public ResponseEntity<ApiResponse<String>> deleteDocument(UUID documentId) {
        log.debug("Deleting document with ID: {}", documentId);

        Document document = documentRepo.findById(documentId).orElse(null);
        if (document == null) {
            log.warn(DOCUMENT_NOT_FOUND_LOG, documentId);
            return ApiResponse.error(DOCUMENT_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        CaseAssetUtility.CaseAssetValidationResult<String> validation =
                CaseAssetUtility.validateUserAndCaseAccess(
                        document.getCaseEntity().getId(),
                        "delete document",
                        userRepo,
                        caseRepo);

        if (validation.hasError()) {
            return validation.errorResponse();
        }

        boolean isOwner = CaseAssetUtility.isAssetOwner(
                document.getUploadedBy().getId(),
                validation.user().getId()
        );

        if (!isOwner) {
            log.warn("User {} attempted to delete document {} they don't own",
                    validation.user().getEmail(), documentId);
            return ApiResponse.error("You can only delete your own documents", HttpStatus.FORBIDDEN);
        }

        // Delete the file from S3 before deleting the database record
        try {
            awsService.deleteFile(bucketName, document.getFileUrl());
            log.info("Document file deleted from S3: {}", document.getFileUrl());
        } catch (Exception e) {
            log.error("Failed to delete document file from S3 for user: {} - file: {}",
                    validation.user().getEmail(), document.getFileUrl(), e);
            return ApiResponse.error("Failed to delete document file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        documentRepo.delete(document);
        log.info("Document {} deleted by user: {}", documentId, validation.user().getEmail());
        return ApiResponse.success("Document deleted successfully", HttpStatus.OK, "Document deleted successfully");
    }

    /**
     * Maps a Document entity to DocumentResponseDTO.
     *
     * @param document the Document entity to map
     * @return the mapped DocumentResponseDTO
     */
    private DocumentResponseDTO mapDocumentToDocumentResponseDTO(Document document) {
        return new DocumentResponseDTO(
                document.getCaseEntity().getId(),
                document.getCaseEntity().getTitle(),
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getUploadedBy().getId(),
                document.getUploadedBy().getFirstName() + " " + document.getUploadedBy().getLastName(),
                document.getPrivacy(),
                document.getCreatedAt()
        );
    }

    public ResponseEntity<ApiResponse<DocumentListResponseDTO>> getMyDocuments(int page, int size, String sortDirection) {
		log.debug("Getting documents for current user with page={}, size={}, sort={}", page, size, sortDirection);
		User user = GetUserUtil.getAuthenticatedUser(userRepo);
		if (user == null) {
			return ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
		}
		Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(direction, CREATED_AT_FIELD);
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Document> documentPage = documentRepo.findByUploadedById(user.getId(), pageable);
		List<DocumentResponseDTO> documents = documentPage.getContent().stream()
				.map(this::mapDocumentToDocumentResponseDTO)
				.toList();
		DocumentListResponseDTO response = new DocumentListResponseDTO(documents);
		Map<String, Object> metadata = buildPaginationMetadata(
			documentPage,
			sortDirection,
			CREATED_AT_FIELD,
			Map.of("uploadedBy", user.getId().toString())
		);
		log.info("Retrieved {} documents for user: {} (page {}/{})", documents.size(), user.getEmail(), page + 1, documentPage.getTotalPages());
		return ApiResponse.success(response, HttpStatus.OK, DOCUMENTS_RETRIEVED_SUCCESS, metadata);
	}

	public ResponseEntity<ApiResponse<DocumentListResponseDTO>> getVisibleDocumentsForCurrentUser(int page, int size, String sortDirection) {
		log.debug("Getting visible documents across all cases for current user with page={}, size={}, sort={}", page, size, sortDirection);
		User user = GetUserUtil.getAuthenticatedUser(userRepo);
		if (user == null) {
			return ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
		}
		Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(direction, CREATED_AT_FIELD);
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Document> documentPage = documentRepo.findVisibleDocumentsForUser(user.getId(), AssetPrivacy.SHARED, pageable);
		List<DocumentResponseDTO> documents = documentPage.getContent().stream()
				.map(this::mapDocumentToDocumentResponseDTO)
				.toList();
		DocumentListResponseDTO response = new DocumentListResponseDTO(documents);
		Map<String, Object> metadata = buildPaginationMetadata(
			documentPage,
			sortDirection,
			CREATED_AT_FIELD,
			Map.of("visibleToUser", user.getId().toString())
		);
		log.info("Retrieved {} visible documents for user: {} (page {}/{})", documents.size(), user.getEmail(), page + 1, documentPage.getTotalPages());
		return ApiResponse.success(response, HttpStatus.OK, DOCUMENTS_RETRIEVED_SUCCESS, metadata);
	}

	private Map<String, Object> buildPaginationMetadata(Page<?> page, String sortDirection, String sortField, Map<String, Object> appliedFilters) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put(META_TOTAL_COUNT, page.getTotalElements());
		metadata.put(META_PAGE_NUMBER, page.getNumber());
		metadata.put(META_PAGE_SIZE, page.getSize());
		metadata.put(META_TOTAL_PAGES, page.getTotalPages());
		metadata.put(META_HAS_NEXT, page.hasNext());
		metadata.put(META_HAS_PREVIOUS, page.hasPrevious());
		metadata.put(META_IS_FIRST, page.isFirst());
		metadata.put(META_IS_LAST, page.isLast());
		metadata.put(META_SORT_DIRECTION, sortDirection);
		metadata.put(META_SORT_FIELD, sortField);
		metadata.put(META_APPLIED_FILTERS, appliedFilters);
		return metadata;
	}
}
