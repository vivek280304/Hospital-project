package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorProfileResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorResponse;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Repository.DoctorRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;


    public DoctorService(
            DoctorRepository doctorRepository,
            UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    public DoctorProfileResponse getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Doctor doctor =  doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Doctor profile not found"));

        return new DoctorProfileResponse(
                user.getName(),
                user.getEmail(),
                doctor.getLicenseNumber(),
                doctor.getSpecialization(),
                doctor.getExperience());
    }

    public List<DoctorResponse> findAvailableDoctors(
            DayOfWeek day,
            String specialization) {

        List<Doctor> doctors =
                doctorRepository.findAvailableDoctors(
                        day,
                        specialization
                );

        return doctors.stream()
                .map(doctor -> new DoctorResponse(
                        doctor.getId(),
                        doctor.getUser().getName(),
                        doctor.getSpecialization(),
                        doctor.getExperience()
                ))
                .toList();
    }


}
