package com.javajedis.legalconnect.lawyer.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpecializationTypeTest {

    @Test
    void testAllSpecializationTypes() {
        // Test all enum values exist and have display names
        SpecializationType[] types = SpecializationType.values();
        assertEquals(21, types.length);
        
        for (SpecializationType type : types) {
            assertNotNull(type);
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isEmpty());
        }
    }

    @Test
    void testSpecificSpecializationTypes() {
        assertEquals("Criminal Law", SpecializationType.CRIMINAL_LAW.getDisplayName());
        assertEquals("Civil Law", SpecializationType.CIVIL_LAW.getDisplayName());
        assertEquals("Family Law", SpecializationType.FAMILY_LAW.getDisplayName());
        assertEquals("Labour Law", SpecializationType.LABOUR_LAW.getDisplayName());
        assertEquals("Corporate Law", SpecializationType.CORPORATE_LAW.getDisplayName());
        assertEquals("Constitutional Law", SpecializationType.CONSTITUTIONAL_LAW.getDisplayName());
        assertEquals("Tax Law", SpecializationType.TAX_LAW.getDisplayName());
        assertEquals("Environmental Law", SpecializationType.ENVIRONMENTAL_LAW.getDisplayName());
        assertEquals("Intellectual Property Law", SpecializationType.INTELLECTUAL_PROPERTY_LAW.getDisplayName());
        assertEquals("Banking and Finance Law", SpecializationType.BANKING_LAW.getDisplayName());
        assertEquals("Property and Real Estate Law", SpecializationType.PROPERTY_LAW.getDisplayName());
        assertEquals("Human Rights Law", SpecializationType.HUMAN_RIGHTS_LAW.getDisplayName());
        assertEquals("International Law", SpecializationType.INTERNATIONAL_LAW.getDisplayName());
        assertEquals("Cyber and ICT Law", SpecializationType.CYBER_LAW.getDisplayName());
        assertEquals("Contract Law", SpecializationType.CONTRACT_LAW.getDisplayName());
        assertEquals("Administrative Law", SpecializationType.ADMINISTRATIVE_LAW.getDisplayName());
        assertEquals("Immigration Law", SpecializationType.IMMIGRATION_LAW.getDisplayName());
        assertEquals("Consumer Protection Law", SpecializationType.CONSUMER_LAW.getDisplayName());
        assertEquals("Insurance Law", SpecializationType.INSURANCE_LAW.getDisplayName());
        assertEquals("Maritime Law", SpecializationType.MARITIME_LAW.getDisplayName());
        assertEquals("Education Law", SpecializationType.EDUCATION_LAW.getDisplayName());
    }

    @Test
    void testValueOf() {
        assertEquals(SpecializationType.CRIMINAL_LAW, SpecializationType.valueOf("CRIMINAL_LAW"));
        assertEquals(SpecializationType.CIVIL_LAW, SpecializationType.valueOf("CIVIL_LAW"));
        assertEquals(SpecializationType.CORPORATE_LAW, SpecializationType.valueOf("CORPORATE_LAW"));
    }

    @Test
    void testValues() {
        SpecializationType[] values = SpecializationType.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // Check that all expected values are present
        assertTrue(contains(values, SpecializationType.CRIMINAL_LAW));
        assertTrue(contains(values, SpecializationType.CIVIL_LAW));
        assertTrue(contains(values, SpecializationType.FAMILY_LAW));
        assertTrue(contains(values, SpecializationType.CORPORATE_LAW));
    }

    @Test
    void testDisplayNameConsistency() {
        // Test that display names are consistent and not null
        for (SpecializationType type : SpecializationType.values()) {
            String displayName = type.getDisplayName();
            assertNotNull(displayName, "Display name should not be null for " + type);
            assertFalse(displayName.trim().isEmpty(), "Display name should not be empty for " + type);
            assertFalse(displayName.isEmpty(), "Display name should have content for " + type);
        }
    }

    @Test
    void testEnumEquality() {
        SpecializationType type1 = SpecializationType.CRIMINAL_LAW;
        SpecializationType type2 = SpecializationType.CRIMINAL_LAW;
        SpecializationType type3 = SpecializationType.CIVIL_LAW;
        
        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
        assertSame(type1, type2);
        assertNotSame(type1, type3);
    }

    @Test
    void testToString() {
        for (SpecializationType type : SpecializationType.values()) {
            String toString = type.toString();
            assertNotNull(toString);
            assertEquals(type.name(), toString);
        }
    }

    private boolean contains(SpecializationType[] array, SpecializationType value) {
        for (SpecializationType type : array) {
            if (type == value) {
                return true;
            }
        }
        return false;
    }
} 