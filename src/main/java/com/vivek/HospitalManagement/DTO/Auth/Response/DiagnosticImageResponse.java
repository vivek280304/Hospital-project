package com.vivek.HospitalManagement.DTO.Auth.Response;

import com.vivek.HospitalManagement.Enums.ImagingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiagnosticImageResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private Long uploadedBy;
    private String uploadedByName;
    private ImagingType imagingType;
    private String fileName;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}