package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.CreateUserRequest;
import com.vivek.HospitalManagement.Service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request){

        adminService.create(request);

        return ResponseEntity.ok("User creation request received for role:" + request.getRole());

    }

}
