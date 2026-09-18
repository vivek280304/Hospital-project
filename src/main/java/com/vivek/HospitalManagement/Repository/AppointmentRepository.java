package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}