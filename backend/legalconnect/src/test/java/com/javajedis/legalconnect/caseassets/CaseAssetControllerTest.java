package com.javajedis.legalconnect.caseassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.javajedis.legalconnect.caseassets.dtos.CreateDocumentDTO;
import com.javajedis.legalconnect.caseassets.dtos.CreateNoteDTO;
import com.javajedis.legalconnect.caseassets.dtos.DocumentListResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.DocumentResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.NoteListResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.NoteResponseDTO;
import com.javajedis.legalconnect.caseassets.dtos.UpdateDocumentDTO;
import com.javajedis.legalconnect.caseassets.dtos.UpdateNoteDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;

class CaseAssetControllerTest {

    @Mock
    private CaseAssetService caseAssetService;

    @InjectMocks
    private CaseAssetController caseAssetController;

    private CreateNoteDTO testCreateNoteDTO;
    private UpdateNoteDTO testUpdateNoteDTO;
    private NoteResponseDTO testNoteResponseDTO;
    private NoteListResponseDTO testNoteListResponseDTO;
    private UpdateDocumentDTO testUpdateDocumentDTO;
    private DocumentResponseDTO testDocumentResponseDTO;
    private DocumentListResponseDTO testDocumentListResponseDTO;
    private MockMultipartFile testMultipartFile;

    private UUID testCaseId;
    private UUID testNoteId;
    private UUID testDocumentId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test IDs
        testCaseId = UUID.randomUUID();
        testNoteId = UUID.randomUUID();
        testDocumentId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        // Setup test DTOs for Notes
        testCreateNoteDTO = new CreateNoteDTO(testCaseId, "Test Note Title", "Test note content", AssetPrivacy.SHARED);

        testUpdateNoteDTO = new UpdateNoteDTO();
        testUpdateNoteDTO.setTitle("Updated Note Title");
        testUpdateNoteDTO.setContent("Updated note content");
        testUpdateNoteDTO.setPrivacy(AssetPrivacy.PRIVATE);

        testNoteResponseDTO = new NoteResponseDTO();
        testNoteResponseDTO.setNoteId(testNoteId);
        testNoteResponseDTO.setCaseId(testCaseId);
        testNoteResponseDTO.setTitle("Test Note Title");
        testNoteResponseDTO.setContent("Test note content");
        testNoteResponseDTO.setPrivacy(AssetPrivacy.SHARED);
        testNoteResponseDTO.setOwnerId(testUserId);

        testNoteListResponseDTO = new NoteListResponseDTO();
        testNoteListResponseDTO.setNotes(List.of(testNoteResponseDTO));

        // Setup test DTOs for Documents
        testUpdateDocumentDTO = new UpdateDocumentDTO();
        testUpdateDocumentDTO.setTitle("Updated Document Title");
        testUpdateDocumentDTO.setDescription("Updated document description");
        testUpdateDocumentDTO.setPrivacy(AssetPrivacy.PRIVATE);

        testDocumentResponseDTO = new DocumentResponseDTO();
        testDocumentResponseDTO.setDocumentId(testDocumentId);
        testDocumentResponseDTO.setCaseId(testCaseId);
        testDocumentResponseDTO.setTitle("Test Document Title");
        testDocumentResponseDTO.setDescription("Test document description");
        testDocumentResponseDTO.setPrivacy(AssetPrivacy.SHARED);
        testDocumentResponseDTO.setUploadedByName("uploader");
        testDocumentResponseDTO.setCreatedAt(OffsetDateTime.now());

