package com.vivek.HospitalManagement.Controller;


import com.vivek.HospitalManagement.DTO.Auth.Request.BookAppointmentRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.CreatePatientRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.DoctorLeaveRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.*;
import com.vivek.HospitalManagement.Entity.DoctorSchedule;
import com.vivek.HospitalManagement.Service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ReceptionistProfileResponse> getProfile(Authentication authentication) {

        String email = authentication.getName();
        ReceptionistProfileResponse response = receptionistService.getMyProfile(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/search")
    public ResponseEntity<List<PatientDetailsResponse>> searchPatients(
            @RequestParam String query) {

        return ResponseEntity.ok(
                receptionistService.searchPatients(query)
        );
    }

    @GetMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<List<DoctorSchedule>> getDoctorSchedule(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                receptionistService.getDoctorSchedule(doctorId)
        );
    }

    @GetMapping("/doctors/{doctorId}/available-slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                receptionistService.getAvailableSlots(
                        doctorId,
                        date
                )
        );
    }
    @PostMapping("/create-patient")
    public ResponseEntity<PatientDetailsResponse> createPatient(
            @Valid @RequestBody CreatePatientRequest request) {

        return ResponseEntity.ok(
                receptionistService.createPatient(request)
        );
    }

    @PostMapping("/appointments/{patientId}")
    public ResponseEntity<String> bookAppointment(
            @PathVariable Long patientId,
            @Valid @RequestBody BookAppointmentRequest request) {

        receptionistService.bookAppointmentForPatient(
                patientId,
                request
        );

        return ResponseEntity.ok("Appointment booked successfully");
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<ReceptionistAppointmentResponse>> getAppointments(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                receptionistService.getAppointments(date)
        );
    }


    @PostMapping("/doctors/{doctorId}/leave")
    public ResponseEntity<DoctorLeaveResponse> createDoctorLeave(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorLeaveRequest request
    ) {

        return ResponseEntity.ok(
                receptionistService.createDoctorLeave(
                        doctorId,
                        request
                )
        );
    }


    @GetMapping("/doctors/{doctorId}/leaves")
    public ResponseEntity<List<DoctorLeaveResponse>> getDoctorLeaves(
            @PathVariable Long doctorId
    ) {

        return ResponseEntity.ok(
                receptionistService.getDoctorLeaves(
                        doctorId
                )
        );
    }


    @DeleteMapping("/doctors/{doctorId}/leave")
    public ResponseEntity<String> removeDoctorLeave(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date
    ) {

        receptionistService.removeDoctorLeave(
                doctorId,
                date
        );

        return ResponseEntity.ok(
                "Doctor leave removed successfully"
        );
    }

}
