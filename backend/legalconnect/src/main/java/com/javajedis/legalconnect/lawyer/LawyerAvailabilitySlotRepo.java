package com.javajedis.legalconnect.lawyer;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LawyerAvailabilitySlotRepo extends JpaRepository<LawyerAvailabilitySlot, UUID> {
    List<LawyerAvailabilitySlot> findByLawyerId(UUID lawyerId);
} 