package com.vivek.HospitalManagement.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CashfreeCustomerDetails {

    private String customer_id;
    private String customer_phone;
}