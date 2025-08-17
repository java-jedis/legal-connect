package com.javajedis.legalconnect.blogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.blogs.dto.AuthorSubscribersListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogSearchListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogSearchResponseDTO;
import com.javajedis.legalconnect.blogs.dto.UserSubscriptionsListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.WriteBlogDTO;
import com.javajedis.legalconnect.blogs.search.EsBlog;
import com.javajedis.legalconnect.blogs.search.EsBlogService;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

class BlogServiceTest {

    @Mock private BlogRepo blogRepo;
    @Mock private UserRepo userRepo;
    @Mock private SubscriberRepo subscriberRepo;
    @Mock private EsBlogService esBlogService;

    @InjectMocks private BlogService blogService;

    private User author;
    private User otherUser;
    private Blog blog;
    private UUID blogId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        author = new User();
        author.setId(UUID.randomUUID());
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john@example.com");
        author.setRole(Role.LAWYER);
        author.setEmailVerified(true);
        author.setCreatedAt(OffsetDateTime.now());
        author.setUpdatedAt(OffsetDateTime.now());

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setFirstName("Jane");
        otherUser.setLastName("Smith");
        otherUser.setEmail("jane@example.com");
        otherUser.setRole(Role.USER);
        otherUser.setEmailVerified(true);

        blogId = UUID.randomUUID();
        blog = new Blog(blogId, author, "Title", "Content", BlogStatus.DRAFT,
                OffsetDateTime.now().minusDays(1), OffsetDateTime.now());
    }

    @Test
    @DisplayName("writeBlog - success")
    void writeBlog_success() {
        WriteBlogDTO dto = new WriteBlogDTO("Title", "Content", BlogStatus.PUBLISHED);
        when(userRepo.findById(author.getId())).thenReturn(Optional.of(author));
        when(blogRepo.save(any(Blog.class))).thenAnswer(inv -> {
            Blog b = inv.getArgument(0);
            b.setId(blogId);
            return b;
        });

        ResponseEntity<ApiResponse<BlogResponseDTO>> res = blogService.writeBlog(author, dto);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals("Title", res.getBody().getData().getTitle());
        verify(esBlogService).index(any(Blog.class));
    }

    @Test
    @DisplayName("writeBlog - author not found")
    void writeBlog_authorNotFound() {
        when(userRepo.findById(author.getId())).thenReturn(Optional.empty());
        ResponseEntity<ApiResponse<BlogResponseDTO>> res = blogService.writeBlog(author, new WriteBlogDTO());
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        verify(blogRepo, never()).save(any());
    }

    @Test
    @DisplayName("updateBlog - success")
    void updateBlog_success() {
        WriteBlogDTO dto = new WriteBlogDTO("New", "NewC", BlogStatus.PUBLISHED);
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog)); // for validate
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog)); // subsequent call; Mockito keeps last but we'll stub again below
        when(blogRepo.save(any(Blog.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog));
            ResponseEntity<ApiResponse<BlogResponseDTO>> res = blogService.updateBlog(blogId, dto);
            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals("New", res.getBody().getData().getTitle());
            verify(esBlogService).update(any(Blog.class));
        }
    }

    @Test
    @DisplayName("updateBlog - blog not found after validate")
    void updateBlog_notFound() {
        WriteBlogDTO dto = new WriteBlogDTO("New", "NewC", BlogStatus.PUBLISHED);
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog)); // validate sees it
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            when(blogRepo.findById(blogId)).thenReturn(Optional.empty());
            ResponseEntity<ApiResponse<BlogResponseDTO>> res = blogService.updateBlog(blogId, dto);
            assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("deleteBlog - success")
    void deleteBlog_success() {
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog)); // validate
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog));
            ResponseEntity<ApiResponse<String>> res = blogService.deleteBlog(blogId);
            assertEquals(HttpStatus.OK, res.getStatusCode());
            verify(esBlogService).delete(blogId);
        }
    }

    @Test
    @DisplayName("deleteBlog - not found")
    void deleteBlog_notFound() {
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog)); // validate
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            when(blogRepo.findById(blogId)).thenReturn(Optional.empty());
            ResponseEntity<ApiResponse<String>> res = blogService.deleteBlog(blogId);
            assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("changeStatus - success")
    void changeStatus_success() {
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog)); // validate
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog));
            when(blogRepo.save(any(Blog.class))).thenAnswer(inv -> inv.getArgument(0));
            ResponseEntity<ApiResponse<BlogResponseDTO>> res = blogService.changeStatus(blogId, BlogStatus.PUBLISHED);
            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals(BlogStatus.PUBLISHED, res.getBody().getData().getStatus());
        }
    }

    @Test
    @DisplayName("getBlog - success and not found")
    void getBlog_cases() {
        when(blogRepo.findById(blogId)).thenReturn(Optional.of(blog));
        ResponseEntity<ApiResponse<BlogResponseDTO>> ok = blogService.getBlog(blogId);
        assertEquals(HttpStatus.OK, ok.getStatusCode());

        when(blogRepo.findById(blogId)).thenReturn(Optional.empty());
        ResponseEntity<ApiResponse<BlogResponseDTO>> nf = blogService.getBlog(blogId);
        assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());
    }

    @Test
    @DisplayName("getAllAuthorBlogs - current user is author")
    void getAllAuthorBlogs_currentUserIsAuthor() {
        Page<Blog> page = new PageImpl<>(List.of(blog), PageRequest.of(0, 10), 1);
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            when(blogRepo.findByAuthorId(eq(author.getId()), any(Pageable.class))).thenReturn(page);
            ResponseEntity<ApiResponse<BlogListResponseDTO>> res = blogService.getAllAuthorBlogs(author.getId(), 0, 10, "DESC");
            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals(1, res.getBody().getData().getBlogs().size());
        }
    }

    @Test
    @DisplayName("getAllAuthorBlogs - viewer is not author, only published")
    void getAllAuthorBlogs_viewerNotAuthor() {
        Page<Blog> page = new PageImpl<>(List.of(blog), PageRequest.of(0, 10), 1);
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            when(blogRepo.findByAuthorIdAndStatus(eq(author.getId()), eq(BlogStatus.PUBLISHED), any(Pageable.class))).thenReturn(page);
            // New: subscription check invoked by mapper when viewer isn't the author
            when(subscriberRepo.findByAuthorIdAndSubscriberId(author.getId(), otherUser.getId())).thenReturn(Optional.empty());
            ResponseEntity<ApiResponse<BlogListResponseDTO>> res = blogService.getAllAuthorBlogs(author.getId(), 0, 10, "DESC");
            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertEquals(1, res.getBody().getData().getBlogs().size());
        }
    }

    @Test
    @DisplayName("subscribe - unauthorized, self, author not found, already, success")
    void subscribe_cases() {
        UUID authorId = author.getId();
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            // Unauthorized
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);
            ResponseEntity<ApiResponse<String>> unauth = blogService.subscribe(authorId);
            assertEquals(HttpStatus.UNAUTHORIZED, unauth.getStatusCode());

            // Self subscribe
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            ResponseEntity<ApiResponse<String>> self = blogService.subscribe(authorId);
            assertEquals(HttpStatus.BAD_REQUEST, self.getStatusCode());

            // Author not found
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            when(userRepo.findById(authorId)).thenReturn(Optional.empty());
            ResponseEntity<ApiResponse<String>> nf = blogService.subscribe(authorId);
            assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());

            // Already subscribed
            when(userRepo.findById(authorId)).thenReturn(Optional.of(author));
            when(subscriberRepo.findByAuthorIdAndSubscriberId(authorId, otherUser.getId()))
                    .thenReturn(Optional.of(new Subscriber()));
            ResponseEntity<ApiResponse<String>> conflict = blogService.subscribe(authorId);
            assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());

            // Success
            when(subscriberRepo.findByAuthorIdAndSubscriberId(authorId, otherUser.getId()))
                    .thenReturn(Optional.empty());
            when(subscriberRepo.save(any(Subscriber.class))).thenAnswer(inv -> inv.getArgument(0));
            ResponseEntity<ApiResponse<String>> ok = blogService.subscribe(authorId);
            assertEquals(HttpStatus.OK, ok.getStatusCode());
        }
    }

    @Test
    @DisplayName("unsubscribe - unauthorized, not found, success")
    void unsubscribe_cases() {
        UUID authorId = author.getId();
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            // Unauthorized
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);
            ResponseEntity<ApiResponse<String>> unauth = blogService.unsubscribe(authorId);
            assertEquals(HttpStatus.UNAUTHORIZED, unauth.getStatusCode());

            // Not found
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            when(subscriberRepo.findByAuthorIdAndSubscriberId(authorId, otherUser.getId())).thenReturn(Optional.empty());
            ResponseEntity<ApiResponse<String>> nf = blogService.unsubscribe(authorId);
            assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());

            // Success
            when(subscriberRepo.findByAuthorIdAndSubscriberId(authorId, otherUser.getId())).thenReturn(Optional.of(new Subscriber()));
            ResponseEntity<ApiResponse<String>> ok = blogService.unsubscribe(authorId);
            assertEquals(HttpStatus.OK, ok.getStatusCode());
        }
    }

    @Test
    @DisplayName("getSubscribers - unauthorized and success")
    void getSubscribers_cases() {
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);
            ResponseEntity<ApiResponse<AuthorSubscribersListResponseDTO>> unauth = blogService.getSubscribers(0, 10);
            assertEquals(HttpStatus.UNAUTHORIZED, unauth.getStatusCode());

            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);
            Subscriber sub = new Subscriber(UUID.randomUUID(), author, otherUser, OffsetDateTime.now(), OffsetDateTime.now());
            Page<Subscriber> page = new PageImpl<>(List.of(sub), PageRequest.of(0, 10), 1);
            when(subscriberRepo.findByAuthorId(eq(author.getId()), any(Pageable.class))).thenReturn(page);
            ResponseEntity<ApiResponse<AuthorSubscribersListResponseDTO>> ok = blogService.getSubscribers(0, 10);
            assertEquals(HttpStatus.OK, ok.getStatusCode());
            assertEquals(1, ok.getBody().getData().getSubscribers().size());
        }
    }

    @Test
    @DisplayName("getSubscribedBlogs - unauthorized, empty, and non-empty")
    void getSubscribedBlogs_cases() {
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);
            ResponseEntity<ApiResponse<BlogListResponseDTO>> unauth = blogService.getSubscribedBlogs(0, 10, "DESC");
            assertEquals(HttpStatus.UNAUTHORIZED, unauth.getStatusCode());

            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            // Empty subscriptions
            Page<Subscriber> emptySubs = Page.empty(PageRequest.of(0, 10));
            when(subscriberRepo.findBySubscriberId(eq(otherUser.getId()), any(Pageable.class))).thenReturn(emptySubs);
            ResponseEntity<ApiResponse<BlogListResponseDTO>> empty = blogService.getSubscribedBlogs(0, 10, "DESC");
            assertEquals(HttpStatus.OK, empty.getStatusCode());
            assertTrue(empty.getBody().getData().getBlogs().isEmpty());

            // Non-empty subscriptions
            Subscriber sub = new Subscriber(UUID.randomUUID(), author, otherUser, OffsetDateTime.now(), OffsetDateTime.now());
            Page<Subscriber> subs = new PageImpl<>(List.of(sub), PageRequest.of(0, 10), 1);
            when(subscriberRepo.findBySubscriberId(eq(otherUser.getId()), any(Pageable.class))).thenReturn(subs);
            Page<Blog> blogPage = new PageImpl<>(List.of(blog), PageRequest.of(0, 10), 1);
            when(blogRepo.findByAuthorIdInAndStatus(anyList(), eq(BlogStatus.PUBLISHED), any(Pageable.class))).thenReturn(blogPage);
            // New: mapper will check subscription for each blog; return present
            when(subscriberRepo.findByAuthorIdAndSubscriberId(author.getId(), otherUser.getId())).thenReturn(Optional.of(new Subscriber()));
            ResponseEntity<ApiResponse<BlogListResponseDTO>> ok = blogService.getSubscribedBlogs(0, 10, "DESC");
            assertEquals(HttpStatus.OK, ok.getStatusCode());
            assertEquals(1, ok.getBody().getData().getBlogs().size());
            assertTrue(ok.getBody().getData().getBlogs().get(0).isSubscribed());
        }
    }

    @Test
    @DisplayName("getSubscribedAuthors - unauthorized and success")
    void getSubscribedAuthors_cases() {
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);
            ResponseEntity<ApiResponse<UserSubscriptionsListResponseDTO>> unauth = blogService.getSubscribedAuthors(0, 10);
            assertEquals(HttpStatus.UNAUTHORIZED, unauth.getStatusCode());

            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            Subscriber sub = new Subscriber(UUID.randomUUID(), author, otherUser, OffsetDateTime.now(), OffsetDateTime.now());
            Page<Subscriber> subs = new PageImpl<>(List.of(sub), PageRequest.of(0, 10), 1);
            when(subscriberRepo.findBySubscriberId(eq(otherUser.getId()), any(Pageable.class))).thenReturn(subs);
            ResponseEntity<ApiResponse<UserSubscriptionsListResponseDTO>> ok = blogService.getSubscribedAuthors(0, 10);
            assertEquals(HttpStatus.OK, ok.getStatusCode());
            assertEquals(1, ok.getBody().getData().getAuthors().size());
        }
    }

    @Test
    @DisplayName("searchPublished - maps results and subscribed flag")
    void searchPublished_success() {
        EsBlog doc = new EsBlog();
        doc.setId(UUID.randomUUID());
        doc.setAuthorId(author.getId());
        doc.setAuthorFirstName(author.getFirstName());
        doc.setAuthorLastName(author.getLastName());
        doc.setAuthorEmail(author.getEmail());
        doc.setTitle("T");
        doc.setContent("C");
        doc.setStatus(BlogStatus.PUBLISHED.name());
        doc.setCreatedAt(OffsetDateTime.now());
        doc.setUpdatedAt(OffsetDateTime.now());

        EsBlogService.SearchResult sr = new EsBlogService.SearchResult(doc, "<em>T</em>", "<em>C</em>");
        EsBlogService.SearchPage page = new EsBlogService.SearchPage(List.of(sr), 1L);
        when(esBlogService.searchPublishedBlogs(anyString(), eq(0), eq(10))).thenReturn(page);

        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(otherUser);
            when(subscriberRepo.findByAuthorIdAndSubscriberId(author.getId(), otherUser.getId()))
                    .thenReturn(Optional.of(new Subscriber()));
            ResponseEntity<ApiResponse<BlogSearchListResponseDTO>> res = blogService.searchPublished("t", 0, 10);
            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            List<BlogSearchResponseDTO> results = res.getBody().getData().getResults();
            assertEquals(1, results.size());
            assertEquals("T", results.get(0).getBlog().getTitle());
            assertTrue(results.get(0).isSubscribed());
        }
    }
} 