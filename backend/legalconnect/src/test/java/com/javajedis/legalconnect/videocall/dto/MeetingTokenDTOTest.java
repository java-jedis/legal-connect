package com.javajedis.legalconnect.videocall.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MeetingTokenDTO Tests")
class MeetingTokenDTOTest {

    private MeetingTokenDTO meetingTokenDTO;
    private UUID testMeetingId;
    private String testRoomName;
    private String testJwtToken;

    @BeforeEach
    void setUp() {
        meetingTokenDTO = new MeetingTokenDTO();
        testMeetingId = UUID.randomUUID();
        testRoomName = "test-room-123";
        testJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        MeetingTokenDTO dto = new MeetingTokenDTO();
        assertNotNull(dto);
        assertNull(dto.getMeetingId());
        assertNull(dto.getRoomName());
        assertNull(dto.getJwtToken());
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        MeetingTokenDTO dto = new MeetingTokenDTO(testMeetingId, testRoomName, testJwtToken);
        
        assertEquals(testMeetingId, dto.getMeetingId());
        assertEquals(testRoomName, dto.getRoomName());
        assertEquals(testJwtToken, dto.getJwtToken());
    }

    @Test
    @DisplayName("Should set and get meeting ID")
    void testMeetingIdGetterSetter() {
        meetingTokenDTO.setMeetingId(testMeetingId);
        assertEquals(testMeetingId, meetingTokenDTO.getMeetingId());
    }

    @Test
    @DisplayName("Should set and get room name")
    void testRoomNameGetterSetter() {
        meetingTokenDTO.setRoomName(testRoomName);
        assertEquals(testRoomName, meetingTokenDTO.getRoomName());
    }

    @Test
    @DisplayName("Should set and get JWT token")
    void testJwtTokenGetterSetter() {
        meetingTokenDTO.setJwtToken(testJwtToken);
        assertEquals(testJwtToken, meetingTokenDTO.getJwtToken());
    }

    @Test
    @DisplayName("Should handle null values for all fields")
    void testNullValues() {
        meetingTokenDTO.setMeetingId(null);
        meetingTokenDTO.setRoomName(null);
        meetingTokenDTO.setJwtToken(null);

        assertNull(meetingTokenDTO.getMeetingId());
        assertNull(meetingTokenDTO.getRoomName());
        assertNull(meetingTokenDTO.getJwtToken());
    }

    @Test
    @DisplayName("Should handle different UUID values")
    void testDifferentUUIDValues() {
        UUID[] uuids = {
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        };

        for (UUID uuid : uuids) {
            meetingTokenDTO.setMeetingId(uuid);
            assertEquals(uuid, meetingTokenDTO.getMeetingId());
        }
    }

    @Test
    @DisplayName("Should handle different room name values")
    void testDifferentRoomNameValues() {
        String[] roomNames = {
            "",
            "A",
            "room-123",
            "meeting-room-with-long-name",
            "ROOM_WITH_UNDERSCORES_AND_CAPS",
            "room.with.dots",
            "room@with#special$chars"
        };

        for (String roomName : roomNames) {
            meetingTokenDTO.setRoomName(roomName);
            assertEquals(roomName, meetingTokenDTO.getRoomName());
        }
    }

    @Test
    @DisplayName("Should handle different JWT token formats")
    void testDifferentJwtTokenValues() {
        String[] jwtTokens = {
            "",
            "simple-token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJqaXRzaSIsImlzcyI6ImxlZ2FsY29ubmVjdCIsInN1YiI6ImxlZ2FsY29ubmVjdCIsInJvb20iOiJ0ZXN0LXJvb20iLCJleHAiOjE2MzQ1Njc4OTB9",
            "VERY_LONG_TOKEN_STRING_WITH_MANY_CHARACTERS_THAT_COULD_BE_A_VALID_JWT_TOKEN_IN_SOME_CASES_12345678901234567890"
        };

        for (String jwtToken : jwtTokens) {
            meetingTokenDTO.setJwtToken(jwtToken);
            assertEquals(jwtToken, meetingTokenDTO.getJwtToken());
        }
    }

