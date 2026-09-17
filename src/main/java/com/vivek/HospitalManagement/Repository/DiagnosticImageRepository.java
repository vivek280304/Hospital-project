package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.DiagnosticImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticImageRepository extends JpaRepository<DiagnosticImage,Long> {


    List<DiagnosticImage> findByPatientId(Long patientId);

    List<DiagnosticImage> findByAppointmentId(Long appointmentId);

    List<DiagnosticImage> findByPatientIdAndAppointmentDoctorId(
            Long patientId,
            Long doctorId
    );

}
