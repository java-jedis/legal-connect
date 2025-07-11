package com.javajedis.legalconnect.lawyer.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PracticingCourtTest {

    @Test
    void testAllPracticingCourts() {
        // Test all enum values exist and have display names
        PracticingCourt[] courts = PracticingCourt.values();
        assertEquals(10, courts.length);
        
        for (PracticingCourt court : courts) {
            assertNotNull(court);
            assertNotNull(court.getDisplayName());
            assertFalse(court.getDisplayName().isEmpty());
        }
    }

    @Test
    void testSpecificPracticingCourts() {
        assertEquals("Supreme Court", PracticingCourt.SUPREME_COURT.getDisplayName());
        assertEquals("High Court Division", PracticingCourt.HIGH_COURT_DIVISION.getDisplayName());
        assertEquals("Appellate Division", PracticingCourt.APPELLATE_DIVISION.getDisplayName());
        assertEquals("District Court", PracticingCourt.DISTRICT_COURT.getDisplayName());
        assertEquals("Sessions Court", PracticingCourt.SESSIONS_COURT.getDisplayName());
        assertEquals("Administrative Tribunal", PracticingCourt.ADMINISTRATIVE_TRIBUNAL.getDisplayName());
        assertEquals("Labour Court", PracticingCourt.LABOUR_COURT.getDisplayName());
        assertEquals("Family Court", PracticingCourt.FAMILY_COURT.getDisplayName());
        assertEquals("Magistrate Court", PracticingCourt.MAGISTRATE_COURT.getDisplayName());
        assertEquals("Special Tribunal", PracticingCourt.SPECIAL_TRIBUNAL.getDisplayName());
    }

    @Test
    void testValueOf() {
        assertEquals(PracticingCourt.SUPREME_COURT, PracticingCourt.valueOf("SUPREME_COURT"));
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, PracticingCourt.valueOf("HIGH_COURT_DIVISION"));
        assertEquals(PracticingCourt.DISTRICT_COURT, PracticingCourt.valueOf("DISTRICT_COURT"));
    }

    @Test
    void testValues() {
        PracticingCourt[] values = PracticingCourt.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // Check that all expected values are present
        assertTrue(contains(values, PracticingCourt.SUPREME_COURT));
        assertTrue(contains(values, PracticingCourt.HIGH_COURT_DIVISION));
        assertTrue(contains(values, PracticingCourt.DISTRICT_COURT));
    }

    @Test
    void testDisplayNameConsistency() {
        // Test that display names are consistent and not null
        for (PracticingCourt court : PracticingCourt.values()) {
            String displayName = court.getDisplayName();
            assertNotNull(displayName, "Display name should not be null for " + court);
            assertFalse(displayName.trim().isEmpty(), "Display name should not be empty for " + court);
            assertFalse(displayName.isEmpty(), "Display name should have content for " + court);
        }
    }

    @Test
    void testEnumEquality() {
        PracticingCourt court1 = PracticingCourt.SUPREME_COURT;
        PracticingCourt court2 = PracticingCourt.SUPREME_COURT;
        PracticingCourt court3 = PracticingCourt.HIGH_COURT_DIVISION;
        
        assertEquals(court1, court2);
        assertNotEquals(court1, court3);
        assertSame(court1, court2);
        assertNotSame(court1, court3);
    }

    @Test
    void testToString() {
        for (PracticingCourt court : PracticingCourt.values()) {
            String toString = court.toString();
            assertNotNull(toString);
            assertEquals(court.name(), toString);
        }
    }

    private boolean contains(PracticingCourt[] array, PracticingCourt value) {
        for (PracticingCourt court : array) {
            if (court == value) {
                return true;
            }
        }
        return false;
    }
} 