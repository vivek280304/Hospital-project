package com.vivek.HospitalManagement.Controller.Payment;


import com.vivek.HospitalManagement.DTO.Payment.CreatePaymentRequest;
import com.vivek.HospitalManagement.DTO.Payment.PaymentResponse;
import com.vivek.HospitalManagement.Service.Payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        PaymentResponse response =
                paymentService.createPayment(request);

        return ResponseEntity.ok(response);
    }
}