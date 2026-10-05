package com.vivek.HospitalManagement.Controller.Payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivek.HospitalManagement.DTO.Payment.CashfreeWebhookRequest;
import com.vivek.HospitalManagement.Service.Payment.CashfreeWebhookService;
import com.vivek.HospitalManagement.Service.Payment.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final CashfreeWebhookService webhookService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentWebhookController(
            CashfreeWebhookService webhookService,
            PaymentService paymentService, ObjectMapper objectMapper
    ) {

        this.webhookService = webhookService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("x-webhook-signature") String signature,
            @RequestHeader("x-webhook-timestamp") String timestamp,
            @RequestBody String rawBody) {

        // 1. Verify Cashfree signature
        boolean valid = webhookService.verifySignature(
                signature,
                timestamp,
                rawBody
        );

        if (!valid) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid webhook signature");
        }

        try {

            // 2. Convert JSON → DTO
            CashfreeWebhookRequest webhook =
                    objectMapper.readValue(
                            rawBody,
                            CashfreeWebhookRequest.class
                    );

            String orderId =
                    webhook.getData()
                            .getOrder()
                            .getOrder_id();

            String paymentStatus =
                    webhook.getData()
                            .getPayment()
                            .getPayment_status();

            System.out.println("================================");
            System.out.println("Cashfree Webhook");
            System.out.println("Order ID: " + orderId);
            System.out.println("Payment Status: " + paymentStatus);
            System.out.println("================================");

            // 3. Only process successful payments
            if (!"SUCCESS".equalsIgnoreCase(paymentStatus)) {

                return ResponseEntity.ok(
                        "Payment status: " + paymentStatus
                );
            }

            // 4. Create appointment
            paymentService.processSuccessfulPayment(orderId);

            return ResponseEntity.ok(
                    "Payment processed successfully"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Webhook processing failed");
        }
    }
}