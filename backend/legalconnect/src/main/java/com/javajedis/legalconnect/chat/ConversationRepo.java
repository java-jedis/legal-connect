package com.javajedis.legalconnect.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation, UUID> {
    
    /**
     * Find all conversations for a specific user ordered by most recent activity
     */
    @Query("SELECT c FROM Conversation c WHERE c.participantOneId = :userId OR c.participantTwoId = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantOrderByUpdatedAtDesc(@Param("userId") UUID userId);
    
    /**
     * Find a conversation between two specific participants
     */
    @Query("SELECT c FROM Conversation c WHERE (c.participantOneId = :user1 AND c.participantTwoId = :user2) OR (c.participantOneId = :user2 AND c.participantTwoId = :user1)")
    Optional<Conversation> findByParticipants(@Param("user1") UUID user1, @Param("user2") UUID user2);
    
    /**
     * Check if a user is a participant in a specific conversation
     */
    @Query("SELECT COUNT(c) > 0 FROM Conversation c WHERE c.id = :conversationId AND (c.participantOneId = :userId OR c.participantTwoId = :userId)")
    boolean isUserParticipant(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}