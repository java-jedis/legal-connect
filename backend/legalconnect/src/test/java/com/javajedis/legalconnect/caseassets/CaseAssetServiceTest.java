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
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.AwsService;
import com.javajedis.legalconnect.common.service.EmailService;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.notifications.NotificationPreferenceService;
import com.javajedis.legalconnect.notifications.NotificationService;
import com.javajedis.legalconnect.notifications.NotificationType;
import com.javajedis.legalconnect.user.Role;
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

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPreferenceService notificationPreferenceService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CaseAssetService caseAssetService;

    private User testUser;
    private User clientUser;
    private User lawyerUser;
    private Lawyer lawyer;
    private Case testCase;
    private Note testNote;
    private Document testDocument;
    private CreateNoteDTO testCreateNoteDTO;
    private UpdateNoteDTO testUpdateNoteDTO;
    private CreateDocumentDTO testCreateDocumentDTO;
    private UpdateDocumentDTO testUpdateDocumentDTO;
    private MockMultipartFile testMultipartFile;

    private UUID testUserId;
    private UUID clientUserId;
    private UUID lawyerUserId;
    private UUID lawyerId;
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
        clientUserId = UUID.randomUUID();
        lawyerUserId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testNoteId = UUID.randomUUID();
        testDocumentId = UUID.randomUUID();

        // Setup test user (current user)
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());

        // Setup client user
        clientUser = new User();
        clientUser.setId(clientUserId);
        clientUser.setEmail("client@example.com");
        clientUser.setFirstName("Client");
        clientUser.setLastName("User");
        clientUser.setRole(Role.USER);
        clientUser.setEmailVerified(true);
        clientUser.setCreatedAt(OffsetDateTime.now());
        clientUser.setUpdatedAt(OffsetDateTime.now());

        // Setup lawyer user
        lawyerUser = new User();
        lawyerUser.setId(lawyerUserId);
        lawyerUser.setEmail("lawyer@example.com");
        lawyerUser.setFirstName("Lawyer");
        lawyerUser.setLastName("User");
        lawyerUser.setRole(Role.LAWYER);
        lawyerUser.setEmailVerified(true);
        lawyerUser.setCreatedAt(OffsetDateTime.now());
        lawyerUser.setUpdatedAt(OffsetDateTime.now());

        // Setup lawyer
        lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setUser(lawyerUser);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyer.setDivision(Division.DHAKA);
        lawyer.setDistrict(District.DHAKA);
        lawyer.setBio("Experienced lawyer");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);
        lawyer.setCreatedAt(OffsetDateTime.now());
        lawyer.setUpdatedAt(OffsetDateTime.now());

        // Setup test case
        testCase = new Case();
        testCase.setId(testCaseId);
        testCase.setTitle("Test Case");
        testCase.setDescription("Test case description");
        testCase.setStatus(CaseStatus.IN_PROGRESS);
        testCase.setLawyer(lawyer);
        testCase.setClient(clientUser);
        testCase.setCreatedAt(OffsetDateTime.now());
        testCase.setUpdatedAt(OffsetDateTime.now());

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
        when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), eq(NotificationType.NOTE_CREATE))).thenReturn(false);
        when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), eq(NotificationType.NOTE_CREATE))).thenReturn(false);

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
        when(notificationPreferenceService.checkWebPushEnabled(any(UUID.class), eq(NotificationType.DOC_UPLOAD))).thenReturn(false);
        when(notificationPreferenceService.checkEmailEnabled(any(UUID.class), eq(NotificationType.DOC_UPLOAD))).thenReturn(false);

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

    @Test
    void getMyDocuments_success_returnsDocumentList() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortDirection = "DESC";
        Page<Document> documentPage = new PageImpl<>(List.of(testDocument), PageRequest.of(page, size), 1);
        when(documentRepo.findByUploadedById(eq(testUserId), any(Pageable.class))).thenReturn(documentPage);

        try (MockedStatic<com.javajedis.legalconnect.common.utility.GetUserUtil> mockedUtil = org.mockito.Mockito.mockStatic(com.javajedis.legalconnect.common.utility.GetUserUtil.class)) {
            mockedUtil.when(() -> com.javajedis.legalconnect.common.utility.GetUserUtil.getAuthenticatedUser(eq(userRepo)))
                .thenReturn(testUser);

            // Act
            ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetService.getMyDocuments(page, size, sortDirection);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Documents retrieved successfully", result.getBody().getMessage());
        }
    }

    @Test
    void getMyDocuments_unauthenticated_returnsUnauthorized() {
        // Arrange
        try (MockedStatic<com.javajedis.legalconnect.common.utility.GetUserUtil> mockedUtil = org.mockito.Mockito.mockStatic(com.javajedis.legalconnect.common.utility.GetUserUtil.class)) {
            mockedUtil.when(() -> com.javajedis.legalconnect.common.utility.GetUserUtil.getAuthenticatedUser(eq(userRepo)))
                .thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetService.getMyDocuments(0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        }
    }

    @Test
    void getVisibleDocumentsForCurrentUser_success_returnsDocumentList() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortDirection = "DESC";
        Page<Document> documentPage = new PageImpl<>(List.of(testDocument), PageRequest.of(page, size), 1);
        when(documentRepo.findVisibleDocumentsForUser(eq(testUserId), eq(AssetPrivacy.SHARED), any(Pageable.class))).thenReturn(documentPage);

        try (MockedStatic<com.javajedis.legalconnect.common.utility.GetUserUtil> mockedUtil = org.mockito.Mockito.mockStatic(com.javajedis.legalconnect.common.utility.GetUserUtil.class)) {
            mockedUtil.when(() -> com.javajedis.legalconnect.common.utility.GetUserUtil.getAuthenticatedUser(eq(userRepo)))
                .thenReturn(testUser);

            // Act
            ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetService.getVisibleDocumentsForCurrentUser(page, size, sortDirection);

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("Documents retrieved successfully", result.getBody().getMessage());
        }
    }

    @Test
    void getVisibleDocumentsForCurrentUser_unauthenticated_returnsUnauthorized() {
        // Arrange
        try (MockedStatic<com.javajedis.legalconnect.common.utility.GetUserUtil> mockedUtil = org.mockito.Mockito.mockStatic(com.javajedis.legalconnect.common.utility.GetUserUtil.class)) {
            mockedUtil.when(() -> com.javajedis.legalconnect.common.utility.GetUserUtil.getAuthenticatedUser(eq(userRepo)))
                .thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetService.getVisibleDocumentsForCurrentUser(0, 10, "DESC");

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        }
    }
} 