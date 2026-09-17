package com.vivek.HospitalManagement.Service.Auth;

import com.vivek.HospitalManagement.DTO.Auth.Request.ChangePasswordRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.LoginRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.LoginResponse;
import com.vivek.HospitalManagement.DTO.Auth.Request.RegisterRequest;
import com.vivek.HospitalManagement.Entity.RefreshToken;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Enums.Role;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Repository.UserRepository;
import com.vivek.HospitalManagement.Security.CustomUserDetails;
import com.vivek.HospitalManagement.Security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RegistrationOtpService registrationOtpService;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenService refreshTokenService, RegistrationOtpService registrationOtpService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.registrationOtpService = registrationOtpService;
    }


    public void register(RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("User already Exist!");
        }

        String encoded = passwordEncoder.encode(registerRequest.getPassword());

        User user = new User();

        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoded);

        // Account is not active until email is verified
        user.setEnabled(false);

        user.setRole(Role.PATIENT);

        user.setEnabled(true);

        userRepository.save(user);

        registrationOtpService.sendRegistrationOtp(user);
    }


    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );


        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();


        User user = userRepository.findByEmail(
                loginRequest.getEmail()
        ).orElseThrow(() ->
                new BadCredentialsException("User not found"));

        assert userDetails != null;
        String accessToken =
                jwtService.generateToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                "Login Successful",
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken.getToken()
        );


    }
    @Transactional
    public LoginResponse refreshToken(String token) {

        RefreshToken newRefreshToken =
                refreshTokenService.rotateToken(token);

        User user = newRefreshToken.getUser();

        UserDetails userDetails =
                new CustomUserDetails(user);

        String accessToken =
                jwtService.generateToken(userDetails);

        return new LoginResponse(
                "Token refreshed successfully",
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                newRefreshToken.getToken()
        );
    }

    @Transactional
    public void logout(String refreshToken) {

        refreshTokenService.revokeToken(refreshToken);
    }

    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        ));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(user.getId());
    }
}
