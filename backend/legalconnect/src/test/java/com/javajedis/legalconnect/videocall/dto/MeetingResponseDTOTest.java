package com.javajedis.legalconnect.videocall.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MeetingResponseDTO Tests")
class MeetingResponseDTOTest {

    private MeetingResponseDTO meetingResponseDTO;
    private UUID testId;
    private String testRoomName;
    private MeetingResponseDTO.UserInfo testClient;
    private MeetingResponseDTO.UserInfo testLawyer;
    private OffsetDateTime testStartTimestamp;
    private OffsetDateTime testEndTimestamp;
    private boolean testIsPaid;
    private MeetingResponseDTO.PaymentInfo testPayment;

    @BeforeEach
    void setUp() {
        meetingResponseDTO = new MeetingResponseDTO();
        testId = UUID.randomUUID();
        testRoomName = "test-room-123";
        testClient = new MeetingResponseDTO.UserInfo(
            UUID.randomUUID(), "John", "Doe", "john.doe@example.com"
        );
        testLawyer = new MeetingResponseDTO.UserInfo(
            UUID.randomUUID(), "Jane", "Smith", "jane.smith@example.com"
        );
        testStartTimestamp = OffsetDateTime.now();
        testEndTimestamp = OffsetDateTime.now().plusHours(1);
        testIsPaid = true;
        testPayment = new MeetingResponseDTO.PaymentInfo(
            UUID.randomUUID(), new BigDecimal("100.00"), "COMPLETED"
        );
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        MeetingResponseDTO dto = new MeetingResponseDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getRoomName());
        assertNull(dto.getClient());
        assertNull(dto.getLawyer());
        assertNull(dto.getStartTimestamp());
        assertNull(dto.getEndTimestamp());
        assertEquals(false, dto.isPaid());
        assertNull(dto.getPayment());
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        MeetingResponseDTO dto = new MeetingResponseDTO(
            testId, testRoomName, testClient, testLawyer,
            testStartTimestamp, testEndTimestamp, testIsPaid, testPayment
        );
        
