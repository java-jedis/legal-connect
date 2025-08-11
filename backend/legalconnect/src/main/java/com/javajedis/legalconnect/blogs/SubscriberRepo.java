package com.javajedis.legalconnect.blogs;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriberRepo extends JpaRepository<Subscriber, UUID> {
    List<Subscriber> findByAuthorId(UUID authorId);
    List<Subscriber> findBySubscriberId(UUID subscriberId);

    Page<Subscriber> findByAuthorId(UUID authorId, Pageable pageable);
    Page<Subscriber> findBySubscriberId(UUID subscriberId, Pageable pageable);

    Optional<Subscriber> findByAuthorIdAndSubscriberId(UUID authorId, UUID subscriberId);

    void deleteByAuthorIdAndSubscriberId(UUID authorId, UUID subscriberId);
} 