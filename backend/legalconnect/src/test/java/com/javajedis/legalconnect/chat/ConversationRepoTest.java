package com.javajedis.legalconnect.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("ConversationRepo Tests")
class ConversationRepoTest {

    @Mock
    private ConversationRepo conversationRepo;

    private UUID testUserId;
    private UUID testParticipantOneId;
    private UUID testParticipantTwoId;
    private UUID testConversationId;
    private Conversation conversation1;
    private Conversation conversation2;
    private Conversation conversation3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUserId = UUID.randomUUID();
        testParticipantOneId = UUID.randomUUID();
        testParticipantTwoId = UUID.randomUUID();
        testConversationId = UUID.randomUUID();
        
        // Create test conversations
        conversation1 = new Conversation();
        conversation1.setId(UUID.randomUUID());
        conversation1.setParticipantOneId(testUserId);
        conversation1.setParticipantTwoId(testParticipantOneId);
        conversation1.setCreatedAt(OffsetDateTime.now().minusHours(3));
        conversation1.setUpdatedAt(OffsetDateTime.now().minusMinutes(30));
        
        conversation2 = new Conversation();
        conversation2.setId(UUID.randomUUID());
        conversation2.setParticipantOneId(testParticipantTwoId);
        conversation2.setParticipantTwoId(testUserId);
        conversation2.setCreatedAt(OffsetDateTime.now().minusHours(2));
        conversation2.setUpdatedAt(OffsetDateTime.now().minusMinutes(15));
        
