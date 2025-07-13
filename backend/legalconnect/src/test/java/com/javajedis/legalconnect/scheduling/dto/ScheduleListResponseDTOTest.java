package com.javajedis.legalconnect.scheduling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.casemanagement.CaseStatus;
import com.javajedis.legalconnect.scheduling.ScheduleType;

class ScheduleListResponseDTOTest {

    private ScheduleListResponseDTO scheduleListResponseDTO;
    private ScheduleResponseDTO testScheduleResponseDTO1;
    private ScheduleResponseDTO testScheduleResponseDTO2;
    private List<ScheduleResponseDTO> testScheduleList;

    @BeforeEach
    void setUp() {
        scheduleListResponseDTO = new ScheduleListResponseDTO();
        
        // Create test ScheduleResponseDTO objects
        testScheduleResponseDTO1 = createTestScheduleResponseDTO(
            UUID.randomUUID(),
            "Test Schedule 1",
            ScheduleType.MEETING,
            "First test schedule"
        );
        
        testScheduleResponseDTO2 = createTestScheduleResponseDTO(
            UUID.randomUUID(),
            "Test Schedule 2", 
            ScheduleType.COURT_HEARING,
            "Second test schedule"
        );
        
        testScheduleList = Arrays.asList(testScheduleResponseDTO1, testScheduleResponseDTO2);
    }

    private ScheduleResponseDTO createTestScheduleResponseDTO(UUID id, String title, ScheduleType type, String description) {
        ScheduleResponseDTO dto = new ScheduleResponseDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setType(type);
        dto.setDescription(description);
        dto.setDate(LocalDate.now());
        dto.setStartTime(OffsetDateTime.now());
        dto.setEndTime(OffsetDateTime.now().plusHours(1));
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setUpdatedAt(OffsetDateTime.now());
        
        // Set nested DTOs
        ScheduleResponseDTO.CaseSummaryDTO caseInfo = new ScheduleResponseDTO.CaseSummaryDTO();
        caseInfo.setId(UUID.randomUUID());
        caseInfo.setTitle("Test Case");
        caseInfo.setStatus(CaseStatus.IN_PROGRESS);
        dto.setCaseInfo(caseInfo);
        
        ScheduleResponseDTO.LawyerSummaryDTO lawyer = new ScheduleResponseDTO.LawyerSummaryDTO();
        lawyer.setId(UUID.randomUUID());
        lawyer.setFirstName("John");
        lawyer.setLastName("Doe");
        lawyer.setEmail("john.doe@example.com");
        dto.setLawyerInfo(lawyer);
        
        ScheduleResponseDTO.ClientSummaryDTO client = new ScheduleResponseDTO.ClientSummaryDTO();
        client.setId(UUID.randomUUID());
        client.setFirstName("Jane");
        client.setLastName("Smith");
        client.setEmail("jane.smith@example.com");
        dto.setClientInfo(client);
        
        return dto;
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(scheduleListResponseDTO);
        assertNull(scheduleListResponseDTO.getSchedules());
    }

    @Test
    void testAllArgsConstructor() {
        ScheduleListResponseDTO dto = new ScheduleListResponseDTO(testScheduleList);

        assertEquals(testScheduleList, dto.getSchedules());
    }

    @Test
    void testSettersAndGetters() {
        scheduleListResponseDTO.setSchedules(testScheduleList);

        assertEquals(testScheduleList, scheduleListResponseDTO.getSchedules());
    }

    @Test
    void testSchedulesListProperty() {
        // Test with empty list
        scheduleListResponseDTO.setSchedules(Collections.emptyList());
        assertNotNull(scheduleListResponseDTO.getSchedules());
        assertTrue(scheduleListResponseDTO.getSchedules().isEmpty());

        // Test with populated list
        scheduleListResponseDTO.setSchedules(testScheduleList);
        assertEquals(2, scheduleListResponseDTO.getSchedules().size());
        assertEquals(testScheduleResponseDTO1, scheduleListResponseDTO.getSchedules().get(0));
        assertEquals(testScheduleResponseDTO2, scheduleListResponseDTO.getSchedules().get(1));
    }

