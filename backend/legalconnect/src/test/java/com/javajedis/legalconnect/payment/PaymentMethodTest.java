package com.javajedis.legalconnect.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentMethod Enum Tests")
class PaymentMethodTest {

    @Test
    @DisplayName("Should have all expected enum values")
    void testAllEnumValues() {
        PaymentMethod[] values = PaymentMethod.values();
        
        assertEquals(2, values.length);
        assertTrue(containsValue(values, PaymentMethod.CARD));
        assertTrue(containsValue(values, PaymentMethod.MFS));
    }

    @Test
    @DisplayName("Should support valueOf operations")
    void testValueOfOperations() {
        assertEquals(PaymentMethod.CARD, PaymentMethod.valueOf("CARD"));
        assertEquals(PaymentMethod.MFS, PaymentMethod.valueOf("MFS"));
    }

    @Test
    @DisplayName("Should have correct enum names")
    void testEnumNames() {
        assertEquals("CARD", PaymentMethod.CARD.name());
        assertEquals("MFS", PaymentMethod.MFS.name());
    }

    @Test
    @DisplayName("Should support ordinal operations")
    void testOrdinalOperations() {
        PaymentMethod[] values = PaymentMethod.values();
        
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i].ordinal());
        }
        
        // Test specific ordinal values
        assertEquals(0, PaymentMethod.CARD.ordinal());
        assertEquals(1, PaymentMethod.MFS.ordinal());
    }

    @Test
    @DisplayName("Should support toString operations")
    void testToStringOperations() {
        // Enum toString() should return the name by default
        assertEquals("CARD", PaymentMethod.CARD.toString());
        assertEquals("MFS", PaymentMethod.MFS.toString());
    }

    @Test
    @DisplayName("Should support comparison operations")
    void testComparisonOperations() {
        // Test enum comparison based on ordinal values
        assertTrue(PaymentMethod.CARD.compareTo(PaymentMethod.MFS) < 0);
        assertTrue(PaymentMethod.MFS.compareTo(PaymentMethod.CARD) > 0);
        assertEquals(0, PaymentMethod.CARD.compareTo(PaymentMethod.CARD));
        assertEquals(0, PaymentMethod.MFS.compareTo(PaymentMethod.MFS));
    }

    @Test
    @DisplayName("Should support equality operations")
    void testEqualityOperations() {
        assertEquals(PaymentMethod.CARD, PaymentMethod.CARD);
        assertEquals(PaymentMethod.MFS, PaymentMethod.MFS);
        assertNotEquals(PaymentMethod.CARD, PaymentMethod.MFS);
        assertNotEquals(PaymentMethod.MFS, PaymentMethod.CARD);
        
        // Test with valueOf
        assertEquals(PaymentMethod.CARD, PaymentMethod.valueOf("CARD"));
        assertEquals(PaymentMethod.MFS, PaymentMethod.valueOf("MFS"));
        
        // Test same reference
        assertSame(PaymentMethod.CARD, PaymentMethod.CARD);
        assertSame(PaymentMethod.MFS, PaymentMethod.MFS);
        assertNotSame(PaymentMethod.CARD, PaymentMethod.MFS);
    }

    @Test
    @DisplayName("Should have proper enum characteristics")
    void testEnumCharacteristics() {
        // Test that it's a proper enum
        assertTrue(PaymentMethod.class.isEnum());
        
        // Test that all values are instances of PaymentMethod
        for (PaymentMethod method : PaymentMethod.values()) {
            assertTrue(method instanceof PaymentMethod);
            assertNotNull(method);
        }
    }

    @Test
    @DisplayName("Should maintain enum ordering consistency")
    void testEnumOrdering() {
        PaymentMethod[] values = PaymentMethod.values();
        
        // Verify the expected order
        assertEquals(PaymentMethod.CARD, values[0]);
        assertEquals(PaymentMethod.MFS, values[1]);
        
        // Verify ordering is consistent with ordinal values
        assertTrue(PaymentMethod.CARD.ordinal() < PaymentMethod.MFS.ordinal());
    }

    @Test
    @DisplayName("Should handle string representation consistently")
    void testStringRepresentation() {
        for (PaymentMethod method : PaymentMethod.values()) {
            String name = method.name();
            String toString = method.toString();
            
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
        for (PaymentMethod method : PaymentMethod.values()) {
            assertNotNull(method);
            count++;
        }
        assertEquals(2, count);
    }

    /**
     * Helper method to check if an array contains a specific value
     */
    private boolean containsValue(PaymentMethod[] values, PaymentMethod target) {
        for (PaymentMethod value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}