package com.vivek.HospitalManagement.Service.Payment;

import com.vivek.HospitalManagement.DTO.Payment.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.UUID;

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

    @Value("${cashfree.webhook-url}")
    private String webhookUrl;

    public CashfreeService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CashfreeCreateOrderReponse createOrder(
            String orderId,
            BigDecimal amount,
            String customerId,
            String customerPhone
    ) {

        System.out.println("========== CASHFREE DEBUG ==========");
        System.out.println("Base URL: " + baseUrl);
        System.out.println("API Version: " + apiVersion);
        System.out.println("Client ID present: "
                + (clientId != null && !clientId.isBlank()));
        System.out.println("Client Secret present: "
                + (clientSecret != null && !clientSecret.isBlank()));
        System.out.println("=====================================");

        CashfreeCustomerDetails customerDetails =
                new CashfreeCustomerDetails(customerId, customerPhone);

        CashfreeCreateOrderRequest requestBody =
                new CashfreeCreateOrderRequest();

        requestBody.setOrder_id(orderId);
        requestBody.setOrder_amount(amount);
        requestBody.setOrder_currency("INR");
        requestBody.setCustomer_details(customerDetails);

        CashfreeOrderMeta orderMeta = new CashfreeOrderMeta();
        orderMeta.setNotify_url(webhookUrl);
        requestBody.setOrder_meta(orderMeta);

        try {
            return restClient.post()
                    .uri(baseUrl + "/orders")
                    .header("x-client-id", clientId)
                    .header("x-client-secret", clientSecret)
                    .header("x-api-version", apiVersion)
                    .header("x-request-id", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(CashfreeCreateOrderReponse.class);

        } catch (org.springframework.web.client.RestClientResponseException e) {
            System.err.println("Cashfree HTTP status: "
                    + e.getStatusCode());
            System.err.println("Cashfree response: "
                    + e.getResponseBodyAsString());

            throw e;
        }
    }

    public CashfreeOrderResponse getOrderStatus(String orderId) {

        return restClient.get()
                .uri(baseUrl + "/orders/" + orderId)
                .header("x-client-id", clientId)
                .header("x-client-secret", clientSecret)
                .header("x-api-version", apiVersion)
                .header("x-request-id", UUID.randomUUID().toString())
                .retrieve()
                .body(CashfreeOrderResponse.class);
    }
}
