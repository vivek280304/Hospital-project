package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.LabTechnicianProfileResponse;
import com.vivek.HospitalManagement.Entity.LabTechnician;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.LabTechnicianRepository;
import com.vivek.HospitalManagement.Repository.LabTestRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class LabTechnicianService {

    private final LabTechnicianRepository labTechnicianRepository;
    private final UserRepository userRepository;

    public LabTechnicianService(LabTechnicianRepository labTechnicianRepository, UserRepository userRepository) {
        this.labTechnicianRepository = labTechnicianRepository;
        this.userRepository = userRepository;
    }

    public LabTechnicianProfileResponse profile(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        LabTechnician labTechnician = labTechnicianRepository.findByUserId(user.getId())
                .orElseThrow(()-> new ResourceNotFoundException("LabTech not found"));

        return new LabTechnicianProfileResponse(labTechnician.getId(),
                                                user.getName(),
                                                user.getEmail());

    }
}
