package com.javajedis.legalconnect.notifications;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.notifications.dto.NotificationPreferenceListResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationPreferenceResponseDTO;
import com.javajedis.legalconnect.notifications.dto.UpdateNotificationPreferenceDTO;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String PREFERENCE_NOT_FOUND_MSG = "Notification preference not found";
    private static final String PREFERENCE_UPDATED_MSG = "Notification preference updated successfully";
    private static final String PREFERENCES_RETRIEVED_MSG = "Notification preferences retrieved successfully";
    private static final String UNAUTHORIZED_PREFERENCE_RETRIEVAL_LOG = "Unauthorized preference retrieval attempt";
    private static final String UNAUTHORIZED_PREFERENCE_UPDATE_LOG = "Unauthorized preference update attempt";
    private static final String PREFERENCE_NOT_FOUND_LOG = "No preference found for user: {} and type: {}, defaulting to true";

    private final NotificationPreferenceRepo notificationPreferenceRepo;
    private final UserRepo userRepo;

    /**
     * Retrieves all notification preferences for the authenticated user.
     */
    public ResponseEntity<ApiResponse<NotificationPreferenceListResponseDTO>> getAllPreferences() {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        if (currentUser == null) {
            log.warn(UNAUTHORIZED_PREFERENCE_RETRIEVAL_LOG);
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        log.debug("Getting all notification preferences for authenticated user: {}", currentUser.getId().toString());

        List<NotificationPreference> notificationPreferences =
                notificationPreferenceRepo.findByUserId(currentUser.getId());

        List<NotificationPreferenceResponseDTO> preferenceDTOs = notificationPreferences.stream()
                .map(this::mapToNotificationPreferenceResponseDTO)
                .toList();

        NotificationPreferenceListResponseDTO responseData = new NotificationPreferenceListResponseDTO(preferenceDTOs);

        log.info("Retrieved {} notification preferences for user: {}", preferenceDTOs.size(), currentUser.getEmail());
        return ApiResponse.success(responseData, HttpStatus.OK, PREFERENCES_RETRIEVED_MSG);
    }

    /**
     * Updates a specific notification preference for the authenticated user.
     */
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDTO>> updateNotificationPref(
            NotificationType type, UpdateNotificationPreferenceDTO data) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);

        if (currentUser == null) {
            log.warn(UNAUTHORIZED_PREFERENCE_UPDATE_LOG);
            return ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED);
        }

        log.debug("Updating notification preference for type: {} for authenticated user {}", type, currentUser.getId().toString());

        Optional<NotificationPreference> preferenceOpt = notificationPreferenceRepo.findByUserIdAndNotificationType(
                currentUser.getId(), type);

        if (preferenceOpt.isEmpty()) {
            log.warn("Notification preference not found for user: {} and type: {}", currentUser.getEmail(), type);
            return ApiResponse.error(PREFERENCE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        NotificationPreference currentPreference = preferenceOpt.get();
        currentPreference.setEmailEnabled(data.isEmailEnabled());
        currentPreference.setWebPushEnabled(data.isWebPushEnabled());

        NotificationPreference updatedPreference = notificationPreferenceRepo.save(currentPreference);

        NotificationPreferenceResponseDTO responseDTO = mapToNotificationPreferenceResponseDTO(updatedPreference);

        log.info("Notification preference updated for user: {} and type: {} (email: {}, webPush: {})",
                currentUser.getEmail(), type, data.isEmailEnabled(), data.isWebPushEnabled());

        return ApiResponse.success(responseDTO, HttpStatus.OK, PREFERENCE_UPDATED_MSG);
    }

    /**
     * Checks if email notifications are enabled for a specific user and notification type.
     */
    public boolean checkEmailEnabled(UUID userId, NotificationType type) {
        log.debug("Checking email enabled status for user: {} and type: {}", userId, type);

        NotificationPreference preference = notificationPreferenceRepo.findByUserIdAndNotificationType(userId, type).orElse(null);

        if (preference == null) {
            log.debug(PREFERENCE_NOT_FOUND_LOG, userId, type);
            return true;
        }

        boolean enabled = preference.isEmailEnabled();
        log.debug("Email enabled status for user: {} and type: {} is: {}", userId, type, enabled);
        return enabled;
    }

    /**
     * Checks if web push notifications are enabled for a specific user and notification type.
     */
    public boolean checkWebPushEnabled(UUID userId, NotificationType type) {
        log.debug("Checking web push enabled status for user: {} and type: {}", userId, type);

        NotificationPreference preference = notificationPreferenceRepo.findByUserIdAndNotificationType(userId, type).orElse(null);

        if (preference == null) {
            log.debug(PREFERENCE_NOT_FOUND_LOG, userId, type);
            return true; // Default to enabled if no preference is set
        }

        boolean enabled = preference.isWebPushEnabled();
        log.debug("Web push enabled status for user: {} and type: {} is: {}", userId, type, enabled);
        return enabled;
    }

    /**
     * Maps a NotificationPreference entity to NotificationPreferenceResponseDTO.
     *
     * @param preference the NotificationPreference entity to map
     * @return the mapped NotificationPreferenceResponseDTO
     */
    private NotificationPreferenceResponseDTO mapToNotificationPreferenceResponseDTO(NotificationPreference preference) {
        return new NotificationPreferenceResponseDTO(
                preference.getNotificationType(),
                preference.getNotificationType().getDisplayName(),
                preference.isEmailEnabled(),
                preference.isWebPushEnabled()
        );
    }
}
