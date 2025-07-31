package com.javajedis.legalconnect.chat;

import com.javajedis.legalconnect.chat.dto.*;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    // Constants for error messages
    private static final String USER_NOT_FOUND_LOG_MSG = "User not found: {}";
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String USER_NOT_PARTICIPANT_LOG_MSG = "User {} is not a participant in conversation {}";
    private static final String ACCESS_DENIED_NOT_PARTICIPANT_MSG = "Access denied: You are not a participant in this conversation";

    private final MessageRepo messageRepo;
    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;
    private final WebSocketService webSocketService;
    private final ChatWebSocketController chatWebSocketController;

    /**
     * Sends a message and triggers real-time delivery via WebSocket.
     * Creates a new conversation if one doesn't exist between the participants.
     */
    @Transactional
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(UUID senderId, SendMessageDTO messageData) {
        log.debug("Sending message from user {} to user {}", senderId, messageData.getReceiverId());

        if (!userRepo.existsById(senderId)) {
            log.warn(USER_NOT_FOUND_LOG_MSG, senderId);
            return ApiResponse.error("Sender user not found", HttpStatus.BAD_REQUEST);
        }

        if (!userRepo.existsById(messageData.getReceiverId())) {
            log.warn(USER_NOT_FOUND_LOG_MSG, messageData.getReceiverId());
            return ApiResponse.error("Receiver user not found", HttpStatus.BAD_REQUEST);
        }

        if (senderId.equals(messageData.getReceiverId())) {
            log.warn("User {} attempted to send message to themselves", senderId);
            return ApiResponse.error("Cannot send message to yourself", HttpStatus.BAD_REQUEST);
        }

        Conversation conversation = findOrCreateConversation(senderId, messageData.getReceiverId());

        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setContent(messageData.getContent());
        message.setRead(false);

        Message savedMessage = messageRepo.save(message);
        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepo.save(conversation);

        MessageResponseDTO responseDTO = convertToMessageResponseDTO(savedMessage);

        try {
            chatWebSocketController.sendMessageToUser(messageData.getReceiverId(), responseDTO);
            log.debug("Real-time message delivery attempted for user {}", messageData.getReceiverId());
        } catch (Exception e) {
            log.warn("Failed to deliver message via WebSocket to user {}: {}", messageData.getReceiverId(), e.getMessage());
            // Note: We don't throw ChatDeliveryException here as the message was saved successfully
            // The exception handler will return PARTIAL_CONTENT status if needed
        }

        log.info("Message sent successfully from {} to {} in conversation {}",
                senderId, messageData.getReceiverId(), conversation.getId());

        return ApiResponse.success(responseDTO, HttpStatus.CREATED, "Message sent successfully");
    }

    /**
     * Retrieves all conversations for a user with unread counts and latest messages.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ConversationListResponseDTO>> getUserConversations(UUID userId) {
        log.debug("Retrieving conversations for user {}", userId);

        if (!userRepo.existsById(userId)) {
            log.warn(USER_NOT_FOUND_LOG_MSG, userId);
            return ApiResponse.error(USER_NOT_FOUND_MSG, HttpStatus.NOT_FOUND);
        }

        List<Conversation> conversations = conversationRepo.findByParticipantOrderByUpdatedAtDesc(userId);
        List<ConversationResponseDTO> conversationDTOs = new ArrayList<>();

        for (Conversation conversation : conversations) {
            ConversationResponseDTO dto = convertToConversationResponseDTO(conversation, userId);
            if (dto != null) {
                conversationDTOs.add(dto);
            }
        }

        ConversationListResponseDTO response = new ConversationListResponseDTO(conversationDTOs);

        log.info("Retrieved {} conversations for user {}", conversationDTOs.size(), userId);
        return ApiResponse.success(response, HttpStatus.OK, "Conversations retrieved successfully");
    }

    /**
     * Retrieves messages for a specific conversation with pagination support.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<MessageListResponseDTO>> getConversationMessages(
            UUID conversationId, UUID userId, int page, int size) {
        log.debug("Retrieving messages for conversation {} by user {}, page {}, size {}",
                conversationId, userId, page, size);

        if (!userRepo.existsById(userId)) {
            log.warn(USER_NOT_FOUND_LOG_MSG, userId);
            return ApiResponse.error(USER_NOT_FOUND_MSG, HttpStatus.BAD_REQUEST);
        }

        if (!conversationRepo.existsById(conversationId)) {
            log.warn("Conversation not found: {}", conversationId);
            return ApiResponse.error("Conversation not found", HttpStatus.NOT_FOUND);
        }

        if (!conversationRepo.isUserParticipant(conversationId, userId)) {
            log.warn(USER_NOT_PARTICIPANT_LOG_MSG, userId, conversationId);
            return ApiResponse.error(ACCESS_DENIED_NOT_PARTICIPANT_MSG, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepo.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        List<MessageResponseDTO> messageDTOs = messagePage.getContent().stream()
                .map(this::convertToMessageResponseDTO)
                .toList();

        MessageListResponseDTO response = new MessageListResponseDTO(messageDTOs);

        log.info("Retrieved {} messages for conversation {} (page {}, size {})",
                messageDTOs.size(), conversationId, page, size);

        return ApiResponse.success(response, HttpStatus.OK, "Messages retrieved successfully");
    }

    /**
     * Marks all messages in a conversation as read for the specified user.
     * Only messages sent by other participants will be marked as read.
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(UUID conversationId, UUID userId) {
        log.debug("Marking conversation {} as read for user {}", conversationId, userId);

        if (!userRepo.existsById(userId)) {
            log.warn(USER_NOT_FOUND_LOG_MSG, userId);
            return ApiResponse.error(USER_NOT_FOUND_MSG, HttpStatus.BAD_REQUEST);
        }

        if (!conversationRepo.existsById(conversationId)) {
            log.warn("Conversation not found: {}", conversationId);
            return ApiResponse.error("Conversation not found", HttpStatus.NOT_FOUND);
        }

        if (!conversationRepo.isUserParticipant(conversationId, userId)) {
            log.warn(USER_NOT_PARTICIPANT_LOG_MSG, userId, conversationId);
            return ApiResponse.error(ACCESS_DENIED_NOT_PARTICIPANT_MSG, HttpStatus.BAD_REQUEST);
        }

        int updatedCount = messageRepo.markMessagesAsReadByConversationAndReceiver(conversationId, userId);

        if (updatedCount > 0) {
            try {
                long totalUnreadCount = messageRepo.countTotalUnreadByUser(userId);
                UnreadCountResponseDTO unreadCountUpdate = new UnreadCountResponseDTO(totalUnreadCount);
                chatWebSocketController.sendUnreadCountUpdate(userId, unreadCountUpdate);
                log.debug("Sent unread count update to user {} after marking conversation as read", userId);
            } catch (Exception e) {
                log.warn("Failed to send unread count update via WebSocket to user {}: {}", userId, e.getMessage());
            }
        }

        log.info("Marked {} messages as read in conversation {} for user {}",
                updatedCount, conversationId, userId);

        return ApiResponse.success(null, HttpStatus.OK, "Messages marked as read successfully");
    }

    /**
     * Marks a specific message as read.
     * Only the recipient of the message can mark it as read.
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(UUID messageId, UUID userId) {
        log.debug("Marking message {} as read for user {}", messageId, userId);

        if (!userRepo.existsById(userId)) {
            log.warn(USER_NOT_FOUND_LOG_MSG, userId);
            return ApiResponse.error(USER_NOT_FOUND_MSG, HttpStatus.BAD_REQUEST);
        }

        Optional<Message> messageOpt = messageRepo.findById(messageId);
        if (messageOpt.isEmpty()) {
            log.warn("Message not found: {}", messageId);
            return ApiResponse.error("Message not found", HttpStatus.NOT_FOUND);
        }

        Message message = messageOpt.get();

        if (!conversationRepo.isUserParticipant(message.getConversationId(), userId)) {
            log.warn(USER_NOT_PARTICIPANT_LOG_MSG, userId, message.getConversationId());
            return ApiResponse.error(ACCESS_DENIED_NOT_PARTICIPANT_MSG, HttpStatus.BAD_REQUEST);
        }

        if (message.getSenderId().equals(userId)) {
            log.warn("User {} attempted to mark their own message {} as read", userId, messageId);
            return ApiResponse.error("Cannot mark your own message as read", HttpStatus.BAD_REQUEST);
        }

        if (!message.isRead()) {
            message.setRead(true);
            messageRepo.save(message);
            try {
                chatWebSocketController.sendReadStatusUpdate(message.getSenderId(), messageId, true);
                log.debug("Sent read status update to sender {} for message {}", message.getSenderId(), messageId);
            } catch (Exception e) {
                log.warn("Failed to send read status update via WebSocket to sender {}: {}", message.getSenderId(), e.getMessage());
            }

            try {
                long totalUnreadCount = messageRepo.countTotalUnreadByUser(userId);
                UnreadCountResponseDTO unreadCountUpdate = new UnreadCountResponseDTO(totalUnreadCount);
                chatWebSocketController.sendUnreadCountUpdate(userId, unreadCountUpdate);
                log.debug("Sent unread count update to user {} after marking message as read", userId);
            } catch (Exception e) {
                log.warn("Failed to send unread count update via WebSocket to user {}: {}", userId, e.getMessage());
            }

            log.info("Message {} marked as read by user {}", messageId, userId);
        } else {
            log.debug("Message {} was already marked as read", messageId);
        }

        return ApiResponse.success(null, HttpStatus.OK, "Message marked as read successfully");
    }

    /**
     * Gets the total count of unread messages for a user across all conversations.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UnreadCountResponseDTO>> getTotalUnreadCount(UUID userId) {
        log.debug("Getting total unread count for user {}", userId);
        if (!userRepo.existsById(userId)) {
            log.warn(USER_NOT_FOUND_LOG_MSG, userId);
            return ApiResponse.error(USER_NOT_FOUND_MSG, HttpStatus.BAD_REQUEST);
        }

        long totalUnreadCount = messageRepo.countTotalUnreadByUser(userId);

        UnreadCountResponseDTO response = new UnreadCountResponseDTO(totalUnreadCount);

        log.info("User {} has {} total unread messages", userId, totalUnreadCount);
        return ApiResponse.success(response, HttpStatus.OK, "Unread count retrieved successfully");
    }

    /**
     * Finds an existing conversation between two users or creates a new one.
     */
    private Conversation findOrCreateConversation(UUID user1Id, UUID user2Id) {
        Optional<Conversation> existingConversation = conversationRepo.findByParticipants(user1Id, user2Id);

        if (existingConversation.isPresent()) {
            log.debug("Found existing conversation between users {} and {}: {}",
                    user1Id, user2Id, existingConversation.get().getId());
            return existingConversation.get();
        }
        Conversation newConversation = new Conversation();
        newConversation.setParticipantOneId(user1Id);
        newConversation.setParticipantTwoId(user2Id);

        Conversation savedConversation = conversationRepo.save(newConversation);
        log.info("Created new conversation between users {} and {}: {}",
                user1Id, user2Id, savedConversation.getId());

        return savedConversation;
    }

    /**
     * Converts a Message entity to MessageResponseDTO.
     */
    private MessageResponseDTO convertToMessageResponseDTO(Message message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setContent(message.getContent());
        dto.setRead(message.isRead());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    /**
     * Converts a Conversation entity to ConversationResponseDTO with additional data.
     */
    private ConversationResponseDTO convertToConversationResponseDTO(Conversation conversation, UUID currentUserId) {
        try {
            UUID otherParticipantId = conversation.getParticipantOneId().equals(currentUserId)
                    ? conversation.getParticipantTwoId()
                    : conversation.getParticipantOneId();

            Optional<User> otherUser = userRepo.findById(otherParticipantId);
            if (otherUser.isEmpty()) {
                log.warn("Other participant not found for conversation {}: {}", conversation.getId(), otherParticipantId);
                return null;
            }

            String otherParticipantName = otherUser.get().getFirstName() + " " + otherUser.get().getLastName();
            Optional<Message> latestMessage = messageRepo.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId());
            MessageResponseDTO latestMessageDTO = latestMessage.map(this::convertToMessageResponseDTO).orElse(null);
            long unreadCount = messageRepo.countUnreadByConversationAndUser(conversation.getId(), currentUserId);

            ConversationResponseDTO dto = new ConversationResponseDTO();
            dto.setId(conversation.getId());
            dto.setOtherParticipantId(otherParticipantId);
            dto.setOtherParticipantName(otherParticipantName);
            dto.setLatestMessage(latestMessageDTO);
            dto.setUnreadCount((int) unreadCount);
            dto.setUpdatedAt(conversation.getUpdatedAt());

            return dto;
        } catch (Exception e) {
            log.error("Error converting conversation {} to DTO: {}", conversation.getId(), e.getMessage());
            return null;
        }
    }
}