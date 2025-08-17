package com.javajedis.legalconnect.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.notifications.dto.NotificationPreferenceListResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationPreferenceResponseDTO;
import com.javajedis.legalconnect.notifications.dto.UpdateNotificationPreferenceDTO;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPreferenceService Tests")
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepo notificationPreferenceRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private NotificationPreferenceService notificationPreferenceService;

    private User testUser;
    private NotificationPreference testPreference1;
    private NotificationPreference testPreference2;
    private UpdateNotificationPreferenceDTO updateDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());

        // Setup test preferences
        testPreference1 = new NotificationPreference();
        testPreference1.setId(UUID.randomUUID());
        testPreference1.setUserId(testUser.getId());
        testPreference1.setNotificationType(NotificationType.CASE_CREATE);
        testPreference1.setEmailEnabled(true);
        testPreference1.setWebPushEnabled(true);
        testPreference1.setCreatedAt(OffsetDateTime.now());
        testPreference1.setUpdatedAt(OffsetDateTime.now());

        testPreference2 = new NotificationPreference();
        testPreference2.setId(UUID.randomUUID());
        testPreference2.setUserId(testUser.getId());
        testPreference2.setNotificationType(NotificationType.EVENT_ADD);
        testPreference2.setEmailEnabled(false);
        testPreference2.setWebPushEnabled(true);
        testPreference2.setCreatedAt(OffsetDateTime.now());
        testPreference2.setUpdatedAt(OffsetDateTime.now());

        // Setup update DTO
        updateDTO = new UpdateNotificationPreferenceDTO();
        updateDTO.setEmailEnabled(false);
        updateDTO.setWebPushEnabled(true);
    }

    @Test
    @DisplayName("Should get all preferences successfully for authenticated user")
    void getAllPreferences_Success() {
        // Arrange
        List<NotificationPreference> preferences = Arrays.asList(testPreference1, testPreference2);
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(notificationPreferenceRepo.findByUserId(testUser.getId()))
                    .thenReturn(preferences);

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceListResponseDTO>> response = 
                    notificationPreferenceService.getAllPreferences();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Notification preferences retrieved successfully", response.getBody().getMessage());
            
            NotificationPreferenceListResponseDTO responseData = response.getBody().getData();
            assertNotNull(responseData);
            assertEquals(2, responseData.getPreferences().size());
            
            // Verify first preference
            NotificationPreferenceResponseDTO pref1 = responseData.getPreferences().get(0);
            assertEquals(NotificationType.CASE_CREATE, pref1.getType());
            assertEquals("Case Creation", pref1.getDisplayName());
            assertTrue(pref1.isEmailEnabled());
            assertTrue(pref1.isWebPushEnabled());
            
            // Verify second preference
            NotificationPreferenceResponseDTO pref2 = responseData.getPreferences().get(1);
            assertEquals(NotificationType.EVENT_ADD, pref2.getType());
            assertEquals("Event Addition", pref2.getDisplayName());
            assertFalse(pref2.isEmailEnabled());
            assertTrue(pref2.isWebPushEnabled());

            verify(notificationPreferenceRepo).findByUserId(testUser.getId());
        }
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated for getAllPreferences")
    void getAllPreferences_Unauthorized() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceListResponseDTO>> response = 
                    notificationPreferenceService.getAllPreferences();

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("User is not authenticated", response.getBody().getError().getMessage());
            
            verify(notificationPreferenceRepo, never()).findByUserId(any());
        }
    }

    @Test
    @DisplayName("Should return empty list when user has no preferences")
    void getAllPreferences_EmptyList() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(notificationPreferenceRepo.findByUserId(testUser.getId()))
                    .thenReturn(Arrays.asList());

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceListResponseDTO>> response = 
                    notificationPreferenceService.getAllPreferences();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Notification preferences retrieved successfully", response.getBody().getMessage());
            
            NotificationPreferenceListResponseDTO responseData = response.getBody().getData();
            assertNotNull(responseData);
            assertTrue(responseData.getPreferences().isEmpty());

            verify(notificationPreferenceRepo).findByUserId(testUser.getId());
        }
    }

    @Test
    @DisplayName("Should update notification preference successfully")
    void updateNotificationPref_Success() {
        // Arrange
        NotificationPreference updatedPreference = new NotificationPreference();
        updatedPreference.setId(testPreference1.getId());
        updatedPreference.setUserId(testUser.getId());
        updatedPreference.setNotificationType(NotificationType.CASE_CREATE);
        updatedPreference.setEmailEnabled(false);
        updatedPreference.setWebPushEnabled(true);
        updatedPreference.setCreatedAt(testPreference1.getCreatedAt());
        updatedPreference.setUpdatedAt(OffsetDateTime.now());

        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                    testUser.getId(), NotificationType.CASE_CREATE))
                    .thenReturn(Optional.of(testPreference1));
            when(notificationPreferenceRepo.save(any(NotificationPreference.class)))
                    .thenReturn(updatedPreference);

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> response = 
                    notificationPreferenceService.updateNotificationPref(NotificationType.CASE_CREATE, updateDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Notification preference updated successfully", response.getBody().getMessage());
            
            NotificationPreferenceResponseDTO responseData = response.getBody().getData();
            assertNotNull(responseData);
            assertEquals(NotificationType.CASE_CREATE, responseData.getType());
            assertEquals("Case Creation", responseData.getDisplayName());
            assertFalse(responseData.isEmailEnabled());
            assertTrue(responseData.isWebPushEnabled());

            verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                    testUser.getId(), NotificationType.CASE_CREATE);
            verify(notificationPreferenceRepo).save(any(NotificationPreference.class));
        }
    }

    @Test
    @DisplayName("Should return unauthorized when user is not authenticated for updateNotificationPref")
    void updateNotificationPref_Unauthorized() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(null);

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> response = 
                    notificationPreferenceService.updateNotificationPref(NotificationType.CASE_CREATE, updateDTO);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("User is not authenticated", response.getBody().getError().getMessage());
            
            verify(notificationPreferenceRepo, never()).findByUserIdAndNotificationType(any(), any());
            verify(notificationPreferenceRepo, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should return not found when preference does not exist for updateNotificationPref")
    void updateNotificationPref_PreferenceNotFound() {
        // Arrange
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                    testUser.getId(), NotificationType.CASE_CREATE))
                    .thenReturn(Optional.empty());

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> response = 
                    notificationPreferenceService.updateNotificationPref(NotificationType.CASE_CREATE, updateDTO);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Notification preference not found", response.getBody().getError().getMessage());
            
            verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                    testUser.getId(), NotificationType.CASE_CREATE);
            verify(notificationPreferenceRepo, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should check email enabled status successfully when preference exists")
    void checkEmailEnabled_PreferenceExists_True() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE))
                .thenReturn(Optional.of(testPreference1));

        // Act
        boolean result = notificationPreferenceService.checkEmailEnabled(
                testUser.getId(), NotificationType.CASE_CREATE);

        // Assert
        assertTrue(result);
        verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should check email enabled status successfully when preference exists and is disabled")
    void checkEmailEnabled_PreferenceExists_False() {
        // Arrange
        testPreference1.setEmailEnabled(false);
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE))
                .thenReturn(Optional.of(testPreference1));

        // Act
        boolean result = notificationPreferenceService.checkEmailEnabled(
                testUser.getId(), NotificationType.CASE_CREATE);

        // Assert
        assertFalse(result);
        verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should return true by default when preference does not exist for checkEmailEnabled")
    void checkEmailEnabled_PreferenceNotExists_DefaultTrue() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE))
                .thenReturn(Optional.empty());

        // Act
        boolean result = notificationPreferenceService.checkEmailEnabled(
                testUser.getId(), NotificationType.CASE_CREATE);

        // Assert
        assertTrue(result);
        verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should check web push enabled status successfully when preference exists")
    void checkWebPushEnabled_PreferenceExists_True() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE))
                .thenReturn(Optional.of(testPreference1));

        // Act
        boolean result = notificationPreferenceService.checkWebPushEnabled(
                testUser.getId(), NotificationType.CASE_CREATE);

        // Assert
        assertTrue(result);
        verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should check web push enabled status successfully when preference exists and is disabled")
    void checkWebPushEnabled_PreferenceExists_False() {
        // Arrange
        testPreference1.setWebPushEnabled(false);
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE))
                .thenReturn(Optional.of(testPreference1));

        // Act
        boolean result = notificationPreferenceService.checkWebPushEnabled(
                testUser.getId(), NotificationType.CASE_CREATE);

        // Assert
        assertFalse(result);
        verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should return true by default when preference does not exist for checkWebPushEnabled")
    void checkWebPushEnabled_PreferenceNotExists_DefaultTrue() {
        // Arrange
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE))
                .thenReturn(Optional.empty());

        // Act
        boolean result = notificationPreferenceService.checkWebPushEnabled(
                testUser.getId(), NotificationType.CASE_CREATE);

        // Assert
        assertTrue(result);
        verify(notificationPreferenceRepo).findByUserIdAndNotificationType(
                testUser.getId(), NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should handle multiple notification types correctly")
    void checkPreferences_MultipleTypes() {
        // Arrange
        UUID userId = testUser.getId();
        
        // Setup different preferences for different types
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                userId, NotificationType.CASE_CREATE))
                .thenReturn(Optional.of(testPreference1)); // email=true, webPush=true
        
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                userId, NotificationType.EVENT_ADD))
                .thenReturn(Optional.of(testPreference2)); // email=false, webPush=true
        
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                userId, NotificationType.DOC_UPLOAD))
                .thenReturn(Optional.empty()); // defaults to true

        // Act & Assert
        assertTrue(notificationPreferenceService.checkEmailEnabled(userId, NotificationType.CASE_CREATE));
        assertTrue(notificationPreferenceService.checkWebPushEnabled(userId, NotificationType.CASE_CREATE));
        
        assertFalse(notificationPreferenceService.checkEmailEnabled(userId, NotificationType.EVENT_ADD));
        assertTrue(notificationPreferenceService.checkWebPushEnabled(userId, NotificationType.EVENT_ADD));
        
        assertTrue(notificationPreferenceService.checkEmailEnabled(userId, NotificationType.DOC_UPLOAD));
        assertTrue(notificationPreferenceService.checkWebPushEnabled(userId, NotificationType.DOC_UPLOAD));

        // Verify repository calls - each type is called twice (once for email, once for webPush)
        verify(notificationPreferenceRepo, times(2)).findByUserIdAndNotificationType(userId, NotificationType.CASE_CREATE);
        verify(notificationPreferenceRepo, times(2)).findByUserIdAndNotificationType(userId, NotificationType.EVENT_ADD);
        verify(notificationPreferenceRepo, times(2)).findByUserIdAndNotificationType(userId, NotificationType.DOC_UPLOAD);
    }

    @Test
    @DisplayName("Should update preference with different values correctly")
    void updateNotificationPref_DifferentValues() {
        // Arrange
        UpdateNotificationPreferenceDTO customUpdateDTO = new UpdateNotificationPreferenceDTO();
        customUpdateDTO.setEmailEnabled(true);
        customUpdateDTO.setWebPushEnabled(false);

        NotificationPreference updatedPreference = new NotificationPreference();
        updatedPreference.setId(testPreference1.getId());
        updatedPreference.setUserId(testUser.getId());
        updatedPreference.setNotificationType(NotificationType.CASE_CREATE);
        updatedPreference.setEmailEnabled(true);
        updatedPreference.setWebPushEnabled(false);
        updatedPreference.setCreatedAt(testPreference1.getCreatedAt());
        updatedPreference.setUpdatedAt(OffsetDateTime.now());

        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo))
                    .thenReturn(testUser);
            when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                    testUser.getId(), NotificationType.CASE_CREATE))
                    .thenReturn(Optional.of(testPreference1));
            when(notificationPreferenceRepo.save(any(NotificationPreference.class)))
                    .thenReturn(updatedPreference);

            // Act
            ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> response = 
                    notificationPreferenceService.updateNotificationPref(NotificationType.CASE_CREATE, customUpdateDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            NotificationPreferenceResponseDTO responseData = response.getBody().getData();
            assertNotNull(responseData);
            assertTrue(responseData.isEmailEnabled());
            assertFalse(responseData.isWebPushEnabled());

            verify(notificationPreferenceRepo).save(any(NotificationPreference.class));
        }
    }

    @Test
    @DisplayName("Should handle null user ID gracefully in check methods")
    void checkPreferences_NullUserId() {
        // Arrange
        UUID nullUserId = null;
        
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(
                eq(nullUserId), any(NotificationType.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(notificationPreferenceService.checkEmailEnabled(nullUserId, NotificationType.CASE_CREATE));
        assertTrue(notificationPreferenceService.checkWebPushEnabled(nullUserId, NotificationType.CASE_CREATE));

        verify(notificationPreferenceRepo, times(2)).findByUserIdAndNotificationType(nullUserId, NotificationType.CASE_CREATE);
    }

    @Test
    @DisplayName("Should test all notification types in check methods")
    void checkPreferences_AllNotificationTypes() {
        // Arrange
        UUID userId = testUser.getId();
        
        // Mock empty responses for all types (testing default behavior)
        for (NotificationType type : NotificationType.values()) {
            when(notificationPreferenceRepo.findByUserIdAndNotificationType(userId, type))
                    .thenReturn(Optional.empty());
        }

        // Act & Assert - all should default to true
        for (NotificationType type : NotificationType.values()) {
            assertTrue(notificationPreferenceService.checkEmailEnabled(userId, type),
                    "Email should be enabled by default for " + type);
            assertTrue(notificationPreferenceService.checkWebPushEnabled(userId, type),
                    "Web push should be enabled by default for " + type);
        }

        // Verify all types were checked
        for (NotificationType type : NotificationType.values()) {
            verify(notificationPreferenceRepo, times(2)).findByUserIdAndNotificationType(userId, type);
        }
    }

    @Test
    @DisplayName("initializeDefaultPreferencesForUser: creates all defaults when none exist")
    void initializeDefaults_CreatesAllWhenNoneExist() {
        UUID userId = testUser.getId();

        for (NotificationType type : NotificationType.values()) {
            when(notificationPreferenceRepo.findByUserIdAndNotificationType(userId, type))
                    .thenReturn(Optional.empty());
        }

        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);

        notificationPreferenceService.initializeDefaultPreferencesForUser(userId);

        verify(notificationPreferenceRepo, times(NotificationType.values().length)).save(captor.capture());

        var saved = captor.getAllValues();
        assertEquals(NotificationType.values().length, saved.size());

        Set<NotificationType> savedTypes = saved.stream()
                .map(NotificationPreference::getNotificationType)
                .collect(Collectors.toSet());

        assertEquals(EnumSet.allOf(NotificationType.class), savedTypes);
        saved.forEach(p -> {
            assertEquals(userId, p.getUserId());
            assertTrue(p.isEmailEnabled());
            assertTrue(p.isWebPushEnabled());
        });
    }

    @Test
    @DisplayName("initializeDefaultPreferencesForUser: skips existing and creates only missing")
    void initializeDefaults_SkipsExistingCreatesMissing() {
        UUID userId = testUser.getId();
        // Pretend preferences already exist for CASE_CREATE and EVENT_ADD
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(userId, NotificationType.CASE_CREATE))
                .thenReturn(Optional.of(new NotificationPreference()));
        when(notificationPreferenceRepo.findByUserIdAndNotificationType(userId, NotificationType.EVENT_ADD))
                .thenReturn(Optional.of(new NotificationPreference()));

        // Remaining types return empty
        for (NotificationType type : EnumSet.complementOf(EnumSet.of(
                NotificationType.CASE_CREATE,
                NotificationType.EVENT_ADD))) {
            when(notificationPreferenceRepo.findByUserIdAndNotificationType(userId, type))
                    .thenReturn(Optional.empty());
        }

        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);

        notificationPreferenceService.initializeDefaultPreferencesForUser(userId);

        int expectedCreates = NotificationType.values().length - 2;
        verify(notificationPreferenceRepo, times(expectedCreates)).save(captor.capture());

        var saved = captor.getAllValues();
        assertEquals(expectedCreates, saved.size());

        Set<NotificationType> expectedMissing = EnumSet.complementOf(EnumSet.of(
                NotificationType.CASE_CREATE,
                NotificationType.EVENT_ADD));
        Set<NotificationType> savedTypes = saved.stream()
                .map(NotificationPreference::getNotificationType)
                .collect(Collectors.toSet());

        assertEquals(expectedMissing, savedTypes);
        saved.forEach(p -> {
            assertEquals(userId, p.getUserId());
            assertTrue(p.isEmailEnabled());
            assertTrue(p.isWebPushEnabled());
        });
    }
}