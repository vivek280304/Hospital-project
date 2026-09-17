package com.vivek.HospitalManagement.DTO.Auth.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagingUploadRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

}