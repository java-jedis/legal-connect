package com.javajedis.legalconnect.caseassets.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.caseassets.AssetPrivacy;

class NoteListResponseDTOTest {

    private NoteListResponseDTO noteListResponseDTO;
    private List<NoteResponseDTO> testNotes;

    @BeforeEach
    void setUp() {
        noteListResponseDTO = new NoteListResponseDTO();
        testNotes = createTestNotes();
    }

    private List<NoteResponseDTO> createTestNotes() {
        List<NoteResponseDTO> notes = new ArrayList<>();
        
        notes.add(new NoteResponseDTO(
            UUID.randomUUID(),
            "Case 1",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Note 1",
            "Content 1",
            AssetPrivacy.SHARED
        ));
        
        notes.add(new NoteResponseDTO(
            UUID.randomUUID(),
            "Case 2",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Note 2",
            "Content 2",
            AssetPrivacy.PRIVATE
        ));
        
        return notes;
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(noteListResponseDTO);
        assertNull(noteListResponseDTO.getNotes());
    }

    @Test
    void testAllArgsConstructor() {
        NoteListResponseDTO dto = new NoteListResponseDTO(testNotes);
        
        assertEquals(testNotes, dto.getNotes());
        assertEquals(2, dto.getNotes().size());
    }

    @Test
    void testSettersAndGetters() {
        noteListResponseDTO.setNotes(testNotes);
        
        assertEquals(testNotes, noteListResponseDTO.getNotes());
        assertEquals(2, noteListResponseDTO.getNotes().size());
    }

    @Test
    void testWithEmptyList() {
        List<NoteResponseDTO> emptyList = new ArrayList<>();
        noteListResponseDTO.setNotes(emptyList);
        
        assertEquals(emptyList, noteListResponseDTO.getNotes());
        assertEquals(0, noteListResponseDTO.getNotes().size());
        assertTrue(noteListResponseDTO.getNotes().isEmpty());
    }

    @Test
    void testWithNullList() {
        noteListResponseDTO.setNotes(null);
        
        assertNull(noteListResponseDTO.getNotes());
    }

    @Test
    void testWithSingleNote() {
        List<NoteResponseDTO> singleNote = Arrays.asList(testNotes.get(0));
        noteListResponseDTO.setNotes(singleNote);
        
        assertEquals(singleNote, noteListResponseDTO.getNotes());
        assertEquals(1, noteListResponseDTO.getNotes().size());
    }

    @Test
    void testWithLargeList() {
        List<NoteResponseDTO> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add(new NoteResponseDTO(
                UUID.randomUUID(),
                "Case " + i,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Note " + i,
                "Content " + i,
                i % 2 == 0 ? AssetPrivacy.SHARED : AssetPrivacy.PRIVATE
            ));
        }
        
        noteListResponseDTO.setNotes(largeList);
        
