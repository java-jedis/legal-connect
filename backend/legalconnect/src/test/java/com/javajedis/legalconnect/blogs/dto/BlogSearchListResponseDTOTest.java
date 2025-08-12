package com.javajedis.legalconnect.blogs.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class BlogSearchListResponseDTOTest {

    @Test
    void testDefaultConstructor() {
        BlogSearchListResponseDTO dto = new BlogSearchListResponseDTO();
        assertNull(dto.getResults());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        BlogSearchResponseDTO r = new BlogSearchResponseDTO();
        List<BlogSearchResponseDTO> list = Collections.singletonList(r);
        BlogSearchListResponseDTO dto = new BlogSearchListResponseDTO(list);
        assertEquals(list, dto.getResults());
    }

    @Test
    void testSettersAndGetters() {
        BlogSearchListResponseDTO dto = new BlogSearchListResponseDTO();
        BlogSearchResponseDTO r1 = new BlogSearchResponseDTO();
        BlogSearchResponseDTO r2 = new BlogSearchResponseDTO();
        List<BlogSearchResponseDTO> list = Arrays.asList(r1, r2);
        dto.setResults(list);
        assertEquals(list, dto.getResults());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        BlogSearchResponseDTO r = new BlogSearchResponseDTO();
        List<BlogSearchResponseDTO> list = Collections.singletonList(r);
        BlogSearchListResponseDTO d1 = new BlogSearchListResponseDTO(list);
        BlogSearchListResponseDTO d2 = new BlogSearchListResponseDTO(list);
        BlogSearchListResponseDTO d3 = new BlogSearchListResponseDTO(Collections.emptyList());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1.hashCode(), d3.hashCode());

        String ts = d1.toString();
        assertNotNull(ts);
    }
} 