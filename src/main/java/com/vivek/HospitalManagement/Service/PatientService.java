package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.PatientProfileResponse;
import com.vivek.HospitalManagement.Entity.Patient;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Repository.PatientRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;


    public PatientService(UserRepository userRepository, PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    public PatientProfileResponse getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Patient patient =  patientRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Doctor profile not found"));

        return new PatientProfileResponse(
                user.getName(),
                user.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber()
                );

    }


}
