package com.javajedis.legalconnect.blogs.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.blogs.BlogStatus;

class BlogListResponseDTOTest {

    @Test
    void testDefaultConstructor() {
        BlogListResponseDTO dto = new BlogListResponseDTO();
        assertNull(dto.getBlogs());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        BlogResponseDTO blog = new BlogResponseDTO(UUID.randomUUID(), null, "T", "C", BlogStatus.DRAFT, null, null);
        List<BlogResponseDTO> list = Collections.singletonList(blog);
        BlogListResponseDTO dto = new BlogListResponseDTO(list);
        assertEquals(list, dto.getBlogs());
    }

    @Test
    void testSettersAndGetters() {
        BlogListResponseDTO dto = new BlogListResponseDTO();
        BlogResponseDTO b1 = new BlogResponseDTO(UUID.randomUUID(), null, "T1", "C1", BlogStatus.DRAFT, null, null);
        BlogResponseDTO b2 = new BlogResponseDTO(UUID.randomUUID(), null, "T2", "C2", BlogStatus.PUBLISHED, null, null);
        List<BlogResponseDTO> list = Arrays.asList(b1, b2);
        dto.setBlogs(list);
        assertEquals(list, dto.getBlogs());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        BlogResponseDTO b = new BlogResponseDTO(UUID.randomUUID(), null, "T", "C", BlogStatus.DRAFT, null, null);
        List<BlogResponseDTO> list = Collections.singletonList(b);
        BlogListResponseDTO d1 = new BlogListResponseDTO(list);
        BlogListResponseDTO d2 = new BlogListResponseDTO(list);
        BlogListResponseDTO d3 = new BlogListResponseDTO(Collections.emptyList());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1.hashCode(), d3.hashCode());

        String ts = d1.toString();
        assertNotNull(ts);
        assert ts.contains("T");
    }
} 