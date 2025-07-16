package com.javajedis.legalconnect.lawyerdirectory.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import org.junit.jupiter.api.Test;

class FindLawyersDTOTest {
    @Test
    void testDefaultConstructor() {
        FindLawyersDTO dto = new FindLawyersDTO();
        assertNull(dto.getMinExperience());
        assertNull(dto.getMaxExperience());
        assertNull(dto.getPracticingCourt());
        assertNull(dto.getDivision());
        assertNull(dto.getDistrict());
        assertNull(dto.getSpecialization());
    }

    @Test
    void testAllArgsConstructor() {
        FindLawyersDTO dto = new FindLawyersDTO(1, 10, PracticingCourt.SUPREME_COURT, Division.DHAKA, District.DHAKA, SpecializationType.CIVIL_LAW);
        assertEquals(1, dto.getMinExperience());
        assertEquals(10, dto.getMaxExperience());
        assertEquals(PracticingCourt.SUPREME_COURT, dto.getPracticingCourt());
        assertEquals(Division.DHAKA, dto.getDivision());
        assertEquals(District.DHAKA, dto.getDistrict());
        assertEquals(SpecializationType.CIVIL_LAW, dto.getSpecialization());
    }

    @Test
    void testSettersAndGetters() {
        FindLawyersDTO dto = new FindLawyersDTO();
        dto.setMinExperience(2);
        dto.setMaxExperience(8);
        dto.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        dto.setDivision(Division.CHATTOGRAM);
        dto.setDistrict(District.CHATTOGRAM);
        dto.setSpecialization(SpecializationType.CRIMINAL_LAW);
        assertEquals(2, dto.getMinExperience());
        assertEquals(8, dto.getMaxExperience());
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, dto.getPracticingCourt());
        assertEquals(Division.CHATTOGRAM, dto.getDivision());
        assertEquals(District.CHATTOGRAM, dto.getDistrict());
        assertEquals(SpecializationType.CRIMINAL_LAW, dto.getSpecialization());
    }

    @Test
    void testEqualsAndHashCode() {
        FindLawyersDTO dto1 = new FindLawyersDTO(1, 10, PracticingCourt.SUPREME_COURT, Division.DHAKA, District.DHAKA, SpecializationType.CIVIL_LAW);
        FindLawyersDTO dto2 = new FindLawyersDTO(1, 10, PracticingCourt.SUPREME_COURT, Division.DHAKA, District.DHAKA, SpecializationType.CIVIL_LAW);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        FindLawyersDTO dto = new FindLawyersDTO();
        dto.setMinExperience(3);
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("3"));
    }
} 