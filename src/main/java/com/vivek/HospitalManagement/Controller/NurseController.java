package com.vivek.HospitalManagement.Controller;
import com.vivek.HospitalManagement.DTO.Auth.Response.DiagnosticImageResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.MedicalReportResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.NurseProfileResponse;
import com.vivek.HospitalManagement.Entity.DiagnosticImage;
import com.vivek.HospitalManagement.Service.Reports.DiagnosticImageService;
import com.vivek.HospitalManagement.Service.Reports.MedicalReportService;
import com.vivek.HospitalManagement.Service.Reports.MinioStorageService;
import com.vivek.HospitalManagement.Service.NurseService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/nurse")
public class NurseController {

    private final NurseService nurseService;
    private final MedicalReportService medicalReportService;
    private final DiagnosticImageService diagnosticImageService;
    private final MinioStorageService minioStorageService;

    public NurseController(NurseService nurseService, MedicalReportService medicalReportService, DiagnosticImageService diagnosticImageService, MinioStorageService minioStorageService) {
        this.nurseService = nurseService;
        this.medicalReportService = medicalReportService;
        this.diagnosticImageService = diagnosticImageService;
        this.minioStorageService = minioStorageService;
    }

    @GetMapping("/profile")
    public ResponseEntity<NurseProfileResponse> getProfile(Authentication authentication){

        String email = authentication.getName();
        NurseProfileResponse response = nurseService.getProfile(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{patientId}/reports")
    public ResponseEntity<List<MedicalReportResponse>> getPatientReports(
            @PathVariable Long patientId) {


        return ResponseEntity.ok(
                medicalReportService
                        .getPatientReportsForNurse(patientId)
        );
    }

    @GetMapping("/patients/{patientId}/imaging")
    public ResponseEntity<List<DiagnosticImageResponse>> getPatientImaging(@PathVariable Long patientID){

        return ResponseEntity.ok(
                diagnosticImageService.getPatientImagingForNurse(patientID)
        );
    }

    @GetMapping("/imaging/{imageId}/view")
    public ResponseEntity<InputStreamResource> viewImage(@PathVariable Long imageID){

        DiagnosticImage image = diagnosticImageService.getImageForNurse(imageID);

        InputStream inputStream =
                minioStorageService.getFile(
                        image.getStorageKey()
                );

        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(
                image.getContentType()
        ))
                .contentLength(image.getFileSize())
                .body(inputStreamResource);

    }
}
