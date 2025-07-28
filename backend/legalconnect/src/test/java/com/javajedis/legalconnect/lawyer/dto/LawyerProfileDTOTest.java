package com.javajedis.legalconnect.lawyer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class LawyerProfileDTOTest {

    private Validator validator;
    private LawyerProfileDTO lawyerProfileDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        lawyerProfileDTO = new LawyerProfileDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(lawyerProfileDTO);
        assertNull(lawyerProfileDTO.getFirm());
        assertNull(lawyerProfileDTO.getYearsOfExperience());
        assertNull(lawyerProfileDTO.getBarCertificateNumber());
        assertNull(lawyerProfileDTO.getPracticingCourt());
        assertNull(lawyerProfileDTO.getDivision());
        assertNull(lawyerProfileDTO.getDistrict());
        assertNull(lawyerProfileDTO.getBio());
        assertNull(lawyerProfileDTO.getSpecializations());
        assertNull(lawyerProfileDTO.getHourlyCharge());
    }

    @Test
    void testAllArgsConstructor() {
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW, SpecializationType.CIVIL_LAW);
        BigDecimal hourlyCharge = new BigDecimal("100.00");
        
        LawyerProfileDTO dto = new LawyerProfileDTO(
            "Test Firm",
            5,
            "BAR123456",
            PracticingCourt.SUPREME_COURT,
            Division.DHAKA,
            District.DHAKA,
            "Test bio",
            specializations,
            hourlyCharge
        );

        assertEquals("Test Firm", dto.getFirm());
        assertEquals(5, dto.getYearsOfExperience());
        assertEquals("BAR123456", dto.getBarCertificateNumber());
        assertEquals(PracticingCourt.SUPREME_COURT, dto.getPracticingCourt());
        assertEquals(Division.DHAKA, dto.getDivision());
        assertEquals(District.DHAKA, dto.getDistrict());
        assertEquals("Test bio", dto.getBio());
        assertEquals(specializations, dto.getSpecializations());
        assertEquals(hourlyCharge, dto.getHourlyCharge());
    }

    @Test
    void testSettersAndGetters() {
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CORPORATE_LAW);
        BigDecimal hourlyCharge = new BigDecimal("150.50");
        
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(10);
        lawyerProfileDTO.setBarCertificateNumber("BAR789012");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        lawyerProfileDTO.setDivision(Division.CHATTOGRAM);
        lawyerProfileDTO.setDistrict(District.CHATTOGRAM);
        lawyerProfileDTO.setBio("Test bio description");
        lawyerProfileDTO.setSpecializations(specializations);
        lawyerProfileDTO.setHourlyCharge(hourlyCharge);

        assertEquals("Test Firm", lawyerProfileDTO.getFirm());
        assertEquals(10, lawyerProfileDTO.getYearsOfExperience());
        assertEquals("BAR789012", lawyerProfileDTO.getBarCertificateNumber());
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, lawyerProfileDTO.getPracticingCourt());
        assertEquals(Division.CHATTOGRAM, lawyerProfileDTO.getDivision());
        assertEquals(District.CHATTOGRAM, lawyerProfileDTO.getDistrict());
        assertEquals("Test bio description", lawyerProfileDTO.getBio());
        assertEquals(specializations, lawyerProfileDTO.getSpecializations());
        assertEquals(hourlyCharge, lawyerProfileDTO.getHourlyCharge());
    }

    @Test
    void testValidLawyerProfileDTO() {
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.FAMILY_LAW);
        
        lawyerProfileDTO.setFirm("Valid Firm");
        lawyerProfileDTO.setYearsOfExperience(3);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setBio("Valid bio");
        lawyerProfileDTO.setSpecializations(specializations);

        var violations = validator.validate(lawyerProfileDTO);
        assertTrue(violations.isEmpty(), "Should have no validation violations");
    }

    @Test
    void testFirmValidation_Blank() {
        lawyerProfileDTO.setFirm("");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firm")));
    }

    @Test
    void testFirmValidation_TooShort() {
        lawyerProfileDTO.setFirm("A");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firm")));
    }

    @Test
    void testYearsOfExperienceValidation_Null() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("yearsOfExperience")));
    }

    @Test
    void testYearsOfExperienceValidation_Negative() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(-1);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("yearsOfExperience")));
    }

    @Test
    void testYearsOfExperienceValidation_TooHigh() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(51);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("yearsOfExperience")));
    }

    @Test
    void testBarCertificateNumberValidation_Blank() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("barCertificateNumber")));
    }

    @Test
    void testBarCertificateNumberValidation_TooShort() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("1234");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("barCertificateNumber")));
    }

    @Test
    void testSpecializationsValidation_Empty() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Collections.emptyList());

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("specializations")));
    }

    @Test
    void testBioValidation_TooLong() {
        String longBio = "A".repeat(2001);
        
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setBio(longBio);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bio")));
    }

    @Test
    void testEqualsAndHashCode() {
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW);
        BigDecimal hourlyCharge = new BigDecimal("100.00");
        
        LawyerProfileDTO dto1 = new LawyerProfileDTO(
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", specializations, hourlyCharge
        );
        
        LawyerProfileDTO dto2 = new LawyerProfileDTO(
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", specializations, hourlyCharge
        );
        
        LawyerProfileDTO dto3 = new LawyerProfileDTO(
            "Different Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", specializations, hourlyCharge
        );

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setHourlyCharge(new BigDecimal("125.75"));
        
        String toString = lawyerProfileDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Test Firm"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("BAR123456"));
        assertTrue(toString.contains("125.75"));
    }

    static Stream<Arguments> hourlyChargeValidationTestCases() {
        return Stream.of(
            Arguments.of(new BigDecimal("150.50"), true, "Valid positive decimal hourly charge should pass validation"),
            Arguments.of(new BigDecimal("0.01"), true, "Minimum valid hourly charge (0.01) should pass validation"),
            Arguments.of(new BigDecimal("-10.00"), false, "Negative hourly charge should fail validation"),
            Arguments.of(new BigDecimal("0.00"), false, "Zero hourly charge should fail validation")
        );
    }

    @ParameterizedTest
    @MethodSource("hourlyChargeValidationTestCases")
    void testHourlyChargeValidation(BigDecimal hourlyCharge, boolean shouldBeValid, String description) {
        // Set up valid profile data
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));
        lawyerProfileDTO.setHourlyCharge(hourlyCharge);

        var violations = validator.validate(lawyerProfileDTO);
        
        if (shouldBeValid) {
            assertTrue(violations.isEmpty(), description);
        } else {
            assertFalse(violations.isEmpty(), description);
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hourlyCharge")));
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("positive")));
        }
    }

    @Test
    void testHourlyChargeValidation_TooManyIntegerDigits() {
        // Set up valid profile data
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));
        // 9 integer digits (exceeds the 8 digit limit)
        lawyerProfileDTO.setHourlyCharge(new BigDecimal("123456789.50"));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hourlyCharge")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("digits")));
    }

    @Test
    void testHourlyChargeValidation_TooManyFractionDigits() {
        // Set up valid profile data
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));
        // 3 fraction digits (exceeds the 2 digit limit)
        lawyerProfileDTO.setHourlyCharge(new BigDecimal("150.123"));

        var violations = validator.validate(lawyerProfileDTO);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("hourlyCharge")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("digits")));
    }

    @Test
    void testHourlyChargeValidation_NullValue() {
        // Set up valid profile data
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));
        lawyerProfileDTO.setHourlyCharge(null);

        var violations = validator.validate(lawyerProfileDTO);
        assertTrue(violations.isEmpty(), "Null hourly charge should be valid (optional field)");
    }

    @Test
    void testHourlyChargeValidation_MaxValidIntegerDigits() {
        // Set up valid profile data
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));
        // 8 integer digits (maximum allowed)
        lawyerProfileDTO.setHourlyCharge(new BigDecimal("12345678.99"));

        var violations = validator.validate(lawyerProfileDTO);
        assertTrue(violations.isEmpty(), "Maximum valid integer digits (8) should pass validation");
    }

    @Test
    void testHourlyChargeValidation_ValidTwoDecimalPlaces() {
        // Set up valid profile data
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(5);
        lawyerProfileDTO.setBarCertificateNumber("BAR123456");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyerProfileDTO.setDivision(Division.DHAKA);
        lawyerProfileDTO.setDistrict(District.DHAKA);
        lawyerProfileDTO.setSpecializations(Arrays.asList(SpecializationType.CRIMINAL_LAW));
        lawyerProfileDTO.setHourlyCharge(new BigDecimal("99.99"));

        var violations = validator.validate(lawyerProfileDTO);
        assertTrue(violations.isEmpty(), "Valid two decimal places should pass validation");
    }
} 