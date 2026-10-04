package com.vivek.HospitalManagement.Service.Payment;

import com.vivek.HospitalManagement.DTO.Payment.CashfreeCreateOrderReponse;
import com.vivek.HospitalManagement.DTO.Payment.CashfreeCreateOrderRequest;
import com.vivek.HospitalManagement.DTO.Payment.CashfreeCustomerDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;

@Service
public class CashfreeService {

    private final RestClient restClient;

    @Value("${cashfree.client-id}")
    private String clientId;

    @Value("${cashfree.client-secret}")
    private String clientSecret;

    @Value("${cashfree.base-url}")
    private String baseUrl;

    @Value("${cashfree.api-version}")
    private String apiVersion;

    public CashfreeService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CashfreeCreateOrderReponse createOrder(String orderId,
                                                  BigDecimal amount,
                                                  String customerId,
                                                  String customerPhone){


        CashfreeCustomerDetails customerDetails = new CashfreeCustomerDetails(customerId,customerPhone);

        CashfreeCreateOrderRequest requestBody  = new CashfreeCreateOrderRequest();
        requestBody.setOrder_id(orderId);
        requestBody.setOrder_amount(amount);
        requestBody.setOrder_currency("INR");
        requestBody.setCustomer_details(customerDetails);

        return restClient.post()
                .uri(baseUrl + "/orders")
                .header("x-client-id", clientId)
                .header("x-client-secret", clientSecret)
                .header("x-api-version", apiVersion)
                .header("x-request-id", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(CashfreeCreateOrderReponse.class);

    }
}
