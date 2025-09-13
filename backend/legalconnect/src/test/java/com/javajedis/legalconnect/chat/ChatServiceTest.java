package com.javajedis.legalconnect.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.chat.dto.ConversationListResponseDTO;
import com.javajedis.legalconnect.chat.dto.ConversationResponseDTO;
import com.javajedis.legalconnect.chat.dto.MessageListResponseDTO;
import com.javajedis.legalconnect.chat.dto.MessageResponseDTO;
import com.javajedis.legalconnect.chat.dto.SendMessageDTO;
import com.javajedis.legalconnect.chat.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.service.WebSocketService;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepo messageRepo;

    @Mock
    private ConversationRepo conversationRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private ChatWebSocketController chatWebSocketController;

    @InjectMocks
    private ChatService chatService;

    private UUID senderId;
    private UUID receiverId;
    private UUID conversationId;
    private User senderUser;
    private User receiverUser;
    private Conversation conversation;
    private Message message;
    private SendMessageDTO sendMessageDTO;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        senderUser = new User();
        senderUser.setId(senderId);
        senderUser.setFirstName("John");
        senderUser.setLastName("Doe");
        senderUser.setEmail("john.doe@example.com");

        receiverUser = new User();
        receiverUser.setId(receiverId);
        receiverUser.setFirstName("Jane");
        receiverUser.setLastName("Smith");
        receiverUser.setEmail("jane.smith@example.com");
        receiverUser.setProfilePictureUrl("https://example.com/jane-full.jpg");
        receiverUser.setProfilePictureThumbnailUrl("https://example.com/jane-thumb.jpg");
        receiverUser.setProfilePicturePublicId("jane-profile-pic");

        conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setParticipantOneId(senderId);
        conversation.setParticipantTwoId(receiverId);
        conversation.setCreatedAt(OffsetDateTime.now());
        conversation.setUpdatedAt(OffsetDateTime.now());

        message = new Message();
        message.setId(UUID.randomUUID());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent("Test message");
        message.setRead(false);
        message.setCreatedAt(OffsetDateTime.now());

        sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setReceiverId(receiverId);
        sendMessageDTO.setContent("Test message");
    }

    @Test
    void sendMessage_Success_NewConversation() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(conversationRepo.findByParticipants(senderId, receiverId)).thenReturn(Optional.empty());
        when(conversationRepo.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepo.save(any(Message.class))).thenReturn(message);


        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message sent successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(message.getId(), response.getBody().getData().getId());
        assertEquals(message.getContent(), response.getBody().getData().getContent());
        assertEquals(senderId, response.getBody().getData().getSenderId());
        assertFalse(response.getBody().getData().isRead());

        verify(conversationRepo, times(2)).save(any(Conversation.class)); // Once to create, once to update timestamp
        verify(messageRepo).save(any(Message.class));
        verify(chatWebSocketController).sendMessageToUser(eq(receiverId), any(MessageResponseDTO.class));
    }

    @Test
    void sendMessage_Success_ExistingConversation() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(conversationRepo.findByParticipants(senderId, receiverId)).thenReturn(Optional.of(conversation));
        when(conversationRepo.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepo.save(any(Message.class))).thenReturn(message);


        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message sent successfully", response.getBody().getMessage());

        verify(conversationRepo, never()).save(argThat(c -> c.getId() == null)); // Should not create new conversation
        verify(conversationRepo).save(conversation); // Should update existing conversation
        verify(messageRepo).save(any(Message.class));
    }

    @Test
    void sendMessage_SenderNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sender user not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).save(any());
        verify(conversationRepo, never()).save(any());
    }

    @Test
    void sendMessage_ReceiverNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Receiver user not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).save(any());
        verify(conversationRepo, never()).save(any());
    }

    @Test
    void sendMessage_SendToSelf() {
        // Arrange
        sendMessageDTO.setReceiverId(senderId); // Same as sender
        when(userRepo.existsById(senderId)).thenReturn(true);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cannot send message to yourself", response.getBody().getError().getMessage());

        verify(messageRepo, never()).save(any());
        verify(conversationRepo, never()).save(any());
    }

    @Test
    void getUserConversations_Success() {
        // Arrange
        Message latestMessage = new Message();
        latestMessage.setId(UUID.randomUUID());
        latestMessage.setConversationId(conversationId);
        latestMessage.setSenderId(receiverId);
        latestMessage.setContent("Latest message");
        latestMessage.setRead(false);
        latestMessage.setCreatedAt(OffsetDateTime.now());

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(senderId)).thenReturn(Arrays.asList(conversation));
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiverUser));
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(conversationId)).thenReturn(Optional.of(latestMessage));
        when(messageRepo.countUnreadByConversationAndUser(conversationId, senderId)).thenReturn(2L);

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conversations retrieved successfully", response.getBody().getMessage());

        ConversationListResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertEquals(1, data.getConversations().size());

        ConversationResponseDTO conversationDTO = data.getConversations().get(0);
        assertEquals(conversationId, conversationDTO.getId());
        assertEquals(receiverId, conversationDTO.getOtherParticipantId());
        assertEquals("Jane Smith", conversationDTO.getOtherParticipantName());
        assertEquals(2, conversationDTO.getUnreadCount());
        assertNotNull(conversationDTO.getLatestMessage());
        assertEquals("Latest message", conversationDTO.getLatestMessage().getContent());
        
        // Verify profile picture data
        assertNotNull(conversationDTO.getOtherParticipantProfilePicture());
        assertEquals("https://example.com/jane-full.jpg", conversationDTO.getOtherParticipantProfilePicture().getFullPictureUrl());
        assertEquals("https://example.com/jane-thumb.jpg", conversationDTO.getOtherParticipantProfilePicture().getThumbnailPictureUrl());
        assertEquals("jane-profile-pic", conversationDTO.getOtherParticipantProfilePicture().getPublicId());
    }

    @Test
    void getUserConversations_UserNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getError().getMessage());

        verify(conversationRepo, never()).findByParticipantOrderByUpdatedAtDesc(any());
    }

    @Test
    void getUserConversations_EmptyList() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(senderId)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ConversationListResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertTrue(data.getConversations().isEmpty());
    }

    @Test
    void getConversationMessages_Success() {
        // Arrange
        Message message1 = new Message();
        message1.setId(UUID.randomUUID());
        message1.setConversationId(conversationId);
        message1.setSenderId(senderId);
        message1.setContent("Message 1");
        message1.setRead(true);
        message1.setCreatedAt(OffsetDateTime.now().minusMinutes(10));

        Message message2 = new Message();
        message2.setId(UUID.randomUUID());
        message2.setConversationId(conversationId);
        message2.setSenderId(receiverId);
        message2.setContent("Message 2");
        message2.setRead(false);
        message2.setCreatedAt(OffsetDateTime.now().minusMinutes(5));

        List<Message> messages = Arrays.asList(message2, message1); // Ordered by creation date desc
        Page<Message> messagePage = new PageImpl<>(messages, PageRequest.of(0, 20), 2);

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(true);
        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(eq(conversationId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response =
                chatService.getConversationMessages(conversationId, senderId, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Messages retrieved successfully", response.getBody().getMessage());

        MessageListResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertEquals(2, data.getMessages().size());

        // Verify order (newest first)
        assertEquals("Message 2", data.getMessages().get(0).getContent());
        assertEquals("Message 1", data.getMessages().get(1).getContent());
    }

    @Test
    void getConversationMessages_UserNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response =
                chatService.getConversationMessages(conversationId, senderId, 0, 20);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getError().getMessage());
    }

    @Test
    void getConversationMessages_ConversationNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response =
                chatService.getConversationMessages(conversationId, senderId, 0, 20);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conversation not found", response.getBody().getError().getMessage());
    }

    @Test
    void getConversationMessages_UserNotParticipant() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response =
                chatService.getConversationMessages(conversationId, senderId, 0, 20);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied: You are not a participant in this conversation", response.getBody().getError().getMessage());
    }

    @Test
    void getConversationMessages_InvalidPagination() {
        // Act & Assert - Negative page
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response1 =
                chatService.getConversationMessages(conversationId, senderId, -1, 20);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
        assertEquals("Page number cannot be negative", response1.getBody().getError().getMessage());

        // Act & Assert - Invalid size (0)
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response2 =
                chatService.getConversationMessages(conversationId, senderId, 0, 0);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
        assertEquals("Page size must be between 1 and 100", response2.getBody().getError().getMessage());

        // Act & Assert - Invalid size (too large)
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response3 =
                chatService.getConversationMessages(conversationId, senderId, 0, 101);
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
        assertEquals("Page size must be between 1 and 100", response3.getBody().getError().getMessage());
    }

    @Test
    void getConversationMessages_EmptyResult() {
        // Arrange
        Page<Message> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(true);
        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(eq(conversationId), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response =
                chatService.getConversationMessages(conversationId, senderId, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageListResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertTrue(data.getMessages().isEmpty());
    }

    @Test
    void sendMessage_WebSocketException() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(conversationRepo.findByParticipants(senderId, receiverId)).thenReturn(Optional.of(conversation));
        when(conversationRepo.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepo.save(any(Message.class))).thenReturn(message);
        doThrow(new RuntimeException("WebSocket error")).when(chatWebSocketController).sendMessageToUser(eq(receiverId), any(MessageResponseDTO.class));

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode()); // Should still succeed despite WebSocket error
        assertNotNull(response.getBody());
        assertEquals("Message sent successfully", response.getBody().getMessage());

        verify(messageRepo).save(any(Message.class));
    }

    @Test
    void getUserConversations_OtherParticipantNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(senderId)).thenReturn(Arrays.asList(conversation));
        when(userRepo.findById(receiverId)).thenReturn(Optional.empty()); // Other participant not found

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ConversationListResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertTrue(data.getConversations().isEmpty()); // Should filter out conversations with missing participants
    }

    // Read Status Management Tests

    @Test
    void markConversationAsRead_Success() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(true);
        when(messageRepo.markMessagesAsReadByConversationAndReceiver(conversationId, senderId)).thenReturn(3);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markConversationAsRead(conversationId, senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Messages marked as read successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(messageRepo).markMessagesAsReadByConversationAndReceiver(conversationId, senderId);
    }

    @Test
    void markConversationAsRead_UserNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markConversationAsRead(conversationId, senderId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).markMessagesAsReadByConversationAndReceiver(any(), any());
    }

    @Test
    void markConversationAsRead_ConversationNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markConversationAsRead(conversationId, senderId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conversation not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).markMessagesAsReadByConversationAndReceiver(any(), any());
    }

    @Test
    void markConversationAsRead_UserNotParticipant() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markConversationAsRead(conversationId, senderId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied: You are not a participant in this conversation", response.getBody().getError().getMessage());

        verify(messageRepo, never()).markMessagesAsReadByConversationAndReceiver(any(), any());
    }

    @Test
    void markMessageAsRead_Success() {
        // Arrange
        UUID messageId = message.getId();
        message.setSenderId(receiverId); // Message sent by receiver, so sender can mark it as read
        message.setRead(false);

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.findById(messageId)).thenReturn(Optional.of(message));
        when(conversationRepo.isUserParticipant(message.getConversationId(), senderId)).thenReturn(true);
        when(messageRepo.save(any(Message.class))).thenReturn(message);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markMessageAsRead(messageId, senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message marked as read successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        assertTrue(message.isRead()); // Message should be marked as read
        verify(messageRepo).save(message);
    }

    @Test
    void markMessageAsRead_AlreadyRead() {
        // Arrange
        UUID messageId = message.getId();
        message.setSenderId(receiverId); // Message sent by receiver
        message.setRead(true); // Already read

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.findById(messageId)).thenReturn(Optional.of(message));
        when(conversationRepo.isUserParticipant(message.getConversationId(), senderId)).thenReturn(true);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markMessageAsRead(messageId, senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message marked as read successfully", response.getBody().getMessage());

        verify(messageRepo, never()).save(any()); // Should not save if already read
    }

    @Test
    void markMessageAsRead_UserNotFound() {
        // Arrange
        UUID messageId = message.getId();
        when(userRepo.existsById(senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markMessageAsRead(messageId, senderId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).findById(any());
        verify(messageRepo, never()).save(any());
    }

    @Test
    void markMessageAsRead_MessageNotFound() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.findById(messageId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markMessageAsRead(messageId, senderId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Message not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).save(any());
    }

    @Test
    void markMessageAsRead_UserNotParticipant() {
        // Arrange
        UUID messageId = message.getId();
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.findById(messageId)).thenReturn(Optional.of(message));
        when(conversationRepo.isUserParticipant(message.getConversationId(), senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markMessageAsRead(messageId, senderId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied: You are not a participant in this conversation", response.getBody().getError().getMessage());

        verify(messageRepo, never()).save(any());
    }

    @Test
    void markMessageAsRead_CannotMarkOwnMessage() {
        // Arrange
        UUID messageId = message.getId();
        message.setSenderId(senderId); // User trying to mark their own message as read

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.findById(messageId)).thenReturn(Optional.of(message));
        when(conversationRepo.isUserParticipant(message.getConversationId(), senderId)).thenReturn(true);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markMessageAsRead(messageId, senderId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cannot mark your own message as read", response.getBody().getError().getMessage());

        verify(messageRepo, never()).save(any());
    }

    @Test
    void getTotalUnreadCount_Success() {
        // Arrange
        long expectedUnreadCount = 5L;
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.countTotalUnreadByUser(senderId)).thenReturn(expectedUnreadCount);

        // Act
        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = chatService.getTotalUnreadCount(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unread count retrieved successfully", response.getBody().getMessage());

        UnreadCountResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertEquals(expectedUnreadCount, data.getTotalUnreadCount());

        verify(messageRepo).countTotalUnreadByUser(senderId);
    }

    @Test
    void getTotalUnreadCount_UserNotFound() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = chatService.getTotalUnreadCount(senderId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getError().getMessage());

        verify(messageRepo, never()).countTotalUnreadByUser(any());
    }

    @Test
    void getTotalUnreadCount_ZeroUnread() {
        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(messageRepo.countTotalUnreadByUser(senderId)).thenReturn(0L);

        // Act
        ResponseEntity<ApiResponse<UnreadCountResponseDTO>> response = chatService.getTotalUnreadCount(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unread count retrieved successfully", response.getBody().getMessage());

        UnreadCountResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertEquals(0L, data.getTotalUnreadCount());
    }

    // Conversation Creation and Participant Management Tests

    @Test
    void sendMessage_ConversationCreation_ParticipantOrderConsistency() {
        // Test that conversation creation maintains consistent participant ordering
        // regardless of who sends the first message

        // Arrange - First message from sender to receiver
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(conversationRepo.findByParticipants(senderId, receiverId)).thenReturn(Optional.empty());

        // Capture the conversation that gets created
        Conversation createdConversation = new Conversation();
        createdConversation.setId(conversationId);
        createdConversation.setParticipantOneId(senderId);
        createdConversation.setParticipantTwoId(receiverId);
        createdConversation.setCreatedAt(OffsetDateTime.now());
        createdConversation.setUpdatedAt(OffsetDateTime.now());

        when(conversationRepo.save(any(Conversation.class))).thenReturn(createdConversation);
        when(messageRepo.save(any(Message.class))).thenReturn(message);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verify that findByParticipants was called to check for existing conversation
        verify(conversationRepo).findByParticipants(senderId, receiverId);

        // Verify that a new conversation was created
        verify(conversationRepo, times(2)).save(any(Conversation.class)); // Once to create, once to update timestamp
    }

    @Test
    void sendMessage_ConversationCreation_ReverseParticipantOrder() {
        // Test that conversation lookup works regardless of participant order

        // Arrange - Receiver sends message to sender (reverse order)
        SendMessageDTO reverseMessageDTO = new SendMessageDTO();
        reverseMessageDTO.setReceiverId(senderId);
        reverseMessageDTO.setContent("Reverse message");

        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipants(receiverId, senderId)).thenReturn(Optional.of(conversation));
        when(conversationRepo.save(any(Conversation.class))).thenReturn(conversation);

        Message reverseMessage = new Message();
        reverseMessage.setId(UUID.randomUUID());
        reverseMessage.setConversationId(conversationId);
        reverseMessage.setSenderId(receiverId);
        reverseMessage.setContent("Reverse message");
        reverseMessage.setRead(false);
        reverseMessage.setCreatedAt(OffsetDateTime.now());

        when(messageRepo.save(any(Message.class))).thenReturn(reverseMessage);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(receiverId, reverseMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verify that existing conversation was found and reused
        verify(conversationRepo).findByParticipants(receiverId, senderId);
        verify(conversationRepo, never()).save(argThat(c -> c.getId() == null)); // Should not create new conversation
        verify(conversationRepo).save(conversation); // Should update existing conversation timestamp
    }

    @Test
    void sendMessage_ConversationCreation_MultipleMessagesReuseConversation() {
        // Test that multiple messages between same participants reuse the same conversation

        // Arrange - First message creates conversation
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(conversationRepo.findByParticipants(senderId, receiverId))
                .thenReturn(Optional.empty()) // First call - no conversation exists
                .thenReturn(Optional.of(conversation)); // Second call - conversation exists
        when(conversationRepo.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepo.save(any(Message.class))).thenReturn(message);

        // Act - Send first message
        ResponseEntity<ApiResponse<MessageResponseDTO>> response1 = chatService.sendMessage(senderId, sendMessageDTO);

        // Act - Send second message
        ResponseEntity<ApiResponse<MessageResponseDTO>> response2 = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());

        // Verify that findByParticipants was called twice
        verify(conversationRepo, times(2)).findByParticipants(senderId, receiverId);

        // Verify conversation was created only once (first message) and updated twice (both messages)
        verify(conversationRepo, times(3)).save(any(Conversation.class)); // 1 create + 2 updates
    }

    @Test
    void getUserConversations_ParticipantValidation_UserAsParticipantOne() {
        // Test conversation retrieval when user is participant one

        // Arrange
        Conversation userAsParticipantOne = new Conversation();
        userAsParticipantOne.setId(conversationId);
        userAsParticipantOne.setParticipantOneId(senderId); // User is participant one
        userAsParticipantOne.setParticipantTwoId(receiverId);
        userAsParticipantOne.setCreatedAt(OffsetDateTime.now());
        userAsParticipantOne.setUpdatedAt(OffsetDateTime.now());

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(senderId))
                .thenReturn(Arrays.asList(userAsParticipantOne));
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiverUser));
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(conversationId))
                .thenReturn(Optional.of(message));
        when(messageRepo.countUnreadByConversationAndUser(conversationId, senderId)).thenReturn(1L);

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConversationListResponseDTO data = response.getBody().getData();
        assertEquals(1, data.getConversations().size());

        ConversationResponseDTO conversationDTO = data.getConversations().get(0);
        assertEquals(receiverId, conversationDTO.getOtherParticipantId()); // Other participant should be receiver
        assertEquals("Jane Smith", conversationDTO.getOtherParticipantName());
    }

    @Test
    void getUserConversations_ParticipantValidation_UserAsParticipantTwo() {
        // Test conversation retrieval when user is participant two

        // Arrange
        Conversation userAsParticipantTwo = new Conversation();
        userAsParticipantTwo.setId(conversationId);
        userAsParticipantTwo.setParticipantOneId(receiverId);
        userAsParticipantTwo.setParticipantTwoId(senderId); // User is participant two
        userAsParticipantTwo.setCreatedAt(OffsetDateTime.now());
        userAsParticipantTwo.setUpdatedAt(OffsetDateTime.now());

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(senderId))
                .thenReturn(Arrays.asList(userAsParticipantTwo));
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiverUser));
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(conversationId))
                .thenReturn(Optional.of(message));
        when(messageRepo.countUnreadByConversationAndUser(conversationId, senderId)).thenReturn(2L);

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConversationListResponseDTO data = response.getBody().getData();
        assertEquals(1, data.getConversations().size());

        ConversationResponseDTO conversationDTO = data.getConversations().get(0);
        assertEquals(receiverId, conversationDTO.getOtherParticipantId()); // Other participant should be receiver
        assertEquals("Jane Smith", conversationDTO.getOtherParticipantName());
    }

    @Test
    void getConversationMessages_ParticipantValidation_ValidParticipant() {
        // Test that valid participants can access conversation messages

        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(true);

        List<Message> messages = Arrays.asList(message);
        Page<Message> messagePage = new PageImpl<>(messages, PageRequest.of(0, 20), 1);
        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(eq(conversationId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        ResponseEntity<ApiResponse<MessageListResponseDTO>> response =
                chatService.getConversationMessages(conversationId, senderId, 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getMessages().size());

        // Verify participant validation was performed
        verify(conversationRepo).isUserParticipant(conversationId, senderId);
    }

    @Test
    void markConversationAsRead_ParticipantValidation_ValidParticipant() {
        // Test that valid participants can mark conversations as read

        // Arrange
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.existsById(conversationId)).thenReturn(true);
        when(conversationRepo.isUserParticipant(conversationId, senderId)).thenReturn(true);
        when(messageRepo.markMessagesAsReadByConversationAndReceiver(conversationId, senderId)).thenReturn(2);
        when(messageRepo.countTotalUnreadByUser(senderId)).thenReturn(3L);

        // Act
        ResponseEntity<ApiResponse<Void>> response = chatService.markConversationAsRead(conversationId, senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Messages marked as read successfully", response.getBody().getMessage());

        // Verify participant validation was performed
        verify(conversationRepo).isUserParticipant(conversationId, senderId);
        verify(messageRepo).markMessagesAsReadByConversationAndReceiver(conversationId, senderId);
    }

    @Test
    void sendMessage_ConversationCreation_TimestampUpdates() {
        // Test that conversation timestamps are properly updated when messages are sent

        // Arrange
        OffsetDateTime initialTime = OffsetDateTime.now().minusMinutes(10);
        conversation.setUpdatedAt(initialTime);

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(true);
        when(conversationRepo.findByParticipants(senderId, receiverId)).thenReturn(Optional.of(conversation));
        when(conversationRepo.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepo.save(any(Message.class))).thenReturn(message);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verify that conversation was saved (timestamp updated)
        verify(conversationRepo).save(conversation);

        // Verify that the conversation's updatedAt timestamp would be updated
        // (In real implementation, this would be set to current time)
    }

    @Test
    void sendMessage_ConversationCreation_WithNonExistentUsers() {
        // Test conversation creation validation with non-existent users

        // Arrange - Sender exists but receiver doesn't
        when(userRepo.existsById(senderId)).thenReturn(true);
        when(userRepo.existsById(receiverId)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<MessageResponseDTO>> response = chatService.sendMessage(senderId, sendMessageDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Receiver user not found", response.getBody().getError().getMessage());

        // Verify no conversation creation was attempted
        verify(conversationRepo, never()).findByParticipants(any(), any());
        verify(conversationRepo, never()).save(any());
        verify(messageRepo, never()).save(any());
    }

    @Test
    void getUserConversations_ParticipantValidation_MultipleConversations() {
        // Test participant validation with multiple conversations

        // Arrange
        UUID thirdUserId = UUID.randomUUID();
        User thirdUser = new User();
        thirdUser.setId(thirdUserId);
        thirdUser.setFirstName("Bob");
        thirdUser.setLastName("Johnson");

        Conversation conversation2 = new Conversation();
        conversation2.setId(UUID.randomUUID());
        conversation2.setParticipantOneId(thirdUserId);
        conversation2.setParticipantTwoId(senderId); // User is participant two
        conversation2.setCreatedAt(OffsetDateTime.now());
        conversation2.setUpdatedAt(OffsetDateTime.now());

        when(userRepo.existsById(senderId)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(senderId))
                .thenReturn(Arrays.asList(conversation, conversation2));

        // Mock user lookups for other participants
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiverUser));
        when(userRepo.findById(thirdUserId)).thenReturn(Optional.of(thirdUser));

        // Mock message and unread count lookups
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId()))
                .thenReturn(Optional.of(message));
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(conversation2.getId()))
                .thenReturn(Optional.empty());
        when(messageRepo.countUnreadByConversationAndUser(conversation.getId(), senderId)).thenReturn(1L);
        when(messageRepo.countUnreadByConversationAndUser(conversation2.getId(), senderId)).thenReturn(0L);

        // Act
        ResponseEntity<ApiResponse<ConversationListResponseDTO>> response = chatService.getUserConversations(senderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ConversationListResponseDTO data = response.getBody().getData();
        assertEquals(2, data.getConversations().size());

        // Verify both conversations are properly processed with correct other participants
        List<UUID> otherParticipantIds = data.getConversations().stream()
                .map(ConversationResponseDTO::getOtherParticipantId)
                .toList();
        assertTrue(otherParticipantIds.contains(receiverId));
        assertTrue(otherParticipantIds.contains(thirdUserId));
        
        // Verify profile picture handling
        ConversationResponseDTO receiverConversation = data.getConversations().stream()
                .filter(conv -> conv.getOtherParticipantId().equals(receiverId))
                .findFirst().orElse(null);
        assertNotNull(receiverConversation);
        assertNotNull(receiverConversation.getOtherParticipantProfilePicture());
        
        ConversationResponseDTO thirdUserConversation = data.getConversations().stream()
                .filter(conv -> conv.getOtherParticipantId().equals(thirdUserId))
                .findFirst().orElse(null);
        assertNotNull(thirdUserConversation);
        assertNull(thirdUserConversation.getOtherParticipantProfilePicture()); // thirdUser has no profile picture
    }


}