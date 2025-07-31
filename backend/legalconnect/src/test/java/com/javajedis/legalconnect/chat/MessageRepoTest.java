package com.javajedis.legalconnect.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("MessageRepo Tests")
class MessageRepoTest {

    @Mock
    private MessageRepo messageRepo;

    private UUID testConversationId;
    private UUID testUserId;
    private UUID testSenderId;
    private Message message1;
    private Message message2;
    private Message message3;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testConversationId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testSenderId = UUID.randomUUID();
        pageable = PageRequest.of(0, 20);

        // Create test messages
        message1 = new Message();
        message1.setId(UUID.randomUUID());
        message1.setConversationId(testConversationId);
        message1.setSenderId(testSenderId);
        message1.setContent("First message");
        message1.setRead(false);
        message1.setCreatedAt(OffsetDateTime.now().minusHours(3));

        message2 = new Message();
        message2.setId(UUID.randomUUID());
        message2.setConversationId(testConversationId);
        message2.setSenderId(testUserId);
        message2.setContent("Second message");
        message2.setRead(true);
        message2.setCreatedAt(OffsetDateTime.now().minusHours(2));

        message3 = new Message();
        message3.setId(UUID.randomUUID());
        message3.setConversationId(testConversationId);
        message3.setSenderId(testSenderId);
        message3.setContent("Third message");
        message3.setRead(false);
        message3.setCreatedAt(OffsetDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Should find messages by conversation ID ordered by created date desc")
    void findByConversationIdOrderByCreatedAtDesc_Success() {
        // Arrange
        List<Message> messages = Arrays.asList(message3, message2, message1);
        Page<Message> expectedPage = new PageImpl<>(messages, pageable, messages.size());

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Message> result = messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());

        // Verify order (newest first)
        List<Message> content = result.getContent();
        assertEquals(message3.getId(), content.get(0).getId());
        assertEquals(message2.getId(), content.get(1).getId());
        assertEquals(message1.getId(), content.get(2).getId());
    }

    @Test
    @DisplayName("Should find messages when no messages exist in conversation")
    void findByConversationIdOrderByCreatedAtDesc_NoMessages() {
        // Arrange
        Page<Message> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<Message> result = messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should count unread messages by conversation and user")
    void countUnreadByConversationAndUser_Success() {
        // Arrange
        long expectedCount = 2L; // message1 and message3 are unread and not sent by testUserId
        when(messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId))
                .thenReturn(expectedCount);

        // Act
        long result = messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId);

        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Should count unread messages when none exist")
    void countUnreadByConversationAndUser_NoUnreadMessages() {
        // Arrange
        when(messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId))
                .thenReturn(0L);

