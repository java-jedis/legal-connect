package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    void testAllArgsConstructor() {
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW, SpecializationType.CIVIL_LAW);
        
        LawyerProfileDTO dto = new LawyerProfileDTO(
            "Test Firm",
            5,
            "BAR123456",
            PracticingCourt.SUPREME_COURT,
            Division.DHAKA,
            District.DHAKA,
            "Test bio",
            specializations
        );

        assertEquals("Test Firm", dto.getFirm());
        assertEquals(5, dto.getYearsOfExperience());
        assertEquals("BAR123456", dto.getBarCertificateNumber());
        assertEquals(PracticingCourt.SUPREME_COURT, dto.getPracticingCourt());
        assertEquals(Division.DHAKA, dto.getDivision());
        assertEquals(District.DHAKA, dto.getDistrict());
        assertEquals("Test bio", dto.getBio());
        assertEquals(specializations, dto.getSpecializations());
    }

    @Test
    void testSettersAndGetters() {
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CORPORATE_LAW);
        
        lawyerProfileDTO.setFirm("Test Firm");
        lawyerProfileDTO.setYearsOfExperience(10);
        lawyerProfileDTO.setBarCertificateNumber("BAR789012");
        lawyerProfileDTO.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        lawyerProfileDTO.setDivision(Division.CHATTOGRAM);
        lawyerProfileDTO.setDistrict(District.CHATTOGRAM);
        lawyerProfileDTO.setBio("Test bio description");
        lawyerProfileDTO.setSpecializations(specializations);

        assertEquals("Test Firm", lawyerProfileDTO.getFirm());
        assertEquals(10, lawyerProfileDTO.getYearsOfExperience());
        assertEquals("BAR789012", lawyerProfileDTO.getBarCertificateNumber());
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, lawyerProfileDTO.getPracticingCourt());
        assertEquals(Division.CHATTOGRAM, lawyerProfileDTO.getDivision());
        assertEquals(District.CHATTOGRAM, lawyerProfileDTO.getDistrict());
        assertEquals("Test bio description", lawyerProfileDTO.getBio());
        assertEquals(specializations, lawyerProfileDTO.getSpecializations());
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
        
        LawyerProfileDTO dto1 = new LawyerProfileDTO(
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", specializations
        );
        
        LawyerProfileDTO dto2 = new LawyerProfileDTO(
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", specializations
        );
        
        LawyerProfileDTO dto3 = new LawyerProfileDTO(
            "Different Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", specializations
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
        
        String toString = lawyerProfileDTO.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Test Firm"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("BAR123456"));
    }
} 