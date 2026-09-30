package com.vivek.HospitalManagement.Controller;

import com.vivek.HospitalManagement.DTO.Auth.Request.CreateUserRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.AdminDetailResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.RoleCountResponse;
import com.vivek.HospitalManagement.Service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/profile")
    public ResponseEntity<AdminDetailResponse> profile(Authentication authentication){

        return ResponseEntity.ok(
                adminService.myProfile(authentication.getName()
                ));
    }

    @PostMapping("/create-users")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request){

        adminService.create(request);

        return ResponseEntity.ok("User creation request received for role:" + request.getRole());

    }

    @GetMapping("/role-counts")
    public ResponseEntity<RoleCountResponse> getRoleCounts() {

        return ResponseEntity.ok(
                adminService.getRoleCounts()
        );
    }

    @PatchMapping("/users/{userId}/lock")
    public ResponseEntity<String> lockUser(
            @PathVariable Long userId) {

        adminService.lockUser(userId);

        return ResponseEntity.ok(
                "User account locked successfully"
        );
    }

    @PatchMapping("/users/{userId}/unlock")
    public ResponseEntity<String> unlockUser(
            @PathVariable Long userId) {

        adminService.unlockUser(userId);

        return ResponseEntity.ok(
                "User account unlocked successfully"
        );
    }
}
