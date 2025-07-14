package com.javajedis.legalconnect.scheduling;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthCalendarTokenRepo extends JpaRepository<OAuthCalendarToken, UUID> {

    Optional<OAuthCalendarToken> findById(UUID id);

    Optional<OAuthCalendarToken> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

} 