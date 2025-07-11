package com.javajedis.legalconnect.lawyer.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DivisionTest {

    @Test
    void testAllDivisions() {
        // Test all enum values exist and have display names
        Division[] divisions = Division.values();
        assertEquals(8, divisions.length);
        
        for (Division division : divisions) {
            assertNotNull(division);
            assertNotNull(division.getDisplayName());
            assertFalse(division.getDisplayName().isEmpty());
        }
    }

    @Test
    void testSpecificDivisions() {
        assertEquals("Dhaka", Division.DHAKA.getDisplayName());
        assertEquals("Chattogram", Division.CHATTOGRAM.getDisplayName());
        assertEquals("Rajshahi", Division.RAJSHAHI.getDisplayName());
        assertEquals("Khulna", Division.KHULNA.getDisplayName());
        assertEquals("Barishal", Division.BARISHAL.getDisplayName());
        assertEquals("Sylhet", Division.SYLHET.getDisplayName());
        assertEquals("Rangpur", Division.RANGPUR.getDisplayName());
        assertEquals("Mymensingh", Division.MYMENSINGH.getDisplayName());
    }

    @Test
    void testValueOf() {
        assertEquals(Division.DHAKA, Division.valueOf("DHAKA"));
        assertEquals(Division.CHATTOGRAM, Division.valueOf("CHATTOGRAM"));
        assertEquals(Division.RAJSHAHI, Division.valueOf("RAJSHAHI"));
    }

    @Test
    void testValues() {
        Division[] values = Division.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // Check that all expected values are present
        assertTrue(contains(values, Division.DHAKA));
        assertTrue(contains(values, Division.CHATTOGRAM));
        assertTrue(contains(values, Division.RAJSHAHI));
    }

    @Test
    void testDisplayNameConsistency() {
        // Test that display names are consistent and not null
        for (Division division : Division.values()) {
            String displayName = division.getDisplayName();
            assertNotNull(displayName, "Display name should not be null for " + division);
            assertFalse(displayName.trim().isEmpty(), "Display name should not be empty for " + division);
            assertFalse(displayName.isEmpty(), "Display name should have content for " + division);
        }
    }

    @Test
    void testEnumEquality() {
        Division division1 = Division.DHAKA;
        Division division2 = Division.DHAKA;
        Division division3 = Division.CHATTOGRAM;
        
        assertEquals(division1, division2);
        assertNotEquals(division1, division3);
        assertSame(division1, division2);
        assertNotSame(division1, division3);
    }

    @Test
    void testToString() {
        for (Division division : Division.values()) {
            String toString = division.toString();
            assertNotNull(toString);
            assertEquals(division.name(), toString);
        }
    }

    private boolean contains(Division[] array, Division value) {
        for (Division division : array) {
            if (division == value) {
                return true;
            }
        }
        return false;
    }
} 