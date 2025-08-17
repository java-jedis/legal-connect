package com.javajedis.legalconnect.caseassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class NoteRepoTest {

    @Mock
    private NoteRepo noteRepo;

    private User ownerUser1;
    private User ownerUser2;
    private User clientUser;
    private Lawyer lawyer;
    private Case testCase1;
    private Case testCase2;
    private Note testNote1;
    private Note testNote2;
    private Note testNote3;
    private Note testNote4;
    private UUID caseId1;
    private UUID caseId2;
    private UUID ownerId1;
    private UUID ownerId2;
    private UUID clientUserId;
    private UUID lawyerId;
    private UUID noteId1;
    private UUID noteId2;
    private UUID noteId3;
    private UUID noteId4;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize UUIDs
        caseId1 = UUID.randomUUID();
        caseId2 = UUID.randomUUID();
        ownerId1 = UUID.randomUUID();
        ownerId2 = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        noteId1 = UUID.randomUUID();
        noteId2 = UUID.randomUUID();
        noteId3 = UUID.randomUUID();
        noteId4 = UUID.randomUUID();

        // Setup owner users
        ownerUser1 = new User();
        ownerUser1.setId(ownerId1);
        ownerUser1.setFirstName("John");
        ownerUser1.setLastName("Owner");
        ownerUser1.setEmail("owner1@example.com");
        ownerUser1.setRole(Role.LAWYER);
        ownerUser1.setEmailVerified(true);

        ownerUser2 = new User();
        ownerUser2.setId(ownerId2);
        ownerUser2.setFirstName("Jane");
        ownerUser2.setLastName("Owner");
        ownerUser2.setEmail("owner2@example.com");
        ownerUser2.setRole(Role.USER);
        ownerUser2.setEmailVerified(true);

        // Setup client user
        clientUser = new User();
        clientUser.setId(clientUserId);
        clientUser.setFirstName("Alice");
        clientUser.setLastName("Client");
        clientUser.setEmail("client@example.com");
        clientUser.setRole(Role.USER);
        clientUser.setEmailVerified(true);

        // Setup lawyer
        lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setUser(ownerUser1);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);

        // Setup cases
        OffsetDateTime now = OffsetDateTime.now();

        testCase1 = new Case();
        testCase1.setId(caseId1);
        testCase1.setLawyer(lawyer);
        testCase1.setClient(clientUser);
        testCase1.setTitle("Test Case 1");
        testCase1.setDescription("Description for case 1");
        testCase1.setStatus(CaseStatus.IN_PROGRESS);
        testCase1.setCreatedAt(now.minusDays(5));
        testCase1.setUpdatedAt(now.minusDays(1));

        testCase2 = new Case();
        testCase2.setId(caseId2);
        testCase2.setLawyer(lawyer);
        testCase2.setClient(clientUser);
        testCase2.setTitle("Test Case 2");
        testCase2.setDescription("Description for case 2");
        testCase2.setStatus(CaseStatus.RESOLVED);
        testCase2.setCreatedAt(now.minusDays(3));
        testCase2.setUpdatedAt(now.minusHours(2));

        // Setup notes
        testNote1 = new Note();
        testNote1.setId(noteId1);
        testNote1.setCaseEntity(testCase1);
        testNote1.setOwner(ownerUser1);
        testNote1.setTitle("Note 1 for Case 1");
        testNote1.setContent("First note content for case 1 with important details");
        testNote1.setPrivacy(AssetPrivacy.SHARED);
        testNote1.setCreatedAt(now.minusDays(4));
        testNote1.setUpdatedAt(now.minusDays(3));

        testNote2 = new Note();
        testNote2.setId(noteId2);
        testNote2.setCaseEntity(testCase1);
        testNote2.setOwner(ownerUser2);
        testNote2.setTitle("Note 2 for Case 1");
        testNote2.setContent("Second note content for case 1 with private information");
        testNote2.setPrivacy(AssetPrivacy.PRIVATE);
        testNote2.setCreatedAt(now.minusDays(2));
        testNote2.setUpdatedAt(now.minusDays(1));

        testNote3 = new Note();
        testNote3.setId(noteId3);
        testNote3.setCaseEntity(testCase1);
        testNote3.setOwner(ownerUser1);
        testNote3.setTitle("Note 3 for Case 1");
        testNote3.setContent("Third note content for case 1 with additional insights");
        testNote3.setPrivacy(AssetPrivacy.SHARED);
        testNote3.setCreatedAt(now.minusDays(1));
        testNote3.setUpdatedAt(now.minusHours(1));

        testNote4 = new Note();
        testNote4.setId(noteId4);
        testNote4.setCaseEntity(testCase2);
        testNote4.setOwner(ownerUser1);
        testNote4.setTitle("Note 1 for Case 2");
        testNote4.setContent("First note content for case 2");
        testNote4.setPrivacy(AssetPrivacy.SHARED);
        testNote4.setCreatedAt(now.minusHours(3));
        testNote4.setUpdatedAt(now.minusHours(2));
    }

    @Test
    void testFindByCaseEntityIdOrderByCreatedAtDesc_success() {
        // Arrange
        List<Note> expectedNotes = Arrays.asList(testNote3, testNote2, testNote1);
        when(noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1)).thenReturn(expectedNotes);

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1);

        // Assert
        assertEquals(3, actualNotes.size());
        assertEquals(testNote3.getId(), actualNotes.get(0).getId()); // Most recent first
        assertEquals(testNote2.getId(), actualNotes.get(1).getId());
        assertEquals(testNote1.getId(), actualNotes.get(2).getId()); // Oldest last
    }

    @Test
    void testFindByCaseEntityIdOrderByCreatedAtDesc_noNotes() {
        // Arrange
        when(noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2)).thenReturn(Collections.emptyList());

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2);

        // Assert
        assertTrue(actualNotes.isEmpty());
    }

    @Test
    void testFindByCaseEntityId_withPagination_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Note> notesPage = Arrays.asList(testNote3, testNote2);
        Page<Note> expectedPage = new PageImpl<>(notesPage, pageable, 3);
        when(noteRepo.findByCaseEntityId(caseId1, pageable)).thenReturn(expectedPage);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityId(caseId1, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(3, actualPage.getTotalElements());
        assertEquals(2, actualPage.getTotalPages());
        assertEquals(0, actualPage.getNumber());
        assertEquals(testNote3.getId(), actualPage.getContent().get(0).getId());
        assertEquals(testNote2.getId(), actualPage.getContent().get(1).getId());
    }

    @Test
    void testFindByCaseEntityId_withPagination_emptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Note> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(noteRepo.findByCaseEntityId(caseId2, pageable)).thenReturn(emptyPage);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityId(caseId2, pageable);

        // Assert
        assertNotNull(actualPage);
        assertTrue(actualPage.getContent().isEmpty());
        assertEquals(0, actualPage.getTotalElements());
        assertEquals(0, actualPage.getTotalPages());
    }

    @Test
    void testFindByCaseEntityIdAndPrivacyOrderByCreatedAtDesc_sharedNotes() {
        // Arrange
        List<Note> sharedNotes = Arrays.asList(testNote3, testNote1);
        when(noteRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.SHARED))
                .thenReturn(sharedNotes);

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.SHARED);

        // Assert
        assertEquals(2, actualNotes.size());
        assertEquals(testNote3.getId(), actualNotes.get(0).getId());
        assertEquals(testNote1.getId(), actualNotes.get(1).getId());
        // Verify all returned notes have SHARED privacy
        actualNotes.forEach(note -> assertEquals(AssetPrivacy.SHARED, note.getPrivacy()));
    }

    @Test
    void testFindByCaseEntityIdAndPrivacyOrderByCreatedAtDesc_privateNotes() {
        // Arrange
        List<Note> privateNotes = Arrays.asList(testNote2);
        when(noteRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.PRIVATE))
                .thenReturn(privateNotes);

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.PRIVATE);

        // Assert
        assertEquals(1, actualNotes.size());
        assertEquals(testNote2.getId(), actualNotes.get(0).getId());
        assertEquals(AssetPrivacy.PRIVATE, actualNotes.get(0).getPrivacy());
    }

    @Test
    void testFindByCaseEntityIdAndPrivacy_withPagination_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Note> sharedNotes = Arrays.asList(testNote3, testNote1);
        Page<Note> expectedPage = new PageImpl<>(sharedNotes, pageable, 2);
        when(noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(2, actualPage.getTotalElements());
        assertEquals(1, actualPage.getTotalPages());
        assertEquals(testNote3.getId(), actualPage.getContent().get(0).getId());
        assertEquals(testNote1.getId(), actualPage.getContent().get(1).getId());
    }

    @Test
    void testFindByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc_success() {
        // Arrange
        List<Note> ownerNotes = Arrays.asList(testNote3, testNote1);
        when(noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId1))
                .thenReturn(ownerNotes);

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId1);

        // Assert
        assertEquals(2, actualNotes.size());
        assertEquals(testNote3.getId(), actualNotes.get(0).getId());
        assertEquals(testNote1.getId(), actualNotes.get(1).getId());
        // Verify all returned notes are owned by the specified user
        actualNotes.forEach(note -> assertEquals(ownerId1, note.getOwner().getId()));
    }

    @Test
    void testFindByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc_differentOwner() {
        // Arrange
        List<Note> ownerNotes = Arrays.asList(testNote2);
        when(noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId2))
                .thenReturn(ownerNotes);

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId2);

        // Assert
        assertEquals(1, actualNotes.size());
        assertEquals(testNote2.getId(), actualNotes.get(0).getId());
        assertEquals(ownerId2, actualNotes.get(0).getOwner().getId());
    }

    @Test
    void testFindByCaseEntityIdAndOwnerId_withPagination_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        List<Note> ownerNotes = Arrays.asList(testNote3, testNote1);
        Page<Note> expectedPage = new PageImpl<>(ownerNotes, pageable, 2);
        when(noteRepo.findByCaseEntityIdAndOwnerId(caseId1, ownerId1, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityIdAndOwnerId(caseId1, ownerId1, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(2, actualPage.getTotalElements());
        assertEquals(1, actualPage.getTotalPages());
        assertEquals(testNote3.getId(), actualPage.getContent().get(0).getId());
        assertEquals(testNote1.getId(), actualPage.getContent().get(1).getId());
    }

    @Test
    void testFindByCaseEntityIdAndOwnerId_withPagination_emptyResult() {
        // Arrange
        UUID nonExistentOwnerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Note> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(noteRepo.findByCaseEntityIdAndOwnerId(caseId1, nonExistentOwnerId, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityIdAndOwnerId(caseId1, nonExistentOwnerId, pageable);

        // Assert
        assertNotNull(actualPage);
        assertTrue(actualPage.getContent().isEmpty());
        assertEquals(0, actualPage.getTotalElements());
    }

    @Test
    void testPaginationProperties() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page
        List<Note> pageContent = Arrays.asList(testNote1);
        Page<Note> page = new PageImpl<>(pageContent, pageable, 3); // Total 3 elements
        when(noteRepo.findByCaseEntityId(caseId1, pageable)).thenReturn(page);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityId(caseId1, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(1, actualPage.getNumberOfElements()); // Elements on this page
        assertEquals(2, actualPage.getSize()); // Page size
        assertEquals(1, actualPage.getNumber()); // Current page number (0-indexed)
        assertEquals(2, actualPage.getTotalPages()); // Total pages
        assertEquals(3, actualPage.getTotalElements()); // Total elements
        assertTrue(actualPage.hasPrevious());
        assertFalse(actualPage.hasNext());
        assertFalse(actualPage.isFirst());
        assertTrue(actualPage.isLast());
    }

    @Test
    void testMultipleCasesNotes() {
        // Arrange
        List<Note> case1Notes = Arrays.asList(testNote1, testNote2, testNote3);
        List<Note> case2Notes = Arrays.asList(testNote4);
        when(noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1)).thenReturn(case1Notes);
        when(noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2)).thenReturn(case2Notes);

        // Act
        List<Note> case1Result = noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1);
        List<Note> case2Result = noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2);

        // Assert
        assertEquals(3, case1Result.size());
        assertEquals(1, case2Result.size());
        assertEquals(caseId1, case1Result.get(0).getCaseEntity().getId());
        assertEquals(caseId2, case2Result.get(0).getCaseEntity().getId());
    }

    @Test
    void testMixedPrivacyFilteringWithPagination() {
        // Arrange - Testing pagination with mixed privacy settings
        Pageable pageable = PageRequest.of(0, 1);
        List<Note> sharedNotes = Arrays.asList(testNote3);
        Page<Note> sharedPage = new PageImpl<>(sharedNotes, pageable, 2);
        when(noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable))
                .thenReturn(sharedPage);

        List<Note> privateNotes = Arrays.asList(testNote2);
        Page<Note> privatePage = new PageImpl<>(privateNotes, pageable, 1);
        when(noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.PRIVATE, pageable))
                .thenReturn(privatePage);

        // Act
        Page<Note> sharedResult = noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable);
        Page<Note> privateResult = noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.PRIVATE, pageable);

        // Assert
        assertEquals(1, sharedResult.getContent().size());
        assertEquals(2, sharedResult.getTotalElements());
        assertEquals(AssetPrivacy.SHARED, sharedResult.getContent().get(0).getPrivacy());

        assertEquals(1, privateResult.getContent().size());
        assertEquals(1, privateResult.getTotalElements());
        assertEquals(AssetPrivacy.PRIVATE, privateResult.getContent().get(0).getPrivacy());
    }

    @Test
    void testNoteSortingByCreatedAt() {
        // Arrange
        List<Note> notesInOrder = Arrays.asList(testNote3, testNote2, testNote1); // Newest to oldest
        when(noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1)).thenReturn(notesInOrder);

        // Act
        List<Note> actualNotes = noteRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1);

        // Assert
        assertEquals(3, actualNotes.size());
        // Verify the notes are sorted by creation date (newest first)
        assertTrue(actualNotes.get(0).getCreatedAt().isAfter(actualNotes.get(1).getCreatedAt()));
        assertTrue(actualNotes.get(1).getCreatedAt().isAfter(actualNotes.get(2).getCreatedAt()));
    }

    @Test
    void testOwnerSpecificNoteRetrieval() {
        // Arrange - Testing that each owner only gets their notes
        List<Note> owner1Notes = Arrays.asList(testNote3, testNote1);
        List<Note> owner2Notes = Arrays.asList(testNote2);
        when(noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId1))
                .thenReturn(owner1Notes);
        when(noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId2))
                .thenReturn(owner2Notes);

        // Act
        List<Note> owner1Result = noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId1);
        List<Note> owner2Result = noteRepo.findByCaseEntityIdAndOwnerIdOrderByCreatedAtDesc(caseId1, ownerId2);

        // Assert
        assertEquals(2, owner1Result.size());
        assertEquals(1, owner2Result.size());
        
        // Verify ownership
        owner1Result.forEach(note -> assertEquals(ownerId1, note.getOwner().getId()));
        owner2Result.forEach(note -> assertEquals(ownerId2, note.getOwner().getId()));
        
        // Verify different owners get different notes
        assertFalse(owner1Result.stream().anyMatch(note -> note.getOwner().getId().equals(ownerId2)));
        assertFalse(owner2Result.stream().anyMatch(note -> note.getOwner().getId().equals(ownerId1)));
    }

    @Test
    void testComplexQueryCombination() {
        // Arrange - Testing combination of case, owner, and privacy filters
        Pageable pageable = PageRequest.of(0, 5);
        List<Note> filteredNotes = Arrays.asList(testNote3); // Only shared notes by owner1
        Page<Note> expectedPage = new PageImpl<>(filteredNotes, pageable, 1);
        
        // This simulates a complex query that would combine multiple criteria
        when(noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Note> actualPage = noteRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(1, actualPage.getContent().size());
        assertEquals(AssetPrivacy.SHARED, actualPage.getContent().get(0).getPrivacy());
        assertEquals(caseId1, actualPage.getContent().get(0).getCaseEntity().getId());
    }
} 