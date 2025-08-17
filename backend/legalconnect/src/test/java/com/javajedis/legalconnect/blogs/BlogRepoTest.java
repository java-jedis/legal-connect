package com.javajedis.legalconnect.blogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class BlogRepoTest {

    @Mock
    private BlogRepo blogRepo;

    private User author1;
    private User author2;

    private Blog blog1;
    private Blog blog2;
    private Blog blog3;

    private UUID authorId1;
    private UUID authorId2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authorId1 = UUID.randomUUID();
        authorId2 = UUID.randomUUID();

        author1 = new User();
        author1.setId(authorId1);
        author1.setFirstName("John");
        author1.setLastName("Writer");
        author1.setEmail("john@example.com");
        author1.setRole(Role.USER);
        author1.setPassword("pw");
        author1.setEmailVerified(true);

        author2 = new User();
        author2.setId(authorId2);
        author2.setFirstName("Jane");
        author2.setLastName("Author");
        author2.setEmail("jane@example.com");
        author2.setRole(Role.USER);
        author2.setPassword("pw");
        author2.setEmailVerified(true);

        OffsetDateTime now = OffsetDateTime.now();

        blog1 = new Blog(UUID.randomUUID(), author1, "Title 1", "Content 1", BlogStatus.PUBLISHED, now.minusDays(3), now.minusDays(1));
        blog2 = new Blog(UUID.randomUUID(), author1, "Title 2", "Content 2", BlogStatus.DRAFT, now.minusDays(2), now);
        blog3 = new Blog(UUID.randomUUID(), author2, "Title 3", "Content 3", BlogStatus.PUBLISHED, now.minusDays(1), now);
    }

    @Test
    void testFindByAuthorId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());
        List<Blog> blogs = Arrays.asList(blog1, blog2);
        Page<Blog> blogPage = new PageImpl<>(blogs, pageable, blogs.size());

        when(blogRepo.findByAuthorId(authorId1, pageable)).thenReturn(blogPage);

        Page<Blog> found = blogRepo.findByAuthorId(authorId1, pageable);
        assertNotNull(found);
        assertEquals(2, found.getContent().size());
        assertEquals("Title 1", found.getContent().get(0).getTitle());
        assertEquals("Title 2", found.getContent().get(1).getTitle());
    }

    @Test
    void testFindByAuthorIdAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Blog> published = Arrays.asList(blog1);
        Page<Blog> blogPage = new PageImpl<>(published, pageable, published.size());

        when(blogRepo.findByAuthorIdAndStatus(authorId1, BlogStatus.PUBLISHED, pageable)).thenReturn(blogPage);

        Page<Blog> found = blogRepo.findByAuthorIdAndStatus(authorId1, BlogStatus.PUBLISHED, pageable);
        assertNotNull(found);
        assertEquals(1, found.getContent().size());
        assertEquals(BlogStatus.PUBLISHED, found.getContent().get(0).getStatus());
    }

    @Test
    void testFindByAuthorIdInAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        List<UUID> authorIds = Arrays.asList(authorId1, authorId2);
        List<Blog> published = Arrays.asList(blog1, blog3);
        Page<Blog> blogPage = new PageImpl<>(published, pageable, published.size());

        when(blogRepo.findByAuthorIdInAndStatus(authorIds, BlogStatus.PUBLISHED, pageable)).thenReturn(blogPage);

        Page<Blog> found = blogRepo.findByAuthorIdInAndStatus(authorIds, BlogStatus.PUBLISHED, pageable);
        assertNotNull(found);
        assertEquals(2, found.getContent().size());
        assertTrue(found.getContent().stream().allMatch(b -> b.getStatus() == BlogStatus.PUBLISHED));
    }
} 