    @Test
    void testSchedulesListContent() {
        scheduleListResponseDTO.setSchedules(testScheduleList);
        
        List<ScheduleResponseDTO> schedules = scheduleListResponseDTO.getSchedules();
        assertEquals(2, schedules.size());
        
        // Verify first schedule
        ScheduleResponseDTO firstSchedule = schedules.get(0);
        assertEquals("Test Schedule 1", firstSchedule.getTitle());
        assertEquals(ScheduleType.MEETING, firstSchedule.getType());
        assertEquals("First test schedule", firstSchedule.getDescription());
        
        // Verify second schedule
        ScheduleResponseDTO secondSchedule = schedules.get(1);
        assertEquals("Test Schedule 2", secondSchedule.getTitle());
        assertEquals(ScheduleType.COURT_HEARING, secondSchedule.getType());
        assertEquals("Second test schedule", secondSchedule.getDescription());
    }

    @Test
    void testSettersWithNullValues() {
        scheduleListResponseDTO.setSchedules(null);

        assertNull(scheduleListResponseDTO.getSchedules());
    }

    @Test
    void testEqualsAndHashCode() {
        ScheduleListResponseDTO dto1 = new ScheduleListResponseDTO(testScheduleList);
        
        ScheduleListResponseDTO dto2 = new ScheduleListResponseDTO(testScheduleList);
        
        ScheduleListResponseDTO dto3 = new ScheduleListResponseDTO(Collections.emptyList());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        scheduleListResponseDTO.setSchedules(testScheduleList);

        String toString = scheduleListResponseDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("schedules"));
    }

    @Test
    void testSingleScheduleInList() {
        List<ScheduleResponseDTO> singleScheduleList = Arrays.asList(testScheduleResponseDTO1);
        scheduleListResponseDTO.setSchedules(singleScheduleList);

        assertEquals(1, scheduleListResponseDTO.getSchedules().size());
        assertEquals(testScheduleResponseDTO1, scheduleListResponseDTO.getSchedules().get(0));
    }

    @Test
    void testEmptySchedulesList() {
        List<ScheduleResponseDTO> emptyList = Collections.emptyList();
        scheduleListResponseDTO.setSchedules(emptyList);

        assertNotNull(scheduleListResponseDTO.getSchedules());
        assertTrue(scheduleListResponseDTO.getSchedules().isEmpty());
        assertEquals(0, scheduleListResponseDTO.getSchedules().size());
    }

    @Test
    void testLargeSchedulesList() {
        // Create a larger list of schedules
        List<ScheduleResponseDTO> largeList = Arrays.asList(
            testScheduleResponseDTO1,
            testScheduleResponseDTO2,
            createTestScheduleResponseDTO(UUID.randomUUID(), "Schedule 3", ScheduleType.ARBITRATION_SESSION, "Third"),
            createTestScheduleResponseDTO(UUID.randomUUID(), "Schedule 4", ScheduleType.DEPOSITION_DATE, "Fourth"),
            createTestScheduleResponseDTO(UUID.randomUUID(), "Schedule 5", ScheduleType.LEGAL_ADVICE_SESSION, "Fifth")
        );

        scheduleListResponseDTO.setSchedules(largeList);

        assertEquals(5, scheduleListResponseDTO.getSchedules().size());
        assertEquals("Schedule 3", scheduleListResponseDTO.getSchedules().get(2).getTitle());
        assertEquals("Schedule 4", scheduleListResponseDTO.getSchedules().get(3).getTitle());
        assertEquals("Schedule 5", scheduleListResponseDTO.getSchedules().get(4).getTitle());
    }

    @Test
    void testScheduleListOrder() {
        // Test that the order of schedules is preserved
        List<ScheduleResponseDTO> orderedList = Arrays.asList(testScheduleResponseDTO2, testScheduleResponseDTO1);
        scheduleListResponseDTO.setSchedules(orderedList);

        assertEquals(2, scheduleListResponseDTO.getSchedules().size());
        // First item should be testScheduleResponseDTO2
        assertEquals(testScheduleResponseDTO2, scheduleListResponseDTO.getSchedules().get(0));
        assertEquals("Test Schedule 2", scheduleListResponseDTO.getSchedules().get(0).getTitle());
        // Second item should be testScheduleResponseDTO1
        assertEquals(testScheduleResponseDTO1, scheduleListResponseDTO.getSchedules().get(1));
        assertEquals("Test Schedule 1", scheduleListResponseDTO.getSchedules().get(1).getTitle());
    }
} 