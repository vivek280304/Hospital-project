package com.vivek.HospitalManagement.DTO.Auth.Response;

import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class PatientAppointmentResponse {

    private Long appointmentId;

    private Long patientId;
    private String patientName;

    private String doctorName;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private AppointmentStatus status;




}
