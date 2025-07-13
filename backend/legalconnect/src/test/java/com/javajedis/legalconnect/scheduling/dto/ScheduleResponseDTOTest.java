package com.javajedis.legalconnect.scheduling.dto;

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

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.scheduling.ScheduleType;

class ScheduleResponseDTOTest {

    private ScheduleResponseDTO scheduleResponseDTO;
    private UUID testId;
    private UUID testCaseId;
    private UUID testLawyerId;
    private UUID testClientId;
    private LocalDate testDate;
    private OffsetDateTime testStartTime;
    private OffsetDateTime testEndTime;
    private OffsetDateTime testCreatedAt;
    private OffsetDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        scheduleResponseDTO = new ScheduleResponseDTO();
        testId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testClientId = UUID.randomUUID();
        testDate = LocalDate.of(2024, 12, 25);
        testStartTime = OffsetDateTime.now();
        testEndTime = testStartTime.plusHours(2);
        testCreatedAt = OffsetDateTime.now().minusDays(1);
        testUpdatedAt = OffsetDateTime.now();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(scheduleResponseDTO);
        assertNull(scheduleResponseDTO.getId());
        assertNull(scheduleResponseDTO.getCaseInfo());
        assertNull(scheduleResponseDTO.getLawyerInfo());
        assertNull(scheduleResponseDTO.getClientInfo());
        assertNull(scheduleResponseDTO.getTitle());
        assertNull(scheduleResponseDTO.getType());
        assertNull(scheduleResponseDTO.getDescription());
        assertNull(scheduleResponseDTO.getDate());
        assertNull(scheduleResponseDTO.getStartTime());
        assertNull(scheduleResponseDTO.getEndTime());
        assertNull(scheduleResponseDTO.getCreatedAt());
        assertNull(scheduleResponseDTO.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        scheduleResponseDTO.setId(testId);
        scheduleResponseDTO.setTitle("Test Schedule");
        scheduleResponseDTO.setType(ScheduleType.MEETING);
        scheduleResponseDTO.setDescription("Test description");
        scheduleResponseDTO.setDate(testDate);
        scheduleResponseDTO.setStartTime(testStartTime);
        scheduleResponseDTO.setEndTime(testEndTime);
        scheduleResponseDTO.setCreatedAt(testCreatedAt);
        scheduleResponseDTO.setUpdatedAt(testUpdatedAt);

        assertEquals(testId, scheduleResponseDTO.getId());
        assertEquals("Test Schedule", scheduleResponseDTO.getTitle());
        assertEquals(ScheduleType.MEETING, scheduleResponseDTO.getType());
        assertEquals("Test description", scheduleResponseDTO.getDescription());
        assertEquals(testDate, scheduleResponseDTO.getDate());
        assertEquals(testStartTime, scheduleResponseDTO.getStartTime());
        assertEquals(testEndTime, scheduleResponseDTO.getEndTime());
        assertEquals(testCreatedAt, scheduleResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, scheduleResponseDTO.getUpdatedAt());
    }

    @Test
    void testCaseSummaryDTO() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO();
        caseInfo.setId(testCaseId);
        caseInfo.setTitle("Test Case");
        caseInfo.setStatus(CaseStatus.IN_PROGRESS);

        scheduleResponseDTO.setCaseInfo(caseInfo);

