package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class AvailableSlotResponse {

    private LocalTime startTime;
    private LocalTime endTime;
}