        assertEquals(testId, dto.getId());
        assertEquals(testRoomName, dto.getRoomName());
        assertEquals(testClient, dto.getClient());
        assertEquals(testLawyer, dto.getLawyer());
        assertEquals(testStartTimestamp, dto.getStartTimestamp());
        assertEquals(testEndTimestamp, dto.getEndTimestamp());
        assertEquals(testIsPaid, dto.isPaid());
        assertEquals(testPayment, dto.getPayment());
    }

    @Test
    @DisplayName("Should set and get meeting ID")
    void testIdGetterSetter() {
        meetingResponseDTO.setId(testId);
        assertEquals(testId, meetingResponseDTO.getId());
    }

    @Test
    @DisplayName("Should set and get room name")
    void testRoomNameGetterSetter() {
        meetingResponseDTO.setRoomName(testRoomName);
        assertEquals(testRoomName, meetingResponseDTO.getRoomName());
    }

    @Test
    @DisplayName("Should set and get client")
    void testClientGetterSetter() {
        meetingResponseDTO.setClient(testClient);
        assertEquals(testClient, meetingResponseDTO.getClient());
    }

    @Test
    @DisplayName("Should set and get lawyer")
    void testLawyerGetterSetter() {
        meetingResponseDTO.setLawyer(testLawyer);
        assertEquals(testLawyer, meetingResponseDTO.getLawyer());
    }

    @Test
    @DisplayName("Should set and get start timestamp")
    void testStartTimestampGetterSetter() {
        meetingResponseDTO.setStartTimestamp(testStartTimestamp);
        assertEquals(testStartTimestamp, meetingResponseDTO.getStartTimestamp());
    }

    @Test
    @DisplayName("Should set and get end timestamp")
    void testEndTimestampGetterSetter() {
        meetingResponseDTO.setEndTimestamp(testEndTimestamp);
        assertEquals(testEndTimestamp, meetingResponseDTO.getEndTimestamp());
    }

    @Test
    @DisplayName("Should set and get paid status")
    void testIsPaidGetterSetter() {
        meetingResponseDTO.setPaid(testIsPaid);
        assertEquals(testIsPaid, meetingResponseDTO.isPaid());
        
        meetingResponseDTO.setPaid(false);
        assertEquals(false, meetingResponseDTO.isPaid());
    }

    @Test
    @DisplayName("Should set and get payment info")
    void testPaymentGetterSetter() {
        meetingResponseDTO.setPayment(testPayment);
        assertEquals(testPayment, meetingResponseDTO.getPayment());
    }

    @Test
    @DisplayName("Should handle null values for optional fields")
    void testNullOptionalFields() {
        meetingResponseDTO.setId(testId);
        meetingResponseDTO.setRoomName(null);
        meetingResponseDTO.setClient(null);
        meetingResponseDTO.setLawyer(null);
        meetingResponseDTO.setStartTimestamp(null);
        meetingResponseDTO.setEndTimestamp(null);
        meetingResponseDTO.setPaid(false);
        meetingResponseDTO.setPayment(null);

        assertEquals(testId, meetingResponseDTO.getId());
        assertNull(meetingResponseDTO.getRoomName());
        assertNull(meetingResponseDTO.getClient());
        assertNull(meetingResponseDTO.getLawyer());
        assertNull(meetingResponseDTO.getStartTimestamp());
        assertNull(meetingResponseDTO.getEndTimestamp());
        assertEquals(false, meetingResponseDTO.isPaid());
        assertNull(meetingResponseDTO.getPayment());
    }

    @Test
    @DisplayName("Should handle different UUID values")
    void testDifferentUUIDValues() {
        UUID[] uuids = {
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        };

        meetingResponseDTO.setId(uuids[0]);
        assertEquals(uuids[0], meetingResponseDTO.getId());

        meetingResponseDTO.setId(uuids[1]);
        assertEquals(uuids[1], meetingResponseDTO.getId());
    }

    @Test
    @DisplayName("Should handle different timestamp values")
    void testDifferentTimestampValues() {
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(30);

        meetingResponseDTO.setStartTimestamp(pastDate);
        meetingResponseDTO.setEndTimestamp(futureDate);

        assertEquals(pastDate, meetingResponseDTO.getStartTimestamp());
        assertEquals(futureDate, meetingResponseDTO.getEndTimestamp());
    }

    @Test
    @DisplayName("Should handle different room name values")
    void testDifferentRoomNameValues() {
        String[] roomNames = {
            "",
            "A",
            "room-123",
            "VERY_LONG_ROOM_NAME_WITH_MANY_CHARACTERS_12345678901234567890"
        };

        for (String roomName : roomNames) {
            meetingResponseDTO.setRoomName(roomName);
            assertEquals(roomName, meetingResponseDTO.getRoomName());
        }
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        MeetingResponseDTO dto1 = new MeetingResponseDTO(
            testId, testRoomName, testClient, testLawyer,
            testStartTimestamp, testEndTimestamp, testIsPaid, testPayment
        );
        
        MeetingResponseDTO dto2 = new MeetingResponseDTO(
            testId, testRoomName, testClient, testLawyer,
            testStartTimestamp, testEndTimestamp, testIsPaid, testPayment
        );
        
        MeetingResponseDTO dto3 = new MeetingResponseDTO(
            UUID.randomUUID(), testRoomName, testClient, testLawyer,
            testStartTimestamp, testEndTimestamp, testIsPaid, testPayment
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        meetingResponseDTO.setId(testId);
        meetingResponseDTO.setRoomName(testRoomName);
        meetingResponseDTO.setClient(testClient);
        meetingResponseDTO.setLawyer(testLawyer);
        meetingResponseDTO.setStartTimestamp(testStartTimestamp);
        meetingResponseDTO.setEndTimestamp(testEndTimestamp);
        meetingResponseDTO.setPaid(testIsPaid);
        meetingResponseDTO.setPayment(testPayment);

        String toString = meetingResponseDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("MeetingResponseDTO"));
        assertTrue(toString.contains(testId.toString()));
        assertTrue(toString.contains(testRoomName));
        assertTrue(toString.contains(String.valueOf(testIsPaid)));
    }

    @Test
    @DisplayName("Should support data transfer functionality")
    void testDataTransferBehavior() {
        // Simulate data transfer by setting all fields and verifying they're preserved
        MeetingResponseDTO sourceDTO = new MeetingResponseDTO();
        sourceDTO.setId(testId);
        sourceDTO.setRoomName(testRoomName);
        sourceDTO.setClient(testClient);
        sourceDTO.setLawyer(testLawyer);
        sourceDTO.setStartTimestamp(testStartTimestamp);
        sourceDTO.setEndTimestamp(testEndTimestamp);
        sourceDTO.setPaid(testIsPaid);
        sourceDTO.setPayment(testPayment);

        // Create target DTO using all-args constructor (simulating mapping)
        MeetingResponseDTO targetDTO = new MeetingResponseDTO(
            sourceDTO.getId(),
            sourceDTO.getRoomName(),
            sourceDTO.getClient(),
            sourceDTO.getLawyer(),
            sourceDTO.getStartTimestamp(),
            sourceDTO.getEndTimestamp(),
            sourceDTO.isPaid(),
            sourceDTO.getPayment()
        );

        // Verify all data was transferred correctly
        assertEquals(sourceDTO.getId(), targetDTO.getId());
        assertEquals(sourceDTO.getRoomName(), targetDTO.getRoomName());
        assertEquals(sourceDTO.getClient(), targetDTO.getClient());
        assertEquals(sourceDTO.getLawyer(), targetDTO.getLawyer());
        assertEquals(sourceDTO.getStartTimestamp(), targetDTO.getStartTimestamp());
        assertEquals(sourceDTO.getEndTimestamp(), targetDTO.getEndTimestamp());
        assertEquals(sourceDTO.isPaid(), targetDTO.isPaid());
        assertEquals(sourceDTO.getPayment(), targetDTO.getPayment());
    }

    @Nested
    @DisplayName("UserInfo Nested Class Tests")
    class UserInfoTest {

        private MeetingResponseDTO.UserInfo userInfo;
        private UUID testUserId;
        private String testFirstName;
        private String testLastName;
        private String testEmail;

        @BeforeEach
        void setUp() {
            userInfo = new MeetingResponseDTO.UserInfo();
            testUserId = UUID.randomUUID();
            testFirstName = "John";
            testLastName = "Doe";
            testEmail = "john.doe@example.com";
        }

        @Test
        @DisplayName("Should create UserInfo with no-args constructor")
        void testUserInfoNoArgsConstructor() {
            MeetingResponseDTO.UserInfo testUserInfo = new MeetingResponseDTO.UserInfo();
            assertNotNull(testUserInfo);
            assertNull(testUserInfo.getId());
            assertNull(testUserInfo.getFirstName());
            assertNull(testUserInfo.getLastName());
            assertNull(testUserInfo.getEmail());
        }

        @Test
        @DisplayName("Should create UserInfo with all-args constructor")
        void testUserInfoAllArgsConstructor() {
            MeetingResponseDTO.UserInfo testUserInfo = new MeetingResponseDTO.UserInfo(
                testUserId, testFirstName, testLastName, testEmail
            );
            
            assertEquals(testUserId, testUserInfo.getId());
            assertEquals(testFirstName, testUserInfo.getFirstName());
            assertEquals(testLastName, testUserInfo.getLastName());
            assertEquals(testEmail, testUserInfo.getEmail());
        }

        @Test
        @DisplayName("Should set and get user ID")
        void testUserIdGetterSetter() {
            userInfo.setId(testUserId);
            assertEquals(testUserId, userInfo.getId());
        }

        @Test
        @DisplayName("Should set and get first name")
        void testFirstNameGetterSetter() {
            userInfo.setFirstName(testFirstName);
            assertEquals(testFirstName, userInfo.getFirstName());
        }

        @Test
        @DisplayName("Should set and get last name")
        void testLastNameGetterSetter() {
            userInfo.setLastName(testLastName);
            assertEquals(testLastName, userInfo.getLastName());
        }

        @Test
        @DisplayName("Should set and get email")
        void testEmailGetterSetter() {
            userInfo.setEmail(testEmail);
            assertEquals(testEmail, userInfo.getEmail());
        }

        @Test
        @DisplayName("Should handle null values")
        void testUserInfoNullValues() {
            userInfo.setId(null);
            userInfo.setFirstName(null);
            userInfo.setLastName(null);
            userInfo.setEmail(null);

            assertNull(userInfo.getId());
            assertNull(userInfo.getFirstName());
            assertNull(userInfo.getLastName());
            assertNull(userInfo.getEmail());
        }

        @Test
        @DisplayName("Should test UserInfo equals and hashCode")
        void testUserInfoEqualsAndHashCode() {
            MeetingResponseDTO.UserInfo userInfo1 = new MeetingResponseDTO.UserInfo(
                testUserId, testFirstName, testLastName, testEmail
            );
            MeetingResponseDTO.UserInfo userInfo2 = new MeetingResponseDTO.UserInfo(
                testUserId, testFirstName, testLastName, testEmail
            );
            MeetingResponseDTO.UserInfo userInfo3 = new MeetingResponseDTO.UserInfo(
                UUID.randomUUID(), testFirstName, testLastName, testEmail
            );

            assertEquals(userInfo1, userInfo2);
            assertEquals(userInfo1.hashCode(), userInfo2.hashCode());
            assertNotEquals(userInfo1, userInfo3);
        }

        @Test
        @DisplayName("Should test UserInfo toString method")
        void testUserInfoToString() {
            userInfo.setId(testUserId);
            userInfo.setFirstName(testFirstName);
            userInfo.setLastName(testLastName);
            userInfo.setEmail(testEmail);

            String toString = userInfo.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("UserInfo"));
            assertTrue(toString.contains(testUserId.toString()));
            assertTrue(toString.contains(testFirstName));
            assertTrue(toString.contains(testLastName));
            assertTrue(toString.contains(testEmail));
        }
    }

    @Nested
    @DisplayName("PaymentInfo Nested Class Tests")
    class PaymentInfoTest {

        private MeetingResponseDTO.PaymentInfo paymentInfo;
        private UUID testPaymentId;
        private BigDecimal testAmount;
        private String testStatus;

        @BeforeEach
        void setUp() {
            paymentInfo = new MeetingResponseDTO.PaymentInfo();
            testPaymentId = UUID.randomUUID();
            testAmount = new BigDecimal("100.00");
            testStatus = "COMPLETED";
        }

        @Test
        @DisplayName("Should create PaymentInfo with no-args constructor")
        void testPaymentInfoNoArgsConstructor() {
            MeetingResponseDTO.PaymentInfo testPaymentInfo = new MeetingResponseDTO.PaymentInfo();
            assertNotNull(testPaymentInfo);
            assertNull(testPaymentInfo.getId());
            assertNull(testPaymentInfo.getAmount());
            assertNull(testPaymentInfo.getStatus());
        }

        @Test
        @DisplayName("Should create PaymentInfo with all-args constructor")
        void testPaymentInfoAllArgsConstructor() {
            MeetingResponseDTO.PaymentInfo testPaymentInfo = new MeetingResponseDTO.PaymentInfo(
                testPaymentId, testAmount, testStatus
            );
            
            assertEquals(testPaymentId, testPaymentInfo.getId());
            assertEquals(testAmount, testPaymentInfo.getAmount());
            assertEquals(testStatus, testPaymentInfo.getStatus());
        }

        @Test
        @DisplayName("Should set and get payment ID")
        void testPaymentIdGetterSetter() {
            paymentInfo.setId(testPaymentId);
            assertEquals(testPaymentId, paymentInfo.getId());
        }

        @Test
        @DisplayName("Should set and get amount")
        void testAmountGetterSetter() {
            paymentInfo.setAmount(testAmount);
            assertEquals(testAmount, paymentInfo.getAmount());
        }

        @Test
        @DisplayName("Should set and get status")
        void testStatusGetterSetter() {
            paymentInfo.setStatus(testStatus);
            assertEquals(testStatus, paymentInfo.getStatus());
        }

        @Test
        @DisplayName("Should handle different BigDecimal amounts")
        void testDifferentAmountValues() {
            BigDecimal[] amounts = {
                new BigDecimal("0.01"),
                new BigDecimal("1.00"),
                new BigDecimal("100.50"),
                new BigDecimal("9999999999.99")
            };

            for (BigDecimal amount : amounts) {
                paymentInfo.setAmount(amount);
                assertEquals(amount, paymentInfo.getAmount());
            }
        }

        @Test
        @DisplayName("Should handle different status values")
        void testDifferentStatusValues() {
            String[] statuses = {
                "PENDING",
                "COMPLETED",
                "FAILED",
                "CANCELLED"
            };

            for (String status : statuses) {
                paymentInfo.setStatus(status);
                assertEquals(status, paymentInfo.getStatus());
            }
        }

        @Test
        @DisplayName("Should handle null values")
        void testPaymentInfoNullValues() {
            paymentInfo.setId(null);
            paymentInfo.setAmount(null);
            paymentInfo.setStatus(null);

            assertNull(paymentInfo.getId());
            assertNull(paymentInfo.getAmount());
            assertNull(paymentInfo.getStatus());
        }

        @Test
        @DisplayName("Should test PaymentInfo equals and hashCode")
        void testPaymentInfoEqualsAndHashCode() {
            MeetingResponseDTO.PaymentInfo paymentInfo1 = new MeetingResponseDTO.PaymentInfo(
                testPaymentId, testAmount, testStatus
            );
            MeetingResponseDTO.PaymentInfo paymentInfo2 = new MeetingResponseDTO.PaymentInfo(
                testPaymentId, testAmount, testStatus
            );
            MeetingResponseDTO.PaymentInfo paymentInfo3 = new MeetingResponseDTO.PaymentInfo(
                UUID.randomUUID(), testAmount, testStatus
            );

            assertEquals(paymentInfo1, paymentInfo2);
            assertEquals(paymentInfo1.hashCode(), paymentInfo2.hashCode());
            assertNotEquals(paymentInfo1, paymentInfo3);
        }

        @Test
        @DisplayName("Should test PaymentInfo toString method")
        void testPaymentInfoToString() {
            paymentInfo.setId(testPaymentId);
            paymentInfo.setAmount(testAmount);
            paymentInfo.setStatus(testStatus);

            String toString = paymentInfo.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("PaymentInfo"));
            assertTrue(toString.contains(testPaymentId.toString()));
            assertTrue(toString.contains(testAmount.toString()));
            assertTrue(toString.contains(testStatus));
        }
    }
}