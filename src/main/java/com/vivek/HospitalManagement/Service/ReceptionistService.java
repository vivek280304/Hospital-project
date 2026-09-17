package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.CreatePatientRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.AvailableSlotResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientDetailsResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.ReceptionistProfileResponse;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import com.vivek.HospitalManagement.Enums.Role;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.EmailSendingException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.*;
import com.vivek.HospitalManagement.Security.InitialPassword;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReceptionistService {

        private final ReceptionistRepository receptionistRepository;
        private final UserRepository userRepository;
        private final PatientRepository patientRepository;
        private final DoctorRepository doctorRepository;
        private final DoctorScheduleRepository doctorScheduleRepository;
        private final AppointmentRepository appointmentRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;
        private final InitialPassword initialPassword;

    private static final Logger log =
            LoggerFactory.getLogger(ReceptionistService.class);

    public ReceptionistService(ReceptionistRepository receptionistRepository, UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository, DoctorScheduleRepository doctorScheduleRepository, AppointmentRepository appointmentRepository, PasswordEncoder passwordEncoder, EmailService emailService, InitialPassword initialPassword) {
        this.receptionistRepository = receptionistRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.initialPassword = initialPassword;
    }



    public ReceptionistProfileResponse getMyProfile(String email) {


        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

         Receptionist receptionist =  receptionistRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Receptionist profile not found"));

        return new ReceptionistProfileResponse(

                receptionist.getUser().getName(),
                receptionist.getUser().getEmail()
                );
    }


    public List<PatientDetailsResponse> searchPatients(String query) {

        return patientRepository
                .findByUserNameContainingIgnoreCaseOrUserEmailContainingIgnoreCaseOrPhoneNumberContaining(
                        query,
                        query,
                        query
                )
                .stream()
                .map(patient -> new PatientDetailsResponse(
                        patient.getId(),
                        patient.getUser().getName(),
                        patient.getUser().getEmail(),
                        patient.getDateOfBirth(),
                        patient.getGender(),
                        patient.getPhoneNumber()
                ))
                .toList();
    }

    public List<DoctorSchedule> getDoctorSchedule(Long doctorId) {

        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found"));

        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    public List<AvailableSlotResponse> getAvailableSlots(
            Long doctorId,
            LocalDate date) {

        DayOfWeek day = date.getDayOfWeek();

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctorId(doctorId);

        List<Appointment> bookedAppointments =
                appointmentRepository
                        .findByDoctorIdAndAppointmentDateAndStatus(
                                doctorId,
                                date,
                                AppointmentStatus.BOOKED
                        );

        List<AvailableSlotResponse> availableSlots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {

            if (!schedule.getDayOfWeek().equals(day)) {
                continue;
            }

            LocalTime slotStart = schedule.getStartTime();

            while (!slotStart.plusMinutes(30)
                    .isAfter(schedule.getEndTime())) {

                LocalTime slotEnd = slotStart.plusMinutes(30);

                LocalTime finalSlotStart = slotStart;

                boolean booked = bookedAppointments.stream()
                        .anyMatch(appointment ->
                                appointment.getAppointmentTime()
                                        .equals(finalSlotStart)
                        );

                if (!booked) {
                    availableSlots.add(
                            new AvailableSlotResponse(
                                    slotStart,
                                    slotEnd
                            )
                    );
                }

                slotStart = slotEnd;
            }
        }

        return availableSlots;
    }

    @Transactional
    public PatientDetailsResponse createPatient(CreatePatientRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        String pass = initialPassword.generateInitialPassword();

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(pass));
        user.setRole(Role.PATIENT);
        user.setEnabled(true);

        userRepository.save(user);

        Patient patient = new Patient();

        patient.setUser(user);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhoneNumber(request.getPhoneNumber());

        patientRepository.save(patient);

        try {

            emailService.sendPatientAccountCreatedEmail(
                    user.getEmail(),
                    user.getName(),
                    pass
            );

        } catch (EmailSendingException e) {

            // Log the failure
            log.error(
                    "Patient {} created successfully but welcome email failed",
                    user.getEmail(), e
            );
        }

        return new PatientDetailsResponse(
                patient.getId(),
                user.getName(),
                user.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber()
        );
    }

//    BOOK APPOINTMENT



}