        testDocumentListResponseDTO = new DocumentListResponseDTO();
        testDocumentListResponseDTO.setDocuments(List.of(testDocumentResponseDTO));

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
    void createNote_returnsSuccess() {
        // Arrange
        ApiResponse<NoteResponseDTO> apiResponse = ApiResponse.success(testNoteResponseDTO, HttpStatus.CREATED, "Note created successfully").getBody();
        when(caseAssetService.createNote(any(CreateNoteDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetController.createNote(testCreateNoteDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testNoteResponseDTO, result.getBody().getData());
        assertEquals("Note created successfully", result.getBody().getMessage());
        verify(caseAssetService).createNote(testCreateNoteDTO);
    }

    @Test
    void createNote_withError_returnsError() {
        // Arrange
        ApiResponse<NoteResponseDTO> apiResponse = ApiResponse.<NoteResponseDTO>error("Case not found", HttpStatus.NOT_FOUND).getBody();
        when(caseAssetService.createNote(any(CreateNoteDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetController.createNote(testCreateNoteDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("Case not found", result.getBody().getError().getMessage());
        verify(caseAssetService).createNote(testCreateNoteDTO);
    }

    @Test
    void updateNote_returnsSuccess() {
        // Arrange
        ApiResponse<NoteResponseDTO> apiResponse = ApiResponse.success(testNoteResponseDTO, HttpStatus.OK, "Note updated successfully").getBody();
        when(caseAssetService.updateNote(eq(testNoteId), any(UpdateNoteDTO.class)))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetController.updateNote(testNoteId, testUpdateNoteDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testNoteResponseDTO, result.getBody().getData());
        assertEquals("Note updated successfully", result.getBody().getMessage());
        verify(caseAssetService).updateNote(testNoteId, testUpdateNoteDTO);
    }

    @Test
    void updateNote_withError_returnsError() {
        // Arrange
        ApiResponse<NoteResponseDTO> apiResponse = ApiResponse.<NoteResponseDTO>error("You don't have permission to update this note", HttpStatus.FORBIDDEN).getBody();
        when(caseAssetService.updateNote(eq(testNoteId), any(UpdateNoteDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetController.updateNote(testNoteId, testUpdateNoteDTO);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("You don't have permission to update this note", result.getBody().getError().getMessage());
        verify(caseAssetService).updateNote(testNoteId, testUpdateNoteDTO);
    }

    @Test
    void deleteNote_returnsSuccess() {
        // Arrange
        ApiResponse<String> apiResponse = ApiResponse.success("Note deleted successfully", HttpStatus.OK, "Note deleted successfully").getBody();
        when(caseAssetService.deleteNote(testNoteId))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = caseAssetController.deleteNote(testNoteId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Note deleted successfully", result.getBody().getData());
        assertEquals("Note deleted successfully", result.getBody().getMessage());
        verify(caseAssetService).deleteNote(testNoteId);
    }

    @Test
    void deleteNote_withError_returnsError() {
        // Arrange
        ApiResponse<String> apiResponse = ApiResponse.<String>error("Note not found", HttpStatus.NOT_FOUND).getBody();
        when(caseAssetService.deleteNote(testNoteId))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = caseAssetController.deleteNote(testNoteId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("Note not found", result.getBody().getError().getMessage());
        verify(caseAssetService).deleteNote(testNoteId);
    }

    @Test
    void getNoteById_returnsSuccess() {
        // Arrange
        ApiResponse<NoteResponseDTO> apiResponse = ApiResponse.success(testNoteResponseDTO, HttpStatus.OK, "Note retrieved successfully").getBody();
        when(caseAssetService.getNoteById(testNoteId))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetController.getNoteById(testNoteId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testNoteResponseDTO, result.getBody().getData());
        assertEquals("Note retrieved successfully", result.getBody().getMessage());
        verify(caseAssetService).getNoteById(testNoteId);
    }

    @Test
    void getNoteById_withError_returnsError() {
        // Arrange
        ApiResponse<NoteResponseDTO> apiResponse = ApiResponse.<NoteResponseDTO>error("Note not found", HttpStatus.NOT_FOUND).getBody();
        when(caseAssetService.getNoteById(testNoteId))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteResponseDTO>> result = caseAssetController.getNoteById(testNoteId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("Note not found", result.getBody().getError().getMessage());
        verify(caseAssetService).getNoteById(testNoteId);
    }

    @Test
    void getAllNotesForCase_returnsSuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortDirection = "DESC";
        ApiResponse<NoteListResponseDTO> apiResponse = ApiResponse.success(testNoteListResponseDTO, HttpStatus.OK, "Notes retrieved successfully").getBody();
        when(caseAssetService.getAllNotesForCase(testCaseId, page, size, sortDirection))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteListResponseDTO>> result = caseAssetController.getAllNotesForCase(testCaseId, page, size, sortDirection);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testNoteListResponseDTO, result.getBody().getData());
        assertEquals("Notes retrieved successfully", result.getBody().getMessage());
        verify(caseAssetService).getAllNotesForCase(testCaseId, page, size, sortDirection);
    }

    @Test
    void getAllNotesForCase_withDefaultParams_returnsSuccess() {
        // Arrange
        ApiResponse<NoteListResponseDTO> apiResponse = ApiResponse.success(testNoteListResponseDTO, HttpStatus.OK, "Notes retrieved successfully").getBody();
        when(caseAssetService.getAllNotesForCase(testCaseId, 0, 10, "DESC"))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<NoteListResponseDTO>> result = caseAssetController.getAllNotesForCase(testCaseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testNoteListResponseDTO, result.getBody().getData());
        verify(caseAssetService).getAllNotesForCase(testCaseId, 0, 10, "DESC");
    }

    // ================== DOCUMENT TESTS ==================

    @Test
    void uploadDocument_returnsSuccess() {
        // Arrange
        ApiResponse<DocumentResponseDTO> apiResponse = ApiResponse.success(testDocumentResponseDTO, HttpStatus.CREATED, "Document uploaded successfully").getBody();
        when(caseAssetService.uploadDocument(any(CreateDocumentDTO.class), eq(testMultipartFile)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetController.uploadDocument(
            testCaseId, "Test Document Title", "Test document description", AssetPrivacy.SHARED, testMultipartFile);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testDocumentResponseDTO, result.getBody().getData());
        assertEquals("Document uploaded successfully", result.getBody().getMessage());
        verify(caseAssetService).uploadDocument(any(CreateDocumentDTO.class), eq(testMultipartFile));
    }

    @Test
    void uploadDocument_withError_returnsError() {
        // Arrange
        ApiResponse<DocumentResponseDTO> apiResponse = ApiResponse.<DocumentResponseDTO>error("File size exceeds 10MB limit", HttpStatus.BAD_REQUEST).getBody();
        when(caseAssetService.uploadDocument(any(CreateDocumentDTO.class), eq(testMultipartFile)))
            .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetController.uploadDocument(
            testCaseId, "Test Document Title", "Test document description", AssetPrivacy.SHARED, testMultipartFile);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("File size exceeds 10MB limit", result.getBody().getError().getMessage());
        verify(caseAssetService).uploadDocument(any(CreateDocumentDTO.class), eq(testMultipartFile));
    }

    @Test
    void updateDocument_returnsSuccess() {
        // Arrange
        ApiResponse<DocumentResponseDTO> apiResponse = ApiResponse.success(testDocumentResponseDTO, HttpStatus.OK, "Document updated successfully").getBody();
        when(caseAssetService.updateDocument(eq(testDocumentId), any(UpdateDocumentDTO.class)))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetController.updateDocument(testDocumentId, testUpdateDocumentDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testDocumentResponseDTO, result.getBody().getData());
        assertEquals("Document updated successfully", result.getBody().getMessage());
        verify(caseAssetService).updateDocument(testDocumentId, testUpdateDocumentDTO);
    }

    @Test
    void updateDocument_withError_returnsError() {
        // Arrange
        ApiResponse<DocumentResponseDTO> apiResponse = ApiResponse.<DocumentResponseDTO>error("You don't have permission to update this document", HttpStatus.FORBIDDEN).getBody();
        when(caseAssetService.updateDocument(eq(testDocumentId), any(UpdateDocumentDTO.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentResponseDTO>> result = caseAssetController.updateDocument(testDocumentId, testUpdateDocumentDTO);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("You don't have permission to update this document", result.getBody().getError().getMessage());
        verify(caseAssetService).updateDocument(testDocumentId, testUpdateDocumentDTO);
    }

    @Test
    void viewDocument_returnsSuccess() {
        // Arrange
        byte[] fileContent = "test document content".getBytes();
        when(caseAssetService.viewDocument(testDocumentId))
            .thenReturn(ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=test-document.pdf")
                .body(fileContent));

        // Act
        ResponseEntity<byte[]> result = caseAssetController.viewDocument(testDocumentId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(fileContent, result.getBody());
        verify(caseAssetService).viewDocument(testDocumentId);
    }

    @Test
    void viewDocument_withError_returnsError() {
        // Arrange
        when(caseAssetService.viewDocument(testDocumentId))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));

        // Act
        ResponseEntity<byte[]> result = caseAssetController.viewDocument(testDocumentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(caseAssetService).viewDocument(testDocumentId);
    }

    @Test
    void deleteDocument_returnsSuccess() {
        // Arrange
        ApiResponse<String> apiResponse = ApiResponse.success("Document deleted successfully", HttpStatus.OK, "Document deleted successfully").getBody();
        when(caseAssetService.deleteDocument(testDocumentId))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = caseAssetController.deleteDocument(testDocumentId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Document deleted successfully", result.getBody().getData());
        assertEquals("Document deleted successfully", result.getBody().getMessage());
        verify(caseAssetService).deleteDocument(testDocumentId);
    }

    @Test
    void deleteDocument_withError_returnsError() {
        // Arrange
        ApiResponse<String> apiResponse = ApiResponse.<String>error("Document not found", HttpStatus.NOT_FOUND).getBody();
        when(caseAssetService.deleteDocument(testDocumentId))
            .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<String>> result = caseAssetController.deleteDocument(testDocumentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("Document not found", result.getBody().getError().getMessage());
        verify(caseAssetService).deleteDocument(testDocumentId);
    }

    @Test
    void getAllDocumentsForCase_returnsSuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortDirection = "DESC";
        ApiResponse<DocumentListResponseDTO> apiResponse = ApiResponse.success(testDocumentListResponseDTO, HttpStatus.OK, "Documents retrieved successfully").getBody();
        when(caseAssetService.getAllDocumentsForCase(testCaseId, page, size, sortDirection))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetController.getAllDocumentsForCase(testCaseId, page, size, sortDirection);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testDocumentListResponseDTO, result.getBody().getData());
        assertEquals("Documents retrieved successfully", result.getBody().getMessage());
        verify(caseAssetService).getAllDocumentsForCase(testCaseId, page, size, sortDirection);
    }

    @Test
    void getAllDocumentsForCase_withDefaultParams_returnsSuccess() {
        // Arrange
        ApiResponse<DocumentListResponseDTO> apiResponse = ApiResponse.success(testDocumentListResponseDTO, HttpStatus.OK, "Documents retrieved successfully").getBody();
        when(caseAssetService.getAllDocumentsForCase(testCaseId, 0, 10, "DESC"))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetController.getAllDocumentsForCase(testCaseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testDocumentListResponseDTO, result.getBody().getData());
        verify(caseAssetService).getAllDocumentsForCase(testCaseId, 0, 10, "DESC");
    }

    @Test
    void getAllDocumentsForCase_withError_returnsError() {
        // Arrange
        ApiResponse<DocumentListResponseDTO> apiResponse = ApiResponse.<DocumentListResponseDTO>error("You don't have permission to access this case", HttpStatus.FORBIDDEN).getBody();
        when(caseAssetService.getAllDocumentsForCase(testCaseId, 0, 10, "DESC"))
            .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetController.getAllDocumentsForCase(testCaseId, 0, 10, "DESC");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getError());
        assertEquals("You don't have permission to access this case", result.getBody().getError().getMessage());
        verify(caseAssetService).getAllDocumentsForCase(testCaseId, 0, 10, "DESC");
    }

    @Test
    void getMyDocuments_returnsSuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortDirection = "DESC";
        ApiResponse<DocumentListResponseDTO> apiResponse = ApiResponse.success(testDocumentListResponseDTO, HttpStatus.OK, "Documents retrieved successfully").getBody();
        when(caseAssetService.getMyDocuments(page, size, sortDirection))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetController.getMyDocuments(page, size, sortDirection);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testDocumentListResponseDTO, result.getBody().getData());
        assertEquals("Documents retrieved successfully", result.getBody().getMessage());
        verify(caseAssetService).getMyDocuments(page, size, sortDirection);
    }

    @Test
    void getVisibleDocuments_returnsSuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortDirection = "DESC";
        ApiResponse<DocumentListResponseDTO> apiResponse = ApiResponse.success(testDocumentListResponseDTO, HttpStatus.OK, "Documents retrieved successfully").getBody();
        when(caseAssetService.getVisibleDocumentsForCurrentUser(page, size, sortDirection))
            .thenReturn(ResponseEntity.ok(apiResponse));

        // Act
        ResponseEntity<ApiResponse<DocumentListResponseDTO>> result = caseAssetController.getVisibleDocuments(page, size, sortDirection);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(testDocumentListResponseDTO, result.getBody().getData());
        assertEquals("Documents retrieved successfully", result.getBody().getMessage());
        verify(caseAssetService).getVisibleDocumentsForCurrentUser(page, size, sortDirection);
    }
} 