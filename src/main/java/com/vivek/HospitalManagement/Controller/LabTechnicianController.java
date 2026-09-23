package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.CompleteLabTestRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.ImagingUploadRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.ImagingOrderResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.LabOrderResponse;
import com.vivek.HospitalManagement.Entity.DiagnosticImage;
import com.vivek.HospitalManagement.Enums.LabTestOrderStatus;
import com.vivek.HospitalManagement.Service.LabTestOrderService;
import com.vivek.HospitalManagement.Service.Reports.DiagnosticImageService;
import com.vivek.HospitalManagement.Service.Reports.ImagingOrderService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab-technician")
public class LabTechnicianController {

    private final DiagnosticImageService diagnosticImageService;
    private final ImagingOrderService imagingOrderService;
    private final LabTestOrderService labTestOrderService;


    public LabTechnicianController(DiagnosticImageService diagnosticImageService,
                                   ImagingOrderService imagingOrderService,
                                   LabTestOrderService labTestOrderService) {

        this.diagnosticImageService = diagnosticImageService;
        this.imagingOrderService = imagingOrderService;
        this.labTestOrderService = labTestOrderService;
    }


    @PostMapping(path = "/imaging-orders/{orderId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @PathVariable Long orderId,
            @RequestParam("file") MultipartFile file ,
            Authentication authentication) {

        ImagingUploadRequest request = new ImagingUploadRequest();

        request.setOrderId(orderId);

        DiagnosticImage image = diagnosticImageService.upload(
                request,
                file,
                authentication.getName());

        return ResponseEntity.ok(
                "Imaging uploaded successfully. ID: " + image.getId()
        );
    }

    @GetMapping("/imaging-orders/pending")
    public ResponseEntity<List<ImagingOrderResponse>> getPendingOrders(){

        return ResponseEntity.ok(imagingOrderService.getPendingOrders());
    }

    @GetMapping("/patient-orders")
    public ResponseEntity<List<LabOrderResponse>> getOrdersByStatus(
            @RequestParam LabTestOrderStatus status) {

        return ResponseEntity.ok(
                labTestOrderService.getOrdersByStatus(status)
        );
    }

    @PostMapping("/patient-orders/{orderId}/claim")
    public ResponseEntity<?> claimOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        labTestOrderService.claimOrder(
                orderId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(
                Map.of("message", "Order claimed successfully")
        );
    }

    @PostMapping("/patient-orders/{orderId}/collect-sample")
    public ResponseEntity<?> collectSample(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        labTestOrderService.collectSample(
                orderId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(
                Map.of("message", "Sample collected successfully")
        );
    }

    @PostMapping("/patient-orders/{orderId}/start-processing")
    public ResponseEntity<?> startProcessing(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        labTestOrderService.startProcessing(
                orderId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(
                Map.of("message", "Lab test processing started")
        );
    }

    @PostMapping("/orders/{orderId}/complete")
    public ResponseEntity<?> completeOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CompleteLabTestRequest request) {

        labTestOrderService.completeOrder(
                orderId,
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(
                Map.of("message", "Lab test completed successfully")
        );
    }

}
