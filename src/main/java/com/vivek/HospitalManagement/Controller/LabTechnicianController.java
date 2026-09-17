package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.ImagingUploadRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.ImagingOrderResponse;
import com.vivek.HospitalManagement.Entity.DiagnosticImage;
import com.vivek.HospitalManagement.Service.Reports.DiagnosticImageService;
import com.vivek.HospitalManagement.Service.Reports.ImagingOrderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lab-technician")
public class LabTechnicianController {

    private final DiagnosticImageService diagnosticImageService;
    private final ImagingOrderService imagingOrderService;


    public LabTechnicianController(DiagnosticImageService diagnosticImageService, ImagingOrderService imagingOrderService) {
        this.diagnosticImageService = diagnosticImageService;
        this.imagingOrderService = imagingOrderService;
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
}
