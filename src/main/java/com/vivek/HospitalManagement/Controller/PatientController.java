package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Response.*;
import com.vivek.HospitalManagement.Entity.DiagnosticImage;
import com.vivek.HospitalManagement.Service.*;
import com.vivek.HospitalManagement.Service.Reports.DiagnosticImageService;
import com.vivek.HospitalManagement.Service.Reports.MedicalReportService;
import com.vivek.HospitalManagement.Service.Reports.MinioStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.DayOfWeek;
import java.util.List;


@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MedicalReportService medicalReportService;
    private final DiagnosticImageService diagnosticImageService;
    private final MinioStorageService minioStorageService;

    public PatientController(PatientService patientService,
                             DoctorService doctorService,
                             MedicalReportService medicalReportService,
                             DiagnosticImageService diagnosticImageService,
                             MinioStorageService minioStorageService) {

        this.patientService = patientService;
        this.doctorService = doctorService;
        this.medicalReportService = medicalReportService;
        this.diagnosticImageService = diagnosticImageService;
        this.minioStorageService = minioStorageService;
    }


    @GetMapping("/profile")
    public ResponseEntity<PatientProfileResponse> getProfile(Authentication authentication) {

        String email = authentication.getName();
        PatientProfileResponse response = patientService.getMyProfile(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> findDoctors(
            @RequestParam DayOfWeek day,
            @RequestParam(required = false) String specialization) {

        return ResponseEntity.ok(
                doctorService.findAvailableDoctors(
                        day,
                        specialization
                )
        );
    }

    @GetMapping("/reports")
    public ResponseEntity<List<MedicalReportResponse>> getMyReports(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                medicalReportService.getMyReports(email)
        );
    }

    @GetMapping("/imaging")
    public ResponseEntity<List<DiagnosticImageResponse>> getImage(Authentication authentication){

        return ResponseEntity.ok(
                diagnosticImageService.getMyImage(authentication.getName()));
    }

    @GetMapping("/imaging/{imageId}/view")
    public ResponseEntity<InputStreamResource> viewImage(@PathVariable("imageId")
                                                         Long id, Authentication authentication){

        DiagnosticImage image = diagnosticImageService.getImageForPatient(id, authentication.getName());
        InputStream inputStream = minioStorageService.getFile(image.getStorageKey());

        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        return ResponseEntity.ok().
                contentType(MediaType.parseMediaType(image.getContentType()))
                .contentLength(image.getFileSize()).body(inputStreamResource);

    }
    @GetMapping("/get-all-appointments")
    public ResponseEntity<List<PatientAppointmentResponse>> getPatientAppointments
            (Authentication authentication){

        return ResponseEntity.ok(
                patientService.getAppointments(authentication.getName()
                ));

    }
}
