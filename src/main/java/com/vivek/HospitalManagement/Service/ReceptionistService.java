package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.BookAppointmentRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.CreatePatientRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.DoctorLeaveRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.*;
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
        private final AppointmentService appointmentService;
        private final DoctorLeaveRepository doctorLeaveRepository;


    private static final Logger log =
            LoggerFactory.getLogger(ReceptionistService.class);

    public ReceptionistService(ReceptionistRepository receptionistRepository,
                               UserRepository userRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               DoctorScheduleRepository doctorScheduleRepository,
                               AppointmentRepository appointmentRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService emailService,
                               InitialPassword initialPassword,
                               AppointmentService appointmentService,
                               DoctorLeaveRepository doctorLeaveRepository) {

        this.receptionistRepository = receptionistRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.initialPassword = initialPassword;
        this.appointmentService = appointmentService;
        this.doctorLeaveRepository = doctorLeaveRepository;
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

        // Check doctor exists
        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found"));

        // Check if doctor is on leave
        boolean doctorOnLeave =
                doctorLeaveRepository.existsByDoctorIdAndLeaveDate(
                        doctorId,
                        date
                );

        // Doctor is unavailable for the entire day
        if (doctorOnLeave) {
            return new ArrayList<>();
        }

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

        List<AvailableSlotResponse> availableSlots =
                new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {

            if (!schedule.getDayOfWeek().equals(day)) {
                continue;
            }

            LocalTime slotStart = schedule.getStartTime();

            while (!slotStart.plusMinutes(30)
                    .isAfter(schedule.getEndTime())) {

                LocalTime slotEnd =
                        slotStart.plusMinutes(30);

                LocalTime finalSlotStart = slotStart;

                boolean booked =
                        bookedAppointments.stream()
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
@Transactional
public void bookAppointmentForPatient(
        Long patientId,
        BookAppointmentRequest request) {

    Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Patient not found"));

    String patientEmail = patient.getUser().getEmail();

    appointmentService.bookAppointment(
            patientEmail,
            request
    );
}

    public List<ReceptionistAppointmentResponse> getAppointments(
            LocalDate date) {

        List<Appointment> appointments =
                appointmentRepository
                        .findByAppointmentDateOrderByAppointmentTimeAsc(date);

        return appointments.stream()
                .map(appointment ->
                        new ReceptionistAppointmentResponse(

                                appointment.getId(),

                                appointment.getPatient().getId(),
                                appointment.getPatient()
                                        .getUser()
                                        .getName(),

                                appointment.getDoctor().getId(),
                                appointment.getDoctor()
                                        .getUser()
                                        .getName(),

                                appointment.getDoctor()
                                        .getSpecialization(),

                                appointment.getAppointmentDate(),
                                appointment.getAppointmentTime(),

                                appointment.getStatus(),

                                appointment.getReason()
                        )
                )
                .toList();
    }

    @Transactional
    public DoctorLeaveResponse createDoctorLeave(
            Long doctorId,
            DoctorLeaveRequest request
    ) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );

        LocalDate leaveDate = request.getLeaveDate();

        // Do not allow past dates
        if (leaveDate.isBefore(LocalDate.now())) {
            throw new BadRequestException(
                    "Leave date cannot be in the past"
            );
        }

        // Check duplicate leave
        if (doctorLeaveRepository.existsByDoctorIdAndLeaveDate(
                doctorId,
                leaveDate
        )) {
            throw new BadRequestException(
                    "Doctor already has leave on this date"
            );
        }

        DoctorLeave leave = new DoctorLeave();

        leave.setDoctor(doctor);
        leave.setLeaveDate(leaveDate);
        leave.setReason(request.getReason());

        DoctorLeave saved =
                doctorLeaveRepository.save(leave);

        return new DoctorLeaveResponse(
                saved.getId(),
                doctor.getId(),
                doctor.getUser().getName(),
                saved.getLeaveDate(),
                saved.getReason()
        );
    }

    public List<DoctorLeaveResponse> getDoctorLeaves(
            Long doctorId
    ) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );

        return doctorLeaveRepository
                .findByDoctorIdOrderByLeaveDateAsc(doctorId)
                .stream()
                .map(leave ->
                        new DoctorLeaveResponse(
                                leave.getId(),
                                doctor.getId(),
                                doctor.getUser().getName(),
                                leave.getLeaveDate(),
                                leave.getReason()
                        )
                )
                .toList();
    }

    @Transactional
    public void removeDoctorLeave(
            Long doctorId,
            LocalDate date
    ) {

        // Check doctor exists
        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found"
                        )
                );

        DoctorLeave leave =
                doctorLeaveRepository
                        .findByDoctorIdAndLeaveDate(
                                doctorId,
                                date
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Doctor leave not found for this date"
                                )
                        );

        doctorLeaveRepository.delete(leave);
    }

}
