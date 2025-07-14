package com.javajedis.legalconnect.scheduling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.scheduling.ScheduleType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ScheduleResponseDTOTest {

    private Validator validator;
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
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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
    void testAllArgsConstructor() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO(
            testCaseId, "Test Case", CaseStatus.IN_PROGRESS
        );
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO(
            testLawyerId, "John", "Doe", "john.doe@example.com"
        );
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO(
            testClientId, "Jane", "Smith", "jane.smith@example.com"
        );

        ScheduleResponseDTO dto = new ScheduleResponseDTO(
            testId, caseInfo, lawyerInfo, clientInfo, "Test Meeting", ScheduleType.MEETING,
            "Test description", testDate, testStartTime, testEndTime, testCreatedAt, testUpdatedAt
        );

        assertEquals(testId, dto.getId());
        assertEquals(caseInfo, dto.getCaseInfo());
        assertEquals(lawyerInfo, dto.getLawyerInfo());
        assertEquals(clientInfo, dto.getClientInfo());
        assertEquals("Test Meeting", dto.getTitle());
        assertEquals(ScheduleType.MEETING, dto.getType());
        assertEquals("Test description", dto.getDescription());
        assertEquals(testDate, dto.getDate());
        assertEquals(testStartTime, dto.getStartTime());
        assertEquals(testEndTime, dto.getEndTime());
        assertEquals(testCreatedAt, dto.getCreatedAt());
        assertEquals(testUpdatedAt, dto.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO(
            testCaseId, "Test Case", CaseStatus.IN_PROGRESS
        );
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO(
            testLawyerId, "John", "Doe", "john.doe@example.com"
        );
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO(
            testClientId, "Jane", "Smith", "jane.smith@example.com"
        );

        scheduleResponseDTO.setId(testId);
        scheduleResponseDTO.setCaseInfo(caseInfo);
        scheduleResponseDTO.setLawyerInfo(lawyerInfo);
        scheduleResponseDTO.setClientInfo(clientInfo);
        scheduleResponseDTO.setTitle("Test Meeting");
        scheduleResponseDTO.setType(ScheduleType.MEETING);
        scheduleResponseDTO.setDescription("Test description");
        scheduleResponseDTO.setDate(testDate);
        scheduleResponseDTO.setStartTime(testStartTime);
        scheduleResponseDTO.setEndTime(testEndTime);
        scheduleResponseDTO.setCreatedAt(testCreatedAt);
        scheduleResponseDTO.setUpdatedAt(testUpdatedAt);

        assertEquals(testId, scheduleResponseDTO.getId());
        assertEquals(caseInfo, scheduleResponseDTO.getCaseInfo());
        assertEquals(lawyerInfo, scheduleResponseDTO.getLawyerInfo());
        assertEquals(clientInfo, scheduleResponseDTO.getClientInfo());
        assertEquals("Test Meeting", scheduleResponseDTO.getTitle());
        assertEquals(ScheduleType.MEETING, scheduleResponseDTO.getType());
        assertEquals("Test description", scheduleResponseDTO.getDescription());
        assertEquals(testDate, scheduleResponseDTO.getDate());
        assertEquals(testStartTime, scheduleResponseDTO.getStartTime());
        assertEquals(testEndTime, scheduleResponseDTO.getEndTime());
        assertEquals(testCreatedAt, scheduleResponseDTO.getCreatedAt());
        assertEquals(testUpdatedAt, scheduleResponseDTO.getUpdatedAt());
    }

    @Test
    void testValidScheduleResponseDTO() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO(
            testCaseId, "Test Case", CaseStatus.IN_PROGRESS
        );
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO(
            testLawyerId, "John", "Doe", "john.doe@example.com"
        );
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO(
            testClientId, "Jane", "Smith", "jane.smith@example.com"
        );

        scheduleResponseDTO.setId(testId);
        scheduleResponseDTO.setCaseInfo(caseInfo);
        scheduleResponseDTO.setLawyerInfo(lawyerInfo);
        scheduleResponseDTO.setClientInfo(clientInfo);
        scheduleResponseDTO.setTitle("Valid Meeting");
        scheduleResponseDTO.setType(ScheduleType.MEETING);
        scheduleResponseDTO.setDescription("Valid description");
        scheduleResponseDTO.setDate(testDate);
        scheduleResponseDTO.setStartTime(testStartTime);
        scheduleResponseDTO.setEndTime(testEndTime);
        scheduleResponseDTO.setCreatedAt(testCreatedAt);
        scheduleResponseDTO.setUpdatedAt(testUpdatedAt);

        Set<ConstraintViolation<ScheduleResponseDTO>> violations = validator.validate(scheduleResponseDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO(
            testCaseId, "Test Case", CaseStatus.IN_PROGRESS
        );
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO(
            testLawyerId, "John", "Doe", "john.doe@example.com"
        );
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO(
            testClientId, "Jane", "Smith", "jane.smith@example.com"
        );

        ScheduleResponseDTO dto1 = new ScheduleResponseDTO(
            testId, caseInfo, lawyerInfo, clientInfo, "Test Meeting", ScheduleType.MEETING,
            "Test description", testDate, testStartTime, testEndTime, testCreatedAt, testUpdatedAt
        );
        ScheduleResponseDTO dto2 = new ScheduleResponseDTO(
            testId, caseInfo, lawyerInfo, clientInfo, "Test Meeting", ScheduleType.MEETING,
            "Test description", testDate, testStartTime, testEndTime, testCreatedAt, testUpdatedAt
        );
        ScheduleResponseDTO dto3 = new ScheduleResponseDTO(
            UUID.randomUUID(), caseInfo, lawyerInfo, clientInfo, "Test Meeting", ScheduleType.MEETING,
            "Test description", testDate, testStartTime, testEndTime, testCreatedAt, testUpdatedAt
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        scheduleResponseDTO.setId(testId);
        scheduleResponseDTO.setTitle("Test Meeting");
        scheduleResponseDTO.setType(ScheduleType.MEETING);
        scheduleResponseDTO.setDescription("Test description");

        String toString = scheduleResponseDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Meeting"));
        assertTrue(toString.contains("MEETING"));
    }

    // Tests for CaseSummaryDTO
    @Test
    void testCaseSummaryDTO() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO();
        
        // Test default constructor
        assertNotNull(caseInfo);
        assertNull(caseInfo.getId());
        assertNull(caseInfo.getTitle());
        assertNull(caseInfo.getStatus());
        
        // Test setters and getters
        caseInfo.setId(testCaseId);
        caseInfo.setTitle("Test Case");
        caseInfo.setStatus(CaseStatus.IN_PROGRESS);
        
        assertEquals(testCaseId, caseInfo.getId());
        assertEquals("Test Case", caseInfo.getTitle());
        assertEquals(CaseStatus.IN_PROGRESS, caseInfo.getStatus());
        
        // Test all args constructor
        ScheduleResponseDTO.CaseSummaryDTO caseInfo2 = new ScheduleResponseDTO.CaseSummaryDTO(
            testCaseId, "Test Case", CaseStatus.IN_PROGRESS
        );
        
        assertEquals(testCaseId, caseInfo2.getId());
        assertEquals("Test Case", caseInfo2.getTitle());
        assertEquals(CaseStatus.IN_PROGRESS, caseInfo2.getStatus());
        
        // Test equals and hashCode
        assertEquals(caseInfo, caseInfo2);
        assertEquals(caseInfo.hashCode(), caseInfo2.hashCode());
    }

    // Tests for LawyerSummaryDTO
    @Test
    void testLawyerSummaryDTO() {
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo = new ScheduleResponseDTO.LawyerSummaryDTO();
        
        // Test default constructor
        assertNotNull(lawyerInfo);
        assertNull(lawyerInfo.getId());
        assertNull(lawyerInfo.getFirstName());
        assertNull(lawyerInfo.getLastName());
        assertNull(lawyerInfo.getEmail());
        
        // Test setters and getters
        lawyerInfo.setId(testLawyerId);
        lawyerInfo.setFirstName("John");
        lawyerInfo.setLastName("Doe");
        lawyerInfo.setEmail("john.doe@example.com");
        
        assertEquals(testLawyerId, lawyerInfo.getId());
        assertEquals("John", lawyerInfo.getFirstName());
        assertEquals("Doe", lawyerInfo.getLastName());
        assertEquals("john.doe@example.com", lawyerInfo.getEmail());
        
        // Test all args constructor
        ScheduleResponseDTO.LawyerSummaryDTO lawyerInfo2 = new ScheduleResponseDTO.LawyerSummaryDTO(
            testLawyerId, "John", "Doe", "john.doe@example.com"
        );
        
        assertEquals(testLawyerId, lawyerInfo2.getId());
        assertEquals("John", lawyerInfo2.getFirstName());
        assertEquals("Doe", lawyerInfo2.getLastName());
        assertEquals("john.doe@example.com", lawyerInfo2.getEmail());
        
        // Test equals and hashCode
        assertEquals(lawyerInfo, lawyerInfo2);
        assertEquals(lawyerInfo.hashCode(), lawyerInfo2.hashCode());
    }

    // Tests for ClientSummaryDTO
    @Test
    void testClientSummaryDTO() {
        ScheduleResponseDTO.ClientSummaryDTO clientInfo = new ScheduleResponseDTO.ClientSummaryDTO();
        
        // Test default constructor
        assertNotNull(clientInfo);
        assertNull(clientInfo.getId());
        assertNull(clientInfo.getFirstName());
        assertNull(clientInfo.getLastName());
        assertNull(clientInfo.getEmail());
        
        // Test setters and getters
        clientInfo.setId(testClientId);
        clientInfo.setFirstName("Jane");
        clientInfo.setLastName("Smith");
        clientInfo.setEmail("jane.smith@example.com");
        
        assertEquals(testClientId, clientInfo.getId());
        assertEquals("Jane", clientInfo.getFirstName());
        assertEquals("Smith", clientInfo.getLastName());
        assertEquals("jane.smith@example.com", clientInfo.getEmail());
        
        // Test all args constructor
        ScheduleResponseDTO.ClientSummaryDTO clientInfo2 = new ScheduleResponseDTO.ClientSummaryDTO(
            testClientId, "Jane", "Smith", "jane.smith@example.com"
        );
        
        assertEquals(testClientId, clientInfo2.getId());
        assertEquals("Jane", clientInfo2.getFirstName());
        assertEquals("Smith", clientInfo2.getLastName());
        assertEquals("jane.smith@example.com", clientInfo2.getEmail());
        
        // Test equals and hashCode
        assertEquals(clientInfo, clientInfo2);
        assertEquals(clientInfo.hashCode(), clientInfo2.hashCode());
    }

    @Test
    void testAllScheduleTypes() {
        scheduleResponseDTO.setType(ScheduleType.MEETING);
        assertEquals(ScheduleType.MEETING, scheduleResponseDTO.getType());
        
        scheduleResponseDTO.setType(ScheduleType.COURT_HEARING);
        assertEquals(ScheduleType.COURT_HEARING, scheduleResponseDTO.getType());
        
        scheduleResponseDTO.setType(ScheduleType.DEPOSITION_DATE);
        assertEquals(ScheduleType.DEPOSITION_DATE, scheduleResponseDTO.getType());
        
        scheduleResponseDTO.setType(ScheduleType.ARBITRATION_SESSION);
        assertEquals(ScheduleType.ARBITRATION_SESSION, scheduleResponseDTO.getType());
        
        scheduleResponseDTO.setType(ScheduleType.LEGAL_ADVICE_SESSION);
        assertEquals(ScheduleType.LEGAL_ADVICE_SESSION, scheduleResponseDTO.getType());
    }

    @Test
    void testAllCaseStatuses() {
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO();
        
        caseInfo.setStatus(CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, caseInfo.getStatus());
        
        caseInfo.setStatus(CaseStatus.RESOLVED);
        assertEquals(CaseStatus.RESOLVED, caseInfo.getStatus());
    }

    @Test
    void testNullNestedObjects() {
        scheduleResponseDTO.setCaseInfo(null);
        scheduleResponseDTO.setLawyerInfo(null);
        scheduleResponseDTO.setClientInfo(null);
        
        assertNull(scheduleResponseDTO.getCaseInfo());
        assertNull(scheduleResponseDTO.getLawyerInfo());
        assertNull(scheduleResponseDTO.getClientInfo());
    }

    @Test
    void testTimestampHandling() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime later = now.plusHours(1);
        
        scheduleResponseDTO.setStartTime(now);
        scheduleResponseDTO.setEndTime(later);
        scheduleResponseDTO.setCreatedAt(now.minusDays(1));
        scheduleResponseDTO.setUpdatedAt(now);
        
        assertEquals(now, scheduleResponseDTO.getStartTime());
        assertEquals(later, scheduleResponseDTO.getEndTime());
        assertEquals(now.minusDays(1), scheduleResponseDTO.getCreatedAt());
        assertEquals(now, scheduleResponseDTO.getUpdatedAt());
        
        assertTrue(scheduleResponseDTO.getEndTime().isAfter(scheduleResponseDTO.getStartTime()));
        assertTrue(scheduleResponseDTO.getUpdatedAt().isAfter(scheduleResponseDTO.getCreatedAt()));
    }
} 