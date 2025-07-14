package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.Role;
import com.javajedis.legalconnect.user.User;

class ScheduleTest {

    private Schedule schedule;
    private Case caseEntity;
    private User lawyerUser;
    private User clientUser;
    private Lawyer lawyer;
    private UUID testScheduleId;
    private UUID testCaseId;
    private UUID lawyerId;
    private UUID lawyerUserId;
    private UUID clientUserId;

    @BeforeEach
    void setUp() {
        testScheduleId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        lawyerUserId = UUID.randomUUID();
        clientUserId = UUID.randomUUID();
        
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
        
        // Setup schedule
        schedule = new Schedule();
        schedule.setId(testScheduleId);
        schedule.setCaseEntity(caseEntity);
        schedule.setLawyer(lawyerUser);
        schedule.setClient(clientUser);
        schedule.setTitle("Test Schedule Title");
        schedule.setType(ScheduleType.MEETING);
        schedule.setDescription("Test schedule description");
        schedule.setDate(LocalDate.now());
        schedule.setStartTime(OffsetDateTime.now());
        schedule.setEndTime(OffsetDateTime.now().plusHours(1));
    }

    @Test
    void testDefaultConstructor() {
        Schedule defaultSchedule = new Schedule();
        
        assertNotNull(defaultSchedule);
        assertNull(defaultSchedule.getId());
        assertNull(defaultSchedule.getCaseEntity());
        assertNull(defaultSchedule.getLawyer());
        assertNull(defaultSchedule.getClient());
        assertNull(defaultSchedule.getTitle());
        assertNull(defaultSchedule.getType());
        assertNull(defaultSchedule.getDescription());
        assertNull(defaultSchedule.getDate());
        assertNull(defaultSchedule.getStartTime());
        assertNull(defaultSchedule.getEndTime());
        assertNull(defaultSchedule.getCreatedAt());
        assertNull(defaultSchedule.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = LocalDate.now();
        
        Schedule constructedSchedule = new Schedule(
            testScheduleId, caseEntity, lawyerUser, clientUser,
            "Constructor Test Schedule", ScheduleType.COURT_HEARING,
            "Constructor test description", today, now, now.plusHours(2),
            now, now
        );
        
        assertEquals(testScheduleId, constructedSchedule.getId());
        assertEquals(caseEntity, constructedSchedule.getCaseEntity());
        assertEquals(lawyerUser, constructedSchedule.getLawyer());
        assertEquals(clientUser, constructedSchedule.getClient());
        assertEquals("Constructor Test Schedule", constructedSchedule.getTitle());
        assertEquals(ScheduleType.COURT_HEARING, constructedSchedule.getType());
        assertEquals("Constructor test description", constructedSchedule.getDescription());
        assertEquals(today, constructedSchedule.getDate());
        assertEquals(now, constructedSchedule.getStartTime());
        assertEquals(now.plusHours(2), constructedSchedule.getEndTime());
        assertEquals(now, constructedSchedule.getCreatedAt());
        assertEquals(now, constructedSchedule.getUpdatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        UUID newId = UUID.randomUUID();
        schedule.setId(newId);
        assertEquals(newId, schedule.getId());
    }

    @Test
    void testCaseEntityGetterAndSetter() {
        Case newCase = new Case();
        newCase.setId(UUID.randomUUID());
        newCase.setTitle("New Case");
        
        schedule.setCaseEntity(newCase);
        assertEquals(newCase, schedule.getCaseEntity());
        assertEquals("New Case", schedule.getCaseEntity().getTitle());
    }

    @Test
    void testLawyerGetterAndSetter() {
        User newLawyer = new User();
        newLawyer.setId(UUID.randomUUID());
        newLawyer.setEmail("newlawyer@example.com");
        newLawyer.setRole(Role.LAWYER);
        
        schedule.setLawyer(newLawyer);
        assertEquals(newLawyer, schedule.getLawyer());
        assertEquals("newlawyer@example.com", schedule.getLawyer().getEmail());
    }

    @Test
    void testClientGetterAndSetter() {
        User newClient = new User();
        newClient.setId(UUID.randomUUID());
        newClient.setEmail("newclient@example.com");
        newClient.setRole(Role.USER);
        
        schedule.setClient(newClient);
        assertEquals(newClient, schedule.getClient());
        assertEquals("newclient@example.com", schedule.getClient().getEmail());
    }

    @Test
    void testTitleGetterAndSetter() {
        String newTitle = "Updated Schedule Title";
        schedule.setTitle(newTitle);
        assertEquals(newTitle, schedule.getTitle());
    }

    @Test
    void testTypeGetterAndSetter() {
        ScheduleType newType = ScheduleType.MEDIATION_SESSION;
        schedule.setType(newType);
        assertEquals(newType, schedule.getType());
    }

    @Test
    void testDescriptionGetterAndSetter() {
        String newDescription = "Updated schedule description with more details";
        schedule.setDescription(newDescription);
        assertEquals(newDescription, schedule.getDescription());
    }

    @Test
    void testDescriptionWithNullValue() {
        schedule.setDescription(null);
        assertNull(schedule.getDescription());
    }

    @Test
    void testDescriptionWithEmptyString() {
        schedule.setDescription("");
        assertEquals("", schedule.getDescription());
    }

    @Test
    void testDateGetterAndSetter() {
        LocalDate newDate = LocalDate.now().plusDays(1);
        schedule.setDate(newDate);
        assertEquals(newDate, schedule.getDate());
    }

    @Test
    void testStartTimeGetterAndSetter() {
        OffsetDateTime newStartTime = OffsetDateTime.now().plusHours(2);
        schedule.setStartTime(newStartTime);
        assertEquals(newStartTime, schedule.getStartTime());
    }

    @Test
    void testEndTimeGetterAndSetter() {
        OffsetDateTime newEndTime = OffsetDateTime.now().plusHours(3);
        schedule.setEndTime(newEndTime);
        assertEquals(newEndTime, schedule.getEndTime());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        OffsetDateTime newCreatedAt = OffsetDateTime.now().minusDays(1);
        schedule.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, schedule.getCreatedAt());
    }

    @Test
    void testUpdatedAtGetterAndSetter() {
        OffsetDateTime newUpdatedAt = OffsetDateTime.now();
        schedule.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, schedule.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Schedule schedule1 = new Schedule();
        schedule1.setId(testScheduleId);
        schedule1.setTitle("Test Schedule");
        schedule1.setType(ScheduleType.MEETING);
        
        Schedule schedule2 = new Schedule();
        schedule2.setId(testScheduleId);
        schedule2.setTitle("Test Schedule");
        schedule2.setType(ScheduleType.MEETING);
        
        Schedule schedule3 = new Schedule();
        schedule3.setId(UUID.randomUUID());
        schedule3.setTitle("Different Schedule");
        schedule3.setType(ScheduleType.COURT_HEARING);
        
        assertEquals(schedule1, schedule2);
        assertEquals(schedule1.hashCode(), schedule2.hashCode());
        assertNotEquals(schedule1, schedule3);
        assertNotEquals(schedule1.hashCode(), schedule3.hashCode());
    }

    @Test
    void testEqualsWithNullValues() {
        Schedule schedule1 = new Schedule();
        Schedule schedule2 = new Schedule();
        
        assertEquals(schedule1, schedule2);
        
        schedule1.setId(testScheduleId);
        assertNotEquals(schedule1, schedule2);
    }

    @Test
    void testToString() {
        String toStringResult = schedule.toString();
        
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Schedule"));
        assertTrue(toStringResult.contains("Test Schedule Title"));
        assertTrue(toStringResult.contains("MEETING"));
    }

    @Test
    void testAllScheduleTypes() {
        ScheduleType[] allTypes = ScheduleType.values();
        
        assertTrue(allTypes.length > 0);
        
        for (ScheduleType type : allTypes) {
            schedule.setType(type);
            assertEquals(type, schedule.getType());
        }
    }

    @Test
    void testScheduleWithLongTitle() {
        String longTitle = "This is a very long schedule title that exceeds normal length to test the entity's ability to handle long titles";
        schedule.setTitle(longTitle);
        assertEquals(longTitle, schedule.getTitle());
    }

    @Test
    void testScheduleWithLongDescription() {
        String longDescription = "This is a very long description that contains detailed information about the schedule, including multiple paragraphs and extensive details about what will be discussed during the meeting";
        schedule.setDescription(longDescription);
        assertEquals(longDescription, schedule.getDescription());
    }

    @Test
    void testScheduleRelationships() {
        assertNotNull(schedule.getCaseEntity());
        assertNotNull(schedule.getLawyer());
        assertNotNull(schedule.getClient());
        
        assertEquals(caseEntity, schedule.getCaseEntity());
        assertEquals(lawyerUser, schedule.getLawyer());
        assertEquals(clientUser, schedule.getClient());
        
        // Test that the relationships are correctly set
        assertEquals(testCaseId, schedule.getCaseEntity().getId());
        assertEquals(lawyerUserId, schedule.getLawyer().getId());
        assertEquals(clientUserId, schedule.getClient().getId());
    }

    @Test
    void testScheduleWithTimestamps() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();
        
        schedule.setCreatedAt(createdAt);
        schedule.setUpdatedAt(updatedAt);
        
        assertEquals(createdAt, schedule.getCreatedAt());
        assertEquals(updatedAt, schedule.getUpdatedAt());
        assertTrue(schedule.getUpdatedAt().isAfter(schedule.getCreatedAt()));
    }

