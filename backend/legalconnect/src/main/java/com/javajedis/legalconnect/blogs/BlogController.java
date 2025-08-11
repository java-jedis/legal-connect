package com.javajedis.legalconnect.blogs;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.blogs.dto.AuthorSubscribersListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogResponseDTO;
import com.javajedis.legalconnect.blogs.dto.UserSubscriptionsListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.WriteBlogDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.common.security.RequireVerifiedLawyer;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "C. Blogs", description = "Blog management endpoints")
@RestController
@RequestMapping("/blogs")
@RequireUserOrVerifiedLawyer
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final UserRepo userRepo;

    @Operation(summary = "Create blog", description = "Creates a new blog for the authenticated author")
    @PostMapping
    @RequireVerifiedLawyer
    public ResponseEntity<ApiResponse<BlogResponseDTO>> createBlog(@Valid @RequestBody WriteBlogDTO blogData) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            return ApiResponse.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        log.info("POST /blogs called for author: {}", currentUser.getId());
        return blogService.writeBlog(currentUser ,blogData);
    }

    @Operation(summary = "Update blog", description = "Updates an existing blog. Only the author can update.")
    @PutMapping("/{blogId}")
    @RequireVerifiedLawyer
    public ResponseEntity<ApiResponse<BlogResponseDTO>> updateBlog(
            @PathVariable UUID blogId,
            @Valid @RequestBody WriteBlogDTO blogData) {
        log.info("PUT /blogs/{} called", blogId);
        return blogService.updateBlog(blogId, blogData);
    }

    @Operation(summary = "Delete blog", description = "Deletes a blog. Only the author can delete.")
    @DeleteMapping("/{blogId}")
    @RequireVerifiedLawyer
    public ResponseEntity<ApiResponse<String>> deleteBlog(@PathVariable UUID blogId) {
        log.info("DELETE /blogs/{} called", blogId);
        return blogService.deleteBlog(blogId);
    }

    @Operation(summary = "Change blog status", description = "Changes a blog's status. Only the author can change status.")
    @PutMapping("/{blogId}/status")
    @RequireVerifiedLawyer
    public ResponseEntity<ApiResponse<BlogResponseDTO>> changeStatus(
            @PathVariable UUID blogId,
            @RequestParam BlogStatus status) {
        log.info("PUT /blogs/{}/status called with status={} ", blogId, status);
        return blogService.changeStatus(blogId, status);
    }

    @Operation(summary = "Get blog by ID", description = "Retrieves a blog by its ID")
    @GetMapping("/{blogId}")
    public ResponseEntity<ApiResponse<BlogResponseDTO>> getBlog(@PathVariable UUID blogId) {
        log.info("GET /blogs/{} called", blogId);
        return blogService.getBlog(blogId);
    }

    @Operation(summary = "Get author's blogs", description = "Retrieves all blogs by an author with pagination")
    @GetMapping("/authors/{authorId}")
    public ResponseEntity<ApiResponse<BlogListResponseDTO>> getAllAuthorBlogs(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /blogs/authors/{} called with page={}, size={}, sortDirection={}", authorId, page, size, sortDirection);
        return blogService.getAllAuthorBlogs(authorId, page, size, sortDirection);
    }

    @Operation(summary = "Subscribe to author", description = "Subscribes the current user to an author")
    @PostMapping("/authors/{authorId}/subscribe")
    public ResponseEntity<ApiResponse<String>> subscribe(@PathVariable UUID authorId) {
        log.info("POST /blogs/authors/{}/subscribe called", authorId);
        return blogService.subscribe(authorId);
    }

    @Operation(summary = "Unsubscribe from author", description = "Unsubscribes the current user from an author")
    @DeleteMapping("/authors/{authorId}/subscribe")
    public ResponseEntity<ApiResponse<String>> unsubscribe(@PathVariable UUID authorId) {
        log.info("DELETE /blogs/authors/{}/subscribe called", authorId);
        return blogService.unsubscribe(authorId);
    }

    @Operation(summary = "Get subscribers of an author", description = "Retrieves a paginated list of users subscribed to an author")
    @GetMapping("/authors/subscribers")
    @RequireVerifiedLawyer
    public ResponseEntity<ApiResponse<AuthorSubscribersListResponseDTO>> getSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /blogs/subscribers called with page={}, size={}", page, size);
        return blogService.getSubscribers(page, size);
    }

    @Operation(summary = "Get subscribed blogs", description = "Retrieves paginated published blogs from authors the current user subscribed to")
    @GetMapping("/subscribed")
    public ResponseEntity<ApiResponse<BlogListResponseDTO>> getSubscribedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /blogs/subscribed called with page={}, size={}, sortDirection={}", page, size, sortDirection);
        return blogService.getSubscribedBlogs(page, size, sortDirection);
    }

    @Operation(summary = "Get subscribed authors", description = "Retrieves a paginated list of authors that the current user subscribed to")
    @GetMapping("/authors/subscribed")
    @RequireVerifiedLawyer
    public ResponseEntity<ApiResponse<UserSubscriptionsListResponseDTO>> getSubscribedAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /blogs/authors/subscribed called with page={}, size={}", page, size);
        return blogService.getSubscribedAuthors(page, size);
    }
} 