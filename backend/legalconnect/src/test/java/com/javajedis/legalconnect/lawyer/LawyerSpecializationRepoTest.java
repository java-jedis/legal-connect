package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class LawyerSpecializationRepoTest {

    @Mock
    private LawyerSpecializationRepo lawyerSpecializationRepo;

    private User testUser1;
    private User testUser2;
    private Lawyer testLawyer1;
    private Lawyer testLawyer2;
    private LawyerSpecialization testSpec1;
    private LawyerSpecialization testSpec2;
    private LawyerSpecialization testSpec3;
    private LawyerSpecialization testSpec4;
    private UUID userId1;
    private UUID userId2;
    private UUID lawyerId1;
    private UUID lawyerId2;
    private UUID specId1;
    private UUID specId2;
    private UUID specId3;
    private UUID specId4;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        lawyerId1 = UUID.randomUUID();
        lawyerId2 = UUID.randomUUID();
        specId1 = UUID.randomUUID();
        specId2 = UUID.randomUUID();
        specId3 = UUID.randomUUID();
        specId4 = UUID.randomUUID();
        
        // Setup test user 1
        testUser1 = new User();
        testUser1.setId(userId1);
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setEmail("lawyer1@example.com");
        testUser1.setRole(Role.LAWYER);
        testUser1.setEmailVerified(true);
        testUser1.setPassword("password123");
        
        // Setup test user 2
        testUser2 = new User();
        testUser2.setId(userId2);
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("lawyer2@example.com");
        testUser2.setRole(Role.LAWYER);
        testUser2.setEmailVerified(true);
        testUser2.setPassword("password456");
        
        // Setup test lawyer 1
        testLawyer1 = new Lawyer();
        testLawyer1.setId(lawyerId1);
        testLawyer1.setUser(testUser1);
        testLawyer1.setFirm("Test Law Firm 1");
        testLawyer1.setYearsOfExperience(5);
        testLawyer1.setBarCertificateNumber("BAR123456");
        testLawyer1.setPracticingCourt(PracticingCourt.SUPREME_COURT);
        testLawyer1.setDivision(Division.DHAKA);
        testLawyer1.setDistrict(District.DHAKA);
        testLawyer1.setVerificationStatus(VerificationStatus.PENDING);
        
        // Setup test lawyer 2
        testLawyer2 = new Lawyer();
        testLawyer2.setId(lawyerId2);
        testLawyer2.setUser(testUser2);
        testLawyer2.setFirm("Test Law Firm 2");
        testLawyer2.setYearsOfExperience(10);
        testLawyer2.setBarCertificateNumber("BAR789012");
        testLawyer2.setPracticingCourt(PracticingCourt.HIGH_COURT_DIVISION);
        testLawyer2.setDivision(Division.CHATTOGRAM);
        testLawyer2.setDistrict(District.CHATTOGRAM);
        testLawyer2.setVerificationStatus(VerificationStatus.APPROVED);
        
        // Setup test specialization 1 (Civil Law for lawyer 1)
        testSpec1 = new LawyerSpecialization();
        testSpec1.setId(specId1);
        testSpec1.setLawyer(testLawyer1);
        testSpec1.setSpecializationType(SpecializationType.CIVIL_LAW);
        testSpec1.setCreatedAt(OffsetDateTime.now());
        
        // Setup test specialization 2 (Criminal Law for lawyer 1)
        testSpec2 = new LawyerSpecialization();
        testSpec2.setId(specId2);
        testSpec2.setLawyer(testLawyer1);
        testSpec2.setSpecializationType(SpecializationType.CRIMINAL_LAW);
        testSpec2.setCreatedAt(OffsetDateTime.now());
        
        // Setup test specialization 3 (Family Law for lawyer 2)
        testSpec3 = new LawyerSpecialization();
        testSpec3.setId(specId3);
        testSpec3.setLawyer(testLawyer2);
        testSpec3.setSpecializationType(SpecializationType.FAMILY_LAW);
        testSpec3.setCreatedAt(OffsetDateTime.now());
        
        // Setup test specialization 4 (Corporate Law for lawyer 2)
        testSpec4 = new LawyerSpecialization();
        testSpec4.setId(specId4);
        testSpec4.setLawyer(testLawyer2);
        testSpec4.setSpecializationType(SpecializationType.CORPORATE_LAW);
        testSpec4.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void testSaveSpecialization() {
        when(lawyerSpecializationRepo.save(testSpec1)).thenReturn(testSpec1);
        
        LawyerSpecialization savedSpec = lawyerSpecializationRepo.save(testSpec1);
        
        assertNotNull(savedSpec);
        assertEquals(testSpec1.getSpecializationType(), savedSpec.getSpecializationType());
        assertEquals(testSpec1.getLawyer().getId(), savedSpec.getLawyer().getId());
    }

    @Test
    void testFindById() {
        when(lawyerSpecializationRepo.findById(specId1)).thenReturn(Optional.of(testSpec1));
        
        Optional<LawyerSpecialization> foundSpec = lawyerSpecializationRepo.findById(specId1);
        
        assertTrue(foundSpec.isPresent());
        assertEquals(testSpec1.getSpecializationType(), foundSpec.get().getSpecializationType());
        assertEquals(testSpec1.getLawyer().getId(), foundSpec.get().getLawyer().getId());
    }

    @Test
    void testFindByIdNotFound() {
        when(lawyerSpecializationRepo.findById(UUID.randomUUID())).thenReturn(Optional.empty());
        
        Optional<LawyerSpecialization> foundSpec = lawyerSpecializationRepo.findById(UUID.randomUUID());
        assertFalse(foundSpec.isPresent());
    }

    @Test
    void testFindByLawyerUser() {
        when(lawyerSpecializationRepo.findByLawyerUser(testUser1)).thenReturn(List.of(testSpec1, testSpec2));
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findByLawyerUser(testUser1);
        
        assertEquals(2, foundSpecs.size());
        assertTrue(foundSpecs.stream().allMatch(spec -> spec.getLawyer().getUser().getId().equals(userId1)));
        assertTrue(foundSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CIVIL_LAW)));
        assertTrue(foundSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CRIMINAL_LAW)));
    }

    @Test
    void testFindByLawyerUserEmpty() {
        when(lawyerSpecializationRepo.findByLawyerUser(testUser1)).thenReturn(List.of());
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findByLawyerUser(testUser1);
        assertTrue(foundSpecs.isEmpty());
    }

    @Test
    void testFindByLawyer() {
        when(lawyerSpecializationRepo.findByLawyer(testLawyer1)).thenReturn(List.of(testSpec1, testSpec2));
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findByLawyer(testLawyer1);
        
        assertEquals(2, foundSpecs.size());
        assertTrue(foundSpecs.stream().allMatch(spec -> spec.getLawyer().getId().equals(lawyerId1)));
        assertTrue(foundSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CIVIL_LAW)));
        assertTrue(foundSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CRIMINAL_LAW)));
    }

    @Test
    void testFindByLawyerAndSpecializationType() {
        when(lawyerSpecializationRepo.findByLawyerAndSpecializationType(testLawyer1, SpecializationType.CIVIL_LAW))
            .thenReturn(List.of(testSpec1));
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findByLawyerAndSpecializationType(
            testLawyer1, SpecializationType.CIVIL_LAW);
        
        assertEquals(1, foundSpecs.size());
        assertEquals(SpecializationType.CIVIL_LAW, foundSpecs.get(0).getSpecializationType());
        assertEquals(lawyerId1, foundSpecs.get(0).getLawyer().getId());
    }

    @Test
    void testFindByLawyerAndSpecializationTypeNotFound() {
        when(lawyerSpecializationRepo.findByLawyerAndSpecializationType(testLawyer1, SpecializationType.FAMILY_LAW))
            .thenReturn(List.of());
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findByLawyerAndSpecializationType(
            testLawyer1, SpecializationType.FAMILY_LAW);
        
        assertTrue(foundSpecs.isEmpty());
    }

    @Test
    void testExistsByLawyerAndSpecializationType() {
        when(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer1, SpecializationType.CIVIL_LAW)).thenReturn(true);
        when(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer1, SpecializationType.CRIMINAL_LAW)).thenReturn(true);
        when(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer1, SpecializationType.FAMILY_LAW)).thenReturn(false);
        when(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer2, SpecializationType.FAMILY_LAW)).thenReturn(true);
        
        assertTrue(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer1, SpecializationType.CIVIL_LAW));
        assertTrue(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer1, SpecializationType.CRIMINAL_LAW));
        assertFalse(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer1, SpecializationType.FAMILY_LAW));
        assertTrue(lawyerSpecializationRepo.existsByLawyerAndSpecializationType(testLawyer2, SpecializationType.FAMILY_LAW));
    }

    @Test
    void testFindBySpecializationType() {
        when(lawyerSpecializationRepo.findBySpecializationType(SpecializationType.CIVIL_LAW)).thenReturn(List.of(testSpec1));
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findBySpecializationType(SpecializationType.CIVIL_LAW);
        
        assertEquals(1, foundSpecs.size());
        assertEquals(SpecializationType.CIVIL_LAW, foundSpecs.get(0).getSpecializationType());
        assertEquals(lawyerId1, foundSpecs.get(0).getLawyer().getId());
    }

    @Test
    void testFindBySpecializationTypeMultiple() {
        // Create another lawyer with Civil Law specialization
        User testUser3 = new User();
        testUser3.setId(UUID.randomUUID());
        testUser3.setFirstName("Bob");
        testUser3.setLastName("Johnson");
        testUser3.setEmail("lawyer3@example.com");
        testUser3.setRole(Role.LAWYER);
        testUser3.setEmailVerified(true);
        testUser3.setPassword("password789");
        
        Lawyer testLawyer3 = new Lawyer();
        testLawyer3.setId(UUID.randomUUID());
        testLawyer3.setUser(testUser3);
        testLawyer3.setFirm("Test Law Firm 3");
        testLawyer3.setYearsOfExperience(7);
        testLawyer3.setBarCertificateNumber("BAR345678");
        testLawyer3.setPracticingCourt(PracticingCourt.DISTRICT_COURT);
        testLawyer3.setDivision(Division.RAJSHAHI);
        testLawyer3.setDistrict(District.RAJSHAHI);
        testLawyer3.setVerificationStatus(VerificationStatus.PENDING);
        
        LawyerSpecialization testSpec5 = new LawyerSpecialization();
        testSpec5.setId(UUID.randomUUID());
        testSpec5.setLawyer(testLawyer3);
        testSpec5.setSpecializationType(SpecializationType.CIVIL_LAW);
        testSpec5.setCreatedAt(OffsetDateTime.now());
        
        when(lawyerSpecializationRepo.findBySpecializationType(SpecializationType.CIVIL_LAW))
            .thenReturn(List.of(testSpec1, testSpec5));
        
        List<LawyerSpecialization> foundSpecs = lawyerSpecializationRepo.findBySpecializationType(SpecializationType.CIVIL_LAW);
        
        assertEquals(2, foundSpecs.size());
        assertTrue(foundSpecs.stream().allMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CIVIL_LAW)));
    }

    @Test
    void testFindAll() {
        when(lawyerSpecializationRepo.findAll()).thenReturn(List.of(testSpec1, testSpec2, testSpec3, testSpec4));
        
        List<LawyerSpecialization> allSpecs = lawyerSpecializationRepo.findAll();
        
        assertEquals(4, allSpecs.size());
        assertTrue(allSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CIVIL_LAW)));
        assertTrue(allSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CRIMINAL_LAW)));
        assertTrue(allSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.FAMILY_LAW)));
        assertTrue(allSpecs.stream().anyMatch(spec -> spec.getSpecializationType().equals(SpecializationType.CORPORATE_LAW)));
    }

    @Test
    void testExistsById() {
        when(lawyerSpecializationRepo.existsById(specId1)).thenReturn(true);
        when(lawyerSpecializationRepo.existsById(UUID.randomUUID())).thenReturn(false);
        
        assertTrue(lawyerSpecializationRepo.existsById(specId1));
        assertFalse(lawyerSpecializationRepo.existsById(UUID.randomUUID()));
    }
} 