package com.javajedis.legalconnect.lawyer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class LawyerAvailabilitySlotListResponseDTOTest {
    @Test
    void testDefaultConstructorAndSettersGetters() {
        LawyerAvailabilitySlotListResponseDTO dto = new LawyerAvailabilitySlotListResponseDTO();
        LawyerAvailabilitySlotResponseDTO slot1 = new LawyerAvailabilitySlotResponseDTO();
        LawyerAvailabilitySlotResponseDTO slot2 = new LawyerAvailabilitySlotResponseDTO();
        List<LawyerAvailabilitySlotResponseDTO> slots = Arrays.asList(slot1, slot2);
        dto.setSlots(slots);
        assertEquals(slots, dto.getSlots());
    }

    @Test
    void testEqualsAndHashCode() {
        LawyerAvailabilitySlotListResponseDTO dto1 = new LawyerAvailabilitySlotListResponseDTO();
        LawyerAvailabilitySlotListResponseDTO dto2 = new LawyerAvailabilitySlotListResponseDTO();
        LawyerAvailabilitySlotResponseDTO slot = new LawyerAvailabilitySlotResponseDTO();
        List<LawyerAvailabilitySlotResponseDTO> slots = Arrays.asList(slot);
        dto1.setSlots(slots);
        dto2.setSlots(slots);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        LawyerAvailabilitySlotListResponseDTO dto = new LawyerAvailabilitySlotListResponseDTO();
        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("slots"));
    }
} 