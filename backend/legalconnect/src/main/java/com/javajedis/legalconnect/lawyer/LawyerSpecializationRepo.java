package com.javajedis.legalconnect.lawyer;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.javajedis.legalconnect.user.User;

@Repository
public interface LawyerSpecializationRepo extends JpaRepository<LawyerSpecialization, UUID> {
    List<LawyerSpecialization> findByLawyerUser(User user);
    List<LawyerSpecialization> findByLawyer(Lawyer lawyer);
    List<LawyerSpecialization> findByLawyerAndSpecializationType(Lawyer lawyer, SpecializationType specializationType);
    boolean existsByLawyerAndSpecializationType(Lawyer lawyer, SpecializationType specializationType);
    List<LawyerSpecialization> findBySpecializationType(SpecializationType specializationType);
} 