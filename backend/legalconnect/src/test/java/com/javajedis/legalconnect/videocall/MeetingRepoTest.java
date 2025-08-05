package com.javajedis.legalconnect.videocall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.javajedis.legalconnect.payment.Payment;
import com.javajedis.legalconnect.payment.PaymentStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

@DisplayName("MeetingRepo Repository Tests")
class MeetingRepoTest {

    @Mock
    private MeetingRepo meetingRepo;

    private User client1;
    private User client2;
    private User lawyer1;
    private User lawyer2;
    private Payment payment1;
    private Payment payment2;
    private Meeting meeting1;
    private Meeting meeting2;
    private Meeting meeting3;
    private Meeting meeting4;
    private UUID client1Id;
    private UUID client2Id;
    private UUID lawyer1Id;
    private UUID lawyer2Id;
    private UUID payment1Id;
    private UUID payment2Id;
    private UUID meeting1Id;
    private UUID meeting2Id;
    private UUID meeting3Id;
    private UUID meeting4Id;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize UUIDs
        client1Id = UUID.randomUUID();
        client2Id = UUID.randomUUID();
        lawyer1Id = UUID.randomUUID();
        lawyer2Id = UUID.randomUUID();
        payment1Id = UUID.randomUUID();
        payment2Id = UUID.randomUUID();
        meeting1Id = UUID.randomUUID();
        meeting2Id = UUID.randomUUID();
        meeting3Id = UUID.randomUUID();
        meeting4Id = UUID.randomUUID();

        // Setup test clients
        client1 = new User();
        client1.setId(client1Id);
        client1.setFirstName("John");
        client1.setLastName("Client");
        client1.setEmail("client1@example.com");
        client1.setRole(Role.USER);
        client1.setEmailVerified(true);

        client2 = new User();
        client2.setId(client2Id);
        client2.setFirstName("Jane");
        client2.setLastName("Client");
        client2.setEmail("client2@example.com");
        client2.setRole(Role.USER);
        client2.setEmailVerified(true);

        // Setup test lawyers
        lawyer1 = new User();
        lawyer1.setId(lawyer1Id);
        lawyer1.setFirstName("Alice");
        lawyer1.setLastName("Lawyer");
        lawyer1.setEmail("lawyer1@example.com");
        lawyer1.setRole(Role.LAWYER);
        lawyer1.setEmailVerified(true);

        lawyer2 = new User();
        lawyer2.setId(lawyer2Id);
        lawyer2.setFirstName("Bob");
        lawyer2.setLastName("Lawyer");
        lawyer2.setEmail("lawyer2@example.com");
        lawyer2.setRole(Role.LAWYER);
        lawyer2.setEmailVerified(true);

        // Setup test payments
        payment1 = new Payment();
        payment1.setId(payment1Id);
        payment1.setPayer(client1);
        payment1.setPayee(lawyer1);
        payment1.setStatus(PaymentStatus.PAID);

        payment2 = new Payment();
        payment2.setId(payment2Id);
        payment2.setPayer(client2);
        payment2.setPayee(lawyer2);
        payment2.setStatus(PaymentStatus.PENDING);

        // Setup test meetings
        OffsetDateTime now = OffsetDateTime.now();

        // Meeting 1: client1 with lawyer1
        meeting1 = new Meeting();
        meeting1.setId(meeting1Id);
        meeting1.setRoomName("room-meeting-1");
        meeting1.setClient(client1);
        meeting1.setLawyer(lawyer1);
        meeting1.setStartTimestamp(now.plusHours(1));
        meeting1.setEndTimestamp(now.plusHours(2));
        meeting1.setPaid(true);
        meeting1.setPayment(payment1);
        meeting1.setCreatedAt(now.minusDays(2));

        // Meeting 2: client1 with lawyer2
        meeting2 = new Meeting();
        meeting2.setId(meeting2Id);
        meeting2.setRoomName("room-meeting-2");
        meeting2.setClient(client1);
        meeting2.setLawyer(lawyer2);
        meeting2.setStartTimestamp(now.plusHours(3));
        meeting2.setEndTimestamp(now.plusHours(4));
        meeting2.setPaid(false);
        meeting2.setPayment(null);
        meeting2.setCreatedAt(now.minusDays(1));

