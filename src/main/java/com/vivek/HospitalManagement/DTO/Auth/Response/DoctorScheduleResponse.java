package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class DoctorScheduleResponse {

    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotDuration;
}
