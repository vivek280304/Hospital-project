package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshToken r WHERE r.token = :token")
    Optional<RefreshToken> findByToken(
            @Param("token") String token
    );

    @Modifying
    @Query("""
        UPDATE RefreshToken r
        SET r.revoked = true
        WHERE r.user.id = :userId
        AND r.revoked = false
        """)
    void revokeAllByUserId(@Param("userId") Long userId);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);


}
