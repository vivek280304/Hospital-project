package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp,Long> {

    Optional<PasswordResetOtp> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
            Long userId
    );

    @Modifying
    @Query("""
        UPDATE PasswordResetOtp p
        SET p.used = true
        WHERE p.user.id = :userId
        AND p.used = false
        """)
    void invalidatePreviousOtps(@Param("userId") Long userId);
}
