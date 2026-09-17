package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.RegistrationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp,Long> {

    Optional<RegistrationOtp>
    findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("""
            UPDATE RegistrationOtp r
            SET r.used = true
            WHERE r.user.id = :userId
            AND r.used = false
            """)
    void invalidatePreviousOtps(@Param("userId") Long userId);

}
