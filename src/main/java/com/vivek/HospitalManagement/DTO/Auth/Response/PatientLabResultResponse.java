package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PatientLabResultResponse {

    private Long orderId;
    private String testName;
    private String sampleType;
    private String result;
    private String remarks;
    private LocalDateTime completedAt;
}