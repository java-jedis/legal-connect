package com.javajedis.legalconnect.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.notifications.dto.NotificationListResponseDTO;
import com.javajedis.legalconnect.notifications.dto.NotificationResponseDTO;
import com.javajedis.legalconnect.notifications.dto.SendNotificationDTO;
import com.javajedis.legalconnect.notifications.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.notifications.exception.NotificationDeliveryException;
import com.javajedis.legalconnect.notifications.exception.NotificationNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

    private static final String NOTIFICATION_SENT_MSG = "Notification sent successfully";
    private static final String INVALID_RECEIVER_MSG = "Invalid receiver ID";
    private static final String NOTIFICATIONS_RETRIEVED_MSG = "Notifications retrieved successfully";
    private static final String UNREAD_COUNT_RETRIEVED_MSG = "Unread count retrieved successfully";
    private static final String NOTIFICATION_MARKED_READ_MSG = "Notification marked as read";
    private static final String ALL_NOTIFICATIONS_MARKED_READ_MSG = "All notifications marked as read";

    private final NotificationRepo notificationRepo;
    private final WebSocketService webSocketService;

    public NotificationService(NotificationRepo notificationRepo, WebSocketService webSocketService) {
        this.notificationRepo = notificationRepo;
        this.webSocketService = webSocketService;
    }

    /**
     * Sends a notification to a specific user. This method creates and stores the notification
     * in the database and attempts real-time delivery via WebSocket if the user is connected.
     * The method returns immediately without blocking the calling service.
     */
    @Transactional
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> sendNotification(SendNotificationDTO notificationData) {
        log.debug("Sending notification to user: {}", notificationData.getReceiverId());

        Notification notification = new Notification();
        notification.setReceiverId(notificationData.getReceiverId());
        notification.setContent(notificationData.getContent().trim());
        notification.setRead(false);

        Notification savedNotification = notificationRepo.save(notification);
        log.info("Notification created with ID: {} for user: {}", savedNotification.getId(), notificationData.getReceiverId());

        NotificationResponseDTO responseDTO = mapNotificationToResponseDTO(savedNotification);

        deliverNotificationRealTime(savedNotification.getReceiverId(), responseDTO);

        return ApiResponse.success(responseDTO, HttpStatus.CREATED, NOTIFICATION_SENT_MSG);
    }

    /**
     * Sends a notification to a specific user by user ID and content.
     * This is a convenience method for internal service calls.
     */
    @Transactional
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> sendNotification(UUID receiverId, String content) {
        log.debug("Sending notification to user: {}", receiverId);

        if (receiverId == null) {
            log.warn("Attempted to send notification with null receiver ID");
            throw new IllegalArgumentException("Receiver ID cannot be null");
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("Attempted to send notification with empty content to user: {}", receiverId);
            throw new IllegalArgumentException("Notification content cannot be empty");
        }

        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setContent(content.trim());
        notification.setRead(false);

        Notification savedNotification = notificationRepo.save(notification);
        log.info("Notification created with ID: {} for user: {}", savedNotification.getId(), receiverId);

        NotificationResponseDTO responseDTO = mapNotificationToResponseDTO(savedNotification);

        deliverNotificationRealTime(savedNotification.getReceiverId(), responseDTO);

        return ApiResponse.success(responseDTO, HttpStatus.CREATED, NOTIFICATION_SENT_MSG);
    }

    /**
     * Delivers a notification via WebSocket in real-time with graceful degradation.
     * This method handles WebSocket delivery failures gracefully without blocking the caller.
     * Implements graceful degradation - if WebSocket delivery fails, the notification is still stored.
     */
    private void deliverNotificationRealTime(UUID receiverId, NotificationResponseDTO notificationResponse) {
        if (receiverId == null || notificationResponse == null) {
            log.warn("Cannot deliver notification: receiverId or notificationResponse is null");
            return;
        }

        try {
            boolean delivered = webSocketService.sendNotificationToUser(receiverId, notificationResponse);
            if (delivered) {
                log.debug("Real-time notification delivered to user: {}", receiverId);
            } else {
                log.debug("User {} not connected via WebSocket, notification stored for later retrieval", receiverId);
            }
        } catch (NotificationDeliveryException e) {
            // Specific WebSocket delivery failure - log but don't fail the operation
            log.warn("WebSocket delivery failed for user {}: {} - notification remains available via REST API",
                    receiverId, e.getMessage());
        } catch (SecurityException e) {
            // Authentication/authorization issues with WebSocket
            log.warn("Security error during WebSocket delivery to user {}: {} - notification remains available via REST API",
                    receiverId, e.getMessage());
        } catch (Exception e) {
            // Any other unexpected error during WebSocket delivery
            log.error("Unexpected error during WebSocket delivery to user {}: {} - notification remains available via REST API",
                    receiverId, e.getMessage(), e);
            // Don't rethrow - we want graceful degradation when WebSocket delivery fails
        }
    }

    /**
     * Retrieves notifications for a specific user with pagination support.
     * Returns notifications ordered by creation date (newest first).
     */
    public ResponseEntity<ApiResponse<NotificationListResponseDTO>> getUserNotifications(
            UUID receiverId, int page, int size, boolean unreadOnly) {
        log.debug("Getting notifications for user: {} with page={}, size={}, unreadOnly={}",
                receiverId, page, size, unreadOnly);

        if (receiverId == null) {
            log.warn("Attempted to get notifications with null receiver ID");
            throw new IllegalArgumentException(INVALID_RECEIVER_MSG);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Notification> notificationPage;
        if (unreadOnly) {
            notificationPage = notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(receiverId, pageable);
        } else {
            notificationPage = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(receiverId, pageable);
        }

        List<NotificationResponseDTO> notificationResponses = notificationPage.getContent().stream()
                .map(this::mapNotificationToResponseDTO)
                .toList();

        NotificationListResponseDTO responseData = new NotificationListResponseDTO(notificationResponses);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCount", notificationPage.getTotalElements());
        metadata.put("pageNumber", notificationPage.getNumber());
        metadata.put("pageSize", notificationPage.getSize());
        metadata.put("totalPages", notificationPage.getTotalPages());
        metadata.put("hasNext", notificationPage.hasNext());
        metadata.put("hasPrevious", notificationPage.hasPrevious());
        metadata.put("isFirst", notificationPage.isFirst());
        metadata.put("isLast", notificationPage.isLast());
        metadata.put("appliedFilters", unreadOnly ? Map.of("unreadOnly", true) : Map.of());

        log.info("Retrieved {} notifications for user: {} (page {}/{})",
                notificationResponses.size(), receiverId, page + 1, notificationPage.getTotalPages());

        return ApiResponse.success(responseData, HttpStatus.OK, NOTIFICATIONS_RETRIEVED_MSG, metadata);
    }

    /**
     * Gets the count of unread notifications for a specific user.
     */
    public ResponseEntity<ApiResponse<UnreadCountResponseDTO>> getUnreadCount(UUID receiverId) {
        log.debug("Getting unread count for user: {}", receiverId);

        if (receiverId == null) {
            log.warn("Attempted to get unread count with null receiver ID");
            throw new IllegalArgumentException(INVALID_RECEIVER_MSG);
        }

        long unreadCount = notificationRepo.countUnreadByReceiverId(receiverId);
        UnreadCountResponseDTO responseData = new UnreadCountResponseDTO((int) unreadCount);

        log.debug("User {} has {} unread notifications", receiverId, unreadCount);
        return ApiResponse.success(responseData, HttpStatus.OK, UNREAD_COUNT_RETRIEVED_MSG);
    }

    /**
     * Marks a specific notification as read.
     */
    @Transactional
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markAsRead(UUID notificationId, UUID receiverId) {
        log.debug("Marking notification {} as read for user: {}", notificationId, receiverId);

        if (notificationId == null || receiverId == null) {
            log.warn("Attempted to mark notification as read with null ID(s)");
            throw new IllegalArgumentException("Notification ID and receiver ID are required");
        }

        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Notification not found with ID: {}", notificationId);
                    return new NotificationNotFoundException("Notification not found with ID: " + notificationId);
                });

        if (!notification.getReceiverId().equals(receiverId)) {
            log.warn("User {} attempted to mark notification {} that doesn't belong to them", receiverId, notificationId);
            throw new SecurityException("You can only mark your own notifications as read");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepo.save(notification);
            log.info("Notification {} marked as read for user: {}", notificationId, receiverId);
        } else {
            log.debug("Notification {} was already marked as read for user: {}", notificationId, receiverId);
        }

        NotificationResponseDTO responseDTO = mapNotificationToResponseDTO(notification);
        return ApiResponse.success(responseDTO, HttpStatus.OK, NOTIFICATION_MARKED_READ_MSG);
    }

    /**
     * Marks all notifications as read for a specific user.
     */
    @Transactional
    public ResponseEntity<ApiResponse<UnreadCountResponseDTO>> markAllAsRead(UUID receiverId) {
        log.debug("Marking all notifications as read for user: {}", receiverId);

        if (receiverId == null) {
            log.warn("Attempted to mark all notifications as read with null receiver ID");
            throw new IllegalArgumentException(INVALID_RECEIVER_MSG);
        }

        List<Notification> unreadNotifications = notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(
                receiverId, Pageable.unpaged()).getContent();

        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificationRepo.saveAll(unreadNotifications);

            log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), receiverId);
        } else {
            log.debug("No unread notifications found for user: {}", receiverId);
        }

        // Return the new unread count (should be 0)
        UnreadCountResponseDTO responseData = new UnreadCountResponseDTO(0);
        return ApiResponse.success(responseData, HttpStatus.OK, ALL_NOTIFICATIONS_MARKED_READ_MSG);
    }

    /**
     * Maps a Notification entity to NotificationResponseDTO.
     */
    private NotificationResponseDTO mapNotificationToResponseDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}