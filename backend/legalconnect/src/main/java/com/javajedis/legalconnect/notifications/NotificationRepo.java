package com.javajedis.legalconnect.notifications;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, UUID> {
    
    /**
     * Find all notifications for a specific receiver ordered by creation date (newest first)
     */
    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(UUID receiverId, Pageable pageable);
    
    /**
     * Find all unread notifications for a specific receiver ordered by creation date (newest first)
     */
    Page<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(UUID receiverId, Pageable pageable);
    
    /**
     * Find all read notifications for a specific receiver ordered by creation date (newest first)
     */
    Page<Notification> findByReceiverIdAndIsReadTrueOrderByCreatedAtDesc(UUID receiverId, Pageable pageable);
    
    /**
     * Count unread notifications for a specific receiver
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiverId = :receiverId AND n.isRead = false")
    long countUnreadByReceiverId(@Param("receiverId") UUID receiverId);
    
    /**
     * Count total notifications for a specific receiver
     */
    long countByReceiverId(UUID receiverId);
}