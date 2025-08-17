package com.javajedis.legalconnect.payment;

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

import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

@DisplayName("Payment Entity Tests")
class PaymentTest {

    private Payment payment;
    private User payer;
    private User payee;
    private UUID testId;
    private UUID payerId;
    private UUID payeeId;
    private UUID meetingId;
    private BigDecimal testAmount;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        payerId = UUID.randomUUID();
        payeeId = UUID.randomUUID();
        meetingId = UUID.randomUUID();
        testAmount = new BigDecimal("100.00");
        
        // Setup test payer
        payer = new User();
        payer.setId(payerId);
        payer.setFirstName("John");
        payer.setLastName("Client");
        payer.setEmail("client@example.com");
        payer.setRole(Role.USER);
        payer.setEmailVerified(true);
        
        // Setup test payee
        payee = new User();
        payee.setId(payeeId);
        payee.setFirstName("Jane");
        payee.setLastName("Lawyer");
        payee.setEmail("lawyer@example.com");
        payee.setRole(Role.LAWYER);
        payee.setEmailVerified(true);
        
        // Setup test payment
        payment = new Payment();
        payment.setId(testId);
        payment.setPayer(payer);
        payment.setPayee(payee);
        payment.setMeetingId(meetingId);
        payment.setAmount(testAmount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setTransactionId("TXN123456");
    }

