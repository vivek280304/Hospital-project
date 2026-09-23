package com.vivek.HospitalManagement.DTO.Auth.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompleteLabTestRequest {

    @NotBlank(message = "Result is required")
    private String result;

    private String remarks;
}
