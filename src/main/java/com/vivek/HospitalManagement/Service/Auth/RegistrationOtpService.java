package com.vivek.HospitalManagement.Service.Auth;

import com.vivek.HospitalManagement.DTO.Auth.Request.ResendRegistrationOtpRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.VerifyRegistrationOtpRequest;
import com.vivek.HospitalManagement.Entity.RegistrationOtp;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Repository.RegistrationOtpRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RegistrationOtpService {

    private final RegistrationOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationOtpService(
            RegistrationOtpRepository otpRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void sendRegistrationOtp(User user) {

        LocalDateTime now = LocalDateTime.now();

        // Check current OTP request window
        if (user.getRegistrationOtpWindowStart() == null
                || now.isAfter(
                user.getRegistrationOtpWindowStart()
                        .plusMinutes(15))) {

            // Start a new 15-minute window
            user.setRegistrationOtpWindowStart(now);
            user.setRegistrationOtpRequests(0);
        }

        // Maximum 3 requests in 15 minutes
        if (user.getRegistrationOtpRequests() >= 3) {
            throw new BadRequestException(
                    "Too many OTP requests. Please try again later."
            );
        }

        // Count this request
        user.setRegistrationOtpRequests(
                user.getRegistrationOtpRequests() + 1
        );

        userRepository.save(user);


        otpRepository.invalidatePreviousOtps(user.getId());

        SecureRandom secureRandom = new SecureRandom();

        String otp = String.valueOf(
                100000 + secureRandom.nextInt(900000)
        );

        RegistrationOtp registrationOtp = new RegistrationOtp();

        registrationOtp.setUser(user);
        registrationOtp.setOtpHash(
                passwordEncoder.encode(otp)
        );
        registrationOtp.setAttempts(0);
        registrationOtp.setUsed(false);
        registrationOtp.setCreatedAt(LocalDateTime.now());
        registrationOtp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRepository.save(registrationOtp);

        emailService.sendRegistrationOtp(user.getEmail(), otp);
    }

    @Transactional
    public void verifyRegistrationOtp(
            VerifyRegistrationOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid email or OTP"
                        ));

        RegistrationOtp registrationOtp =
                otpRepository
                        .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid or expired OTP"
                                ));

        // Check expiry
        if (LocalDateTime.now().isAfter(
                registrationOtp.getExpiresAt())) {

            throw new BadRequestException(
                    "OTP has expired"
            );
        }

        // Maximum 5 attempts
        if (registrationOtp.getAttempts() >= 5) {

            throw new BadRequestException(
                    "Too many invalid OTP attempts"
            );
        }

        // Verify OTP against BCrypt hash
        if (!passwordEncoder.matches(
                request.getOtp(),
                registrationOtp.getOtpHash())) {

            registrationOtp.setAttempts(
                    registrationOtp.getAttempts() + 1
            );

            otpRepository.save(registrationOtp);

            throw new BadRequestException(
                    "Invalid OTP"
            );
        }

        // OTP is correct
        registrationOtp.setUsed(true);
        otpRepository.save(registrationOtp);

        // Activate the account
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void resendRegistrationOtp(
            ResendRegistrationOtpRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        // Don't reveal whether the email exists
        if (user == null) {
            return;
        }

        // Already verified — no need to resend
        if (user.isEnabled()) {
            return;
        }

        sendRegistrationOtp(user);
    }
}