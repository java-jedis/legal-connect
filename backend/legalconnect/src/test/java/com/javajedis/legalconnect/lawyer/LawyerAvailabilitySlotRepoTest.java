package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;
import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class LawyerAvailabilitySlotRepoTest {

    @Mock
    private LawyerAvailabilitySlotRepo lawyerAvailabilitySlotRepo;

    private User testUser1;
    private User testUser2;
    private Lawyer testLawyer1;
    private Lawyer testLawyer2;
    private LawyerAvailabilitySlot testSlot1;
    private LawyerAvailabilitySlot testSlot2;
    private LawyerAvailabilitySlot testSlot3;
    private UUID userId1;
    private UUID userId2;
    private UUID lawyerId1;
    private UUID lawyerId2;
    private UUID slotId1;
    private UUID slotId2;
    private UUID slotId3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        lawyerId1 = UUID.randomUUID();
        lawyerId2 = UUID.randomUUID();
        slotId1 = UUID.randomUUID();
        slotId2 = UUID.randomUUID();
        slotId3 = UUID.randomUUID();
        
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
        
        // Setup test slot 1 (Monday 9-17)
        testSlot1 = new LawyerAvailabilitySlot();
        testSlot1.setId(slotId1);
        testSlot1.setLawyer(testLawyer1);
        testSlot1.setDay(DayOfWeek.MON);
        testSlot1.setStartTime(LocalTime.of(9, 0));
        testSlot1.setEndTime(LocalTime.of(17, 0));
        
        // Setup test slot 2 (Tuesday 10-18)
        testSlot2 = new LawyerAvailabilitySlot();
        testSlot2.setId(slotId2);
        testSlot2.setLawyer(testLawyer1);
        testSlot2.setDay(DayOfWeek.TUE);
        testSlot2.setStartTime(LocalTime.of(10, 0));
        testSlot2.setEndTime(LocalTime.of(18, 0));
        
        // Setup test slot 3 (Wednesday 8-16) for lawyer 2
        testSlot3 = new LawyerAvailabilitySlot();
        testSlot3.setId(slotId3);
        testSlot3.setLawyer(testLawyer2);
        testSlot3.setDay(DayOfWeek.WED);
        testSlot3.setStartTime(LocalTime.of(8, 0));
        testSlot3.setEndTime(LocalTime.of(16, 0));
    }

    @Test
    void testSaveSlot() {
        when(lawyerAvailabilitySlotRepo.save(testSlot1)).thenReturn(testSlot1);
        
        LawyerAvailabilitySlot savedSlot = lawyerAvailabilitySlotRepo.save(testSlot1);
        
        assertNotNull(savedSlot);
        assertEquals(testSlot1.getDay(), savedSlot.getDay());
        assertEquals(testSlot1.getStartTime(), savedSlot.getStartTime());
        assertEquals(testSlot1.getEndTime(), savedSlot.getEndTime());
        assertEquals(testSlot1.getLawyer().getId(), savedSlot.getLawyer().getId());
    }

    @Test
    void testFindById() {
        when(lawyerAvailabilitySlotRepo.findById(slotId1)).thenReturn(Optional.of(testSlot1));
        
        Optional<LawyerAvailabilitySlot> foundSlot = lawyerAvailabilitySlotRepo.findById(slotId1);
        
        assertTrue(foundSlot.isPresent());
        assertEquals(testSlot1.getDay(), foundSlot.get().getDay());
        assertEquals(testSlot1.getStartTime(), foundSlot.get().getStartTime());
        assertEquals(testSlot1.getEndTime(), foundSlot.get().getEndTime());
    }

    @Test
    void testFindByIdNotFound() {
        when(lawyerAvailabilitySlotRepo.findById(UUID.randomUUID())).thenReturn(Optional.empty());
        
        Optional<LawyerAvailabilitySlot> foundSlot = lawyerAvailabilitySlotRepo.findById(UUID.randomUUID());
        assertFalse(foundSlot.isPresent());
    }

    @Test
    void testFindByLawyerId() {
        when(lawyerAvailabilitySlotRepo.findByLawyerId(lawyerId1)).thenReturn(List.of(testSlot1, testSlot2));
        
        List<LawyerAvailabilitySlot> foundSlots = lawyerAvailabilitySlotRepo.findByLawyerId(lawyerId1);
        
        assertEquals(2, foundSlots.size());
        assertTrue(foundSlots.stream().allMatch(slot -> slot.getLawyer().getId().equals(lawyerId1)));
        assertTrue(foundSlots.stream().anyMatch(slot -> slot.getDay().equals(DayOfWeek.MON)));
        assertTrue(foundSlots.stream().anyMatch(slot -> slot.getDay().equals(DayOfWeek.TUE)));
    }

    @Test
    void testFindByLawyerIdEmpty() {
        when(lawyerAvailabilitySlotRepo.findByLawyerId(lawyerId1)).thenReturn(List.of());
        
        List<LawyerAvailabilitySlot> foundSlots = lawyerAvailabilitySlotRepo.findByLawyerId(lawyerId1);
        assertTrue(foundSlots.isEmpty());
    }

    @Test
    void testFindByLawyerIdNonExistent() {
        when(lawyerAvailabilitySlotRepo.findByLawyerId(UUID.randomUUID())).thenReturn(List.of());
        
        List<LawyerAvailabilitySlot> foundSlots = lawyerAvailabilitySlotRepo.findByLawyerId(UUID.randomUUID());
        assertTrue(foundSlots.isEmpty());
    }

    @Test
    void testFindAll() {
        when(lawyerAvailabilitySlotRepo.findAll()).thenReturn(List.of(testSlot1, testSlot2, testSlot3));
        
        List<LawyerAvailabilitySlot> allSlots = lawyerAvailabilitySlotRepo.findAll();
        
        assertEquals(3, allSlots.size());
        assertTrue(allSlots.stream().anyMatch(slot -> slot.getDay().equals(DayOfWeek.MON)));
        assertTrue(allSlots.stream().anyMatch(slot -> slot.getDay().equals(DayOfWeek.TUE)));
        assertTrue(allSlots.stream().anyMatch(slot -> slot.getDay().equals(DayOfWeek.WED)));
    }

    @Test
    void testExistsById() {
        when(lawyerAvailabilitySlotRepo.existsById(slotId1)).thenReturn(true);
        when(lawyerAvailabilitySlotRepo.existsById(UUID.randomUUID())).thenReturn(false);
        
        assertTrue(lawyerAvailabilitySlotRepo.existsById(slotId1));
        assertFalse(lawyerAvailabilitySlotRepo.existsById(UUID.randomUUID()));
    }
}