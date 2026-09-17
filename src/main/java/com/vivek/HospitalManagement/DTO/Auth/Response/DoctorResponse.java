package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DoctorResponse {

    private Long doctorId;
    private String name;
    private String specialization;
    private Integer experience;
}
