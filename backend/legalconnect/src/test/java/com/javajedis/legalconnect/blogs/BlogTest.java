package com.javajedis.legalconnect.blogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class BlogTest {

    private Blog blog;
    private User author;
    private UUID blogId;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        blogId = UUID.randomUUID();
        authorId = UUID.randomUUID();

        author = new User();
        author.setId(authorId);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john.doe@example.com");
        author.setRole(Role.USER);
        author.setPassword("secret");
        author.setEmailVerified(true);

        blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(author);
        blog.setTitle("Sample Title");
        blog.setContent("Sample Content");
        blog.setStatus(BlogStatus.DRAFT);
        blog.setCreatedAt(OffsetDateTime.now().minusDays(1));
        blog.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testDefaultConstructor() {
        Blog defaultBlog = new Blog();
        assertNotNull(defaultBlog);
        assertNull(defaultBlog.getId());
        assertNull(defaultBlog.getAuthor());
        assertNull(defaultBlog.getTitle());
        assertNull(defaultBlog.getContent());
        assertEquals(BlogStatus.DRAFT, defaultBlog.getStatus());
        assertNull(defaultBlog.getCreatedAt());
        assertNull(defaultBlog.getUpdatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        blog.setId(newId);
        assertEquals(newId, blog.getId());
    }

    @Test
    void testAuthorGetterAndSetter() {
        User newAuthor = new User();
        newAuthor.setId(UUID.randomUUID());
        newAuthor.setFirstName("Alice");
        newAuthor.setLastName("Writer");
        newAuthor.setEmail("alice@example.com");
        newAuthor.setRole(Role.USER);
        newAuthor.setPassword("pw");
        blog.setAuthor(newAuthor);
        assertEquals(newAuthor, blog.getAuthor());
    }

    @Test
    void testTitleAndContentGettersAndSetters() {
        blog.setTitle("New Title");
        blog.setContent("New Content");
        assertEquals("New Title", blog.getTitle());
        assertEquals("New Content", blog.getContent());
    }

    @Test
    void testStatusGetterAndSetter() {
        blog.setStatus(BlogStatus.PUBLISHED);
        assertEquals(BlogStatus.PUBLISHED, blog.getStatus());
    }

    @Test
    void testTimestampsGettersAndSetters() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(2);
        OffsetDateTime updated = OffsetDateTime.now();
        blog.setCreatedAt(created);
        blog.setUpdatedAt(updated);
        assertEquals(created, blog.getCreatedAt());
        assertEquals(updated, blog.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        OffsetDateTime now = OffsetDateTime.now();
        Blog b1 = new Blog(blogId, author, "T", "C", BlogStatus.PUBLISHED, now, now);
        Blog b2 = new Blog(blogId, author, "T", "C", BlogStatus.PUBLISHED, now, now);
        Blog b3 = new Blog(UUID.randomUUID(), author, "T2", "C2", BlogStatus.DRAFT, now, now);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
        assertNotEquals(b1.hashCode(), b3.hashCode());
    }

    @Test
    void testToString() {
        String ts = blog.toString();
        assertNotNull(ts);
        assertTrue(ts.contains("Sample Title"));
        assertTrue(ts.contains("Sample Content"));
        assertTrue(ts.contains("DRAFT"));
    }
} 