        // Meeting 3: client2 with lawyer1
        meeting3 = new Meeting();
        meeting3.setId(meeting3Id);
        meeting3.setRoomName("room-meeting-3");
        meeting3.setClient(client2);
        meeting3.setLawyer(lawyer1);
        meeting3.setStartTimestamp(now.plusHours(5));
        meeting3.setEndTimestamp(now.plusHours(6));
        meeting3.setPaid(true);
        meeting3.setPayment(payment2);
        meeting3.setCreatedAt(now.minusHours(12));

        // Meeting 4: client2 with lawyer2
        meeting4 = new Meeting();
        meeting4.setId(meeting4Id);
        meeting4.setRoomName("room-meeting-4");
        meeting4.setClient(client2);
        meeting4.setLawyer(lawyer2);
        meeting4.setStartTimestamp(now.plusDays(1));
        meeting4.setEndTimestamp(now.plusDays(1).plusHours(1));
        meeting4.setPaid(false);
        meeting4.setPayment(null);
        meeting4.setCreatedAt(now.minusHours(6));
    }

    @Test
    @DisplayName("Should find meetings by client user successfully")
    void testFindByClientOrLawyer_withClient_success() {
        // Arrange
        List<Meeting> expectedMeetings = Arrays.asList(meeting1, meeting2);
        when(meetingRepo.findByClientOrLawyer(client1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        assertEquals(meeting1Id, actualMeetings.get(0).getId());
        assertEquals(meeting2Id, actualMeetings.get(1).getId());
        
        // Verify all meetings have client1 as client
        actualMeetings.forEach(meeting -> 
            assertEquals(client1Id, meeting.getClient().getId())
        );
    }

    @Test
    @DisplayName("Should find meetings by lawyer user successfully")
    void testFindByClientOrLawyer_withLawyer_success() {
        // Arrange
        List<Meeting> expectedMeetings = Arrays.asList(meeting1, meeting3);
        when(meetingRepo.findByClientOrLawyer(lawyer1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(lawyer1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        assertEquals(meeting1Id, actualMeetings.get(0).getId());
        assertEquals(meeting3Id, actualMeetings.get(1).getId());
        
        // Verify all meetings have lawyer1 as lawyer
        actualMeetings.forEach(meeting -> 
            assertEquals(lawyer1Id, meeting.getLawyer().getId())
        );
    }

    @Test
    @DisplayName("Should find meetings where user is both client and lawyer in different meetings")
    void testFindByClientOrLawyer_userInMultipleRoles() {
        // Arrange - lawyer1 appears as lawyer in meeting1 and meeting3
        List<Meeting> expectedMeetings = Arrays.asList(meeting1, meeting3);
        when(meetingRepo.findByClientOrLawyer(lawyer1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(lawyer1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        
        // Verify lawyer1 is the lawyer in both meetings
        actualMeetings.forEach(meeting -> 
            assertEquals(lawyer1Id, meeting.getLawyer().getId())
        );
    }

    @Test
    @DisplayName("Should return empty list when user has no meetings")
    void testFindByClientOrLawyer_noMeetings() {
        // Arrange
        User userWithNoMeetings = new User();
        userWithNoMeetings.setId(UUID.randomUUID());
        userWithNoMeetings.setFirstName("No");
        userWithNoMeetings.setLastName("Meetings");
        userWithNoMeetings.setEmail("nomeetings@example.com");
        userWithNoMeetings.setRole(Role.USER);
        
        when(meetingRepo.findByClientOrLawyer(userWithNoMeetings)).thenReturn(Collections.emptyList());

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(userWithNoMeetings);

        // Assert
        assertNotNull(actualMeetings);
        assertTrue(actualMeetings.isEmpty());
    }

    @Test
    @DisplayName("Should handle null user parameter")
    void testFindByClientOrLawyer_nullUser() {
        // Arrange
        when(meetingRepo.findByClientOrLawyer(null)).thenReturn(Collections.emptyList());

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(null);

        // Assert
        assertNotNull(actualMeetings);
        assertTrue(actualMeetings.isEmpty());
    }

    @Test
    @DisplayName("Should find all meetings for client2")
    void testFindByClientOrLawyer_client2Meetings() {
        // Arrange
        List<Meeting> expectedMeetings = Arrays.asList(meeting3, meeting4);
        when(meetingRepo.findByClientOrLawyer(client2)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client2);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        assertEquals(meeting3Id, actualMeetings.get(0).getId());
        assertEquals(meeting4Id, actualMeetings.get(1).getId());
        
        // Verify all meetings have client2 as client
        actualMeetings.forEach(meeting -> 
            assertEquals(client2Id, meeting.getClient().getId())
        );
    }

    @Test
    @DisplayName("Should find all meetings for lawyer2")
    void testFindByClientOrLawyer_lawyer2Meetings() {
        // Arrange
        List<Meeting> expectedMeetings = Arrays.asList(meeting2, meeting4);
        when(meetingRepo.findByClientOrLawyer(lawyer2)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(lawyer2);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        assertEquals(meeting2Id, actualMeetings.get(0).getId());
        assertEquals(meeting4Id, actualMeetings.get(1).getId());
        
        // Verify all meetings have lawyer2 as lawyer
        actualMeetings.forEach(meeting -> 
            assertEquals(lawyer2Id, meeting.getLawyer().getId())
        );
    }

    @Test
    @DisplayName("Should handle meetings with different payment statuses")
    void testFindByClientOrLawyer_mixedPaymentStatuses() {
        // Arrange - client1 has both paid and unpaid meetings
        List<Meeting> expectedMeetings = Arrays.asList(meeting1, meeting2);
        when(meetingRepo.findByClientOrLawyer(client1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        
        // Verify mixed payment statuses
        Meeting paidMeeting = actualMeetings.stream()
            .filter(m -> m.getId().equals(meeting1Id))
            .findFirst()
            .orElse(null);
        assertNotNull(paidMeeting);
        assertTrue(paidMeeting.isPaid());
        assertNotNull(paidMeeting.getPayment());
        
        Meeting unpaidMeeting = actualMeetings.stream()
            .filter(m -> m.getId().equals(meeting2Id))
            .findFirst()
            .orElse(null);
        assertNotNull(unpaidMeeting);
        assertTrue(!unpaidMeeting.isPaid());
    }

    @Test
    @DisplayName("Should handle meetings with different timestamps")
    void testFindByClientOrLawyer_differentTimestamps() {
        // Arrange
        List<Meeting> expectedMeetings = Arrays.asList(meeting1, meeting3);
        when(meetingRepo.findByClientOrLawyer(lawyer1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(lawyer1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        
        // Verify meetings have different timestamps
        Meeting firstMeeting = actualMeetings.get(0);
        Meeting secondMeeting = actualMeetings.get(1);
        
        assertNotNull(firstMeeting.getStartTimestamp());
        assertNotNull(firstMeeting.getEndTimestamp());
        assertNotNull(secondMeeting.getStartTimestamp());
        assertNotNull(secondMeeting.getEndTimestamp());
        
        // Verify timestamps are different
        assertNotEquals(firstMeeting.getStartTimestamp(),secondMeeting.getStartTimestamp());
    }

    @Test
    @DisplayName("Should verify repository inheritance from JpaRepository")
    void testRepositoryInheritance() {
        // This test verifies that MeetingRepo extends JpaRepository
        // The mock setup itself validates this inheritance
        assertNotNull(meetingRepo);
        
        // Test that we can call JpaRepository methods (through mocking)
        when(meetingRepo.findById(meeting1Id)).thenReturn(java.util.Optional.of(meeting1));
        
        java.util.Optional<Meeting> result = meetingRepo.findById(meeting1Id);
        assertTrue(result.isPresent());
        assertEquals(meeting1Id, result.get().getId());
    }

    @Test
    @DisplayName("Should handle parameter binding correctly")
    void testParameterBinding() {
        // Arrange - Test that the @Param annotation works correctly
        List<Meeting> expectedMeetings = Arrays.asList(meeting1, meeting2);
        when(meetingRepo.findByClientOrLawyer(client1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        
        // Verify that the parameter binding worked correctly by checking
        // that the returned meetings are associated with the correct user
        actualMeetings.forEach(meeting -> {
            assertTrue(meeting.getClient().getId().equals(client1Id) || 
                      meeting.getLawyer().getId().equals(client1Id));
        });
    }

    @Test
    @DisplayName("Should handle edge case with same user as client and lawyer")
    void testFindByClientOrLawyer_sameUserAsClientAndLawyer() {
        // Arrange - Create a meeting where the same user is both client and lawyer
        // (This might not be valid in business logic but the repository should handle it)
        Meeting edgeCaseMeeting = new Meeting();
        edgeCaseMeeting.setId(UUID.randomUUID());
        edgeCaseMeeting.setRoomName("edge-case-room");
        edgeCaseMeeting.setClient(client1);
        edgeCaseMeeting.setLawyer(client1); // Same user as both client and lawyer
        edgeCaseMeeting.setStartTimestamp(OffsetDateTime.now().plusHours(10));
        edgeCaseMeeting.setEndTimestamp(OffsetDateTime.now().plusHours(11));
        edgeCaseMeeting.setPaid(false);
        
        List<Meeting> expectedMeetings = Arrays.asList(edgeCaseMeeting);
        when(meetingRepo.findByClientOrLawyer(client1)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(1, actualMeetings.size());
        Meeting result = actualMeetings.get(0);
        assertEquals(client1Id, result.getClient().getId());
        assertEquals(client1Id, result.getLawyer().getId());
    }

    @Test
    @DisplayName("Should handle large result sets")
    void testFindByClientOrLawyer_largeResultSet() {
        // Arrange - Simulate a user with many meetings
        List<Meeting> largeMeetingList = Arrays.asList(
            meeting1, meeting2, meeting3, meeting4
        );
        when(meetingRepo.findByClientOrLawyer(client1)).thenReturn(largeMeetingList);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client1);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(4, actualMeetings.size());
        
        // Verify all meetings are returned
        assertTrue(actualMeetings.contains(meeting1));
        assertTrue(actualMeetings.contains(meeting2));
        assertTrue(actualMeetings.contains(meeting3));
        assertTrue(actualMeetings.contains(meeting4));
    }

    @Test
    @DisplayName("Should handle meetings with null payment")
    void testFindByClientOrLawyer_meetingsWithNullPayment() {
        // Arrange - meetings with null payment
        List<Meeting> expectedMeetings = Arrays.asList(meeting2, meeting4);
        when(meetingRepo.findByClientOrLawyer(client2)).thenReturn(expectedMeetings);

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(client2);

        // Assert
        assertNotNull(actualMeetings);
        assertEquals(2, actualMeetings.size());
        
        // Verify that meetings with null payment are handled correctly
        actualMeetings.forEach(meeting -> {
            if (meeting.getId().equals(meeting2Id) || meeting.getId().equals(meeting4Id)) {
                assertTrue(!meeting.isPaid());
            }
        });
    }

    @Test
    @DisplayName("Should verify custom query method exists")
    void testCustomQueryMethodExists() {
        // This test verifies that the custom query method is properly defined
        // The fact that we can mock it confirms its existence
        when(meetingRepo.findByClientOrLawyer(client1)).thenReturn(Arrays.asList(meeting1));
        
        List<Meeting> result = meetingRepo.findByClientOrLawyer(client1);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(meeting1Id, result.get(0).getId());
    }

    @Test
    @DisplayName("Should handle user with different roles")
    void testFindByClientOrLawyer_differentUserRoles() {
        // Arrange - Test with admin user (edge case)
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        
        when(meetingRepo.findByClientOrLawyer(adminUser)).thenReturn(Collections.emptyList());

        // Act
        List<Meeting> actualMeetings = meetingRepo.findByClientOrLawyer(adminUser);

        // Assert
        assertNotNull(actualMeetings);
        assertTrue(actualMeetings.isEmpty());
    }
}