package com.javajedis.legalconnect.blogs;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepo extends JpaRepository<Blog, UUID> {
    Page<Blog> findByAuthorId(UUID authorId, Pageable pageable);
    Page<Blog> findByAuthorIdAndStatus(UUID authorId, BlogStatus status, Pageable pageable);
    Page<Blog> findByAuthorIdInAndStatus(Iterable<UUID> authorIds, BlogStatus status, Pageable pageable);
} 