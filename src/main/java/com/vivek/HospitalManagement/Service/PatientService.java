package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.PatientProfileRequest;
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

    public PatientProfileResponse createProfile( String email,PatientProfileRequest request){

        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found"));


        if (patientRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Patient profile already exists");
        }

        Patient patient = new Patient();

        patient.setUser(user);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhoneNumber(request.getPhoneNumber());

        patientRepository.save(patient);

        return new PatientProfileResponse(
                user.getName()
                ,user.getEmail()
                ,patient.getDateOfBirth()
                ,patient.getGender()
                ,patient.getPhoneNumber());

    }


}
