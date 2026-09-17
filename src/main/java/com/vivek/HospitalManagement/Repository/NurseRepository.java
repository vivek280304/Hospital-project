package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NurseRepository extends JpaRepository<Nurse,Long> {

    Optional<Nurse> findByUserId(Long userId);
}
