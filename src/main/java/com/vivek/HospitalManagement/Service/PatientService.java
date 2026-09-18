package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.PatientAppointmentResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientProfileResponse;
import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Entity.Patient;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Repository.PatientRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;


    public PatientService(UserRepository userRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository) {

        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
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
    public List<PatientAppointmentResponse> getAppointments(String email){

        List<Appointment> PatientAppointment = appointmentRepository.
                findByPatientUserEmailOrderByAppointmentDateAscAppointmentTimeAsc(email);

        return PatientAppointment.stream()
                .map(appointment-> new PatientAppointmentResponse(
                        appointment.getId(),
                        appointment.getPatient().getId(),
                        appointment.getPatient().getUser().getName(),
                        appointment.getDoctor().getUser().getName(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        appointment.getStatus()
                )).toList();
    }


}
