package com.vivek.HospitalManagement.DTO.Payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashfreeWebhookPayment {

    private String cf_payment_id;
    private String payment_status;
    private String payment_group;
}