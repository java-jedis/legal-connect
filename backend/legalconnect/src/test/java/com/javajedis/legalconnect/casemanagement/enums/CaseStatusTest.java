package com.javajedis.legalconnect.casemanagement.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.CaseStatus;

class CaseStatusTest {

    @Test
    void testAllCaseStatuses() {
        CaseStatus[] statuses = CaseStatus.values();
        assertEquals(2, statuses.length);
        
        for (CaseStatus status : statuses) {
            assertNotNull(status);
            assertNotNull(status.getDisplayName());
            assertFalse(status.getDisplayName().isEmpty());
        }
    }

    @Test
    void testSpecificCaseStatuses() {
        assertEquals("In Progress", CaseStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Resolved", CaseStatus.RESOLVED.getDisplayName());
    }

    @Test
    void testValueOf() {
        assertEquals(CaseStatus.IN_PROGRESS, CaseStatus.valueOf("IN_PROGRESS"));
        assertEquals(CaseStatus.RESOLVED, CaseStatus.valueOf("RESOLVED"));
    }

    @Test
    void testValues() {
        CaseStatus[] values = CaseStatus.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        assertTrue(contains(values, CaseStatus.IN_PROGRESS));
        assertTrue(contains(values, CaseStatus.RESOLVED));
    }

    @Test
    void testDisplayNameConsistency() {
        for (CaseStatus status : CaseStatus.values()) {
            String displayName = status.getDisplayName();
            assertNotNull(displayName, "Display name should not be null for " + status);
            assertFalse(displayName.trim().isEmpty(), "Display name should not be empty for " + status);
            assertFalse(displayName.isEmpty(), "Display name should have content for " + status);
        }
    }

    @Test
    void testEnumEquality() {
        CaseStatus status1 = CaseStatus.IN_PROGRESS;
        CaseStatus status2 = CaseStatus.IN_PROGRESS;
        CaseStatus status3 = CaseStatus.RESOLVED;
        
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertSame(status1, status2);
        assertNotSame(status1, status3);
    }

    @Test
    void testToString() {
        for (CaseStatus status : CaseStatus.values()) {
            String toString = status.toString();
            assertNotNull(toString);
            assertEquals(status.name(), toString);
        }
    }

    private boolean contains(CaseStatus[] array, CaseStatus value) {
        for (CaseStatus status : array) {
            if (status == value) {
                return true;
            }
        }
        return false;
    }
} 