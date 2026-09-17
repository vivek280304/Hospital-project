package com.vivek.HospitalManagement.Service.Auth;

import com.vivek.HospitalManagement.DTO.Auth.Request.ForgotPasswordRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.ResetPasswordRequest;
import com.vivek.HospitalManagement.Entity.PasswordResetOtp;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Repository.PasswordResetOtpRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetOtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService, RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void sendPasswordResetOtp(ForgotPasswordRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                        .orElse(null);

        if (user == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (user.getPasswordResetOtpWindowStart() == null
                || now.isAfter(
                user.getPasswordResetOtpWindowStart()
                        .plusMinutes(15))) {

            user.setPasswordResetOtpWindowStart(now);
            user.setPasswordResetOtpRequests(0);
        }

        if (user.getPasswordResetOtpRequests() >= 3) {
            throw new BadRequestException(
                    "Too many OTP requests. Please try again later."
            );
        }

        user.setPasswordResetOtpRequests(
                user.getPasswordResetOtpRequests() + 1
        );

        userRepository.save(user);

        // Invalidate previous OTPs
        otpRepository.invalidatePreviousOtps(user.getId());

        // Generate 6-digit OTP
        SecureRandom secureRandom = new SecureRandom();

        String otp = String.valueOf(
                100000 + secureRandom.nextInt(900000)
        );

        // Hash OTP before storing
        String otpHash =
                passwordEncoder.encode(otp);

        PasswordResetOtp resetOtp =
                new PasswordResetOtp();

        resetOtp.setUser(user);
        resetOtp.setOtpHash(otpHash);
        resetOtp.setAttempts(0);
        resetOtp.setUsed(false);
        resetOtp.setCreatedAt(now);
        resetOtp.setExpiresAt(
                now.plusMinutes(5)
        );

        otpRepository.save(resetOtp);

        // Send RAW OTP to email
        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException("Invalid email or OTP"));

        PasswordResetOtp resetOtp =
                otpRepository
                        .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new BadRequestException("Invalid or expired OTP"));

        // 1. Check expiry
        if (LocalDateTime.now().isAfter(resetOtp.getExpiresAt())) {
            throw new BadRequestException("OTP has expired");
        }

        // 2. Check maximum attempts
        if (resetOtp.getAttempts() >= 5) {
            throw new BadRequestException(
                    "Too many invalid OTP attempts"
            );
        }

        // 3. Verify OTP against B-Crypt hash
        if (!passwordEncoder.matches(
                request.getOtp(),
                resetOtp.getOtpHash()
        )) {

            resetOtp.setAttempts(
                    resetOtp.getAttempts() + 1
            );

            otpRepository.save(resetOtp);

            throw new BadRequestException("Invalid OTP");
        }

        // 4. Change password
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // 5. OTP can never be reused
        resetOtp.setUsed(true);
        otpRepository.save(resetOtp);

        // 6. Logout all existing sessions
        refreshTokenService.revokeAllUserTokens(user.getId());
    }
}