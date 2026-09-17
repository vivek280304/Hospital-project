package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.MedicalReport;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MedicalReportRepository extends JpaRepository<MedicalReport,Long> {



    Optional<MedicalReport> findByAppointmentId(Long appointmentId);

    List<MedicalReport> findByPatientId(Long patientId);

    List<MedicalReport> findByDoctorId(Long doctorId);



}
