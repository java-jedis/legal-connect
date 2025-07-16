package com.javajedis.legalconnect.lawyerdirectory.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LawyerReviewListResponseDTOTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        LawyerReviewResponseDTO review = new LawyerReviewResponseDTO(UUID.randomUUID(), UUID.randomUUID(), "Lawyer", UUID.randomUUID(), "Client", UUID.randomUUID(), (short)5, "Review", java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now());
        List<LawyerReviewResponseDTO> reviews = Collections.singletonList(review);
        LawyerReviewListResponseDTO dto = new LawyerReviewListResponseDTO(reviews);
        assertEquals(reviews, dto.getReviews());
    }

    @Test
    void testSettersAndGetters() {
        LawyerReviewListResponseDTO dto = new LawyerReviewListResponseDTO();
        LawyerReviewResponseDTO review = new LawyerReviewResponseDTO(UUID.randomUUID(), UUID.randomUUID(), "Lawyer", UUID.randomUUID(), "Client", UUID.randomUUID(), (short)4, "Review", java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now());
        List<LawyerReviewResponseDTO> reviews = Collections.singletonList(review);
        dto.setReviews(reviews);
        assertEquals(reviews, dto.getReviews());
    }

    @Test
    void testEqualsAndHashCode() {
        LawyerReviewResponseDTO review = new LawyerReviewResponseDTO(UUID.randomUUID(), UUID.randomUUID(), "Lawyer", UUID.randomUUID(), "Client", UUID.randomUUID(), (short)3, "Review", java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now());
        List<LawyerReviewResponseDTO> reviews = Collections.singletonList(review);
        LawyerReviewListResponseDTO dto1 = new LawyerReviewListResponseDTO(reviews);
        LawyerReviewListResponseDTO dto2 = new LawyerReviewListResponseDTO(reviews);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LawyerReviewListResponseDTO dto = new LawyerReviewListResponseDTO();
        dto.setReviews(Collections.emptyList());
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("reviews"));
    }
} 