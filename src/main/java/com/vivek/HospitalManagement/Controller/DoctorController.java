package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.CreateMedicalReportRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.ImagingOrderRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.SharePatientRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.*;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Service.*;
import com.vivek.HospitalManagement.Service.Reports.DiagnosticImageService;
import com.vivek.HospitalManagement.Service.Reports.ImagingOrderService;
import com.vivek.HospitalManagement.Service.Reports.MedicalReportService;
import com.vivek.HospitalManagement.Service.Reports.MinioStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final ImagingOrderService imagingOrderService;
    private final DiagnosticImageService diagnosticImageService;
    private final MinioStorageService minioStorageService;
    private final DoctorPatientShareService doctorPatientShareService;
    private final MedicalReportService medicalReportService;

    public DoctorController(DoctorService doctorService,
                            AppointmentService appointmentService,
                            ImagingOrderService imagingOrderService,
                            DiagnosticImageService diagnosticImageService,
                            MinioStorageService minioStorageService,
                            DoctorPatientShareService doctorPatientShareService,
                            MedicalReportService medicalReportService) {


        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.imagingOrderService = imagingOrderService;
        this.diagnosticImageService = diagnosticImageService;
        this.minioStorageService = minioStorageService;
        this.doctorPatientShareService = doctorPatientShareService;
        this.medicalReportService = medicalReportService;
    }

    @GetMapping("/profile")
    public ResponseEntity<DoctorProfileResponse> getProfile(Authentication authentication) {

        String email = authentication.getName();
        DoctorProfileResponse response = doctorService.getMyProfile(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @RequestParam LocalDate date,
            Authentication authentication) {

        String email = authentication.getName();

        List<AppointmentResponse> appointments =
                appointmentService.getDoctorAppointments(
                        email,
                        date
                );

        return ResponseEntity.ok(appointments);
    }

    @PatchMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<String> completeAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {

        doctorService.completeAppointment(
                appointmentId,
                authentication.getName()
        );

        return ResponseEntity.ok("Appointment completed successfully");
    }

    @GetMapping("/appointments/{appointmentId}/patient")
    public ResponseEntity<PatientDetailsResponse> getPatientDetails(
            @PathVariable Long appointmentId,
            Authentication authentication) {

        String email = authentication.getName();

        PatientDetailsResponse response =
                appointmentService.getPatientDetails(
                        email,
                        appointmentId
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/appointments/{appointmentId}/report")
    public ResponseEntity<MedicalReportResponse> createMedicalReport(
            @PathVariable Long appointmentId,
            @Valid @RequestBody CreateMedicalReportRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        MedicalReportResponse response =
                appointmentService.createMedicalReport(
                        email,
                        appointmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/patients/{patientId}/reports")
    public ResponseEntity<List<MedicalReportResponse>> getPatientReports(
            @PathVariable Long patientId,
            Authentication authentication) {


        List<MedicalReportResponse> reports =
                medicalReportService.getPatientReportsForDoctor(
                        authentication.getName(),
                        patientId
                );

        return ResponseEntity.ok(reports);
    }

    @PostMapping("/create-Report-order")
    public ResponseEntity<String> createImageOrder(
            @Valid @RequestBody ImagingOrderRequest request,
            Authentication authentication){

        ImagingOrder order = imagingOrderService.createOrder(request,authentication.getName());

        return ResponseEntity.ok("Imaging order created successfully. ID: " + order.getId());
    }

    @GetMapping("/imaging/{imageId}/view")
    public ResponseEntity<InputStreamResource> viewImage( @PathVariable("imageId")
                                                              Long id,Authentication authentication){

        DiagnosticImage image = diagnosticImageService.getImageForDoctor(id,authentication.getName());

        InputStream inputStream = minioStorageService.getFile(image.getStorageKey());

        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        return ResponseEntity.ok().
                contentType(MediaType.parseMediaType(image.getContentType()))
                .contentLength(image.getFileSize()).body(inputStreamResource);

    }

    @GetMapping("/imaging-orders")
    public ResponseEntity<List<ImagingOrderResponse>> getOrders(Authentication authentication){

        return ResponseEntity.ok(
                imagingOrderService.getPendingOrdersForDoctor(authentication.getName()
                ));
    }

    @PostMapping("/patient/{patientId}/share")
    public ResponseEntity<String> share(@PathVariable Long patientId,
                                        @Valid @RequestBody SharePatientRequest request,
                                        Authentication authentication){


        doctorPatientShareService.share(patientId,request,authentication.getName());

        return ResponseEntity.ok("Patient reports shared successfully");
    }

    @GetMapping("/shared-patients")
    public ResponseEntity<List<SharedPatientResponse>> getSharedPatient(Authentication authentication){

        return ResponseEntity.ok(
                doctorPatientShareService.getSharedPatients(authentication.getName()
                ));
    }

    @GetMapping("/shared-patients/{patientId}/reports")
    public ResponseEntity<List<MedicalReportResponse>> getSharedPatientReports(
            @PathVariable Long patientId,
            Authentication authentication) {

        return ResponseEntity.ok(
                medicalReportService.getSharedReport(
                        authentication.getName(),
                        patientId
                )
        );
    }

    @GetMapping("/shared-patients/{patientId}/imaging")
    public ResponseEntity<List<DiagnosticImageResponse>> getSharedPatientImaging(
            @PathVariable Long patientId,
            Authentication authentication) {

        return ResponseEntity.ok(
                diagnosticImageService.getSharedPatientImaging(
                        patientId,
                        authentication.getName()
                )
        );
    }


}

