package com.javajedis.legalconnect.casemanagement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateCaseDTOTest {

    private CreateCaseDTO createCaseDTO;

    @BeforeEach
    void setUp() {
        createCaseDTO = new CreateCaseDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(createCaseDTO);
        assertNull(createCaseDTO.getTitle());
        assertNull(createCaseDTO.getClientEmail());
        assertNull(createCaseDTO.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        CreateCaseDTO dto = new CreateCaseDTO(
            "Test Case Title",
            "client@example.com",
            "Test case description"
        );

        assertEquals("Test Case Title", dto.getTitle());
        assertEquals("client@example.com", dto.getClientEmail());
        assertEquals("Test case description", dto.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        createCaseDTO.setTitle("Updated Case Title");
        createCaseDTO.setClientEmail("updated@example.com");
        createCaseDTO.setDescription("Updated description");

        assertEquals("Updated Case Title", createCaseDTO.getTitle());
        assertEquals("updated@example.com", createCaseDTO.getClientEmail());
        assertEquals("Updated description", createCaseDTO.getDescription());
    }

    @Test
    void testSettersWithNullValues() {
        createCaseDTO.setTitle(null);
        createCaseDTO.setClientEmail(null);
        createCaseDTO.setDescription(null);

        assertNull(createCaseDTO.getTitle());
        assertNull(createCaseDTO.getClientEmail());
        assertNull(createCaseDTO.getDescription());
    }

    @Test
    void testSettersWithEmptyValues() {
        createCaseDTO.setTitle("");
        createCaseDTO.setClientEmail("");
        createCaseDTO.setDescription("");

        assertEquals("", createCaseDTO.getTitle());
        assertEquals("", createCaseDTO.getClientEmail());
        assertEquals("", createCaseDTO.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        CreateCaseDTO dto1 = new CreateCaseDTO(
            "Test Title",
            "test@example.com",
            "Test description"
        );
        
        CreateCaseDTO dto2 = new CreateCaseDTO(
            "Test Title",
            "test@example.com",
            "Test description"
        );
        
        CreateCaseDTO dto3 = new CreateCaseDTO(
            "Different Title",
            "different@example.com",
            "Different description"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        createCaseDTO.setTitle("Test Title");
        createCaseDTO.setClientEmail("test@example.com");
        createCaseDTO.setDescription("Test description");

        String toString = createCaseDTO.toString();
        assertNotNull(toString);
        // Lombok toString includes all fields
        assert toString.contains("Test Title");
        assert toString.contains("test@example.com");
        assert toString.contains("Test description");
    }

    @Test
    void testValidEmailFormats() {
        // Test various valid email formats
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "test+tag@example.org",
            "123@example.com"
        };

        for (String email : validEmails) {
            createCaseDTO.setClientEmail(email);
            assertEquals(email, createCaseDTO.getClientEmail());
        }
    }

    @Test
    void testLongTitle() {
        // Test with title at max length (255 characters)
        String longTitle = "T".repeat(255);
        createCaseDTO.setTitle(longTitle);
        assertEquals(longTitle, createCaseDTO.getTitle());
        assertEquals(255, createCaseDTO.getTitle().length());
    }

    @Test
    void testLongDescription() {
        // Test with description at max length (2000 characters)
        String longDescription = "D".repeat(2000);
        createCaseDTO.setDescription(longDescription);
        assertEquals(longDescription, createCaseDTO.getDescription());
        assertEquals(2000, createCaseDTO.getDescription().length());
    }

    @Test
    void testMinimalValidData() {
        // Test with minimum required data
        createCaseDTO.setTitle("Ab"); // 2 characters minimum
        createCaseDTO.setClientEmail("a@b.c");
        createCaseDTO.setDescription(null); // Description is optional

        assertEquals("Ab", createCaseDTO.getTitle());
        assertEquals("a@b.c", createCaseDTO.getClientEmail());
        assertNull(createCaseDTO.getDescription());
    }
} 