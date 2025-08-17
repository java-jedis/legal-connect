package com.javajedis.legalconnect.lawyer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;

class LawyerInfoDTOTest {

    private LawyerInfoDTO lawyerInfoDTO;

    @BeforeEach
    void setUp() {
        lawyerInfoDTO = new LawyerInfoDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(lawyerInfoDTO);
        assertNull(lawyerInfoDTO.getId());
        assertNull(lawyerInfoDTO.getFirstName());
        assertNull(lawyerInfoDTO.getLastName());
        assertNull(lawyerInfoDTO.getEmail());
        assertNull(lawyerInfoDTO.getFirm());
        assertNull(lawyerInfoDTO.getYearsOfExperience());
        assertNull(lawyerInfoDTO.getBarCertificateNumber());
        assertNull(lawyerInfoDTO.getPracticingCourt());
        assertNull(lawyerInfoDTO.getDivision());
        assertNull(lawyerInfoDTO.getDistrict());
        assertNull(lawyerInfoDTO.getBio());
        assertNull(lawyerInfoDTO.getVerificationStatus());
        assertNull(lawyerInfoDTO.getLawyerCreatedAt());
        assertNull(lawyerInfoDTO.getLawyerUpdatedAt());
        assertNull(lawyerInfoDTO.getSpecializations());
        assertNull(lawyerInfoDTO.getHourlyCharge());
        assertNull(lawyerInfoDTO.getCompleteProfile());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW, SpecializationType.CIVIL_LAW);
        UUID id = UUID.randomUUID();
        java.math.BigDecimal hourlyCharge = new java.math.BigDecimal("150.00");
        LawyerInfoDTO dto = new LawyerInfoDTO(
            id,
            "John",
            "Doe",
            "john.doe@example.com",
            "Test Firm",
            5,
            "BAR123456",
            PracticingCourt.SUPREME_COURT,
            Division.DHAKA,
            District.DHAKA,
            "Test bio",
            VerificationStatus.PENDING,
            now,
            now,
            specializations,
            hourlyCharge,
            true
        );
        assertEquals(id, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("Test Firm", dto.getFirm());
        assertEquals(5, dto.getYearsOfExperience());
        assertEquals("BAR123456", dto.getBarCertificateNumber());
        assertEquals(PracticingCourt.SUPREME_COURT, dto.getPracticingCourt());
        assertEquals(Division.DHAKA, dto.getDivision());
        assertEquals(District.DHAKA, dto.getDistrict());
        assertEquals("Test bio", dto.getBio());
        assertEquals(VerificationStatus.PENDING, dto.getVerificationStatus());
        assertEquals(now, dto.getLawyerCreatedAt());
        assertEquals(now, dto.getLawyerUpdatedAt());
        assertEquals(specializations, dto.getSpecializations());
        assertEquals(hourlyCharge, dto.getHourlyCharge());
        assertEquals(true, dto.getCompleteProfile());
    }

