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
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CRIMINAL_LAW, SpecializationType.CIVIL_LAW);
        UUID id = UUID.randomUUID();
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
            specializations
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
    }

    @Test
    void testSettersAndGetters() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SpecializationType> specializations = Arrays.asList(SpecializationType.CORPORATE_LAW);
        UUID id = UUID.randomUUID();
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
            now, now, specializations
        );
        LawyerInfoDTO dto2 = new LawyerInfoDTO(
            id, "John", "Doe", "john.doe@example.com",
            "Test Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations
        );
        LawyerInfoDTO dto3 = new LawyerInfoDTO(
            UUID.randomUUID(), "Jane", "Smith", "jane.smith@example.com",
            "Different Firm", 5, "BAR123456", PracticingCourt.SUPREME_COURT,
            Division.DHAKA, District.DHAKA, "Test bio", VerificationStatus.PENDING,
            now, now, specializations
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
}