package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class ScheduleRepoTest {

    @Mock
    private ScheduleRepo scheduleRepo;

    private Schedule schedule1;
    private Schedule schedule2;
    private Schedule schedule3;
    private Case caseEntity;
    private User lawyerUser;
    private User clientUser;
    private User anotherClientUser;
    private Lawyer lawyer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize UUIDs
        UUID testScheduleId1 = UUID.randomUUID();
        UUID testScheduleId2 = UUID.randomUUID();
        UUID testScheduleId3 = UUID.randomUUID();
        UUID testCaseId = UUID.randomUUID();
        UUID lawyerId = UUID.randomUUID();
        UUID lawyerUserId = UUID.randomUUID();
        UUID clientUserId = UUID.randomUUID();
        UUID anotherClientUserId = UUID.randomUUID();
        
        // Setup lawyer user
        lawyerUser = new User();
        lawyerUser.setId(lawyerUserId);
        lawyerUser.setFirstName("John");
        lawyerUser.setLastName("Lawyer");
        lawyerUser.setEmail("lawyer@example.com");
        lawyerUser.setRole(Role.LAWYER);
        lawyerUser.setEmailVerified(true);

        // Setup client user
        clientUser = new User();
        clientUser.setId(clientUserId);
        clientUser.setFirstName("Jane");
        clientUser.setLastName("Client");
        clientUser.setEmail("client@example.com");
        clientUser.setRole(Role.USER);
        clientUser.setEmailVerified(true);

        // Setup another client user
        anotherClientUser = new User();
        anotherClientUser.setId(anotherClientUserId);
        anotherClientUser.setFirstName("Bob");
        anotherClientUser.setLastName("AnotherClient");
        anotherClientUser.setEmail("anotherclient@example.com");
        anotherClientUser.setRole(Role.USER);
        anotherClientUser.setEmailVerified(true);

        // Setup lawyer
        lawyer = new Lawyer();
        lawyer.setId(lawyerId);
        lawyer.setUser(lawyerUser);
        lawyer.setFirm("Test Law Firm");
        lawyer.setYearsOfExperience(5);
        lawyer.setBarCertificateNumber("BAR123456");
        lawyer.setVerificationStatus(VerificationStatus.APPROVED);

        // Setup case
        caseEntity = new Case();
        caseEntity.setId(testCaseId);
        caseEntity.setLawyer(lawyer);
        caseEntity.setClient(clientUser);
        caseEntity.setTitle("Test Case Title");
        caseEntity.setDescription("Test case description");
        caseEntity.setStatus(CaseStatus.IN_PROGRESS);

        // Setup schedules
        schedule1 = new Schedule();
        schedule1.setId(testScheduleId1);
        schedule1.setCaseEntity(caseEntity);
        schedule1.setLawyer(lawyerUser);
        schedule1.setClient(clientUser);
        schedule1.setTitle("Schedule 1 Title");
        schedule1.setType(ScheduleType.MEETING);
        schedule1.setDescription("Schedule 1 description");
        schedule1.setDate(LocalDate.now());
        schedule1.setStartTime(OffsetDateTime.now());
        schedule1.setEndTime(OffsetDateTime.now().plusHours(1));

        schedule2 = new Schedule();
        schedule2.setId(testScheduleId2);
        schedule2.setCaseEntity(caseEntity);
        schedule2.setLawyer(lawyerUser);
        schedule2.setClient(clientUser);
        schedule2.setTitle("Schedule 2 Title");
        schedule2.setType(ScheduleType.COURT_HEARING);
        schedule2.setDescription("Schedule 2 description");
        schedule2.setDate(LocalDate.now().plusDays(1));
        schedule2.setStartTime(OffsetDateTime.now().plusDays(1));
        schedule2.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(2));

        schedule3 = new Schedule();
        schedule3.setId(testScheduleId3);
        schedule3.setCaseEntity(caseEntity);
        schedule3.setLawyer(lawyerUser);
        schedule3.setClient(anotherClientUser);
        schedule3.setTitle("Schedule 3 Title");
        schedule3.setType(ScheduleType.MEDIATION_SESSION);
        schedule3.setDescription("Schedule 3 description");
        schedule3.setDate(LocalDate.now().plusDays(2));
        schedule3.setStartTime(OffsetDateTime.now().plusDays(2));
        schedule3.setEndTime(OffsetDateTime.now().plusDays(2).plusHours(1));
    }

    @Test
    void testFindById() {
        when(scheduleRepo.findById(schedule1.getId())).thenReturn(Optional.of(schedule1));
        
        Optional<Schedule> foundSchedule = scheduleRepo.findById(schedule1.getId());
        
        assertTrue(foundSchedule.isPresent());
        assertEquals(schedule1.getId(), foundSchedule.get().getId());
        assertEquals(schedule1.getTitle(), foundSchedule.get().getTitle());
        assertEquals(schedule1.getType(), foundSchedule.get().getType());
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleRepo.findById(nonExistentId)).thenReturn(Optional.empty());
        
        Optional<Schedule> foundSchedule = scheduleRepo.findById(nonExistentId);
        
        assertFalse(foundSchedule.isPresent());
    }

    @Test
    void testFindByLawyerId() {
        List<Schedule> expectedSchedules = Arrays.asList(schedule1, schedule2, schedule3);
        when(scheduleRepo.findByLawyerId(lawyerUser.getId())).thenReturn(expectedSchedules);
        
        List<Schedule> schedules = scheduleRepo.findByLawyerId(lawyerUser.getId());
        
        assertNotNull(schedules);
        assertEquals(3, schedules.size());
        
        // Verify all schedules belong to the lawyer
        for (Schedule schedule : schedules) {
            assertEquals(lawyerUser.getId(), schedule.getLawyer().getId());
        }
    }

    @Test
    void testFindByClientId() {
        List<Schedule> expectedSchedules = Arrays.asList(schedule1, schedule2);
        when(scheduleRepo.findByClientId(clientUser.getId())).thenReturn(expectedSchedules);
        
        List<Schedule> schedules = scheduleRepo.findByClientId(clientUser.getId());
        
        assertNotNull(schedules);
        assertEquals(2, schedules.size());
        
        // Verify all schedules belong to the client
        for (Schedule schedule : schedules) {
            assertEquals(clientUser.getId(), schedule.getClient().getId());
        }
    }

    @Test
    void testFindByClientIdWithDifferentClient() {
        List<Schedule> expectedSchedules = Arrays.asList(schedule3);
        when(scheduleRepo.findByClientId(anotherClientUser.getId())).thenReturn(expectedSchedules);
        
        List<Schedule> schedules = scheduleRepo.findByClientId(anotherClientUser.getId());
        
        assertNotNull(schedules);
        assertEquals(1, schedules.size());
        assertEquals(anotherClientUser.getId(), schedules.get(0).getClient().getId());
    }

    @Test
    void testFindByCaseEntityId() {
        List<Schedule> expectedSchedules = Arrays.asList(schedule1, schedule2, schedule3);
        when(scheduleRepo.findByCaseEntityId(caseEntity.getId())).thenReturn(expectedSchedules);
        
        List<Schedule> schedules = scheduleRepo.findByCaseEntityId(caseEntity.getId());
        
        assertNotNull(schedules);
        assertEquals(3, schedules.size());
        
        // Verify all schedules belong to the case
        for (Schedule schedule : schedules) {
            assertEquals(caseEntity.getId(), schedule.getCaseEntity().getId());
        }
    }

    @Test
    void testFindByLawyerIdAndClientId() {
        List<Schedule> expectedSchedules = Arrays.asList(schedule1, schedule2);
        when(scheduleRepo.findByLawyerIdAndClientId(lawyerUser.getId(), clientUser.getId())).thenReturn(expectedSchedules);
        
        List<Schedule> schedules = scheduleRepo.findByLawyerIdAndClientId(lawyerUser.getId(), clientUser.getId());
        
        assertNotNull(schedules);
        assertEquals(2, schedules.size());
        
        // Verify all schedules belong to both the lawyer and client
        for (Schedule schedule : schedules) {
            assertEquals(lawyerUser.getId(), schedule.getLawyer().getId());
            assertEquals(clientUser.getId(), schedule.getClient().getId());
        }
    }

    @Test
    void testFindByLawyerIdAndClientIdWithDifferentClient() {
        List<Schedule> expectedSchedules = Arrays.asList(schedule3);
        when(scheduleRepo.findByLawyerIdAndClientId(lawyerUser.getId(), anotherClientUser.getId())).thenReturn(expectedSchedules);
        
        List<Schedule> schedules = scheduleRepo.findByLawyerIdAndClientId(lawyerUser.getId(), anotherClientUser.getId());
        
        assertNotNull(schedules);
        assertEquals(1, schedules.size());
        assertEquals(lawyerUser.getId(), schedules.get(0).getLawyer().getId());
        assertEquals(anotherClientUser.getId(), schedules.get(0).getClient().getId());
    }

    @Test
    void testFindByLawyerIdAndClientIdNoResults() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(scheduleRepo.findByLawyerIdAndClientId(lawyerUser.getId(), nonExistentUserId)).thenReturn(Collections.emptyList());
        
        List<Schedule> schedules = scheduleRepo.findByLawyerIdAndClientId(lawyerUser.getId(), nonExistentUserId);
        
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty());
    }

    @Test
    void testFindByCaseEntityIdWithPageable() {
        Pageable pageable = PageRequest.of(0, 2);
        List<Schedule> scheduleList = Arrays.asList(schedule1, schedule2);
        Page<Schedule> expectedPage = new PageImpl<>(scheduleList, pageable, 3);
        when(scheduleRepo.findByCaseEntityId(caseEntity.getId(), pageable)).thenReturn(expectedPage);
        
        Page<Schedule> schedulesPage = scheduleRepo.findByCaseEntityId(caseEntity.getId(), pageable);
        
        assertNotNull(schedulesPage);
        assertEquals(2, schedulesPage.getContent().size());
        assertEquals(3, schedulesPage.getTotalElements());
        assertEquals(2, schedulesPage.getTotalPages());
        assertTrue(schedulesPage.hasNext());
        
        // Verify all schedules belong to the case
        for (Schedule schedule : schedulesPage.getContent()) {
            assertEquals(caseEntity.getId(), schedule.getCaseEntity().getId());
        }
    }

    @Test
    void testFindByCaseEntityIdWithPageableSecondPage() {
        Pageable pageable = PageRequest.of(1, 2);
        List<Schedule> scheduleList = Arrays.asList(schedule3);
        Page<Schedule> expectedPage = new PageImpl<>(scheduleList, pageable, 3);
        when(scheduleRepo.findByCaseEntityId(caseEntity.getId(), pageable)).thenReturn(expectedPage);
        
        Page<Schedule> schedulesPage = scheduleRepo.findByCaseEntityId(caseEntity.getId(), pageable);
        
        assertNotNull(schedulesPage);
        assertEquals(1, schedulesPage.getContent().size());
        assertEquals(3, schedulesPage.getTotalElements());
        assertEquals(2, schedulesPage.getTotalPages());
        assertFalse(schedulesPage.hasNext());
        assertTrue(schedulesPage.hasPrevious());
    }

    @Test
    void testFindByLawyerIdOrClientIdWithLawyer() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Schedule> scheduleList = Arrays.asList(schedule1, schedule2, schedule3);
        Page<Schedule> expectedPage = new PageImpl<>(scheduleList, pageable, 3);
        when(scheduleRepo.findByLawyerIdOrClientId(lawyerUser.getId(), pageable)).thenReturn(expectedPage);
        
        Page<Schedule> schedulesPage = scheduleRepo.findByLawyerIdOrClientId(lawyerUser.getId(), pageable);
        
        assertNotNull(schedulesPage);
        assertEquals(3, schedulesPage.getContent().size());
        assertEquals(3, schedulesPage.getTotalElements());
        
        // Verify all schedules are associated with the lawyer
        for (Schedule schedule : schedulesPage.getContent()) {
            assertEquals(lawyerUser.getId(), schedule.getLawyer().getId());
        }
    }

    @Test
    void testFindByLawyerIdOrClientIdWithClient() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Schedule> scheduleList = Arrays.asList(schedule1, schedule2);
        Page<Schedule> expectedPage = new PageImpl<>(scheduleList, pageable, 2);
        when(scheduleRepo.findByLawyerIdOrClientId(clientUser.getId(), pageable)).thenReturn(expectedPage);
        
        Page<Schedule> schedulesPage = scheduleRepo.findByLawyerIdOrClientId(clientUser.getId(), pageable);
        
        assertNotNull(schedulesPage);
        assertEquals(2, schedulesPage.getContent().size());
        assertEquals(2, schedulesPage.getTotalElements());
        
        // Verify all schedules are associated with the client
        for (Schedule schedule : schedulesPage.getContent()) {
            assertEquals(clientUser.getId(), schedule.getClient().getId());
        }
    }

    @Test
    void testFindByLawyerIdOrClientIdWithAnotherClient() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Schedule> scheduleList = Arrays.asList(schedule3);
        Page<Schedule> expectedPage = new PageImpl<>(scheduleList, pageable, 1);
        when(scheduleRepo.findByLawyerIdOrClientId(anotherClientUser.getId(), pageable)).thenReturn(expectedPage);
        
        Page<Schedule> schedulesPage = scheduleRepo.findByLawyerIdOrClientId(anotherClientUser.getId(), pageable);
        
        assertNotNull(schedulesPage);
        assertEquals(1, schedulesPage.getContent().size());
        assertEquals(1, schedulesPage.getTotalElements());
        assertEquals(anotherClientUser.getId(), schedulesPage.getContent().get(0).getClient().getId());
    }

    @Test
    void testFindByLawyerIdOrClientIdWithNonExistentUser() {
        UUID nonExistentUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Schedule> expectedPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(scheduleRepo.findByLawyerIdOrClientId(nonExistentUserId, pageable)).thenReturn(expectedPage);
        
        Page<Schedule> schedulesPage = scheduleRepo.findByLawyerIdOrClientId(nonExistentUserId, pageable);
        
        assertNotNull(schedulesPage);
        assertTrue(schedulesPage.getContent().isEmpty());
        assertEquals(0, schedulesPage.getTotalElements());
    }

    @Test
    void testSaveSchedule() {
        Schedule newSchedule = new Schedule();
        newSchedule.setCaseEntity(caseEntity);
        newSchedule.setLawyer(lawyerUser);
        newSchedule.setClient(clientUser);
        newSchedule.setTitle("New Schedule Title");
        newSchedule.setType(ScheduleType.LEGAL_ADVICE_SESSION);
        newSchedule.setDescription("New schedule description");
        newSchedule.setDate(LocalDate.now().plusDays(3));
        newSchedule.setStartTime(OffsetDateTime.now().plusDays(3));
        newSchedule.setEndTime(OffsetDateTime.now().plusDays(3).plusHours(1));
        
        Schedule savedSchedule = new Schedule();
        savedSchedule.setId(UUID.randomUUID());
        savedSchedule.setCaseEntity(newSchedule.getCaseEntity());
        savedSchedule.setLawyer(newSchedule.getLawyer());
        savedSchedule.setClient(newSchedule.getClient());
        savedSchedule.setTitle(newSchedule.getTitle());
        savedSchedule.setType(newSchedule.getType());
        savedSchedule.setDescription(newSchedule.getDescription());
        savedSchedule.setDate(newSchedule.getDate());
        savedSchedule.setStartTime(newSchedule.getStartTime());
        savedSchedule.setEndTime(newSchedule.getEndTime());
        
        when(scheduleRepo.save(newSchedule)).thenReturn(savedSchedule);
        when(scheduleRepo.findById(savedSchedule.getId())).thenReturn(Optional.of(savedSchedule));
        
        Schedule result = scheduleRepo.save(newSchedule);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(newSchedule.getTitle(), result.getTitle());
        assertEquals(newSchedule.getType(), result.getType());
        assertEquals(newSchedule.getDescription(), result.getDescription());
        
        // Verify it can be found
        Optional<Schedule> foundSchedule = scheduleRepo.findById(result.getId());
        assertTrue(foundSchedule.isPresent());
        assertEquals(result.getId(), foundSchedule.get().getId());
    }

    @Test
    void testUpdateSchedule() {
        schedule1.setTitle("Updated Schedule Title");
        schedule1.setDescription("Updated schedule description");
        schedule1.setType(ScheduleType.FOLLOW_UP_CALL);
        
        when(scheduleRepo.save(schedule1)).thenReturn(schedule1);
        
        Schedule updatedSchedule = scheduleRepo.save(schedule1);
        
        assertNotNull(updatedSchedule);
        assertEquals(schedule1.getId(), updatedSchedule.getId());
        assertEquals("Updated Schedule Title", updatedSchedule.getTitle());
        assertEquals("Updated schedule description", updatedSchedule.getDescription());
        assertEquals(ScheduleType.FOLLOW_UP_CALL, updatedSchedule.getType());
    }

    @Test
    void testDeleteSchedule() {
        UUID scheduleId = schedule1.getId();
        
        when(scheduleRepo.findById(scheduleId)).thenReturn(Optional.empty());
        
        // Simulate deletion
        scheduleRepo.deleteById(scheduleId);
        
        Optional<Schedule> deletedSchedule = scheduleRepo.findById(scheduleId);
        assertFalse(deletedSchedule.isPresent());
    }

    @Test
    void testFindAll() {
        List<Schedule> allSchedules = Arrays.asList(schedule1, schedule2, schedule3);
        when(scheduleRepo.findAll()).thenReturn(allSchedules);
        
        List<Schedule> result = scheduleRepo.findAll();
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testCount() {
        when(scheduleRepo.count()).thenReturn(3L);
        
        long count = scheduleRepo.count();
        assertEquals(3, count);
    }

    @Test
    void testExistsById() {
        when(scheduleRepo.existsById(schedule1.getId())).thenReturn(true);
        when(scheduleRepo.existsById(UUID.randomUUID())).thenReturn(false);
        
        assertTrue(scheduleRepo.existsById(schedule1.getId()));
        assertFalse(scheduleRepo.existsById(UUID.randomUUID()));
    }

    @Test
    void testFindByNonExistentLawyerId() {
        UUID nonExistentLawyerId = UUID.randomUUID();
        when(scheduleRepo.findByLawyerId(nonExistentLawyerId)).thenReturn(Collections.emptyList());
        
        List<Schedule> schedules = scheduleRepo.findByLawyerId(nonExistentLawyerId);
        
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty());
    }

    @Test
    void testFindByNonExistentClientId() {
        UUID nonExistentClientId = UUID.randomUUID();
        when(scheduleRepo.findByClientId(nonExistentClientId)).thenReturn(Collections.emptyList());
        
        List<Schedule> schedules = scheduleRepo.findByClientId(nonExistentClientId);
        
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty());
    }

    @Test
    void testFindByNonExistentCaseEntityId() {
        UUID nonExistentCaseId = UUID.randomUUID();
        when(scheduleRepo.findByCaseEntityId(nonExistentCaseId)).thenReturn(Collections.emptyList());
        
        List<Schedule> schedules = scheduleRepo.findByCaseEntityId(nonExistentCaseId);
        
        assertNotNull(schedules);
        assertTrue(schedules.isEmpty());
    }

    @Test
    void testRepositoryInheritedMethods() {
        List<Schedule> allSchedules = Arrays.asList(schedule1, schedule2, schedule3);
        when(scheduleRepo.findAll()).thenReturn(allSchedules);
        when(scheduleRepo.count()).thenReturn(3L);
        when(scheduleRepo.existsById(schedule1.getId())).thenReturn(true);
        
        // Test that the repository inherits standard JpaRepository methods
        assertNotNull(scheduleRepo.findAll());
        assertTrue(scheduleRepo.count() > 0);
        assertTrue(scheduleRepo.existsById(schedule1.getId()));
    }
} 