    @Test
    @DisplayName("Should create payment with default constructor")
    void testDefaultConstructor() {
        Payment defaultPayment = new Payment();
        
        assertNotNull(defaultPayment);
        assertNull(defaultPayment.getId());
        assertNull(defaultPayment.getPayer());
        assertNull(defaultPayment.getPayee());
        assertNull(defaultPayment.getMeetingId());
        assertNull(defaultPayment.getAmount());
        assertEquals(PaymentStatus.PENDING, defaultPayment.getStatus()); // Default value
        assertNull(defaultPayment.getPaymentMethod());
        assertNull(defaultPayment.getTransactionId());
        assertNull(defaultPayment.getPaymentDate());
        assertNull(defaultPayment.getReleaseAt());
        assertNull(defaultPayment.getCreatedAt());
        assertNull(defaultPayment.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create payment with all args constructor")
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime paymentDate = now.minusHours(1);
        OffsetDateTime releaseAt = now.plusDays(7);
        
        Payment constructedPayment = new Payment(
            testId, payer, payee, meetingId, testAmount, PaymentStatus.PAID,
            PaymentMethod.MFS, "TXN789012", paymentDate, releaseAt, now, now
        );
        
        assertEquals(testId, constructedPayment.getId());
        assertEquals(payer, constructedPayment.getPayer());
        assertEquals(payee, constructedPayment.getPayee());
        assertEquals(meetingId, constructedPayment.getMeetingId());
        assertEquals(testAmount, constructedPayment.getAmount());
        assertEquals(PaymentStatus.PAID, constructedPayment.getStatus());
        assertEquals(PaymentMethod.MFS, constructedPayment.getPaymentMethod());
        assertEquals("TXN789012", constructedPayment.getTransactionId());
        assertEquals(paymentDate, constructedPayment.getPaymentDate());
        assertEquals(releaseAt, constructedPayment.getReleaseAt());
        assertEquals(now, constructedPayment.getCreatedAt());
        assertEquals(now, constructedPayment.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle id getter and setter")
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        payment.setId(newId);
        assertEquals(newId, payment.getId());
    }

    @Test
    @DisplayName("Should handle payer getter and setter")
    void testPayerGetterAndSetter() {
        User newPayer = new User();
        newPayer.setId(UUID.randomUUID());
        newPayer.setEmail("newclient@example.com");
        newPayer.setRole(Role.USER);
        
        payment.setPayer(newPayer);
        assertEquals(newPayer, payment.getPayer());
    }

    @Test
    @DisplayName("Should handle payee getter and setter")
    void testPayeeGetterAndSetter() {
        User newPayee = new User();
        newPayee.setId(UUID.randomUUID());
        newPayee.setEmail("newlawyer@example.com");
        newPayee.setRole(Role.LAWYER);
        
        payment.setPayee(newPayee);
        assertEquals(newPayee, payment.getPayee());
    }

    @Test
    @DisplayName("Should handle meetingId getter and setter")
    void testmeetingIdGetterAndSetter() {
        UUID newmeetingId = UUID.randomUUID();
        payment.setMeetingId(newmeetingId);
        assertEquals(newmeetingId, payment.getMeetingId());
    }

    @Test
    @DisplayName("Should handle amount getter and setter")
    void testAmountGetterAndSetter() {
        BigDecimal newAmount = new BigDecimal("250.75");
        payment.setAmount(newAmount);
        assertEquals(newAmount, payment.getAmount());
    }

    @Test
    @DisplayName("Should handle amount with null value")
    void testAmountWithNullValue() {
        payment.setAmount(null);
        assertNull(payment.getAmount());
    }

    @Test
    @DisplayName("Should handle amount with zero value")
    void testAmountWithZeroValue() {
        BigDecimal zeroAmount = BigDecimal.ZERO;
        payment.setAmount(zeroAmount);
        assertEquals(zeroAmount, payment.getAmount());
    }

    @Test
    @DisplayName("Should handle amount with large value")
    void testAmountWithLargeValue() {
        BigDecimal largeAmount = new BigDecimal("99999999.99");
        payment.setAmount(largeAmount);
        assertEquals(largeAmount, payment.getAmount());
    }

    @Test
    @DisplayName("Should handle amount with precision")
    void testAmountWithPrecision() {
        BigDecimal preciseAmount = new BigDecimal("123.45");
        payment.setAmount(preciseAmount);
        assertEquals(preciseAmount, payment.getAmount());
    }

    @Test
    @DisplayName("Should handle status getter and setter")
    void testStatusGetterAndSetter() {
        payment.setStatus(PaymentStatus.PAID);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        
        payment.setStatus(PaymentStatus.RELEASED);
        assertEquals(PaymentStatus.RELEASED, payment.getStatus());
        
        payment.setStatus(PaymentStatus.REFUNDED);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        
        payment.setStatus(PaymentStatus.CANCELED);
        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    }

    @Test
    @DisplayName("Should handle paymentMethod getter and setter")
    void testPaymentMethodGetterAndSetter() {
        payment.setPaymentMethod(PaymentMethod.CARD);
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        
        payment.setPaymentMethod(PaymentMethod.MFS);
        assertEquals(PaymentMethod.MFS, payment.getPaymentMethod());
    }

    @Test
    @DisplayName("Should handle paymentMethod with null value")
    void testPaymentMethodWithNullValue() {
        payment.setPaymentMethod(null);
        assertNull(payment.getPaymentMethod());
    }

    @Test
    @DisplayName("Should handle transactionId getter and setter")
    void testTransactionIdGetterAndSetter() {
        String newTransactionId = "TXN987654321";
        payment.setTransactionId(newTransactionId);
        assertEquals(newTransactionId, payment.getTransactionId());
    }

    @Test
    @DisplayName("Should handle transactionId with null value")
    void testTransactionIdWithNullValue() {
        payment.setTransactionId(null);
        assertNull(payment.getTransactionId());
    }

    @Test
    @DisplayName("Should handle transactionId with empty string")
    void testTransactionIdWithEmptyString() {
        payment.setTransactionId("");
        assertEquals("", payment.getTransactionId());
    }

    @Test
    @DisplayName("Should handle paymentDate getter and setter")
    void testPaymentDateGetterAndSetter() {
        OffsetDateTime newPaymentDate = OffsetDateTime.now().minusHours(2);
        payment.setPaymentDate(newPaymentDate);
        assertEquals(newPaymentDate, payment.getPaymentDate());
    }

    @Test
    @DisplayName("Should handle paymentDate with null value")
    void testPaymentDateWithNullValue() {
        payment.setPaymentDate(null);
        assertNull(payment.getPaymentDate());
    }

    @Test
    @DisplayName("Should handle releaseAt getter and setter")
    void testReleaseAtGetterAndSetter() {
        OffsetDateTime newReleaseAt = OffsetDateTime.now().plusDays(14);
        payment.setReleaseAt(newReleaseAt);
        assertEquals(newReleaseAt, payment.getReleaseAt());
    }

    @Test
    @DisplayName("Should handle releaseAt with null value")
    void testReleaseAtWithNullValue() {
        payment.setReleaseAt(null);
        assertNull(payment.getReleaseAt());
    }

    @Test
    @DisplayName("Should handle createdAt getter and setter")
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        payment.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, payment.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle updatedAt getter and setter")
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        payment.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        Payment payment1 = new Payment();
        payment1.setId(testId);
        payment1.setPayer(payer);
        payment1.setPayee(payee);
        payment1.setMeetingId(meetingId);
        payment1.setAmount(testAmount);
        payment1.setStatus(PaymentStatus.PENDING);
        
        Payment payment2 = new Payment();
        payment2.setId(testId);
        payment2.setPayer(payer);
        payment2.setPayee(payee);
        payment2.setMeetingId(meetingId);
        payment2.setAmount(testAmount);
        payment2.setStatus(PaymentStatus.PENDING);
        
        Payment payment3 = new Payment();
        payment3.setId(UUID.randomUUID());
        payment3.setPayer(payer);
        payment3.setPayee(payee);
        payment3.setMeetingId(meetingId);
        payment3.setAmount(new BigDecimal("200.00"));
        payment3.setStatus(PaymentStatus.PAID);
        
        // Test equals
        assertEquals(payment1, payment2);
        assertNotEquals(payment1, payment3);
        assertNotEquals(null, payment1);
        assertEquals(payment1, payment1);
        
        // Test hashCode
        assertEquals(payment1.hashCode(), payment2.hashCode());
        assertNotEquals(payment1.hashCode(), payment3.hashCode());
    }

    @Test
    @DisplayName("Should handle equals with null values")
    void testEqualsWithNullValues() {
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        
        assertEquals(payment1, payment2);
        assertEquals(payment1.hashCode(), payment2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        OffsetDateTime now = OffsetDateTime.now();
        payment.setPaymentDate(now);
        payment.setReleaseAt(now.plusDays(7));
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        String toString = payment.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("id=" + testId));
        assertTrue(toString.contains("meetingId=" + meetingId));
        assertTrue(toString.contains("amount=" + testAmount));
        assertTrue(toString.contains("status=PENDING"));
        assertTrue(toString.contains("paymentMethod=CARD"));
        assertTrue(toString.contains("transactionId=TXN123456"));
    }

    @Test
    @DisplayName("Should handle default status value")
    void testDefaultStatusValue() {
        Payment newPayment = new Payment();
        assertEquals(PaymentStatus.PENDING, newPayment.getStatus());
    }

    @Test
    @DisplayName("Should handle all payment statuses")
    void testAllPaymentStatuses() {
        payment.setStatus(PaymentStatus.PENDING);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        
        payment.setStatus(PaymentStatus.PAID);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        
        payment.setStatus(PaymentStatus.RELEASED);
        assertEquals(PaymentStatus.RELEASED, payment.getStatus());
        
        payment.setStatus(PaymentStatus.REFUNDED);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        
        payment.setStatus(PaymentStatus.CANCELED);
        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    }

    @Test
    @DisplayName("Should handle all payment methods including null transitions")
    void testAllPaymentMethods() {
        // Test setting each payment method
        payment.setPaymentMethod(PaymentMethod.CARD);
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        
        payment.setPaymentMethod(PaymentMethod.MFS);
        assertEquals(PaymentMethod.MFS, payment.getPaymentMethod());
        
        // Test transitioning from one method to another
        payment.setPaymentMethod(PaymentMethod.CARD);
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        
        // Test setting to null after having a value
        payment.setPaymentMethod(null);
        assertNull(payment.getPaymentMethod());
        
        // Test setting back to a value after null
        payment.setPaymentMethod(PaymentMethod.MFS);
        assertEquals(PaymentMethod.MFS, payment.getPaymentMethod());
    }

    @Test
    @DisplayName("Should handle payment with minimal data")
    void testPaymentWithMinimalData() {
        Payment minimalPayment = new Payment();
        minimalPayment.setPayer(payer);
        minimalPayment.setPayee(payee);
        minimalPayment.setMeetingId(meetingId);
        minimalPayment.setAmount(new BigDecimal("1.00"));
        
        assertEquals(payer, minimalPayment.getPayer());
        assertEquals(payee, minimalPayment.getPayee());
        assertEquals(meetingId, minimalPayment.getMeetingId());
        assertEquals(new BigDecimal("1.00"), minimalPayment.getAmount());
        assertEquals(PaymentStatus.PENDING, minimalPayment.getStatus()); // Default value
        assertNull(minimalPayment.getId()); // Not set
        assertNull(minimalPayment.getPaymentMethod()); // Not set
        assertNull(minimalPayment.getTransactionId()); // Not set
    }

    @Test
    @DisplayName("Should handle payment entity fields validation")
    void testPaymentEntityFields() {
        // Test that all required fields are properly configured
        assertNotNull(payment.getId());
        assertNotNull(payment.getPayer());
        assertNotNull(payment.getPayee());
        assertNotNull(payment.getMeetingId());
        assertNotNull(payment.getAmount());
        assertNotNull(payment.getStatus());
        // Optional fields can be null
    }

    @Test
    @DisplayName("Should handle payment with various amounts")
    void testPaymentWithVariousAmounts() {
        BigDecimal[] amounts = {
            new BigDecimal("0.01"),
            new BigDecimal("1.00"),
            new BigDecimal("99.99"),
            new BigDecimal("100.00"),
            new BigDecimal("999.99"),
            new BigDecimal("1000.00"),
            new BigDecimal("9999.99")
        };
        
        for (BigDecimal amount : amounts) {
            payment.setAmount(amount);
            assertEquals(amount, payment.getAmount());
        }
    }

    @Test
    @DisplayName("Should handle payment timestamps")
    void testPaymentWithTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime past = now.minusHours(1);
        OffsetDateTime future = now.plusHours(1);
        
        // Test with current time
        payment.setPaymentDate(now);
        payment.setReleaseAt(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        assertEquals(now, payment.getPaymentDate());
        assertEquals(now, payment.getReleaseAt());
        assertEquals(now, payment.getCreatedAt());
        assertEquals(now, payment.getUpdatedAt());
        
        // Test with past time
        payment.setPaymentDate(past);
        payment.setCreatedAt(past);
        
        assertEquals(past, payment.getPaymentDate());
        assertEquals(past, payment.getCreatedAt());
        
        // Test with future time
        payment.setReleaseAt(future);
        payment.setUpdatedAt(future);
        
        assertEquals(future, payment.getReleaseAt());
        assertEquals(future, payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle payment equality with different statuses")
    void testPaymentEqualityWithDifferentStatuses() {
        Payment payment1 = new Payment();
        payment1.setId(testId);
        payment1.setPayer(payer);
        payment1.setPayee(payee);
        payment1.setMeetingId(meetingId);
        payment1.setAmount(testAmount);
        payment1.setStatus(PaymentStatus.PENDING);
        
        Payment payment2 = new Payment();
        payment2.setId(testId);
        payment2.setPayer(payer);
        payment2.setPayee(payee);
        payment2.setMeetingId(meetingId);
        payment2.setAmount(testAmount);
        payment2.setStatus(PaymentStatus.PAID); // Different status
        
        // With Lombok @Data, objects with different field values are not equal
        assertNotEquals(payment1, payment2);
    }

    @Test
    @DisplayName("Should handle payment with null payer")
    void testPaymentWithNullPayer() {
        payment.setPayer(null);
        assertNull(payment.getPayer());
    }

    @Test
    @DisplayName("Should handle payment with null payee")
    void testPaymentWithNullPayee() {
        payment.setPayee(null);
        assertNull(payment.getPayee());
    }

    @Test
    @DisplayName("Should handle transactionId edge cases")
    void testTransactionIdEdgeCases() {
        // Test with whitespace-only content
        payment.setTransactionId("   ");
        assertEquals("   ", payment.getTransactionId());
        
        // Test with special characters
        payment.setTransactionId("TXN-123_456@789");
        assertEquals("TXN-123_456@789", payment.getTransactionId());
        
        // Test with long transaction ID
        String longTxnId = "TXN" + "1".repeat(100);
        payment.setTransactionId(longTxnId);
        assertEquals(longTxnId, payment.getTransactionId());
    }

    @Test
    @DisplayName("Should handle amount decimal precision")
    void testAmountDecimalPrecision() {
        // Test various decimal precisions
        BigDecimal amount1 = new BigDecimal("99.99");
        BigDecimal amount2 = new BigDecimal("100.00");
        BigDecimal amount3 = new BigDecimal("0.01");
        BigDecimal amount4 = new BigDecimal("123.45");
        
        payment.setAmount(amount1);
        assertEquals(amount1, payment.getAmount());
        
        payment.setAmount(amount2);
        assertEquals(amount2, payment.getAmount());
        
        payment.setAmount(amount3);
        assertEquals(amount3, payment.getAmount());
        
        payment.setAmount(amount4);
        assertEquals(amount4, payment.getAmount());
    }

    @Test
    @DisplayName("Should handle payment relationships")
    void testPaymentRelationships() {
        // Test that payment correctly maintains relationships with users
        assertEquals(payer, payment.getPayer());
        assertEquals(payee, payment.getPayee());
        
        // Test that changing user properties doesn't affect payment
        String originalPayerEmail = payer.getEmail();
        payer.setEmail("newemail@example.com");
        
        assertEquals(payer, payment.getPayer());
        assertEquals("newemail@example.com", payment.getPayer().getEmail());
        
        // Restore original email
        payer.setEmail(originalPayerEmail);
    }

    @Test
    @DisplayName("Should handle constructor with null values")
    void testConstructorWithNullValues() {
        Payment constructedPayment = new Payment(
            testId, null, null, meetingId, testAmount, PaymentStatus.PENDING,
            null, null, null, null, null, null
        );
        
        assertEquals(testId, constructedPayment.getId());
        assertNull(constructedPayment.getPayer());
        assertNull(constructedPayment.getPayee());
        assertEquals(meetingId, constructedPayment.getMeetingId());
        assertEquals(testAmount, constructedPayment.getAmount());
        assertEquals(PaymentStatus.PENDING, constructedPayment.getStatus());
        assertNull(constructedPayment.getPaymentMethod());
        assertNull(constructedPayment.getTransactionId());
        assertNull(constructedPayment.getPaymentDate());
        assertNull(constructedPayment.getReleaseAt());
        assertNull(constructedPayment.getCreatedAt());
        assertNull(constructedPayment.getUpdatedAt());
    }
}