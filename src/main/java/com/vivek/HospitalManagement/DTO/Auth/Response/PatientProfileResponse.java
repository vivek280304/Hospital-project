package com.vivek.HospitalManagement.DTO.Auth.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;


@Getter
@AllArgsConstructor
public class PatientProfileResponse {

        private Long id;
        private String name;
        private String email;
        private LocalDate dateOfBirth;
        private String gender;
        private String phoneNumber;
}
