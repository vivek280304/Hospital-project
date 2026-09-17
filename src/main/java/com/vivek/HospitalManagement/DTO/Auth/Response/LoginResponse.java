package com.vivek.HospitalManagement.DTO.Auth.Response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String message;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
}
