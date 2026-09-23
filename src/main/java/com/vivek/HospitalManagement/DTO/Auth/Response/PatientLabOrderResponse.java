package com.vivek.HospitalManagement.DTO.Auth.Response;

import com.vivek.HospitalManagement.Enums.LabTestOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class PatientLabOrderResponse {

    private Long orderId;
    private String testName;
    private String sampleType;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private LabTestOrderStatus status;
}