        assertEquals(caseInfo, scheduleResponseDTO.getCaseInfo());
        assertEquals(testCaseId, scheduleResponseDTO.getCaseInfo().getId());
        assertEquals("Test Case", scheduleResponseDTO.getCaseInfo().getTitle());
        assertEquals(CaseStatus.IN_PROGRESS, scheduleResponseDTO.getCaseInfo().getStatus());
    }

    @Test
    void testLawyerSummaryDTO() {
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO();
        lawyerInfo.setId(testLawyerId);
        lawyerInfo.setFirstName("John");
        lawyerInfo.setLastName("Doe");
        lawyerInfo.setEmail("john.doe@example.com");

        scheduleResponseDTO.setLawyerInfo(lawyerInfo);

        assertEquals(lawyerInfo, scheduleResponseDTO.getLawyerInfo());
        assertEquals(testLawyerId, scheduleResponseDTO.getLawyerInfo().getId());
        assertEquals("John", scheduleResponseDTO.getLawyerInfo().getFirstName());
        assertEquals("Doe", scheduleResponseDTO.getLawyerInfo().getLastName());
        assertEquals("john.doe@example.com", scheduleResponseDTO.getLawyerInfo().getEmail());
    }

    @Test
    void testClientSummaryDTO() {
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO();
        clientInfo.setId(testClientId);
        clientInfo.setFirstName("Jane");
        clientInfo.setLastName("Smith");
        clientInfo.setEmail("jane.smith@example.com");

        scheduleResponseDTO.setClientInfo(clientInfo);

        assertEquals(clientInfo, scheduleResponseDTO.getClientInfo());
        assertEquals(testClientId, scheduleResponseDTO.getClientInfo().getId());
        assertEquals("Jane", scheduleResponseDTO.getClientInfo().getFirstName());
        assertEquals("Smith", scheduleResponseDTO.getClientInfo().getLastName());
        assertEquals("jane.smith@example.com", scheduleResponseDTO.getClientInfo().getEmail());
    }

    @Test
    void testScheduleTypeValues() {
        for (ScheduleType type : ScheduleType.values()) {
            scheduleResponseDTO.setType(type);
            assertEquals(type, scheduleResponseDTO.getType());
        }
    }

    @Test
    void testCaseSummaryDTOConstructorAndMethods() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO();
        
        // Test default constructor
        assertNotNull(caseInfo);
        assertNull(caseInfo.getId());
        assertNull(caseInfo.getTitle());
        assertNull(caseInfo.getStatus());

        // Test setters and getters
        caseInfo.setId(testCaseId);
        caseInfo.setTitle("Court Case");
        caseInfo.setStatus(CaseStatus.RESOLVED);

        assertEquals(testCaseId, caseInfo.getId());
        assertEquals("Court Case", caseInfo.getTitle());
        assertEquals(CaseStatus.RESOLVED, caseInfo.getStatus());
    }

    @Test
    void testLawyerSummaryDTOConstructorAndMethods() {
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO();
        
        // Test default constructor
        assertNotNull(lawyerInfo);
        assertNull(lawyerInfo.getId());
        assertNull(lawyerInfo.getFirstName());
        assertNull(lawyerInfo.getLastName());
        assertNull(lawyerInfo.getEmail());

        // Test setters and getters
        lawyerInfo.setId(testLawyerId);
        lawyerInfo.setFirstName("Alice");
        lawyerInfo.setLastName("Johnson");
        lawyerInfo.setEmail("alice.johnson@example.com");

        assertEquals(testLawyerId, lawyerInfo.getId());
        assertEquals("Alice", lawyerInfo.getFirstName());
        assertEquals("Johnson", lawyerInfo.getLastName());
        assertEquals("alice.johnson@example.com", lawyerInfo.getEmail());
    }

    @Test
    void testClientSummaryDTOConstructorAndMethods() {
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO();
        
        // Test default constructor
        assertNotNull(clientInfo);
        assertNull(clientInfo.getId());
        assertNull(clientInfo.getFirstName());
        assertNull(clientInfo.getLastName());
        assertNull(clientInfo.getEmail());

        // Test setters and getters
        clientInfo.setId(testClientId);
        clientInfo.setFirstName("Bob");
        clientInfo.setLastName("Wilson");
        clientInfo.setEmail("bob.wilson@example.com");

        assertEquals(testClientId, clientInfo.getId());
        assertEquals("Bob", clientInfo.getFirstName());
        assertEquals("Wilson", clientInfo.getLastName());
        assertEquals("bob.wilson@example.com", clientInfo.getEmail());
    }

    @Test
    void testCompleteScheduleResponse() {
        // Create nested DTOs
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO();
        caseInfo.setId(testCaseId);
        caseInfo.setTitle("Complete Case");
        caseInfo.setStatus(CaseStatus.IN_PROGRESS);

        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO();
        lawyerInfo.setId(testLawyerId);
        lawyerInfo.setFirstName("John");
        lawyerInfo.setLastName("Doe");
        lawyerInfo.setEmail("john.doe@example.com");

        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO();
        clientInfo.setId(testClientId);
        clientInfo.setFirstName("Jane");
        clientInfo.setLastName("Smith");
        clientInfo.setEmail("jane.smith@example.com");

        // Set all fields
        scheduleResponseDTO.setId(testId);
        scheduleResponseDTO.setCaseInfo(caseInfo);
        scheduleResponseDTO.setLawyerInfo(lawyerInfo);
        scheduleResponseDTO.setClientInfo(clientInfo);
        scheduleResponseDTO.setTitle("Complete Schedule");
        scheduleResponseDTO.setType(ScheduleType.COURT_HEARING);
        scheduleResponseDTO.setDescription("Complete description");
        scheduleResponseDTO.setDate(testDate);
        scheduleResponseDTO.setStartTime(testStartTime);
        scheduleResponseDTO.setEndTime(testEndTime);
        scheduleResponseDTO.setCreatedAt(testCreatedAt);
        scheduleResponseDTO.setUpdatedAt(testUpdatedAt);

        // Verify all fields
        assertEquals(testId, scheduleResponseDTO.getId());
        assertEquals("Complete Schedule", scheduleResponseDTO.getTitle());
        assertEquals(ScheduleType.COURT_HEARING, scheduleResponseDTO.getType());
        assertEquals("Complete description", scheduleResponseDTO.getDescription());
        assertEquals(testDate, scheduleResponseDTO.getDate());
        assertEquals(testStartTime, scheduleResponseDTO.getStartTime());
        assertEquals(testEndTime, scheduleResponseDTO.getEndTime());
        assertEquals(testCreatedAt, scheduleResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, scheduleResponseDTO.getUpdatedAt());

        // Verify nested objects
        assertEquals(testCaseId, scheduleResponseDTO.getCaseInfo().getId());
        assertEquals("Complete Case", scheduleResponseDTO.getCaseInfo().getTitle());
        assertEquals(CaseStatus.IN_PROGRESS, scheduleResponseDTO.getCaseInfo().getStatus());

        assertEquals(testLawyerId, scheduleResponseDTO.getLawyerInfo().getId());
        assertEquals("John", scheduleResponseDTO.getLawyerInfo().getFirstName());
        assertEquals("Doe", scheduleResponseDTO.getLawyerInfo().getLastName());
        assertEquals("john.doe@example.com", scheduleResponseDTO.getLawyerInfo().getEmail());

        assertEquals(testClientId, scheduleResponseDTO.getClientInfo().getId());
        assertEquals("Jane", scheduleResponseDTO.getClientInfo().getFirstName());
        assertEquals("Smith", scheduleResponseDTO.getClientInfo().getLastName());
        assertEquals("jane.smith@example.com", scheduleResponseDTO.getClientInfo().getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        ScheduleResponseDTO dto1 = new ScheduleResponseDTO();
        dto1.setId(testId);
        dto1.setTitle("Test Title");
        dto1.setType(ScheduleType.MEETING);
        
        ScheduleResponseDTO dto2 = new ScheduleResponseDTO();
        dto2.setId(testId);
        dto2.setTitle("Test Title");
        dto2.setType(ScheduleType.MEETING);
        
        ScheduleResponseDTO dto3 = new ScheduleResponseDTO();
        dto3.setId(UUID.randomUUID());
        dto3.setTitle("Different Title");
        dto3.setType(ScheduleType.COURT_HEARING);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        scheduleResponseDTO.setId(testId);
        scheduleResponseDTO.setTitle("Test Schedule");
        scheduleResponseDTO.setType(ScheduleType.MEETING);

        String toString = scheduleResponseDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Schedule"));
        assertTrue(toString.contains("MEETING"));
        assertTrue(toString.contains(testId.toString()));
    }

    @Test
    void testSettersWithNullValues() {
        scheduleResponseDTO.setId(null);
        scheduleResponseDTO.setCaseInfo(null);
        scheduleResponseDTO.setLawyerInfo(null);
        scheduleResponseDTO.setClientInfo(null);
        scheduleResponseDTO.setTitle(null);
        scheduleResponseDTO.setType(null);
        scheduleResponseDTO.setDescription(null);
        scheduleResponseDTO.setDate(null);
        scheduleResponseDTO.setStartTime(null);
        scheduleResponseDTO.setEndTime(null);
        scheduleResponseDTO.setCreatedAt(null);
        scheduleResponseDTO.setUpdatedAt(null);

        assertNull(scheduleResponseDTO.getId());
        assertNull(scheduleResponseDTO.getCaseInfo());
        assertNull(scheduleResponseDTO.getLawyerInfo());
        assertNull(scheduleResponseDTO.getClientInfo());
        assertNull(scheduleResponseDTO.getTitle());
        assertNull(scheduleResponseDTO.getType());
        assertNull(scheduleResponseDTO.getDescription());
        assertNull(scheduleResponseDTO.getDate());
        assertNull(scheduleResponseDTO.getStartTime());
        assertNull(scheduleResponseDTO.getEndTime());
        assertNull(scheduleResponseDTO.getCreatedAt());
        assertNull(scheduleResponseDTO.getUpdatedAt());
    }

    @Test
    void testAuditDates() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime earlier = now.minusHours(1);

        scheduleResponseDTO.setCreatedAt(earlier);
        scheduleResponseDTO.setUpdatedAt(now);

        assertEquals(earlier, scheduleResponseDTO.getCreatedAt());
        assertEquals(now, scheduleResponseDTO.getUpdatedAt());
        assertTrue(scheduleResponseDTO.getUpdatedAt().isAfter(scheduleResponseDTO.getCreatedAt()));
    }

    @Test
    void testTimeRange() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusHours(3);

        scheduleResponseDTO.setStartTime(start);
        scheduleResponseDTO.setEndTime(end);

        assertEquals(start, scheduleResponseDTO.getStartTime());
        assertEquals(end, scheduleResponseDTO.getEndTime());
        assertTrue(scheduleResponseDTO.getEndTime().isAfter(scheduleResponseDTO.getStartTime()));
    }
} 