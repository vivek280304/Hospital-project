package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.BookLabTestRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.LabTestResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabOrderResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientLabResultResponse;
import com.vivek.HospitalManagement.Service.LabTestOrderService;
import com.vivek.HospitalManagement.Service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/lab-tests")
@RequiredArgsConstructor
public class PatientLabTestController {

    private final LabTestService labTestService;
    private final LabTestOrderService labTestOrderService;

    @GetMapping
    public ResponseEntity<List<LabTestResponse>> getAvailableTests() {
        return ResponseEntity.ok(
                labTestService.getActiveTests()
        );
    }

    @PostMapping("/orders")
    public ResponseEntity<?> bookLabTest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookLabTestRequest request) {

        labTestOrderService.bookLabTest(
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(
                Map.of("message", "Lab test booked successfully")
        );
    }

    @GetMapping("/get-orders")
    public ResponseEntity<List<PatientLabOrderResponse>> getMyLabOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                labTestOrderService.getMyLabOrders(
                        userDetails.getUsername()
                )
        );
    }

    @GetMapping("/get-orders/{orderId}/result")
    public ResponseEntity<PatientLabResultResponse> getMyLabResult(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                labTestOrderService.getPatientResult(
                        orderId,
                        userDetails.getUsername()
                )
        );
    }
}