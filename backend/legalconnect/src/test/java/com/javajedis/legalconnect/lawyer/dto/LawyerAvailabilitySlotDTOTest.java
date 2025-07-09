package com.javajedis.legalconnect.lawyer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.javajedis.legalconnect.lawyer.enums.DayOfWeek;

class LawyerAvailabilitySlotDTOTest {
    @Test
    void testDefaultConstructorAndSettersGetters() {
        LawyerAvailabilitySlotDTO dto = new LawyerAvailabilitySlotDTO();
        dto.setDay(DayOfWeek.MON);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(17, 0));

        assertEquals(DayOfWeek.MON, dto.getDay());
        assertEquals(LocalTime.of(9, 0), dto.getStartTime());
        assertEquals(LocalTime.of(17, 0), dto.getEndTime());
    }

    @Test
    void testEqualsAndHashCode() {
        LawyerAvailabilitySlotDTO dto1 = new LawyerAvailabilitySlotDTO();
        dto1.setDay(DayOfWeek.TUE);
        dto1.setStartTime(LocalTime.of(10, 0));
        dto1.setEndTime(LocalTime.of(18, 0));

        LawyerAvailabilitySlotDTO dto2 = new LawyerAvailabilitySlotDTO();
        dto2.setDay(DayOfWeek.TUE);
        dto2.setStartTime(LocalTime.of(10, 0));
        dto2.setEndTime(LocalTime.of(18, 0));

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LawyerAvailabilitySlotDTO dto = new LawyerAvailabilitySlotDTO();
        dto.setDay(DayOfWeek.FRI);
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(12, 0));
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("FRI"));
    }
} 