package com.javajedis.legalconnect.notifications;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for NotificationPreference entity.
 * Provides methods to query notification preferences by various criteria.
 */
@Repository
public interface NotificationPreferenceRepo extends JpaRepository<NotificationPreference, UUID> {
    
    /**
     * Find a notification preference by user ID and notification type.
     * 
     * @param userId the ID of the user
     * @param notificationType the type of notification
     * @return Optional containing the notification preference if found
     */
    Optional<NotificationPreference> findByUserIdAndNotificationType(UUID userId, NotificationType notificationType);
    
    /**
     * Find all notification preferences for a specific notification type where email is enabled.
     * 
     * @param type the notification type
     * @param enabled whether email should be enabled (true) or disabled (false)
     * @return List of notification preferences matching the criteria
     */
    List<NotificationPreference> findByNotificationTypeAndEmailEnabled(NotificationType type, boolean enabled);
    
    /**
     * Find all notification preferences for a specific notification type where web push is enabled.
     * 
     * @param type the notification type
     * @param enabled whether web push should be enabled (true) or disabled (false)
     * @return List of notification preferences matching the criteria
     */
    List<NotificationPreference> findByNotificationTypeAndWebPushEnabled(NotificationType type, boolean enabled);
    
    /**
     * Find all notification preferences for a specific user.
     * 
     * @param userId the ID of the user
     * @return List of all notification preferences for the user
     */
    List<NotificationPreference> findByUserId(UUID userId);
} 