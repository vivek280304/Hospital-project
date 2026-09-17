package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class SharedPatientResponse {

    private Long shareId;
    private Long patientId;
    private String patientName;
    private Long DoctorId;
    private String DoctorName;
    private LocalDateTime sharedAt;


}
