package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DoctorWithScheduleResponse {

    private Long doctorId;
    private String name;
    private String specialization;
    private int experience;
    private List<DoctorScheduleResponse> schedules;

}
