package com.javajedis.legalconnect.videocall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.payment.Payment;
import com.javajedis.legalconnect.payment.PaymentStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

@DisplayName("Meeting Entity Tests")
class MeetingTest {

    private Meeting meeting;
    private User client;
    private User lawyer;
    private Payment payment;
    private UUID testId;
    private UUID clientId;
    private UUID lawyerId;
    private UUID paymentId;
    private String testRoomName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        testRoomName = "meeting-room-" + UUID.randomUUID().toString();
        startTime = OffsetDateTime.now().plusHours(1);
        endTime = startTime.plusHours(1);
        
        // Setup test client
        client = new User();
        client.setId(clientId);
        client.setFirstName("John");
        client.setLastName("Client");
        client.setEmail("client@example.com");
        client.setRole(Role.USER);
        client.setEmailVerified(true);
        
        // Setup test lawyer
        lawyer = new User();
        lawyer.setId(lawyerId);
        lawyer.setFirstName("Jane");
        lawyer.setLastName("Lawyer");
        lawyer.setEmail("lawyer@example.com");
        lawyer.setRole(Role.LAWYER);
        lawyer.setEmailVerified(true);
        
        // Setup test payment
        payment = new Payment();
        payment.setId(paymentId);
        payment.setPayer(client);
        payment.setPayee(lawyer);
        payment.setStatus(PaymentStatus.PAID);
        
