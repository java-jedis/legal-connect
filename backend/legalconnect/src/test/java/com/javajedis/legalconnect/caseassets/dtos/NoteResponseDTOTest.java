package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

class NoteResponseDTOTest {

    private NoteResponseDTO noteResponseDTO;
    private UUID testCaseId;
    private UUID testNoteId;
    private UUID testOwnerId;

    @BeforeEach
    void setUp() {
        noteResponseDTO = new NoteResponseDTO();
        testCaseId = UUID.randomUUID();
        testNoteId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(noteResponseDTO);
        assertNull(noteResponseDTO.getCaseId());
        assertNull(noteResponseDTO.getCaseTitle());
        assertNull(noteResponseDTO.getNoteId());
        assertNull(noteResponseDTO.getOwnerId());
        assertNull(noteResponseDTO.getTitle());
        assertNull(noteResponseDTO.getContent());
        assertNull(noteResponseDTO.getPrivacy());
    }

    @Test
    void testAllArgsConstructor() {
        NoteResponseDTO dto = new NoteResponseDTO(
            testCaseId,
            "Test Case Title",
            testNoteId,
            testOwnerId,
            "Test Note Title",
            "Test note content",
            AssetPrivacy.SHARED
        );

        assertEquals(testCaseId, dto.getCaseId());
        assertEquals("Test Case Title", dto.getCaseTitle());
        assertEquals(testNoteId, dto.getNoteId());
        assertEquals(testOwnerId, dto.getOwnerId());
        assertEquals("Test Note Title", dto.getTitle());
        assertEquals("Test note content", dto.getContent());
        assertEquals(AssetPrivacy.SHARED, dto.getPrivacy());
    }

    @Test
    void testSettersAndGetters() {
        noteResponseDTO.setCaseId(testCaseId);
        noteResponseDTO.setCaseTitle("Updated Case Title");
        noteResponseDTO.setNoteId(testNoteId);
        noteResponseDTO.setOwnerId(testOwnerId);
        noteResponseDTO.setTitle("Updated Note Title");
        noteResponseDTO.setContent("Updated content");
        noteResponseDTO.setPrivacy(AssetPrivacy.PRIVATE);

        assertEquals(testCaseId, noteResponseDTO.getCaseId());
        assertEquals("Updated Case Title", noteResponseDTO.getCaseTitle());
        assertEquals(testNoteId, noteResponseDTO.getNoteId());
        assertEquals(testOwnerId, noteResponseDTO.getOwnerId());
        assertEquals("Updated Note Title", noteResponseDTO.getTitle());
        assertEquals("Updated content", noteResponseDTO.getContent());
        assertEquals(AssetPrivacy.PRIVATE, noteResponseDTO.getPrivacy());
    }

    @Test
    void testSettersWithNullValues() {
        noteResponseDTO.setCaseId(testCaseId);
        noteResponseDTO.setCaseTitle("Case Title");
        noteResponseDTO.setNoteId(testNoteId);
        noteResponseDTO.setOwnerId(testOwnerId);
        noteResponseDTO.setTitle("Note Title");
        noteResponseDTO.setContent("Content");
        noteResponseDTO.setPrivacy(AssetPrivacy.SHARED);

        // Set all to null
        noteResponseDTO.setCaseId(null);
        noteResponseDTO.setCaseTitle(null);
        noteResponseDTO.setNoteId(null);
        noteResponseDTO.setOwnerId(null);
        noteResponseDTO.setTitle(null);
        noteResponseDTO.setContent(null);
        noteResponseDTO.setPrivacy(null);

        assertNull(noteResponseDTO.getCaseId());
        assertNull(noteResponseDTO.getCaseTitle());
        assertNull(noteResponseDTO.getNoteId());
        assertNull(noteResponseDTO.getOwnerId());
        assertNull(noteResponseDTO.getTitle());
        assertNull(noteResponseDTO.getContent());
        assertNull(noteResponseDTO.getPrivacy());
    }

    @Test
    void testSettersWithEmptyValues() {
        noteResponseDTO.setCaseTitle("");
        noteResponseDTO.setTitle("");
        noteResponseDTO.setContent("");

        assertEquals("", noteResponseDTO.getCaseTitle());
        assertEquals("", noteResponseDTO.getTitle());
        assertEquals("", noteResponseDTO.getContent());
    }

    @Test
    void testPrivacyEnumValues() {
        // Test with PRIVATE
        noteResponseDTO.setPrivacy(AssetPrivacy.PRIVATE);
        assertEquals(AssetPrivacy.PRIVATE, noteResponseDTO.getPrivacy());

        // Test with SHARED
        noteResponseDTO.setPrivacy(AssetPrivacy.SHARED);
        assertEquals(AssetPrivacy.SHARED, noteResponseDTO.getPrivacy());
    }

    @Test
    void testEqualsAndHashCode() {
        NoteResponseDTO dto1 = new NoteResponseDTO(
            testCaseId,
            "Test Case",
            testNoteId,
            testOwnerId,
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );
        
        NoteResponseDTO dto2 = new NoteResponseDTO(
            testCaseId,
            "Test Case",
            testNoteId,
            testOwnerId,
            "Test Title",
            "Test content",
            AssetPrivacy.SHARED
        );
        
        NoteResponseDTO dto3 = new NoteResponseDTO(
            UUID.randomUUID(),
            "Different Case",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Different Title",
            "Different content",
            AssetPrivacy.PRIVATE
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        noteResponseDTO.setCaseId(testCaseId);
        noteResponseDTO.setCaseTitle("Test Case");
        noteResponseDTO.setNoteId(testNoteId);
        noteResponseDTO.setOwnerId(testOwnerId);
        noteResponseDTO.setTitle("Test Note");
        noteResponseDTO.setContent("Test content");
        noteResponseDTO.setPrivacy(AssetPrivacy.SHARED);

        String toString = noteResponseDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes all fields
        assert toString.contains("Test Case");
        assert toString.contains("Test Note");
        assert toString.contains("Test content");
        assert toString.contains("SHARED");
    }

    @Test
    void testWithLongStrings() {
        String longCaseTitle = "C".repeat(1000);
        String longTitle = "T".repeat(1000);
        String longContent = "D".repeat(5000);

        noteResponseDTO.setCaseTitle(longCaseTitle);
        noteResponseDTO.setTitle(longTitle);
        noteResponseDTO.setContent(longContent);

        assertEquals(longCaseTitle, noteResponseDTO.getCaseTitle());
        assertEquals(longTitle, noteResponseDTO.getTitle());
        assertEquals(longContent, noteResponseDTO.getContent());
    }

    @Test
    void testBuilderPattern() {
        // Testing that the DTO can be constructed and modified in a builder-like pattern
        NoteResponseDTO dto = new NoteResponseDTO();
        dto.setCaseId(testCaseId);
        dto.setCaseTitle("Case");
        dto.setNoteId(testNoteId);
        dto.setOwnerId(testOwnerId);
        dto.setTitle("Note");
        dto.setContent("Content");
        dto.setPrivacy(AssetPrivacy.PRIVATE);

        // Verify all fields are set correctly
        assertEquals(testCaseId, dto.getCaseId());
        assertEquals("Case", dto.getCaseTitle());
        assertEquals(testNoteId, dto.getNoteId());
        assertEquals(testOwnerId, dto.getOwnerId());
        assertEquals("Note", dto.getTitle());
        assertEquals("Content", dto.getContent());
        assertEquals(AssetPrivacy.PRIVATE, dto.getPrivacy());
    }
} 