package com.javajedis.legalconnect.caseassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AssetPrivacyTest {

    @Test
    void testPrivateEnum() {
        AssetPrivacy privacy = AssetPrivacy.PRIVATE;
        
        assertNotNull(privacy);
        assertEquals("Private", privacy.getDisplayName());
        assertEquals("PRIVATE", privacy.name());
    }

    @Test
    void testSharedEnum() {
        AssetPrivacy privacy = AssetPrivacy.SHARED;
        
        assertNotNull(privacy);
        assertEquals("Shared", privacy.getDisplayName());
        assertEquals("SHARED", privacy.name());
    }

    @Test
    void testAllEnumValues() {
        AssetPrivacy[] values = AssetPrivacy.values();
        
        assertEquals(2, values.length);
        assertEquals(AssetPrivacy.PRIVATE, values[0]);
        assertEquals(AssetPrivacy.SHARED, values[1]);
    }

    @Test
    void testValueOf() {
        assertEquals(AssetPrivacy.PRIVATE, AssetPrivacy.valueOf("PRIVATE"));
        assertEquals(AssetPrivacy.SHARED, AssetPrivacy.valueOf("SHARED"));
    }

    @Test
    void testDisplayNames() {
        for (AssetPrivacy privacy : AssetPrivacy.values()) {
            assertNotNull(privacy.getDisplayName());
            assertNotNull(privacy.name());
        }
    }
} 