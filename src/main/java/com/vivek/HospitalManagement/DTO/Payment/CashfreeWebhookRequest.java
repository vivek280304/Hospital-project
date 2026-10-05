package com.vivek.HospitalManagement.DTO.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashfreeWebhookRequest {

    private String type;
    private CashfreeWebhookData data;
}