package com.javajedis.legalconnect.scheduling;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleGoogleCalendarEventRepo extends JpaRepository<ScheduleGoogleCalendarEvent, UUID> {
    
    Optional<ScheduleGoogleCalendarEvent> findByScheduleId(UUID scheduleId);
    
    @Query("SELECT sgce.googleCalendarEventId FROM ScheduleGoogleCalendarEvent sgce WHERE sgce.schedule.id = :scheduleId")
    Optional<String> findGoogleCalendarEventIdByScheduleId(@Param("scheduleId") UUID scheduleId);
    
    void deleteByScheduleId(UUID scheduleId);
} 