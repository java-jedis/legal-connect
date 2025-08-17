package com.javajedis.legalconnect.notifications.dto;

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
        int unreadCount = 5;

        // Act
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(unreadCount);

        // Assert
        assertEquals(unreadCount, dto.getUnreadCount());
    }

    @Test
    @DisplayName("Should create DTO with no args constructor")
    void testNoArgsConstructor() {
        // Act
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO();

        // Assert
        assertEquals(0, dto.getUnreadCount()); // Default int value is 0
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void testGettersAndSetters() {
        // Arrange
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO();
        int unreadCount = 10;

        // Act
        dto.setUnreadCount(unreadCount);

        // Assert
        assertEquals(unreadCount, dto.getUnreadCount());
    }

    @Test
    @DisplayName("Should handle zero unread count")
    void testZeroUnreadCount() {
        // Act
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(0);

        // Assert
        assertEquals(0, dto.getUnreadCount());
    }

    @Test
    @DisplayName("Should handle positive unread count")
    void testPositiveUnreadCount() {
        // Test various positive values
        int[] testValues = {1, 5, 10, 50, 100, 999, 1000, Integer.MAX_VALUE};

        for (int value : testValues) {
            // Act
            UnreadCountResponseDTO dto = new UnreadCountResponseDTO(value);

            // Assert
            assertEquals(value, dto.getUnreadCount());
        }
    }

    @Test
    @DisplayName("Should handle negative unread count")
    void testNegativeUnreadCount() {
        // Test various negative values (though logically shouldn't happen)
        int[] testValues = {-1, -5, -10, -100, Integer.MIN_VALUE};

        for (int value : testValues) {
            // Act
            UnreadCountResponseDTO dto = new UnreadCountResponseDTO(value);

            // Assert
            assertEquals(value, dto.getUnreadCount());
        }
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Arrange
        int unreadCount = 15;
        UnreadCountResponseDTO dto1 = new UnreadCountResponseDTO(unreadCount);
        UnreadCountResponseDTO dto2 = new UnreadCountResponseDTO(unreadCount);
        UnreadCountResponseDTO dto3 = new UnreadCountResponseDTO(20);

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
        int unreadCount = 25;
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(unreadCount);

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("UnreadCountResponseDTO"));
        assertTrue(toString.contains("25"));
    }

    @Test
    @DisplayName("Should handle boundary values")
    void testBoundaryValues() {
        // Test Integer.MAX_VALUE
        UnreadCountResponseDTO maxDto = new UnreadCountResponseDTO(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, maxDto.getUnreadCount());

        // Test Integer.MIN_VALUE
        UnreadCountResponseDTO minDto = new UnreadCountResponseDTO(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, minDto.getUnreadCount());

        // Test zero
        UnreadCountResponseDTO zeroDto = new UnreadCountResponseDTO(0);
        assertEquals(0, zeroDto.getUnreadCount());
    }

    @Test
    @DisplayName("Should handle count modifications")
    void testCountModifications() {
        // Arrange
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO();

        // Test setting various values
        int[] testValues = {0, 1, 5, 10, 100, 1000};

        for (int value : testValues) {
            // Act
            dto.setUnreadCount(value);

            // Assert
            assertEquals(value, dto.getUnreadCount());
        }
    }

    @Test
    @DisplayName("Should maintain consistency across operations")
    void testConsistencyAcrossOperations() {
        // Arrange
        int initialCount = 42;
        UnreadCountResponseDTO dto = new UnreadCountResponseDTO(initialCount);

        // Verify initial state
        assertEquals(initialCount, dto.getUnreadCount());

        // Test multiple get operations
        for (int i = 0; i < 10; i++) {
            assertEquals(initialCount, dto.getUnreadCount());
        }

        // Test setting new value
        int newCount = 84;
        dto.setUnreadCount(newCount);

        // Verify new state
        for (int i = 0; i < 10; i++) {
            assertEquals(newCount, dto.getUnreadCount());
        }
    }

    @Test
    @DisplayName("Should handle equals with same values")
    void testEqualsWithSameValues() {
        // Test multiple DTOs with same value
        int count = 7;
        UnreadCountResponseDTO dto1 = new UnreadCountResponseDTO(count);
        UnreadCountResponseDTO dto2 = new UnreadCountResponseDTO(count);
        UnreadCountResponseDTO dto3 = new UnreadCountResponseDTO();
        dto3.setUnreadCount(count);

        // All should be equal
        assertEquals(dto1, dto2);
        assertEquals(dto1, dto3);
        assertEquals(dto2, dto3);

        // Hash codes should be equal
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertEquals(dto1.hashCode(), dto3.hashCode());
        assertEquals(dto2.hashCode(), dto3.hashCode());
    }

    @Test
    @DisplayName("Should handle toString with different values")
    void testToStringWithDifferentValues() {
        // Test toString with various values
        int[] testValues = {0, 1, 10, 100, 1000, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

        for (int value : testValues) {
            UnreadCountResponseDTO dto = new UnreadCountResponseDTO(value);
            String toString = dto.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("UnreadCountResponseDTO"));
            assertTrue(toString.contains(String.valueOf(value)));
        }
    }

    @Test
    @DisplayName("Should handle realistic notification count scenarios")
    void testRealisticNotificationCountScenarios() {
        // Test common notification count scenarios
        
        // No unread notifications
        UnreadCountResponseDTO noUnread = new UnreadCountResponseDTO(0);
        assertEquals(0, noUnread.getUnreadCount());

        // Few unread notifications
        UnreadCountResponseDTO fewUnread = new UnreadCountResponseDTO(3);
        assertEquals(3, fewUnread.getUnreadCount());

        // Many unread notifications
        UnreadCountResponseDTO manyUnread = new UnreadCountResponseDTO(50);
        assertEquals(50, manyUnread.getUnreadCount());

        // Very high unread count (edge case)
        UnreadCountResponseDTO veryHighUnread = new UnreadCountResponseDTO(9999);
        assertEquals(9999, veryHighUnread.getUnreadCount());
    }
}