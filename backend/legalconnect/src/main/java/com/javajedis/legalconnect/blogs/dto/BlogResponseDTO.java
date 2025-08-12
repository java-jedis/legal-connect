package com.javajedis.legalconnect.blogs.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.javajedis.legalconnect.blogs.BlogStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDTO {
    private UUID blogId;
    private AuthorSummaryDTO author;
    private String title;
    private String content;
    private BlogStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean subscribed;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorSummaryDTO {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
} 