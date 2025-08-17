package com.javajedis.legalconnect.lawyerdirectory.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LawyerReviewResponseDTOTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        UUID lawyerId = UUID.randomUUID();
        String lawyerName = "John Doe";
        UUID clientId = UUID.randomUUID();
        String clientName = "Jane Smith";
        UUID caseId = UUID.randomUUID();
        short rating = 5;
        String review = "Excellent!";
        OffsetDateTime createdAt = OffsetDateTime.now();
        OffsetDateTime updatedAt = OffsetDateTime.now();
        LawyerReviewResponseDTO dto = new LawyerReviewResponseDTO(id, lawyerId, lawyerName, clientId, clientName, caseId, rating, review, createdAt, updatedAt);
        assertEquals(id, dto.getId());
        assertEquals(lawyerId, dto.getLawyerId());
        assertEquals(lawyerName, dto.getLawyerName());
        assertEquals(clientId, dto.getClientId());
        assertEquals(clientName, dto.getClientName());
        assertEquals(caseId, dto.getCaseId());
        assertEquals(rating, dto.getRating());
        assertEquals(review, dto.getReview());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        LawyerReviewResponseDTO dto = new LawyerReviewResponseDTO();
        UUID id = UUID.randomUUID();
        dto.setId(id);
        dto.setLawyerId(UUID.randomUUID());
        dto.setLawyerName("Lawyer");
        dto.setClientId(UUID.randomUUID());
        dto.setClientName("Client");
        dto.setCaseId(UUID.randomUUID());
        dto.setRating((short) 4);
        dto.setReview("Review");
        OffsetDateTime now = OffsetDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        assertEquals(id, dto.getId());
        assertEquals("Lawyer", dto.getLawyerName());
        assertEquals("Client", dto.getClientName());
        assertEquals((short) 4, dto.getRating());
        assertEquals("Review", dto.getReview());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        LawyerReviewResponseDTO dto1 = new LawyerReviewResponseDTO();
        dto1.setId(id);
        LawyerReviewResponseDTO dto2 = new LawyerReviewResponseDTO();
        dto2.setId(id);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LawyerReviewResponseDTO dto = new LawyerReviewResponseDTO();
        dto.setLawyerName("Lawyer");
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("Lawyer"));
    }
} 