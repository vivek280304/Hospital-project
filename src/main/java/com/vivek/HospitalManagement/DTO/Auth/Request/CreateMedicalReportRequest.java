package com.vivek.HospitalManagement.DTO.Auth.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateMedicalReportRequest {

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String symptoms;

    private String treatment;

    private String prescription;

    private String notes;

}
