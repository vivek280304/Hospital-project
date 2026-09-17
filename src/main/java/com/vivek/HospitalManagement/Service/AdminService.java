package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.CreateUserRequest;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.Role;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final LabTechnicianRepository labTechnicianRepository;
    private final AdminRepository adminRepository;
    private final ReceptionistRepository receptionistRepository;


    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder, DoctorRepository doctorRepository, NurseRepository nurseRepository, LabTechnicianRepository labTechnicianRepository, AdminRepository adminRepository, ReceptionistRepository receptionistRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.labTechnicianRepository = labTechnicianRepository;
        this.adminRepository = adminRepository;
        this.receptionistRepository = receptionistRepository;
    }

@Transactional
    public void create(CreateUserRequest request){

        if (userRepository.existsByEmail(request.getEmail())){
            throw new ResourceNotFoundException("User already exist");
        }

        User user = new User();

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(request.getRole());

        user.setEnabled(true);

        userRepository.save(user);

//        DOCTOR
        if (request.getRole() == Role.DOCTOR){

            if (request.getLicenseNumber() == null ||
                    request.getLicenseNumber().isBlank()) {
                throw new BadRequestException("License number is required for doctor");
            }

            if (request.getSpecialization() == null ||
                    request.getSpecialization().isBlank()) {
                throw new BadRequestException("Specialization is required for doctor");
            }

            if (request.getExperience() == null ||
                    request.getExperience() < 0) {
                throw new BadRequestException("Valid experience is required for doctor");
            }

            Doctor doctor = new Doctor();

            doctor.setUser(user);
            doctor.setLicenseNumber(request.getLicenseNumber());
            doctor.setExperience(request.getExperience());
            doctor.setSpecialization(request.getSpecialization());


            doctorRepository.save(doctor);
        }

//    NURSE
    if (request.getRole() == Role.NURSE){

        if (request.getLicenseNumber() == null ||
                request.getLicenseNumber().isBlank()) {
            throw new BadRequestException("License number is required for nurse");
        }

        if (request.getDepartment() == null ||
                request.getDepartment().isBlank()) {
            throw new BadRequestException("Specialization is required for nurse");
        }

        if (request.getExperience() == null ||
                request.getExperience() < 0) {
            throw new BadRequestException("Valid experience is required for nurse");
        }

        Nurse nurse = new Nurse();

        nurse.setUser(user);
        nurse.setExperience(request.getExperience());
        nurse.setDepartment(request.getDepartment());
        nurse.setLicenseNumber(request.getLicenseNumber());

        nurseRepository.save(nurse);
    }

//    LAB_TECHNICIAN
    if (request.getRole() == Role.LAB_TECHNICIAN) {

        if (request.getDepartment() == null ||
                request.getDepartment().isBlank()) {
            throw new BadRequestException("Department is required ");
        }

        LabTechnician labTechnician = new LabTechnician();

        labTechnician.setUser(user);
        labTechnician.setDepartment(request.getDepartment());

        labTechnicianRepository.save(labTechnician);
    }

//    ADMIN
    if (request.getRole() == Role.ADMIN) {

        Admin admin = new Admin();

        admin.setUser(user);

        adminRepository.save(admin);
    }

//    Receptionist
    if (request.getRole() == Role.RECEPTIONIST) {

        Receptionist receptionist = new Receptionist();

        receptionist.setUser(user);

        receptionistRepository.save(receptionist);
    }

    }
}
