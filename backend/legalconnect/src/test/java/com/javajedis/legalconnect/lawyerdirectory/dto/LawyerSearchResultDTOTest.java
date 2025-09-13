package com.javajedis.legalconnect.lawyerdirectory.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.user.ProfilePictureDTO;

class LawyerSearchResultDTOTest {
    @Test
    void testBuilderAndGetters() {
        UUID lawyerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<SpecializationType> specs = Arrays.asList(SpecializationType.CIVIL_LAW, SpecializationType.CRIMINAL_LAW);
        ProfilePictureDTO profilePicture = new ProfilePictureDTO("https://example.com/full.jpg", "https://example.com/thumb.jpg", "public-id");
        
        LawyerSearchResultDTO dto = LawyerSearchResultDTO.builder()
                .lawyerId(lawyerId)
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .firm("Firm")
                .yearsOfExperience(10)
                .practicingCourt(PracticingCourt.SUPREME_COURT)
                .division(Division.DHAKA)
                .district(District.DHAKA)
                .bio("Bio")
                .specializations(specs)
                .averageRating(4.5)
                .profilePicture(profilePicture)
                .build();
        assertEquals(lawyerId, dto.getLawyerId());
        assertEquals(userId, dto.getUserId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("Firm", dto.getFirm());
        assertEquals(10, dto.getYearsOfExperience());
        assertEquals(PracticingCourt.SUPREME_COURT, dto.getPracticingCourt());
        assertEquals(Division.DHAKA, dto.getDivision());
        assertEquals(District.DHAKA, dto.getDistrict());
        assertEquals("Bio", dto.getBio());
        assertEquals(specs, dto.getSpecializations());
        assertEquals(4.5, dto.getAverageRating());
        assertEquals(profilePicture, dto.getProfilePicture());
        // Display name assertions
        assertEquals(PracticingCourt.SUPREME_COURT.getDisplayName(), dto.getPracticingCourtDisplayName());
        assertEquals(Division.DHAKA.getDisplayName(), dto.getDivisionDisplayName());
        assertEquals(District.DHAKA.getDisplayName(), dto.getDistrictDisplayName());
        assertEquals(Arrays.asList(SpecializationType.CIVIL_LAW.getDisplayName(), SpecializationType.CRIMINAL_LAW.getDisplayName()), dto.getSpecializationDisplayNames());
    }

    @Test
    void testSettersAndGetters() {
        LawyerSearchResultDTO dto = new LawyerSearchResultDTO();
        ProfilePictureDTO profilePicture = new ProfilePictureDTO("https://example.com/full2.jpg", "https://example.com/thumb2.jpg", "public-id-2");
        
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane@example.com");
        dto.setFirm("Firm2");
        dto.setYearsOfExperience(5);
        dto.setBio("Bio2");
        dto.setProfilePicture(profilePicture);
        
        assertEquals("Jane", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("jane@example.com", dto.getEmail());
        assertEquals("Firm2", dto.getFirm());
        assertEquals(5, dto.getYearsOfExperience());
        assertEquals("Bio2", dto.getBio());
        assertEquals(profilePicture, dto.getProfilePicture());
    }

    @Test
    void testParseSpecializations() {
        String csv = "CIVIL_LAW,CRIMINAL_LAW";
        List<SpecializationType> list = LawyerSearchResultDTO.parseSpecializations(csv);
        assertEquals(2, list.size());
        assertTrue(list.contains(SpecializationType.CIVIL_LAW));
        assertTrue(list.contains(SpecializationType.CRIMINAL_LAW));
        assertTrue(LawyerSearchResultDTO.parseSpecializations("").isEmpty());
        assertTrue(LawyerSearchResultDTO.parseSpecializations(null).isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID lawyerId = UUID.randomUUID();
        LawyerSearchResultDTO dto1 = LawyerSearchResultDTO.builder().lawyerId(lawyerId).build();
        LawyerSearchResultDTO dto2 = LawyerSearchResultDTO.builder().lawyerId(lawyerId).build();
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LawyerSearchResultDTO dto = new LawyerSearchResultDTO();
        dto.setFirstName("Test");
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test"));
    }
}