package com.vivek.HospitalManagement.DTO.Auth.Response;

import com.vivek.HospitalManagement.Enums.ImagingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ImagingOrderResponse {

    private Long orderId;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private ImagingType imagingType;
    private String status;
    private LocalDateTime createdAt;
}