    @Test
    @DisplayName("Should handle JWT token with special characters")
    void testJwtTokenWithSpecialCharacters() {
        String tokenWithSpecialChars = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        meetingTokenDTO.setJwtToken(tokenWithSpecialChars);
        assertEquals(tokenWithSpecialChars, meetingTokenDTO.getJwtToken());
        
        // Verify the token contains expected JWT characteristics
        assertTrue(meetingTokenDTO.getJwtToken().contains("."));
        assertTrue(meetingTokenDTO.getJwtToken().split("\\.").length >= 2);
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testEmptyStringValues() {
        meetingTokenDTO.setRoomName("");
        meetingTokenDTO.setJwtToken("");

        assertEquals("", meetingTokenDTO.getRoomName());
        assertEquals("", meetingTokenDTO.getJwtToken());
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void testDataIntegrity() {
        // Set initial values
        meetingTokenDTO.setMeetingId(testMeetingId);
        meetingTokenDTO.setRoomName(testRoomName);
        meetingTokenDTO.setJwtToken(testJwtToken);

        // Verify initial values
        assertEquals(testMeetingId, meetingTokenDTO.getMeetingId());
        assertEquals(testRoomName, meetingTokenDTO.getRoomName());
        assertEquals(testJwtToken, meetingTokenDTO.getJwtToken());

        // Change values
        UUID newMeetingId = UUID.randomUUID();
        String newRoomName = "new-room-456";
        String newJwtToken = "new.jwt.token";

        meetingTokenDTO.setMeetingId(newMeetingId);
        meetingTokenDTO.setRoomName(newRoomName);
        meetingTokenDTO.setJwtToken(newJwtToken);

        // Verify new values
        assertEquals(newMeetingId, meetingTokenDTO.getMeetingId());
        assertEquals(newRoomName, meetingTokenDTO.getRoomName());
        assertEquals(newJwtToken, meetingTokenDTO.getJwtToken());
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        MeetingTokenDTO dto1 = new MeetingTokenDTO(testMeetingId, testRoomName, testJwtToken);
        MeetingTokenDTO dto2 = new MeetingTokenDTO(testMeetingId, testRoomName, testJwtToken);
        MeetingTokenDTO dto3 = new MeetingTokenDTO(UUID.randomUUID(), testRoomName, testJwtToken);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        meetingTokenDTO.setMeetingId(testMeetingId);
        meetingTokenDTO.setRoomName(testRoomName);
        meetingTokenDTO.setJwtToken(testJwtToken);

        String toString = meetingTokenDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("MeetingTokenDTO"));
        assertTrue(toString.contains(testMeetingId.toString()));
        assertTrue(toString.contains(testRoomName));
        assertTrue(toString.contains(testJwtToken));
    }

    @Test
    @DisplayName("Should support data transfer functionality")
    void testDataTransferBehavior() {
        // Simulate data transfer by setting all fields and verifying they're preserved
        MeetingTokenDTO sourceDTO = new MeetingTokenDTO();
        sourceDTO.setMeetingId(testMeetingId);
        sourceDTO.setRoomName(testRoomName);
        sourceDTO.setJwtToken(testJwtToken);

        // Create target DTO using all-args constructor (simulating mapping)
        MeetingTokenDTO targetDTO = new MeetingTokenDTO(
            sourceDTO.getMeetingId(),
            sourceDTO.getRoomName(),
            sourceDTO.getJwtToken()
        );

        // Verify all data was transferred correctly
        assertEquals(sourceDTO.getMeetingId(), targetDTO.getMeetingId());
        assertEquals(sourceDTO.getRoomName(), targetDTO.getRoomName());
        assertEquals(sourceDTO.getJwtToken(), targetDTO.getJwtToken());
    }

    @Test
    @DisplayName("Should handle field mapping consistency")
    void testFieldMappingConsistency() {
        // Test that all fields can be set and retrieved consistently
        meetingTokenDTO.setMeetingId(testMeetingId);
        meetingTokenDTO.setRoomName(testRoomName);
        meetingTokenDTO.setJwtToken(testJwtToken);

        // Verify all fields maintain their values
        assertEquals(testMeetingId, meetingTokenDTO.getMeetingId());
        assertEquals(testRoomName, meetingTokenDTO.getRoomName());
        assertEquals(testJwtToken, meetingTokenDTO.getJwtToken());
    }

    @Test
    @DisplayName("Should handle token-specific edge cases")
    void testTokenSpecificEdgeCases() {
        // Test with malformed JWT-like strings
        String[] edgeCaseTokens = {
            ".",
            "..",
            "...",
            "header.",
            ".payload",
            "header.payload.",
            ".payload.signature",
            "header..signature"
        };

        for (String token : edgeCaseTokens) {
            meetingTokenDTO.setJwtToken(token);
            assertEquals(token, meetingTokenDTO.getJwtToken());
        }
    }

    @Test
    @DisplayName("Should handle very long token values")
    void testVeryLongTokenValues() {
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longToken.append("a");
        }
        String veryLongToken = longToken.toString();

        meetingTokenDTO.setJwtToken(veryLongToken);
        assertEquals(veryLongToken, meetingTokenDTO.getJwtToken());
        assertEquals(1000, meetingTokenDTO.getJwtToken().length());
    }

    @Test
    @DisplayName("Should handle UUID edge cases")
    void testUUIDEdgeCases() {
        // Test with different UUID formats
        UUID zeroUUID = new UUID(0L, 0L);
        UUID maxUUID = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
        UUID minUUID = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

        meetingTokenDTO.setMeetingId(zeroUUID);
        assertEquals(zeroUUID, meetingTokenDTO.getMeetingId());

        meetingTokenDTO.setMeetingId(maxUUID);
        assertEquals(maxUUID, meetingTokenDTO.getMeetingId());

        meetingTokenDTO.setMeetingId(minUUID);
        assertEquals(minUUID, meetingTokenDTO.getMeetingId());
    }
}