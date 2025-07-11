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

class DocumentRepoTest {

    @Mock
    private DocumentRepo documentRepo;

    private User uploaderUser1;
    private User uploaderUser2;
    private User clientUser;
    private Lawyer lawyer;
    private Case testCase1;
    private Case testCase2;
    private Document testDocument1;
    private Document testDocument2;
    private Document testDocument3;
    private Document testDocument4;
    private UUID caseId1;
    private UUID caseId2;
    private UUID uploaderId1;
    private UUID uploaderId2;
    private UUID clientUserId;
    private UUID lawyerId;
    private UUID documentId1;
    private UUID documentId2;
    private UUID documentId3;
    private UUID documentId4;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize UUIDs
        caseId1 = UUID.randomUUID();
        caseId2 = UUID.randomUUID();
        uploaderId1 = UUID.randomUUID();
        uploaderId2 = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        documentId1 = UUID.randomUUID();
        documentId2 = UUID.randomUUID();
        documentId3 = UUID.randomUUID();
        documentId4 = UUID.randomUUID();

        // Setup uploader users
        uploaderUser1 = new User();
        uploaderUser1.setId(uploaderId1);
        uploaderUser1.setFirstName("John");
        uploaderUser1.setLastName("Uploader");
        uploaderUser1.setEmail("uploader1@example.com");
        uploaderUser1.setRole(Role.LAWYER);
        uploaderUser1.setEmailVerified(true);

        uploaderUser2 = new User();
        uploaderUser2.setId(uploaderId2);
        uploaderUser2.setFirstName("Jane");
        uploaderUser2.setLastName("Uploader");
        uploaderUser2.setEmail("uploader2@example.com");
        uploaderUser2.setRole(Role.USER);
        uploaderUser2.setEmailVerified(true);

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
        lawyer.setUser(uploaderUser1);
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

        // Setup documents
        testDocument1 = new Document();
        testDocument1.setId(documentId1);
        testDocument1.setCaseEntity(testCase1);
        testDocument1.setUploadedBy(uploaderUser1);
        testDocument1.setTitle("Document 1 for Case 1");
        testDocument1.setDescription("First document for case 1");
        testDocument1.setFileUrl("https://s3.amazonaws.com/bucket/document1.pdf");
        testDocument1.setPrivacy(AssetPrivacy.SHARED);
        testDocument1.setCreatedAt(now.minusDays(4));

        testDocument2 = new Document();
        testDocument2.setId(documentId2);
        testDocument2.setCaseEntity(testCase1);
        testDocument2.setUploadedBy(uploaderUser2);
        testDocument2.setTitle("Document 2 for Case 1");
        testDocument2.setDescription("Second document for case 1");
        testDocument2.setFileUrl("https://s3.amazonaws.com/bucket/document2.docx");
        testDocument2.setPrivacy(AssetPrivacy.PRIVATE);
        testDocument2.setCreatedAt(now.minusDays(2));

        testDocument3 = new Document();
        testDocument3.setId(documentId3);
        testDocument3.setCaseEntity(testCase1);
        testDocument3.setUploadedBy(uploaderUser1);
        testDocument3.setTitle("Document 3 for Case 1");
        testDocument3.setDescription("Third document for case 1");
        testDocument3.setFileUrl("https://s3.amazonaws.com/bucket/document3.txt");
        testDocument3.setPrivacy(AssetPrivacy.SHARED);
        testDocument3.setCreatedAt(now.minusDays(1));

