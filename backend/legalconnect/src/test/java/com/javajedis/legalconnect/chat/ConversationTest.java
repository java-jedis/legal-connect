package com.javajedis.legalconnect.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Conversation Entity Tests")
class ConversationTest {

    private Conversation conversation;
    private UUID testConversationId;
    private UUID testParticipantOneId;
    private UUID testParticipantTwoId;
    private OffsetDateTime testCreatedAt;
    private OffsetDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        testConversationId = UUID.randomUUID();
        testParticipantOneId = UUID.randomUUID();
        testParticipantTwoId = UUID.randomUUID();
        testCreatedAt = OffsetDateTime.now();
        testUpdatedAt = OffsetDateTime.now();

        conversation = new Conversation();
        conversation.setId(testConversationId);
        conversation.setParticipantOneId(testParticipantOneId);
        conversation.setParticipantTwoId(testParticipantTwoId);
        conversation.setCreatedAt(testCreatedAt);
        conversation.setUpdatedAt(testUpdatedAt);
    }

    @Test
    @DisplayName("Should create conversation with default constructor")
    void testDefaultConstructor() {
        Conversation defaultConversation = new Conversation();

        assertNotNull(defaultConversation);
        assertNull(defaultConversation.getId());
        assertNull(defaultConversation.getParticipantOneId());
        assertNull(defaultConversation.getParticipantTwoId());
        assertNull(defaultConversation.getCreatedAt());
        assertNull(defaultConversation.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create conversation with all args constructor")
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime updated = now.plusMinutes(5);
        
        Conversation constructedConversation = new Conversation(
            testConversationId, testParticipantOneId, testParticipantTwoId, now, updated
        );

        assertEquals(testConversationId, constructedConversation.getId());
        assertEquals(testParticipantOneId, constructedConversation.getParticipantOneId());
        assertEquals(testParticipantTwoId, constructedConversation.getParticipantTwoId());
        assertEquals(now, constructedConversation.getCreatedAt());
        assertEquals(updated, constructedConversation.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle id getter and setter")
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        conversation.setId(newId);
        assertEquals(newId, conversation.getId());
    }

    @Test
    @DisplayName("Should handle participantOneId getter and setter")
    void testParticipantOneIdGetterAndSetter() {
        UUID newParticipantId = UUID.randomUUID();
        conversation.setParticipantOneId(newParticipantId);
        assertEquals(newParticipantId, conversation.getParticipantOneId());
    }

    @Test
    @DisplayName("Should handle participantTwoId getter and setter")
    void testParticipantTwoIdGetterAndSetter() {
        UUID newParticipantId = UUID.randomUUID();
        conversation.setParticipantTwoId(newParticipantId);
        assertEquals(newParticipantId, conversation.getParticipantTwoId());
    }

    @Test
    @DisplayName("Should handle createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        conversation.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, conversation.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle updatedAt getter and setter")
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now().plusHours(1);
        conversation.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, conversation.getUpdatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Conversation conversation1 = new Conversation();
        conversation1.setId(testConversationId);
        conversation1.setParticipantOneId(testParticipantOneId);
        conversation1.setParticipantTwoId(testParticipantTwoId);

        Conversation conversation2 = new Conversation();
        conversation2.setId(testConversationId);
        conversation2.setParticipantOneId(testParticipantOneId);
        conversation2.setParticipantTwoId(testParticipantTwoId);

        Conversation conversation3 = new Conversation();
        conversation3.setId(UUID.randomUUID());
        conversation3.setParticipantOneId(UUID.randomUUID());
        conversation3.setParticipantTwoId(UUID.randomUUID());

        assertEquals(conversation1, conversation2);
        assertEquals(conversation1.hashCode(), conversation2.hashCode());
        assertNotEquals(conversation1, conversation3);
        assertNotEquals(conversation1.hashCode(), conversation3.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with null values")
    void testEqualsWithNullValues() {
        Conversation conversation1 = new Conversation();
        Conversation conversation2 = new Conversation();

        assertEquals(conversation1, conversation2);
        assertEquals(conversation1.hashCode(), conversation2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        String toString = conversation.toString();

        assertNotNull(toString);
        assertTrue(toString.contains(testParticipantOneId.toString()));
        assertTrue(toString.contains(testParticipantTwoId.toString()));
    }

    @Test
    @DisplayName("Should handle conversation with same participants in different order")
    void testConversationWithSameParticipants() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Conversation conversation1 = new Conversation();
        conversation1.setParticipantOneId(participant1);
        conversation1.setParticipantTwoId(participant2);

        Conversation conversation2 = new Conversation();
        conversation2.setParticipantOneId(participant2);
        conversation2.setParticipantTwoId(participant1);

        // These should be different objects since the participant order matters in the entity
        assertNotEquals(conversation1, conversation2);
    }

    @Test
    @DisplayName("Should handle conversation with null participant IDs")
    void testConversationWithNullParticipantIds() {
        conversation.setParticipantOneId(null);
        conversation.setParticipantTwoId(null);
        
        assertNull(conversation.getParticipantOneId());
        assertNull(conversation.getParticipantTwoId());
    }

    @Test
    @DisplayName("Should handle conversation timestamps")
    void testConversationWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(1);
        OffsetDateTime future = now.plusHours(1);

        // Test with current time
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(future);
        assertEquals(now, conversation.getCreatedAt());
        assertEquals(future, conversation.getUpdatedAt());

        // Test with past time
        conversation.setCreatedAt(past);
        assertEquals(past, conversation.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle conversation entity fields validation")
    void testConversationEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(conversation.getId());
        assertNotNull(conversation.getParticipantOneId());
        assertNotNull(conversation.getParticipantTwoId());
        assertNotNull(conversation.getCreatedAt());
        assertNotNull(conversation.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle conversation with minimal data")
    void testConversationWithMinimalData() {
        Conversation minimalConversation = new Conversation();
        minimalConversation.setParticipantOneId(testParticipantOneId);
        minimalConversation.setParticipantTwoId(testParticipantTwoId);

        assertEquals(testParticipantOneId, minimalConversation.getParticipantOneId());
        assertEquals(testParticipantTwoId, minimalConversation.getParticipantTwoId());
        assertNull(minimalConversation.getId()); // Not set
        assertNull(minimalConversation.getCreatedAt()); // Not set
        assertNull(minimalConversation.getUpdatedAt()); // Not set
    }

    @Test
    @DisplayName("Should handle conversation equality with different timestamps")
    void testConversationEqualityWithDifferentTimestamps() {
        Conversation conversation1 = new Conversation();
        conversation1.setId(testConversationId);
        conversation1.setParticipantOneId(testParticipantOneId);
        conversation1.setParticipantTwoId(testParticipantTwoId);
        conversation1.setCreatedAt(testCreatedAt);
        conversation1.setUpdatedAt(testUpdatedAt);

        Conversation conversation2 = new Conversation();
        conversation2.setId(testConversationId);
        conversation2.setParticipantOneId(testParticipantOneId);
        conversation2.setParticipantTwoId(testParticipantTwoId);
        conversation2.setCreatedAt(testCreatedAt.plusMinutes(1)); // Different timestamp
        conversation2.setUpdatedAt(testUpdatedAt.plusMinutes(1)); // Different timestamp

        // With Lombok @Data, objects with different field values are not equal
        assertNotEquals(conversation1, conversation2);
    }

    private void assertTrue(boolean contains) {
        org.junit.jupiter.api.Assertions.assertTrue(contains);
    }
}