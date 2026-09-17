package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.DoctorScheduleRequest;
import com.vivek.HospitalManagement.Service.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/doctor-schedules")
public class AdminDoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    public AdminDoctorScheduleController(
            DoctorScheduleService doctorScheduleService) {
        this.doctorScheduleService = doctorScheduleService;
    }

    @PostMapping
    public ResponseEntity<String> createSchedule(
            @Valid @RequestBody DoctorScheduleRequest request) {

        doctorScheduleService.createSchedule(request);

        return ResponseEntity.ok(
                "Doctor schedule created successfully"
        );
    }
}