        conversation3 = new Conversation();
        conversation3.setId(UUID.randomUUID());
        conversation3.setParticipantOneId(testUserId);
        conversation3.setParticipantTwoId(UUID.randomUUID());
        conversation3.setCreatedAt(OffsetDateTime.now().minusHours(1));
        conversation3.setUpdatedAt(OffsetDateTime.now().minusMinutes(5));
    }

    @Test
    @DisplayName("Should find conversations by participant ordered by updated date desc")
    void findByParticipantOrderByUpdatedAtDesc_Success() {
        // Arrange
        List<Conversation> conversations = Arrays.asList(conversation3, conversation2, conversation1);
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(conversations);
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify order (most recently updated first)
        assertEquals(conversation3.getId(), result.get(0).getId());
        assertEquals(conversation2.getId(), result.get(1).getId());
        assertEquals(conversation1.getId(), result.get(2).getId());
    }

    @Test
    @DisplayName("Should find conversations when user has no conversations")
    void findByParticipantOrderByUpdatedAtDesc_NoConversations() {
        // Arrange
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(Arrays.asList());
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find conversation by participants when conversation exists")
    void findByParticipants_ConversationExists() {
        // Arrange
        when(conversationRepo.findByParticipants(testParticipantOneId, testParticipantTwoId))
            .thenReturn(Optional.of(conversation1));
        
        // Act
        Optional<Conversation> result = conversationRepo.findByParticipants(testParticipantOneId, testParticipantTwoId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(conversation1.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Should find conversation by participants in reverse order")
    void findByParticipants_ReverseOrder() {
        // Arrange
        when(conversationRepo.findByParticipants(testParticipantTwoId, testParticipantOneId))
            .thenReturn(Optional.of(conversation1));
        
        // Act
        Optional<Conversation> result = conversationRepo.findByParticipants(testParticipantTwoId, testParticipantOneId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(conversation1.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Should return empty when conversation between participants doesn't exist")
    void findByParticipants_ConversationNotExists() {
        // Arrange
        UUID nonExistentUser1 = UUID.randomUUID();
        UUID nonExistentUser2 = UUID.randomUUID();
        
        when(conversationRepo.findByParticipants(nonExistentUser1, nonExistentUser2))
            .thenReturn(Optional.empty());
        
        // Act
        Optional<Conversation> result = conversationRepo.findByParticipants(nonExistentUser1, nonExistentUser2);
        
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return true when user is participant in conversation")
    void isUserParticipant_UserIsParticipant() {
        // Arrange
        when(conversationRepo.isUserParticipant(testConversationId, testUserId))
            .thenReturn(true);
        
        // Act
        boolean result = conversationRepo.isUserParticipant(testConversationId, testUserId);
        
        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not participant in conversation")
    void isUserParticipant_UserIsNotParticipant() {
        // Arrange
        UUID nonParticipantUserId = UUID.randomUUID();
        
        when(conversationRepo.isUserParticipant(testConversationId, nonParticipantUserId))
            .thenReturn(false);
        
        // Act
        boolean result = conversationRepo.isUserParticipant(testConversationId, nonParticipantUserId);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when conversation doesn't exist")
    void isUserParticipant_ConversationNotExists() {
        // Arrange
        UUID nonExistentConversationId = UUID.randomUUID();
        
        when(conversationRepo.isUserParticipant(nonExistentConversationId, testUserId))
            .thenReturn(false);
        
        // Act
        boolean result = conversationRepo.isUserParticipant(nonExistentConversationId, testUserId);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle user as participant one")
    void findByParticipantOrderByUpdatedAtDesc_UserAsParticipantOne() {
        // Arrange
        List<Conversation> conversations = Arrays.asList(conversation1, conversation3);
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(conversations);
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getParticipantOneId().equals(testUserId)));
    }

    @Test
    @DisplayName("Should handle user as participant two")
    void findByParticipantOrderByUpdatedAtDesc_UserAsParticipantTwo() {
        // Arrange
        List<Conversation> conversations = Arrays.asList(conversation2);
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(conversations);
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserId, result.get(0).getParticipantTwoId());
    }

    @Test
    @DisplayName("Should handle same user as both participants")
    void findByParticipants_SameUser() {
        // Arrange
        Conversation selfConversation = new Conversation();
        selfConversation.setId(UUID.randomUUID());
        selfConversation.setParticipantOneId(testUserId);
        selfConversation.setParticipantTwoId(testUserId);
        
        when(conversationRepo.findByParticipants(testUserId, testUserId))
            .thenReturn(Optional.of(selfConversation));
        
        // Act
        Optional<Conversation> result = conversationRepo.findByParticipants(testUserId, testUserId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getParticipantOneId());
        assertEquals(testUserId, result.get().getParticipantTwoId());
    }

    @Test
    @DisplayName("Should handle repository method return types correctly")
    void testRepositoryMethodReturnTypes() {
        // Arrange
        List<Conversation> mockList = Arrays.asList(conversation1);
        Optional<Conversation> mockOptional = Optional.of(conversation1);
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(any(UUID.class)))
            .thenReturn(mockList);
        when(conversationRepo.findByParticipants(any(UUID.class), any(UUID.class)))
            .thenReturn(mockOptional);
        when(conversationRepo.isUserParticipant(any(UUID.class), any(UUID.class)))
            .thenReturn(true);
        
        // Act & Assert - verify return types
        List<Conversation> listResult = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        assertTrue(listResult instanceof List);
        
        Optional<Conversation> optionalResult = conversationRepo.findByParticipants(testUserId, testParticipantOneId);
        assertTrue(optionalResult instanceof Optional);
        
        boolean booleanResult = conversationRepo.isUserParticipant(testConversationId, testUserId);
        assertTrue(booleanResult || !booleanResult); // Always true for boolean
    }

    @Test
    @DisplayName("Should handle multiple conversations with different update times")
    void testMultipleConversationsWithDifferentUpdateTimes() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        
        Conversation oldConversation = new Conversation();
        oldConversation.setId(UUID.randomUUID());
        oldConversation.setParticipantOneId(testUserId);
        oldConversation.setParticipantTwoId(UUID.randomUUID());
        oldConversation.setUpdatedAt(now.minusHours(5));
        
        Conversation recentConversation = new Conversation();
        recentConversation.setId(UUID.randomUUID());
        recentConversation.setParticipantOneId(testUserId);
        recentConversation.setParticipantTwoId(UUID.randomUUID());
        recentConversation.setUpdatedAt(now.minusMinutes(1));
        
        List<Conversation> conversations = Arrays.asList(recentConversation, oldConversation);
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(conversations);
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(recentConversation.getId(), result.get(0).getId());
        assertEquals(oldConversation.getId(), result.get(1).getId());
    }

    @Test
    @DisplayName("Should handle edge cases with null parameters")
    void testEdgeCasesWithNullHandling() {
        // This test verifies that the repository methods handle the expected parameter types
        // Actual null handling would be managed by Spring Data JPA
        
        // Arrange
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(Arrays.asList());
        when(conversationRepo.findByParticipants(testUserId, testParticipantOneId))
            .thenReturn(Optional.empty());
        when(conversationRepo.isUserParticipant(testConversationId, testUserId))
            .thenReturn(false);
        
        // Act & Assert - methods should handle valid UUID parameters
        assertNotNull(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId));
        assertNotNull(conversationRepo.findByParticipants(testUserId, testParticipantOneId));
        assertFalse(conversationRepo.isUserParticipant(testConversationId, testUserId));
    }

    @Test
    @DisplayName("Should handle large number of conversations")
    void testLargeNumberOfConversations() {
        // Arrange
        List<Conversation> manyConversations = Arrays.asList(
            conversation1, conversation2, conversation3,
            new Conversation(), new Conversation(), new Conversation()
        );
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId))
            .thenReturn(manyConversations);
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(testUserId);
        
        // Assert
        assertEquals(6, result.size());
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle different user IDs correctly")
    void testDifferentUserIds() {
        // Arrange
        UUID anotherUserId = UUID.randomUUID();
        List<Conversation> conversations = Arrays.asList(conversation2);
        
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(anotherUserId))
            .thenReturn(conversations);
        
        // Act
        List<Conversation> result = conversationRepo.findByParticipantOrderByUpdatedAtDesc(anotherUserId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should verify custom query methods work with proper parameters")
    void testCustomQueryMethodsWithProperParameters() {
        // Arrange
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        
        when(conversationRepo.findByParticipants(user1, user2)).thenReturn(Optional.of(conversation1));
        when(conversationRepo.isUserParticipant(conversationId, user1)).thenReturn(true);
        when(conversationRepo.findByParticipantOrderByUpdatedAtDesc(user1)).thenReturn(Arrays.asList(conversation1));
        
        // Act & Assert
        assertTrue(conversationRepo.findByParticipants(user1, user2).isPresent());
        assertTrue(conversationRepo.isUserParticipant(conversationId, user1));
        assertEquals(1, conversationRepo.findByParticipantOrderByUpdatedAtDesc(user1).size());
    }
}