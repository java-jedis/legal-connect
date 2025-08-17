package com.javajedis.legalconnect.lawyer.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DayOfWeekTest {
    @Test
    void testAllDaysExist() {
        DayOfWeek[] days = DayOfWeek.values();
        assertEquals(7, days.length);
        for (DayOfWeek day : days) {
            assertNotNull(day);
        }
    }

    @Test
    void testValueOf() {
        assertEquals(DayOfWeek.SAT, DayOfWeek.valueOf("SAT"));
        assertEquals(DayOfWeek.SUN, DayOfWeek.valueOf("SUN"));
        assertEquals(DayOfWeek.MON, DayOfWeek.valueOf("MON"));
        assertEquals(DayOfWeek.TUE, DayOfWeek.valueOf("TUE"));
        assertEquals(DayOfWeek.WED, DayOfWeek.valueOf("WED"));
        assertEquals(DayOfWeek.THU, DayOfWeek.valueOf("THU"));
        assertEquals(DayOfWeek.FRI, DayOfWeek.valueOf("FRI"));
    }

    @Test
    void testToString() {
        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(day.name(), day.toString());
        }
    }
} 