    @Test
    void testSettersAndGetters() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CORPORATE_LAW);
        UUID id = UUID.randomUUID();
        java.math.BigDecimal hourlyCharge = new java.math.BigDecimal("200.75");
        
        lawyerInfoDTO.setId(id);
        lawyerInfoDTO.setFirstName("Jane");
        lawyerInfoDTO.setLastName("Smith");
        lawyerInfoDTO.setEmail("jane.smith@example.com");
        lawyerInfoDTO.setFirm("Test Firm");
        lawyerInfoDTO.setYearsOfExperience(10);
        lawyerInfoDTO.setBarCertificateNumber("BAR789012");
        lawyerInfoDTO.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        lawyerInfoDTO.setDivision(Division.CHATTOGRAM);
        lawyerInfoDTO.setDistrict(District.CHATTOGRAM);
        lawyerInfoDTO.setBio("Test bio description");
        lawyerInfoDTO.setVerificationStatus(VerificationStatus.APPROVED);
        lawyerInfoDTO.setLawyerCreatedAt(now);
        lawyerInfoDTO.setLawyerUpdatedAt(now);
        lawyerInfoDTO.setSpecializations(specializations);
        lawyerInfoDTO.setHourlyCharge(hourlyCharge);
        lawyerInfoDTO.setCompleteProfile(false);

        assertEquals(id, lawyerInfoDTO.getId());
        assertEquals("Jane", lawyerInfoDTO.getFirstName());
        assertEquals("Smith", lawyerInfoDTO.getLastName());
        assertEquals("jane.smith@example.com", lawyerInfoDTO.getEmail());
        assertEquals("Test Firm", lawyerInfoDTO.getFirm());
        assertEquals(10, lawyerInfoDTO.getYearsOfExperience());
        assertEquals("BAR789012", lawyerInfoDTO.getBarCertificateNumber());
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, lawyerInfoDTO.getPracticingCourt());
        assertEquals(Division.CHATTOGRAM, lawyerInfoDTO.getDivision());
        assertEquals(District.CHATTOGRAM, lawyerInfoDTO.getDistrict());
        assertEquals("Test bio description", lawyerInfoDTO.getBio());
        assertEquals(VerificationStatus.APPROVED, lawyerInfoDTO.getVerificationStatus());
        assertEquals(now, lawyerInfoDTO.getLawyerCreatedAt());
        assertEquals(now, lawyerInfoDTO.getLawyerUpdatedAt());
        assertEquals(specializations, lawyerInfoDTO.getSpecializations());
        assertEquals(hourlyCharge, lawyerInfoDTO.getHourlyCharge());
        assertEquals(false, lawyerInfoDTO.getCompleteProfile());
    }

    @Test
    void testGetPracticingCourtDisplayName_WithValue() {
        lawyerInfoDTO.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        assertEquals("Supreme Court", lawyerInfoDTO.getPracticingCourtDisplayName());
    }

    @Test
    void testGetPracticingCourtDisplayName_Null() {
        lawyerInfoDTO.setPracticingCourt(null);
        assertNull(lawyerInfoDTO.getPracticingCourtDisplayName());
    }

    @Test
    void testGetDivisionDisplayName_WithValue() {
        lawyerInfoDTO.setDivision(Division.DHAKA);
        assertEquals("Dhaka", lawyerInfoDTO.getDivisionDisplayName());
    }

    @Test
    void testGetDivisionDisplayName_Null() {
        lawyerInfoDTO.setDivision(null);
        assertNull(lawyerInfoDTO.getDivisionDisplayName());
    }

    @Test
    void testGetDistrictDisplayName_WithValue() {
        lawyerInfoDTO.setDistrict(District.DHAKA);
        assertEquals("Dhaka", lawyerInfoDTO.getDistrictDisplayName());
    }

    @Test
    void testGetDistrictDisplayName_Null() {
        lawyerInfoDTO.setDistrict(null);
        assertNull(lawyerInfoDTO.getDistrictDisplayName());
    }

    @Test
    void testGetVerificationStatusDisplayName_WithValue() {
        lawyerInfoDTO.setVerificationStatus(VerificationStatus.PENDING);
        assertEquals("Pending", lawyerInfoDTO.getVerificationStatusDisplayName());
    }

    @Test
    void testGetVerificationStatusDisplayName_Null() {
        lawyerInfoDTO.setVerificationStatus(null);
        assertNull(lawyerInfoDTO.getVerificationStatusDisplayName());
    }

    @Test
    void testGetSpecializationDisplayNames_WithSpecializations() {
        List<SpecializationType> specializations = Arrays.asList(
            SpecializationType.CRIMINAL_LAW,
            SpecializationType.CIVIL_LAW,
            SpecializationType.FAMILY_LAW
        );
        lawyerInfoDTO.setSpecializations(specializations);
        
        List<String> displayNames = lawyerInfoDTO.getSpecializationDisplayNames();
        
        assertNotNull(displayNames);
        assertEquals(3, displayNames.size());
        assertTrue(displayNames.contains("Criminal Law"));
        assertTrue(displayNames.contains("Civil Law"));
        assertTrue(displayNames.contains("Family Law"));
    }

    @Test
    void testGetSpecializationDisplayNames_NullSpecializations() {
        lawyerInfoDTO.setSpecializations(null);
        
        List<String> displayNames = lawyerInfoDTO.getSpecializationDisplayNames();
        
        assertNotNull(displayNames);
        assertTrue(displayNames.isEmpty());
    }

    @Test
    void testGetSpecializationDisplayNames_EmptySpecializations() {
        lawyerInfoDTO.setSpecializations(Arrays.asList());
        
        List<String> displayNames = lawyerInfoDTO.getSpecializationDisplayNames();
        
        assertNotNull(displayNames);
        assertTrue(displayNames.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW);
        UUID id = UUID.randomUUID();
        LawyerInfoDTO dto1 = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, null, null
        );
        LawyerInfoDTO dto2 = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, null, null
        );
        LawyerInfoDTO dto3 = new LawyerInfoDTO(
            UUID.randomUUID(), "Jane", "Smith", "jane.smith@example.com",
            "Different Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, null, null
        );
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        lawyerInfoDTO.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        lawyerInfoDTO.setFirstName("John");
        lawyerInfoDTO.setLastName("Doe");
        lawyerInfoDTO.setEmail("john.doe@example.com");
        lawyerInfoDTO.setFirm("Test Firm");
        lawyerInfoDTO.setYearsOfExperience(5);
        lawyerInfoDTO.setBarCertificateNumber("BAR123456");
        String toString = lawyerInfoDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("123e4567-e89b-12d3-a456-426614174000"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("john.doe@example.com"));
        assertTrue(toString.contains("Test Firm"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("BAR123456"));
    }

    @Test
    void testGetHourlyChargeDisplayText_WithValue() {
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("150.50"));
        assertEquals("150.50", lawyerInfoDTO.getHourlyChargeDisplayText());
    }

    @Test
    void testGetHourlyChargeDisplayText_Null() {
        lawyerInfoDTO.setHourlyCharge(null);
        assertNull(lawyerInfoDTO.getHourlyChargeDisplayText());
    }

    @Test
    void testGetCompleteProfileDisplayText_True() {
        lawyerInfoDTO.setCompleteProfile(true);
        assertEquals("Complete", lawyerInfoDTO.getCompleteProfileDisplayText());
    }

    @Test
    void testGetCompleteProfileDisplayText_False() {
        lawyerInfoDTO.setCompleteProfile(false);
        assertEquals("Incomplete", lawyerInfoDTO.getCompleteProfileDisplayText());
    }

    @Test
    void testGetCompleteProfileDisplayText_Null() {
        lawyerInfoDTO.setCompleteProfile(null);
        assertEquals("Unknown", lawyerInfoDTO.getCompleteProfileDisplayText());
    }

    @Test
    void testHourlyChargeSetterAndGetter() {
        // Test setting and getting hourly charge
        java.math.BigDecimal hourlyCharge = new java.math.BigDecimal("125.75");
        lawyerInfoDTO.setHourlyCharge(hourlyCharge);
        assertEquals(hourlyCharge, lawyerInfoDTO.getHourlyCharge());
    }

    @Test
    void testHourlyChargeNullHandling() {
        // Test null handling for hourly charge
        lawyerInfoDTO.setHourlyCharge(null);
        assertNull(lawyerInfoDTO.getHourlyCharge());
        assertNull(lawyerInfoDTO.getHourlyChargeDisplayText());
    }

    @Test
    void testHourlyChargeDisplayFormatting() {
        // Test various decimal formats
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("100.00"));
        assertEquals("100.00", lawyerInfoDTO.getHourlyChargeDisplayText());
        
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("99.99"));
        assertEquals("99.99", lawyerInfoDTO.getHourlyChargeDisplayText());
        
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("1000"));
        assertEquals("1000", lawyerInfoDTO.getHourlyChargeDisplayText());
        
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("0.01"));
        assertEquals("0.01", lawyerInfoDTO.getHourlyChargeDisplayText());
    }

    @Test
    void testCompleteProfileSetterAndGetter() {
        // Test setting and getting complete profile status
        lawyerInfoDTO.setCompleteProfile(true);
        assertEquals(true, lawyerInfoDTO.getCompleteProfile());
        
        lawyerInfoDTO.setCompleteProfile(false);
        assertEquals(false, lawyerInfoDTO.getCompleteProfile());
    }

    @Test
    void testCompleteProfileNullHandling() {
        // Test null handling for complete profile
        lawyerInfoDTO.setCompleteProfile(null);
        assertNull(lawyerInfoDTO.getCompleteProfile());
        assertEquals("Unknown", lawyerInfoDTO.getCompleteProfileDisplayText());
    }

    @Test
    void testCompleteProfileDisplayFormatting() {
        // Test display text formatting for different boolean values
        lawyerInfoDTO.setCompleteProfile(Boolean.TRUE);
        assertEquals("Complete", lawyerInfoDTO.getCompleteProfileDisplayText());
        
        lawyerInfoDTO.setCompleteProfile(Boolean.FALSE);
        assertEquals("Incomplete", lawyerInfoDTO.getCompleteProfileDisplayText());
    }

    @Test
    void testNewFieldsInToString() {
        // Test that new fields are included in toString output
        java.math.BigDecimal hourlyCharge = new java.math.BigDecimal("175.50");
        lawyerInfoDTO.setHourlyCharge(hourlyCharge);
        lawyerInfoDTO.setCompleteProfile(true);
        
        String toString = lawyerInfoDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("175.50"));
        assertTrue(toString.contains("true"));
    }

    @Test
    void testEqualsAndHashCodeWithNewFields() {
        // Test equals and hashCode with new fields
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW);
        UUID id = UUID.randomUUID();
        java.math.BigDecimal hourlyCharge = new java.math.BigDecimal("150.00");
        
        LawyerInfoDTO dto1 = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, hourlyCharge, true
        );
        LawyerInfoDTO dto2 = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, hourlyCharge, true
        );
        LawyerInfoDTO dto3 = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, new java.math.BigDecimal("200.00"), false
        );
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testHourlyChargeWithDifferentPrecision() {
        // Test hourly charge with different decimal precision
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("100.1"));
        assertEquals("100.1", lawyerInfoDTO.getHourlyChargeDisplayText());
        
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("100.123"));
        assertEquals("100.123", lawyerInfoDTO.getHourlyChargeDisplayText());
        
        lawyerInfoDTO.setHourlyCharge(new java.math.BigDecimal("100"));
        assertEquals("100", lawyerInfoDTO.getHourlyChargeDisplayText());
    }

    @Test
    void testAllArgsConstructorWithNullNewFields() {
        // Test all args constructor with null values for new fields
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW);
        UUID id = UUID.randomUUID();
        
        LawyerInfoDTO dto = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations, null, null
        );
        
        assertNull(dto.getHourlyCharge());
        assertNull(dto.getCompleteProfile());
        assertNull(dto.getHourlyChargeDisplayText());
        assertEquals("Unknown", dto.getCompleteProfileDisplayText());
    }
}