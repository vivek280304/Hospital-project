package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave,Long> {

    boolean existsByDoctorIdAndLeaveDate(
            Long doctorId,
            LocalDate leaveDate
    );

    Optional<DoctorLeave> findByDoctorIdAndLeaveDate(
            Long doctorId,
            LocalDate leaveDate
    );

    List<DoctorLeave> findByDoctorIdOrderByLeaveDateAsc(
            Long doctorId
    );

}
