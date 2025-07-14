package com.javajedis.legalconnect.caseassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class NoteTest {

    private Note note;
    private Case caseEntity;
    private User ownerUser;
    private Lawyer lawyer;
    private User clientUser;
    private UUID testNoteId;
    private UUID testCaseId;
    private UUID testOwnerId;
    private UUID testLawyerId;
    private UUID testClientId;

    @BeforeEach
    void setUp() {
        testNoteId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testClientId = UUID.randomUUID();

        // Setup owner user
        ownerUser = new User();
        ownerUser.setId(testOwnerId);
        ownerUser.setFirstName("John");
        ownerUser.setLastName("Owner");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setRole(Role.LAWYER);
        ownerUser.setEmailVerified(true);

        // Setup client user
        clientUser = new User();
        clientUser.setId(testClientId);
        clientUser.setFirstName("Jane");
        clientUser.setLastName("Client");
        clientUser.setEmail("client@example.com");
        clientUser.setRole(Role.USER);
        clientUser.setEmailVerified(true);

        // Setup lawyer
        lawyer = new Lawyer();
        lawyer.setId(testLawyerId);
        lawyer.setUser(ownerUser);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);

        // Setup case entity
        caseEntity = new Case();
        caseEntity.setId(testCaseId);
        caseEntity.setLawyer(lawyer);
        caseEntity.setClient(clientUser);
        caseEntity.setTitle("Test Case Title");
        caseEntity.setDescription("Test case description");
        caseEntity.setStatus(CaseStatus.IN_PROGRESS);

        // Setup note
        note = new Note();
        note.setId(testNoteId);
        note.setCaseEntity(caseEntity);
        note.setOwner(ownerUser);
        note.setTitle("Test Note Title");
        note.setContent("Test note content with important details about the case");
        note.setPrivacy(AssetPrivacy.SHARED);
    }

    @Test
    void testDefaultConstructor() {
        Note defaultNote = new Note();

        assertNotNull(defaultNote);
        assertNull(defaultNote.getId());
        assertNull(defaultNote.getCaseEntity());
        assertNull(defaultNote.getOwner());
        assertNull(defaultNote.getTitle());
        assertNull(defaultNote.getContent());
        assertEquals(AssetPrivacy.SHARED, defaultNote.getPrivacy());
        assertNull(defaultNote.getCreatedAt());
        assertNull(defaultNote.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime earlier = now.minusDays(1);
        Note constructedNote = new Note(
            testNoteId, caseEntity, ownerUser, "Constructor Test Note",
            "Constructor test content", AssetPrivacy.PRIVATE, earlier, now
        );

        assertEquals(testNoteId, constructedNote.getId());
        assertEquals(caseEntity, constructedNote.getCaseEntity());
        assertEquals(ownerUser, constructedNote.getOwner());
        assertEquals("Constructor Test Note", constructedNote.getTitle());
        assertEquals("Constructor test content", constructedNote.getContent());
        assertEquals(AssetPrivacy.PRIVATE, constructedNote.getPrivacy());
        assertEquals(earlier, constructedNote.getCreatedAt());
        assertEquals(now, constructedNote.getUpdatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        note.setId(newId);
        assertEquals(newId, note.getId());
    }

    @Test
    void testCaseEntityGetterAndSetter() {
        Case newCase = new Case();
        newCase.setId(UUID.randomUUID());
        newCase.setTitle("New Case Title");

        note.setCaseEntity(newCase);
        assertEquals(newCase, note.getCaseEntity());
        assertEquals("New Case Title", note.getCaseEntity().getTitle());
    }

    @Test
    void testOwnerGetterAndSetter() {
        User newOwner = new User();
        newOwner.setId(UUID.randomUUID());
        newOwner.setEmail("newowner@example.com");
        newOwner.setRole(Role.USER);

        note.setOwner(newOwner);
        assertEquals(newOwner, note.getOwner());
        assertEquals("newowner@example.com", note.getOwner().getEmail());
    }

    @Test
    void testTitleGetterAndSetter() {
        String newTitle = "Updated Note Title";
        note.setTitle(newTitle);
        assertEquals(newTitle, note.getTitle());
    }

    @Test
    void testContentGetterAndSetter() {
        String newContent = "Updated note content with even more important details about the case proceedings";
        note.setContent(newContent);
        assertEquals(newContent, note.getContent());
    }

    @Test
    void testPrivacyGetterAndSetter() {
        AssetPrivacy newPrivacy = AssetPrivacy.PRIVATE;
        note.setPrivacy(newPrivacy);
        assertEquals(newPrivacy, note.getPrivacy());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        note.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, note.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        note.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, note.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Note note1 = new Note();
        note1.setId(testNoteId);
        note1.setTitle("Test Note");
        note1.setContent("Test content");
        note1.setPrivacy(AssetPrivacy.SHARED);

        Note note2 = new Note();
        note2.setId(testNoteId);
        note2.setTitle("Test Note");
        note2.setContent("Test content");
        note2.setPrivacy(AssetPrivacy.SHARED);

        Note note3 = new Note();
        note3.setId(UUID.randomUUID());
        note3.setTitle("Different Note");
        note3.setContent("Different content");
        note3.setPrivacy(AssetPrivacy.PRIVATE);

        assertEquals(note1, note2);
        assertEquals(note1.hashCode(), note2.hashCode());
        assertNotEquals(note1, note3);
        assertNotEquals(note1.hashCode(), note3.hashCode());
    }

    @Test
    void testEqualsWithNullValues() {
        Note note1 = new Note();
        Note note2 = new Note();

        assertEquals(note1, note2);
        assertEquals(note1.hashCode(), note2.hashCode());
    }

    @Test
    void testToString() {
        String toString = note.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Test Note Title"));
        assertTrue(toString.contains("Test note content"));
        assertTrue(toString.contains("SHARED"));
    }

    @Test
    void testDefaultPrivacy() {
        Note newNote = new Note();
        assertEquals(AssetPrivacy.SHARED, newNote.getPrivacy());
    }

    @Test
    void testAllAssetPrivacies() {
        // Test SHARED privacy
        note.setPrivacy(AssetPrivacy.SHARED);
        assertEquals(AssetPrivacy.SHARED, note.getPrivacy());
        assertEquals("Shared", note.getPrivacy().getDisplayName());

        // Test PRIVATE privacy
        note.setPrivacy(AssetPrivacy.PRIVATE);
        assertEquals(AssetPrivacy.PRIVATE, note.getPrivacy());
        assertEquals("Private", note.getPrivacy().getDisplayName());
    }

    @Test
    void testNoteWithLongTitle() {
        String longTitle = "T".repeat(255); // Max length according to typical validation
        note.setTitle(longTitle);
        assertEquals(longTitle, note.getTitle());
        assertEquals(255, note.getTitle().length());
    }

    @Test
    void testNoteWithLongContent() {
        String longContent = "C".repeat(10000); // Max length according to validation
        note.setContent(longContent);
        assertEquals(longContent, note.getContent());
        assertEquals(10000, note.getContent().length());
    }

    @Test
    void testNoteRelationships() {
        // Test case relationship
        assertNotNull(note.getCaseEntity());
        assertEquals(caseEntity.getId(), note.getCaseEntity().getId());
        assertEquals("Test Case Title", note.getCaseEntity().getTitle());
        assertEquals(lawyer, note.getCaseEntity().getLawyer());
        assertEquals(clientUser, note.getCaseEntity().getClient());

        // Test owner relationship
        assertNotNull(note.getOwner());
        assertEquals(ownerUser.getId(), note.getOwner().getId());
        assertEquals("owner@example.com", note.getOwner().getEmail());
        assertEquals(Role.LAWYER, note.getOwner().getRole());
    }

    @Test
    void testNoteWithTimestamps() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        
        note.setCreatedAt(createdAt);
        note.setUpdatedAt(updatedAt);

        assertEquals(createdAt, note.getCreatedAt());
        assertEquals(updatedAt, note.getUpdatedAt());
    }

    @Test
    void testNotePrivacyTransition() {
        // Start with SHARED
        assertEquals(AssetPrivacy.SHARED, note.getPrivacy());

        // Change to PRIVATE
        note.setPrivacy(AssetPrivacy.PRIVATE);
        assertEquals(AssetPrivacy.PRIVATE, note.getPrivacy());

        // Change back to SHARED
        note.setPrivacy(AssetPrivacy.SHARED);
        assertEquals(AssetPrivacy.SHARED, note.getPrivacy());
    }

    @Test
    void testNoteWithMinimalData() {
        Note minimalNote = new Note();
        minimalNote.setCaseEntity(caseEntity);
        minimalNote.setOwner(ownerUser);
        minimalNote.setTitle("M"); // Minimum title length
        minimalNote.setContent("C"); // Minimum content length

        assertEquals(caseEntity, minimalNote.getCaseEntity());
        assertEquals(ownerUser, minimalNote.getOwner());
        assertEquals("M", minimalNote.getTitle());
        assertEquals("C", minimalNote.getContent());
        assertEquals(AssetPrivacy.SHARED, minimalNote.getPrivacy()); // Default privacy
    }

    @Test
    void testNoteEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(note.getId());
        assertNotNull(note.getCaseEntity());
        assertNotNull(note.getOwner());
        assertNotNull(note.getTitle());
        assertNotNull(note.getContent());
        assertNotNull(note.getPrivacy());
    }

    @Test
    void testNoteTimestampUpdate() {
        OffsetDateTime initialCreated = OffsetDateTime.now().minusDays(2);
        OffsetDateTime initialUpdated = OffsetDateTime.now().minusDays(1);
        OffsetDateTime newUpdated = OffsetDateTime.now();

        note.setCreatedAt(initialCreated);
        note.setUpdatedAt(initialUpdated);

        assertEquals(initialCreated, note.getCreatedAt());
        assertEquals(initialUpdated, note.getUpdatedAt());

        // Simulate update
        note.setUpdatedAt(newUpdated);

        assertEquals(initialCreated, note.getCreatedAt()); // Should remain unchanged
        assertEquals(newUpdated, note.getUpdatedAt()); // Should be updated
    }

    @Test
    void testNoteWithSpecialCharactersInFields() {
        String specialTitle = "Note with Special Characters: @#$%^&*()";
        String specialContent = "Content with UTF-8: ñáéíóú 中文 العربية and line breaks\nand tabs\t";

        note.setTitle(specialTitle);
        note.setContent(specialContent);

        assertEquals(specialTitle, note.getTitle());
        assertEquals(specialContent, note.getContent());
    }

    @Test
    void testNoteContentWithLineBreaks() {
        String contentWithLineBreaks = "Line 1\nLine 2\n\nLine 4 after empty line\r\nWindows line ending";
        note.setContent(contentWithLineBreaks);
        assertEquals(contentWithLineBreaks, note.getContent());
    }

    @Test
    void testNoteWithEmptyContent() {
        // Testing edge case with empty content
        note.setContent("");
        assertEquals("", note.getContent());
    }

    @Test
    void testNoteOwnershipChange() {
        User originalOwner = note.getOwner();
        assertEquals(ownerUser, originalOwner);

        User newOwner = new User();
        newOwner.setId(UUID.randomUUID());
        newOwner.setEmail("newowner@example.com");
        newOwner.setRole(Role.USER);

        note.setOwner(newOwner);
        assertEquals(newOwner, note.getOwner());
        assertNotEquals(originalOwner, note.getOwner());
    }

    @Test
    void testNoteCaseTransfer() {
        Case originalCase = note.getCaseEntity();
        assertEquals(caseEntity, originalCase);

        Case newCase = new Case();
        newCase.setId(UUID.randomUUID());
        newCase.setTitle("New Case for Note Transfer");

        note.setCaseEntity(newCase);
        assertEquals(newCase, note.getCaseEntity());
        assertEquals("New Case for Note Transfer", note.getCaseEntity().getTitle());
        assertNotEquals(originalCase, note.getCaseEntity());
    }
} 