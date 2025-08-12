package com.javajedis.legalconnect.blogs.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EsBlogTest {

    private EsBlog esBlog;

    private UUID id;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        authorId = UUID.randomUUID();

        esBlog = new EsBlog();
        esBlog.setId(id);
        esBlog.setAuthorId(authorId);
        esBlog.setAuthorFirstName("John");
        esBlog.setAuthorLastName("Doe");
        esBlog.setAuthorEmail("john@example.com");
        esBlog.setTitle("Sample Title");
        esBlog.setContent("Sample Content");
        esBlog.setStatus("PUBLISHED");
        esBlog.setCreatedAt(OffsetDateTime.now().minusDays(1));
        esBlog.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testDefaultConstructor() {
        EsBlog b = new EsBlog();
        assertNotNull(b);
        assertNull(b.getId());
        assertNull(b.getAuthorId());
        assertNull(b.getAuthorFirstName());
        assertNull(b.getAuthorLastName());
        assertNull(b.getAuthorEmail());
        assertNull(b.getTitle());
        assertNull(b.getContent());
        assertNull(b.getStatus());
        assertNull(b.getCreatedAt());
        assertNull(b.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        UUID newId = UUID.randomUUID();
        esBlog.setId(newId);
        assertEquals(newId, esBlog.getId());

        UUID newAuthorId = UUID.randomUUID();
        esBlog.setAuthorId(newAuthorId);
        assertEquals(newAuthorId, esBlog.getAuthorId());

        esBlog.setAuthorFirstName("Alice");
        esBlog.setAuthorLastName("Writer");
        esBlog.setAuthorEmail("alice@example.com");
        esBlog.setTitle("New Title");
        esBlog.setContent("New Content");
        esBlog.setStatus("DRAFT");

        assertEquals("Alice", esBlog.getAuthorFirstName());
        assertEquals("Writer", esBlog.getAuthorLastName());
        assertEquals("alice@example.com", esBlog.getAuthorEmail());
        assertEquals("New Title", esBlog.getTitle());
        assertEquals("New Content", esBlog.getContent());
        assertEquals("DRAFT", esBlog.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        OffsetDateTime now = OffsetDateTime.now();
        EsBlog b1 = new EsBlog();
        b1.setId(id);
        b1.setAuthorId(authorId);
        b1.setTitle("T");
        b1.setContent("C");
        b1.setStatus("PUBLISHED");
        b1.setCreatedAt(now);
        b1.setUpdatedAt(now);

        EsBlog b2 = new EsBlog();
        b2.setId(id);
        b2.setAuthorId(authorId);
        b2.setTitle("T");
        b2.setContent("C");
        b2.setStatus("PUBLISHED");
        b2.setCreatedAt(now);
        b2.setUpdatedAt(now);

        EsBlog b3 = new EsBlog();
        b3.setId(UUID.randomUUID());
        b3.setAuthorId(authorId);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
        assertNotEquals(b1.hashCode(), b3.hashCode());
    }

    @Test
    void testToString() {
        String ts = esBlog.toString();
        assertNotNull(ts);
        assertTrue(ts.contains("Sample Title"));
        assertTrue(ts.contains("PUBLISHED"));
    }
} 