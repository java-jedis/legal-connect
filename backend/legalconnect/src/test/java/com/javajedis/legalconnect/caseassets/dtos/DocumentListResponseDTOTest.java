package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

class DocumentListResponseDTOTest {

    private DocumentListResponseDTO documentListResponseDTO;
    private List<DocumentResponseDTO> testDocuments;

    @BeforeEach
    void setUp() {
        documentListResponseDTO = new DocumentListResponseDTO();
        testDocuments = createTestDocuments();
    }

    private List<DocumentResponseDTO> createTestDocuments() {
        List<DocumentResponseDTO> documents = new ArrayList<>();
        
        documents.add(new DocumentResponseDTO(
            UUID.randomUUID(),
            "Case 1",
            UUID.randomUUID(),
            "Document 1",
            "Description 1",
            UUID.randomUUID(),
            "User 1",
            AssetPrivacy.SHARED,
            OffsetDateTime.now()
        ));
        
        documents.add(new DocumentResponseDTO(
            UUID.randomUUID(),
            "Case 2",
            UUID.randomUUID(),
            "Document 2",
            "Description 2",
            UUID.randomUUID(),
            "User 2",
            AssetPrivacy.PRIVATE,
            OffsetDateTime.now()
        ));
        
        return documents;
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(documentListResponseDTO);
        assertNull(documentListResponseDTO.getDocuments());
    }

    @Test
    void testAllArgsConstructor() {
        DocumentListResponseDTO dto = new DocumentListResponseDTO(testDocuments);
        
        assertEquals(testDocuments, dto.getDocuments());
        assertEquals(2, dto.getDocuments().size());
    }

    @Test
    void testSettersAndGetters() {
        documentListResponseDTO.setDocuments(testDocuments);
        
        assertEquals(testDocuments, documentListResponseDTO.getDocuments());
        assertEquals(2, documentListResponseDTO.getDocuments().size());
    }

    @Test
    void testWithEmptyList() {
        List<DocumentResponseDTO> emptyList = new ArrayList<>();
        documentListResponseDTO.setDocuments(emptyList);
        
        assertEquals(emptyList, documentListResponseDTO.getDocuments());
        assertEquals(0, documentListResponseDTO.getDocuments().size());
        assertTrue(documentListResponseDTO.getDocuments().isEmpty());
    }

    @Test
    void testWithNullList() {
        documentListResponseDTO.setDocuments(null);
        
        assertNull(documentListResponseDTO.getDocuments());
    }

    @Test
    void testWithSingleDocument() {
        List<DocumentResponseDTO> singleDocument = Arrays.asList(testDocuments.get(0));
        documentListResponseDTO.setDocuments(singleDocument);
        
        assertEquals(singleDocument, documentListResponseDTO.getDocuments());
        assertEquals(1, documentListResponseDTO.getDocuments().size());
    }

    @Test
    void testWithLargeList() {
        List<DocumentResponseDTO> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add(new DocumentResponseDTO(
                UUID.randomUUID(),
                "Case " + i,
                UUID.randomUUID(),
                "Document " + i,
                "Description " + i,
                UUID.randomUUID(),
                "User " + i,
                i % 2 == 0 ? AssetPrivacy.SHARED : AssetPrivacy.PRIVATE,
                OffsetDateTime.now()
            ));
        }
        
        documentListResponseDTO.setDocuments(largeList);
        
        assertEquals(largeList, documentListResponseDTO.getDocuments());
        assertEquals(100, documentListResponseDTO.getDocuments().size());
    }

    @Test
    void testEqualsAndHashCode() {
        DocumentListResponseDTO dto1 = new DocumentListResponseDTO(testDocuments);
        DocumentListResponseDTO dto2 = new DocumentListResponseDTO(testDocuments);
        DocumentListResponseDTO dto3 = new DocumentListResponseDTO(Collections.emptyList());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        documentListResponseDTO.setDocuments(testDocuments);
        
        String toString = documentListResponseDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("documents"));
    }

    @Test
    void testModifyingListAfterSetting() {
        List<DocumentResponseDTO> modifiableList = new ArrayList<>(testDocuments);
        documentListResponseDTO.setDocuments(modifiableList);
        
        // Modify the original list
        modifiableList.add(new DocumentResponseDTO(
            UUID.randomUUID(),
            "New Case",
            UUID.randomUUID(),
            "New Document",
            "New Description",
            UUID.randomUUID(),
            "New User",
            AssetPrivacy.SHARED,
            OffsetDateTime.now()
        ));
        
        // The DTO should reflect the change (since it's the same list reference)
        assertEquals(3, documentListResponseDTO.getDocuments().size());
    }

    @Test
    void testWithImmutableList() {
        List<DocumentResponseDTO> immutableList = Collections.unmodifiableList(testDocuments);
        documentListResponseDTO.setDocuments(immutableList);
        
        assertEquals(immutableList, documentListResponseDTO.getDocuments());
        assertEquals(2, documentListResponseDTO.getDocuments().size());
    }

    @Test
    void testEqualsWithNullLists() {
        DocumentListResponseDTO dto1 = new DocumentListResponseDTO();
        DocumentListResponseDTO dto2 = new DocumentListResponseDTO();
        
        // Both have null lists
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // One has null, other has empty list
        dto2.setDocuments(Collections.emptyList());
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testWithMixedPrivacyDocuments() {
        List<DocumentResponseDTO> mixedDocuments = Arrays.asList(
            new DocumentResponseDTO(
                UUID.randomUUID(), "Case 1", UUID.randomUUID(), "Doc 1", "Desc 1",
                UUID.randomUUID(), "User 1", AssetPrivacy.SHARED, OffsetDateTime.now()
            ),
            new DocumentResponseDTO(
                UUID.randomUUID(), "Case 2", UUID.randomUUID(), "Doc 2", "Desc 2",
                UUID.randomUUID(), "User 2", AssetPrivacy.PRIVATE, OffsetDateTime.now()
            )
        );
        
        documentListResponseDTO.setDocuments(mixedDocuments);
        
        assertEquals(2, documentListResponseDTO.getDocuments().size());
        assertEquals(AssetPrivacy.SHARED, documentListResponseDTO.getDocuments().get(0).getPrivacy());
        assertEquals(AssetPrivacy.PRIVATE, documentListResponseDTO.getDocuments().get(1).getPrivacy());
    }
} 