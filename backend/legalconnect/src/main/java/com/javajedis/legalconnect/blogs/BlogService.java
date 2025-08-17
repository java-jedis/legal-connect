package com.javajedis.legalconnect.blogs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javajedis.legalconnect.blogs.dto.AuthorSubscribersListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogSearchListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.BlogSearchResponseDTO;
import com.javajedis.legalconnect.blogs.dto.UserSubscriptionsListResponseDTO;
import com.javajedis.legalconnect.blogs.dto.WriteBlogDTO;
import com.javajedis.legalconnect.blogs.search.EsBlogService;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserInfoResponseDTO;
import com.javajedis.legalconnect.user.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private static final String UPDATED_AT_FIELD = "updatedAt";
    private static final String LOG_BLOG_NOT_FOUND_WITH_ID = "Blog not found with ID: {}";
    private static final String ERR_BLOG_NOT_FOUND = "Blog not found";
    private static final String META_TOTAL_COUNT = "totalCount";
    private static final String META_PAGE_NUMBER = "pageNumber";
    private static final String META_PAGE_SIZE = "pageSize";
    private static final String META_TOTAL_PAGES = "totalPages";
    private static final String META_HAS_NEXT = "hasNext";
    private static final String META_HAS_PREVIOUS = "hasPrevious";
    private static final String ERR_UNAUTHORIZED = "Unauthorized";

    private final BlogRepo blogRepo;
    private final UserRepo userRepo;
    private final SubscriberRepo subscriberRepo;
    private final EsBlogService esBlogService;

    public ResponseEntity<ApiResponse<BlogResponseDTO>> writeBlog( User currentUser,WriteBlogDTO blogData) {
        log.debug("Creating blog for author ID: {}", currentUser.getId());

        User author = userRepo.findById(currentUser.getId()).orElse(null);
        if (author == null) {
            log.warn("Author not found: {}", currentUser.getId());
            return ApiResponse.error("Author not found", HttpStatus.NOT_FOUND);
        }

        Blog blog = new Blog();
        blog.setAuthor(author);
        blog.setTitle(blogData.getTitle());
        blog.setContent(blogData.getContent());
        blog.setStatus(blogData.getStatus());

        Blog saved = blogRepo.save(blog);
        esBlogService.index(saved);
        log.info("Blog {} created by user: {}", saved.getId(), currentUser.getEmail());

        return ApiResponse.success(mapToBlogResponseDTO(saved), HttpStatus.CREATED, "Blog created successfully");
    }

    public ResponseEntity<ApiResponse<BlogResponseDTO>> updateBlog(UUID blogId, WriteBlogDTO blogData) {
        log.debug("Updating blog with ID: {}", blogId);

        ResponseEntity<ApiResponse<BlogResponseDTO>> validationResult = validateBlogAccessForAuthor(blogId, "update");
        if (validationResult != null) {
            return validationResult;
        }

        Blog existing = blogRepo.findById(blogId).orElse(null);
        if (existing == null) {
            log.warn(LOG_BLOG_NOT_FOUND_WITH_ID, blogId);
            return ApiResponse.error(ERR_BLOG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existing.setTitle(blogData.getTitle());
        existing.setContent(blogData.getContent());
        if (blogData.getStatus() != null) {
            existing.setStatus(blogData.getStatus());
        }

        Blog updated = blogRepo.save(existing);
        esBlogService.update(updated);
        log.info("Blog {} updated", blogId);

        return ApiResponse.success(mapToBlogResponseDTO(updated), HttpStatus.OK, "Blog updated successfully");
    }

    public ResponseEntity<ApiResponse<String>> deleteBlog(UUID blogId) {
        log.debug("Deleting blog with ID: {}", blogId);

        ResponseEntity<ApiResponse<BlogResponseDTO>> validationResult = validateBlogAccessForAuthor(blogId, "delete");
        if (validationResult != null) {
            ApiResponse<BlogResponseDTO> body = validationResult.getBody();
            if (body != null && body.getError() != null) {
                int code = body.getError().getCode();
                String message = body.getError().getMessage();
                return ApiResponse.error(message != null ? message : "Operation not allowed", HttpStatus.valueOf(code));
            }
            return ApiResponse.error("Operation not allowed", HttpStatus.FORBIDDEN);
        }

        Blog existing = blogRepo.findById(blogId).orElse(null);
        if (existing == null) {
            log.warn(LOG_BLOG_NOT_FOUND_WITH_ID, blogId);
            return ApiResponse.error(ERR_BLOG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        blogRepo.delete(existing);
        esBlogService.delete(blogId);
        log.info("Blog {} deleted", blogId);

        return ApiResponse.success("Blog deleted successfully", HttpStatus.OK, "Blog deleted successfully");
    }

    public ResponseEntity<ApiResponse<BlogResponseDTO>> changeStatus(UUID blogId, BlogStatus status) {
        log.debug("Changing status for blog ID: {} to {}", blogId, status);

        ResponseEntity<ApiResponse<BlogResponseDTO>> validationResult = validateBlogAccessForAuthor(blogId, "change status of");
        if (validationResult != null) {
            return validationResult;
        }

        Blog existing = blogRepo.findById(blogId).orElse(null);
        if (existing == null) {
            log.warn(LOG_BLOG_NOT_FOUND_WITH_ID, blogId);
            return ApiResponse.error(ERR_BLOG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existing.setStatus(status);
        Blog updated = blogRepo.save(existing);
        esBlogService.update(updated);
        log.info("Blog {} status changed to {}", blogId, status);

        return ApiResponse.success(mapToBlogResponseDTO(updated), HttpStatus.OK, "Blog status updated successfully");
    }

    public ResponseEntity<ApiResponse<BlogResponseDTO>> getBlog(UUID blogId) {
        log.debug("Getting blog with ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId).orElse(null);
        if (blog == null) {
            log.warn(LOG_BLOG_NOT_FOUND_WITH_ID, blogId);
            return ApiResponse.error(ERR_BLOG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        return ApiResponse.success(mapToBlogResponseDTO(blog), HttpStatus.OK, "Blog retrieved successfully");
    }

    public ResponseEntity<ApiResponse<BlogListResponseDTO>> getAllAuthorBlogs(UUID authorId, int page, int size, String sortDirection) {
        log.debug("Getting blogs for author: {} page={}, size={}, sort={}", authorId, page, size, sortDirection);

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, UPDATED_AT_FIELD));

        Page<Blog> blogPage;
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser != null && currentUser.getId().equals(authorId)) {
            blogPage = blogRepo.findByAuthorId(authorId, pageable);
        } else {
            blogPage = blogRepo.findByAuthorIdAndStatus(authorId, BlogStatus.PUBLISHED, pageable);
        }
        List<BlogResponseDTO> items = blogPage.getContent().stream().map(this::mapToBlogResponseDTO).toList();
        BlogListResponseDTO response = new BlogListResponseDTO(items);

        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put(META_TOTAL_COUNT, blogPage.getTotalElements());
        metadata.put(META_PAGE_NUMBER, blogPage.getNumber());
        metadata.put(META_PAGE_SIZE, blogPage.getSize());
        metadata.put(META_TOTAL_PAGES, blogPage.getTotalPages());
        metadata.put(META_HAS_NEXT, blogPage.hasNext());
        metadata.put(META_HAS_PREVIOUS, blogPage.hasPrevious());
        metadata.put("isFirst", blogPage.isFirst());
        metadata.put("isLast", blogPage.isLast());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", UPDATED_AT_FIELD);
        metadata.put("appliedFilters", java.util.Map.of("authorId", authorId.toString()));

        return ApiResponse.success(response, HttpStatus.OK, "Author blogs retrieved successfully", metadata);
    }

    public ResponseEntity<ApiResponse<String>> subscribe(UUID authorId) {
        log.debug("Subscribing to author: {}", authorId);

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            return ApiResponse.error(ERR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (currentUser.getId().equals(authorId)) {
            return ApiResponse.error("You cannot subscribe to yourself", HttpStatus.BAD_REQUEST);
        }

        User author = userRepo.findById(authorId).orElse(null);
        if (author == null) {
            return ApiResponse.error("Author not found", HttpStatus.NOT_FOUND);
        }

        boolean exists = subscriberRepo.findByAuthorIdAndSubscriberId(authorId, currentUser.getId()).isPresent();
        if (exists) {
            return ApiResponse.error("Already subscribed", HttpStatus.CONFLICT);
        }

        Subscriber sub = new Subscriber();
        sub.setAuthor(author);
        sub.setSubscriber(currentUser);
        subscriberRepo.save(sub);

        return ApiResponse.success("Subscribed successfully", HttpStatus.OK, "Subscribed successfully");
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> unsubscribe(UUID authorId) {
        log.debug("Unsubscribing from author: {}", authorId);

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            return ApiResponse.error(ERR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Subscriber sub = subscriberRepo.findByAuthorIdAndSubscriberId(authorId, currentUser.getId()).orElse(null);
        if (sub == null) {
            return ApiResponse.error("Subscription not found", HttpStatus.NOT_FOUND);
        }

        subscriberRepo.deleteByAuthorIdAndSubscriberId(authorId, currentUser.getId());
        return ApiResponse.success("Unsubscribed successfully", HttpStatus.OK, "Unsubscribed successfully");
    }

    public ResponseEntity<ApiResponse<AuthorSubscribersListResponseDTO>> getSubscribers(int page, int size) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            return ApiResponse.error(ERR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        
        log.debug("Getting subscribers for author: {} page={}, size={}", currentUser.getId(),page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, UPDATED_AT_FIELD));
        Page<Subscriber> subsPage = subscriberRepo.findByAuthorId(currentUser.getId(), pageable);

        List<UserInfoResponseDTO> subscribers = subsPage.getContent().stream().map(sub -> {
            User s = sub.getSubscriber();
            UserInfoResponseDTO dto = new UserInfoResponseDTO();
            dto.setFirstName(s.getFirstName());
            dto.setLastName(s.getLastName());
            dto.setEmail(s.getEmail());
            dto.setRole(s.getRole());
            dto.setEmailVerified(s.isEmailVerified());
            dto.setCreatedAt(s.getCreatedAt());
            dto.setUpdatedAt(s.getUpdatedAt());
            return dto;
        }).toList();

        AuthorSubscribersListResponseDTO response = new AuthorSubscribersListResponseDTO(subscribers);
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put(META_TOTAL_COUNT, subsPage.getTotalElements());
        metadata.put(META_PAGE_NUMBER, subsPage.getNumber());
        metadata.put(META_PAGE_SIZE, subsPage.getSize());
        metadata.put(META_TOTAL_PAGES, subsPage.getTotalPages());
        metadata.put(META_HAS_NEXT, subsPage.hasNext());
        metadata.put(META_HAS_PREVIOUS, subsPage.hasPrevious());
        return ApiResponse.success(response, HttpStatus.OK, "Subscribers retrieved successfully", metadata);
    }

    public ResponseEntity<ApiResponse<BlogListResponseDTO>> getSubscribedBlogs(int page, int size, String sortDirection) {
        log.debug("Getting subscribed blogs for current user page={}, size={}, sort={}", page, size, sortDirection);

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            return ApiResponse.error(ERR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC, UPDATED_AT_FIELD));

        Page<Subscriber> subscriptions = subscriberRepo.findBySubscriberId(currentUser.getId(), pageable);
        List<UUID> authorIds = subscriptions.getContent().stream().map(s -> s.getAuthor().getId()).toList();

        Page<Blog> blogPage;
        if (authorIds.isEmpty()) {
            blogPage = Page.empty(pageable);
        } else {
            blogPage = blogRepo.findByAuthorIdInAndStatus(authorIds, BlogStatus.PUBLISHED, pageable);
        }

        List<BlogResponseDTO> items = blogPage.getContent().stream().map(this::mapToBlogResponseDTO).toList();
        BlogListResponseDTO response = new BlogListResponseDTO(items);

        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put(META_TOTAL_COUNT, blogPage.getTotalElements());
        metadata.put(META_PAGE_NUMBER, blogPage.getNumber());
        metadata.put(META_PAGE_SIZE, blogPage.getSize());
        metadata.put(META_TOTAL_PAGES, blogPage.getTotalPages());
        metadata.put(META_HAS_NEXT, blogPage.hasNext());
        metadata.put(META_HAS_PREVIOUS, blogPage.hasPrevious());
        metadata.put("sortDirection", sortDirection);
        metadata.put("sortField", UPDATED_AT_FIELD);
        return ApiResponse.success(response, HttpStatus.OK, "Subscribed blogs retrieved successfully", metadata);
    }

    public ResponseEntity<ApiResponse<UserSubscriptionsListResponseDTO>> getSubscribedAuthors(int page, int size) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            return ApiResponse.error(ERR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        
        log.debug("Getting authors current user subscribed to: {} page={}, size={}", currentUser.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, UPDATED_AT_FIELD));
        Page<Subscriber> subsPage = subscriberRepo.findBySubscriberId(currentUser.getId(), pageable);

        List<UserInfoResponseDTO> authors = subsPage.getContent().stream().map(sub -> {
            User a = sub.getAuthor();
            UserInfoResponseDTO dto = new UserInfoResponseDTO();
            dto.setFirstName(a.getFirstName());
            dto.setLastName(a.getLastName());
            dto.setEmail(a.getEmail());
            dto.setRole(a.getRole());
            dto.setEmailVerified(a.isEmailVerified());
            dto.setCreatedAt(a.getCreatedAt());
            dto.setUpdatedAt(a.getUpdatedAt());
            return dto;
        }).toList();

        UserSubscriptionsListResponseDTO response = new UserSubscriptionsListResponseDTO(authors);
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put(META_TOTAL_COUNT, subsPage.getTotalElements());
        metadata.put(META_PAGE_NUMBER, subsPage.getNumber());
        metadata.put(META_PAGE_SIZE, subsPage.getSize());
        metadata.put(META_TOTAL_PAGES, subsPage.getTotalPages());
        metadata.put(META_HAS_NEXT, subsPage.hasNext());
        metadata.put(META_HAS_PREVIOUS, subsPage.hasPrevious());
        return ApiResponse.success(response, HttpStatus.OK, "Subscribed authors retrieved successfully", metadata);
    }

    public ResponseEntity<ApiResponse<BlogSearchListResponseDTO>> searchPublished(String query, int page, int size) {
        log.debug("Searching published blogs with query='{}', page={}, size={}", query, page, size);
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        var searchPage = esBlogService.searchPublishedBlogs(query, page, size);

        List<BlogSearchResponseDTO> items = searchPage.results().stream().map(r -> {
            BlogResponseDTO.AuthorSummaryDTO authorDto = new BlogResponseDTO.AuthorSummaryDTO(
                    r.blog().getAuthorId(),
                    r.blog().getAuthorFirstName(),
                    r.blog().getAuthorLastName(),
                    r.blog().getAuthorEmail()
            );
            boolean subscribed = false;
            if (currentUser != null && !currentUser.getId().equals(r.blog().getAuthorId())) {
                subscribed = subscriberRepo.findByAuthorIdAndSubscriberId(r.blog().getAuthorId(), currentUser.getId()).isPresent();
            }
            BlogResponseDTO blogInfo = new BlogResponseDTO(
                    r.blog().getId(),
                    authorDto,
                    r.blog().getTitle(),
                    r.blog().getContent(),
                    BlogStatus.valueOf(r.blog().getStatus()),
                    r.blog().getCreatedAt(),
                    r.blog().getUpdatedAt(),
                    subscribed
            );
            return new BlogSearchResponseDTO(blogInfo, r.highlightedTitle(), r.highlightedContent(), subscribed);
        }).toList();

        BlogSearchListResponseDTO response = new BlogSearchListResponseDTO(items);
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put(META_TOTAL_COUNT, searchPage.total());
        metadata.put(META_PAGE_NUMBER, page);
        metadata.put(META_PAGE_SIZE, size);
        long totalPages = (searchPage.total() + size - 1) / size;
        metadata.put(META_TOTAL_PAGES, totalPages);

        return ApiResponse.success(response, HttpStatus.OK, "Search completed", metadata);
    }

    private ResponseEntity<ApiResponse<BlogResponseDTO>> validateBlogAccessForAuthor(UUID blogId, String operation) {
        Blog blog = blogRepo.findById(blogId).orElse(null);
        if (blog == null) {
            log.warn(LOG_BLOG_NOT_FOUND_WITH_ID, blogId);
            return ApiResponse.error(ERR_BLOG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("Unauthenticated user attempted to {} blog", operation);
            return ApiResponse.error(ERR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!blog.getAuthor().getId().equals(currentUser.getId())) {
            log.warn("User {} not authorized to {} blog {}", currentUser.getEmail(), operation, blog.getId());
            String message = "You can only " + operation + " your own blogs";
            return ApiResponse.error(message, HttpStatus.FORBIDDEN);
        }

        return null;
    }

    private BlogResponseDTO mapToBlogResponseDTO(Blog blog) {
        BlogResponseDTO.AuthorSummaryDTO authorDto = new BlogResponseDTO.AuthorSummaryDTO(
                blog.getAuthor().getId(),
                blog.getAuthor().getFirstName(),
                blog.getAuthor().getLastName(),
                blog.getAuthor().getEmail()
        );
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        boolean subscribed = false;
        if (currentUser != null && !currentUser.getId().equals(blog.getAuthor().getId())) {
            subscribed = subscriberRepo.findByAuthorIdAndSubscriberId(blog.getAuthor().getId(), currentUser.getId()).isPresent();
        }
        return new BlogResponseDTO(
                blog.getId(),
                authorDto,
                blog.getTitle(),
                blog.getContent(),
                blog.getStatus(),
                blog.getCreatedAt(),
                blog.getUpdatedAt(),
                subscribed
        );
    }
} 