package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.DTO.Auth.Response.PatientAppointmentResponse;
import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDateAndStatus(
            LocalDate appointmentDate,
            AppointmentStatus status
    );

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            AppointmentStatus status
    );

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(
            Long doctorId,
            LocalDate appointmentDate,
            AppointmentStatus status
    );

    List<Appointment> findByDoctorIdAndAppointmentDate(
            Long doctorId,
            LocalDate appointmentDate
    );

    boolean existsByDoctorIdAndPatientId(
            Long doctorId,
            Long patientId
    );

    List<Appointment> findByPatientUserEmailOrderByAppointmentDateAscAppointmentTimeAsc(
            String email
    );

    @Query("""
    SELECT new com.vivek.HospitalManagement.DTO.Auth.Response.PatientAppointmentResponse(
        a.id,
        p.id,
        pu.name,
        du.name,
        a.appointmentDate,
        a.appointmentTime,
        a.status
    )
    FROM Appointment a
    JOIN a.patient p
    JOIN p.user pu
    JOIN a.doctor d
    JOIN d.user du
    WHERE pu.email = :email
    ORDER BY a.appointmentDate ASC, a.appointmentTime ASC
""")
    List<PatientAppointmentResponse> findPatientAppointments(
            @Param("email") String email
    );
}