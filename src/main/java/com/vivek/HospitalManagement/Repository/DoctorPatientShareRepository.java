package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.DoctorPatientShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorPatientShareRepository
        extends JpaRepository<DoctorPatientShare, Long> {

    List<DoctorPatientShare> findByJuniorDoctorIdAndRevokedAtIsNull(
            Long juniorDoctorId
    );

    Optional<DoctorPatientShare> findByPatientIdAndJuniorDoctorIdAndRevokedAtIsNull(
            Long patientId,
            Long juniorDoctorId
    );

    List<DoctorPatientShare> findBySeniorDoctorIdAndRevokedAtIsNull(
            Long seniorDoctorId
    );

    boolean existsByPatientIdAndJuniorDoctorIdAndRevokedAtIsNull(
            Long patientId,
            Long juniorDoctorId
    );
}