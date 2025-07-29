package com.javajedis.legalconnect.payment.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.javajedis.legalconnect.payment.PaymentMethod;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("CompletePaymentDTO Tests")
class CompletePaymentDTOTest {

    private Validator validator;
    private CompletePaymentDTO completePaymentDTO;
    private UUID testId;
    private PaymentMethod testPaymentMethod;
    private String testTransactionId;
    private OffsetDateTime testPaymentDate;
    private OffsetDateTime testReleaseAt;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        completePaymentDTO = new CompletePaymentDTO();
        testId = UUID.randomUUID();
        testPaymentMethod = PaymentMethod.CARD;
        testTransactionId = "TXN123456789";
        testPaymentDate = OffsetDateTime.now();
        testReleaseAt = testPaymentDate.plusDays(7);
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        CompletePaymentDTO dto = new CompletePaymentDTO();
        assertNotNull(dto);
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        CompletePaymentDTO dto = new CompletePaymentDTO(testId, testPaymentMethod, testTransactionId, testPaymentDate, testReleaseAt);
        
        assertEquals(testId, dto.getId());
        assertEquals(testPaymentMethod, dto.getPaymentMethod());
        assertEquals(testTransactionId, dto.getTransactionId());
        assertEquals(testPaymentDate, dto.getPaymentDate());
        assertEquals(testReleaseAt, dto.getReleaseAt());
    }

    @Test
    @DisplayName("Should set and get payment ID")
    void testIdGetterSetter() {
        completePaymentDTO.setId(testId);
        assertEquals(testId, completePaymentDTO.getId());
    }

    @Test
    @DisplayName("Should set and get payment method")
    void testPaymentMethodGetterSetter() {
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        assertEquals(testPaymentMethod, completePaymentDTO.getPaymentMethod());
    }

    @Test
    @DisplayName("Should set and get transaction ID")
    void testTransactionIdGetterSetter() {
        completePaymentDTO.setTransactionId(testTransactionId);
        assertEquals(testTransactionId, completePaymentDTO.getTransactionId());
    }

    @Test
    @DisplayName("Should set and get payment date")
    void testPaymentDateGetterSetter() {
        completePaymentDTO.setPaymentDate(testPaymentDate);
        assertEquals(testPaymentDate, completePaymentDTO.getPaymentDate());
    }

    @Test
    @DisplayName("Should set and get release date")
    void testReleaseAtGetterSetter() {
        completePaymentDTO.setReleaseAt(testReleaseAt);
        assertEquals(testReleaseAt, completePaymentDTO.getReleaseAt());
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidData_NoViolations() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with valid data");
    }

    @Test
    @DisplayName("Should fail validation when payment ID is null")
    void testNullId_ValidationError() {
        completePaymentDTO.setId(null);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("id") && 
            v.getMessage().equals("Payment ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when payment method is null")
    void testNullPaymentMethod_ValidationError() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(null);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("paymentMethod") && 
            v.getMessage().equals("Payment method is required")));
    }

    @Test
    @DisplayName("Should fail validation when transaction ID is null")
    void testNullTransactionId_ValidationError() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(null);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("transactionId") && 
            v.getMessage().equals("Transaction ID is required")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should fail validation when transaction ID is blank")
    void testBlankTransactionId_ValidationError(String transactionId) {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(transactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("transactionId") && 
            v.getMessage().equals("Transaction ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when transaction ID is too long")
    void testTooLongTransactionId_ValidationError() {
        String longTransactionId = "A".repeat(256); // 256 characters, exceeds max of 255
        
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(longTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("transactionId") && 
            v.getMessage().equals("Transaction ID must be between 1 and 255 characters")));
    }

    @Test
    @DisplayName("Should pass validation with maximum length transaction ID")
    void testMaxLengthTransactionId_NoViolations() {
        String maxLengthTransactionId = "A".repeat(255); // 255 characters, at the limit
        
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(maxLengthTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with maximum length transaction ID");
    }

    @Test
    @DisplayName("Should pass validation with minimum length transaction ID")
    void testMinLengthTransactionId_NoViolations() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId("A"); // 1 character, minimum valid
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with minimum length transaction ID");
    }

    @Test
    @DisplayName("Should fail validation when payment date is null")
    void testNullPaymentDate_ValidationError() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(null);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("paymentDate") && 
            v.getMessage().equals("Payment date is required")));
    }

    @Test
    @DisplayName("Should fail validation when release date is null")
    void testNullReleaseAt_ValidationError() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(null);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("releaseAt") && 
            v.getMessage().equals("Release date is required")));
    }

    @Test
    @DisplayName("Should test all payment method enum values")
    void testAllPaymentMethodValues_NoViolations() {
        for (PaymentMethod method : PaymentMethod.values()) {
            completePaymentDTO.setId(testId);
            completePaymentDTO.setPaymentMethod(method);
            completePaymentDTO.setTransactionId(testTransactionId);
            completePaymentDTO.setPaymentDate(testPaymentDate);
            completePaymentDTO.setReleaseAt(testReleaseAt);

            Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
            
            assertTrue(violations.isEmpty(), 
                "Should have no validation violations with payment method: " + method);
        }
    }

    @Test
    @DisplayName("Should handle past payment date")
    void testPastPaymentDate_NoViolations() {
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(1);
        
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(pastDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with past payment date");
    }

    @Test
    @DisplayName("Should handle future release date")
    void testFutureReleaseDate_NoViolations() {
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(30);
        
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(futureDate);

        Set<ConstraintViolation<CompletePaymentDTO>> violations = validator.validate(completePaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with future release date");
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        CompletePaymentDTO dto1 = new CompletePaymentDTO(testId, testPaymentMethod, testTransactionId, testPaymentDate, testReleaseAt);
        CompletePaymentDTO dto2 = new CompletePaymentDTO(testId, testPaymentMethod, testTransactionId, testPaymentDate, testReleaseAt);
        CompletePaymentDTO dto3 = new CompletePaymentDTO(UUID.randomUUID(), testPaymentMethod, testTransactionId, testPaymentDate, testReleaseAt);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        completePaymentDTO.setId(testId);
        completePaymentDTO.setPaymentMethod(testPaymentMethod);
        completePaymentDTO.setTransactionId(testTransactionId);
        completePaymentDTO.setPaymentDate(testPaymentDate);
        completePaymentDTO.setReleaseAt(testReleaseAt);

        String toString = completePaymentDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("CompletePaymentDTO"));
        assertTrue(toString.contains(testId.toString()));
        assertTrue(toString.contains(testPaymentMethod.toString()));
        assertTrue(toString.contains(testTransactionId));
    }
}