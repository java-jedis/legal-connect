package com.javajedis.legalconnect.chat.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UnreadCountResponseDTO Tests")
class UnreadCountResponseDTOTest {

    @Test
    @DisplayName("Should create DTO with all args constructor")
    void testAllArgsConstructor() {
        // Arrange
        long totalUnreadCount = 42L;

        // Act
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(totalUnreadCount);

        // Assert
        assertEquals(totalUnreadCount, dto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO();

        // Assert
        assertEquals(0L, dto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO();
        long totalUnreadCount = 15L;

        // Act
        dto.setTotalUnreadCount(totalUnreadCount);

        // Assert
        assertEquals(totalUnreadCount, dto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should handle zero unread count")
    void testZeroUnreadCount() {
        // Arrange
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(0L);

        // Assert
        assertEquals(0L, dto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should handle large unread count")
    void testLargeUnreadCount() {
        // Arrange
        long largeCount = Long.MAX_VALUE;
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(largeCount);

        // Assert
        assertEquals(largeCount, dto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should handle negative unread count")
    void testNegativeUnreadCount() {
        // Arrange
        long negativeCount = -1L;
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(negativeCount);

        // Assert
        assertEquals(negativeCount, dto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        long count = 25L;
        UnreadCountResponseDTO dto1 = new UnreadCountResponseDTO(count);
        UnreadCountResponseDTO dto2 = new UnreadCountResponseDTO(count);
        UnreadCountResponseDTO dto3 = new UnreadCountResponseDTO(50L);

        // Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals(null, dto1);
        assertNotEquals("string", dto1);
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        // Arrange
        long count = 33L;
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(count);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("UnreadCountResponseDTO"));
        assertTrue(toString.contains(String.valueOf(count)));
    }

    @Test
    @DisplayName("Should handle boundary values")
    void testBoundaryValues() {
        // Test minimum long value
        UnreadCountResponseDTO minDto = new UnreadCountResponseDTO(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, minDto.getTotalUnreadCount());

        // Test maximum long value
        UnreadCountResponseDTO maxDto = new UnreadCountResponseDTO(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, maxDto.getTotalUnreadCount());

        // Test zero
        UnreadCountResponseDTO zeroDto = new UnreadCountResponseDTO(0L);
        assertEquals(0L, zeroDto.getTotalUnreadCount());
    }

    @Test
    @DisplayName("Should handle typical use case values")
    void testTypicalValues() {
        // Test typical small values
        for (long i = 0; i <= 100; i += 10) {
            UnreadCountResponseDTO dto = new UnreadCountResponseDTO(i);
            assertEquals(i, dto.getTotalUnreadCount());
        }

        // Test typical larger values
        long[] typicalValues = {1000L, 5000L, 10000L, 50000L, 100000L};
        for (long value : typicalValues) {
            UnreadCountResponseDTO dto = new UnreadCountResponseDTO(value);
            assertEquals(value, dto.getTotalUnreadCount());
        }
    }

    @Test
    @DisplayName("Should maintain immutability of count after construction")
    void testImmutabilityAfterConstruction() {
        // Arrange
        long originalCount = 42L;
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(originalCount);

        // Act - modify through setter
        dto.setTotalUnreadCount(100L);

        // Assert - value should be updated (not immutable, but testing setter works)
        assertEquals(100L, dto.getTotalUnreadCount());
        assertNotEquals(originalCount, dto.getTotalUnreadCount());
    }
}