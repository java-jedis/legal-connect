package com.javajedis.legalconnect.payment.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("CreatePaymentDTO Tests")
class CreatePaymentDTOTest {

    private Validator validator;
    private CreatePaymentDTO createPaymentDTO;
    private UUID testPayerId;
    private UUID testPayeeId;
    private UUID testmeetingId;
    private BigDecimal testAmount;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        createPaymentDTO = new CreatePaymentDTO();
        testPayerId = UUID.randomUUID();
        testPayeeId = UUID.randomUUID();
        testmeetingId = UUID.randomUUID();
        testAmount = new BigDecimal("100.00");
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void testNoArgsConstructor() {
        CreatePaymentDTO dto = new CreatePaymentDTO();
        assertNotNull(dto);
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void testAllArgsConstructor() {
        CreatePaymentDTO dto = new CreatePaymentDTO(testPayerId, testPayeeId, testmeetingId, testAmount);
        
        assertEquals(testPayerId, dto.getPayerId());
        assertEquals(testPayeeId, dto.getPayeeId());
        assertEquals(testmeetingId, dto.getMeetingId());
        assertEquals(testAmount, dto.getAmount());
    }

    @Test
    @DisplayName("Should set and get payer ID")
    void testPayerIdGetterSetter() {
        createPaymentDTO.setPayerId(testPayerId);
        assertEquals(testPayerId, createPaymentDTO.getPayerId());
    }

    @Test
    @DisplayName("Should set and get payee ID")
    void testPayeeIdGetterSetter() {
        createPaymentDTO.setPayeeId(testPayeeId);
        assertEquals(testPayeeId, createPaymentDTO.getPayeeId());
    }

    @Test
    @DisplayName("Should set and get reference ID")
    void testmeetingIdGetterSetter() {
        createPaymentDTO.setMeetingId(testmeetingId);
        assertEquals(testmeetingId, createPaymentDTO.getMeetingId());
    }

    @Test
    @DisplayName("Should set and get amount")
    void testAmountGetterSetter() {
        createPaymentDTO.setAmount(testAmount);
        assertEquals(testAmount, createPaymentDTO.getAmount());
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidData_NoViolations() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(testAmount);

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with valid data");
    }

    @Test
    @DisplayName("Should fail validation when payer ID is null")
    void testNullPayerId_ValidationError() {
        createPaymentDTO.setPayerId(null);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(testAmount);

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("payerId") && 
            v.getMessage().equals("Payer ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when payee ID is null")
    void testNullPayeeId_ValidationError() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(null);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(testAmount);

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("payeeId") && 
            v.getMessage().equals("Payee ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when reference ID is null")
    void testNullmeetingId_ValidationError() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(null);
        createPaymentDTO.setAmount(testAmount);

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("meetingId") && 
            v.getMessage().equals("Reference ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when amount is null")
    void testNullAmount_ValidationError() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(null);

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("amount") && 
            v.getMessage().equals("Amount is required")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00", "-100.50"})
    @DisplayName("Should fail validation when amount is zero or negative")
    void testInvalidAmount_ValidationError(String amountValue) {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(new BigDecimal(amountValue));

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("amount") && 
            v.getMessage().equals("Amount must be positive")));
    }

    @Test
    @DisplayName("Should pass validation with minimum valid amount")
    void testMinimumValidAmount_NoViolations() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(new BigDecimal("0.01"));

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with minimum valid amount");
    }

    @Test
    @DisplayName("Should fail validation when amount has too many decimal places")
    void testTooManyDecimalPlaces_ValidationError() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(new BigDecimal("100.123")); // 3 decimal places

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("amount") && 
            v.getMessage().equals("Amount must have at most 10 digits before decimal and 2 digits after")));
    }

    @Test
    @DisplayName("Should fail validation when amount has too many integer digits")
    void testTooManyIntegerDigits_ValidationError() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(new BigDecimal("12345678901.00")); // 11 integer digits

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("amount") && 
            v.getMessage().equals("Amount must have at most 10 digits before decimal and 2 digits after")));
    }

    @Test
    @DisplayName("Should pass validation with maximum valid amount")
    void testMaximumValidAmount_NoViolations() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(new BigDecimal("9999999999.99")); // 10 integer digits, 2 decimal places

        Set<ConstraintViolation<CreatePaymentDTO>> violations = validator.validate(createPaymentDTO);
        
        assertTrue(violations.isEmpty(), "Should have no validation violations with maximum valid amount");
    }

    @Test
    @DisplayName("Should test equals and hashCode methods")
    void testEqualsAndHashCode() {
        CreatePaymentDTO dto1 = new CreatePaymentDTO(testPayerId, testPayeeId, testmeetingId, testAmount);
        CreatePaymentDTO dto2 = new CreatePaymentDTO(testPayerId, testPayeeId, testmeetingId, testAmount);
        CreatePaymentDTO dto3 = new CreatePaymentDTO(UUID.randomUUID(), testPayeeId, testmeetingId, testAmount);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        createPaymentDTO.setPayerId(testPayerId);
        createPaymentDTO.setPayeeId(testPayeeId);
        createPaymentDTO.setMeetingId(testmeetingId);
        createPaymentDTO.setAmount(testAmount);

        String toString = createPaymentDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("CreatePaymentDTO"));
        assertTrue(toString.contains(testPayerId.toString()));
        assertTrue(toString.contains(testPayeeId.toString()));
        assertTrue(toString.contains(testmeetingId.toString()));
        assertTrue(toString.contains(testAmount.toString()));
    }
}