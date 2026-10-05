package com.vivek.HospitalManagement.DTO.Payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashfreeOrderMeta {

    private String notify_url;
    private String return_url;
}