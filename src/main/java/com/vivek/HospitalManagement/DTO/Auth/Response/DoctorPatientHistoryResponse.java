package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DoctorPatientHistoryResponse {

    private DoctorPatientResponse patient;

    private List<AppointmentResponse> appointments;

    private List<MedicalReportResponse> medicalReports;

    private List<PatientLabOrderResponse> labTests;

    private List<PatientLabResultResponse> labResults;

    private List<DiagnosticImageResponse> imaging;
}