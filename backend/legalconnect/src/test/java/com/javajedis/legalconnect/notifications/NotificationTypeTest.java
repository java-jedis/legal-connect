package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationType Enum Tests")
class NotificationTypeTest {

    @Test
    @DisplayName("Should have correct display name for CASE_CREATE")
    void testCaseCreateDisplayName() {
        assertEquals("Case Creation", NotificationType.CASE_CREATE.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct display name for EVENT_ADD")
    void testEventAddDisplayName() {
        assertEquals("Event Addition", NotificationType.EVENT_ADD.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct display name for SCHEDULE_REMINDER")
    void testScheduleReminderDisplayName() {
        assertEquals("Schedule Reminder", NotificationType.SCHEDULE_REMINDER.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct display name for DOC_UPLOAD")
    void testDocUploadDisplayName() {
        assertEquals("Document Upload", NotificationType.DOC_UPLOAD.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct display name for NOTE_CREATE")
    void testNoteCreateDisplayName() {
        assertEquals("Note Creation", NotificationType.NOTE_CREATE.getDisplayName());
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void testAllEnumValues() {
        NotificationType[] values = NotificationType.values();
        
        assertEquals(5, values.length);
        assertTrue(containsValue(values, NotificationType.CASE_CREATE));
        assertTrue(containsValue(values, NotificationType.EVENT_ADD));
        assertTrue(containsValue(values, NotificationType.SCHEDULE_REMINDER));
        assertTrue(containsValue(values, NotificationType.DOC_UPLOAD));
        assertTrue(containsValue(values, NotificationType.NOTE_CREATE));
    }

    @Test
    @DisplayName("Should have non-null display names for all values")
    void testAllDisplayNamesNotNull() {
        for (NotificationType type : NotificationType.values()) {
            assertNotNull(type.getDisplayName());
            assertTrue(!type.getDisplayName().isEmpty());
        }
    }

    @Test
    @DisplayName("Should have unique display names")
    void testUniqueDisplayNames() {
        NotificationType[] values = NotificationType.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getDisplayName(), values[j].getDisplayName(),
                    "Display names should be unique: " + values[i].getDisplayName() + " vs " + values[j].getDisplayName());
            }
        }
    }

    @Test
    @DisplayName("Should support valueOf operations")
    void testValueOfOperations() {
        assertEquals(NotificationType.CASE_CREATE, NotificationType.valueOf("CASE_CREATE"));
        assertEquals(NotificationType.EVENT_ADD, NotificationType.valueOf("EVENT_ADD"));
        assertEquals(NotificationType.SCHEDULE_REMINDER, NotificationType.valueOf("SCHEDULE_REMINDER"));
        assertEquals(NotificationType.DOC_UPLOAD, NotificationType.valueOf("DOC_UPLOAD"));
        assertEquals(NotificationType.NOTE_CREATE, NotificationType.valueOf("NOTE_CREATE"));
    }

    @Test
    @DisplayName("Should have consistent enum name and display name relationship")
    void testEnumNameDisplayNameRelationship() {
        // Test that display names are human-readable versions of enum names
        assertEquals("CASE_CREATE", NotificationType.CASE_CREATE.name());
        assertEquals("Case Creation", NotificationType.CASE_CREATE.getDisplayName());
        
        assertEquals("EVENT_ADD", NotificationType.EVENT_ADD.name());
        assertEquals("Event Addition", NotificationType.EVENT_ADD.getDisplayName());
        
        assertEquals("SCHEDULE_REMINDER", NotificationType.SCHEDULE_REMINDER.name());
        assertEquals("Schedule Reminder", NotificationType.SCHEDULE_REMINDER.getDisplayName());
        
        assertEquals("DOC_UPLOAD", NotificationType.DOC_UPLOAD.name());
        assertEquals("Document Upload", NotificationType.DOC_UPLOAD.getDisplayName());
        
        assertEquals("NOTE_CREATE", NotificationType.NOTE_CREATE.name());
        assertEquals("Note Creation", NotificationType.NOTE_CREATE.getDisplayName());
    }

    @Test
    @DisplayName("Should support ordinal operations")
    void testOrdinalOperations() {
        NotificationType[] values = NotificationType.values();
        
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i].ordinal());
        }
    }

    @Test
    @DisplayName("Should support toString operations")
    void testToStringOperations() {
        // Enum toString() should return the name by default
        assertEquals("CASE_CREATE", NotificationType.CASE_CREATE.toString());
        assertEquals("EVENT_ADD", NotificationType.EVENT_ADD.toString());
        assertEquals("SCHEDULE_REMINDER", NotificationType.SCHEDULE_REMINDER.toString());
        assertEquals("DOC_UPLOAD", NotificationType.DOC_UPLOAD.toString());
        assertEquals("NOTE_CREATE", NotificationType.NOTE_CREATE.toString());
    }

    @Test
    @DisplayName("Should support comparison operations")
    void testComparisonOperations() {
        // Test enum comparison based on ordinal values
        assertTrue(NotificationType.CASE_CREATE.compareTo(NotificationType.EVENT_ADD) < 0);
        assertTrue(NotificationType.EVENT_ADD.compareTo(NotificationType.CASE_CREATE) > 0);
        assertEquals(0, NotificationType.CASE_CREATE.compareTo(NotificationType.CASE_CREATE));
    }

    @Test
    @DisplayName("Should support equality operations")
    void testEqualityOperations() {
        assertEquals(NotificationType.CASE_CREATE, NotificationType.CASE_CREATE);
        assertNotEquals(NotificationType.CASE_CREATE, NotificationType.EVENT_ADD);
        
        // Test with valueOf
        assertEquals(NotificationType.CASE_CREATE, NotificationType.valueOf("CASE_CREATE"));
    }

    @Test
    @DisplayName("Should have proper enum characteristics")
    void testEnumCharacteristics() {
        // Test that it's a proper enum
        assertTrue(NotificationType.class.isEnum());
        
        // Test that all values are instances of NotificationType
        for (NotificationType type : NotificationType.values()) {
            assertTrue(type instanceof NotificationType);
        }
    }

    @Test
    @DisplayName("Should handle display name formatting consistently")
    void testDisplayNameFormatting() {
        for (NotificationType type : NotificationType.values()) {
            String displayName = type.getDisplayName();
            
            // Display names should start with capital letter
            assertTrue(Character.isUpperCase(displayName.charAt(0)));
            
            // Display names should not be empty or just whitespace
            assertTrue(!displayName.trim().isEmpty());
            
            // Display names should not contain underscores (should be human-readable)
            assertTrue(!displayName.contains("_"));
        }
    }

    /**
     * Helper method to check if an array contains a specific value
     */
    private boolean containsValue(NotificationType[] values, NotificationType target) {
        for (NotificationType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}