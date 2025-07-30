package com.javajedis.legalconnect.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepo extends JpaRepository<Message, UUID> {
    
    /**
     * Find messages for a conversation with pagination, ordered by creation date (newest first)
     */
    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
    
    /**
     * Count unread messages in a conversation for a specific user (excluding messages sent by the user)
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.senderId != :userId AND m.isRead = false")
    long countUnreadByConversationAndUser(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
    
    /**
     * Find unread messages in a conversation for a specific user (excluding messages sent by the user)
     */
    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.senderId != :userId AND m.isRead = false")
    List<Message> findUnreadByConversationAndUser(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
    
    /**
     * Find the most recent message in a conversation
     */
    Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);
    
    /**
     * Count total unread messages for a user across all conversations
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId != :userId AND m.isRead = false AND m.conversationId IN " +
           "(SELECT c.id FROM Conversation c WHERE c.participantOneId = :userId OR c.participantTwoId = :userId)")
    long countTotalUnreadByUser(@Param("userId") UUID userId);
    
    /**
     * Mark all unread messages in a conversation as read for a specific user (excluding messages sent by the user)
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.senderId != :userId AND m.isRead = false")
    int markMessagesAsReadByConversationAndReceiver(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}