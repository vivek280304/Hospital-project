package com.vivek.HospitalManagement.DTO.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashfreeWebhookData {

    private CashfreeWebhookOrder order;
    private CashfreeWebhookPayment payment;
}