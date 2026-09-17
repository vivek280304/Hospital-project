package com.vivek.HospitalManagement.DTO.Auth.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SharePatientRequest {

    @NotNull(message = "Junior doctor ID is required")
    private Long juniorDoctorID;

}
