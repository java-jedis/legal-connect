package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DistrictTest {

    @Test
    void testAllDistricts() {
        // Test all enum values exist and have display names
        District[] districts = District.values();
        assertEquals(64, districts.length);
        
        for (District district : districts) {
            assertNotNull(district);
            assertNotNull(district.getDisplayName());
            assertFalse(district.getDisplayName().isEmpty());
        }
    }

    @Test
    void testSpecificDistricts() {
        assertEquals("Dhaka", District.DHAKA.getDisplayName());
        assertEquals("Chattogram", District.CHATTOGRAM.getDisplayName());
        assertEquals("Rajshahi", District.RAJSHAHI.getDisplayName());
        assertEquals("Khulna", District.KHULNA.getDisplayName());
        assertEquals("Barishal", District.BARISHAL.getDisplayName());
        assertEquals("Sylhet", District.SYLHET.getDisplayName());
        assertEquals("Rangpur", District.RANGPUR.getDisplayName());
        assertEquals("Mymensingh", District.MYMENSINGH.getDisplayName());
        assertEquals("Bagerhat", District.BAGERHAT.getDisplayName());
        assertEquals("Bandarban", District.BANDARBAN.getDisplayName());
    }

    @Test
    void testValueOf() {
        assertEquals(District.DHAKA, District.valueOf("DHAKA"));
        assertEquals(District.CHATTOGRAM, District.valueOf("CHATTOGRAM"));
        assertEquals(District.RAJSHAHI, District.valueOf("RAJSHAHI"));
    }

    @Test
    void testValues() {
        District[] values = District.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // Check that all expected values are present
        assertTrue(contains(values, District.DHAKA));
        assertTrue(contains(values, District.CHATTOGRAM));
        assertTrue(contains(values, District.RAJSHAHI));
    }

    @Test
    void testDisplayNameConsistency() {
        // Test that display names are consistent and not null
        for (District district : District.values()) {
            String displayName = district.getDisplayName();
            assertNotNull(displayName, "Display name should not be null for " + district);
            assertFalse(displayName.trim().isEmpty(), "Display name should not be empty for " + district);
            assertTrue(displayName.length() > 0, "Display name should have content for " + district);
        }
    }

    @Test
    void testEnumEquality() {
        District district1 = District.DHAKA;
        District district2 = District.DHAKA;
        District district3 = District.CHATTOGRAM;
        
        assertEquals(district1, district2);
        assertNotEquals(district1, district3);
        assertSame(district1, district2);
        assertNotSame(district1, district3);
    }

    @Test
    void testToString() {
        for (District district : District.values()) {
            String toString = district.toString();
            assertNotNull(toString);
            assertEquals(district.name(), toString);
        }
    }

    @Test
    void testAllDistrictNamesAreUnique() {
        District[] districts = District.values();
        for (int i = 0; i < districts.length; i++) {
            for (int j = i + 1; j < districts.length; j++) {
                assertNotEquals(districts[i].getDisplayName(), districts[j].getDisplayName(),
                    "District display names should be unique: " + districts[i].getDisplayName());
            }
        }
    }

    private boolean contains(District[] array, District value) {
        for (District district : array) {
            if (district == value) {
                return true;
            }
        }
        return false;
    }
} 