        assertEquals(largeList, noteListResponseDTO.getNotes());
        assertEquals(100, noteListResponseDTO.getNotes().size());
    }

    @Test
    void testEqualsAndHashCode() {
        NoteListResponseDTO dto1 = new NoteListResponseDTO(testNotes);
        NoteListResponseDTO dto2 = new NoteListResponseDTO(testNotes);
        NoteListResponseDTO dto3 = new NoteListResponseDTO(Collections.emptyList());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        noteListResponseDTO.setNotes(testNotes);
        
        String toString = noteListResponseDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("notes"));
    }

    @Test
    void testModifyingListAfterSetting() {
        List<NoteResponseDTO> modifiableList = new ArrayList<>(testNotes);
        noteListResponseDTO.setNotes(modifiableList);
        
        // Modify the original list
        modifiableList.add(new NoteResponseDTO(
            UUID.randomUUID(),
            "New Case",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "New Note",
            "New Content",
            AssetPrivacy.SHARED
        ));
        
        // The DTO should reflect the change (since it's the same list reference)
        assertEquals(3, noteListResponseDTO.getNotes().size());
    }

    @Test
    void testWithImmutableList() {
        List<NoteResponseDTO> immutableList = Collections.unmodifiableList(testNotes);
        noteListResponseDTO.setNotes(immutableList);
        
        assertEquals(immutableList, noteListResponseDTO.getNotes());
        assertEquals(2, noteListResponseDTO.getNotes().size());
    }

    @Test
    void testEqualsWithNullLists() {
        NoteListResponseDTO dto1 = new NoteListResponseDTO();
        NoteListResponseDTO dto2 = new NoteListResponseDTO();
        
        // Both have null lists
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // One has null, other has empty list
        dto2.setNotes(Collections.emptyList());
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testWithMixedPrivacyNotes() {
        List<NoteResponseDTO> mixedNotes = Arrays.asList(
            new NoteResponseDTO(
                UUID.randomUUID(), "Case 1", UUID.randomUUID(), UUID.randomUUID(),
                "Note 1", "Content 1", AssetPrivacy.SHARED
            ),
            new NoteResponseDTO(
                UUID.randomUUID(), "Case 2", UUID.randomUUID(), UUID.randomUUID(),
                "Note 2", "Content 2", AssetPrivacy.PRIVATE
            )
        );
        
        noteListResponseDTO.setNotes(mixedNotes);
        
        assertEquals(2, noteListResponseDTO.getNotes().size());
        assertEquals(AssetPrivacy.SHARED, noteListResponseDTO.getNotes().get(0).getPrivacy());
        assertEquals(AssetPrivacy.PRIVATE, noteListResponseDTO.getNotes().get(1).getPrivacy());
    }

    @Test
    void testWithNotesFromDifferentCases() {
        UUID case1Id = UUID.randomUUID();
        UUID case2Id = UUID.randomUUID();
        
        List<NoteResponseDTO> multiCaseNotes = Arrays.asList(
            new NoteResponseDTO(
                case1Id, "Case 1 Title", UUID.randomUUID(), UUID.randomUUID(),
                "Note from Case 1", "Content 1", AssetPrivacy.SHARED
            ),
            new NoteResponseDTO(
                case2Id, "Case 2 Title", UUID.randomUUID(), UUID.randomUUID(),
                "Note from Case 2", "Content 2", AssetPrivacy.PRIVATE
            )
        );
        
        noteListResponseDTO.setNotes(multiCaseNotes);
        
        assertEquals(2, noteListResponseDTO.getNotes().size());
        assertEquals(case1Id, noteListResponseDTO.getNotes().get(0).getCaseId());
        assertEquals(case2Id, noteListResponseDTO.getNotes().get(1).getCaseId());
        assertEquals("Case 1 Title", noteListResponseDTO.getNotes().get(0).getCaseTitle());
        assertEquals("Case 2 Title", noteListResponseDTO.getNotes().get(1).getCaseTitle());
    }

    @Test
    void testWithNotesFromDifferentOwners() {
        UUID owner1Id = UUID.randomUUID();
        UUID owner2Id = UUID.randomUUID();
        
        List<NoteResponseDTO> multiOwnerNotes = Arrays.asList(
            new NoteResponseDTO(
                UUID.randomUUID(), "Case 1", UUID.randomUUID(), owner1Id,
                "Note by Owner 1", "Content 1", AssetPrivacy.SHARED
            ),
            new NoteResponseDTO(
                UUID.randomUUID(), "Case 2", UUID.randomUUID(), owner2Id,
                "Note by Owner 2", "Content 2", AssetPrivacy.PRIVATE
            )
        );
        
        noteListResponseDTO.setNotes(multiOwnerNotes);
        
        assertEquals(2, noteListResponseDTO.getNotes().size());
        assertEquals(owner1Id, noteListResponseDTO.getNotes().get(0).getOwnerId());
        assertEquals(owner2Id, noteListResponseDTO.getNotes().get(1).getOwnerId());
    }
} 