package com.javajedis.legalconnect.videocall;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.javajedis.legalconnect.user.User;

@Repository
public interface MeetingRepo extends JpaRepository<Meeting, UUID> {
    
    /**
     * Find all meetings where the user is either the client or the lawyer.
     */
    @Query("SELECT m FROM Meeting m WHERE m.client = :user OR m.lawyer = :user")
    List<Meeting> findByClientOrLawyer(@Param("user") User user);
}
