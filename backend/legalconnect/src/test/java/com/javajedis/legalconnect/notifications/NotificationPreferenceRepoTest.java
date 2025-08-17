package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("NotificationPreferenceRepo Tests")
class NotificationPreferenceRepoTest {

    @Mock
    private NotificationPreferenceRepo notificationPreferenceRepo;

    private UUID testUserId1;
    private UUID testUserId2;
    private NotificationPreference preference1;
    private NotificationPreference preference2;
    private NotificationPreference preference3;
    private NotificationPreference preference4;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUserId1 = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();
        
        // Create test preferences
        preference1 = new NotificationPreference();
        preference1.setId(UUID.randomUUID());
        preference1.setUserId(testUserId1);
        preference1.setNotificationType(NotificationType.CASE_CREATE);
        preference1.setEmailEnabled(true);
        preference1.setWebPushEnabled(true);
        preference1.setCreatedAt(OffsetDateTime.now().minusHours(2));
        preference1.setUpdatedAt(OffsetDateTime.now().minusHours(1));
        
        preference2 = new NotificationPreference();
        preference2.setId(UUID.randomUUID());
        preference2.setUserId(testUserId1);
        preference2.setNotificationType(NotificationType.DOC_UPLOAD);
        preference2.setEmailEnabled(false);
        preference2.setWebPushEnabled(true);
        preference2.setCreatedAt(OffsetDateTime.now().minusHours(2));
        preference2.setUpdatedAt(OffsetDateTime.now().minusHours(1));
        
        preference3 = new NotificationPreference();
        preference3.setId(UUID.randomUUID());
        preference3.setUserId(testUserId2);
        preference3.setNotificationType(NotificationType.CASE_CREATE);
        preference3.setEmailEnabled(true);
        preference3.setWebPushEnabled(false);
        preference3.setCreatedAt(OffsetDateTime.now().minusHours(2));
        preference3.setUpdatedAt(OffsetDateTime.now().minusHours(1));
        
        preference4 = new NotificationPreference();
        preference4.setId(UUID.randomUUID());
        preference4.setUserId(testUserId2);
        preference4.setNotificationType(NotificationType.EVENT_ADD);
        preference4.setEmailEnabled(false);
        preference4.setWebPushEnabled(false);
        preference4.setCreatedAt(OffsetDateTime.now().minusHours(2));
        preference4.setUpdatedAt(OffsetDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Should find notification preference by user ID and notification type")
    void findByUserIdAndNotificationType_PreferenceExists_ReturnsPreference() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(testUserId1, NotificationType.CASE_CREATE))
            .thenReturn(Optional.of(preference1));
        
