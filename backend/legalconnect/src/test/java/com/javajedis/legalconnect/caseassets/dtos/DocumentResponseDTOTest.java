package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

class DocumentResponseDTOTest {

    private DocumentResponseDTO documentResponseDTO;
    private UUID testCaseId;
    private UUID testDocumentId;
    private UUID testUploadedById;
    private OffsetDateTime testDateTime;

    @BeforeEach
    void setUp() {
        documentResponseDTO = new DocumentResponseDTO();
        testCaseId = UUID.randomUUID();
        testDocumentId = UUID.randomUUID();
        testUploadedById = UUID.randomUUID();
        testDateTime = OffsetDateTime.now();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(documentResponseDTO);
        assertNull(documentResponseDTO.getCaseId());
        assertNull(documentResponseDTO.getCaseTitle());
        assertNull(documentResponseDTO.getDocumentId());
        assertNull(documentResponseDTO.getTitle());
        assertNull(documentResponseDTO.getDescription());
        assertNull(documentResponseDTO.getUploadedById());
        assertNull(documentResponseDTO.getUploadedByName());
        assertNull(documentResponseDTO.getPrivacy());
        assertNull(documentResponseDTO.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        DocumentResponseDTO dto = new DocumentResponseDTO(
            testCaseId,
            "Test Case Title",
            testDocumentId,
            "Test Document Title",
            "Test document description",
            testUploadedById,
            "John Doe",
            AssetPrivacy.SHARED,
            testDateTime
        );

        assertEquals(testCaseId, dto.getCaseId());
        assertEquals("Test Case Title", dto.getCaseTitle());
        assertEquals(testDocumentId, dto.getDocumentId());
        assertEquals("Test Document Title", dto.getTitle());
        assertEquals("Test document description", dto.getDescription());
        assertEquals(testUploadedById, dto.getUploadedById());
        assertEquals("John Doe", dto.getUploadedByName());
        assertEquals(AssetPrivacy.SHARED, dto.getPrivacy());
        assertEquals(testDateTime, dto.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        documentResponseDTO.setCaseId(testCaseId);
        documentResponseDTO.setCaseTitle("Updated Case Title");
        documentResponseDTO.setDocumentId(testDocumentId);
        documentResponseDTO.setTitle("Updated Document Title");
        documentResponseDTO.setDescription("Updated description");
        documentResponseDTO.setUploadedById(testUploadedById);
        documentResponseDTO.setUploadedByName("Jane Smith");
        documentResponseDTO.setPrivacy(AssetPrivacy.PRIVATE);
        documentResponseDTO.setCreatedAt(testDateTime);

        assertEquals(testCaseId, documentResponseDTO.getCaseId());
        assertEquals("Updated Case Title", documentResponseDTO.getCaseTitle());
        assertEquals(testDocumentId, documentResponseDTO.getDocumentId());
        assertEquals("Updated Document Title", documentResponseDTO.getTitle());
        assertEquals("Updated description", documentResponseDTO.getDescription());
        assertEquals(testUploadedById, documentResponseDTO.getUploadedById());
        assertEquals("Jane Smith", documentResponseDTO.getUploadedByName());
        assertEquals(AssetPrivacy.PRIVATE, documentResponseDTO.getPrivacy());
        assertEquals(testDateTime, documentResponseDTO.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        DocumentResponseDTO dto1 = new DocumentResponseDTO(
            testCaseId,
            "Test Case",
            testDocumentId,
            "Test Title",
            "Test description",
            testUploadedById,
            "John Doe",
            AssetPrivacy.SHARED,
            testDateTime
        );
        
        DocumentResponseDTO dto2 = new DocumentResponseDTO(
            testCaseId,
            "Test Case",
            testDocumentId,
            "Test Title",
            "Test description",
            testUploadedById,
            "John Doe",
            AssetPrivacy.SHARED,
            testDateTime
        );
        
        DocumentResponseDTO dto3 = new DocumentResponseDTO(
            UUID.randomUUID(),
            "Different Case",
            UUID.randomUUID(),
            "Different Title",
            "Different description",
            UUID.randomUUID(),
            "Jane Smith",
            AssetPrivacy.PRIVATE,
            OffsetDateTime.now()
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        documentResponseDTO.setCaseId(testCaseId);
        documentResponseDTO.setCaseTitle("Test Case");
        documentResponseDTO.setDocumentId(testDocumentId);
        documentResponseDTO.setTitle("Test Document");
        documentResponseDTO.setDescription("Test description");
        documentResponseDTO.setUploadedById(testUploadedById);
        documentResponseDTO.setUploadedByName("John Doe");
        documentResponseDTO.setPrivacy(AssetPrivacy.SHARED);
        documentResponseDTO.setCreatedAt(testDateTime);

        String toString = documentResponseDTO.toString();
        assertNotNull(toString);
        assert toString.contains("Test Case");
        assert toString.contains("Test Document");
        assert toString.contains("Test description");
        assert toString.contains("John Doe");
        assert toString.contains("SHARED");
    }
} 