package com.javajedis.legalconnect.notifications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.common.utility.JWTFilter;
import com.javajedis.legalconnect.notifications.dto.NotificationListResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationPreferenceListResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationPreferenceResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.dto.SendNotificationDTO;
import com.javajedis.legalconnect.notifications.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.notifications.dto.UpdateNotificationPreferenceDTO;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@WebMvcTest(controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {EmailVerificationFilter.class, JWTFilter.class}))
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(NotificationControllerTest.TestConfig.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPreferenceService notificationPreferenceService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private SendNotificationDTO sendNotificationDTO;
    private NotificationResponseDTO notificationResponseDTO;
    private NotificationListResponseDTO notificationListResponseDTO;
    private UnreadCountResponseDTO unreadCountResponseDTO;
    private NotificationPreferenceListResponseDTO preferenceListResponseDTO;
    private NotificationPreferenceResponseDTO preferenceResponseDTO;
    private UpdateNotificationPreferenceDTO updatePreferenceDTO;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        public NotificationPreferenceService notificationPreferenceService() {
            return mock(NotificationPreferenceService.class);
        }

        @Bean
        public UserRepo userRepo() {
            return mock(UserRepo.class);
        }
    }

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

        // Setup DTOs
        sendNotificationDTO = new SendNotificationDTO();
        sendNotificationDTO.setReceiverId(UUID.randomUUID());
        sendNotificationDTO.setContent("Test notification content");

        notificationResponseDTO = new NotificationResponseDTO();
        notificationResponseDTO.setId(UUID.randomUUID());
        notificationResponseDTO.setContent("Test notification");
        notificationResponseDTO.setRead(false);
        notificationResponseDTO.setCreatedAt(OffsetDateTime.now());

        notificationListResponseDTO = new NotificationListResponseDTO();
        notificationListResponseDTO.setNotifications(Arrays.asList(notificationResponseDTO));

        unreadCountResponseDTO = new UnreadCountResponseDTO();
        unreadCountResponseDTO.setUnreadCount(5);

        preferenceResponseDTO = new NotificationPreferenceResponseDTO();
        preferenceResponseDTO.setType(NotificationType.CASE_CREATE);
        preferenceResponseDTO.setDisplayName("Case Creation");
        preferenceResponseDTO.setEmailEnabled(true);
        preferenceResponseDTO.setWebPushEnabled(true);

        preferenceListResponseDTO = new NotificationPreferenceListResponseDTO();
        preferenceListResponseDTO.setPreferences(Arrays.asList(preferenceResponseDTO));

        updatePreferenceDTO = new UpdateNotificationPreferenceDTO();
        updatePreferenceDTO.setEmailEnabled(true);
        updatePreferenceDTO.setWebPushEnabled(false);
    }

    @Test
    @DisplayName("Should send notification successfully")
    void sendNotification_Success() throws Exception {
        ResponseEntity<ApiResponse<NotificationResponseDTO>> responseEntity = 
            ApiResponse.success(notificationResponseDTO, HttpStatus.CREATED, "Notification sent successfully");
        when(notificationService.sendNotification(any(SendNotificationDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendNotificationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("Test notification"))
                .andExpect(jsonPath("$.message").value("Notification sent successfully"));
    }

    @Test
    @DisplayName("Should handle invalid send notification request")
    void sendNotification_InvalidRequest() throws Exception {
        SendNotificationDTO invalidDTO = new SendNotificationDTO();
        // Missing required fields

        mockMvc.perform(post("/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get user notifications successfully")
    void getUserNotifications_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<NotificationListResponseDTO>> responseEntity = 
                ApiResponse.success(notificationListResponseDTO, HttpStatus.OK, "Notifications retrieved successfully");
            when(notificationService.getUserNotifications(testUser.getId(), 0, 10, false))
                .thenReturn(responseEntity);

            mockMvc.perform(get("/notifications/")
                            .param("page", "0")
                            .param("size", "10")
                            .param("unreadOnly", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notifications").isArray())
                    .andExpect(jsonPath("$.data.notifications[0].content").value("Test notification"));
        }
    }

    @Test
    @DisplayName("Should get user notifications with default parameters")
    void getUserNotifications_DefaultParams() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<NotificationListResponseDTO>> responseEntity = 
                ApiResponse.success(notificationListResponseDTO, HttpStatus.OK, "Notifications retrieved successfully");
            when(notificationService.getUserNotifications(testUser.getId(), 0, 10, false))
                .thenReturn(responseEntity);

            mockMvc.perform(get("/notifications/"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notifications").isArray());
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for get notifications")
    void getUserNotifications_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(get("/notifications/"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should get unread count successfully")
    void getUnreadCount_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<UnreadCountResponseDTO>> responseEntity = 
                ApiResponse.success(unreadCountResponseDTO, HttpStatus.OK, "Unread count retrieved successfully");
            when(notificationService.getUnreadCount(testUser.getId())).thenReturn(responseEntity);

            mockMvc.perform(get("/notifications/unread-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.unreadCount").value(5));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for unread count")
    void getUnreadCount_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(get("/notifications/unread-count"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void markNotificationAsRead_Success() throws Exception {
        UUID notificationId = UUID.randomUUID();
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            NotificationResponseDTO readNotification = new NotificationResponseDTO();
            readNotification.setId(notificationId);
            readNotification.setContent("Test notification");
            readNotification.setRead(true);
            readNotification.setCreatedAt(OffsetDateTime.now());
            
            ResponseEntity<ApiResponse<NotificationResponseDTO>> responseEntity = 
                ApiResponse.success(readNotification, HttpStatus.OK, "Notification marked as read");
            when(notificationService.markAsRead(notificationId, testUser.getId())).thenReturn(responseEntity);

            mockMvc.perform(put("/notifications/{id}/read", notificationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.read").value(true));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for mark as read")
    void markNotificationAsRead_Unauthenticated() throws Exception {
        UUID notificationId = UUID.randomUUID();
        
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(put("/notifications/{id}/read", notificationId))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void markAllNotificationsAsRead_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            UnreadCountResponseDTO zeroCount = new UnreadCountResponseDTO();
            zeroCount.setUnreadCount(0);
            
            ResponseEntity<ApiResponse<UnreadCountResponseDTO>> responseEntity = 
                ApiResponse.success(zeroCount, HttpStatus.OK, "All notifications marked as read");
            when(notificationService.markAllAsRead(testUser.getId())).thenReturn(responseEntity);

            mockMvc.perform(put("/notifications/mark-all-read"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.unreadCount").value(0));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for mark all as read")
    void markAllNotificationsAsRead_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(put("/notifications/mark-all-read"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should get notification preferences successfully")
    void getNotificationPreferences_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<NotificationPreferenceListResponseDTO>> responseEntity = 
                ApiResponse.success(preferenceListResponseDTO, HttpStatus.OK, "Preferences retrieved successfully");
            when(notificationPreferenceService.getAllPreferences()).thenReturn(responseEntity);

            mockMvc.perform(get("/notifications/preferences"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.preferences").isArray())
                    .andExpect(jsonPath("$.data.preferences[0].type").value("CASE_CREATE"));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for get preferences")
    void getNotificationPreferences_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(get("/notifications/preferences"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should update notification preference successfully")
    void updateNotificationPreference_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            NotificationPreferenceResponseDTO updatedPreference = new NotificationPreferenceResponseDTO();
            updatedPreference.setType(NotificationType.CASE_CREATE);
            updatedPreference.setDisplayName("Case Creation");
            updatedPreference.setEmailEnabled(true);
            updatedPreference.setWebPushEnabled(false);
            
            ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> responseEntity = 
                ApiResponse.success(updatedPreference, HttpStatus.OK, "Preference updated successfully");
            when(notificationPreferenceService.updateNotificationPref(eq(NotificationType.CASE_CREATE), any(UpdateNotificationPreferenceDTO.class)))
                .thenReturn(responseEntity);

            mockMvc.perform(put("/notifications/preferences/{type}", "CASE_CREATE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePreferenceDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.emailEnabled").value(true))
                    .andExpect(jsonPath("$.data.webPushEnabled").value(false));
        }
    }

    @Test
    @DisplayName("Should handle invalid notification type for preference update")
    void updateNotificationPreference_InvalidType() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            mockMvc.perform(put("/notifications/preferences/{type}", "INVALID_TYPE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePreferenceDTO)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for preference update")
    void updateNotificationPreference_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(put("/notifications/preferences/{type}", "CASE_CREATE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePreferenceDTO)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle preference update request with default values")
    void updateNotificationPreference_DefaultValues() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            UpdateNotificationPreferenceDTO defaultDTO = new UpdateNotificationPreferenceDTO();
            // Primitive booleans default to false, so this is valid
            
            NotificationPreferenceResponseDTO updatedPreference = new NotificationPreferenceResponseDTO();
            updatedPreference.setType(NotificationType.CASE_CREATE);
            updatedPreference.setDisplayName("Case Creation");
            updatedPreference.setEmailEnabled(false);
            updatedPreference.setWebPushEnabled(false);
            
            ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> responseEntity = 
                ApiResponse.success(updatedPreference, HttpStatus.OK, "Preference updated successfully");
            when(notificationPreferenceService.updateNotificationPref(eq(NotificationType.CASE_CREATE), any(UpdateNotificationPreferenceDTO.class)))
                .thenReturn(responseEntity);

            mockMvc.perform(put("/notifications/preferences/{type}", "CASE_CREATE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.emailEnabled").value(false))
                    .andExpect(jsonPath("$.data.webPushEnabled").value(false));
        }
    }

    @Test
    @DisplayName("Should handle JSON parsing errors")
    void handleJsonParsingErrors() throws Exception {
        mockMvc.perform(post("/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle missing content type")
    void handleMissingContentType() throws Exception {
        mockMvc.perform(post("/notifications/send")
                        .content(objectMapper.writeValueAsString(sendNotificationDTO)))
                .andExpect(status().isInternalServerError());
    }
}