package com.javajedis.legalconnect.scheduling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepo extends JpaRepository<Schedule, UUID> {

    Optional<Schedule> findById(UUID id);

    List<Schedule> findByLawyerId(UUID lawyerId);

    List<Schedule> findByClientId(UUID clientId);

    List<Schedule> findByCaseEntityId(UUID caseId);

    List<Schedule> findByLawyerIdAndClientId(UUID lawyerId, UUID clientId);

    // Pageable methods
    Page<Schedule> findByCaseEntityId(UUID caseId, Pageable pageable);

    @Query("SELECT s FROM Schedule s WHERE s.lawyer.id = :userId OR s.client.id = :userId")
    Page<Schedule> findByLawyerIdOrClientId(@Param("userId") UUID userId, Pageable pageable);

} 