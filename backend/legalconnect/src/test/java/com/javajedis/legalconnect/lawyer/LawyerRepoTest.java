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
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class LawyerRepoTest {

    @Mock
    private LawyerRepo lawyerRepo;

    private User testUser1;
    private User testUser2;
    private Lawyer testLawyer1;
    private Lawyer testLawyer2;
    private UUID userId1;
    private UUID userId2;
    private UUID lawyerId1;
    private UUID lawyerId2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        lawyerId1 = UUID.randomUUID();
        lawyerId2 = UUID.randomUUID();
        
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
        testLawyer1.setBio("Experienced lawyer with expertise in civil law");
        testLawyer1.setBarCertificateFileUrl("https://example.com/certificate1.pdf");
        testLawyer1.setVerificationStatus(VerificationStatus.PENDING);
        testLawyer1.setCreatedAt(OffsetDateTime.now());
        testLawyer1.setUpdatedAt(OffsetDateTime.now());
        
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
        testLawyer2.setBio("Senior lawyer with expertise in criminal law");
        testLawyer2.setBarCertificateFileUrl("https://example.com/certificate2.pdf");
        testLawyer2.setVerificationStatus(VerificationStatus.APPROVED);
        testLawyer2.setCreatedAt(OffsetDateTime.now());
        testLawyer2.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testFindById() {
        when(lawyerRepo.findById(lawyerId1)).thenReturn(Optional.of(testLawyer1));
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findById(lawyerId1);
        
        assertTrue(foundLawyer.isPresent());
        assertEquals(testLawyer1.getFirm(), foundLawyer.get().getFirm());
        assertEquals(testLawyer1.getBarCertificateNumber(), foundLawyer.get().getBarCertificateNumber());
    }

    @Test
    void testFindByIdNotFound() {
        when(lawyerRepo.findById(UUID.randomUUID())).thenReturn(Optional.empty());
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findById(UUID.randomUUID());
        assertFalse(foundLawyer.isPresent());
    }

    @Test
    void testFindByUser() {
        when(lawyerRepo.findByUser(testUser1)).thenReturn(Optional.of(testLawyer1));
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findByUser(testUser1);
        
        assertTrue(foundLawyer.isPresent());
        assertEquals(testLawyer1.getFirm(), foundLawyer.get().getFirm());
        assertEquals(testLawyer1.getBarCertificateNumber(), foundLawyer.get().getBarCertificateNumber());
    }

    @Test
    void testFindByUserNotFound() {
        when(lawyerRepo.findByUser(testUser1)).thenReturn(Optional.empty());
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findByUser(testUser1);
        assertFalse(foundLawyer.isPresent());
    }

    @Test
    void testFindByUserId() {
        when(lawyerRepo.findByUserId(userId1)).thenReturn(Optional.of(testLawyer1));
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findByUserId(userId1);
        
        assertTrue(foundLawyer.isPresent());
        assertEquals(testLawyer1.getFirm(), foundLawyer.get().getFirm());
        assertEquals(testLawyer1.getBarCertificateNumber(), foundLawyer.get().getBarCertificateNumber());
    }

    @Test
    void testFindByUserIdNotFound() {
        when(lawyerRepo.findByUserId(UUID.randomUUID())).thenReturn(Optional.empty());
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findByUserId(UUID.randomUUID());
        assertFalse(foundLawyer.isPresent());
    }

    @Test
    void testFindByBarCertificateNumber() {
        when(lawyerRepo.findByBarCertificateNumber("BAR123456")).thenReturn(Optional.of(testLawyer1));
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findByBarCertificateNumber("BAR123456");
        
        assertTrue(foundLawyer.isPresent());
        assertEquals(testLawyer1.getFirm(), foundLawyer.get().getFirm());
        assertEquals(testLawyer1.getUser().getId(), foundLawyer.get().getUser().getId());
    }

    @Test
    void testFindByBarCertificateNumberNotFound() {
        when(lawyerRepo.findByBarCertificateNumber("NONEXISTENT")).thenReturn(Optional.empty());
        
        Optional<Lawyer> foundLawyer = lawyerRepo.findByBarCertificateNumber("NONEXISTENT");
        assertFalse(foundLawyer.isPresent());
    }

    @Test
    void testExistsByBarCertificateNumber() {
        when(lawyerRepo.existsByBarCertificateNumber("BAR123456")).thenReturn(true);
        when(lawyerRepo.existsByBarCertificateNumber("NONEXISTENT")).thenReturn(false);
        
        assertTrue(lawyerRepo.existsByBarCertificateNumber("BAR123456"));
        assertFalse(lawyerRepo.existsByBarCertificateNumber("NONEXISTENT"));
    }

    @Test
    void testExistsByUser() {
        when(lawyerRepo.existsByUser(testUser1)).thenReturn(true);
        when(lawyerRepo.existsByUser(testUser2)).thenReturn(false);
        
        assertTrue(lawyerRepo.existsByUser(testUser1));
        assertFalse(lawyerRepo.existsByUser(testUser2));
    }

    @Test
    void testExistsByUserId() {
        when(lawyerRepo.existsByUserId(userId1)).thenReturn(true);
        when(lawyerRepo.existsByUserId(userId2)).thenReturn(false);
        
        assertTrue(lawyerRepo.existsByUserId(userId1));
        assertFalse(lawyerRepo.existsByUserId(userId2));
    }

    @Test
    void testFindAll() {
        when(lawyerRepo.findAll()).thenReturn(List.of(testLawyer1, testLawyer2));
        
        var allLawyers = lawyerRepo.findAll();
        
        assertEquals(2, allLawyers.size());
        assertTrue(allLawyers.stream().anyMatch(l -> l.getBarCertificateNumber().equals("BAR123456")));
        assertTrue(allLawyers.stream().anyMatch(l -> l.getBarCertificateNumber().equals("BAR789012")));
    }

    @Test
    void testSaveLawyer() {
        when(lawyerRepo.save(testLawyer1)).thenReturn(testLawyer1);
        
        Lawyer savedLawyer = lawyerRepo.save(testLawyer1);
        
        assertNotNull(savedLawyer);
        assertEquals(testLawyer1.getFirm(), savedLawyer.getFirm());
        assertEquals(testLawyer1.getBarCertificateNumber(), savedLawyer.getBarCertificateNumber());
        assertEquals(testLawyer1.getUser().getId(), savedLawyer.getUser().getId());
    }

    @Test
    void testExistsById() {
        when(lawyerRepo.existsById(lawyerId1)).thenReturn(true);
        when(lawyerRepo.existsById(UUID.randomUUID())).thenReturn(false);
        
        assertTrue(lawyerRepo.existsById(lawyerId1));
        assertFalse(lawyerRepo.existsById(UUID.randomUUID()));
    }
} 