package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient,Long> {

    Optional<Patient> findByUserId(Long userId);

    Optional<Patient> findByUserEmail(String userId);

    List<Patient> findByUserNameContainingIgnoreCaseOrUserEmailContainingIgnoreCaseOrPhoneNumberContaining(
            String name,
            String email,
            String phoneNumber
    );

}