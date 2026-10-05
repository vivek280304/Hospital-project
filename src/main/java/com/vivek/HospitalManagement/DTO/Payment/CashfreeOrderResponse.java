package com.vivek.HospitalManagement.DTO.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CashfreeOrderResponse {

    @JsonProperty("cf_order_id")
    private String cfOrderId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("payment_session_id")
    private String paymentSessionId;

    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    @JsonProperty("order_currency")
    private String orderCurrency;
}