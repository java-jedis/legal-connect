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

class DocumentTest {

    private Document document;
    private Case caseEntity;
    private User uploadedByUser;
    private Lawyer lawyer;
    private User clientUser;
    private UUID testDocumentId;
    private UUID testCaseId;
    private UUID testUploadedById;
    private UUID testLawyerId;
    private UUID testClientId;

    @BeforeEach
    void setUp() {
        testDocumentId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testUploadedById = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testClientId = UUID.randomUUID();

        // Setup uploaded by user
        uploadedByUser = new User();
        uploadedByUser.setId(testUploadedById);
        uploadedByUser.setFirstName("John");
        uploadedByUser.setLastName("Uploader");
        uploadedByUser.setEmail("uploader@example.com");
        uploadedByUser.setRole(Role.LAWYER);
        uploadedByUser.setEmailVerified(true);

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
        lawyer.setUser(uploadedByUser);
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

        // Setup document
        document = new Document();
        document.setId(testDocumentId);
        document.setCaseEntity(caseEntity);
        document.setUploadedBy(uploadedByUser);
        document.setTitle("Test Document Title");
        document.setDescription("Test document description");
        document.setFileUrl("https://s3.amazonaws.com/bucket/document.pdf");
        document.setPrivacy(AssetPrivacy.SHARED);
    }

