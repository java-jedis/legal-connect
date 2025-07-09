package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class LawyerTest {

    private Lawyer lawyer;
    private User user;
    private UUID testId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        // Setup test user
        user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("lawyer@example.com");
        user.setRole(Role.LAWYER);
        user.setEmailVerified(true);
        
        // Setup test lawyer
        lawyer = new Lawyer();
        lawyer.setId(testId);
        lawyer.setUser(user);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        lawyer.setDivision(Division.DHAKA);
        lawyer.setDistrict(District.DHAKA);
        lawyer.setBio("Experienced lawyer with expertise in civil law");
        lawyer.setBarCertificateFileUrl("https://example.com/certificate.pdf");
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
    }

    @Test
    void testDefaultConstructor() {
        Lawyer defaultLawyer = new Lawyer();
        
        assertNotNull(defaultLawyer);
        assertNull(defaultLawyer.getId());
        assertNull(defaultLawyer.getUser());
        assertNull(defaultLawyer.getFirm());
        assertNull(defaultLawyer.getYearsOfExperience());
        assertNull(defaultLawyer.getBarCertificateNumber());
        assertNull(defaultLawyer.getPracticingCourt());
        assertNull(defaultLawyer.getDivision());
        assertNull(defaultLawyer.getDistrict());
        assertNull(defaultLawyer.getBio());
        assertNull(defaultLawyer.getBarCertificateFileUrl());
        assertEquals(VerificationStatus.PENDING, defaultLawyer.getVerificationStatus());
        assertNull(defaultLawyer.getCreatedAt());
        assertNull(defaultLawyer.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        Lawyer constructedLawyer = new Lawyer(
            testId, user, "Test Firm", 10, "BAR789012",
            PracticingCourt.HIGH_COURT_DIVISION, Division.CHATTOGRAM, District.CHATTOGRAM,
            "Test bio", "https://test.com/cert.pdf", VerificationStatus.APPROVED,
            now, now
        );
        
        assertEquals(testId, constructedLawyer.getId());
        assertEquals(user, constructedLawyer.getUser());
        assertEquals("Test Firm", constructedLawyer.getFirm());
        assertEquals(10, constructedLawyer.getYearsOfExperience());
        assertEquals("BAR789012", constructedLawyer.getBarCertificateNumber());
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, constructedLawyer.getPracticingCourt());
        assertEquals(Division.CHATTOGRAM, constructedLawyer.getDivision());
        assertEquals(District.CHATTOGRAM, constructedLawyer.getDistrict());
        assertEquals("Test bio", constructedLawyer.getBio());
        assertEquals("https://test.com/cert.pdf", constructedLawyer.getBarCertificateFileUrl());
        assertEquals(VerificationStatus.APPROVED, constructedLawyer.getVerificationStatus());
        assertEquals(now, constructedLawyer.getCreatedAt());
        assertEquals(now, constructedLawyer.getUpdatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        lawyer.setId(newId);
        assertEquals(newId, lawyer.getId());
    }

    @Test
    void testUserGetterAndSetter() {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail("newlawyer@example.com");
        newUser.setRole(Role.LAWYER);
        
        lawyer.setUser(newUser);
        assertEquals(newUser, lawyer.getUser());
    }

    @Test
    void testFirmGetterAndSetter() {
        String newFirm = "New Law Firm";
        lawyer.setFirm(newFirm);
        assertEquals(newFirm, lawyer.getFirm());
    }

    @Test
    void testYearsOfExperienceGetterAndSetter() {
        Integer newYears = 15;
        lawyer.setYearsOfExperience(newYears);
        assertEquals(newYears, lawyer.getYearsOfExperience());
    }

    @Test
    void testBarCertificateNumberGetterAndSetter() {
        String newCertNumber = "BAR654321";
        lawyer.setBarCertificateNumber(newCertNumber);
        assertEquals(newCertNumber, lawyer.getBarCertificateNumber());
    }

    @Test
    void testPracticingCourtGetterAndSetter() {
        PracticingCourt newCourt = PracticingCourt.DISTRICT_COURT;
        lawyer.setPracticingCourt(newCourt);
        assertEquals(newCourt, lawyer.getPracticingCourt());
    }

    @Test
    void testDivisionGetterAndSetter() {
        Division newDivision = Division.RAJSHAHI;
        lawyer.setDivision(newDivision);
        assertEquals(newDivision, lawyer.getDivision());
    }

    @Test
    void testDistrictGetterAndSetter() {
        District newDistrict = District.RAJSHAHI;
        lawyer.setDistrict(newDistrict);
        assertEquals(newDistrict, lawyer.getDistrict());
    }

    @Test
    void testBioGetterAndSetter() {
        String newBio = "Updated bio with more details";
        lawyer.setBio(newBio);
        assertEquals(newBio, lawyer.getBio());
    }

    @Test
    void testBarCertificateFileUrlGetterAndSetter() {
        String newUrl = "https://new-example.com/certificate.pdf";
        lawyer.setBarCertificateFileUrl(newUrl);
        assertEquals(newUrl, lawyer.getBarCertificateFileUrl());
    }

    @Test
    void testVerificationStatusGetterAndSetter() {
        VerificationStatus newStatus = VerificationStatus.APPROVED;
        lawyer.setVerificationStatus(newStatus);
        assertEquals(newStatus, lawyer.getVerificationStatus());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        lawyer.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, lawyer.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        lawyer.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, lawyer.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Lawyer lawyer1 = new Lawyer();
        lawyer1.setId(testId);
        lawyer1.setUser(user);
        lawyer1.setFirm("Test Law Firm");
        
        Lawyer lawyer2 = new Lawyer();
        lawyer2.setId(testId);
        lawyer2.setUser(user);
        lawyer2.setFirm("Test Law Firm");
        
        Lawyer lawyer3 = new Lawyer();
        lawyer3.setId(UUID.randomUUID());
        lawyer3.setUser(user);
        lawyer3.setFirm("Test Law Firm");
        
        // Test equals
        assertEquals(lawyer1, lawyer2);
        assertNotEquals(lawyer1, lawyer3);
        assertNotEquals(null,lawyer1);
        assertEquals(lawyer1, lawyer1);
        
        // Test hashCode
        assertEquals(lawyer1.hashCode(), lawyer2.hashCode());
        assertNotEquals(lawyer1.hashCode(), lawyer3.hashCode());
    }

    @Test
    void testToString() {
        String toString = lawyer.toString();
        
        assertTrue(toString.contains("id=" + testId));
        assertTrue(toString.contains("firm=Test Law Firm"));
        assertTrue(toString.contains("yearsOfExperience=5"));
        assertTrue(toString.contains("barCertificateNumber=BAR123456"));
        assertTrue(toString.contains("practicingCourt=SUPREME_COURT"));
        assertTrue(toString.contains("division=DHAKA"));
        assertTrue(toString.contains("district=DHAKA"));
        assertTrue(toString.contains("verificationStatus=PENDING"));
    }

    @Test
    void testDefaultVerificationStatus() {
        Lawyer newLawyer = new Lawyer();
        assertEquals(VerificationStatus.PENDING, newLawyer.getVerificationStatus());
    }

    @Test
    void testAllVerificationStatuses() {
        lawyer.setVerificationStatus(VerificationStatus.PENDING);
        assertEquals(VerificationStatus.PENDING, lawyer.getVerificationStatus());
        
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);
        assertEquals(VerificationStatus.APPROVED, lawyer.getVerificationStatus());
        
        lawyer.setVerificationStatus(VerificationStatus.REJECTED);
        assertEquals(VerificationStatus.REJECTED, lawyer.getVerificationStatus());
    }

    @Test
    void testAllPracticingCourts() {
        lawyer.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        assertEquals(PracticingCourt.SUPREME_COURT, lawyer.getPracticingCourt());
        
        lawyer.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        assertEquals(PracticingCourt.HIGH_COURT_DIVISION, lawyer.getPracticingCourt());
        
        lawyer.setPracticingCourt(PracticingCourt.DISTRICT_COURT);
        assertEquals(PracticingCourt.DISTRICT_COURT, lawyer.getPracticingCourt());
    }

    @Test
    void testAllDivisions() {
        lawyer.setDivision(Division.DHAKA);
        assertEquals(Division.DHAKA, lawyer.getDivision());
        
        lawyer.setDivision(Division.CHATTOGRAM);
        assertEquals(Division.CHATTOGRAM, lawyer.getDivision());
        
        lawyer.setDivision(Division.RAJSHAHI);
        assertEquals(Division.RAJSHAHI, lawyer.getDivision());
        
        lawyer.setDivision(Division.KHULNA);
        assertEquals(Division.KHULNA, lawyer.getDivision());
        
        lawyer.setDivision(Division.BARISHAL);
        assertEquals(Division.BARISHAL, lawyer.getDivision());
        
        lawyer.setDivision(Division.SYLHET);
        assertEquals(Division.SYLHET, lawyer.getDivision());
        
        lawyer.setDivision(Division.RANGPUR);
        assertEquals(Division.RANGPUR, lawyer.getDivision());
        
        lawyer.setDivision(Division.MYMENSINGH);
        assertEquals(Division.MYMENSINGH, lawyer.getDivision());
    }

    @Test
    void testAllDistricts() {
        lawyer.setDistrict(District.DHAKA);
        assertEquals(District.DHAKA, lawyer.getDistrict());
        
        lawyer.setDistrict(District.CHATTOGRAM);
        assertEquals(District.CHATTOGRAM, lawyer.getDistrict());
        
        lawyer.setDistrict(District.RAJSHAHI);
        assertEquals(District.RAJSHAHI, lawyer.getDistrict());
        
        lawyer.setDistrict(District.KHULNA);
        assertEquals(District.KHULNA, lawyer.getDistrict());
        
        lawyer.setDistrict(District.BARISHAL);
        assertEquals(District.BARISHAL, lawyer.getDistrict());
        
        lawyer.setDistrict(District.SYLHET);
        assertEquals(District.SYLHET, lawyer.getDistrict());
        
        lawyer.setDistrict(District.RANGPUR);
        assertEquals(District.RANGPUR, lawyer.getDistrict());
        
        lawyer.setDistrict(District.MYMENSINGH);
        assertEquals(District.MYMENSINGH, lawyer.getDistrict());
    }
} 