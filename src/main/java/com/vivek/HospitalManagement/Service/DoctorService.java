package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorProfileResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.DoctorResponse;
import com.vivek.HospitalManagement.Entity.Appointment;
import com.vivek.HospitalManagement.Entity.Doctor;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.AppointmentRepository;
import com.vivek.HospitalManagement.Repository.DoctorRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;


    public DoctorService(
            DoctorRepository doctorRepository,
            UserRepository userRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
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

    @Transactional
    public void completeAppointment(Long appointmentId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Appointment not found"));

        // Make sure this appointment belongs to this doctor
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException(
                    "You cannot complete this appointment"
            );
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cancelled appointment cannot be completed"
            );
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException(
                    "Appointment is already completed"
            );
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        // Slot is no longer occupied
        appointment.setBookingKey(null);

        appointmentRepository.save(appointment);
    }

    public List<DoctorResponse> findDoctorsBySpecialization(
            String specialization) {

        return doctorRepository.findDoctorsBySpecialization(
                specialization
        );
    }

}
