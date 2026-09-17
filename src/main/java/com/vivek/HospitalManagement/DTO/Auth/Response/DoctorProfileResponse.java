package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DoctorProfileResponse {

    private String name;
    private String email;
    private String licenseNumber;
    private String specialization;
    private Integer experience;

}
