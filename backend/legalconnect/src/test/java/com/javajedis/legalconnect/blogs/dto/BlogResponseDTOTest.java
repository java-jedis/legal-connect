package com.javajedis.legalconnect.blogs.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.blogs.BlogStatus;

class BlogResponseDTOTest {

    @Test
    void testDefaultConstructor() {
        BlogResponseDTO dto = new BlogResponseDTO();
        assertNull(dto.getBlogId());
        assertNull(dto.getAuthor());
        assertNull(dto.getTitle());
        assertNull(dto.getContent());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertEquals(false, dto.isSubscribed());
    }

    @Test
    void testAllArgsConstructor() {
        UUID blogId = UUID.randomUUID();
        BlogResponseDTO.AuthorSummaryDTO author = new BlogResponseDTO.AuthorSummaryDTO(
            UUID.randomUUID(), "John", "Doe", "john@example.com"
        );
        OffsetDateTime now = OffsetDateTime.now();

        BlogResponseDTO dto = new BlogResponseDTO(
            blogId,
            author,
            "Sample Title",
            "Sample Content",
            BlogStatus.PUBLISHED,
            now,
            now,
            true
        );

        assertEquals(blogId, dto.getBlogId());
        assertEquals(author, dto.getAuthor());
        assertEquals("Sample Title", dto.getTitle());
        assertEquals("Sample Content", dto.getContent());
        assertEquals(BlogStatus.PUBLISHED, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(true, dto.isSubscribed());
    }

    @Test
    void testSettersAndGetters() {
        BlogResponseDTO dto = new BlogResponseDTO();
        UUID blogId = UUID.randomUUID();
        BlogResponseDTO.AuthorSummaryDTO author = new BlogResponseDTO.AuthorSummaryDTO();
        author.setId(UUID.randomUUID());
        author.setFirstName("Jane");
        author.setLastName("Smith");
        author.setEmail("jane@example.com");
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now();

        dto.setBlogId(blogId);
        dto.setAuthor(author);
        dto.setTitle("T1");
        dto.setContent("C1");
        dto.setStatus(BlogStatus.DRAFT);
        dto.setCreatedAt(created);
        dto.setUpdatedAt(updated);
        dto.setSubscribed(true);

        assertEquals(blogId, dto.getBlogId());
        assertEquals(author, dto.getAuthor());
        assertEquals("T1", dto.getTitle());
        assertEquals("C1", dto.getContent());
        assertEquals(BlogStatus.DRAFT, dto.getStatus());
        assertEquals(created, dto.getCreatedAt());
        assertEquals(updated, dto.getUpdatedAt());
        assertEquals(true, dto.isSubscribed());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID blogId = UUID.randomUUID();
        BlogResponseDTO.AuthorSummaryDTO author = new BlogResponseDTO.AuthorSummaryDTO(
            UUID.randomUUID(), "John", "Doe", "john@example.com"
        );
        OffsetDateTime now = OffsetDateTime.now();

        BlogResponseDTO dto1 = new BlogResponseDTO(blogId, author, "T", "C", BlogStatus.PUBLISHED, now, now, true);
        BlogResponseDTO dto2 = new BlogResponseDTO(blogId, author, "T", "C", BlogStatus.PUBLISHED, now, now, true);
        BlogResponseDTO dto3 = new BlogResponseDTO(UUID.randomUUID(), author, "T2", "C2", BlogStatus.DRAFT, now, now, false);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        BlogResponseDTO dto = new BlogResponseDTO();
        dto.setBlogId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        dto.setTitle("Title");
        dto.setContent("Content");
        String s = dto.toString();
        assertNotNull(s);
        assert s.contains("123e4567-e89b-12d3-a456-426614174000");
        assert s.contains("Title");
        assert s.contains("Content");
    }

    // Nested AuthorSummaryDTO tests
    @Test
    void testAuthorSummaryDefaultConstructor() {
        BlogResponseDTO.AuthorSummaryDTO author = new BlogResponseDTO.AuthorSummaryDTO();
        assertNull(author.getId());
        assertNull(author.getFirstName());
        assertNull(author.getLastName());
        assertNull(author.getEmail());
    }

    @Test
    void testAuthorSummaryAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        BlogResponseDTO.AuthorSummaryDTO author = new BlogResponseDTO.AuthorSummaryDTO(id, "A", "B", "a@b.c");
        assertEquals(id, author.getId());
        assertEquals("A", author.getFirstName());
        assertEquals("B", author.getLastName());
        assertEquals("a@b.c", author.getEmail());
    }

    @Test
    void testAuthorSummarySettersAndGetters() {
        BlogResponseDTO.AuthorSummaryDTO author = new BlogResponseDTO.AuthorSummaryDTO();
        UUID id = UUID.randomUUID();
        author.setId(id);
        author.setFirstName("X");
        author.setLastName("Y");
        author.setEmail("x@y.z");
        assertEquals(id, author.getId());
        assertEquals("X", author.getFirstName());
        assertEquals("Y", author.getLastName());
        assertEquals("x@y.z", author.getEmail());
    }

    @Test
    void testAuthorSummaryEqualsHashCodeAndToString() {
        UUID id = UUID.randomUUID();
        BlogResponseDTO.AuthorSummaryDTO a1 = new BlogResponseDTO.AuthorSummaryDTO(id, "F", "L", "e@x.com");
        BlogResponseDTO.AuthorSummaryDTO a2 = new BlogResponseDTO.AuthorSummaryDTO(id, "F", "L", "e@x.com");
        BlogResponseDTO.AuthorSummaryDTO a3 = new BlogResponseDTO.AuthorSummaryDTO(UUID.randomUUID(), "F2", "L2", "e2@x.com");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, a3);
        assertNotEquals(a1.hashCode(), a3.hashCode());

        String ts = a1.toString();
        assertNotNull(ts);
        assert ts.contains("F");
        assert ts.contains("L");
        assert ts.contains("e@x.com");
    }
} 