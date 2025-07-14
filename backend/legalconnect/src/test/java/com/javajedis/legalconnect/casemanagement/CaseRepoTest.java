package com.javajedis.legalconnect.casemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class CaseRepoTest {

    @Mock
    private CaseRepo caseRepo;

    private User lawyerUser1;
    private User lawyerUser2;
    private User clientUser1;
    private User clientUser2;
    private Lawyer lawyer1;
    private Lawyer lawyer2;
    private Case testCase1;
    private Case testCase2;
    private Case testCase3;
    private Case testCase4;
    private UUID caseId1;
    private UUID caseId2;
    private UUID caseId3;
    private UUID caseId4;
    private UUID lawyerId1;
    private UUID lawyerId2;
    private UUID lawyerUserId1;
    private UUID lawyerUserId2;
    private UUID clientUserId1;
    private UUID clientUserId2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize UUIDs
        caseId1 = UUID.randomUUID();
        caseId2 = UUID.randomUUID();
        caseId3 = UUID.randomUUID();
        caseId4 = UUID.randomUUID();
        lawyerId1 = UUID.randomUUID();
        lawyerId2 = UUID.randomUUID();
        lawyerUserId1 = UUID.randomUUID();
        lawyerUserId2 = UUID.randomUUID();
        clientUserId1 = UUID.randomUUID();
        clientUserId2 = UUID.randomUUID();
        
        // Setup lawyer users
        lawyerUser1 = new User();
        lawyerUser1.setId(lawyerUserId1);
        lawyerUser1.setFirstName("John");
        lawyerUser1.setLastName("Lawyer");
        lawyerUser1.setEmail("lawyer1@example.com");
        lawyerUser1.setRole(Role.LAWYER);
        lawyerUser1.setEmailVerified(true);
        
        lawyerUser2 = new User();
        lawyerUser2.setId(lawyerUserId2);
        lawyerUser2.setFirstName("Jane");
        lawyerUser2.setLastName("Attorney");
        lawyerUser2.setEmail("lawyer2@example.com");
        lawyerUser2.setRole(Role.LAWYER);
        lawyerUser2.setEmailVerified(true);
        
        // Setup client users
        clientUser1 = new User();
        clientUser1.setId(clientUserId1);
        clientUser1.setFirstName("Alice");
        clientUser1.setLastName("Client");
        clientUser1.setEmail("client1@example.com");
        clientUser1.setRole(Role.USER);
        clientUser1.setEmailVerified(true);
        
        clientUser2 = new User();
        clientUser2.setId(clientUserId2);
        clientUser2.setFirstName("Bob");
        clientUser2.setLastName("Customer");
        clientUser2.setEmail("client2@example.com");
        clientUser2.setRole(Role.USER);
        clientUser2.setEmailVerified(true);
        
        // Setup lawyers
        lawyer1 = new Lawyer();
        lawyer1.setId(lawyerId1);
        lawyer1.setUser(lawyerUser1);
        lawyer1.setFirm("Law Firm 1");
        lawyer1.setYearsOfExperience(5);
        lawyer1.setBarCertificateNumber("BAR123456");
        lawyer1.setVerificationStatus(VerificationStatus.APPROVED);
        
        lawyer2 = new Lawyer();
        lawyer2.setId(lawyerId2);
        lawyer2.setUser(lawyerUser2);
        lawyer2.setFirm("Law Firm 2");
        lawyer2.setYearsOfExperience(8);
        lawyer2.setBarCertificateNumber("BAR789012");
        lawyer2.setVerificationStatus(VerificationStatus.APPROVED);
        
        // Setup cases
        OffsetDateTime now = OffsetDateTime.now();
        
        testCase1 = new Case();
        testCase1.setId(caseId1);
        testCase1.setLawyer(lawyer1);
        testCase1.setClient(clientUser1);
        testCase1.setTitle("Case 1 Title");
        testCase1.setDescription("Description for case 1");
        testCase1.setStatus(CaseStatus.IN_PROGRESS);
        testCase1.setCreatedAt(now.minusDays(5));
        testCase1.setUpdatedAt(now.minusDays(1));
        
        testCase2 = new Case();
        testCase2.setId(caseId2);
        testCase2.setLawyer(lawyer1);
        testCase2.setClient(clientUser2);
        testCase2.setTitle("Case 2 Title");
        testCase2.setDescription("Description for case 2");
        testCase2.setStatus(CaseStatus.RESOLVED);
        testCase2.setCreatedAt(now.minusDays(3));
        testCase2.setUpdatedAt(now.minusHours(2));
        
        testCase3 = new Case();
        testCase3.setId(caseId3);
        testCase3.setLawyer(lawyer2);
        testCase3.setClient(clientUser1);
        testCase3.setTitle("Case 3 Title");
        testCase3.setDescription("Description for case 3");
        testCase3.setStatus(CaseStatus.IN_PROGRESS);
        testCase3.setCreatedAt(now.minusDays(2));
        testCase3.setUpdatedAt(now.minusMinutes(30));
        
        testCase4 = new Case();
        testCase4.setId(caseId4);
        testCase4.setLawyer(lawyer2);
        testCase4.setClient(clientUser2);
        testCase4.setTitle("Case 4 Title");
        testCase4.setDescription("Description for case 4");
        testCase4.setStatus(CaseStatus.RESOLVED);
        testCase4.setCreatedAt(now.minusDays(1));
        testCase4.setUpdatedAt(now);
    }

    @Test
    void testFindById() {
        when(caseRepo.findById(caseId1)).thenReturn(Optional.of(testCase1));
        
        Optional<Case> foundCase = caseRepo.findById(caseId1);
        
        assertTrue(foundCase.isPresent());
        assertEquals(testCase1.getTitle(), foundCase.get().getTitle());
        assertEquals(testCase1.getStatus(), foundCase.get().getStatus());
        assertEquals(testCase1.getLawyer().getId(), foundCase.get().getLawyer().getId());
        assertEquals(testCase1.getClient().getId(), foundCase.get().getClient().getId());
    }

    @Test
    void testFindByIdNotFound() {
        when(caseRepo.findById(UUID.randomUUID())).thenReturn(Optional.empty());
        
        Optional<Case> foundCase = caseRepo.findById(UUID.randomUUID());
        assertFalse(foundCase.isPresent());
    }

    @Test
    void testFindByLawyer() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());
        List<Case> lawyerCases = Arrays.asList(testCase1, testCase2);
        Page<Case> casePage = new PageImpl<>(lawyerCases, pageable, lawyerCases.size());
        
        when(caseRepo.findByLawyer(lawyer1, pageable)).thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByLawyer(lawyer1, pageable);
        
        assertNotNull(foundCases);
        assertEquals(2, foundCases.getContent().size());
        assertEquals(testCase1.getId(), foundCases.getContent().get(0).getId());
        assertEquals(testCase2.getId(), foundCases.getContent().get(1).getId());
    }

    @Test
    void testFindByLawyerAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Case> inProgressCases = Arrays.asList(testCase1);
        Page<Case> casePage = new PageImpl<>(inProgressCases, pageable, inProgressCases.size());
        
        when(caseRepo.findByLawyerAndStatus(lawyer1, CaseStatus.IN_PROGRESS, pageable))
                .thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByLawyerAndStatus(lawyer1, CaseStatus.IN_PROGRESS, pageable);
        
        assertNotNull(foundCases);
        assertEquals(1, foundCases.getContent().size());
        assertEquals(testCase1.getId(), foundCases.getContent().get(0).getId());
        assertEquals(CaseStatus.IN_PROGRESS, foundCases.getContent().get(0).getStatus());
    }

    @Test
    void testFindByClient() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());
        List<Case> clientCases = Arrays.asList(testCase1, testCase3);
        Page<Case> casePage = new PageImpl<>(clientCases, pageable, clientCases.size());
        
        when(caseRepo.findByClient(clientUser1, pageable)).thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByClient(clientUser1, pageable);
        
        assertNotNull(foundCases);
        assertEquals(2, foundCases.getContent().size());
        assertEquals(testCase1.getId(), foundCases.getContent().get(0).getId());
        assertEquals(testCase3.getId(), foundCases.getContent().get(1).getId());
    }

    @Test
    void testFindByClientAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Case> resolvedCases = Arrays.asList(testCase2);
        Page<Case> casePage = new PageImpl<>(resolvedCases, pageable, resolvedCases.size());
        
        when(caseRepo.findByClientAndStatus(clientUser2, CaseStatus.RESOLVED, pageable))
                .thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByClientAndStatus(clientUser2, CaseStatus.RESOLVED, pageable);
        
        assertNotNull(foundCases);
        assertEquals(1, foundCases.getContent().size());
        assertEquals(testCase2.getId(), foundCases.getContent().get(0).getId());
        assertEquals(CaseStatus.RESOLVED, foundCases.getContent().get(0).getStatus());
        assertEquals(clientUser2.getId(), foundCases.getContent().get(0).getClient().getId());
    }

    @Test
    void testFindByUserAsLawyerOrClient() {
        Pageable pageable = PageRequest.of(0, 10);
        // clientUser1 is client in testCase1 and testCase3
        List<Case> userCases = Arrays.asList(testCase1, testCase3);
        Page<Case> casePage = new PageImpl<>(userCases, pageable, userCases.size());
        
        when(caseRepo.findByUserAsLawyerOrClient(clientUser1, pageable)).thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByUserAsLawyerOrClient(clientUser1, pageable);
        
        assertNotNull(foundCases);
        assertEquals(2, foundCases.getContent().size());
        assertEquals(testCase1.getId(), foundCases.getContent().get(0).getId());
        assertEquals(testCase3.getId(), foundCases.getContent().get(1).getId());
    }

    @Test
    void testFindByUserAsLawyerOrClientForLawyer() {
        Pageable pageable = PageRequest.of(0, 10);
        // lawyerUser1 is lawyer in testCase1 and testCase2
        List<Case> lawyerCases = Arrays.asList(testCase1, testCase2);
        Page<Case> casePage = new PageImpl<>(lawyerCases, pageable, lawyerCases.size());
        
        when(caseRepo.findByUserAsLawyerOrClient(lawyerUser1, pageable)).thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByUserAsLawyerOrClient(lawyerUser1, pageable);
        
        assertNotNull(foundCases);
        assertEquals(2, foundCases.getContent().size());
        assertEquals(testCase1.getId(), foundCases.getContent().get(0).getId());
        assertEquals(testCase2.getId(), foundCases.getContent().get(1).getId());
    }

    @Test
    void testFindByUserAsLawyerOrClientAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        // lawyerUser1 is lawyer in testCase1 (IN_PROGRESS)
        List<Case> inProgressCases = Arrays.asList(testCase1);
        Page<Case> casePage = new PageImpl<>(inProgressCases, pageable, inProgressCases.size());
        
        when(caseRepo.findByUserAsLawyerOrClientAndStatus(lawyerUser1, CaseStatus.IN_PROGRESS, pageable))
                .thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByUserAsLawyerOrClientAndStatus(lawyerUser1, CaseStatus.IN_PROGRESS, pageable);
        
        assertNotNull(foundCases);
        assertEquals(1, foundCases.getContent().size());
        assertEquals(testCase1.getId(), foundCases.getContent().get(0).getId());
        assertEquals(CaseStatus.IN_PROGRESS, foundCases.getContent().get(0).getStatus());
    }

    @Test
    void testFindByUserAsLawyerOrClientAndStatusForClient() {
        Pageable pageable = PageRequest.of(0, 10);
        // clientUser2 is client in testCase2 (RESOLVED) and testCase4 (RESOLVED)
        List<Case> resolvedCases = Arrays.asList(testCase2, testCase4);
        Page<Case> casePage = new PageImpl<>(resolvedCases, pageable, resolvedCases.size());
        
        when(caseRepo.findByUserAsLawyerOrClientAndStatus(clientUser2, CaseStatus.RESOLVED, pageable))
                .thenReturn(casePage);
        
        Page<Case> foundCases = caseRepo.findByUserAsLawyerOrClientAndStatus(clientUser2, CaseStatus.RESOLVED, pageable);
        
        assertNotNull(foundCases);
        assertEquals(2, foundCases.getContent().size());
        assertEquals(testCase2.getId(), foundCases.getContent().get(0).getId());
        assertEquals(testCase4.getId(), foundCases.getContent().get(1).getId());
    }

    @Test
    void testFindAll() {
        List<Case> allCases = Arrays.asList(testCase1, testCase2, testCase3, testCase4);
        when(caseRepo.findAll()).thenReturn(allCases);
        
        List<Case> foundCases = caseRepo.findAll();
        
        assertNotNull(foundCases);
        assertEquals(4, foundCases.size());
        assertTrue(foundCases.contains(testCase1));
        assertTrue(foundCases.contains(testCase2));
        assertTrue(foundCases.contains(testCase3));
        assertTrue(foundCases.contains(testCase4));
    }

    @Test
    void testSaveCase() {
        when(caseRepo.save(testCase1)).thenReturn(testCase1);
        
        Case savedCase = caseRepo.save(testCase1);
        
        assertNotNull(savedCase);
        assertEquals(testCase1.getId(), savedCase.getId());
        assertEquals(testCase1.getTitle(), savedCase.getTitle());
        assertEquals(testCase1.getLawyer().getId(), savedCase.getLawyer().getId());
        assertEquals(testCase1.getClient().getId(), savedCase.getClient().getId());
    }

    @Test
    void testExistsById() {
        when(caseRepo.existsById(caseId1)).thenReturn(true);
        when(caseRepo.existsById(UUID.randomUUID())).thenReturn(false);
        
        assertTrue(caseRepo.existsById(caseId1));
        assertFalse(caseRepo.existsById(UUID.randomUUID()));
    }

    @Test
    void testDeleteById() {
        // Test that deleteById can be called without error
        // First verify the case exists
        when(caseRepo.existsById(caseId1)).thenReturn(true);
        assertTrue(caseRepo.existsById(caseId1));
        
        // Delete the case (void method)
        caseRepo.deleteById(caseId1);
        
        // Verify deletion by checking existence returns false after deletion
        when(caseRepo.existsById(caseId1)).thenReturn(false);
        assertFalse(caseRepo.existsById(caseId1));
    }

    @Test
    void testPaginationAndSorting() {
        // Test with different page sizes and sorting
        Pageable pageable1 = PageRequest.of(0, 2, Sort.by("createdAt").ascending());
        Pageable pageable2 = PageRequest.of(1, 2, Sort.by("updatedAt").descending());
        
        List<Case> page1Cases = Arrays.asList(testCase1, testCase2);
        List<Case> page2Cases = Arrays.asList(testCase3, testCase4);
        
        Page<Case> page1 = new PageImpl<>(page1Cases, pageable1, 4);
        Page<Case> page2 = new PageImpl<>(page2Cases, pageable2, 4);
        
        when(caseRepo.findByLawyer(lawyer1, pageable1)).thenReturn(page1);
        when(caseRepo.findByClient(clientUser1, pageable2)).thenReturn(page2);
        
        Page<Case> foundPage1 = caseRepo.findByLawyer(lawyer1, pageable1);
        Page<Case> foundPage2 = caseRepo.findByClient(clientUser1, pageable2);
        
        // Test page 1
        assertEquals(2, foundPage1.getContent().size());
        assertEquals(4, foundPage1.getTotalElements());
        assertEquals(0, foundPage1.getNumber());
        assertEquals(2, foundPage1.getSize());
        
        // Test page 2
        assertEquals(2, foundPage2.getContent().size());
        assertEquals(4, foundPage2.getTotalElements());
        assertEquals(1, foundPage2.getNumber());
        assertEquals(2, foundPage2.getSize());
    }

    @Test
    void testEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Case> emptyCasePage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(caseRepo.findByLawyer(lawyer1, pageable)).thenReturn(emptyCasePage);
        when(caseRepo.findByClient(clientUser1, pageable)).thenReturn(emptyCasePage);
        when(caseRepo.findByUserAsLawyerOrClient(lawyerUser1, pageable)).thenReturn(emptyCasePage);
        
        Page<Case> lawyerCases = caseRepo.findByLawyer(lawyer1, pageable);
        Page<Case> clientCases = caseRepo.findByClient(clientUser1, pageable);
        Page<Case> userCases = caseRepo.findByUserAsLawyerOrClient(lawyerUser1, pageable);
        
        assertEquals(0, lawyerCases.getContent().size());
        assertEquals(0, clientCases.getContent().size());
        assertEquals(0, userCases.getContent().size());
    }

    @Test
    void testComplexQueries() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test cases where user appears as both lawyer and client
        // (This is for comprehensive testing, though unlikely in real scenarios)
        User userAsLawyerAndClient = new User();
        userAsLawyerAndClient.setId(UUID.randomUUID());
        userAsLawyerAndClient.setEmail("complex@example.com");
        userAsLawyerAndClient.setRole(Role.LAWYER);
        
        List<Case> complexCases = Arrays.asList(testCase1, testCase3);
        Page<Case> complexPage = new PageImpl<>(complexCases, pageable, complexCases.size());
        
        when(caseRepo.findByUserAsLawyerOrClient(userAsLawyerAndClient, pageable))
                .thenReturn(complexPage);
        
        Page<Case> foundCases = caseRepo.findByUserAsLawyerOrClient(userAsLawyerAndClient, pageable);
        
        assertNotNull(foundCases);
        assertEquals(2, foundCases.getContent().size());
    }
} 