package com.vivek.HospitalManagement.Controller;


import com.vivek.HospitalManagement.DTO.Auth.Request.*;
import com.vivek.HospitalManagement.DTO.Auth.Response.LoginResponse;
import com.vivek.HospitalManagement.Service.Auth.AuthService;
import com.vivek.HospitalManagement.Service.Auth.OtpLoginService;
import com.vivek.HospitalManagement.Service.Auth.PasswordResetService;
import com.vivek.HospitalManagement.Service.Auth.RegistrationOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RegistrationOtpService registrationOtpService;
    private final OtpLoginService otpLoginService;


    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          RegistrationOtpService registrationOtpService,
                          OtpLoginService otpLoginService) {

        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.registrationOtpService = registrationOtpService;
        this.otpLoginService = otpLoginService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest){
        authService.register(registerRequest);
        return ResponseEntity.ok("User registered successfully!");

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@Valid @RequestBody LoginRequest loginRequest){

        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response =
                authService.refreshToken(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        authService.changePassword(email, request);

        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.sendPasswordResetOtp(request);

        return ResponseEntity.ok(
                "If the email is registered, an OTP has been sent"
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(
                "Password reset successfully"
        );
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<String> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationOtpRequest request) {

        registrationOtpService.verifyRegistrationOtp(request);

        return ResponseEntity.ok(
                "Email verified successfully. Account activated."
        );
    }

    @PostMapping("/resend-registration-otp")
    public ResponseEntity<String> resendRegistrationOtp(
            @Valid @RequestBody ResendRegistrationOtpRequest request) {

        registrationOtpService.resendRegistrationOtp(request);

        return ResponseEntity.ok(
                "If the email is registered and not verified, an OTP has been sent"
        );
    }

    @PostMapping("/otp/request")
    public ResponseEntity<String> requestOtp(
            @Valid @RequestBody OtpLoginRequest request) {

        otpLoginService.requestOtp(request);

        return ResponseEntity.ok(
                "If the account exists, a login OTP has been sent to the registered email"
        );
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<LoginResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpLoginRequest request) {

        return ResponseEntity.ok(
                otpLoginService.verifyOtp(request)
        );
    }

}
