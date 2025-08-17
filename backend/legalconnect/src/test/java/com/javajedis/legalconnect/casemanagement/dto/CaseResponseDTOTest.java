package com.javajedis.legalconnect.casemanagement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO.ClientSummaryDTO;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO.LawyerSummaryDTO;

class CaseResponseDTOTest {

    private CaseResponseDTO caseResponseDTO;
    private UUID testCaseId;
    private UUID testLawyerId;
    private UUID testClientId;
    private OffsetDateTime testDateTime;

    @BeforeEach
    void setUp() {
        caseResponseDTO = new CaseResponseDTO();
        testCaseId = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testClientId = UUID.randomUUID();
        testDateTime = OffsetDateTime.now();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(caseResponseDTO);
        assertNull(caseResponseDTO.getCaseId());
        assertNull(caseResponseDTO.getLawyer());
        assertNull(caseResponseDTO.getClient());
        assertNull(caseResponseDTO.getTitle());
        assertNull(caseResponseDTO.getDescription());
        assertNull(caseResponseDTO.getStatus());
        assertNull(caseResponseDTO.getCreatedAt());
        assertNull(caseResponseDTO.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");
        ClientSummaryDTO client = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");

        CaseResponseDTO dto = new CaseResponseDTO(
            testCaseId,
            lawyer,
            client,
            "Test Case",
            "Test Description",
            CaseStatus.IN_PROGRESS,
            testDateTime,
            testDateTime
        );

        assertEquals(testCaseId, dto.getCaseId());
        assertEquals(lawyer, dto.getLawyer());
        assertEquals(client, dto.getClient());
        assertEquals("Test Case", dto.getTitle());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(CaseStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(testDateTime, dto.getCreatedAt());
        assertEquals(testDateTime, dto.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");
        ClientSummaryDTO client = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");

        caseResponseDTO.setCaseId(testCaseId);
        caseResponseDTO.setLawyer(lawyer);
        caseResponseDTO.setClient(client);
        caseResponseDTO.setTitle("Test Case");
        caseResponseDTO.setDescription("Test Description");
        caseResponseDTO.setStatus(CaseStatus.RESOLVED);
        caseResponseDTO.setCreatedAt(testDateTime);
        caseResponseDTO.setUpdatedAt(testDateTime);

        assertEquals(testCaseId, caseResponseDTO.getCaseId());
        assertEquals(lawyer, caseResponseDTO.getLawyer());
        assertEquals(client, caseResponseDTO.getClient());
        assertEquals("Test Case", caseResponseDTO.getTitle());
        assertEquals("Test Description", caseResponseDTO.getDescription());
        assertEquals(CaseStatus.RESOLVED, caseResponseDTO.getStatus());
        assertEquals(testDateTime, caseResponseDTO.getCreatedAt());
        assertEquals(testDateTime, caseResponseDTO.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");
        ClientSummaryDTO client = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");

        CaseResponseDTO dto1 = new CaseResponseDTO(testCaseId, lawyer, client, "Test Case", "Description", CaseStatus.IN_PROGRESS, testDateTime, testDateTime);
        CaseResponseDTO dto2 = new CaseResponseDTO(testCaseId, lawyer, client, "Test Case", "Description", CaseStatus.IN_PROGRESS, testDateTime, testDateTime);
        CaseResponseDTO dto3 = new CaseResponseDTO(UUID.randomUUID(), lawyer, client, "Different Case", "Description", CaseStatus.RESOLVED, testDateTime, testDateTime);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");
        ClientSummaryDTO client = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");

        caseResponseDTO.setCaseId(testCaseId);
        caseResponseDTO.setLawyer(lawyer);
        caseResponseDTO.setClient(client);
        caseResponseDTO.setTitle("Test Case");

        String toString = caseResponseDTO.toString();
        assertNotNull(toString);
        assert toString.contains("Test Case");
    }

    @Test
    void testWithNullValues() {
        caseResponseDTO.setCaseId(null);
        caseResponseDTO.setLawyer(null);
        caseResponseDTO.setClient(null);
        caseResponseDTO.setTitle(null);
        caseResponseDTO.setDescription(null);
        caseResponseDTO.setStatus(null);
        caseResponseDTO.setCreatedAt(null);
        caseResponseDTO.setUpdatedAt(null);

        assertNull(caseResponseDTO.getCaseId());
        assertNull(caseResponseDTO.getLawyer());
        assertNull(caseResponseDTO.getClient());
        assertNull(caseResponseDTO.getTitle());
        assertNull(caseResponseDTO.getDescription());
        assertNull(caseResponseDTO.getStatus());
        assertNull(caseResponseDTO.getCreatedAt());
        assertNull(caseResponseDTO.getUpdatedAt());
    }

    // LawyerSummaryDTO Tests
    @Test
    void testLawyerSummaryDTODefaultConstructor() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO();
        assertNotNull(lawyer);
        assertNull(lawyer.getId());
        assertNull(lawyer.getFirstName());
        assertNull(lawyer.getLastName());
        assertNull(lawyer.getEmail());
        assertNull(lawyer.getFirm());
    }

    @Test
    void testLawyerSummaryDTOAllArgsConstructor() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");

        assertEquals(testLawyerId, lawyer.getId());
        assertEquals("John", lawyer.getFirstName());
        assertEquals("Doe", lawyer.getLastName());
        assertEquals("john.doe@law.com", lawyer.getEmail());
        assertEquals("Doe & Associates", lawyer.getFirm());
    }

    @Test
    void testLawyerSummaryDTOSettersAndGetters() {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO();
        
        lawyer.setId(testLawyerId);
        lawyer.setFirstName("John");
        lawyer.setLastName("Doe");
        lawyer.setEmail("john.doe@law.com");
        lawyer.setFirm("Doe & Associates");

        assertEquals(testLawyerId, lawyer.getId());
        assertEquals("John", lawyer.getFirstName());
        assertEquals("Doe", lawyer.getLastName());
        assertEquals("john.doe@law.com", lawyer.getEmail());
        assertEquals("Doe & Associates", lawyer.getFirm());
    }

    @Test
    void testLawyerSummaryDTOEqualsAndHashCode() {
        LawyerSummaryDTO lawyer1 = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");
        LawyerSummaryDTO lawyer2 = new LawyerSummaryDTO(testLawyerId, "John", "Doe", "john.doe@law.com", "Doe & Associates");
        LawyerSummaryDTO lawyer3 = new LawyerSummaryDTO(UUID.randomUUID(), "Jane", "Smith", "jane.smith@law.com", "Smith & Co");

        assertEquals(lawyer1, lawyer2);
        assertEquals(lawyer1.hashCode(), lawyer2.hashCode());
        assertNotEquals(lawyer1, lawyer3);
        assertNotEquals(lawyer1.hashCode(), lawyer3.hashCode());
    }

    // ClientSummaryDTO Tests
    @Test
    void testClientSummaryDTODefaultConstructor() {
        ClientSummaryDTO client = new ClientSummaryDTO();
        assertNotNull(client);
        assertNull(client.getId());
        assertNull(client.getFirstName());
        assertNull(client.getLastName());
        assertNull(client.getEmail());
    }

    @Test
    void testClientSummaryDTOAllArgsConstructor() {
        ClientSummaryDTO client = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");

        assertEquals(testClientId, client.getId());
        assertEquals("Jane", client.getFirstName());
        assertEquals("Smith", client.getLastName());
        assertEquals("jane.smith@email.com", client.getEmail());
    }

    @Test
    void testClientSummaryDTOSettersAndGetters() {
        ClientSummaryDTO client = new ClientSummaryDTO();
        
        client.setId(testClientId);
        client.setFirstName("Jane");
        client.setLastName("Smith");
        client.setEmail("jane.smith@email.com");

        assertEquals(testClientId, client.getId());
        assertEquals("Jane", client.getFirstName());
        assertEquals("Smith", client.getLastName());
        assertEquals("jane.smith@email.com", client.getEmail());
    }

    @Test
    void testClientSummaryDTOEqualsAndHashCode() {
        ClientSummaryDTO client1 = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");
        ClientSummaryDTO client2 = new ClientSummaryDTO(testClientId, "Jane", "Smith", "jane.smith@email.com");
        ClientSummaryDTO client3 = new ClientSummaryDTO(UUID.randomUUID(), "John", "Doe", "john.doe@email.com");

        assertEquals(client1, client2);
        assertEquals(client1.hashCode(), client2.hashCode());
        assertNotEquals(client1, client3);
        assertNotEquals(client1.hashCode(), client3.hashCode());
    }

    @Test
    void testCaseStatusValues() {
        // Test with different CaseStatus values
        caseResponseDTO.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, caseResponseDTO.getStatus());

        caseResponseDTO.setStatus(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, caseResponseDTO.getStatus());
    }
} 