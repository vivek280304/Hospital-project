package com.vivek.HospitalManagement.Service.Auth;

import com.vivek.HospitalManagement.DTO.Auth.Request.OtpLoginRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.VerifyOtpLoginRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.LoginResponse;
import com.vivek.HospitalManagement.Entity.OtpLogin;
import com.vivek.HospitalManagement.Entity.RefreshToken;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Repository.OtpLoginRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import com.vivek.HospitalManagement.Security.JwtService;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpLoginService {

    private final UserRepository userRepository;
    private final OtpLoginRepository otpLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public OtpLoginService(UserRepository userRepository,
                           OtpLoginRepository otpLoginRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           RefreshTokenService refreshTokenService,
                           JwtService jwtService,
                           UserDetailsService userDetailsService) {

        this.userRepository = userRepository;
        this.otpLoginRepository = otpLoginRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public void requestOtp(OtpLoginRequest request){

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);


        if (user == null) {
            return;
        }

        otpLoginRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .ifPresent(existingOtp -> {
                    existingOtp.setUsed(true);
                    otpLoginRepository.save(existingOtp);
                });

        SecureRandom random = new SecureRandom();

        String otp = String.valueOf(
                100000 + random.nextInt(900000)
        );


        OtpLogin otpLogin = new OtpLogin();

        otpLogin.setUser(user);
        otpLogin.setOtpHash(passwordEncoder.encode(otp));
        otpLogin.setAttempts(0);
        otpLogin.setCreatedAt(LocalDateTime.now());
        otpLogin.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );
        otpLogin.setUsed(false);

        otpLoginRepository.saveAndFlush(otpLogin);

        emailService.sendLoginOtp(user.getEmail(), otp);

    }

    @Transactional
    public LoginResponse verifyOtp(VerifyOtpLoginRequest request){


        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException("Invalid email or OTP"));

        OtpLogin otpLogin = otpLoginRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                            user.getId())
                .orElseThrow(()-> new BadRequestException("Invalid or expired OTP"));


        if (!LocalDateTime.now().isBefore(otpLogin.getExpiresAt())) {
            throw new BadRequestException("OTP is expired");
        }

        if (otpLogin.getAttempts()>=5 ) {
            throw new BadRequestException(
                    "Too many invalid OTP attempts");
        }

        if (!passwordEncoder.matches(request.getOtp(),otpLogin.getOtpHash())) {

            otpLogin.setAttempts(
                    otpLogin.getAttempts() + 1
            );

            otpLoginRepository.save(otpLogin);

            throw new BadRequestException("Invalid OTP");

        }

        otpLogin.setUsed(true);
        otpLoginRepository.save(otpLogin);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken =
                jwtService.generateToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);


        return new LoginResponse(
                "OTP login successful",
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken.getToken()
        );
    }



}
