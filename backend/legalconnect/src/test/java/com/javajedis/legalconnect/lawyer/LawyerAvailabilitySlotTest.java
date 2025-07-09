package com.javajedis.legalconnect.lawyer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class LawyerAvailabilitySlotTest {

    private LawyerAvailabilitySlot slot;
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
        
        // Setup test slot
        slot = new LawyerAvailabilitySlot();
        slot.setId(testId);
        slot.setLawyer(lawyer);
        slot.setDay(DayOfWeek.MON);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(17, 0));
    }

    @Test
    void testDefaultConstructor() {
        LawyerAvailabilitySlot defaultSlot = new LawyerAvailabilitySlot();
        
        assertNotNull(defaultSlot);
        assertNull(defaultSlot.getId());
        assertNull(defaultSlot.getLawyer());
        assertNull(defaultSlot.getDay());
        assertNull(defaultSlot.getStartTime());
        assertNull(defaultSlot.getEndTime());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        slot.setId(newId);
        assertEquals(newId, slot.getId());
    }

    @Test
    void testLawyerGetterAndSetter() {
        Lawyer newLawyer = new Lawyer();
        newLawyer.setId(UUID.randomUUID());
        newLawyer.setFirm("New Law Firm");
        
        slot.setLawyer(newLawyer);
        assertEquals(newLawyer, slot.getLawyer());
    }

    @Test
    void testDayGetterAndSetter() {
        DayOfWeek newDay = DayOfWeek.TUE;
        slot.setDay(newDay);
        assertEquals(newDay, slot.getDay());
    }

    @Test
    void testStartTimeGetterAndSetter() {
        LocalTime newStartTime = LocalTime.of(10, 30);
        slot.setStartTime(newStartTime);
        assertEquals(newStartTime, slot.getStartTime());
    }

    @Test
    void testEndTimeGetterAndSetter() {
        LocalTime newEndTime = LocalTime.of(18, 30);
        slot.setEndTime(newEndTime);
        assertEquals(newEndTime, slot.getEndTime());
    }

    @Test
    void testEqualsAndHashCode() {
        LawyerAvailabilitySlot slot1 = new LawyerAvailabilitySlot();
        slot1.setId(testId);
        slot1.setLawyer(lawyer);
        slot1.setDay(DayOfWeek.MON);
        slot1.setStartTime(LocalTime.of(9, 0));
        slot1.setEndTime(LocalTime.of(17, 0));
        
        LawyerAvailabilitySlot slot2 = new LawyerAvailabilitySlot();
        slot2.setId(testId);
        slot2.setLawyer(lawyer);
        slot2.setDay(DayOfWeek.MON);
        slot2.setStartTime(LocalTime.of(9, 0));
        slot2.setEndTime(LocalTime.of(17, 0));
        
        LawyerAvailabilitySlot slot3 = new LawyerAvailabilitySlot();
        slot3.setId(UUID.randomUUID());
        slot3.setLawyer(lawyer);
        slot3.setDay(DayOfWeek.MON);
        slot3.setStartTime(LocalTime.of(9, 0));
        slot3.setEndTime(LocalTime.of(17, 0));
        
        // Test equals
        assertEquals(slot1, slot2);
        assertNotEquals(slot1, slot3);
        assertNotEquals(null,slot1);
        assertEquals(slot1, slot1);
        
        // Test hashCode
        assertEquals(slot1.hashCode(), slot2.hashCode());
        assertNotEquals(slot1.hashCode(), slot3.hashCode());
    }

    @Test
    void testToString() {
        String toString = slot.toString();
        
        assertTrue(toString.contains("id=" + testId));
        assertTrue(toString.contains("day=MON"));
        assertTrue(toString.contains("startTime=09:00"));
        assertTrue(toString.contains("endTime=17:00"));
    }

    @Test
    void testAllDaysOfWeek() {
        slot.setDay(DayOfWeek.MON);
        assertEquals(DayOfWeek.MON, slot.getDay());
        
        slot.setDay(DayOfWeek.TUE);
        assertEquals(DayOfWeek.TUE, slot.getDay());
        
        slot.setDay(DayOfWeek.WED);
        assertEquals(DayOfWeek.WED, slot.getDay());
        
        slot.setDay(DayOfWeek.THU);
        assertEquals(DayOfWeek.THU, slot.getDay());
        
        slot.setDay(DayOfWeek.FRI);
        assertEquals(DayOfWeek.FRI, slot.getDay());
        
        slot.setDay(DayOfWeek.SAT);
        assertEquals(DayOfWeek.SAT, slot.getDay());
        
        slot.setDay(DayOfWeek.SUN);
        assertEquals(DayOfWeek.SUN, slot.getDay());
    }

    @Test
    void testTimeRanges() {
        // Test morning hours
        slot.setStartTime(LocalTime.of(8, 0));
        slot.setEndTime(LocalTime.of(12, 0));
        assertEquals(LocalTime.of(8, 0), slot.getStartTime());
        assertEquals(LocalTime.of(12, 0), slot.getEndTime());
        
        // Test afternoon hours
        slot.setStartTime(LocalTime.of(13, 30));
        slot.setEndTime(LocalTime.of(17, 30));
        assertEquals(LocalTime.of(13, 30), slot.getStartTime());
        assertEquals(LocalTime.of(17, 30), slot.getEndTime());
        
        // Test evening hours
        slot.setStartTime(LocalTime.of(18, 0));
        slot.setEndTime(LocalTime.of(22, 0));
        assertEquals(LocalTime.of(18, 0), slot.getStartTime());
        assertEquals(LocalTime.of(22, 0), slot.getEndTime());
    }

    @Test
    void testNullValues() {
        LawyerAvailabilitySlot nullSlot = new LawyerAvailabilitySlot();
        
        nullSlot.setId(null);
        nullSlot.setLawyer(null);
        nullSlot.setDay(null);
        nullSlot.setStartTime(null);
        nullSlot.setEndTime(null);
        
        assertNull(nullSlot.getId());
        assertNull(nullSlot.getLawyer());
        assertNull(nullSlot.getDay());
        assertNull(nullSlot.getStartTime());
        assertNull(nullSlot.getEndTime());
    }

    @Test
    void testEdgeCaseTimes() {
        // Test midnight
        slot.setStartTime(LocalTime.of(0, 0));
        slot.setEndTime(LocalTime.of(23, 59));
        assertEquals(LocalTime.of(0, 0), slot.getStartTime());
        assertEquals(LocalTime.of(23, 59), slot.getEndTime());
        
        // Test same start and end time
        slot.setStartTime(LocalTime.of(12, 0));
        slot.setEndTime(LocalTime.of(12, 0));
        assertEquals(LocalTime.of(12, 0), slot.getStartTime());
        assertEquals(LocalTime.of(12, 0), slot.getEndTime());
    }
} 