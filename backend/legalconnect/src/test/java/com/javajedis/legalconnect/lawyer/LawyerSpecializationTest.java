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

import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class LawyerSpecializationTest {

    private LawyerSpecialization specialization;
    private Lawyer lawyer;
    private User user;
    private UUID testId;
    private UUID lawyerId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
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
        lawyer.setId(lawyerId);
        lawyer.setUser(user);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        
        // Setup test specialization
        specialization = new LawyerSpecialization();
        specialization.setId(testId);
        specialization.setLawyer(lawyer);
        specialization.setSpecializationType(SpecializationType.CIVIL_LAW);
    }

    @Test
    void testDefaultConstructor() {
        LawyerSpecialization defaultSpecialization = new LawyerSpecialization();
        
        assertNotNull(defaultSpecialization);
        assertNull(defaultSpecialization.getId());
        assertNull(defaultSpecialization.getLawyer());
        assertNull(defaultSpecialization.getSpecializationType());
        assertNull(defaultSpecialization.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        LawyerSpecialization constructedSpecialization = new LawyerSpecialization(
            testId, lawyer, SpecializationType.CRIMINAL_LAW, now
        );
        
        assertEquals(testId, constructedSpecialization.getId());
        assertEquals(lawyer, constructedSpecialization.getLawyer());
        assertEquals(SpecializationType.CRIMINAL_LAW, constructedSpecialization.getSpecializationType());
        assertEquals(now, constructedSpecialization.getCreatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        specialization.setId(newId);
        assertEquals(newId, specialization.getId());
    }

    @Test
    void testLawyerGetterAndSetter() {
        Lawyer newLawyer = new Lawyer();
        newLawyer.setId(UUID.randomUUID());
        newLawyer.setFirm("New Law Firm");
        
        specialization.setLawyer(newLawyer);
        assertEquals(newLawyer, specialization.getLawyer());
    }

    @Test
    void testSpecializationTypeGetterAndSetter() {
        SpecializationType newType = SpecializationType.FAMILY_LAW;
        specialization.setSpecializationType(newType);
        assertEquals(newType, specialization.getSpecializationType());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        specialization.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, specialization.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        LawyerSpecialization spec1 = new LawyerSpecialization();
        spec1.setId(testId);
        spec1.setLawyer(lawyer);
        spec1.setSpecializationType(SpecializationType.CIVIL_LAW);
        
        LawyerSpecialization spec2 = new LawyerSpecialization();
        spec2.setId(testId);
        spec2.setLawyer(lawyer);
        spec2.setSpecializationType(SpecializationType.CIVIL_LAW);
        
        LawyerSpecialization spec3 = new LawyerSpecialization();
        spec3.setId(UUID.randomUUID());
        spec3.setLawyer(lawyer);
        spec3.setSpecializationType(SpecializationType.CIVIL_LAW);
        
        // Test equals
        assertEquals(spec1, spec2);
        assertNotEquals(spec1, spec3);
        assertNotEquals(null, spec1);
        assertEquals(spec1, spec1);
        
        // Test hashCode
        assertEquals(spec1.hashCode(), spec2.hashCode());
        assertNotEquals(spec1.hashCode(), spec3.hashCode());
    }

    @Test
    void testToString() {
        String toString = specialization.toString();
        
        assertTrue(toString.contains("id=" + testId));
        assertTrue(toString.contains("specializationType=CIVIL_LAW"));
    }

    @Test
    void testAllSpecializationTypes() {
        specialization.setSpecializationType(SpecializationType.CIVIL_LAW);
        assertEquals(SpecializationType.CIVIL_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.CRIMINAL_LAW);
        assertEquals(SpecializationType.CRIMINAL_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.FAMILY_LAW);
        assertEquals(SpecializationType.FAMILY_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.CORPORATE_LAW);
        assertEquals(SpecializationType.CORPORATE_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.LABOUR_LAW);
        assertEquals(SpecializationType.LABOUR_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.INTELLECTUAL_PROPERTY_LAW);
        assertEquals(SpecializationType.INTELLECTUAL_PROPERTY_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.TAX_LAW);
        assertEquals(SpecializationType.TAX_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.ENVIRONMENTAL_LAW);
        assertEquals(SpecializationType.ENVIRONMENTAL_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.BANKING_LAW);
        assertEquals(SpecializationType.BANKING_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.PROPERTY_LAW);
        assertEquals(SpecializationType.PROPERTY_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.IMMIGRATION_LAW);
        assertEquals(SpecializationType.IMMIGRATION_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.CONSTITUTIONAL_LAW);
        assertEquals(SpecializationType.CONSTITUTIONAL_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.INTERNATIONAL_LAW);
        assertEquals(SpecializationType.INTERNATIONAL_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.ADMINISTRATIVE_LAW);
        assertEquals(SpecializationType.ADMINISTRATIVE_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.HUMAN_RIGHTS_LAW);
        assertEquals(SpecializationType.HUMAN_RIGHTS_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.CYBER_LAW);
        assertEquals(SpecializationType.CYBER_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.CONTRACT_LAW);
        assertEquals(SpecializationType.CONTRACT_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.CONSUMER_LAW);
        assertEquals(SpecializationType.CONSUMER_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.INSURANCE_LAW);
        assertEquals(SpecializationType.INSURANCE_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.MARITIME_LAW);
        assertEquals(SpecializationType.MARITIME_LAW, specialization.getSpecializationType());
        
        specialization.setSpecializationType(SpecializationType.EDUCATION_LAW);
        assertEquals(SpecializationType.EDUCATION_LAW, specialization.getSpecializationType());
    }

    @Test
    void testNullValues() {
        LawyerSpecialization nullSpecialization = new LawyerSpecialization();
        
        nullSpecialization.setId(null);
        nullSpecialization.setLawyer(null);
        nullSpecialization.setSpecializationType(null);
        nullSpecialization.setCreatedAt(null);
        
        assertNull(nullSpecialization.getId());
        assertNull(nullSpecialization.getLawyer());
        assertNull(nullSpecialization.getSpecializationType());
        assertNull(nullSpecialization.getCreatedAt());
    }

    @Test
    void testTimestampBehavior() {
        OffsetDateTime past = OffsetDateTime.now().minusDays(30);
        OffsetDateTime present = OffsetDateTime.now();
        OffsetDateTime future = OffsetDateTime.now().plusDays(30);
        
        specialization.setCreatedAt(past);
        assertEquals(past, specialization.getCreatedAt());
        
        specialization.setCreatedAt(present);
        assertEquals(present, specialization.getCreatedAt());
        
        specialization.setCreatedAt(future);
        assertEquals(future, specialization.getCreatedAt());
    }
} 