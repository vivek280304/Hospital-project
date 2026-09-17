package com.vivek.HospitalManagement.DTO.Auth.Request;

import com.vivek.HospitalManagement.Enums.ImagingType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagingOrderRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Imaging type is required")
    private ImagingType imagingType;
}