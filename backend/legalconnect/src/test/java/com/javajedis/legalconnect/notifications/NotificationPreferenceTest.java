package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationPreference Entity Tests")
class NotificationPreferenceTest {

    private NotificationPreference notificationPreference;
    private UUID testPreferenceId;
    private UUID testUserId;
    private NotificationType testNotificationType;
    private OffsetDateTime testCreatedAt;
    private OffsetDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        testPreferenceId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testNotificationType = NotificationType.CASE_CREATE;
        testCreatedAt = OffsetDateTime.now().minusHours(1);
        testUpdatedAt = OffsetDateTime.now();

        notificationPreference = new NotificationPreference();
        notificationPreference.setId(testPreferenceId);
        notificationPreference.setUserId(testUserId);
        notificationPreference.setNotificationType(testNotificationType);
        notificationPreference.setEmailEnabled(true);
        notificationPreference.setWebPushEnabled(true);
        notificationPreference.setCreatedAt(testCreatedAt);
        notificationPreference.setUpdatedAt(testUpdatedAt);
    }

    @Test
    @DisplayName("Should create notification preference with default constructor")
    void testDefaultConstructor() {
        NotificationPreference defaultPreference = new NotificationPreference();

        assertNotNull(defaultPreference);
        assertNull(defaultPreference.getId());
        assertNull(defaultPreference.getUserId());
        assertNull(defaultPreference.getNotificationType());
        assertTrue(defaultPreference.isEmailEnabled()); // Default value should be true
        assertTrue(defaultPreference.isWebPushEnabled()); // Default value should be true
        assertNull(defaultPreference.getCreatedAt());
        assertNull(defaultPreference.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create notification preference with all args constructor")
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime created = now.minusMinutes(30);
        NotificationPreference constructedPreference = new NotificationPreference(
            testPreferenceId, testUserId, NotificationType.EVENT_ADD, false, false, created, now
        );

        assertEquals(testPreferenceId, constructedPreference.getId());
        assertEquals(testUserId, constructedPreference.getUserId());
        assertEquals(NotificationType.EVENT_ADD, constructedPreference.getNotificationType());
        assertFalse(constructedPreference.isEmailEnabled());
        assertFalse(constructedPreference.isWebPushEnabled());
        assertEquals(created, constructedPreference.getCreatedAt());
        assertEquals(now, constructedPreference.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle id getter and setter")
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        notificationPreference.setId(newId);
        assertEquals(newId, notificationPreference.getId());
    }

    @Test
    @DisplayName("Should handle userId getter and setter")
    void testUserIdGetterAndSetter() {
        UUID newUserId = UUID.randomUUID();
        notificationPreference.setUserId(newUserId);
        assertEquals(newUserId, notificationPreference.getUserId());
    }

    @Test
    @DisplayName("Should handle notificationType getter and setter")
    void testNotificationTypeGetterAndSetter() {
        NotificationType newType = NotificationType.DOC_UPLOAD;
        notificationPreference.setNotificationType(newType);
        assertEquals(newType, notificationPreference.getNotificationType());
    }

    @Test
    @DisplayName("Should handle emailEnabled getter and setter")
    void testEmailEnabledGetterAndSetter() {
        // Test setting to false
        notificationPreference.setEmailEnabled(false);
        assertFalse(notificationPreference.isEmailEnabled());

        // Test setting to true
        notificationPreference.setEmailEnabled(true);
        assertTrue(notificationPreference.isEmailEnabled());
    }

    @Test
    @DisplayName("Should handle webPushEnabled getter and setter")
    void testWebPushEnabledGetterAndSetter() {
        // Test setting to false
        notificationPreference.setWebPushEnabled(false);
        assertFalse(notificationPreference.isWebPushEnabled());

        // Test setting to true
        notificationPreference.setWebPushEnabled(true);
        assertTrue(notificationPreference.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        notificationPreference.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, notificationPreference.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle updatedAt getter and setter")
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now().plusMinutes(30);
        notificationPreference.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, notificationPreference.getUpdatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        NotificationPreference preference1 = new NotificationPreference();
        preference1.setId(testPreferenceId);
        preference1.setUserId(testUserId);
        preference1.setNotificationType(NotificationType.CASE_CREATE);
        preference1.setEmailEnabled(true);
        preference1.setWebPushEnabled(false);

        NotificationPreference preference2 = new NotificationPreference();
        preference2.setId(testPreferenceId);
        preference2.setUserId(testUserId);
        preference2.setNotificationType(NotificationType.CASE_CREATE);
        preference2.setEmailEnabled(true);
        preference2.setWebPushEnabled(false);

        NotificationPreference preference3 = new NotificationPreference();
        preference3.setId(UUID.randomUUID());
        preference3.setUserId(UUID.randomUUID());
        preference3.setNotificationType(NotificationType.DOC_UPLOAD);
        preference3.setEmailEnabled(false);
        preference3.setWebPushEnabled(true);

        assertEquals(preference1, preference2);
        assertEquals(preference1.hashCode(), preference2.hashCode());
        assertNotEquals(preference1, preference3);
        assertNotEquals(preference1.hashCode(), preference3.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with null values")
    void testEqualsWithNullValues() {
        NotificationPreference preference1 = new NotificationPreference();
        NotificationPreference preference2 = new NotificationPreference();

        assertEquals(preference1, preference2);
        assertEquals(preference1.hashCode(), preference2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        String toString = notificationPreference.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("CASE_CREATE"));
        assertTrue(toString.contains("true")); // emailEnabled and webPushEnabled values
        assertTrue(toString.contains(testUserId.toString()));
    }

    @Test
    @DisplayName("Should handle default boolean values")
    void testDefaultBooleanValues() {
        NotificationPreference newPreference = new NotificationPreference();
        assertTrue(newPreference.isEmailEnabled());
        assertTrue(newPreference.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle all notification types")
    void testAllNotificationTypes() {
        NotificationType[] allTypes = NotificationType.values();
        
        for (NotificationType type : allTypes) {
            notificationPreference.setNotificationType(type);
            assertEquals(type, notificationPreference.getNotificationType());
        }
    }

    @Test
    @DisplayName("Should handle preference combinations")
    void testPreferenceCombinations() {
        // Test all combinations of email and web push preferences
        boolean[] booleanValues = {true, false};
        
        for (boolean emailEnabled : booleanValues) {
            for (boolean webPushEnabled : booleanValues) {
                notificationPreference.setEmailEnabled(emailEnabled);
                notificationPreference.setWebPushEnabled(webPushEnabled);
                
                assertEquals(emailEnabled, notificationPreference.isEmailEnabled());
                assertEquals(webPushEnabled, notificationPreference.isWebPushEnabled());
            }
        }
    }

    @Test
    @DisplayName("Should handle notification preference with minimal data")
    void testNotificationPreferenceWithMinimalData() {
        NotificationPreference minimalPreference = new NotificationPreference();
        minimalPreference.setUserId(testUserId);
        minimalPreference.setNotificationType(NotificationType.NOTE_CREATE);

        assertEquals(testUserId, minimalPreference.getUserId());
        assertEquals(NotificationType.NOTE_CREATE, minimalPreference.getNotificationType());
        assertTrue(minimalPreference.isEmailEnabled()); // Default value
        assertTrue(minimalPreference.isWebPushEnabled()); // Default value
        assertNull(minimalPreference.getId()); // Not set
        assertNull(minimalPreference.getCreatedAt()); // Not set
        assertNull(minimalPreference.getUpdatedAt()); // Not set
    }

    @Test
    @DisplayName("Should handle notification preference entity fields validation")
    void testNotificationPreferenceEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(notificationPreference.getId());
        assertNotNull(notificationPreference.getUserId());
        assertNotNull(notificationPreference.getNotificationType());
        assertNotNull(notificationPreference.getCreatedAt());
        assertNotNull(notificationPreference.getUpdatedAt());
        // boolean fields are primitive, so they're never null
    }

    @Test
    @DisplayName("Should handle notification preference timestamps")
    void testNotificationPreferenceWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(2);
        OffsetDateTime future = now.plusHours(1);

        // Test with past created time and current updated time
        notificationPreference.setCreatedAt(past);
        notificationPreference.setUpdatedAt(now);
        assertEquals(past, notificationPreference.getCreatedAt());
        assertEquals(now, notificationPreference.getUpdatedAt());

        // Test with future updated time
        notificationPreference.setUpdatedAt(future);
        assertEquals(future, notificationPreference.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle notification preference equality with different settings")
    void testNotificationPreferenceEqualityWithDifferentSettings() {
        NotificationPreference preference1 = new NotificationPreference();
        preference1.setId(testPreferenceId);
        preference1.setUserId(testUserId);
        preference1.setNotificationType(NotificationType.CASE_CREATE);
        preference1.setEmailEnabled(true);
        preference1.setWebPushEnabled(false);

        NotificationPreference preference2 = new NotificationPreference();
        preference2.setId(testPreferenceId);
        preference2.setUserId(testUserId);
        preference2.setNotificationType(NotificationType.CASE_CREATE);
        preference2.setEmailEnabled(false); // Different email setting
        preference2.setWebPushEnabled(true); // Different web push setting

        // With Lombok @Data, objects with different field values are not equal
        // This tests the actual behavior of the Lombok-generated equals method
        assertNotEquals(preference1, preference2);
    }

    @Test
    @DisplayName("Should handle notification preference with null user ID")
    void testNotificationPreferenceWithNullUserId() {
        notificationPreference.setUserId(null);
        assertNull(notificationPreference.getUserId());
    }

    @Test
    @DisplayName("Should handle notification preference with null notification type")
    void testNotificationPreferenceWithNullNotificationType() {
        notificationPreference.setNotificationType(null);
        assertNull(notificationPreference.getNotificationType());
    }

    @Test
    @DisplayName("Should handle notification preference state transitions")
    void testNotificationPreferenceStateTransitions() {
        // Start with both enabled (default)
        assertTrue(notificationPreference.isEmailEnabled());
        assertTrue(notificationPreference.isWebPushEnabled());

        // Disable email, keep web push enabled
        notificationPreference.setEmailEnabled(false);
        assertFalse(notificationPreference.isEmailEnabled());
        assertTrue(notificationPreference.isWebPushEnabled());

        // Disable web push, enable email
        notificationPreference.setWebPushEnabled(false);
        notificationPreference.setEmailEnabled(true);
        assertTrue(notificationPreference.isEmailEnabled());
        assertFalse(notificationPreference.isWebPushEnabled());

        // Enable both again
        notificationPreference.setEmailEnabled(true);
        notificationPreference.setWebPushEnabled(true);
        assertTrue(notificationPreference.isEmailEnabled());
        assertTrue(notificationPreference.isWebPushEnabled());
    }

    @Test
    @DisplayName("Should handle notification preference type changes")
    void testNotificationPreferenceTypeChanges() {
        // Start with CASE_CREATE
        assertEquals(NotificationType.CASE_CREATE, notificationPreference.getNotificationType());

        // Change to EVENT_ADD
        notificationPreference.setNotificationType(NotificationType.EVENT_ADD);
        assertEquals(NotificationType.EVENT_ADD, notificationPreference.getNotificationType());

        // Change to SCHEDULE_REMINDER
        notificationPreference.setNotificationType(NotificationType.SCHEDULE_REMINDER);
        assertEquals(NotificationType.SCHEDULE_REMINDER, notificationPreference.getNotificationType());

        // Change to DOC_UPLOAD
        notificationPreference.setNotificationType(NotificationType.DOC_UPLOAD);
        assertEquals(NotificationType.DOC_UPLOAD, notificationPreference.getNotificationType());

        // Change to NOTE_CREATE
        notificationPreference.setNotificationType(NotificationType.NOTE_CREATE);
        assertEquals(NotificationType.NOTE_CREATE, notificationPreference.getNotificationType());
    }

    @Test
    @DisplayName("Should handle notification preference with same user different types")
    void testNotificationPreferenceWithSameUserDifferentTypes() {
        UUID sameUserId = UUID.randomUUID();
        
        NotificationPreference preference1 = new NotificationPreference();
        preference1.setUserId(sameUserId);
        preference1.setNotificationType(NotificationType.CASE_CREATE);
        
        NotificationPreference preference2 = new NotificationPreference();
        preference2.setUserId(sameUserId);
        preference2.setNotificationType(NotificationType.DOC_UPLOAD);
        
        assertEquals(sameUserId, preference1.getUserId());
        assertEquals(sameUserId, preference2.getUserId());
        assertNotEquals(preference1.getNotificationType(), preference2.getNotificationType());
    }
}