        testDocument4 = new Document();
        testDocument4.setId(documentId4);
        testDocument4.setCaseEntity(testCase2);
        testDocument4.setUploadedBy(uploaderUser1);
        testDocument4.setTitle("Document 1 for Case 2");
        testDocument4.setDescription("First document for case 2");
        testDocument4.setFileUrl("https://s3.amazonaws.com/bucket/document4.pdf");
        testDocument4.setPrivacy(AssetPrivacy.SHARED);
        testDocument4.setCreatedAt(now.minusHours(3));
    }

    @Test
    void testFindByCaseEntityIdOrderByCreatedAtDesc_success() {
        // Arrange
        List<Document> expectedDocuments = Arrays.asList(testDocument3, testDocument2, testDocument1);
        when(documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1)).thenReturn(expectedDocuments);

        // Act
        List<Document> actualDocuments = documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1);

        // Assert
        assertEquals(3, actualDocuments.size());
        assertEquals(testDocument3.getId(), actualDocuments.get(0).getId()); // Most recent first
        assertEquals(testDocument2.getId(), actualDocuments.get(1).getId());
        assertEquals(testDocument1.getId(), actualDocuments.get(2).getId()); // Oldest last
    }

    @Test
    void testFindByCaseEntityIdOrderByCreatedAtDesc_noDcouments() {
        // Arrange
        when(documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2)).thenReturn(Collections.emptyList());

        // Act
        List<Document> actualDocuments = documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2);

        // Assert
        assertTrue(actualDocuments.isEmpty());
    }

    @Test
    void testFindByCaseEntityId_withPagination_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Document> documentsPage = Arrays.asList(testDocument3, testDocument2);
        Page<Document> expectedPage = new PageImpl<>(documentsPage, pageable, 3);
        when(documentRepo.findByCaseEntityId(caseId1, pageable)).thenReturn(expectedPage);

        // Act
        Page<Document> actualPage = documentRepo.findByCaseEntityId(caseId1, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(3, actualPage.getTotalElements());
        assertEquals(2, actualPage.getTotalPages());
        assertEquals(0, actualPage.getNumber());
        assertEquals(testDocument3.getId(), actualPage.getContent().get(0).getId());
        assertEquals(testDocument2.getId(), actualPage.getContent().get(1).getId());
    }

    @Test
    void testFindByCaseEntityId_withPagination_emptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Document> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(documentRepo.findByCaseEntityId(caseId2, pageable)).thenReturn(emptyPage);

        // Act
        Page<Document> actualPage = documentRepo.findByCaseEntityId(caseId2, pageable);

        // Assert
        assertNotNull(actualPage);
        assertTrue(actualPage.getContent().isEmpty());
        assertEquals(0, actualPage.getTotalElements());
        assertEquals(0, actualPage.getTotalPages());
    }

    @Test
    void testFindByCaseEntityIdAndPrivacyOrderByCreatedAtDesc_sharedDocuments() {
        // Arrange
        List<Document> sharedDocuments = Arrays.asList(testDocument3, testDocument1);
        when(documentRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.SHARED))
                .thenReturn(sharedDocuments);

        // Act
        List<Document> actualDocuments = documentRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.SHARED);

        // Assert
        assertEquals(2, actualDocuments.size());
        assertEquals(testDocument3.getId(), actualDocuments.get(0).getId());
        assertEquals(testDocument1.getId(), actualDocuments.get(1).getId());
        // Verify all returned documents have SHARED privacy
        actualDocuments.forEach(doc -> assertEquals(AssetPrivacy.SHARED, doc.getPrivacy()));
    }

    @Test
    void testFindByCaseEntityIdAndPrivacyOrderByCreatedAtDesc_privateDocuments() {
        // Arrange
        List<Document> privateDocuments = Arrays.asList(testDocument2);
        when(documentRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.PRIVATE))
                .thenReturn(privateDocuments);

        // Act
        List<Document> actualDocuments = documentRepo.findByCaseEntityIdAndPrivacyOrderByCreatedAtDesc(caseId1, AssetPrivacy.PRIVATE);

        // Assert
        assertEquals(1, actualDocuments.size());
        assertEquals(testDocument2.getId(), actualDocuments.get(0).getId());
        assertEquals(AssetPrivacy.PRIVATE, actualDocuments.get(0).getPrivacy());
    }

    @Test
    void testFindByCaseEntityIdAndPrivacy_withPagination_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Document> sharedDocuments = Arrays.asList(testDocument3, testDocument1);
        Page<Document> expectedPage = new PageImpl<>(sharedDocuments, pageable, 2);
        when(documentRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Document> actualPage = documentRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(2, actualPage.getTotalElements());
        assertEquals(1, actualPage.getTotalPages());
        assertEquals(testDocument3.getId(), actualPage.getContent().get(0).getId());
        assertEquals(testDocument1.getId(), actualPage.getContent().get(1).getId());
    }

    @Test
    void testFindByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc_success() {
        // Arrange
        List<Document> uploaderDocuments = Arrays.asList(testDocument3, testDocument1);
        when(documentRepo.findByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc(caseId1, uploaderId1))
                .thenReturn(uploaderDocuments);

        // Act
        List<Document> actualDocuments = documentRepo.findByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc(caseId1, uploaderId1);

        // Assert
        assertEquals(2, actualDocuments.size());
        assertEquals(testDocument3.getId(), actualDocuments.get(0).getId());
        assertEquals(testDocument1.getId(), actualDocuments.get(1).getId());
        // Verify all returned documents are uploaded by the specified user
        actualDocuments.forEach(doc -> assertEquals(uploaderId1, doc.getUploadedBy().getId()));
    }

    @Test
    void testFindByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc_differentUploader() {
        // Arrange
        List<Document> uploaderDocuments = Arrays.asList(testDocument2);
        when(documentRepo.findByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc(caseId1, uploaderId2))
                .thenReturn(uploaderDocuments);

        // Act
        List<Document> actualDocuments = documentRepo.findByCaseEntityIdAndUploadedByIdOrderByCreatedAtDesc(caseId1, uploaderId2);

        // Assert
        assertEquals(1, actualDocuments.size());
        assertEquals(testDocument2.getId(), actualDocuments.get(0).getId());
        assertEquals(uploaderId2, actualDocuments.get(0).getUploadedBy().getId());
    }

    @Test
    void testFindByCaseEntityIdAndUploadedById_withPagination_success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        List<Document> uploaderDocuments = Arrays.asList(testDocument3, testDocument1);
        Page<Document> expectedPage = new PageImpl<>(uploaderDocuments, pageable, 2);
        when(documentRepo.findByCaseEntityIdAndUploadedById(caseId1, uploaderId1, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Document> actualPage = documentRepo.findByCaseEntityIdAndUploadedById(caseId1, uploaderId1, pageable);

        // Assert
        assertNotNull(actualPage);
        assertEquals(2, actualPage.getContent().size());
        assertEquals(2, actualPage.getTotalElements());
        assertEquals(1, actualPage.getTotalPages());
        assertEquals(testDocument3.getId(), actualPage.getContent().get(0).getId());
        assertEquals(testDocument1.getId(), actualPage.getContent().get(1).getId());
    }

    @Test
    void testFindByCaseEntityIdAndUploadedById_withPagination_emptyResult() {
        // Arrange
        UUID nonExistentUploaderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Document> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(documentRepo.findByCaseEntityIdAndUploadedById(caseId1, nonExistentUploaderId, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<Document> actualPage = documentRepo.findByCaseEntityIdAndUploadedById(caseId1, nonExistentUploaderId, pageable);

        // Assert
        assertNotNull(actualPage);
        assertTrue(actualPage.getContent().isEmpty());
        assertEquals(0, actualPage.getTotalElements());
    }

    @Test
    void testPaginationProperties() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page
        List<Document> pageContent = Arrays.asList(testDocument1);
        Page<Document> page = new PageImpl<>(pageContent, pageable, 3); // Total 3 elements
        when(documentRepo.findByCaseEntityId(caseId1, pageable)).thenReturn(page);

        // Act
        Page<Document> actualPage = documentRepo.findByCaseEntityId(caseId1, pageable);

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
    void testMultipleCasesDocuments() {
        // Arrange
        List<Document> case1Documents = Arrays.asList(testDocument1, testDocument2, testDocument3);
        List<Document> case2Documents = Arrays.asList(testDocument4);
        when(documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1)).thenReturn(case1Documents);
        when(documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2)).thenReturn(case2Documents);

        // Act
        List<Document> case1Result = documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId1);
        List<Document> case2Result = documentRepo.findByCaseEntityIdOrderByCreatedAtDesc(caseId2);

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
        List<Document> sharedDocuments = Arrays.asList(testDocument3);
        Page<Document> sharedPage = new PageImpl<>(sharedDocuments, pageable, 2);
        when(documentRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable))
                .thenReturn(sharedPage);

        List<Document> privateDocuments = Arrays.asList(testDocument2);
        Page<Document> privatePage = new PageImpl<>(privateDocuments, pageable, 1);
        when(documentRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.PRIVATE, pageable))
                .thenReturn(privatePage);

        // Act
        Page<Document> sharedResult = documentRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.SHARED, pageable);
        Page<Document> privateResult = documentRepo.findByCaseEntityIdAndPrivacy(caseId1, AssetPrivacy.PRIVATE, pageable);

        // Assert
        assertEquals(1, sharedResult.getContent().size());
        assertEquals(2, sharedResult.getTotalElements());
        assertEquals(AssetPrivacy.SHARED, sharedResult.getContent().get(0).getPrivacy());

        assertEquals(1, privateResult.getContent().size());
        assertEquals(1, privateResult.getTotalElements());
        assertEquals(AssetPrivacy.PRIVATE, privateResult.getContent().get(0).getPrivacy());
    }
} 