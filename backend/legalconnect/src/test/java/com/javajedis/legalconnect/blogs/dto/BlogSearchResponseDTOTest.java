package com.javajedis.legalconnect.blogs.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.blogs.BlogStatus;

class BlogSearchResponseDTOTest {

    private BlogResponseDTO sampleBlog() {
        return new BlogResponseDTO(
            UUID.randomUUID(),
            new BlogResponseDTO.AuthorSummaryDTO(UUID.randomUUID(), "John", "Doe", "john@example.com"),
            "T",
            "C",
            BlogStatus.PUBLISHED,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            true
        );
    }

    @Test
    void testDefaultConstructor() {
        BlogSearchResponseDTO dto = new BlogSearchResponseDTO();
        assertNull(dto.getBlog());
        assertNull(dto.getHighlightedTitle());
        assertNull(dto.getHighlightedContent());
        // primitive boolean defaults to false
        assertEquals(false, dto.isSubscribed());
    }

    @Test
    void testAllArgsConstructor() {
        BlogResponseDTO blog = sampleBlog();
        BlogSearchResponseDTO dto = new BlogSearchResponseDTO(blog, "<em>T</em>", "<em>C</em>", true);
        assertEquals(blog, dto.getBlog());
        assertEquals("<em>T</em>", dto.getHighlightedTitle());
        assertEquals("<em>C</em>", dto.getHighlightedContent());
        assertEquals(true, dto.isSubscribed());
    }

    @Test
    void testSettersAndGetters() {
        BlogSearchResponseDTO dto = new BlogSearchResponseDTO();
        dto.setBlog(sampleBlog());
        dto.setHighlightedTitle("HT");
        dto.setHighlightedContent("HC");
        dto.setSubscribed(true);
        assertNotNull(dto.getBlog());
        assertEquals("HT", dto.getHighlightedTitle());
        assertEquals("HC", dto.getHighlightedContent());
        assertEquals(true, dto.isSubscribed());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        BlogResponseDTO blog = sampleBlog();
        BlogSearchResponseDTO d1 = new BlogSearchResponseDTO(blog, "A", "B", true);
        BlogSearchResponseDTO d2 = new BlogSearchResponseDTO(blog, "A", "B", true);
        BlogSearchResponseDTO d3 = new BlogSearchResponseDTO(sampleBlog(), "X", "Y", false);

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