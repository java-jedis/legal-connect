package com.javajedis.legalconnect.lawyer.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VerificationStatusTest {

    @Test
    void testAllVerificationStatuses() {
        VerificationStatus[] statuses = VerificationStatus.values();
        assertEquals(3, statuses.length);
        
        for (VerificationStatus status : statuses) {
            assertNotNull(status);
            assertNotNull(status.getDisplayName());
            assertFalse(status.getDisplayName().isEmpty());
        }
    }

    @Test
    void testSpecificVerificationStatuses() {
        assertEquals("Pending", VerificationStatus.PENDING.getDisplayName());
        assertEquals("Approved", VerificationStatus.APPROVED.getDisplayName());
        assertEquals("Rejected", VerificationStatus.REJECTED.getDisplayName());
    }

    @Test
    void testValueOf() {
        assertEquals(VerificationStatus.PENDING, VerificationStatus.valueOf("PENDING"));
        assertEquals(VerificationStatus.APPROVED, VerificationStatus.valueOf("APPROVED"));
        assertEquals(VerificationStatus.REJECTED, VerificationStatus.valueOf("REJECTED"));
    }

    @Test
    void testValues() {
        VerificationStatus[] values = VerificationStatus.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        assertTrue(contains(values, VerificationStatus.PENDING));
        assertTrue(contains(values, VerificationStatus.APPROVED));
        assertTrue(contains(values, VerificationStatus.REJECTED));
    }

    @Test
    void testDisplayNameConsistency() {
        for (VerificationStatus status : VerificationStatus.values()) {
            String displayName = status.getDisplayName();
            assertNotNull(displayName, "Display name should not be null for " + status);
            assertFalse(displayName.trim().isEmpty(), "Display name should not be empty for " + status);
            assertTrue(displayName.length() > 0, "Display name should have content for " + status);
        }
    }

    @Test
    void testEnumEquality() {
        VerificationStatus status1 = VerificationStatus.PENDING;
        VerificationStatus status2 = VerificationStatus.PENDING;
        VerificationStatus status3 = VerificationStatus.APPROVED;
        
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertSame(status1, status2);
        assertNotSame(status1, status3);
    }

    @Test
    void testToString() {
        for (VerificationStatus status : VerificationStatus.values()) {
            String toString = status.toString();
            assertNotNull(toString);
            assertEquals(status.name(), toString);
        }
    }

    private boolean contains(VerificationStatus[] array, VerificationStatus value) {
        for (VerificationStatus status : array) {
            if (status == value) {
                return true;
            }
        }
        return false;
    }
} 