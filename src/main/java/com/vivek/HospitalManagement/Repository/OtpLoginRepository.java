package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.OtpLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpLoginRepository extends JpaRepository<OtpLogin,Long> {


    Optional<OtpLogin>
    findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);

}
