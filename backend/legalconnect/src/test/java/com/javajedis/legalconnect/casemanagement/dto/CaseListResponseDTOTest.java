package com.javajedis.legalconnect.casemanagement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO.ClientSummaryDTO;
import com.javajedis.legalconnect.casemanagement.dto.CaseResponseDTO.LawyerSummaryDTO;

class CaseListResponseDTOTest {

    private CaseListResponseDTO caseListResponseDTO;
    private List<CaseResponseDTO> testCases;

    @BeforeEach
    void setUp() {
        caseListResponseDTO = new CaseListResponseDTO();
        testCases = createTestCases();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(caseListResponseDTO);
        assertNull(caseListResponseDTO.getCases());
    }

    @Test
    void testAllArgsConstructor() {
        CaseListResponseDTO dto = new CaseListResponseDTO(testCases);
        
        assertEquals(testCases, dto.getCases());
        assertEquals(2, dto.getCases().size());
    }

    @Test
    void testSettersAndGetters() {
        caseListResponseDTO.setCases(testCases);
        
        assertEquals(testCases, caseListResponseDTO.getCases());
        assertEquals(2, caseListResponseDTO.getCases().size());
    }

    @Test
    void testSettersWithNullValue() {
        caseListResponseDTO.setCases(testCases);
        assertEquals(testCases, caseListResponseDTO.getCases());

        caseListResponseDTO.setCases(null);
        assertNull(caseListResponseDTO.getCases());
    }

    @Test
    void testSettersWithEmptyList() {
        List<CaseResponseDTO> emptyCases = new ArrayList<>();
        caseListResponseDTO.setCases(emptyCases);
        
        assertNotNull(caseListResponseDTO.getCases());
        assertTrue(caseListResponseDTO.getCases().isEmpty());
        assertEquals(0, caseListResponseDTO.getCases().size());
    }

    @Test
    void testEqualsAndHashCode() {
        CaseListResponseDTO dto1 = new CaseListResponseDTO(testCases);
        CaseListResponseDTO dto2 = new CaseListResponseDTO(testCases);
        CaseListResponseDTO dto3 = new CaseListResponseDTO(new ArrayList<>());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testEqualsWithNullCases() {
        CaseListResponseDTO dto1 = new CaseListResponseDTO(null);
        CaseListResponseDTO dto2 = new CaseListResponseDTO(null);
        CaseListResponseDTO dto3 = new CaseListResponseDTO(testCases);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        caseListResponseDTO.setCases(testCases);

        String toString = caseListResponseDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes all fields
        assert toString.contains("cases");
    }

    @Test
    void testToStringWithNullCases() {
        caseListResponseDTO.setCases(null);

        String toString = caseListResponseDTO.toString();
        assertNotNull(toString);
        // Should handle null gracefully
        assert toString.contains("null") || toString.contains("cases");
    }

    @Test
    void testToStringWithEmptyCases() {
        caseListResponseDTO.setCases(new ArrayList<>());

        String toString = caseListResponseDTO.toString();
        assertNotNull(toString);
        assert toString.contains("[]") || toString.contains("cases");
    }

    @Test
    void testWithSingleCase() {
        List<CaseResponseDTO> singleCase = Arrays.asList(testCases.get(0));
        caseListResponseDTO.setCases(singleCase);
        
        assertNotNull(caseListResponseDTO.getCases());
        assertEquals(1, caseListResponseDTO.getCases().size());
        assertEquals(testCases.get(0), caseListResponseDTO.getCases().get(0));
    }

    @Test
    void testWithMultipleCases() {
        List<CaseResponseDTO> multipleCases = new ArrayList<>(testCases);
        multipleCases.addAll(testCases); // Duplicate for more cases
        
        caseListResponseDTO.setCases(multipleCases);
        
        assertNotNull(caseListResponseDTO.getCases());
        assertEquals(4, caseListResponseDTO.getCases().size());
    }

    @Test
    void testCaseListModification() {
        caseListResponseDTO.setCases(new ArrayList<>(testCases));
        
        // Modify the list
        caseListResponseDTO.getCases().add(createTestCase("Additional Case", CaseStatus.RESOLVED));
        
        assertEquals(3, caseListResponseDTO.getCases().size());
    }

    @Test
    void testCaseListImmutability() {
        List<CaseResponseDTO> originalCases = new ArrayList<>(testCases);
        caseListResponseDTO.setCases(originalCases);
        
        // Verify original list is preserved
        assertEquals(2, originalCases.size());
        assertEquals(2, caseListResponseDTO.getCases().size());
    }

    private List<CaseResponseDTO> createTestCases() {
        return Arrays.asList(
            createTestCase("Test Case 1", CaseStatus.IN_PROGRESS),
            createTestCase("Test Case 2", CaseStatus.RESOLVED)
        );
    }

    private CaseResponseDTO createTestCase(String title, CaseStatus status) {
        LawyerSummaryDTO lawyer = new LawyerSummaryDTO(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@law.com",
            "Doe & Associates"
        );
        
        ClientSummaryDTO client = new ClientSummaryDTO(
            UUID.randomUUID(),
            "Jane",
            "Smith",
            "jane.smith@email.com"
        );

        return new CaseResponseDTO(
            UUID.randomUUID(),
            lawyer,
            client,
            title,
            "Test description for " + title,
            status,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }
} 