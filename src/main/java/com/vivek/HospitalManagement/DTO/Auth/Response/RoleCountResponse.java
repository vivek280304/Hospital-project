package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleCountResponse {

    private long doctors;
    private long nurses;
    private long receptionists;
    private long labTechnicians;
}