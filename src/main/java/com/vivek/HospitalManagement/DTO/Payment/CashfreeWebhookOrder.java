package com.vivek.HospitalManagement.DTO.Payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CashfreeWebhookOrder {

    private String order_id;

    private BigDecimal order_amount;

    private String order_currency;
}