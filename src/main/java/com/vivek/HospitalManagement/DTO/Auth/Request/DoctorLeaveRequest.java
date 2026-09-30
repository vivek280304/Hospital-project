package com.vivek.HospitalManagement.DTO.Auth.Request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class DoctorLeaveRequest {

    private Long id;
    private LocalDate leaveDate;
    private String reason;
}