    @Test
    void testDefaultConstructor() {
        Document defaultDocument = new Document();

        assertNotNull(defaultDocument);
        assertNull(defaultDocument.getId());
        assertNull(defaultDocument.getCaseEntity());
        assertNull(defaultDocument.getUploadedBy());
        assertNull(defaultDocument.getTitle());
        assertNull(defaultDocument.getDescription());
        assertNull(defaultDocument.getFileUrl());
        assertEquals(AssetPrivacy.SHARED, defaultDocument.getPrivacy());
        assertNull(defaultDocument.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Document constructedDocument = new Document(
            testDocumentId, caseEntity, uploadedByUser, "Constructor Test Document",
            "Constructor test description", "https://s3.amazonaws.com/bucket/constructor.pdf",
            AssetPrivacy.PRIVATE, now
        );

        assertEquals(testDocumentId, constructedDocument.getId());
        assertEquals(caseEntity, constructedDocument.getCaseEntity());
        assertEquals(uploadedByUser, constructedDocument.getUploadedBy());
        assertEquals("Constructor Test Document", constructedDocument.getTitle());
        assertEquals("Constructor test description", constructedDocument.getDescription());
        assertEquals("https://s3.amazonaws.com/bucket/constructor.pdf", constructedDocument.getFileUrl());
        assertEquals(AssetPrivacy.PRIVATE, constructedDocument.getPrivacy());
        assertEquals(now, constructedDocument.getCreatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        document.setId(newId);
        assertEquals(newId, document.getId());
    }

    @Test
    void testCaseEntityGetterAndSetter() {
        Case newCase = new Case();
        newCase.setId(UUID.randomUUID());
        newCase.setTitle("New Case Title");

        document.setCaseEntity(newCase);
        assertEquals(newCase, document.getCaseEntity());
        assertEquals("New Case Title", document.getCaseEntity().getTitle());
    }

    @Test
    void testUploadedByGetterAndSetter() {
        User newUploader = new User();
        newUploader.setId(UUID.randomUUID());
        newUploader.setEmail("newuploader@example.com");
        newUploader.setRole(Role.USER);

        document.setUploadedBy(newUploader);
        assertEquals(newUploader, document.getUploadedBy());
        assertEquals("newuploader@example.com", document.getUploadedBy().getEmail());
    }

    @Test
    void testTitleGetterAndSetter() {
        String newTitle = "Updated Document Title";
        document.setTitle(newTitle);
        assertEquals(newTitle, document.getTitle());
    }

    @Test
    void testDescriptionGetterAndSetter() {
        String newDescription = "Updated document description with more details";
        document.setDescription(newDescription);
        assertEquals(newDescription, document.getDescription());
    }

    @Test
    void testDescriptionWithNullValue() {
        document.setDescription(null);
        assertNull(document.getDescription());
    }

    @Test
    void testDescriptionWithEmptyString() {
        document.setDescription("");
        assertEquals("", document.getDescription());
    }

    @Test
    void testFileUrlGetterAndSetter() {
        String newFileUrl = "https://s3.amazonaws.com/bucket/updated-document.pdf";
        document.setFileUrl(newFileUrl);
        assertEquals(newFileUrl, document.getFileUrl());
    }

    @Test
    void testPrivacyGetterAndSetter() {
        AssetPrivacy newPrivacy = AssetPrivacy.PRIVATE;
        document.setPrivacy(newPrivacy);
        assertEquals(newPrivacy, document.getPrivacy());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        document.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, document.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Document document1 = new Document();
        document1.setId(testDocumentId);
        document1.setTitle("Test Document");
        document1.setFileUrl("https://s3.amazonaws.com/bucket/test.pdf");
        document1.setPrivacy(AssetPrivacy.SHARED);

        Document document2 = new Document();
        document2.setId(testDocumentId);
        document2.setTitle("Test Document");
        document2.setFileUrl("https://s3.amazonaws.com/bucket/test.pdf");
        document2.setPrivacy(AssetPrivacy.SHARED);

        Document document3 = new Document();
        document3.setId(UUID.randomUUID());
        document3.setTitle("Different Document");
        document3.setFileUrl("https://s3.amazonaws.com/bucket/different.pdf");
        document3.setPrivacy(AssetPrivacy.PRIVATE);

        assertEquals(document1, document2);
        assertEquals(document1.hashCode(), document2.hashCode());
        assertNotEquals(document1, document3);
        assertNotEquals(document1.hashCode(), document3.hashCode());
    }

    @Test
    void testEqualsWithNullValues() {
        Document document1 = new Document();
        Document document2 = new Document();

        assertEquals(document1, document2);
        assertEquals(document1.hashCode(), document2.hashCode());
    }

    @Test
    void testToString() {
        String toString = document.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Test Document Title"));
        assertTrue(toString.contains("Test document description"));
        assertTrue(toString.contains("SHARED"));
        assertTrue(toString.contains("document.pdf"));
    }

    @Test
    void testDefaultPrivacy() {
        Document newDocument = new Document();
        assertEquals(AssetPrivacy.SHARED, newDocument.getPrivacy());
    }

    @Test
    void testAllAssetPrivacies() {
        // Test SHARED privacy
        document.setPrivacy(AssetPrivacy.SHARED);
        assertEquals(AssetPrivacy.SHARED, document.getPrivacy());
        assertEquals("Shared", document.getPrivacy().getDisplayName());

        // Test PRIVATE privacy
        document.setPrivacy(AssetPrivacy.PRIVATE);
        assertEquals(AssetPrivacy.PRIVATE, document.getPrivacy());
        assertEquals("Private", document.getPrivacy().getDisplayName());
    }

    @Test
    void testDocumentWithLongTitle() {
        String longTitle = "T".repeat(255); // Max length according to typical validation
        document.setTitle(longTitle);
        assertEquals(longTitle, document.getTitle());
        assertEquals(255, document.getTitle().length());
    }

    @Test
    void testDocumentWithLongDescription() {
        String longDescription = "D".repeat(10000); // Max length according to validation
        document.setDescription(longDescription);
        assertEquals(longDescription, document.getDescription());
        assertEquals(10000, document.getDescription().length());
    }

    @Test
    void testDocumentRelationships() {
        // Test case relationship
        assertNotNull(document.getCaseEntity());
        assertEquals(caseEntity.getId(), document.getCaseEntity().getId());
        assertEquals("Test Case Title", document.getCaseEntity().getTitle());
        assertEquals(lawyer, document.getCaseEntity().getLawyer());
        assertEquals(clientUser, document.getCaseEntity().getClient());

        // Test uploaded by relationship
        assertNotNull(document.getUploadedBy());
        assertEquals(uploadedByUser.getId(), document.getUploadedBy().getId());
        assertEquals("uploader@example.com", document.getUploadedBy().getEmail());
        assertEquals(Role.LAWYER, document.getUploadedBy().getRole());
    }

    @Test
    void testDocumentWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        document.setCreatedAt(now);

        assertEquals(now, document.getCreatedAt());
    }

    @Test
    void testDocumentPrivacyTransition() {
        // Start with SHARED
        assertEquals(AssetPrivacy.SHARED, document.getPrivacy());

        // Change to PRIVATE
        document.setPrivacy(AssetPrivacy.PRIVATE);
        assertEquals(AssetPrivacy.PRIVATE, document.getPrivacy());

        // Change back to SHARED
        document.setPrivacy(AssetPrivacy.SHARED);
        assertEquals(AssetPrivacy.SHARED, document.getPrivacy());
    }

    @Test
    void testDocumentWithMinimalData() {
        Document minimalDocument = new Document();
        minimalDocument.setCaseEntity(caseEntity);
        minimalDocument.setUploadedBy(uploadedByUser);
        minimalDocument.setTitle("M"); // Minimum title length
        minimalDocument.setFileUrl("file.pdf");

        assertEquals(caseEntity, minimalDocument.getCaseEntity());
        assertEquals(uploadedByUser, minimalDocument.getUploadedBy());
        assertEquals("M", minimalDocument.getTitle());
        assertEquals("file.pdf", minimalDocument.getFileUrl());
        assertNull(minimalDocument.getDescription()); // Can be null
        assertEquals(AssetPrivacy.SHARED, minimalDocument.getPrivacy()); // Default privacy
    }

    @Test
    void testDocumentEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(document.getId());
        assertNotNull(document.getCaseEntity());
        assertNotNull(document.getUploadedBy());
        assertNotNull(document.getTitle());
        assertNotNull(document.getDescription());
        assertNotNull(document.getFileUrl());
        assertNotNull(document.getPrivacy());
    }

    @Test
    void testDocumentWithVariousFileUrls() {
        String[] fileUrls = {
            "https://s3.amazonaws.com/bucket/document.pdf",
            "https://storage.googleapis.com/bucket/file.docx",
            "/local/path/to/file.txt",
            "file.jpg"
        };

        for (String fileUrl : fileUrls) {
            document.setFileUrl(fileUrl);
            assertEquals(fileUrl, document.getFileUrl());
        }
    }

    @Test
    void testDocumentWithSpecialCharactersInFields() {
        String specialTitle = "Document with Special Characters: @#$%^&*()";
        String specialDescription = "Description with UTF-8: ñáéíóú 中文 العربية";
        String specialFileUrl = "https://s3.amazonaws.com/bucket/file with spaces & special chars.pdf";

        document.setTitle(specialTitle);
        document.setDescription(specialDescription);
        document.setFileUrl(specialFileUrl);

        assertEquals(specialTitle, document.getTitle());
        assertEquals(specialDescription, document.getDescription());
        assertEquals(specialFileUrl, document.getFileUrl());
    }
} 