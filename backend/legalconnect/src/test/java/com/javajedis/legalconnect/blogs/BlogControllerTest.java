package com.javajedis.legalconnect.blogs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.blogs.dto.AuthorSubscribersListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogSearchListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogSearchResponseDTO;
import com.javajedis.legalconnect.blogs.dto.WriteBlogDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.common.utility.JWTFilter;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@WebMvcTest(controllers = BlogController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {EmailVerificationFilter.class, JWTFilter.class}))
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(BlogControllerTest.TestConfig.class)
@DisplayName("BlogController Tests")
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlogService blogService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User author;
    private WriteBlogDTO writeBlogDTO;
    private BlogResponseDTO blogResponseDTO;
    private BlogListResponseDTO blogListResponseDTO;
    private AuthorSubscribersListResponseDTO subscribersListResponseDTO;
    private BlogSearchListResponseDTO searchListResponseDTO;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BlogService blogService() {
            return mock(BlogService.class);
        }

        @Bean
        public UserRepo userRepo() {
            return mock(UserRepo.class);
        }
    }

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(UUID.randomUUID());
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setEmail("john@example.com");
        author.setRole(Role.LAWYER);
        author.setEmailVerified(true);
        author.setCreatedAt(OffsetDateTime.now());
        author.setUpdatedAt(OffsetDateTime.now());

        writeBlogDTO = new WriteBlogDTO("Title", "Content", BlogStatus.DRAFT);

        BlogResponseDTO.AuthorSummaryDTO authorSummary = new BlogResponseDTO.AuthorSummaryDTO(
            author.getId(), author.getFirstName(), author.getLastName(), author.getEmail()
        );
        blogResponseDTO = new BlogResponseDTO(
            UUID.randomUUID(),
            authorSummary,
            "Title",
            "Content",
            BlogStatus.DRAFT,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        blogListResponseDTO = new BlogListResponseDTO(List.of(blogResponseDTO));
        subscribersListResponseDTO = new AuthorSubscribersListResponseDTO(List.of());
        searchListResponseDTO = new BlogSearchListResponseDTO(Arrays.asList(
            new BlogSearchResponseDTO(blogResponseDTO, "<em>Title</em>", "<em>Content</em>", true)
        ));
    }

    @Test
    @DisplayName("Create blog - success")
    void createBlog_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(author);

            ResponseEntity<ApiResponse<BlogResponseDTO>> response =
                ApiResponse.success(blogResponseDTO, HttpStatus.CREATED, "Blog created");
            when(blogService.writeBlog(eq(author), any(WriteBlogDTO.class))).thenReturn(response);

            mockMvc.perform(post("/blogs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(writeBlogDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("Title"));
        }
    }

    @Test
    @DisplayName("Create blog - unauthorized when no user")
    void createBlog_Unauthorized() throws Exception {
        try (MockedStatic<GetUserUtil> mocked = mockStatic(GetUserUtil.class)) {
            mocked.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(post("/blogs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(writeBlogDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Update blog - success")
    void updateBlog_Success() throws Exception {
        UUID blogId = UUID.randomUUID();
        ResponseEntity<ApiResponse<BlogResponseDTO>> response =
            ApiResponse.success(blogResponseDTO, HttpStatus.OK, "Blog updated");
        when(blogService.updateBlog(eq(blogId), any(WriteBlogDTO.class))).thenReturn(response);

        mockMvc.perform(put("/blogs/{blogId}", blogId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(writeBlogDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    @DisplayName("Delete blog - success")
    void deleteBlog_Success() throws Exception {
        UUID blogId = UUID.randomUUID();
        ResponseEntity<ApiResponse<String>> response =
            ApiResponse.success("Deleted", HttpStatus.OK, "Blog deleted");
        when(blogService.deleteBlog(blogId)).thenReturn(response);

        mockMvc.perform(delete("/blogs/{blogId}", blogId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Deleted"));
    }

    @Test
    @DisplayName("Change status - success")
    void changeStatus_Success() throws Exception {
        UUID blogId = UUID.randomUUID();
        ResponseEntity<ApiResponse<BlogResponseDTO>> response =
            ApiResponse.success(blogResponseDTO, HttpStatus.OK, "Status changed");
        when(blogService.changeStatus(blogId, BlogStatus.PUBLISHED)).thenReturn(response);

        mockMvc.perform(put("/blogs/{blogId}/status", blogId)
                        .param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    @DisplayName("Get blog by ID - success")
    void getBlog_Success() throws Exception {
        UUID blogId = UUID.randomUUID();
        ResponseEntity<ApiResponse<BlogResponseDTO>> response =
            ApiResponse.success(blogResponseDTO, HttpStatus.OK, "Fetched");
        when(blogService.getBlog(blogId)).thenReturn(response);

        mockMvc.perform(get("/blogs/{blogId}", blogId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    @DisplayName("Get author's blogs - success with params")
    void getAllAuthorBlogs_Success() throws Exception {
        UUID authorId = UUID.randomUUID();
        ResponseEntity<ApiResponse<BlogListResponseDTO>> response =
            ApiResponse.success(blogListResponseDTO, HttpStatus.OK, "Fetched");
        when(blogService.getAllAuthorBlogs(authorId, 0, 10, "DESC")).thenReturn(response);

        mockMvc.perform(get("/blogs/authors/{authorId}", authorId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blogs").isArray());
    }

    @Test
    @DisplayName("Subscribe to author - success")
    void subscribe_Success() throws Exception {
        UUID authorId = UUID.randomUUID();
        ResponseEntity<ApiResponse<String>> response =
            ApiResponse.success("Subscribed", HttpStatus.OK, "Subscribed");
        when(blogService.subscribe(authorId)).thenReturn(response);

        mockMvc.perform(post("/blogs/authors/{authorId}/subscribe", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Subscribed"));
    }

    @Test
    @DisplayName("Unsubscribe from author - success")
    void unsubscribe_Success() throws Exception {
        UUID authorId = UUID.randomUUID();
        ResponseEntity<ApiResponse<String>> response =
            ApiResponse.success("Unsubscribed", HttpStatus.OK, "Unsubscribed");
        when(blogService.unsubscribe(authorId)).thenReturn(response);

        mockMvc.perform(delete("/blogs/authors/{authorId}/subscribe", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Unsubscribed"));
    }

    @Test
    @DisplayName("Get subscribers - success with defaults")
    void getSubscribers_Success_Defaults() throws Exception {
        ResponseEntity<ApiResponse<AuthorSubscribersListResponseDTO>> response =
            ApiResponse.success(subscribersListResponseDTO, HttpStatus.OK, "Fetched");
        when(blogService.getSubscribers(0, 10)).thenReturn(response);

        mockMvc.perform(get("/blogs/authors/subscribers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subscribers").isArray());
    }

    @Test
    @DisplayName("Get subscribed blogs - success with defaults")
    void getSubscribedBlogs_Success_Defaults() throws Exception {
        ResponseEntity<ApiResponse<BlogListResponseDTO>> response =
            ApiResponse.success(blogListResponseDTO, HttpStatus.OK, "Fetched");
        when(blogService.getSubscribedBlogs(0, 10, "DESC")).thenReturn(response);

        mockMvc.perform(get("/blogs/subscribed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blogs").isArray());
    }

    @Test
    @DisplayName("Search published blogs - success")
    void searchPublished_Success() throws Exception {
        ResponseEntity<ApiResponse<BlogSearchListResponseDTO>> response =
            ApiResponse.success(searchListResponseDTO, HttpStatus.OK, "Fetched");
        when(blogService.searchPublished("test", 0, 10)).thenReturn(response);

        mockMvc.perform(get("/blogs/search")
                        .param("q", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results").isArray());
    }
} 