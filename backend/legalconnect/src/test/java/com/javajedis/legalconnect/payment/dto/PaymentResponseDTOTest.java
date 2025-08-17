package com.javajedis.legalconnect.payment.dto;

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
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.payment.PaymentMethod;
import com.javajedis.legalconnect.payment.PaymentStatus;

@DisplayName("PaymentResponseDTO Tests")
class PaymentResponseDTOTest {

    private PaymentResponseDTO paymentResponseDTO;
    private UUID testId;
    private UUID testPayeeId;
    private UUID testPayerId;
    private UUID testmeetingId;
    private BigDecimal testAmount;
    private PaymentStatus testStatus;
    private PaymentMethod testPaymentMethod;
    private String testTransactionId;
    private OffsetDateTime testPaymentDate;
    private OffsetDateTime testCreatedAt;
    private OffsetDateTime testUpdatedAt;
    private String testPayerFirstName;
    private String testPayerLastName;
    private String testPayerEmail;
    private String testPayeeFirstName;
    private String testPayeeLastName;
    private String testPayeeEmail;

    @BeforeEach
    void setUp() {
        paymentResponseDTO = new PaymentResponseDTO();
        testId = UUID.randomUUID();
        testPayeeId = UUID.randomUUID();
        testPayerId = UUID.randomUUID();
        testmeetingId = UUID.randomUUID();
        testAmount = new BigDecimal("100.00");
        testStatus = PaymentStatus.PENDING;
        testPaymentMethod = PaymentMethod.CARD;
        testTransactionId = "TXN123456789";
        testPaymentDate = OffsetDateTime.now();
        testCreatedAt = OffsetDateTime.now().minusDays(1);
        testUpdatedAt = OffsetDateTime.now();
        testPayerFirstName = "John";
        testPayerLastName = "Doe";
        testPayerEmail = "john.doe@example.com";
        testPayeeFirstName = "Jane";
        testPayeeLastName = "Smith";
        testPayeeEmail = "jane.smith@example.com";
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getPayeeId());
        assertNull(dto.getPayerId());
        assertNull(dto.getMeetingId());
        assertNull(dto.getAmount());
        assertNull(dto.getStatus());
        assertNull(dto.getPaymentMethod());
        assertNull(dto.getTransactionId());
        assertNull(dto.getPaymentDate());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getPayerFirstName());
        assertNull(dto.getPayerLastName());
        assertNull(dto.getPayerEmail());
        assertNull(dto.getPayeeFirstName());
        assertNull(dto.getPayeeLastName());
        assertNull(dto.getPayeeEmail());
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        PaymentResponseDTO dto = new PaymentResponseDTO(
            testId, testPayeeId, testPayerId, testmeetingId, testAmount, 
            testStatus, testPaymentMethod, testTransactionId, 
            testPaymentDate, testCreatedAt, testUpdatedAt,
            testPayerFirstName, testPayerLastName, testPayerEmail,
            testPayeeFirstName, testPayeeLastName, testPayeeEmail
        );
        
        assertEquals(testId, dto.getId());
        assertEquals(testPayeeId, dto.getPayeeId());
        assertEquals(testPayerId, dto.getPayerId());
        assertEquals(testmeetingId, dto.getMeetingId());
        assertEquals(testAmount, dto.getAmount());
        assertEquals(testStatus, dto.getStatus());
        assertEquals(testPaymentMethod, dto.getPaymentMethod());
        assertEquals(testTransactionId, dto.getTransactionId());
        assertEquals(testPaymentDate, dto.getPaymentDate());
        assertEquals(testCreatedAt, dto.getCreatedAt());
        assertEquals(testUpdatedAt, dto.getUpdatedAt());
        assertEquals(testPayerFirstName, dto.getPayerFirstName());
        assertEquals(testPayerLastName, dto.getPayerLastName());
        assertEquals(testPayerEmail, dto.getPayerEmail());
        assertEquals(testPayeeFirstName, dto.getPayeeFirstName());
        assertEquals(testPayeeLastName, dto.getPayeeLastName());
        assertEquals(testPayeeEmail, dto.getPayeeEmail());
    }

    @Test
    @DisplayName("Should set and get payment ID")
    void testIdGetterSetter() {
        paymentResponseDTO.setId(testId);
        assertEquals(testId, paymentResponseDTO.getId());
    }

    @Test
    @DisplayName("Should set and get payee ID")
    void testPayeeIdGetterSetter() {
        paymentResponseDTO.setPayeeId(testPayeeId);
        assertEquals(testPayeeId, paymentResponseDTO.getPayeeId());
    }

    @Test
    @DisplayName("Should set and get payer ID")
    void testPayerIdGetterSetter() {
        paymentResponseDTO.setPayerId(testPayerId);
        assertEquals(testPayerId, paymentResponseDTO.getPayerId());
    }

    @Test
    @DisplayName("Should set and get reference ID")
    void testmeetingIdGetterSetter() {
        paymentResponseDTO.setMeetingId(testmeetingId);
        assertEquals(testmeetingId, paymentResponseDTO.getMeetingId());
    }

    @Test
    @DisplayName("Should set and get amount")
    void testAmountGetterSetter() {
        paymentResponseDTO.setAmount(testAmount);
        assertEquals(testAmount, paymentResponseDTO.getAmount());
    }

    @Test
    @DisplayName("Should set and get payment status")
    void testStatusGetterSetter() {
        paymentResponseDTO.setStatus(testStatus);
        assertEquals(testStatus, paymentResponseDTO.getStatus());
    }

    @Test
    @DisplayName("Should set and get payment method")
    void testPaymentMethodGetterSetter() {
        paymentResponseDTO.setPaymentMethod(testPaymentMethod);
        assertEquals(testPaymentMethod, paymentResponseDTO.getPaymentMethod());
    }

    @Test
    @DisplayName("Should set and get transaction ID")
    void testTransactionIdGetterSetter() {
        paymentResponseDTO.setTransactionId(testTransactionId);
        assertEquals(testTransactionId, paymentResponseDTO.getTransactionId());
    }

    @Test
    @DisplayName("Should set and get payment date")
    void testPaymentDateGetterSetter() {
        paymentResponseDTO.setPaymentDate(testPaymentDate);
        assertEquals(testPaymentDate, paymentResponseDTO.getPaymentDate());
    }

    @Test
    @DisplayName("Should set and get created at timestamp")
    void testCreatedAtGetterSetter() {
        paymentResponseDTO.setCreatedAt(testCreatedAt);
        assertEquals(testCreatedAt, paymentResponseDTO.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get updated at timestamp")
    void testUpdatedAtGetterSetter() {
        paymentResponseDTO.setUpdatedAt(testUpdatedAt);
        assertEquals(testUpdatedAt, paymentResponseDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get payer first name")
    void testPayerFirstNameGetterSetter() {
        paymentResponseDTO.setPayerFirstName(testPayerFirstName);
        assertEquals(testPayerFirstName, paymentResponseDTO.getPayerFirstName());
    }

    @Test
    @DisplayName("Should set and get payer last name")
    void testPayerLastNameGetterSetter() {
        paymentResponseDTO.setPayerLastName(testPayerLastName);
        assertEquals(testPayerLastName, paymentResponseDTO.getPayerLastName());
    }

    @Test
    @DisplayName("Should set and get payer email")
    void testPayerEmailGetterSetter() {
        paymentResponseDTO.setPayerEmail(testPayerEmail);
        assertEquals(testPayerEmail, paymentResponseDTO.getPayerEmail());
    }

    @Test
    @DisplayName("Should set and get payee first name")
    void testPayeeFirstNameGetterSetter() {
        paymentResponseDTO.setPayeeFirstName(testPayeeFirstName);
        assertEquals(testPayeeFirstName, paymentResponseDTO.getPayeeFirstName());
    }

    @Test
    @DisplayName("Should set and get payee last name")
    void testPayeeLastNameGetterSetter() {
        paymentResponseDTO.setPayeeLastName(testPayeeLastName);
        assertEquals(testPayeeLastName, paymentResponseDTO.getPayeeLastName());
    }

    @Test
    @DisplayName("Should set and get payee email")
    void testPayeeEmailGetterSetter() {
        paymentResponseDTO.setPayeeEmail(testPayeeEmail);
        assertEquals(testPayeeEmail, paymentResponseDTO.getPayeeEmail());
    }

    @Test
    @DisplayName("Should handle null values for optional fields")
    void testNullOptionalFields() {
        paymentResponseDTO.setId(testId);
        paymentResponseDTO.setPayeeId(testPayeeId);
        paymentResponseDTO.setPayerId(testPayerId);
        paymentResponseDTO.setMeetingId(testmeetingId);
        paymentResponseDTO.setAmount(testAmount);
        paymentResponseDTO.setStatus(testStatus);
        paymentResponseDTO.setPaymentMethod(null); // Optional field
        paymentResponseDTO.setTransactionId(null); // Optional field
        paymentResponseDTO.setPaymentDate(null); // Optional field
        paymentResponseDTO.setCreatedAt(testCreatedAt);
        paymentResponseDTO.setUpdatedAt(testUpdatedAt);

        assertEquals(testId, paymentResponseDTO.getId());
        assertEquals(testPayeeId, paymentResponseDTO.getPayeeId());
        assertEquals(testPayerId, paymentResponseDTO.getPayerId());
        assertEquals(testmeetingId, paymentResponseDTO.getMeetingId());
        assertEquals(testAmount, paymentResponseDTO.getAmount());
        assertEquals(testStatus, paymentResponseDTO.getStatus());
        assertNull(paymentResponseDTO.getPaymentMethod());
        assertNull(paymentResponseDTO.getTransactionId());
        assertNull(paymentResponseDTO.getPaymentDate());
        assertEquals(testCreatedAt, paymentResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, paymentResponseDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle all payment status enum values")
    void testAllPaymentStatusValues() {
        for (PaymentStatus status : PaymentStatus.values()) {
            paymentResponseDTO.setStatus(status);
            assertEquals(status, paymentResponseDTO.getStatus());
        }
    }

    @Test
    @DisplayName("Should handle all payment method enum values")
    void testAllPaymentMethodValues() {
        for (PaymentMethod method : PaymentMethod.values()) {
            paymentResponseDTO.setPaymentMethod(method);
            assertEquals(method, paymentResponseDTO.getPaymentMethod());
        }
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
            paymentResponseDTO.setAmount(amount);
            assertEquals(amount, paymentResponseDTO.getAmount());
        }
    }

    @Test
    @DisplayName("Should handle different UUID values")
    void testDifferentUUIDValues() {
        UUID[] uuids = {
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        };

        paymentResponseDTO.setId(uuids[0]);
        paymentResponseDTO.setPayeeId(uuids[1]);
        paymentResponseDTO.setPayerId(uuids[2]);

        assertEquals(uuids[0], paymentResponseDTO.getId());
        assertEquals(uuids[1], paymentResponseDTO.getPayeeId());
        assertEquals(uuids[2], paymentResponseDTO.getPayerId());
    }

    @Test
    @DisplayName("Should handle different timestamp values")
    void testDifferentTimestampValues() {
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime currentDate = OffsetDateTime.now();
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(30);

        paymentResponseDTO.setPaymentDate(pastDate);
        paymentResponseDTO.setCreatedAt(currentDate);
        paymentResponseDTO.setUpdatedAt(futureDate);

        assertEquals(pastDate, paymentResponseDTO.getPaymentDate());
        assertEquals(currentDate, paymentResponseDTO.getCreatedAt());
        assertEquals(futureDate, paymentResponseDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle empty and long transaction IDs")
    void testTransactionIdVariations() {
        String[] transactionIds = {
            "",
            "A",
            "TXN123",
            "VERY_LONG_TRANSACTION_ID_WITH_MANY_CHARACTERS_12345678901234567890"
        };

        for (String transactionId : transactionIds) {
            paymentResponseDTO.setTransactionId(transactionId);
            assertEquals(transactionId, paymentResponseDTO.getTransactionId());
        }
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        PaymentResponseDTO dto1 = new PaymentResponseDTO(
            testId, testPayeeId, testPayerId, testmeetingId, testAmount, 
            testStatus, testPaymentMethod, testTransactionId, 
            testPaymentDate, testCreatedAt, testUpdatedAt,
            testPayerFirstName, testPayerLastName, testPayerEmail,
            testPayeeFirstName, testPayeeLastName, testPayeeEmail
        );
        
        PaymentResponseDTO dto2 = new PaymentResponseDTO(
            testId, testPayeeId, testPayerId, testmeetingId, testAmount, 
            testStatus, testPaymentMethod, testTransactionId, 
            testPaymentDate, testCreatedAt, testUpdatedAt,
            testPayerFirstName, testPayerLastName, testPayerEmail,
            testPayeeFirstName, testPayeeLastName, testPayeeEmail
        );
        
        PaymentResponseDTO dto3 = new PaymentResponseDTO(
            UUID.randomUUID(), testPayeeId, testPayerId, testmeetingId, testAmount, 
            testStatus, testPaymentMethod, testTransactionId, 
            testPaymentDate, testCreatedAt, testUpdatedAt,
            testPayerFirstName, testPayerLastName, testPayerEmail,
            testPayeeFirstName, testPayeeLastName, testPayeeEmail
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        paymentResponseDTO.setId(testId);
        paymentResponseDTO.setPayeeId(testPayeeId);
        paymentResponseDTO.setPayerId(testPayerId);
        paymentResponseDTO.setMeetingId(testmeetingId);
        paymentResponseDTO.setAmount(testAmount);
        paymentResponseDTO.setStatus(testStatus);
        paymentResponseDTO.setPaymentMethod(testPaymentMethod);
        paymentResponseDTO.setTransactionId(testTransactionId);
        paymentResponseDTO.setPaymentDate(testPaymentDate);
        paymentResponseDTO.setCreatedAt(testCreatedAt);
        paymentResponseDTO.setUpdatedAt(testUpdatedAt);
        paymentResponseDTO.setPayerFirstName(testPayerFirstName);
        paymentResponseDTO.setPayerLastName(testPayerLastName);
        paymentResponseDTO.setPayerEmail(testPayerEmail);
        paymentResponseDTO.setPayeeFirstName(testPayeeFirstName);
        paymentResponseDTO.setPayeeLastName(testPayeeLastName);
        paymentResponseDTO.setPayeeEmail(testPayeeEmail);

        String toString = paymentResponseDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("PaymentResponseDTO"));
        assertTrue(toString.contains(testId.toString()));
        assertTrue(toString.contains(testPayeeId.toString()));
        assertTrue(toString.contains(testPayerId.toString()));
        assertTrue(toString.contains(testmeetingId.toString()));
        assertTrue(toString.contains(testAmount.toString()));
        assertTrue(toString.contains(testStatus.toString()));
        assertTrue(toString.contains(testPaymentMethod.toString()));
        assertTrue(toString.contains(testTransactionId));
        assertTrue(toString.contains(testPayerFirstName));
        assertTrue(toString.contains(testPayerLastName));
        assertTrue(toString.contains(testPayerEmail));
        assertTrue(toString.contains(testPayeeFirstName));
        assertTrue(toString.contains(testPayeeLastName));
        assertTrue(toString.contains(testPayeeEmail));
    }

    @Test
    @DisplayName("Should support data transfer functionality")
    void testDataTransferBehavior() {
        // Simulate data transfer by setting all fields and verifying they're preserved
        PaymentResponseDTO sourceDTO = new PaymentResponseDTO();
        sourceDTO.setId(testId);
        sourceDTO.setPayeeId(testPayeeId);
        sourceDTO.setPayerId(testPayerId);
        sourceDTO.setMeetingId(testmeetingId);
        sourceDTO.setAmount(testAmount);
        sourceDTO.setStatus(testStatus);
        sourceDTO.setPaymentMethod(testPaymentMethod);
        sourceDTO.setTransactionId(testTransactionId);
        sourceDTO.setPaymentDate(testPaymentDate);
        sourceDTO.setCreatedAt(testCreatedAt);
        sourceDTO.setUpdatedAt(testUpdatedAt);
        sourceDTO.setPayerFirstName(testPayerFirstName);
        sourceDTO.setPayerLastName(testPayerLastName);
        sourceDTO.setPayerEmail(testPayerEmail);
        sourceDTO.setPayeeFirstName(testPayeeFirstName);
        sourceDTO.setPayeeLastName(testPayeeLastName);
        sourceDTO.setPayeeEmail(testPayeeEmail);

        // Create target DTO using all-args constructor (simulating mapping)
        PaymentResponseDTO targetDTO = new PaymentResponseDTO(
            sourceDTO.getId(),
            sourceDTO.getPayeeId(),
            sourceDTO.getPayerId(),
            sourceDTO.getMeetingId(),
            sourceDTO.getAmount(),
            sourceDTO.getStatus(),
            sourceDTO.getPaymentMethod(),
            sourceDTO.getTransactionId(),
            sourceDTO.getPaymentDate(),
            sourceDTO.getCreatedAt(),
            sourceDTO.getUpdatedAt(),
            sourceDTO.getPayerFirstName(),
            sourceDTO.getPayerLastName(),
            sourceDTO.getPayerEmail(),
            sourceDTO.getPayeeFirstName(),
            sourceDTO.getPayeeLastName(),
            sourceDTO.getPayeeEmail()
        );

        // Verify all data was transferred correctly
        assertEquals(sourceDTO.getId(), targetDTO.getId());
        assertEquals(sourceDTO.getPayeeId(), targetDTO.getPayeeId());
        assertEquals(sourceDTO.getPayerId(), targetDTO.getPayerId());
        assertEquals(sourceDTO.getMeetingId(), targetDTO.getMeetingId());
        assertEquals(sourceDTO.getAmount(), targetDTO.getAmount());
        assertEquals(sourceDTO.getStatus(), targetDTO.getStatus());
        assertEquals(sourceDTO.getPaymentMethod(), targetDTO.getPaymentMethod());
        assertEquals(sourceDTO.getTransactionId(), targetDTO.getTransactionId());
        assertEquals(sourceDTO.getPaymentDate(), targetDTO.getPaymentDate());
        assertEquals(sourceDTO.getCreatedAt(), targetDTO.getCreatedAt());
        assertEquals(sourceDTO.getUpdatedAt(), targetDTO.getUpdatedAt());
        assertEquals(sourceDTO.getPayerFirstName(), targetDTO.getPayerFirstName());
        assertEquals(sourceDTO.getPayerLastName(), targetDTO.getPayerLastName());
        assertEquals(sourceDTO.getPayerEmail(), targetDTO.getPayerEmail());
        assertEquals(sourceDTO.getPayeeFirstName(), targetDTO.getPayeeFirstName());
        assertEquals(sourceDTO.getPayeeLastName(), targetDTO.getPayeeLastName());
        assertEquals(sourceDTO.getPayeeEmail(), targetDTO.getPayeeEmail());
    }

    @Test
    @DisplayName("Should handle field mapping consistency")
    void testFieldMappingConsistency() {
        // Test that all fields can be set and retrieved consistently
        paymentResponseDTO.setId(testId);
        paymentResponseDTO.setPayeeId(testPayeeId);
        paymentResponseDTO.setPayerId(testPayerId);
        paymentResponseDTO.setMeetingId(testmeetingId);
        paymentResponseDTO.setAmount(testAmount);
        paymentResponseDTO.setStatus(testStatus);
        paymentResponseDTO.setPaymentMethod(testPaymentMethod);
        paymentResponseDTO.setTransactionId(testTransactionId);
        paymentResponseDTO.setPaymentDate(testPaymentDate);
        paymentResponseDTO.setCreatedAt(testCreatedAt);
        paymentResponseDTO.setUpdatedAt(testUpdatedAt);
        paymentResponseDTO.setPayerFirstName(testPayerFirstName);
        paymentResponseDTO.setPayerLastName(testPayerLastName);
        paymentResponseDTO.setPayerEmail(testPayerEmail);
        paymentResponseDTO.setPayeeFirstName(testPayeeFirstName);
        paymentResponseDTO.setPayeeLastName(testPayeeLastName);
        paymentResponseDTO.setPayeeEmail(testPayeeEmail);

        // Verify all fields maintain their values
        assertEquals(testId, paymentResponseDTO.getId());
        assertEquals(testPayeeId, paymentResponseDTO.getPayeeId());
        assertEquals(testPayerId, paymentResponseDTO.getPayerId());
        assertEquals(testmeetingId, paymentResponseDTO.getMeetingId());
        assertEquals(testAmount, paymentResponseDTO.getAmount());
        assertEquals(testStatus, paymentResponseDTO.getStatus());
        assertEquals(testPaymentMethod, paymentResponseDTO.getPaymentMethod());
        assertEquals(testTransactionId, paymentResponseDTO.getTransactionId());
        assertEquals(testPaymentDate, paymentResponseDTO.getPaymentDate());
        assertEquals(testCreatedAt, paymentResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, paymentResponseDTO.getUpdatedAt());
        assertEquals(testPayerFirstName, paymentResponseDTO.getPayerFirstName());
        assertEquals(testPayerLastName, paymentResponseDTO.getPayerLastName());
        assertEquals(testPayerEmail, paymentResponseDTO.getPayerEmail());
        assertEquals(testPayeeFirstName, paymentResponseDTO.getPayeeFirstName());
        assertEquals(testPayeeLastName, paymentResponseDTO.getPayeeLastName());
        assertEquals(testPayeeEmail, paymentResponseDTO.getPayeeEmail());
    }
}