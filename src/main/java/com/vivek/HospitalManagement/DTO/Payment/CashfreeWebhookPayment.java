package com.vivek.HospitalManagement.DTO.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashfreeWebhookPayment {

    private String cf_payment_id;
    private String payment_status;
    private String payment_group;
}