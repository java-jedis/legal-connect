package com.javajedis.legalconnect.chat;

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
import com.javajedis.legalconnect.chat.dto.ConversationListResponseDTO;
import com.javajedis.legalconnect.chat.dto.ConversationResponseDTO;
import com.javajedis.legalconnect.chat.dto.MessageListResponseDTO;
import com.javajedis.legalconnect.chat.dto.MessageResponseDTO;
import com.javajedis.legalconnect.chat.dto.SendMessageDTO;
import com.javajedis.legalconnect.chat.dto.UnreadCountResponseDTO;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.common.utility.JWTFilter;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@WebMvcTest(controllers = ChatController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {EmailVerificationFilter.class, JWTFilter.class}))
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(ChatControllerTest.TestConfig.class)
@DisplayName("ChatController Tests")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UUID conversationId;
    private SendMessageDTO sendMessageDTO;
    private MessageResponseDTO messageResponseDTO;
    private ConversationResponseDTO conversationResponseDTO;
    private ConversationListResponseDTO conversationListResponseDTO;
    private MessageListResponseDTO messageListResponseDTO;
    private UnreadCountResponseDTO unreadCountResponseDTO;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ChatService chatService() {
            return mock(ChatService.class);
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

        conversationId = UUID.randomUUID();

        // Setup DTOs
        sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setReceiverId(UUID.randomUUID());
        sendMessageDTO.setContent("Test message content");

        messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setId(UUID.randomUUID());
        messageResponseDTO.setConversationId(conversationId);
        messageResponseDTO.setSenderId(testUser.getId());
        messageResponseDTO.setContent("Test message content");
        messageResponseDTO.setRead(false);
        messageResponseDTO.setCreatedAt(OffsetDateTime.now());

        conversationResponseDTO = new ConversationResponseDTO();
        conversationResponseDTO.setId(conversationId);
        conversationResponseDTO.setOtherParticipantId(UUID.randomUUID());
        conversationResponseDTO.setOtherParticipantName("Other User");
        conversationResponseDTO.setLatestMessage(messageResponseDTO);
        conversationResponseDTO.setUnreadCount(2);
        conversationResponseDTO.setUpdatedAt(OffsetDateTime.now());

        conversationListResponseDTO = new ConversationListResponseDTO();
        conversationListResponseDTO.setConversations(Arrays.asList(conversationResponseDTO));

        messageListResponseDTO = new MessageListResponseDTO();
        messageListResponseDTO.setMessages(Arrays.asList(messageResponseDTO));

        unreadCountResponseDTO = new UnreadCountResponseDTO();
        unreadCountResponseDTO.setTotalUnreadCount(5L);
    }

    @Test
    @DisplayName("Should send message successfully")
    void sendMessage_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageResponseDTO>> responseEntity = 
                ApiResponse.success(messageResponseDTO, HttpStatus.CREATED, "Message sent successfully");
            when(chatService.sendMessage(eq(testUser.getId()), any(SendMessageDTO.class))).thenReturn(responseEntity);

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sendMessageDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.content").value("Test message content"))
                    .andExpect(jsonPath("$.data.senderId").value(testUser.getId().toString()))
                    .andExpect(jsonPath("$.message").value("Message sent successfully"));
        }
    }

    @Test
    @DisplayName("Should handle invalid send message request")
    void sendMessage_InvalidRequest() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            SendMessageDTO invalidDTO = new SendMessageDTO();
            // Missing required fields

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for send message")
    void sendMessage_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sendMessageDTO)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle send message with empty content")
    void sendMessage_EmptyContent() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            SendMessageDTO emptyContentDTO = new SendMessageDTO();
            emptyContentDTO.setReceiverId(UUID.randomUUID());
            emptyContentDTO.setContent("");

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyContentDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Should handle send message with content too long")
    void sendMessage_ContentTooLong() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            SendMessageDTO longContentDTO = new SendMessageDTO();
            longContentDTO.setReceiverId(UUID.randomUUID());
            longContentDTO.setContent("a".repeat(1001)); // Exceeds 1000 character limit

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(longContentDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Should get user conversations successfully")
    void getUserConversations_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<ConversationListResponseDTO>> responseEntity = 
                ApiResponse.success(conversationListResponseDTO, HttpStatus.OK, "Conversations retrieved successfully");
            when(chatService.getUserConversations(testUser.getId())).thenReturn(responseEntity);

            mockMvc.perform(get("/chat/conversations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.conversations").isArray())
                    .andExpect(jsonPath("$.data.conversations[0].otherParticipantName").value("Other User"))
                    .andExpect(jsonPath("$.data.conversations[0].unreadCount").value(2))
                    .andExpect(jsonPath("$.message").value("Conversations retrieved successfully"));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for get conversations")
    void getUserConversations_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(get("/chat/conversations"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should get conversation messages successfully")
    void getConversationMessages_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageListResponseDTO>> responseEntity = 
                ApiResponse.success(messageListResponseDTO, HttpStatus.OK, "Messages retrieved successfully");
            when(chatService.getConversationMessages(conversationId, testUser.getId(), 0, 20))
                .thenReturn(responseEntity);

            mockMvc.perform(get("/chat/conversations/{id}/messages", conversationId)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages").isArray())
                    .andExpect(jsonPath("$.data.messages[0].content").value("Test message content"))
                    .andExpect(jsonPath("$.message").value("Messages retrieved successfully"));
        }
    }

    @Test
    @DisplayName("Should get conversation messages with default parameters")
    void getConversationMessages_DefaultParams() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageListResponseDTO>> responseEntity = 
                ApiResponse.success(messageListResponseDTO, HttpStatus.OK, "Messages retrieved successfully");
            when(chatService.getConversationMessages(conversationId, testUser.getId(), 0, 20))
                .thenReturn(responseEntity);

            mockMvc.perform(get("/chat/conversations/{id}/messages", conversationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages").isArray());
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for get conversation messages")
    void getConversationMessages_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(get("/chat/conversations/{id}/messages", conversationId))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should get conversation messages with custom pagination")
    void getConversationMessages_CustomPagination() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageListResponseDTO>> responseEntity = 
                ApiResponse.success(messageListResponseDTO, HttpStatus.OK, "Messages retrieved successfully");
            when(chatService.getConversationMessages(conversationId, testUser.getId(), 1, 10))
                .thenReturn(responseEntity);

            mockMvc.perform(get("/chat/conversations/{id}/messages", conversationId)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages").isArray());
        }
    }

    @Test
    @DisplayName("Should mark conversation as read successfully")
    void markConversationAsRead_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<Void>> responseEntity = 
                ApiResponse.success(null, HttpStatus.OK, "Messages marked as read successfully");
            when(chatService.markConversationAsRead(conversationId, testUser.getId())).thenReturn(responseEntity);

            mockMvc.perform(put("/chat/conversations/{id}/read", conversationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Messages marked as read successfully"));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for mark conversation as read")
    void markConversationAsRead_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(put("/chat/conversations/{id}/read", conversationId))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should get total unread count successfully")
    void getTotalUnreadCount_Success() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<UnreadCountResponseDTO>> responseEntity = 
                ApiResponse.success(unreadCountResponseDTO, HttpStatus.OK, "Unread count retrieved successfully");
            when(chatService.getTotalUnreadCount(testUser.getId())).thenReturn(responseEntity);

            mockMvc.perform(get("/chat/unread-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalUnreadCount").value(5))
                    .andExpect(jsonPath("$.message").value("Unread count retrieved successfully"));
        }
    }

    @Test
    @DisplayName("Should handle unauthenticated user for get total unread count")
    void getTotalUnreadCount_Unauthenticated() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            mockMvc.perform(get("/chat/unread-count"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle JSON parsing errors")
    void handleJsonParsingErrors() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle missing content type")
    void handleMissingContentType() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            mockMvc.perform(post("/chat/send")
                            .content(objectMapper.writeValueAsString(sendMessageDTO)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle service errors gracefully")
    void handleServiceErrors() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageResponseDTO>> errorResponse = 
                ApiResponse.error("Service error", HttpStatus.INTERNAL_SERVER_ERROR);
            when(chatService.sendMessage(eq(testUser.getId()), any(SendMessageDTO.class))).thenReturn(errorResponse);

            mockMvc.perform(post("/chat/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sendMessageDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.message").value("Service error"));
        }
    }

    @Test
    @DisplayName("Should handle invalid UUID in path parameter")
    void handleInvalidUuidInPath() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);

            mockMvc.perform(get("/chat/conversations/{id}/messages", "invalid-uuid"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    @DisplayName("Should handle negative page parameter")
    void handleNegativePageParameter() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageListResponseDTO>> errorResponse = 
                ApiResponse.error("Page number cannot be negative", HttpStatus.BAD_REQUEST);
            when(chatService.getConversationMessages(conversationId, testUser.getId(), -1, 20))
                .thenReturn(errorResponse);

            mockMvc.perform(get("/chat/conversations/{id}/messages", conversationId)
                            .param("page", "-1")
                            .param("size", "20"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Page number cannot be negative"));
        }
    }

    @Test
    @DisplayName("Should handle invalid size parameter")
    void handleInvalidSizeParameter() throws Exception {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(testUser);
            
            ResponseEntity<ApiResponse<MessageListResponseDTO>> errorResponse = 
                ApiResponse.error("Page size must be between 1 and 100", HttpStatus.BAD_REQUEST);
            when(chatService.getConversationMessages(conversationId, testUser.getId(), 0, 101))
                .thenReturn(errorResponse);

            mockMvc.perform(get("/chat/conversations/{id}/messages", conversationId)
                            .param("page", "0")
                            .param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Page size must be between 1 and 100"));
        }
    }
}