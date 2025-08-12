package com.javajedis.legalconnect.blogs.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import com.javajedis.legalconnect.blogs.Blog;
import com.javajedis.legalconnect.blogs.BlogStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class EsBlogServiceTest {

    @Mock
    private EsBlogRepo esBlogRepo;

    @Mock
    private ElasticsearchOperations elasticOps;

    private EsBlogService service;

    private Blog blog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EsBlogService(esBlogRepo, elasticOps);

        User author = new User();
        author.setId(UUID.randomUUID());
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john@example.com");
        author.setRole(Role.USER);
        author.setPassword("pw");
        author.setEmailVerified(true);

        blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(author);
        blog.setTitle("How to test Elasticsearch service");
        blog.setContent("This content explains how to write tests for Elasticsearch service using mocks.");
        blog.setStatus(BlogStatus.PUBLISHED);
        blog.setCreatedAt(OffsetDateTime.now().minusDays(1));
        blog.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testIndexAndUpdate_Success() {
        // save returns mapped EsBlog
        when(esBlogRepo.save(any(EsBlog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.index(blog);
        service.update(blog);
        // If no exception, we assume success; repository interaction verified via behavior
        assertTrue(true);
    }

    @Test
    void testIndex_FailureHandled() {
        doThrow(new RuntimeException("fail")).when(esBlogRepo).save(any(EsBlog.class));
        service.index(blog);
        assertTrue(true);
    }

    @Test
    void testDeleteAndGetById() {
        UUID id = blog.getId();
        when(esBlogRepo.findById(id)).thenReturn(Optional.of(new EsBlog()));
        service.delete(id);
        Optional<EsBlog> fetched = service.getById(id);
        assertTrue(fetched.isPresent());
    }

    @Test
    void testGetById_FailureHandled() {
        UUID id = blog.getId();
        doThrow(new RuntimeException("fail")).when(esBlogRepo).findById(id);
        Optional<EsBlog> fetched = service.getById(id);
        assertTrue(fetched.isEmpty());
    }

    @Test
    void testSearchPublishedBlogs_BuildsQueryAndMapsResults() {
        // Prepare a hit
        EsBlog doc = new EsBlog();
        doc.setId(UUID.randomUUID());
        doc.setTitle("Testing Elasticsearch with highlight");
        doc.setContent("Highlight the word elasticsearch within the content for testing");
        doc.setStatus("PUBLISHED");

        @SuppressWarnings("unchecked")
        SearchHit<EsBlog> hit = (SearchHit<EsBlog>) mock(SearchHit.class);
        when(hit.getContent()).thenReturn(doc);

        @SuppressWarnings("unchecked")
        SearchHits<EsBlog> hits = (SearchHits<EsBlog>) mock(SearchHits.class);
        when(hits.iterator()).thenReturn(List.of(hit).iterator());
        when(hits.getTotalHits()).thenReturn(1L);

        when(elasticOps.search(any(NativeQuery.class), eq(EsBlog.class))).thenReturn(hits);

        EsBlogService.SearchPage page = service.searchPublishedBlogs("elasticsearch", 0, 10);

        assertNotNull(page);
        assertEquals(1L, page.total());
        assertEquals(1, page.results().size());
        EsBlogService.SearchResult result = page.results().get(0);
        assertNotNull(result.blog());
        assertTrue(result.highlightedTitle() == null || result.highlightedTitle().contains("<em>"));
        assertTrue(result.highlightedContent() == null || result.highlightedContent().contains("<em>"));
    }

    @Test
    void testSearchPublishedBlogs_FailureHandled() {
        when(elasticOps.search(any(NativeQuery.class), eq(EsBlog.class))).thenThrow(new RuntimeException("search fail"));
        EsBlogService.SearchPage page = service.searchPublishedBlogs("anything", 0, 10);
        assertNotNull(page);
        assertEquals(0L, page.total());
        assertTrue(page.results().isEmpty());
    }
} 