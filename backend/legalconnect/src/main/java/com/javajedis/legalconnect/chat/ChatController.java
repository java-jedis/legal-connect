package com.javajedis.legalconnect.chat;

import com.javajedis.legalconnect.chat.dto.*;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@Tag(name = "A. Chat", description = "Chat and messaging endpoints")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final String AUTHENTICATION_REQUIRED_MSG = "Authentication required";

    private final ChatService chatService;
    private final UserRepo userRepo;

    /**
     * Send a message to another user.
     * Creates a new conversation if one doesn't exist between the participants.
     */
    @Operation(summary = "Send message", description = "Sends a message to another user. Creates a new conversation if one doesn't exist between the participants.")
    @RequireUserOrVerifiedLawyer
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(@Valid @RequestBody SendMessageDTO messageData) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("POST /chat/send called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("POST /chat/send called for user: {} to receiver: {}", currentUser.getId(), messageData.getReceiverId());
        return chatService.sendMessage(currentUser.getId(), messageData);
    }

    /**
     * Get all conversations for the authenticated user.
     * Returns conversations ordered by most recent activity with unread counts and latest messages.
     */
    @Operation(summary = "Get user conversations", description = "Retrieves all conversations for the authenticated user ordered by most recent activity with unread counts and latest messages.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationListResponseDTO>> getUserConversations() {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("GET /chat/conversations called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("GET /chat/conversations called for user: {}", currentUser.getId());
        return chatService.getUserConversations(currentUser.getId());
    }

    /**
     * Get messages for a specific conversation with pagination support.
     * Only participants of the conversation can access its messages.
     */
    @Operation(summary = "Get conversation messages", description = "Retrieves messages for a specific conversation with pagination support. Only participants can access conversation messages.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<MessageListResponseDTO>> getConversationMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("GET /chat/conversations/{}/messages called but no authenticated user found", id);
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("GET /chat/conversations/{}/messages called for user: {} with page={}, size={}",
                id, currentUser.getId(), page, size);
        return chatService.getConversationMessages(id, currentUser.getId(), page, size);
    }

    /**
     * Mark all messages in a conversation as read for the authenticated user.
     * Only messages sent by other participants will be marked as read.
     */
    @Operation(summary = "Mark conversation as read", description = "Marks all messages in a conversation as read for the authenticated user. Only messages sent by other participants will be marked as read.")
    @RequireUserOrVerifiedLawyer
    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(@PathVariable UUID id) {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("PUT /chat/conversations/{}/read called but no authenticated user found", id);
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("PUT /chat/conversations/{}/read called for user: {}", id, currentUser.getId());
        return chatService.markConversationAsRead(id, currentUser.getId());
    }

    /**
     * Get the total count of unread messages for the authenticated user across all conversations.
     */
    @Operation(summary = "Get total unread count", description = "Retrieves the total count of unread messages for the authenticated user across all conversations.")
    @RequireUserOrVerifiedLawyer
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponseDTO>> getTotalUnreadCount() {
        User currentUser = GetUserUtil.getAuthenticatedUser(userRepo);
        if (currentUser == null) {
            log.warn("GET /chat/unread-count called but no authenticated user found");
            throw new SecurityException(AUTHENTICATION_REQUIRED_MSG);
        }

        log.info("GET /chat/unread-count called for user: {}", currentUser.getId());
        return chatService.getTotalUnreadCount(currentUser.getId());
    }
}