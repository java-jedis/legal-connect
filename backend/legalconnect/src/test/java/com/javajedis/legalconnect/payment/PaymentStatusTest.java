package com.javajedis.legalconnect.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentStatus Enum Tests")
class PaymentStatusTest {

    @Test
    @DisplayName("Should have all expected enum values")
    void testAllEnumValues() {
        PaymentStatus[] values = PaymentStatus.values();
        
        assertEquals(5, values.length);
        assertTrue(containsValue(values, PaymentStatus.PENDING));
        assertTrue(containsValue(values, PaymentStatus.PAID));
        assertTrue(containsValue(values, PaymentStatus.RELEASED));
        assertTrue(containsValue(values, PaymentStatus.REFUNDED));
        assertTrue(containsValue(values, PaymentStatus.CANCELED));
    }

    @Test
    @DisplayName("Should support valueOf operations")
    void testValueOfOperations() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.PAID, PaymentStatus.valueOf("PAID"));
        assertEquals(PaymentStatus.RELEASED, PaymentStatus.valueOf("RELEASED"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
        assertEquals(PaymentStatus.CANCELED, PaymentStatus.valueOf("CANCELED"));
    }

    @Test
    @DisplayName("Should have correct enum names")
    void testEnumNames() {
        assertEquals("PENDING", PaymentStatus.PENDING.name());
        assertEquals("PAID", PaymentStatus.PAID.name());
        assertEquals("RELEASED", PaymentStatus.RELEASED.name());
        assertEquals("REFUNDED", PaymentStatus.REFUNDED.name());
        assertEquals("CANCELED", PaymentStatus.CANCELED.name());
    }

    @Test
    @DisplayName("Should support ordinal operations")
    void testOrdinalOperations() {
        PaymentStatus[] values = PaymentStatus.values();
        
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i].ordinal());
        }
        
        // Test specific ordinal values
        assertEquals(0, PaymentStatus.PENDING.ordinal());
        assertEquals(1, PaymentStatus.PAID.ordinal());
        assertEquals(2, PaymentStatus.RELEASED.ordinal());
        assertEquals(3, PaymentStatus.REFUNDED.ordinal());
        assertEquals(4, PaymentStatus.CANCELED.ordinal());
    }

    @Test
    @DisplayName("Should support toString operations")
    void testToStringOperations() {
        // Enum toString() should return the name by default
        assertEquals("PENDING", PaymentStatus.PENDING.toString());
        assertEquals("PAID", PaymentStatus.PAID.toString());
        assertEquals("RELEASED", PaymentStatus.RELEASED.toString());
        assertEquals("REFUNDED", PaymentStatus.REFUNDED.toString());
        assertEquals("CANCELED", PaymentStatus.CANCELED.toString());
    }

    @Test
    @DisplayName("Should support comparison operations")
    void testComparisonOperations() {
        // Test enum comparison based on ordinal values
        assertTrue(PaymentStatus.PENDING.compareTo(PaymentStatus.PAID) < 0);
        assertTrue(PaymentStatus.PAID.compareTo(PaymentStatus.PENDING) > 0);
        assertTrue(PaymentStatus.PAID.compareTo(PaymentStatus.RELEASED) < 0);
        assertTrue(PaymentStatus.RELEASED.compareTo(PaymentStatus.REFUNDED) < 0);
        assertTrue(PaymentStatus.REFUNDED.compareTo(PaymentStatus.CANCELED) < 0);
        
        // Test equality comparison
        assertEquals(0, PaymentStatus.PENDING.compareTo(PaymentStatus.PENDING));
        assertEquals(0, PaymentStatus.PAID.compareTo(PaymentStatus.PAID));
        assertEquals(0, PaymentStatus.RELEASED.compareTo(PaymentStatus.RELEASED));
        assertEquals(0, PaymentStatus.REFUNDED.compareTo(PaymentStatus.REFUNDED));
        assertEquals(0, PaymentStatus.CANCELED.compareTo(PaymentStatus.CANCELED));
    }

    @Test
    @DisplayName("Should support equality operations")
    void testEqualityOperations() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.PENDING);
        assertEquals(PaymentStatus.PAID, PaymentStatus.PAID);
        assertEquals(PaymentStatus.RELEASED, PaymentStatus.RELEASED);
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.REFUNDED);
        assertEquals(PaymentStatus.CANCELED, PaymentStatus.CANCELED);
        
        assertNotEquals(PaymentStatus.PENDING, PaymentStatus.PAID);
        assertNotEquals(PaymentStatus.PAID, PaymentStatus.RELEASED);
        assertNotEquals(PaymentStatus.RELEASED, PaymentStatus.REFUNDED);
        assertNotEquals(PaymentStatus.REFUNDED, PaymentStatus.CANCELED);
        
        // Test with valueOf
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.PAID, PaymentStatus.valueOf("PAID"));
        assertEquals(PaymentStatus.RELEASED, PaymentStatus.valueOf("RELEASED"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
        assertEquals(PaymentStatus.CANCELED, PaymentStatus.valueOf("CANCELED"));
        
        // Test same reference
        assertSame(PaymentStatus.PENDING, PaymentStatus.PENDING);
        assertSame(PaymentStatus.PAID, PaymentStatus.PAID);
        assertNotSame(PaymentStatus.PENDING, PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should have proper enum characteristics")
    void testEnumCharacteristics() {
        // Test that it's a proper enum
        assertTrue(PaymentStatus.class.isEnum());
        
        // Test that all values are instances of PaymentStatus
        for (PaymentStatus status : PaymentStatus.values()) {
            assertTrue(status instanceof PaymentStatus);
            assertNotNull(status);
        }
    }

    @Test
    @DisplayName("Should maintain enum ordering consistency")
    void testEnumOrdering() {
        PaymentStatus[] values = PaymentStatus.values();
        
        // Verify the expected order
        assertEquals(PaymentStatus.PENDING, values[0]);
        assertEquals(PaymentStatus.PAID, values[1]);
        assertEquals(PaymentStatus.RELEASED, values[2]);
        assertEquals(PaymentStatus.REFUNDED, values[3]);
        assertEquals(PaymentStatus.CANCELED, values[4]);
        
        // Verify ordering is consistent with ordinal values
        assertTrue(PaymentStatus.PENDING.ordinal() < PaymentStatus.PAID.ordinal());
        assertTrue(PaymentStatus.PAID.ordinal() < PaymentStatus.RELEASED.ordinal());
        assertTrue(PaymentStatus.RELEASED.ordinal() < PaymentStatus.REFUNDED.ordinal());
        assertTrue(PaymentStatus.REFUNDED.ordinal() < PaymentStatus.CANCELED.ordinal());
    }

    @Test
    @DisplayName("Should handle string representation consistently")
    void testStringRepresentation() {
        for (PaymentStatus status : PaymentStatus.values()) {
            String name = status.name();
            String toString = status.toString();
            
            // Name and toString should be the same for simple enums
            assertEquals(name, toString);
            
            // Should not be null or empty
            assertNotNull(name);
            assertNotNull(toString);
            assertTrue(!name.trim().isEmpty());
            assertTrue(!toString.trim().isEmpty());
        }
    }

    @Test
    @DisplayName("Should support enum iteration")
    void testEnumIteration() {
        int count = 0;
        for (PaymentStatus status : PaymentStatus.values()) {
            assertNotNull(status);
            count++;
        }
        assertEquals(5, count);
    }

    @Test
    @DisplayName("Should validate payment status workflow ordering")
    void testPaymentStatusWorkflow() {
        // Test that the enum ordering reflects a logical payment workflow
        PaymentStatus[] values = PaymentStatus.values();
        
        // PENDING should be first (initial state)
        assertEquals(PaymentStatus.PENDING, values[0]);
        
        // PAID should come after PENDING
        assertTrue(PaymentStatus.PENDING.ordinal() < PaymentStatus.PAID.ordinal());
        
        // RELEASED should come after PAID
        assertTrue(PaymentStatus.PAID.ordinal() < PaymentStatus.RELEASED.ordinal());
        
        // REFUNDED and CANCELED are terminal states
        assertTrue(PaymentStatus.PAID.ordinal() < PaymentStatus.REFUNDED.ordinal());
        assertTrue(PaymentStatus.PAID.ordinal() < PaymentStatus.CANCELED.ordinal());
    }

    /**
     * Helper method to check if an array contains a specific value
     */
    private boolean containsValue(PaymentStatus[] values, PaymentStatus target) {
        for (PaymentStatus value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}