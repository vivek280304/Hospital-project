package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.BookAppointmentRequest;
import com.vivek.HospitalManagement.Service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/patient/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<String> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        appointmentService.bookAppointment(email, request);

        return ResponseEntity.ok(
                "Appointment booked successfully"
        );
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date) {

        List<LocalTime> slots =
                appointmentService.getAvailableSlots(
                        doctorId,
                        date
                );

        return ResponseEntity.ok(slots);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {

        appointmentService.cancelAppointment(
                appointmentId,
                authentication.getName()
        );

        return ResponseEntity.ok("Appointment cancelled successfully");
    }


}