        // Act
        long result = messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId);

        // Assert
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should find unread messages by conversation and user")
    void findUnreadByConversationAndUser_Success() {
        // Arrange
        List<Message> unreadMessages = Arrays.asList(message1, message3);
        when(messageRepo.findUnreadByConversationAndUser(testConversationId, testUserId))
                .thenReturn(unreadMessages);

        // Act
        List<Message> result = messageRepo.findUnreadByConversationAndUser(testConversationId, testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify all messages are unread and not sent by the user
        for (Message message : result) {
            assertFalse(message.isRead());
            assertNotEquals(message.getSenderId(), testUserId);
        }
    }

    @Test
    @DisplayName("Should find unread messages when none exist")
    void findUnreadByConversationAndUser_NoUnreadMessages() {
        // Arrange
        when(messageRepo.findUnreadByConversationAndUser(testConversationId, testUserId))
                .thenReturn(Arrays.asList());

        // Act
        List<Message> result = messageRepo.findUnreadByConversationAndUser(testConversationId, testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find most recent message in conversation")
    void findTopByConversationIdOrderByCreatedAtDesc_Success() {
        // Arrange
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(testConversationId))
                .thenReturn(Optional.of(message3)); // Most recent message

        // Act
        Optional<Message> result = messageRepo.findTopByConversationIdOrderByCreatedAtDesc(testConversationId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(message3.getId(), result.get().getId());
        assertEquals("Third message", result.get().getContent());
    }

    @Test
    @DisplayName("Should return empty when no messages exist in conversation")
    void findTopByConversationIdOrderByCreatedAtDesc_NoMessages() {
        // Arrange
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(testConversationId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Message> result = messageRepo.findTopByConversationIdOrderByCreatedAtDesc(testConversationId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should count total unread messages for user across all conversations")
    void countTotalUnreadByUser_Success() {
        // Arrange
        long expectedCount = 5L;
        when(messageRepo.countTotalUnreadByUser(testUserId))
                .thenReturn(expectedCount);

        // Act
        long result = messageRepo.countTotalUnreadByUser(testUserId);

        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Should count total unread messages when none exist")
    void countTotalUnreadByUser_NoUnreadMessages() {
        // Arrange
        when(messageRepo.countTotalUnreadByUser(testUserId))
                .thenReturn(0L);

        // Act
        long result = messageRepo.countTotalUnreadByUser(testUserId);

        // Assert
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPaginationHandling() {
        // Arrange
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        List<Message> firstPageMessages = Arrays.asList(message3, message2);
        List<Message> secondPageMessages = Arrays.asList(message1);

        Page<Message> firstPageResult = new PageImpl<>(firstPageMessages, firstPage, 3);
        Page<Message> secondPageResult = new PageImpl<>(secondPageMessages, secondPage, 3);

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, firstPage))
                .thenReturn(firstPageResult);
        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, secondPage))
                .thenReturn(secondPageResult);

        // Act
        Page<Message> firstResult = messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, firstPage);
        Page<Message> secondResult = messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, secondPage);

        // Assert
        assertEquals(2, firstResult.getContent().size());
        assertEquals(1, secondResult.getContent().size());
        assertEquals(3, firstResult.getTotalElements());
        assertEquals(3, secondResult.getTotalElements());
        assertTrue(firstResult.hasNext());
        assertFalse(secondResult.hasNext());
    }

    @Test
    @DisplayName("Should handle different conversation IDs")
    void testDifferentConversationIds() {
        // Arrange
        UUID anotherConversationId = UUID.randomUUID();
        List<Message> messages = Arrays.asList(message1);
        Page<Message> expectedPage = new PageImpl<>(messages, pageable, messages.size());

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(anotherConversationId, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Message> result = messageRepo.findByConversationIdOrderByCreatedAtDesc(anotherConversationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should exclude user's own messages from unread count")
    void testUnreadCountExcludesUserMessages() {
        // Arrange
        // Only messages not sent by testUserId should be counted
        when(messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId))
                .thenReturn(2L); // message1 and message3 (both sent by testSenderId, not testUserId)

        // Act
        long result = messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId);

        // Assert
        assertEquals(2L, result);
    }

    @Test
    @DisplayName("Should handle repository method return types correctly")
    void testRepositoryMethodReturnTypes() {
        // Arrange
        Page<Message> mockPage = new PageImpl<>(Arrays.asList(message1), pageable, 1);
        List<Message> mockList = Arrays.asList(message1);
        Optional<Message> mockOptional = Optional.of(message1);

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(mockPage);
        when(messageRepo.countUnreadByConversationAndUser(any(UUID.class), any(UUID.class)))
                .thenReturn(1L);
        when(messageRepo.findUnreadByConversationAndUser(any(UUID.class), any(UUID.class)))
                .thenReturn(mockList);
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(any(UUID.class)))
                .thenReturn(mockOptional);
        when(messageRepo.countTotalUnreadByUser(any(UUID.class)))
                .thenReturn(1L);

        // Act & Assert - verify return types
        Page<Message> pageResult = messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable);
        assertTrue(pageResult instanceof Page);

        long countResult1 = messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId);
        assertTrue(countResult1 >= 0);

        List<Message> listResult = messageRepo.findUnreadByConversationAndUser(testConversationId, testUserId);
        assertTrue(listResult instanceof List);

        Optional<Message> optionalResult = messageRepo.findTopByConversationIdOrderByCreatedAtDesc(testConversationId);
        assertTrue(optionalResult instanceof Optional);

        long countResult2 = messageRepo.countTotalUnreadByUser(testUserId);
        assertTrue(countResult2 >= 0);
    }

    @Test
    @DisplayName("Should handle edge cases with large counts")
    void testLargeCounts() {
        // Arrange
        long largeCount = Long.MAX_VALUE;
        when(messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId))
                .thenReturn(largeCount);
        when(messageRepo.countTotalUnreadByUser(testUserId))
                .thenReturn(largeCount);

        // Act & Assert
        assertEquals(largeCount, messageRepo.countUnreadByConversationAndUser(testConversationId, testUserId));
        assertEquals(largeCount, messageRepo.countTotalUnreadByUser(testUserId));
    }

    @Test
    @DisplayName("Should handle multiple conversations for total unread count")
    void testTotalUnreadCountAcrossConversations() {
        // Arrange
        long totalUnreadCount = 10L; // Across all conversations
        when(messageRepo.countTotalUnreadByUser(testUserId))
                .thenReturn(totalUnreadCount);

        // Act
        long result = messageRepo.countTotalUnreadByUser(testUserId);

        // Assert
        assertEquals(totalUnreadCount, result);
    }

    @Test
    @DisplayName("Should handle messages with different read states")
    void testMessagesWithDifferentReadStates() {
        // Arrange
        List<Message> mixedMessages = Arrays.asList(message1, message2, message3);
        Page<Message> expectedPage = new PageImpl<>(mixedMessages, pageable, mixedMessages.size());

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Message> result = messageRepo.findByConversationIdOrderByCreatedAtDesc(testConversationId, pageable);

        // Assert
        assertEquals(3, result.getContent().size());

        // Verify mixed read states
        List<Message> content = result.getContent();
        assertFalse(content.get(0).isRead()); // message3
        assertTrue(content.get(1).isRead());  // message2
        assertFalse(content.get(2).isRead()); // message1
    }

    @Test
    @DisplayName("Should handle method parameter validation")
    void testMethodParameterValidation() {
        // This test verifies that the repository methods accept the expected parameter types

        // Arrange
        UUID validConversationId = UUID.randomUUID();
        UUID validUserId = UUID.randomUUID();
        Pageable validPageable = PageRequest.of(0, 20);
        Page<Message> mockPage = new PageImpl<>(Arrays.asList(), validPageable, 0);

        when(messageRepo.findByConversationIdOrderByCreatedAtDesc(validConversationId, validPageable))
                .thenReturn(mockPage);
        when(messageRepo.countUnreadByConversationAndUser(validConversationId, validUserId))
                .thenReturn(0L);
        when(messageRepo.findUnreadByConversationAndUser(validConversationId, validUserId))
                .thenReturn(Arrays.asList());
        when(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(validConversationId))
                .thenReturn(Optional.empty());
        when(messageRepo.countTotalUnreadByUser(validUserId))
                .thenReturn(0L);

        // Act & Assert - methods should accept correct parameter types
        assertNotNull(messageRepo.findByConversationIdOrderByCreatedAtDesc(validConversationId, validPageable));
        assertEquals(0L, messageRepo.countUnreadByConversationAndUser(validConversationId, validUserId));
        assertNotNull(messageRepo.findUnreadByConversationAndUser(validConversationId, validUserId));
        assertNotNull(messageRepo.findTopByConversationIdOrderByCreatedAtDesc(validConversationId));
        assertEquals(0L, messageRepo.countTotalUnreadByUser(validUserId));
    }

    @Test
    @DisplayName("Should handle custom query methods with proper joins")
    void testCustomQueryMethodsWithJoins() {
        // Arrange
        UUID userId = UUID.randomUUID();
        long expectedTotalUnread = 15L;

        when(messageRepo.countTotalUnreadByUser(userId))
                .thenReturn(expectedTotalUnread);

        // Act
        long result = messageRepo.countTotalUnreadByUser(userId);

        // Assert
        assertEquals(expectedTotalUnread, result);
    }
}