        // Setup test meeting
        meeting = new Meeting();
        meeting.setId(testId);
        meeting.setRoomName(testRoomName);
        meeting.setClient(client);
        meeting.setLawyer(lawyer);
        meeting.setStartTimestamp(startTime);
        meeting.setEndTimestamp(endTime);
        meeting.setPaid(true);
        meeting.setPayment(payment);
    }

    @Test
    @DisplayName("Should create meeting with default constructor")
    void testDefaultConstructor() {
        Meeting defaultMeeting = new Meeting();
        
        assertNotNull(defaultMeeting);
        assertNull(defaultMeeting.getId());
        assertNull(defaultMeeting.getRoomName());
        assertNull(defaultMeeting.getClient());
        assertNull(defaultMeeting.getLawyer());
        assertNull(defaultMeeting.getStartTimestamp());
        assertNull(defaultMeeting.getEndTimestamp());
        assertEquals(false, defaultMeeting.isPaid()); // Default boolean value
        assertNull(defaultMeeting.getPayment());
        assertNull(defaultMeeting.getCreatedAt());
        assertNull(defaultMeeting.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create meeting with all args constructor")
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime createdAt = now.minusHours(1);
        OffsetDateTime updatedAt = now;
        
        Meeting constructedMeeting = new Meeting(
            testId, testRoomName, client, lawyer, startTime, endTime,
            true, payment, createdAt, updatedAt
        );
        
        assertEquals(testId, constructedMeeting.getId());
        assertEquals(testRoomName, constructedMeeting.getRoomName());
        assertEquals(client, constructedMeeting.getClient());
        assertEquals(lawyer, constructedMeeting.getLawyer());
        assertEquals(startTime, constructedMeeting.getStartTimestamp());
        assertEquals(endTime, constructedMeeting.getEndTimestamp());
        assertEquals(true, constructedMeeting.isPaid());
        assertEquals(payment, constructedMeeting.getPayment());
        assertEquals(createdAt, constructedMeeting.getCreatedAt());
        assertEquals(updatedAt, constructedMeeting.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle id getter and setter")
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        meeting.setId(newId);
        assertEquals(newId, meeting.getId());
    }

    @Test
    @DisplayName("Should handle roomName getter and setter")
    void testRoomNameGetterAndSetter() {
        String newRoomName = "new-meeting-room-123";
        meeting.setRoomName(newRoomName);
        assertEquals(newRoomName, meeting.getRoomName());
    }

    @Test
    @DisplayName("Should handle roomName with null value")
    void testRoomNameWithNullValue() {
        meeting.setRoomName(null);
        assertNull(meeting.getRoomName());
    }

    @Test
    @DisplayName("Should handle roomName with empty string")
    void testRoomNameWithEmptyString() {
        meeting.setRoomName("");
        assertEquals("", meeting.getRoomName());
    }

    @Test
    @DisplayName("Should handle client getter and setter")
    void testClientGetterAndSetter() {
        User newClient = new User();
        newClient.setId(UUID.randomUUID());
        newClient.setFirstName("New");
        newClient.setLastName("Client");
        newClient.setEmail("newclient@example.com");
        newClient.setRole(Role.USER);
        
        meeting.setClient(newClient);
        assertEquals(newClient, meeting.getClient());
    }

    @Test
    @DisplayName("Should handle client with null value")
    void testClientWithNullValue() {
        meeting.setClient(null);
        assertNull(meeting.getClient());
    }

    @Test
    @DisplayName("Should handle lawyer getter and setter")
    void testLawyerGetterAndSetter() {
        User newLawyer = new User();
        newLawyer.setId(UUID.randomUUID());
        newLawyer.setFirstName("New");
        newLawyer.setLastName("Lawyer");
        newLawyer.setEmail("newlawyer@example.com");
        newLawyer.setRole(Role.LAWYER);
        
        meeting.setLawyer(newLawyer);
        assertEquals(newLawyer, meeting.getLawyer());
    }

    @Test
    @DisplayName("Should handle lawyer with null value")
    void testLawyerWithNullValue() {
        meeting.setLawyer(null);
        assertNull(meeting.getLawyer());
    }

    @Test
    @DisplayName("Should handle startTimestamp getter and setter")
    void testStartTimestampGetterAndSetter() {
        OffsetDateTime newStartTime = OffsetDateTime.now().plusDays(1);
        meeting.setStartTimestamp(newStartTime);
        assertEquals(newStartTime, meeting.getStartTimestamp());
    }

    @Test
    @DisplayName("Should handle startTimestamp with null value")
    void testStartTimestampWithNullValue() {
        meeting.setStartTimestamp(null);
        assertNull(meeting.getStartTimestamp());
    }

    @Test
    @DisplayName("Should handle endTimestamp getter and setter")
    void testEndTimestampGetterAndSetter() {
        OffsetDateTime newEndTime = OffsetDateTime.now().plusDays(1).plusHours(2);
        meeting.setEndTimestamp(newEndTime);
        assertEquals(newEndTime, meeting.getEndTimestamp());
    }

    @Test
    @DisplayName("Should handle endTimestamp with null value")
    void testEndTimestampWithNullValue() {
        meeting.setEndTimestamp(null);
        assertNull(meeting.getEndTimestamp());
    }

    @Test
    @DisplayName("Should handle isPaid getter and setter")
    void testIsPaidGetterAndSetter() {
        meeting.setPaid(false);
        assertEquals(false, meeting.isPaid());
        
        meeting.setPaid(true);
        assertEquals(true, meeting.isPaid());
    }

    @Test
    @DisplayName("Should handle payment getter and setter")
    void testPaymentGetterAndSetter() {
        Payment newPayment = new Payment();
        newPayment.setId(UUID.randomUUID());
        newPayment.setPayer(client);
        newPayment.setPayee(lawyer);
        newPayment.setStatus(PaymentStatus.PENDING);
        
        meeting.setPayment(newPayment);
        assertEquals(newPayment, meeting.getPayment());
    }

    @Test
    @DisplayName("Should handle payment with null value")
    void testPaymentWithNullValue() {
        meeting.setPayment(null);
        assertNull(meeting.getPayment());
    }

    @Test
    @DisplayName("Should handle createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        meeting.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, meeting.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle updatedAt getter and setter")
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        meeting.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, meeting.getUpdatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Meeting meeting1 = new Meeting();
        meeting1.setId(testId);
        meeting1.setRoomName(testRoomName);
        meeting1.setClient(client);
        meeting1.setLawyer(lawyer);
        meeting1.setStartTimestamp(startTime);
        meeting1.setEndTimestamp(endTime);
        meeting1.setPaid(true);
        meeting1.setPayment(payment);
        
        Meeting meeting2 = new Meeting();
        meeting2.setId(testId);
        meeting2.setRoomName(testRoomName);
        meeting2.setClient(client);
        meeting2.setLawyer(lawyer);
        meeting2.setStartTimestamp(startTime);
        meeting2.setEndTimestamp(endTime);
        meeting2.setPaid(true);
        meeting2.setPayment(payment);
        
        Meeting meeting3 = new Meeting();
        meeting3.setId(UUID.randomUUID());
        meeting3.setRoomName("different-room");
        meeting3.setClient(client);
        meeting3.setLawyer(lawyer);
        meeting3.setStartTimestamp(startTime.plusHours(1));
        meeting3.setEndTimestamp(endTime.plusHours(1));
        meeting3.setPaid(false);
        meeting3.setPayment(null);
        
        // Test equals
        assertEquals(meeting1, meeting2);
        assertNotEquals(meeting1, meeting3);
        assertNotEquals(null, meeting1);
        assertEquals(meeting1, meeting1);
        
        // Test hashCode
        assertEquals(meeting1.hashCode(), meeting2.hashCode());
        assertNotEquals(meeting1.hashCode(), meeting3.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with null values")
    void testEqualsWithNullValues() {
        Meeting meeting1 = new Meeting();
        Meeting meeting2 = new Meeting();
        
        assertEquals(meeting1, meeting2);
        assertEquals(meeting1.hashCode(), meeting2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        OffsetDateTime now = OffsetDateTime.now();
        meeting.setCreatedAt(now);
        meeting.setUpdatedAt(now);
        
        String toString = meeting.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("id=" + testId));
        assertTrue(toString.contains("roomName=" + testRoomName));
        assertTrue(toString.contains("isPaid=true"));
    }

    @Test
    @DisplayName("Should handle meeting with minimal data")
    void testMeetingWithMinimalData() {
        Meeting minimalMeeting = new Meeting();
        minimalMeeting.setRoomName("minimal-room");
        minimalMeeting.setClient(client);
        minimalMeeting.setLawyer(lawyer);
        minimalMeeting.setStartTimestamp(startTime);
        minimalMeeting.setEndTimestamp(endTime);
        
        assertEquals("minimal-room", minimalMeeting.getRoomName());
        assertEquals(client, minimalMeeting.getClient());
        assertEquals(lawyer, minimalMeeting.getLawyer());
        assertEquals(startTime, minimalMeeting.getStartTimestamp());
        assertEquals(endTime, minimalMeeting.getEndTimestamp());
        assertEquals(false, minimalMeeting.isPaid()); // Default value
        assertNull(minimalMeeting.getId()); // Not set
        assertNull(minimalMeeting.getPayment()); // Not set
    }

    @Test
    @DisplayName("Should handle meeting entity fields validation")
    void testMeetingEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(meeting.getId());
        assertNotNull(meeting.getRoomName());
        assertNotNull(meeting.getClient());
        assertNotNull(meeting.getLawyer());
        assertNotNull(meeting.getStartTimestamp());
        assertNotNull(meeting.getEndTimestamp());
        // isPaid has default value
        // payment can be null
    }

    @Test
    @DisplayName("Should handle meeting with various timestamps")
    void testMeetingWithVariousTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(2);
        OffsetDateTime future = now.plusHours(2);
        OffsetDateTime farFuture = now.plusDays(1);
        
        // Test with current time
        meeting.setStartTimestamp(now);
        meeting.setEndTimestamp(now.plusHours(1));
        meeting.setCreatedAt(now);
        meeting.setUpdatedAt(now);
        
        assertEquals(now, meeting.getStartTimestamp());
        assertEquals(now.plusHours(1), meeting.getEndTimestamp());
        assertEquals(now, meeting.getCreatedAt());
        assertEquals(now, meeting.getUpdatedAt());
        
        // Test with past time
        meeting.setStartTimestamp(past);
        meeting.setEndTimestamp(past.plusHours(1));
        meeting.setCreatedAt(past);
        
        assertEquals(past, meeting.getStartTimestamp());
        assertEquals(past.plusHours(1), meeting.getEndTimestamp());
        assertEquals(past, meeting.getCreatedAt());
        
        // Test with future time
        meeting.setStartTimestamp(future);
        meeting.setEndTimestamp(farFuture);
        meeting.setUpdatedAt(farFuture);
        
        assertEquals(future, meeting.getStartTimestamp());
        assertEquals(farFuture, meeting.getEndTimestamp());
        assertEquals(farFuture, meeting.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle meeting equality with different paid status")
    void testMeetingEqualityWithDifferentPaidStatus() {
        Meeting meeting1 = new Meeting();
        meeting1.setId(testId);
        meeting1.setRoomName(testRoomName);
        meeting1.setClient(client);
        meeting1.setLawyer(lawyer);
        meeting1.setStartTimestamp(startTime);
        meeting1.setEndTimestamp(endTime);
        meeting1.setPaid(true);
        
        Meeting meeting2 = new Meeting();
        meeting2.setId(testId);
        meeting2.setRoomName(testRoomName);
        meeting2.setClient(client);
        meeting2.setLawyer(lawyer);
        meeting2.setStartTimestamp(startTime);
        meeting2.setEndTimestamp(endTime);
        meeting2.setPaid(false); // Different paid status
        
        // With Lombok @Data, objects with different field values are not equal
        assertNotEquals(meeting1, meeting2);
    }

    @Test
    @DisplayName("Should handle meeting relationships")
    void testMeetingRelationships() {
        // Test that meeting correctly maintains relationships with users and payment
        assertEquals(client, meeting.getClient());
        assertEquals(lawyer, meeting.getLawyer());
        assertEquals(payment, meeting.getPayment());
        
        // Test that changing user properties doesn't affect meeting
        String originalClientEmail = client.getEmail();
        client.setEmail("newemail@example.com");
        
        assertEquals(client, meeting.getClient());
        assertEquals("newemail@example.com", meeting.getClient().getEmail());
        
        // Restore original email
        client.setEmail(originalClientEmail);
    }

    @Test
    @DisplayName("Should handle constructor with null values")
    void testConstructorWithNullValues() {
        Meeting constructedMeeting = new Meeting(
            testId, testRoomName, null, null, startTime, endTime,
            false, null, null, null
        );
        
        assertEquals(testId, constructedMeeting.getId());
        assertEquals(testRoomName, constructedMeeting.getRoomName());
        assertNull(constructedMeeting.getClient());
        assertNull(constructedMeeting.getLawyer());
        assertEquals(startTime, constructedMeeting.getStartTimestamp());
        assertEquals(endTime, constructedMeeting.getEndTimestamp());
        assertEquals(false, constructedMeeting.isPaid());
        assertNull(constructedMeeting.getPayment());
        assertNull(constructedMeeting.getCreatedAt());
        assertNull(constructedMeeting.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle roomName edge cases")
    void testRoomNameEdgeCases() {
        // Test with whitespace-only content
        meeting.setRoomName("   ");
        assertEquals("   ", meeting.getRoomName());
        
        // Test with special characters
        meeting.setRoomName("room-123_456@789");
        assertEquals("room-123_456@789", meeting.getRoomName());
        
        // Test with long room name
        String longRoomName = "room-" + "a".repeat(100);
        meeting.setRoomName(longRoomName);
        assertEquals(longRoomName, meeting.getRoomName());
    }

    @Test
    @DisplayName("Should handle default isPaid value")
    void testDefaultIsPaidValue() {
        Meeting newMeeting = new Meeting();
        assertEquals(false, newMeeting.isPaid());
    }

    @Test
    @DisplayName("Should handle meeting with same client and lawyer")
    void testMeetingWithSameClientAndLawyer() {
        // This is an edge case that might not be valid in business logic
        // but the entity should handle it
        meeting.setClient(client);
        meeting.setLawyer(client); // Same user as both client and lawyer
        
        assertEquals(client, meeting.getClient());
        assertEquals(client, meeting.getLawyer());
        assertEquals(meeting.getClient(), meeting.getLawyer());
    }

    @Test
    @DisplayName("Should handle meeting time edge cases")
    void testMeetingTimeEdgeCases() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Test with same start and end time
        meeting.setStartTimestamp(now);
        meeting.setEndTimestamp(now);
        
        assertEquals(now, meeting.getStartTimestamp());
        assertEquals(now, meeting.getEndTimestamp());
        assertEquals(meeting.getStartTimestamp(), meeting.getEndTimestamp());
        
        // Test with end time before start time (invalid but entity should handle)
        meeting.setStartTimestamp(now);
        meeting.setEndTimestamp(now.minusHours(1));
        
        assertEquals(now, meeting.getStartTimestamp());
        assertEquals(now.minusHours(1), meeting.getEndTimestamp());
        assertTrue(meeting.getEndTimestamp().isBefore(meeting.getStartTimestamp()));
    }

    @Test
    @DisplayName("Should handle payment relationship changes")
    void testPaymentRelationshipChanges() {
        // Test changing payment
        Payment newPayment = new Payment();
        newPayment.setId(UUID.randomUUID());
        newPayment.setPayer(client);
        newPayment.setPayee(lawyer);
        newPayment.setStatus(PaymentStatus.PENDING);
        
        meeting.setPayment(newPayment);
        assertEquals(newPayment, meeting.getPayment());
        assertNotEquals(payment, meeting.getPayment());
        
        // Test removing payment
        meeting.setPayment(null);
        assertNull(meeting.getPayment());
    }

    @Test
    @DisplayName("Should handle meeting with different user roles")
    void testMeetingWithDifferentUserRoles() {
        // Create users with different roles
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        
        // Test setting admin as client (might not be valid in business logic)
        meeting.setClient(adminUser);
        assertEquals(adminUser, meeting.getClient());
        assertEquals(Role.ADMIN, meeting.getClient().getRole());
        
        // Test setting admin as lawyer
        meeting.setLawyer(adminUser);
        assertEquals(adminUser, meeting.getLawyer());
        assertEquals(Role.ADMIN, meeting.getLawyer().getRole());
    }
}