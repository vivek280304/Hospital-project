package com.vivek.HospitalManagement.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class PaymentStatusResponse {

    private String status;
    private Long appointmentId;
    private BigDecimal amount;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
}