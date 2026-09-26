package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorWithScheduleResponse;
import com.vivek.HospitalManagement.Service.AppointmentService;
import com.vivek.HospitalManagement.Service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class MainController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public MainController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> findDoctors(
            @RequestParam String specialization) {

        return ResponseEntity.ok(
                doctorService.findDoctorsBySpecialization(
                        specialization
                )
        );
    }

    @GetMapping("/{doctorId}/available-slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                appointmentService.getAvailableSlots(
                        doctorId,
                        date
                )
        );
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponse> getDoctor(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                doctorService.getDoctorById(doctorId)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<DoctorWithScheduleResponse>> getAllDoctors() {

        return ResponseEntity.ok(
                doctorService.getAllDoctorsWithSchedules()
        );
    }
}