    @Test
    void testScheduleTimeRange() {
        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plusHours(2);
        
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
        assertTrue(schedule.getEndTime().isAfter(schedule.getStartTime()));
    }

    @Test
    void testScheduleWithMinimalData() {
        Schedule minimalSchedule = new Schedule();
        minimalSchedule.setTitle("Minimal Schedule");
        minimalSchedule.setType(ScheduleType.MEETING);
        minimalSchedule.setDate(LocalDate.now());
        minimalSchedule.setStartTime(OffsetDateTime.now());
        minimalSchedule.setEndTime(OffsetDateTime.now().plusHours(1));
        
        assertEquals("Minimal Schedule", minimalSchedule.getTitle());
        assertEquals(ScheduleType.MEETING, minimalSchedule.getType());
        assertNotNull(minimalSchedule.getDate());
        assertNotNull(minimalSchedule.getStartTime());
        assertNotNull(minimalSchedule.getEndTime());
    }

    @Test
    void testScheduleEntityFields() {
        assertNotNull(schedule.getId());
        assertNotNull(schedule.getCaseEntity());
        assertNotNull(schedule.getLawyer());
        assertNotNull(schedule.getClient());
        assertNotNull(schedule.getTitle());
        assertNotNull(schedule.getType());
        assertNotNull(schedule.getDescription());
        assertNotNull(schedule.getDate());
        assertNotNull(schedule.getStartTime());
        assertNotNull(schedule.getEndTime());
        
        assertEquals(testScheduleId, schedule.getId());
        assertEquals("Test Schedule Title", schedule.getTitle());
        assertEquals(ScheduleType.MEETING, schedule.getType());
        assertEquals("Test schedule description", schedule.getDescription());
    }
} 