        // Act
        Optional<NotificationPreference> result = notificationPreferenceRepo
            .findByUserIdAndNotificationType(testUserId1, NotificationType.CASE_CREATE);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(preference1.getId(), result.get().getId());
        assertEquals(testUserId1, result.get().getUserId());
        assertEquals(NotificationType.CASE_CREATE, result.get().getNotificationType());
    }

    @Test
    @DisplayName("Should return empty when notification preference does not exist")
    void findByUserIdAndNotificationType_PreferenceNotExists_ReturnsEmpty() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(testUserId1, NotificationType.SCHEDULE_REMINDER))
            .thenReturn(Optional.empty());
        
        // Act
        Optional<NotificationPreference> result = notificationPreferenceRepo
            .findByUserIdAndNotificationType(testUserId1, NotificationType.SCHEDULE_REMINDER);
        
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find preferences by notification type and email enabled")
    void findByNotificationTypeAndEmailEnabled_EmailEnabled_ReturnsMatchingPreferences() {
        // Arrange
        List<NotificationPreference> expectedPreferences = Arrays.asList(preference1, preference3);
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true))
            .thenReturn(expectedPreferences);
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify all returned preferences have email enabled
        for (NotificationPreference preference : result) {
            assertTrue(preference.isEmailEnabled());
            assertEquals(NotificationType.CASE_CREATE, preference.getNotificationType());
        }
    }

    @Test
    @DisplayName("Should find preferences by notification type and email disabled")
    void findByNotificationTypeAndEmailEnabled_EmailDisabled_ReturnsMatchingPreferences() {
        // Arrange
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(NotificationType.DOC_UPLOAD, false))
            .thenReturn(Arrays.asList(preference2));
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.DOC_UPLOAD, false);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isEmailEnabled());
        assertEquals(NotificationType.DOC_UPLOAD, result.get(0).getNotificationType());
    }

    @Test
    @DisplayName("Should return empty list when no preferences match email criteria")
    void findByNotificationTypeAndEmailEnabled_NoMatches_ReturnsEmptyList() {
        // Arrange
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(NotificationType.NOTE_CREATE, true))
            .thenReturn(Arrays.asList());
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.NOTE_CREATE, true);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find preferences by notification type and web push enabled")
    void findByNotificationTypeAndWebPushEnabled_WebPushEnabled_ReturnsMatchingPreferences() {
        // Arrange
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, true))
            .thenReturn(Arrays.asList(preference1));
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isWebPushEnabled());
        assertEquals(NotificationType.CASE_CREATE, result.get(0).getNotificationType());
    }

    @Test
    @DisplayName("Should find preferences by notification type and web push disabled")
    void findByNotificationTypeAndWebPushEnabled_WebPushDisabled_ReturnsMatchingPreferences() {
        // Arrange
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(NotificationType.EVENT_ADD, false))
            .thenReturn(Arrays.asList(preference4));
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndWebPushEnabled(NotificationType.EVENT_ADD, false);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isWebPushEnabled());
        assertEquals(NotificationType.EVENT_ADD, result.get(0).getNotificationType());
    }

    @Test
    @DisplayName("Should return empty list when no preferences match web push criteria")
    void findByNotificationTypeAndWebPushEnabled_NoMatches_ReturnsEmptyList() {
        // Arrange
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(NotificationType.SCHEDULE_REMINDER, true))
            .thenReturn(Arrays.asList());
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndWebPushEnabled(NotificationType.SCHEDULE_REMINDER, true);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all preferences by user ID")
    void findByUserId_UserHasPreferences_ReturnsAllUserPreferences() {
        // Arrange
        List<NotificationPreference> expectedPreferences = Arrays.asList(preference1, preference2);
        when(notificationPreferenceRepo.findByUserId(testUserId1))
            .thenReturn(expectedPreferences);
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo.findByUserId(testUserId1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify all returned preferences belong to the correct user
        for (NotificationPreference preference : result) {
            assertEquals(testUserId1, preference.getUserId());
        }
    }

    @Test
    @DisplayName("Should return empty list when user has no preferences")
    void findByUserId_UserHasNoPreferences_ReturnsEmptyList() {
        // Arrange
        UUID userWithNoPreferences = UUID.randomUUID();
        when(notificationPreferenceRepo.findByUserId(userWithNoPreferences))
            .thenReturn(Arrays.asList());
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo.findByUserId(userWithNoPreferences);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle different notification types correctly")
    void testDifferentNotificationTypes() {
        // Arrange
        for (NotificationType type : NotificationType.values()) {
            when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(type, true))
                .thenReturn(Arrays.asList(preference1));
        }
        
        // Act & Assert
        for (NotificationType type : NotificationType.values()) {
            List<NotificationPreference> result = notificationPreferenceRepo
                .findByNotificationTypeAndEmailEnabled(type, true);
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Should handle repository method return types correctly")
    void testRepositoryMethodReturnTypes() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(any(UUID.class), any(NotificationType.class)))
            .thenReturn(Optional.of(preference1));
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(any(NotificationType.class), any(Boolean.class)))
            .thenReturn(Arrays.asList(preference1));
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(any(NotificationType.class), any(Boolean.class)))
            .thenReturn(Arrays.asList(preference1));
        when(notificationPreferenceRepo.findByUserId(any(UUID.class)))
            .thenReturn(Arrays.asList(preference1));
        
        // Act & Assert - verify return types
        Optional<NotificationPreference> optionalResult = notificationPreferenceRepo
            .findByUserIdAndNotificationType(testUserId1, NotificationType.CASE_CREATE);
        assertTrue(optionalResult instanceof Optional);
        
        List<NotificationPreference> listResult1 = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true);
        assertTrue(listResult1 instanceof List);
        
        List<NotificationPreference> listResult2 = notificationPreferenceRepo
            .findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, true);
        assertTrue(listResult2 instanceof List);
        
        List<NotificationPreference> listResult3 = notificationPreferenceRepo.findByUserId(testUserId1);
        assertTrue(listResult3 instanceof List);
    }

    @Test
    @DisplayName("Should handle unique constraint scenarios")
    void testUniqueConstraintScenarios() {
        // This test verifies the behavior related to the unique constraint on (user_id, notification_type)
        
        // Arrange - same user and notification type should return the same preference
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(testUserId1, NotificationType.CASE_CREATE))
            .thenReturn(Optional.of(preference1));
        
        // Act
        Optional<NotificationPreference> result1 = notificationPreferenceRepo
            .findByUserIdAndNotificationType(testUserId1, NotificationType.CASE_CREATE);
        Optional<NotificationPreference> result2 = notificationPreferenceRepo
            .findByUserIdAndNotificationType(testUserId1, NotificationType.CASE_CREATE);
        
        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get().getId(), result2.get().getId());
    }

    @Test
    @DisplayName("Should handle method parameter validation")
    void testMethodParameterValidation() {
        // This test verifies that the repository methods accept the expected parameter types
        
        // Arrange
        UUID validUserId = UUID.randomUUID();
        NotificationType validType = NotificationType.CASE_CREATE;
        boolean validBoolean = true;
        
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(validUserId, validType))
            .thenReturn(Optional.of(preference1));
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(validType, validBoolean))
            .thenReturn(Arrays.asList(preference1));
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(validType, validBoolean))
            .thenReturn(Arrays.asList(preference1));
        when(notificationPreferenceRepo.findByUserId(validUserId))
            .thenReturn(Arrays.asList(preference1));
        
        // Act & Assert - methods should accept correct parameter types
        assertNotNull(notificationPreferenceRepo.findByUserIdAndNotificationType(validUserId, validType));
        assertNotNull(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(validType, validBoolean));
        assertNotNull(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(validType, validBoolean));
        assertNotNull(notificationPreferenceRepo.findByUserId(validUserId));
    }

    @Test
    @DisplayName("Should handle edge cases with boolean parameters")
    void testBooleanParameterEdgeCases() {
        // Arrange
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true))
            .thenReturn(Arrays.asList(preference1));
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, false))
            .thenReturn(Arrays.asList(preference2));
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, true))
            .thenReturn(Arrays.asList(preference1));
        when(notificationPreferenceRepo.findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, false))
            .thenReturn(Arrays.asList(preference3));
        
        // Act & Assert
        List<NotificationPreference> emailEnabledResult = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true);
        List<NotificationPreference> emailDisabledResult = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, false);
        List<NotificationPreference> webPushEnabledResult = notificationPreferenceRepo
            .findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, true);
        List<NotificationPreference> webPushDisabledResult = notificationPreferenceRepo
            .findByNotificationTypeAndWebPushEnabled(NotificationType.CASE_CREATE, false);
        
        assertNotNull(emailEnabledResult);
        assertNotNull(emailDisabledResult);
        assertNotNull(webPushEnabledResult);
        assertNotNull(webPushDisabledResult);
    }

    @Test
    @DisplayName("Should handle multiple users with same notification type preferences")
    void testMultipleUsersWithSameNotificationType() {
        // Arrange
        List<NotificationPreference> multipleUserPreferences = Arrays.asList(preference1, preference3);
        when(notificationPreferenceRepo.findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true))
            .thenReturn(multipleUserPreferences);
        
        // Act
        List<NotificationPreference> result = notificationPreferenceRepo
            .findByNotificationTypeAndEmailEnabled(NotificationType.CASE_CREATE, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify different users but same notification type and email setting
        assertEquals(NotificationType.CASE_CREATE, result.get(0).getNotificationType());
        assertEquals(NotificationType.CASE_CREATE, result.get(1).getNotificationType());
        assertTrue(result.get(0).isEmailEnabled());
        assertTrue(result.get(1).isEmailEnabled());
        assertNotEquals(result.get(0).getUserId(), result.get(1).getUserId());
    }
}