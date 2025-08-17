package com.javajedis.legalconnect.blogs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogSearchResponseDTO {
    private BlogResponseDTO blog;
    private String highlightedTitle;
    private String highlightedContent;
    private boolean subscribed;
} 