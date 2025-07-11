package com.javajedis.legalconnect.caseassets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.javajedis.legalconnect.caseassets.CaseAssetUtility.CaseAssetValidationResult;
import com.javajedis.legalconnect.caseassets.dtos.CreateDocumentDTO;
import com.javajedis.legalconnect.caseassets.dtos.CreateNoteDTO;
import com.javajedis.legalconnect.caseassets.dtos.DocumentListResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.DocumentResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.NoteListResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.NoteResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.UpdateDocumentDTO;
import com.javajedis.legalconnect.caseassets.dtos.UpdateNoteDTO;
import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.AwsService;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class CaseAssetServiceTest {

    @Mock
    private NoteRepo noteRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CaseRepo caseRepo;

    @Mock
    private DocumentRepo documentRepo;

    @Mock
    private AwsService awsService;

    @InjectMocks
    private CaseAssetService caseAssetService;

    private User testUser;
    private Case testCase;
    private Note testNote;
    private Document testDocument;
    private CreateNoteDTO testCreateNoteDTO;
    private UpdateNoteDTO testUpdateNoteDTO;
    private CreateDocumentDTO testCreateDocumentDTO;
    private UpdateDocumentDTO testUpdateDocumentDTO;
    private MockMultipartFile testMultipartFile;

    private UUID testUserId;
    private UUID testCaseId;
    private UUID testNoteId;
    private UUID testDocumentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set bucket name using reflection
        ReflectionTestUtils.setField(caseAssetService, "bucketName", "test-bucket");

        // Setup test IDs
        testUserId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testNoteId = UUID.randomUUID();
        testDocumentId = UUID.randomUUID();

        // Setup test user
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup test case
        testCase = new Case();
        testCase.setId(testCaseId);
        testCase.setTitle("Test Case");

        // Setup test note
        testNote = new Note();
        testNote.setId(testNoteId);
        testNote.setCaseEntity(testCase);
        testNote.setOwner(testUser);
        testNote.setTitle("Test Note");
        testNote.setContent("Test content");
        testNote.setPrivacy(AssetPrivacy.SHARED);
        testNote.setCreatedAt(OffsetDateTime.now());
        testNote.setUpdatedAt(OffsetDateTime.now());

        // Setup test document
        testDocument = new Document();
        testDocument.setId(testDocumentId);
        testDocument.setCaseEntity(testCase);
        testDocument.setUploadedBy(testUser);
        testDocument.setTitle("Test Document");
        testDocument.setDescription("Test description");
        testDocument.setFileUrl("test-file-url");
        testDocument.setPrivacy(AssetPrivacy.SHARED);
        testDocument.setCreatedAt(OffsetDateTime.now());

        // Setup test DTOs
        testCreateNoteDTO = new CreateNoteDTO(testCaseId, "Test Note", "Test content", AssetPrivacy.SHARED);
        testUpdateNoteDTO = new UpdateNoteDTO();
        testUpdateNoteDTO.setTitle("Updated Note");
        testUpdateNoteDTO.setContent("Updated content");
        testUpdateNoteDTO.setPrivacy(AssetPrivacy.PRIVATE);

        testCreateDocumentDTO = new CreateDocumentDTO(testCaseId, "Test Document", "Test description", AssetPrivacy.SHARED);
        testUpdateDocumentDTO = new UpdateDocumentDTO();
        testUpdateDocumentDTO.setTitle("Updated Document");
        testUpdateDocumentDTO.setDescription("Updated description");
        testUpdateDocumentDTO.setPrivacy(AssetPrivacy.PRIVATE);

        // Setup test file
        testMultipartFile = new MockMultipartFile(
            "file",
            "test-document.pdf",
            "application/pdf",
            "test document content".getBytes()
        );
    }

    // ================== NOTE TESTS ==================

    @Test
    void createNote_success_returnsCreatedResponse() {
        // Arrange
        CaseAssetValidationResult<NoteResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(noteRepo.save(any(Note.class))).thenReturn(testNote);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("create note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.createNote(testCreateNoteDTO);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Note created successfully", result.getBody().getMessage());
            verify(noteRepo).save(any(Note.class));
        }
    }

    @Test
    void createNote_validationError_returnsErrorResponse() {
        // Arrange
        ResponseEntity<ApiResponse<NoteResponseDTO>> errorResponse = ApiResponse.error("Case not found", HttpStatus.NOT_FOUND);
        CaseAssetValidationResult<NoteResponseDTO> validationResult = new CaseAssetValidationResult<>(null, null, errorResponse);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("create note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.createNote(testCreateNoteDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            verify(noteRepo, never()).save(any(Note.class));
        }
    }

    @Test
    void updateNote_success_returnsUpdatedNote() {
        // Arrange
        CaseAssetValidationResult<NoteResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(noteRepo.findById(testNoteId)).thenReturn(Optional.of(testNote));
        when(noteRepo.save(any(Note.class))).thenReturn(testNote);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("update note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.updateNote(testNoteId, testUpdateNoteDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Note updated successfully", result.getBody().getMessage());
            verify(noteRepo).save(any(Note.class));
        }
    }

    @Test
    void updateNote_noteNotFound_returnsNotFound() {
        // Arrange
        when(noteRepo.findById(testNoteId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.updateNote(testNoteId, testUpdateNoteDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Note not found", result.getBody().getError().getMessage());
    }

    @Test
    void updateNote_notOwner_returnsForbidden() {
        // Arrange
        CaseAssetValidationResult<NoteResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(noteRepo.findById(testNoteId)).thenReturn(Optional.of(testNote));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("update note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.updateNote(testNoteId, testUpdateNoteDTO);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("You can only update your own notes", result.getBody().getError().getMessage());
        }
    }

    @Test
    void deleteNote_success_returnsSuccessMessage() {
        // Arrange
        CaseAssetValidationResult<String> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(noteRepo.findById(testNoteId)).thenReturn(Optional.of(testNote));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("delete note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<String>> result = caseAssetService.deleteNote(testNoteId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Note deleted successfully", result.getBody().getMessage());
            verify(noteRepo).delete(testNote);
        }
    }

    @Test
    void getNoteById_success_returnsNote() {
        // Arrange
        CaseAssetValidationResult<NoteResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(noteRepo.findById(testNoteId)).thenReturn(Optional.of(testNote));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.getNoteById(testNoteId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Note retrieved successfully", result.getBody().getMessage());
        }
    }

    @Test
    void getNoteById_privateNoteNotOwner_returnsNotFound() {
        // Arrange
        testNote.setPrivacy(AssetPrivacy.PRIVATE);
        CaseAssetValidationResult<NoteResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(noteRepo.findById(testNoteId)).thenReturn(Optional.of(testNote));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view note"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetService.getNoteById(testNoteId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Note not found", result.getBody().getError().getMessage());
        }
    }

    @Test
    void getAllNotesForCase_success_returnsNoteList() {
        // Arrange
        CaseAssetValidationResult<NoteListResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        Page<Note> notePage = new PageImpl<>(List.of(testNote), PageRequest.of(0, 10), 1);
        when(noteRepo.findByCaseEntityId(eq(testCaseId), any(Pageable.class))).thenReturn(notePage);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view case notes"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<NoteListResponseDTO>> result = caseAssetService.getAllNotesForCase(testCaseId, 0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Notes retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getMetadata());
        }
    }

    // ================== DOCUMENT TESTS ==================

    @Test
    void uploadDocument_success_returnsCreatedResponse() {
        // Arrange
        CaseAssetValidationResult<DocumentResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(awsService.uploadFile(anyString(), anyString(), anyLong(), anyString(), any()))
            .thenReturn("uploaded-file-url");
        when(documentRepo.save(any(Document.class))).thenReturn(testDocument);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("upload document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetService.uploadDocument(testCreateDocumentDTO, testMultipartFile);

            // Assert
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Document uploaded successfully", result.getBody().getMessage());
            verify(awsService).uploadFile(anyString(), anyString(), anyLong(), anyString(), any());
            verify(documentRepo).save(any(Document.class));
        }
    }

    @Test
    void uploadDocument_fileTooLarge_returnsBadRequest() {
        // Arrange
        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large-document.pdf",
            "application/pdf",
            new byte[11 * 1_048_576] // 11MB file
        );
        CaseAssetValidationResult<DocumentResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("upload document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetService.uploadDocument(testCreateDocumentDTO, largeFile);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("File size exceeds 10MB limit", result.getBody().getError().getMessage());
        }
    }

    @Test
    void uploadDocument_awsUploadFails_throwsException() {
        // Arrange
        CaseAssetValidationResult<DocumentResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(awsService.uploadFile(anyString(), anyString(), anyLong(), anyString(), any()))
            .thenThrow(new RuntimeException("AWS upload failed"));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("upload document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act & Assert - Expect RuntimeException to bubble up since service only catches IOException
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> caseAssetService.uploadDocument(testCreateDocumentDTO, testMultipartFile)
            );
            assertEquals("AWS upload failed", exception.getMessage());
        }
    }

    @Test
    void updateDocument_success_returnsUpdatedDocument() {
        // Arrange
        CaseAssetValidationResult<DocumentResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));
        when(documentRepo.save(any(Document.class))).thenReturn(testDocument);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("update document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetService.updateDocument(testDocumentId, testUpdateDocumentDTO);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Document updated successfully", result.getBody().getMessage());
            verify(documentRepo).save(any(Document.class));
        }
    }

    @Test
    void updateDocument_notOwner_returnsForbidden() {
        // Arrange
        CaseAssetValidationResult<DocumentResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("update document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetService.updateDocument(testDocumentId, testUpdateDocumentDTO);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("You can only update your own documents", result.getBody().getError().getMessage());
        }
    }

    @Test
    void viewDocument_success_returnsFileBytes() throws IOException {
        // Arrange
        byte[] fileContent = "test document content".getBytes();
        CaseAssetValidationResult<byte[]> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(fileContent);
        when(awsService.downloadFile(anyString(), anyString())).thenReturn(outputStream);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<byte[]> result = caseAssetService.viewDocument(testDocumentId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertArrayEquals(fileContent, result.getBody());
            verify(awsService).downloadFile(anyString(), anyString());
        }
    }

    @Test
    void viewDocument_documentNotFound_returnsNotFound() {
        // Arrange
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> result = caseAssetService.viewDocument(testDocumentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void viewDocument_privateDocumentNotOwner_returnsForbidden() {
        // Arrange
        testDocument.setPrivacy(AssetPrivacy.PRIVATE);
        CaseAssetValidationResult<byte[]> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(false);

            // Act
            ResponseEntity<byte[]> result = caseAssetService.viewDocument(testDocumentId);

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        }
    }

    @Test
    void viewDocument_awsDownloadFails_returnsInternalServerError() throws IOException {
        // Arrange
        CaseAssetValidationResult<byte[]> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));
        when(awsService.downloadFile(anyString(), anyString())).thenThrow(new IOException("Download failed"));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);

            // Act
            ResponseEntity<byte[]> result = caseAssetService.viewDocument(testDocumentId);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        }
    }

    @Test
    void getAllDocumentsForCase_success_returnsDocumentList() {
        // Arrange
        CaseAssetValidationResult<DocumentListResponseDTO> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        Page<Document> documentPage = new PageImpl<>(List.of(testDocument), PageRequest.of(0, 10), 1);
        when(documentRepo.findByCaseEntityId(eq(testCaseId), any(Pageable.class))).thenReturn(documentPage);

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("view case documents"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetService.getAllDocumentsForCase(testCaseId, 0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Documents retrieved successfully", result.getBody().getMessage());
            assertNotNull(result.getBody().getMetadata());
        }
    }

    @Test
    void deleteDocument_success_returnsSuccessMessage() {
        // Arrange
        CaseAssetValidationResult<String> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("delete document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<String>> result = caseAssetService.deleteDocument(testDocumentId);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Document deleted successfully", result.getBody().getMessage());
            verify(awsService).deleteFile(anyString(), anyString());
            verify(documentRepo).delete(testDocument);
        }
    }

    @Test
    void deleteDocument_awsDeleteFails_returnsInternalServerError() {
        // Arrange
        CaseAssetValidationResult<String> validationResult = new CaseAssetValidationResult<>(testUser, testCase, null);
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.of(testDocument));
        doThrow(new RuntimeException("AWS delete failed")).when(awsService).deleteFile(anyString(), anyString());

        try (MockedStatic<CaseAssetUtility> mockedUtility = org.mockito.Mockito.mockStatic(CaseAssetUtility.class)) {
            mockedUtility.when(() -> CaseAssetUtility.validateUserAndCaseAccess(
                eq(testCaseId), eq("delete document"), eq(userRepo), eq(caseRepo)))
                .thenReturn(validationResult);
            mockedUtility.when(() -> CaseAssetUtility.isAssetOwner(eq(testUserId), eq(testUserId)))
                .thenReturn(true);

            // Act
            ResponseEntity<ApiResponse<String>> result = caseAssetService.deleteDocument(testDocumentId);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Failed to delete document file", result.getBody().getError().getMessage());
            verify(documentRepo, never()).delete(any(Document.class));
        }
    }

    @Test
    void deleteDocument_documentNotFound_returnsNotFound() {
        // Arrange
        when(documentRepo.findById(testDocumentId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<String>> result = caseAssetService.deleteDocument(testDocumentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Document not found", result.getBody().getError().getMessage());
    }
} 