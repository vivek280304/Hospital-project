package com.vivek.HospitalManagement.Config;

import com.vivek.HospitalManagement.Exceptions.SecurityExceptionHandler;
import com.vivek.HospitalManagement.Security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, SecurityExceptionHandler securityExceptionHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth->
                                auth.requestMatchers( "/api/auth/register",
                                                "/api/auth/login",
                                                "/api/auth/refresh",
                                                "/api/auth/logout",
                                                "/api/auth/otp/request",
                                                "/api/auth/otp/verify",
                                                "/api/auth/forgot-password",
                                                "/api/auth/reset-password",
                                                "/api/auth/verify-registration",
                                                "/api/auth/resend-registration-otp"
                                                )

                                .permitAll()
                                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                                        .requestMatchers("/api/nurse/**").hasRole("NURSE")
                                        .requestMatchers("/api/lab-technician/**").hasRole("LAB_TECHNICIAN")
                                        .requestMatchers("/api/receptionist/**").hasRole("RECEPTIONIST")
                                        .anyRequest().authenticated())

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                securityExceptionHandler
                        ) .accessDeniedHandler(
                                securityExceptionHandler
                        ))

                                .addFilterBefore(
                                        jwtAuthenticationFilter,
                                    UsernamePasswordAuthenticationFilter.class
                                 );

        return httpSecurity.build();
    }


}
