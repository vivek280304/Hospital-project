package com.vivek.HospitalManagement.DTO.Payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashfreeWebhookRequest {

    private String type;
    private CashfreeWebhookData data;
}