package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule,Long> {

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);
}
