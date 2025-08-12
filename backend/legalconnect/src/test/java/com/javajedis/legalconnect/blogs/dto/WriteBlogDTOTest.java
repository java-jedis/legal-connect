package com.javajedis.legalconnect.blogs.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.blogs.BlogStatus;

class WriteBlogDTOTest {

    @Test
    void testDefaultConstructor() {
        WriteBlogDTO dto = new WriteBlogDTO();
        assertNull(dto.getTitle());
        assertNull(dto.getContent());
        // Lombok default sets status to DRAFT in field declaration
        assertEquals(BlogStatus.DRAFT, dto.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        WriteBlogDTO dto = new WriteBlogDTO("T", "C", BlogStatus.PUBLISHED);
        assertEquals("T", dto.getTitle());
        assertEquals("C", dto.getContent());
        assertEquals(BlogStatus.PUBLISHED, dto.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        WriteBlogDTO dto = new WriteBlogDTO();
        dto.setTitle("Title");
        dto.setContent("Content");
        dto.setStatus(BlogStatus.ARCHIVED);
        assertEquals("Title", dto.getTitle());
        assertEquals("Content", dto.getContent());
        assertEquals(BlogStatus.ARCHIVED, dto.getStatus());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        WriteBlogDTO d1 = new WriteBlogDTO("A", "B", BlogStatus.DRAFT);
        WriteBlogDTO d2 = new WriteBlogDTO("A", "B", BlogStatus.DRAFT);
        WriteBlogDTO d3 = new WriteBlogDTO("A2", "B2", BlogStatus.PUBLISHED);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1.hashCode(), d3.hashCode());

        String ts = d1.toString();
        assertNotNull(ts);
        assert ts.contains("A");
        assert ts.contains("B");
    }
} 