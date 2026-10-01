package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.LabTechnician;
import com.vivek.HospitalManagement.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabTechnicianRepository extends JpaRepository<LabTechnician,Long> {

    Optional<LabTechnician> findByUserId(Long userId);
}
