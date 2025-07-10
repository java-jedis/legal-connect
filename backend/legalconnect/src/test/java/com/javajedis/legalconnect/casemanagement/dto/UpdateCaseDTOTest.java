package com.javajedis.legalconnect.casemanagement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateCaseDTOTest {

    private UpdateCaseDTO updateCaseDTO;

    @BeforeEach
    void setUp() {
        updateCaseDTO = new UpdateCaseDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(updateCaseDTO);
        assertNull(updateCaseDTO.getTitle());
        assertNull(updateCaseDTO.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        UpdateCaseDTO dto = new UpdateCaseDTO(
            "Updated Case Title",
            "Updated case description"
        );

        assertEquals("Updated Case Title", dto.getTitle());
        assertEquals("Updated case description", dto.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        updateCaseDTO.setTitle("Test Case Title");
        updateCaseDTO.setDescription("Test description");

        assertEquals("Test Case Title", updateCaseDTO.getTitle());
        assertEquals("Test description", updateCaseDTO.getDescription());
    }

    @Test
    void testSettersWithNullValues() {
        updateCaseDTO.setTitle(null);
        updateCaseDTO.setDescription(null);

        assertNull(updateCaseDTO.getTitle());
        assertNull(updateCaseDTO.getDescription());
    }

    @Test
    void testSettersWithEmptyValues() {
        updateCaseDTO.setTitle("");
        updateCaseDTO.setDescription("");

        assertEquals("", updateCaseDTO.getTitle());
        assertEquals("", updateCaseDTO.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        UpdateCaseDTO dto1 = new UpdateCaseDTO(
            "Test Title",
            "Test description"
        );
        
        UpdateCaseDTO dto2 = new UpdateCaseDTO(
            "Test Title",
            "Test description"
        );
        
        UpdateCaseDTO dto3 = new UpdateCaseDTO(
            "Different Title",
            "Different description"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        updateCaseDTO.setTitle("Test Title");
        updateCaseDTO.setDescription("Test description");

        String toString = updateCaseDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes all fields
        assert toString.contains("Test Title");
        assert toString.contains("Test description");
    }

    @Test
    void testLongTitle() {
        // Test with title at max length (255 characters)
        String longTitle = "T".repeat(255);
        updateCaseDTO.setTitle(longTitle);
        assertEquals(longTitle, updateCaseDTO.getTitle());
        assertEquals(255, updateCaseDTO.getTitle().length());
    }

    @Test
    void testLongDescription() {
        // Test with description at max length (2000 characters)
        String longDescription = "D".repeat(2000);
        updateCaseDTO.setDescription(longDescription);
        assertEquals(longDescription, updateCaseDTO.getDescription());
        assertEquals(2000, updateCaseDTO.getDescription().length());
    }

    @Test
    void testMinimalValidData() {
        // Test with minimum required data
        updateCaseDTO.setTitle("Ab"); // 2 characters minimum
        updateCaseDTO.setDescription(null); // Description is optional

        assertEquals("Ab", updateCaseDTO.getTitle());
        assertNull(updateCaseDTO.getDescription());
    }

    @Test
    void testDescriptionOnly() {
        // Test updating only description
        updateCaseDTO.setDescription("Only description updated");
        
        assertNull(updateCaseDTO.getTitle()); // Title not set
        assertEquals("Only description updated", updateCaseDTO.getDescription());
    }

    @Test
    void testTitleOnly() {
        // Test updating only title
        updateCaseDTO.setTitle("Only title updated");
        
        assertEquals("Only title updated", updateCaseDTO.getTitle());
        assertNull(updateCaseDTO.getDescription()); // Description not set
    }
} 