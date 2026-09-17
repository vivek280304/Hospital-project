package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class MedicalReportResponse {


    private Long id;

    private Long appointmentId;

    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String prescription;
    private String notes;

    private LocalDateTime createdAt;

    public MedicalReportResponse(
            Long id,
            Long appointmentId,
            Long patientId,
            Long doctorId,
            String diagnosis,
            String symptoms,
            String treatment,
            String prescription,
            String notes,
            LocalDateTime createdAt) {

        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.symptoms = symptoms;
        this.treatment = treatment;
        this.prescription = prescription;
        this.notes = notes;
        this.createdAt = createdAt;
    }

}
