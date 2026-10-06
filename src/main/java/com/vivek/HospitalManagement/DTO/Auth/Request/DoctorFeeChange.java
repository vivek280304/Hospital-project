package com.vivek.HospitalManagement.DTO.Auth.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorFeeChange {

    @NotBlank
    private String email;

    private BigDecimal amount;

}
