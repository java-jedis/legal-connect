package com.javajedis.legalconnect.casemanagement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.CaseStatus;

class UpdateCaseStatusDTOTest {

    private UpdateCaseStatusDTO updateCaseStatusDTO;

    @BeforeEach
    void setUp() {
        updateCaseStatusDTO = new UpdateCaseStatusDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(updateCaseStatusDTO);
        assertNull(updateCaseStatusDTO.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        UpdateCaseStatusDTO dto = new UpdateCaseStatusDTO(CaseStatus.RESOLVED);

        assertEquals(CaseStatus.RESOLVED, dto.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        updateCaseStatusDTO.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, updateCaseStatusDTO.getStatus());

        updateCaseStatusDTO.setStatus(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, updateCaseStatusDTO.getStatus());
    }

    @Test
    void testSettersWithNullValue() {
        updateCaseStatusDTO.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, updateCaseStatusDTO.getStatus());

        updateCaseStatusDTO.setStatus(null);
        assertNull(updateCaseStatusDTO.getStatus());
    }

    @Test
    void testAllCaseStatusValues() {
        // Test with all possible CaseStatus values
        for (CaseStatus status : CaseStatus.values()) {
            updateCaseStatusDTO.setStatus(status);
            assertEquals(status, updateCaseStatusDTO.getStatus());
        }
    }

    @Test
    void testEqualsAndHashCode() {
        UpdateCaseStatusDTO dto1 = new UpdateCaseStatusDTO(CaseStatus.IN_PROGRESS);
        UpdateCaseStatusDTO dto2 = new UpdateCaseStatusDTO(CaseStatus.IN_PROGRESS);
        UpdateCaseStatusDTO dto3 = new UpdateCaseStatusDTO(CaseStatus.RESOLVED);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testEqualsWithNullStatus() {
        UpdateCaseStatusDTO dto1 = new UpdateCaseStatusDTO(null);
        UpdateCaseStatusDTO dto2 = new UpdateCaseStatusDTO(null);
        UpdateCaseStatusDTO dto3 = new UpdateCaseStatusDTO(CaseStatus.IN_PROGRESS);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        updateCaseStatusDTO.setStatus(CaseStatus.RESOLVED);

        String toString = updateCaseStatusDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes all fields
        assert toString.contains("RESOLVED");
    }

    @Test
    void testToStringWithNullStatus() {
        updateCaseStatusDTO.setStatus(null);

        String toString = updateCaseStatusDTO.toString();
        assertNotNull(toString);
        // Should handle null gracefully
        assert toString.contains("null") || toString.contains("status");
    }

    @Test
    void testStatusTransitions() {
        // Test transitioning between different statuses
        updateCaseStatusDTO.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, updateCaseStatusDTO.getStatus());

        updateCaseStatusDTO.setStatus(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, updateCaseStatusDTO.getStatus());

        updateCaseStatusDTO.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, updateCaseStatusDTO.getStatus());
    }

    @Test
    void testConstructorWithInProgressStatus() {
        UpdateCaseStatusDTO dto = new UpdateCaseStatusDTO(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, dto.getStatus());
    }

    @Test
    void testConstructorWithResolvedStatus() {
        UpdateCaseStatusDTO dto = new UpdateCaseStatusDTO(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, dto.getStatus());
        
        // Additional assertions to differentiate from testAllArgsConstructor
        assertNotNull(dto);
        assertEquals("Resolved", dto.getStatus().getDisplayName());
        
        // Test that it's different from IN_PROGRESS
        UpdateCaseStatusDTO inProgressDto = new UpdateCaseStatusDTO(CaseStatus.IN_PROGRESS);
        assertNotEquals(dto.getStatus(), inProgressDto.getStatus());
        assertNotEquals(dto.getStatus().getDisplayName(), inProgressDto.getStatus().getDisplayName());
    }

    @Test
    void testConstructorWithNullStatus() {
        UpdateCaseStatusDTO dto = new UpdateCaseStatusDTO(null);
        assertNull(dto.getStatus());
    }
} 