package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.NurseProfileResponse;
import com.vivek.HospitalManagement.Entity.Nurse;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Repository.NurseRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
 public class NurseService {

    private final NurseRepository nurseRepository;
    private final UserRepository userRepository;

    public NurseService(NurseRepository nurseRepository, UserRepository userRepository) {
        this.nurseRepository = nurseRepository;
        this.userRepository = userRepository;
    }


    public NurseProfileResponse getProfile(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));


        Nurse nurse = nurseRepository.findByUserId(user.getId())
                .orElseThrow(()-> new  RuntimeException("Nurse not found of id" ));


        return new NurseProfileResponse(
                 user.getName()
                ,user.getEmail()
                ,nurse.getLicenseNumber()
                ,nurse.getDepartment()
                ,nurse.getExperience());
    }
}
