package com.vivek.HospitalManagement.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SlotPaymentResponse {

    private Long appointmentId;

    private String orderId;

    private String paymentSessionId;

    private BigDecimal amount;

}
