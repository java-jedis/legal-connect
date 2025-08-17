package com.javajedis.legalconnect.lawyer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;

class LawyerAvailabilitySlotResponseDTOTest {
    @Test
    void testDefaultConstructorAndSettersGetters() {
        LawyerAvailabilitySlotResponseDTO dto = new LawyerAvailabilitySlotResponseDTO();
        UUID id = UUID.randomUUID();
        dto.setId(id);
        dto.setDay(DayOfWeek.SUN);
        dto.setStartTime(LocalTime.of(7, 0));
        dto.setEndTime(LocalTime.of(15, 0));

        assertEquals(id, dto.getId());
        assertEquals(DayOfWeek.SUN, dto.getDay());
        assertEquals(LocalTime.of(7, 0), dto.getStartTime());
        assertEquals(LocalTime.of(15, 0), dto.getEndTime());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        LawyerAvailabilitySlotResponseDTO dto1 = new LawyerAvailabilitySlotResponseDTO();
        dto1.setId(id);
        dto1.setDay(DayOfWeek.SAT);
        dto1.setStartTime(LocalTime.of(6, 0));
        dto1.setEndTime(LocalTime.of(14, 0));

        LawyerAvailabilitySlotResponseDTO dto2 = new LawyerAvailabilitySlotResponseDTO();
        dto2.setId(id);
        dto2.setDay(DayOfWeek.SAT);
        dto2.setStartTime(LocalTime.of(6, 0));
        dto2.setEndTime(LocalTime.of(14, 0));

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LawyerAvailabilitySlotResponseDTO dto = new LawyerAvailabilitySlotResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setDay(DayOfWeek.THU);
        dto.setStartTime(LocalTime.of(13, 0));
        dto.setEndTime(LocalTime.of(19, 0));
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("THU"));
    }
} 