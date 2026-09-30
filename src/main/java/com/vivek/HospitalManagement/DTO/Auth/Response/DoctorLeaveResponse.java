package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DoctorLeaveResponse {

    private Long saveId;
    private Long doctorId;
    private String name;
    private LocalDate leave;
    private String reason;
}
