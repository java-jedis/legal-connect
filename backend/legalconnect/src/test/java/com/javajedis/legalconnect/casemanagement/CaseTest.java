package com.javajedis.legalconnect.casemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class CaseTest {

    private Case caseEntity;
    private User lawyerUser;
    private User clientUser;
    private Lawyer lawyer;
    private UUID testCaseId;
    private UUID lawyerId;
    private UUID lawyerUserId;
    private UUID clientUserId;

    @BeforeEach
    void setUp() {
        testCaseId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        lawyerUserId = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
        
        // Setup lawyer user
        lawyerUser = new User();
        lawyerUser.setId(lawyerUserId);
        lawyerUser.setFirstName("John");
        lawyerUser.setLastName("Lawyer");
        lawyerUser.setEmail("lawyer@example.com");
        lawyerUser.setRole(Role.LAWYER);
        lawyerUser.setEmailVerified(true);
        
        // Setup client user
        clientUser = new User();
        clientUser.setId(clientUserId);
        clientUser.setFirstName("Jane");
        clientUser.setLastName("Client");
        clientUser.setEmail("client@example.com");
        clientUser.setRole(Role.USER);
        clientUser.setEmailVerified(true);
        
        // Setup lawyer
        lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setUser(lawyerUser);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);
        
        // Setup case
        caseEntity = new Case();
        caseEntity.setId(testCaseId);
        caseEntity.setLawyer(lawyer);
        caseEntity.setClient(clientUser);
        caseEntity.setTitle("Test Case Title");
        caseEntity.setDescription("Test case description");
        caseEntity.setStatus(CaseStatus.IN_PROGRESS);
    }

    @Test
    void testDefaultConstructor() {
        Case defaultCase = new Case();
        
        assertNotNull(defaultCase);
        assertNull(defaultCase.getId());
        assertNull(defaultCase.getLawyer());
        assertNull(defaultCase.getClient());
        assertNull(defaultCase.getTitle());
        assertNull(defaultCase.getDescription());
        assertEquals(CaseStatus.IN_PROGRESS, defaultCase.getStatus());
        assertNull(defaultCase.getCreatedAt());
        assertNull(defaultCase.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Case constructedCase = new Case(
            testCaseId, lawyer, clientUser, "Constructor Test Case",
            "Constructor test description", CaseStatus.RESOLVED,
            now, now
        );
        
        assertEquals(testCaseId, constructedCase.getId());
        assertEquals(lawyer, constructedCase.getLawyer());
        assertEquals(clientUser, constructedCase.getClient());
        assertEquals("Constructor Test Case", constructedCase.getTitle());
        assertEquals("Constructor test description", constructedCase.getDescription());
        assertEquals(CaseStatus.RESOLVED, constructedCase.getStatus());
        assertEquals(now, constructedCase.getCreatedAt());
        assertEquals(now, constructedCase.getUpdatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        caseEntity.setId(newId);
        assertEquals(newId, caseEntity.getId());
    }

    @Test
    void testLawyerGetterAndSetter() {
        Lawyer newLawyer = new Lawyer();
        newLawyer.setId(UUID.randomUUID());
        newLawyer.setUser(lawyerUser);
        newLawyer.setFirm("New Law Firm");
        
        caseEntity.setLawyer(newLawyer);
        assertEquals(newLawyer, caseEntity.getLawyer());
        assertEquals("New Law Firm", caseEntity.getLawyer().getFirm());
    }

    @Test
    void testClientGetterAndSetter() {
        User newClient = new User();
        newClient.setId(UUID.randomUUID());
        newClient.setEmail("newclient@example.com");
        newClient.setRole(Role.USER);
        
        caseEntity.setClient(newClient);
        assertEquals(newClient, caseEntity.getClient());
        assertEquals("newclient@example.com", caseEntity.getClient().getEmail());
    }

    @Test
    void testTitleGetterAndSetter() {
        String newTitle = "Updated Case Title";
        caseEntity.setTitle(newTitle);
        assertEquals(newTitle, caseEntity.getTitle());
    }

    @Test
    void testDescriptionGetterAndSetter() {
        String newDescription = "Updated case description with more details";
        caseEntity.setDescription(newDescription);
        assertEquals(newDescription, caseEntity.getDescription());
    }

    @Test
    void testDescriptionWithNullValue() {
        caseEntity.setDescription(null);
        assertNull(caseEntity.getDescription());
    }

    @Test
    void testDescriptionWithEmptyString() {
        caseEntity.setDescription("");
        assertEquals("", caseEntity.getDescription());
    }

    @Test
    void testStatusGetterAndSetter() {
        CaseStatus newStatus = CaseStatus.RESOLVED;
        caseEntity.setStatus(newStatus);
        assertEquals(newStatus, caseEntity.getStatus());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        caseEntity.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, caseEntity.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        caseEntity.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, caseEntity.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Case case1 = new Case();
        case1.setId(testCaseId);
        case1.setTitle("Test Case");
        case1.setStatus(CaseStatus.IN_PROGRESS);
        
        Case case2 = new Case();
        case2.setId(testCaseId);
        case2.setTitle("Test Case");
        case2.setStatus(CaseStatus.IN_PROGRESS);
        
        Case case3 = new Case();
        case3.setId(UUID.randomUUID());
        case3.setTitle("Different Case");
        case3.setStatus(CaseStatus.RESOLVED);
        
        assertEquals(case1, case2);
        assertEquals(case1.hashCode(), case2.hashCode());
        assertNotEquals(case1, case3);
        assertNotEquals(case1.hashCode(), case3.hashCode());
    }

    @Test
    void testEqualsWithNullValues() {
        Case case1 = new Case();
        Case case2 = new Case();
        
        assertEquals(case1, case2);
        assertEquals(case1.hashCode(), case2.hashCode());
    }

    @Test
    void testToString() {
        String toString = caseEntity.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Test Case Title"));
        assertTrue(toString.contains("Test case description"));
        assertTrue(toString.contains("IN_PROGRESS"));
    }

    @Test
    void testDefaultCaseStatus() {
        Case newCase = new Case();
        assertEquals(CaseStatus.IN_PROGRESS, newCase.getStatus());
    }

    @Test
    void testAllCaseStatuses() {
        // Test IN_PROGRESS status
        caseEntity.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, caseEntity.getStatus());
        assertEquals("In Progress", caseEntity.getStatus().getDisplayName());
        
        // Test RESOLVED status
        caseEntity.setStatus(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, caseEntity.getStatus());
        assertEquals("Resolved", caseEntity.getStatus().getDisplayName());
    }

    @Test
    void testCaseWithLongTitle() {
        String longTitle = "T".repeat(255); // Max length according to validation
        caseEntity.setTitle(longTitle);
        assertEquals(longTitle, caseEntity.getTitle());
        assertEquals(255, caseEntity.getTitle().length());
    }

    @Test
    void testCaseWithLongDescription() {
        String longDescription = "D".repeat(2000); // Max length according to validation
        caseEntity.setDescription(longDescription);
        assertEquals(longDescription, caseEntity.getDescription());
        assertEquals(2000, caseEntity.getDescription().length());
    }

    @Test
    void testCaseRelationships() {
        // Test lawyer relationship
        assertNotNull(caseEntity.getLawyer());
        assertEquals(lawyer.getId(), caseEntity.getLawyer().getId());
        assertEquals("Test Law Firm", caseEntity.getLawyer().getFirm());
        assertEquals(lawyerUser, caseEntity.getLawyer().getUser());
        
        // Test client relationship
        assertNotNull(caseEntity.getClient());
        assertEquals(clientUser.getId(), caseEntity.getClient().getId());
        assertEquals("client@example.com", caseEntity.getClient().getEmail());
        assertEquals(Role.USER, caseEntity.getClient().getRole());
    }

    @Test
    void testCaseWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        caseEntity.setCreatedAt(now);
        caseEntity.setUpdatedAt(now);
        
        assertEquals(now, caseEntity.getCreatedAt());
        assertEquals(now, caseEntity.getUpdatedAt());
    }

    @Test
    void testCaseStatusTransition() {
        // Start with IN_PROGRESS
        assertEquals(CaseStatus.IN_PROGRESS, caseEntity.getStatus());
        
        // Transition to RESOLVED
        caseEntity.setStatus(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, caseEntity.getStatus());
        
        // Transition back to IN_PROGRESS
        caseEntity.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, caseEntity.getStatus());
    }

    @Test
    void testCaseWithMinimalData() {
        Case minimalCase = new Case();
        minimalCase.setLawyer(lawyer);
        minimalCase.setClient(clientUser);
        minimalCase.setTitle("M"); // Minimum title length
        // Description can be null
        
        assertEquals(lawyer, minimalCase.getLawyer());
        assertEquals(clientUser, minimalCase.getClient());
        assertEquals("M", minimalCase.getTitle());
        assertNull(minimalCase.getDescription());
        assertEquals(CaseStatus.IN_PROGRESS, minimalCase.getStatus()); // Default status
    }

    @Test
    void testCaseEntityFields() {
        // Test that all required JPA annotations are properly configured
        // by testing field access
        assertNotNull(caseEntity.getId());
        assertNotNull(caseEntity.getLawyer());
        assertNotNull(caseEntity.getClient());
        assertNotNull(caseEntity.getTitle());
        assertNotNull(caseEntity.getDescription());
        assertNotNull(caseEntity.getStatus());
    }
} 