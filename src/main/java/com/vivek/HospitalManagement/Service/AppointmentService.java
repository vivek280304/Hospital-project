package com.vivek.HospitalManagement.Service;

import com.vivek.HospitalManagement.DTO.Auth.Request.BookAppointmentRequest;
import com.vivek.HospitalManagement.DTO.Auth.Request.CreateMedicalReportRequest;
import com.vivek.HospitalManagement.DTO.Auth.Response.AppointmentResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.MedicalReportResponse;
import com.vivek.HospitalManagement.DTO.Auth.Response.PatientDetailsResponse;
import com.vivek.HospitalManagement.Entity.*;
import com.vivek.HospitalManagement.Enums.AppointmentStatus;
import com.vivek.HospitalManagement.Exceptions.AlreadyExistException;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Exceptions.SlotAlreadyBookedException;
import com.vivek.HospitalManagement.Repository.*;
import com.vivek.HospitalManagement.Service.NotificationService.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final EmailService emailService;


    public AppointmentService(UserRepository userRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorScheduleRepository doctorScheduleRepository, AppointmentRepository appointmentRepository, MedicalReportRepository medicalReportRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void bookAppointment(
            String email,
            BookAppointmentRequest request) {

        // 1. Find logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2. Find or create patient profile
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseGet(() -> {

                    Patient newPatient = new Patient();

                    newPatient.setUser(user);
                    newPatient.setDateOfBirth(request.getDateOfBirth());
                    newPatient.setGender(request.getGender());
                    newPatient.setPhoneNumber(request.getPhoneNumber());

                    return patientRepository.save(newPatient);
                });

        // 3. Find doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found"));

        // 4. Check doctor's schedule
        DayOfWeek day =
                request.getAppointmentDate().getDayOfWeek();

        DoctorSchedule schedule = doctorScheduleRepository
                .findByDoctorId(doctor.getId())
                .stream()
                .filter(s ->
                        s.getDayOfWeek().equals(day)
                )
                .filter(s ->
                        !request.getAppointmentTime()
                                .isBefore(s.getStartTime())
                                &&
                                !request.getAppointmentTime()
                                        .plusMinutes(s.getSlotDuration())
                                        .isAfter(s.getEndTime())
                )
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "Doctor is not available at this time"
                        ));

        // 5. Check double booking
        boolean alreadyBooked =
                appointmentRepository
                        .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                                doctor.getId(),
                                request.getAppointmentDate(),
                                request.getAppointmentTime(),
                                AppointmentStatus.BOOKED
                        );

        if (alreadyBooked) {
            throw new SlotAlreadyBookedException(
                    "This time slot is already booked"
            );
        }

        // 6. Generate unique booking key
        String bookingKey =
                doctor.getId() + "-" +
                        request.getAppointmentDate() + "-" +
                        request.getAppointmentTime();

        // 7. Create appointment
        Appointment appointment = new Appointment();

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(
                request.getAppointmentDate()
        );
        appointment.setAppointmentTime(
                request.getAppointmentTime()
        );
        appointment.setReason(request.getReason());
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setBookingKey(bookingKey);

        appointmentRepository.save(appointment);

        // 8. Appointment email
        // Keep disabled during load testing.

    emailService.sendAppointmentBookedEmail(
            patient.getUser().getEmail(),
            patient.getUser().getName(),
            doctor.getUser().getName(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getReason()
    );

    }

    public void cancelAppointment(Long appointmentId, String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(()->new ResourceNotFoundException("Patient not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(()-> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())){
            throw new BadRequestException("You cannot cancel this appointment");

        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED){
            throw new BadRequestException("Appointment already canceled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException(
                    "Completed appointment cannot be cancelled"
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        appointment.setBookingKey(null);

        appointmentRepository.save(appointment);

    }

    public List<LocalTime> getAvailableSlots(
            Long doctorId,
            LocalDate date) {

        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found"));

        DayOfWeek day = date.getDayOfWeek();

        DoctorSchedule schedule = doctorScheduleRepository
                .findByDoctorId(doctorId)
                .stream()
                .filter(s -> s.getDayOfWeek().equals(day))
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "Doctor does not work on this day"));

        List<Appointment> appointments =
                appointmentRepository
                        .findByDoctorIdAndAppointmentDateAndStatus(
                                doctorId,
                                date,
                                AppointmentStatus.BOOKED
                        );

        Set<LocalTime> bookedTimes = appointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        List<LocalTime> availableSlots = new ArrayList<>();

        int duration = schedule.getSlotDuration();

        LocalTime slot = schedule.getStartTime();

        while (!slot.plusMinutes(duration)
                .isAfter(schedule.getEndTime())) {

            if (!bookedTimes.contains(slot)) {
                availableSlots.add(slot);
            }

            slot = slot.plusMinutes(duration);
        }

        return availableSlots;
    }

    public List<AppointmentResponse> getDoctorAppointments(
            String email,
            LocalDate date) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        List<Appointment> appointments =
                appointmentRepository
                        .findByDoctorIdAndAppointmentDate(
                                doctor.getId(),
                                date
                        );

        return appointments.stream()
                .map(appointment ->
                        new AppointmentResponse(
                                appointment.getId(),
                                appointment.getPatient().getId(),
                                appointment.getPatient().getUser().getName(),
                                appointment.getAppointmentDate(),
                                appointment.getAppointmentTime(),
                                appointment.getStatus(),
                                appointment.getReason()
                        )
                )
                .toList();
    }

    public PatientDetailsResponse getPatientDetails(
            String email,
            Long appointmentId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Appointment not found"));

        // Security check
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to access this patient"
            );
        }

        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();

        return new PatientDetailsResponse(
                patient.getId(),
                patientUser.getName(),
                patientUser.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber()
        );
    }

    @Transactional
    public MedicalReportResponse createMedicalReport(
            String email,
            Long appointmentId,
            CreateMedicalReportRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Appointment not found"));

        // Security check
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to create a report for this appointment"
            );
        }

        // One appointment = one report
        if (medicalReportRepository
                .findByAppointmentId(appointmentId)
                .isPresent()) {

            throw new AlreadyExistException(
                    "Medical report already exists for this appointment"
            );
        }

        MedicalReport report = new MedicalReport();

        report.setAppointment(appointment);
        report.setPatient(appointment.getPatient());
        report.setDoctor(doctor);

        report.setDiagnosis(request.getDiagnosis());
        report.setSymptoms(request.getSymptoms());
        report.setTreatment(request.getTreatment());
        report.setPrescription(request.getPrescription());
        report.setNotes(request.getNotes());

        report.setCreatedAt(LocalDateTime.now());

        medicalReportRepository.save(report);

        return new MedicalReportResponse(
                report.getId(),
                appointment.getId(),
                report.getPatient().getId(),
                report.getDoctor().getId(),
                report.getDiagnosis(),
                report.getSymptoms(),
                report.getTreatment(),
                report.getPrescription(),
                report.getNotes(),
                report.getCreatedAt()
        );
    }



}
