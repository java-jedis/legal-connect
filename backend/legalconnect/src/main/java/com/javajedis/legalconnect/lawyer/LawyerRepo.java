package com.javajedis.legalconnect.lawyer;

import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LawyerRepo extends JpaRepository<Lawyer, UUID> {
    Optional<Lawyer> findByUser(User user);

    Optional<Lawyer> findByUserId(UUID userId);

    Optional<Lawyer> findByBarCertificateNumber(String barCertificateNumber);

    boolean existsByBarCertificateNumber(String barCertificateNumber);

    boolean existsByUser(User user);

    boolean existsByUserId(UUID userId);

    // Pagination method for admin operations
    Page<Lawyer> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    /**
     * Returns raw lawyer search results as Object[] for mapping in the service layer.
     * Fields: lawyerId, userId, firstName, lastName, email, firm, yearsOfExperience, practicingCourt, division, district, bio, specializationCsv, averageRating
     */
    @Query(value = "SELECT l.id, u.id, u.first_name, u.last_name, u.email, l.firm, l.years_of_experience, " +
            "l.practicing_court, l.division, l.district, l.bio, " +
            "(SELECT string_agg(ls.specialization_type, ',') FROM lawyer_specializations ls WHERE ls.lawyer_id = l.id), " +
            "(SELECT AVG(lr.rating) FROM lawyer_reviews lr WHERE lr.lawyer_id = u.id) " +
            "FROM lawyers l JOIN users u ON u.id = l.user_id " +
            "WHERE l.verification_status = 'APPROVED' " +
            "AND (:minExperience IS NULL OR l.years_of_experience >= :minExperience) " +
            "AND (:maxExperience IS NULL OR l.years_of_experience <= :maxExperience) " +
            "AND (:practicingCourt IS NULL OR l.practicing_court = :practicingCourt) " +
            "AND (:division IS NULL OR l.division = :division) " +
            "AND (:district IS NULL OR l.district = :district) " +
            "AND (:specialization IS NULL OR EXISTS (SELECT 1 FROM lawyer_specializations ls WHERE ls.lawyer_id = l.id AND ls.specialization_type = :specialization)) " +
            "ORDER BY l.created_at DESC",
            countQuery = "SELECT COUNT(l.id) FROM lawyers l WHERE l.verification_status = 'APPROVED' " +
                    "AND (:minExperience IS NULL OR l.years_of_experience >= :minExperience) " +
                    "AND (:maxExperience IS NULL OR l.years_of_experience <= :maxExperience) " +
                    "AND (:practicingCourt IS NULL OR l.practicing_court = :practicingCourt) " +
                    "AND (:division IS NULL OR l.division = :division) " +
                    "AND (:district IS NULL OR l.district = :district) " +
                    "AND (:specialization IS NULL OR EXISTS (SELECT 1 FROM lawyer_specializations ls WHERE ls.lawyer_id = l.id AND ls.specialization_type = :specialization))",
            nativeQuery = true)
    Page<Object[]> findVerifiedLawyerRawResultsByCriteria(
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("practicingCourt") String practicingCourt,
            @Param("division") String division,
            @Param("district") String district,
            @Param("specialization") String specialization,
            Pageable pageable);
}